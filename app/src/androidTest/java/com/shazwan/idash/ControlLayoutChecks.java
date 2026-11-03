package com.shazwan.idash;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.view.*;import java.io.*;import android.widget.*;
/** Real landscape geometry, text alignment and touch targets on a disposable emulator. */
public class ControlLayoutChecks extends Instrumentation {
 LauncherActivity a;int count;
 void check(boolean ok,String message){if(!ok)throw new AssertionError(message);count++;}
 View find(View root,String name){if(name.contentEquals(root.getContentDescription()==null?"":root.getContentDescription()))return root;if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int i=0;i<g.getChildCount();i++){View match=find(g.getChildAt(i),name);if(match!=null)return match;}}return null;}
 TextView findText(View root,String value){if(root instanceof TextView&&value.contentEquals(((TextView)root).getText()))return (TextView)root;if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int i=0;i<g.getChildCount();i++){TextView result=findText(g.getChildAt(i),value);if(result!=null)return result;}}return null;}
 void ui(Runnable work){Throwable[] failure={null};runOnMainSync(()->{try{work.run();}catch(Throwable e){failure[0]=e;}});waitForIdleSync();if(failure[0]!=null)throw new AssertionError(failure[0]);}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;try{
 a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();long loadedBy=SystemClock.elapsedRealtime()+20000;while(!a.loaded&&SystemClock.elapsedRealtime()<loadedBy)Thread.sleep(100);ui(()->{a.page="home";a.render();a.controlCenter();});waitForIdleSync();Thread.sleep(1200);
 ui(()->{View decor=a.quickControls.dialog.getWindow().getDecorView();Rect frame=new Rect();decor.getGlobalVisibleRect(frame);check(frame.width()>frame.height(),"landscape dialog");
 for(String name:new String[]{"Connectivity card","Media card","Media volume","System brightness","Panel","Wallpapers","Brightness access / Display"}){View view=find(decor,name);check(view!=null,"component exists: "+name);Rect visible=new Rect();check(view.getGlobalVisibleRect(visible)&&visible.height()==view.getHeight(),"not clipped: "+name);check(frame.contains(visible),"inside dialog: "+name);}
 for(String name:new String[]{"Wi-Fi · toggle or open Android settings","Bluetooth · toggle or open Android settings","Panel","Wallpapers","Media volume","System brightness","Panel"}){View view=find(decor,name);check(view!=null&&view.getWidth()>=a.dp(48)&&view.getHeight()>=a.dp(48),"48dp touch target: "+name);}
 for(String label:new String[]{"Camera","Flashlight","No flash","Focus","Settings","Timer","Calculator","Airplane","Connections"})check(findText(decor,label)==null,"removed UI: "+label);
 check(a.quickControls.utilitiesControl.focus==null,"Focus UI removed");
 check(!a.quickControls.utilitiesControl.active&&!a.quickControls.utilitiesControl.receiverRegistered,"removed utilities have no active callbacks");for(View utility:a.quickControls.utilitiesControl.buttons)check(utility==null,"utility UI removed without placeholder");
 TextView b=a.quickControls.brightnessValue,v=a.quickControls.volumeValue;int[] bp=new int[2],vp=new int[2];b.getLocationOnScreen(bp);v.getLocationOnScreen(vp);check(bp[1]+b.getBaseline()==vp[1]+v.getBaseline(),"slider value baselines match");check(b.getTextSize()==v.getTextSize()&&b.getGravity()==Gravity.CENTER&&v.getGravity()==Gravity.CENTER,"slider value typography centered");
 check((a.quickControls.mediaControl.title.getGravity()&Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)==Gravity.START,"media title left aligned");
 for(View tile:new View[]{a.quickControls.mediaControl.previous,a.quickControls.mediaControl.play,a.quickControls.mediaControl.next}){ViewGroup group=(ViewGroup)tile;TextView label=(TextView)group.getChildAt(1);check(label.getGravity()==Gravity.CENTER&&label.getTextSize()>=12*a.getResources().getDisplayMetrics().scaledDensity,"transport label centered and readable");int[] iconAt=new int[2],labelAt=new int[2];group.getChildAt(0).getLocationOnScreen(iconAt);label.getLocationOnScreen(labelAt);check(Math.abs(iconAt[0]+group.getChildAt(0).getWidth()/2-(labelAt[0]+label.getWidth()/2))<=1,"icon and label centers match");check(tile.getWidth()>=a.dp(48)&&tile.getHeight()>=a.dp(48),"transport48dp target");}
 check(a.quickControls.volume instanceof CapsuleSlider&&a.quickControls.brightness instanceof CapsuleSlider,"vertical capsules preserve SeekBar interface");
 });
 Bitmap screenshot=getUiAutomation().takeScreenshot();if(screenshot!=null){try(OutputStream out=new FileOutputStream(new File(a.getFilesDir(),"control-center-landscape.png"))){screenshot.compress(Bitmap.CompressFormat.PNG,100,out);}screenshot.recycle();}
 result.putString("stream","PASS: "+count+" landscape Control Center assertions\n");code=Activity.RESULT_OK;
 }catch(Throwable failure){result.putString("stream","FAIL after "+count+": "+failure+"\n");}finally{if(a!=null)runOnMainSync(()->{if(a.quickControls!=null)a.quickControls.close();a.finish();});}finish(code,result);}
}
