#!/usr/bin/env python3
"""Read-only ADB checks. Does not install, enable, grant, root or modify anything."""
import argparse,subprocess,json,re,sys
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--serial');p.add_argument('--metadata',default=str(Path(__file__).with_name('artifact.json')));args=p.parse_args()
adb=['adb']+(['-s',args.serial] if args.serial else [])
def run(*words):
 r=subprocess.run(adb+list(words),capture_output=True,text=True,timeout=30)
 if r.returncode:raise RuntimeError(r.stderr.strip() or r.stdout.strip())
 return r.stdout.strip()
try:
 meta=json.loads(Path(args.metadata).read_text());sdk=run('shell','getprop','ro.build.version.sdk')
 if sdk!='27':raise RuntimeError('This candidate targets the reviewed API27 firmware only; found API'+sdk)
 uid=run('shell','id');print('ADB UID:',uid.split(' ')[0]);good=True
 for part,target in meta['targets'].items():
  paths=run('shell','pm','path',target['package']).splitlines()
  if len(paths)!=1 or not paths[0].startswith('package:'):raise RuntimeError('Unexpected target APK layout: '+target['package'])
  path=paths[0][8:]
  if not re.fullmatch(r'/[A-Za-z0-9_./+\-=]+',path):raise RuntimeError('Unexpected APK path')
  actual=run('shell','sha256sum',path).split()[0];match=actual==target['target_apk_sha256'];good &= match
  print(target['package']+(': MATCH' if match else ': DIFFERENT FIRMWARE — do not activate this candidate'))
 result=run('shell','cmd','overlay','list')
 if not result or 'Unknown command' in result or 'Exception' in result or 'Error' in result:raise RuntimeError('Overlay list unavailable: '+result[:200])
 print('Overlay Manager list: accessible. This does not prove enable permission or successful resource mapping.')
 print('Preflight '+('PASS; still verify each activation before continuing.' if good else 'FAILED; stop here.'))
 sys.exit(0 if good else 1)
except Exception as error:
 print('STOP:',error,file=sys.stderr);sys.exit(1)
