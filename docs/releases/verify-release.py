"""Verify local Android release APKs; optionally compare published GitHub assets.

Requires Java, Android SDK build-tools, and (with --remote) authenticated gh.
No signing key or password is read by this verifier.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys

CERT = 'f454c26d3753e5bd096b1f22ec5f1ef0296ffa53eaea8b55b7e960edd1acddb8'
REPO = 'kenje24/OracleRead'
ABIS = ['', '-arm64-v8a', '-armeabi-v7a', '-x86', '-x86_64']

def run(args):
    p = subprocess.run([str(x) for x in args], capture_output=True, text=True, encoding='utf8', errors='replace')
    if p.returncode:
        raise RuntimeError(f'{Path(str(args[0])).name} exited {p.returncode}: {p.stderr[-2000:]}')
    return p.stdout

def verify(version, root, remote=False):
    sdk = Path(os.environ.get('ANDROID_HOME', os.environ.get('ANDROID_SDK_ROOT', str(Path.home()/'AppData/Local/Android/Sdk'))))
    tools = sorted((sdk/'build-tools').iterdir(), key=lambda p: [int(x) if x.isdigit() else 0 for x in p.name.split('.')])[-1]
    java = Path(os.environ['JAVA_HOME'])/'bin/java.exe' if os.environ.get('JAVA_HOME') else 'java'
    aapt = tools/('aapt2.exe' if os.name == 'nt' else 'aapt2')
    apkdir = root/'app/build/outputs/apk/release'
    actual = sorted(apkdir.glob(f'OracleRead-release-{version}*.apk'))
    expected = {f'OracleRead-release-{version}{abi}.apk' for abi in ABIS}
    assert {p.name for p in actual} == expected, f'APK variants differ: found {[p.name for p in actual]}'
    result = {'version': version, 'certificateSha256': CERT, 'assets': []}
    version_code = None
    for p in actual:
        cert = run([java, '-jar', tools/'lib/apksigner.jar', 'verify', '--print-certs', p])
        assert f'certificate SHA-256 digest: {CERT}' in cert, f'Wrong release certificate: {p.name}'
        meta = run([aapt, 'dump', 'badging', p])
        pkg = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", meta)
        assert pkg, f'No APK package metadata: {p.name}'
        assert pkg[1] == 'io.github.kenje24.oracleread', f'Unexpected application ID: {pkg[1]}'
        assert pkg[3] == version, f'Unexpected versionName: {pkg[3]}'
        version_code = version_code or pkg[2]
        assert pkg[2] == version_code, 'Variant version codes differ'
        result['assets'].append({'name':p.name,'size':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
    result['versionCode'] = int(version_code)
    if version == '1.4.1':
        assert result['versionCode'] == 1410, f'Wrong update versionCode: {version_code}'
    if remote:
        release = json.loads(run(['gh','release','view','v'+version,'-R',REPO,'--json','assets,url,isDraft,isPrerelease,tagName']))
        assert not release['isDraft'] and not release['isPrerelease'], 'Release is draft or prerelease'
        assets = {a['name']:a for a in release['assets']}
        for local in result['assets']:
            target = assets[local['name']]
            assert target['state'] == 'uploaded', f'Asset not uploaded: {local["name"]}'
            assert target['size'] == local['size'], f'Size mismatch: {local["name"]}'
            assert target.get('digest') == 'sha256:'+local['sha256'], f'Digest mismatch: {local["name"]}'
        head = run(['git','-C',root,'rev-parse','HEAD']).strip()
        refs = run(['git','-C',root,'ls-remote','origin','refs/heads/main',f'refs/tags/v{version}',f'refs/tags/v{version}^{{}}'])
        pairs = {line.split()[1]:line.split()[0] for line in refs.splitlines()}
        assert pairs['refs/heads/main'] == head, 'Remote main differs from verified HEAD'
        tag = pairs.get(f'refs/tags/v{version}^{{}}',pairs.get(f'refs/tags/v{version}'))
        assert tag == head, 'Remote tag differs from verified HEAD'
        result.update(commit=head,url=release['url'],remoteVerified=True)
    return result

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--version',required=True)
    p.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[2])
    p.add_argument('--remote',action='store_true')
    p.add_argument('--output',type=Path)
    args=p.parse_args()
    try:
        result=verify(args.version,args.root.resolve(),args.remote)
    except (AssertionError,KeyError,RuntimeError,OSError) as e:
        print(f'RELEASE VERIFICATION FAILED: {e}',file=sys.stderr)
        return 1
    if args.output:
        args.output.parent.mkdir(parents=True,exist_ok=True)
        args.output.write_text(json.dumps(result,indent=2)+'\n',encoding='utf8')
    print(json.dumps(result,indent=2))
    print('RELEASE VERIFICATION PASSED')
    return 0
if __name__ == '__main__':
    raise SystemExit(main())
