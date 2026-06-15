# OracleRead Community Setup

OracleRead Community uses Supabase Auth, Postgres, Realtime-ready tables, and Storage for avatars. The Android app only needs the public URL and a publishable key. Legacy Supabase projects may call this the anon key.

1. Create a Supabase project.
2. Open the Supabase SQL Editor and run the contents of `supabase.db`.
3. Enable email magic links in Supabase Authentication.
4. Create a private `avatars` storage bucket if you want profile avatars.
5. Build OracleRead with:

```powershell
$env:SUPABASE_URL="https://your-project.supabase.co"
$env:SUPABASE_PUBLISHABLE_KEY="your-publishable-key"
.\gradlew.bat :app:assembleDebug
```

`SUPABASE_ANON_KEY` is still accepted for older projects, but you do not need both. Do not use or ship the service role key or an `sb_secret_...` key. The database RLS policies enforce ownership, voting uniqueness, reporting, and community moderator/admin permissions.

The Community button appears on each manga details page. Opening it checks Supabase for an existing manga community by stable local manga identifier and slug. If none exists, authenticated users can create `oracle/<slug>` and become the initial moderator/admin through database triggers.

Friend requests require the latest `supabase.db` schema. Run the updated SQL in the Supabase SQL Editor so the project has `friend_requests`, the `friend_request_id` notification column, and the `accept_friend_request` RPC. Users now become friends only after the receiver accepts the request from Notifications or the sender's profile.
