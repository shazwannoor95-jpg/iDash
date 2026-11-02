#!/usr/bin/env python3
"""Package the reviewed signed artifacts; never include workspace/private inputs."""
from pathlib import Path
import hashlib,json,zipfile
root=Path(__file__).resolve().parent.parent
main=json.loads((root/'dist/artifact.json').read_text())
companion=json.loads((root/'dist/companions/artifact.json').read_text())
files=[(root/'dist'/main['file'],main),(root/'dist/companions'/companion['file'],companion)]
for file,meta in files:
    assert hashlib.sha256(file.read_bytes()).hexdigest()==meta['sha256'],file.name+' differs from reviewed artifact'
instructions='''iDash + SmartTube iDash companion — PREVIEW / EXPERIMENTAL

1. Extract/unzip this archive. A ZIP is not an installable APK.
2. Install iDash first. If upgrading, install over the existing app to preserve settings.
3. OPTIONAL: read SMARTTUBE-REVIEW.md before installing SmartTube-iDash.apk.
4. Android asks you to approve each installation. Enable installs from your file manager only if requested.
5. Open iDash -> Panel -> Mode -> SmartTube -> Start.
   SmartTube uses a separate com.shazwan.idash.smarttube identity and matching iDash signer.
   An ordinary official/differently signed SmartTube APK cannot use this cooperative panel path.
6. For prayer times, tap the JAKIM widget and explicitly select the correct zone.
   Clock -> prayers -> calendar share one scroll area. Scroll on shorter displays.

PENTING / IMPORTANT:
SmartTube upstream reports a compromised build environment affecting some releases.
The official stable32.68 input was rechecked; no independent malware clearance is established here.
This optional derivative is an experimental candidate, not an upstream-endorsed release.
ARM32/ARM64 libraries and minimum SDK compatibility do not certify actual head-unit playback,
touch navigation, login, video/audio, RAM use or secondary-display continuity.
iDash cannot silently install another app on ordinary Android. Nothing auto-installs, roots,
debloats, removes other apps or changes the default launcher without your action.
Keep your existing launcher. Test iDash before selecting it as the default home app.

Public source, notices and rebuild tools: https://github.com/shazwannoor95-jpg/iDash
No private signing key, owner diagnostics, original system archives or custom wallpaper are included.
'''
out=root/'dist/bundles'/('iDash-'+main['version_name']+'-SmartTube-preview.zip');out.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(out,'w',compression=zipfile.ZIP_STORED) as archive:
    for file,_ in files:archive.write(file,file.name)
    archive.writestr('INSTALL-ME-FIRST.txt',instructions)
    archive.writestr('SHA256SUMS.txt',''.join(meta['sha256']+'  '+file.name+'\n' for file,meta in files))
    for file,name in [('docs/SMARTTUBE_REVIEW.md','SMARTTUBE-REVIEW.md'),('docs/licenses/SmartTube-MIT.txt','SmartTube-LICENSE.txt'),('NOTICE','NOTICE.txt'),('LICENSE','iDash-LICENSE.txt')]:archive.write(root/file,name)
    assert archive.testzip() is None
metadata={'file':out.name,'sha256':hashlib.sha256(out.read_bytes()).hexdigest(),'size_bytes':out.stat().st_size,'idash':main['version_name'],'smarttube':companion['version_name'],'install':'Extract ZIP and approve APK installations separately; companion optional','security_status':companion['security_status'],'physical_head_unit_tested':False}
(out.parent/'artifact.json').write_text(json.dumps(metadata,indent=2)+'\n')
print(out.name,metadata['sha256'])
