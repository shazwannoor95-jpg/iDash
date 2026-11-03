package com.shazwan.idash.a1lab;
import android.app.*;import android.widget.Toast;
/** Protects Appearance drafts at the existing central render boundary; other navigation is unchanged. */
final class SettingsNavigation {
 final LauncherActivity a;AlertDialog prompt;
 SettingsNavigation(LauncherActivity a){this.a=a;}
 boolean defer(){AppearanceEditor editor=a.appearanceEditor;if(editor==null||editor.closed||editor.committing||!editor.dirty)return false;
  if("settings".equals(a.page)&&"Appearance".equals(a.settingsSection))return true;
  final String page=a.page,section=a.settingsSection,query=a.query;final boolean panel=a.panelVisible;
  a.page="settings";a.settingsSection="Appearance";a.panelVisible=editor.panelAtOpen;
  if(prompt!=null&&prompt.isShowing())return true;
  Runnable navigate=()->{a.page=page;a.settingsSection=section;a.query=query;a.panelVisible=panel;a.render();};
  prompt=new AlertDialog.Builder(a).setTitle("Simpan perubahan Appearance?").setMessage("Draft belum disimpan. Simpan sebelum berpindah, buang draft, atau kekal di halaman ini.")
   .setPositiveButton("Simpan",(d,w)->{if(!editor.cacheReady){Toast.makeText(a,"Preview masih disediakan. Cuba Simpan semula sebentar lagi.",Toast.LENGTH_LONG).show();return;}editor.apply();navigate.run();})
   .setNeutralButton("Buang draft",(d,w)->{editor.close();navigate.run();}).setNegativeButton("Kekal",(d,w)->{}).create();prompt.setOnDismissListener(d->prompt=null);prompt.show();return true;
 }
 void close(){if(prompt!=null){prompt.dismiss();prompt=null;}}
}
