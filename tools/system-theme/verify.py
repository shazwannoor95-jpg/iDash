from pathlib import Path
import subprocess,hashlib,json,xml.etree.ElementTree as ET,zipfile
root=Path(__file__).resolve().parents[2]
base=Path('/workspace/headunit35/extracted')
targets={'settings':base/'Settings_com.android.settings/Settings_MtkSettings.apk','systemui':base/'System_UI_com.android.systemui/System_UI_MtkSystemUI.apk'}
import os
bt=Path(os.environ['ANDROID_HOME'])/'build-tools/36.0.0'
report={'version':'0.1-experimental','activation':'dynamic RRO through shell OMS; acceptance on owner firmware unverified','root_required_by_tools':False,'scope':'Palette only; no full iOS layout, blur, gesture, fonts or app-code replacement','targets':{}}
for part,target in targets.items():
 resources=subprocess.check_output([str(bt/'aapt'),'dump','resources',str(target)],text=True)
 colors=ET.parse(root/f'tools/system-theme/res-{part}/values/colors.xml').getroot()
 names=[c.attrib['name'] for c in colors]
 assert all('color/'+n+':' in resources for n in names),'Unknown target color'
 apk=root/f'dist/system-theme/iDash-{part}-overlay.apk'
 manifest=subprocess.check_output([str(bt/'aapt'),'dump','xmltree',str(apk),'AndroidManifest.xml'],text=True)
 assert 'android:isStatic' in manifest and '=(type 0x12)0x0' in manifest
 with zipfile.ZipFile(apk) as z:assert not any(n.endswith(('.dex','.so')) for n in z.namelist());assert z.testzip() is None
 report['targets'][part]={'package':'com.android.'+part,'target_apk_sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'overlay_file':apk.name,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'colors':names,'code_or_native_libraries':0}
(root/'dist/system-theme/artifact.json').write_text(json.dumps(report,indent=2)+'\n')
print('Target color names, resource-only APKs, manifests and hashes verified.')
