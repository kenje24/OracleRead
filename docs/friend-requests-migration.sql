-- OracleRead friend request migration.
-- Run this in Supabase SQL Editor if your project already has the older Community schema.

create extension if not exists pgcrypto;

create or replace function public.touch_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

create table if not exists public.friend_requests (
    id uuid primary key default gen_random_uuid(),
    requester_id uuid not null references public.profiles(id) on delete cascade default auth.uid(),
    recipient_id uuid not null references public.profiles(id) on delete cascade,
    status text not null default 'pending' check (status in ('pending', 'accepted', 'declined')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (requester_id, recipient_id),
    check (requester_id <> recipient_id)
);

alter table public.community_notifications
    add column if not exists friend_request_id uuid references public.friend_requests(id) on delete cascade;

do $$
begin
    alter table public.community_notifications drop constraint if exists community_notifications_type_check;
    alter table public.community_notifications
        add constraint community_notifications_type_check
        check (type in ('mention', 'reply', 'friend', 'friend_request', 'post', 'comment'));
end $$;

create index if not exists friend_requests_requester_idx on public.friend_requests(requester_id, status);
create index if not exists friend_requests_recipient_idx on public.friend_requests(recipient_id, status);
create index if not exists community_notifications_friend_request_idx on public.community_notifications(friend_request_id);

create or replace function public.create_post_notification()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    community_title text;
begin
    select title into community_title from public.communities where id = new.community_id;

    insert into public.community_notifications (user_id, actor_id, type, title, body, post_id)
    select
        member.user_id,
        new.author_id,
        'post',
        'New discussion in ' || coalesce(community_title, 'a community'),
        left(new.title, 160),
        new.id
    from public.community_members member
    where member.community_id = new.community_id
      and member.user_id <> new.author_id
      and member.is_banned = false;

    return new;
end;
$$;

create or replace function public.create_reply_notification()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    parent_author uuid;
    post_author uuid;
begin
    if new.parent_comment_id is not null then
        select author_id into parent_author from public.comments where id = new.parent_comment_id;
    end if;

    if parent_author is not null and parent_author <> new.author_id then
        insert into public.community_notifications (user_id, actor_id, type, title, body, post_id, comment_id)
        values (parent_author, new.author_id, 'reply', 'Someone replied to your comment', left(new.content, 160), new.post_id, new.id);
    elsif new.parent_comment_id is null then
        select author_id into post_author from public.posts where id = new.post_id;
        if post_author is not null and post_author <> new.author_id then
            insert into public.community_notifications (user_id, actor_id, type, title, body, post_id, comment_id)
            values (post_author, new.author_id, 'comment', 'Someone commented on your discussion', left(new.content, 160), new.post_id, new.id);
        end if;
    end if;
    return new;
end;
$$;

drop trigger if exists posts_notification on public.posts;
create trigger posts_notification after insert on public.posts
for each row execute function public.create_post_notification();

drop trigger if exists comments_reply_notification on public.comments;
create trigger comments_reply_notification after insert on public.comments
for each row execute function public.create_reply_notification();

drop trigger if exists friend_requests_touch_updated_at on public.friend_requests;
create trigger friend_requests_touch_updated_at before update on public.friend_requests for each row execute function public.touch_updated_at();

create or replace function public.accept_friend_request(target_request_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    requester uuid;
    recipient uuid;
begin
    update public.friend_requests
    set status = 'accepted', updated_at = now()
    where id = target_request_id
      and recipient_id = auth.uid()
      and status = 'pending'
    returning requester_id, recipient_id into requester, recipient;

    if requester is null or recipient is null then
        raise exception 'Friend request not found or already handled.';
    end if;

    insert into public.friends (user_id, friend_id)
    values (requester, recipient), (recipient, requester)
    on conflict (user_id, friend_id) do nothing;

    update public.community_notifications
    set is_read = true
    where friend_request_id = target_request_id
      and user_id = auth.uid();

    insert into public.community_notifications (user_id, actor_id, type, title, body, friend_request_id)
    values (requester, recipient, 'friend', 'Friend request accepted', 'You are now friends.', target_request_id);
end;
$$;

grant execute on function public.accept_friend_request(uuid) to authenticated;

alter table public.friend_requests enable row level security;

drop policy if exists "friend requests participants read" on public.friend_requests;
drop policy if exists "friend requests create self" on public.friend_requests;
drop policy if exists "friend requests recipient update" on public.friend_requests;
drop policy if exists "friend requests requester resend" on public.friend_requests;
drop policy if exists "friend requests requester delete" on public.friend_requests;

create policy "friend requests participants read" on public.friend_requests for select to authenticated
using (requester_id = auth.uid() or recipient_id = auth.uid());
create policy "friend requests create self" on public.friend_requests for insert to authenticated
with check (requester_id = auth.uid() and status = 'pending');
create policy "friend requests recipient update" on public.friend_requests for update to authenticated
using (recipient_id = auth.uid())
with check (recipient_id = auth.uid() and status in ('accepted', 'declined'));
create policy "friend requests requester resend" on public.friend_requests for update to authenticated
using (requester_id = auth.uid())
with check (requester_id = auth.uid() and status = 'pending');
create policy "friend requests requester delete" on public.friend_requests for delete to authenticated
using (requester_id = auth.uid());
