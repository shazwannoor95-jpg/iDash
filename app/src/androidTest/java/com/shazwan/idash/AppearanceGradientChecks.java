package com.shazwan.idash;
import android.app.*;import android.content.*;import android.os.*;import java.util.*;
/** Procedural wallpaper fallback follows Blue/Rose without bitmap allocation or changing default legacy material. */
public class AppearanceGradientChecks extends Instrumentation {
 LauncherActivity a;int count;Map<String,?> backup;SharedPreferences prefs;
 void check(boolean ok,String why){if(!ok)throw new AssertionError(why);count++;}
 void ui(Runnable r){Throwable[] error={null};runOnMainSync(()->{try{r.run();}catch(Throwable e){error[0]=e;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;try{
  prefs=getTargetContext().getSharedPreferences("idash",0);backup=new HashMap<>(prefs.getAll());a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));long until=SystemClock.elapsedRealtime()+20000;while(!a.loaded&&SystemClock.elapsedRealtime()<until)Thread.sleep(100);
  ui(()->{prefs.edit().putInt("theme",1).putString("wallpaper","").apply();a.theme=1;a.requestedWallpaper=null;a.selectWallpaper("");new DashSettings(a).open("Appearance");a.appearanceEditor.change(0,2);});Thread.sleep(350);final int rose=a.appearanceEditor.cache.frame.tint;ui(()->{check(a.appearanceEditor.source==null&&a.appearanceEditor.cache.frame.bitmap==null,"procedural Rose preview allocates no bitmap");check(rose!=0xff526783,"Adaptive Rose replaces old neutral fallback");a.appearanceEditor.apply();check(IosStyle.backdrop(a).frame.tint==rose,"Apply uses same Rose tint as live preview");});
  ui(()->{a.prefs.edit().putInt("theme",0).apply();a.theme=0;a.requestedWallpaper=null;a.selectWallpaper("");a.render();});Thread.sleep(350);ui(()->{check(a.appearanceEditor.cache.frame.tint!=rose,"Adaptive Blue and Rose remain visually different");check(a.appearanceEditor.preparedCount==0&&a.appearanceEditor.cache.frame.bitmap==null,"procedural Blue does not build frost bitmaps");});
  result.putString("stream","PASS: "+count+" procedural-gradient appearance checks\n");code=Activity.RESULT_OK;
 }catch(Throwable error){result.putString("stream","FAIL after "+count+": "+error+"\n");}finally{if(prefs!=null&&backup!=null)AppearanceChecks.restore(prefs,backup);if(a!=null)ui(()->{if(a.appearanceEditor!=null)a.appearanceEditor.close();a.finish();});}finish(code,result);}
}
