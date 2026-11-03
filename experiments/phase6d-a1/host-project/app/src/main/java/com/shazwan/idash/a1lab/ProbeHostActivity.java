package com.shazwan.idash.a1lab;
import android.os.Bundle;import android.widget.*;
/** Experiment subclass: F6C left widgets/glass stay inherited unchanged; only right contents differ. */
public final class ProbeHostActivity extends LauncherActivity {
 @Override public void onCreate(Bundle state){super.onCreate(state);prefs.edit().putString("jakim_zone","SGR02").putBoolean("calendar",true).putBoolean("jakim",true).putBoolean("prayer_countdown",true).putInt("dock_size",0).apply();page="home";panelVisible=true;render();}
 @Override void buildPanel(){LinearLayout header=row();TextView title=text("A1 lab · own target, NOT BitChord",13,android.graphics.Color.WHITE);header.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));header.addView(button("Close",()->{panelVisible=false;render();}));panelBox.addView(header);
  nativePanel=new DashNativeFrame(this,"a1lab");panelBox.addView(nativePanel,new LinearLayout.LayoutParams(-1,0,1));panelBox.addView(button("Own-display diagnostic · not A1 pass",()->nativePanel.probeOwnDisplay()),new LinearLayout.LayoutParams(-1,dp(48)));if(resumed)nativePanel.resume();}
}
