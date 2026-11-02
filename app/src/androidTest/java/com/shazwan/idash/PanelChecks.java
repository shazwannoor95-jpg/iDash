package com.shazwan.idash;
import android.app.*;import android.os.*;import android.content.*;import android.view.*;
/** Separate-UID fixture checks. Never claims TizenTube or live YouTube playback. */
public class PanelChecks extends Instrumentation {
 int count;LauncherActivity a;
 void check(boolean value,String message){if(!value)throw new AssertionError(message);count++;}
 int fixtureCount(DashNativeFrame frame)throws Exception{Parcel in=Parcel.obtain(),out=Parcel.obtain();try{in.writeInterfaceToken(DashNativeFrame.TOKEN);frame.remote.transact(2,in,out,0);out.readException();return out.readInt();}finally{in.recycle();out.recycle();}}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();long deadline=SystemClock.elapsedRealtime()+20000;while(!a.loaded&&SystemClock.elapsedRealtime()<deadline)Thread.sleep(100);
  runOnMainSync(()->{check(!a.panelVisible&&a.panelBox==null&&a.nativePanel==null&&a.webPanel==null,"panel hidden and unallocated initially");a.panelVisible=true;a.prefs.edit().putInt("panel_mode",1).apply();a.render();});waitForIdleSync();
  final DashNativeFrame[] frame={null};runOnMainSync(()->{frame[0]=a.nativePanel;check(frame[0]!=null&&frame[0].display==null,"show panel does not auto-start native app");frame[0].select(1);frame[0].resume();frame[0].start();});
  deadline=SystemClock.elapsedRealtime()+65000;while(!frame[0].targetConfirmed&&frame[0].wanted&&SystemClock.elapsedRealtime()<deadline)Thread.sleep(200);
  check(frame[0].targetConfirmed,"actual companion confirms requested native display: "+frame[0].status.getText());check(frame[0].requestedDisplay>0&&frame[0].requestedDisplay==frame[0].lastObservedDisplay,"fixture runs on requested secondary display");check(frame[0].display!=null,"native display allocated");
  int before=fixtureCount(frame[0]);runOnMainSync(()->{float x=frame[0].surface.getWidth()/2f,y=110f*frame[0].surface.getHeight()/frame[0].renderHeight;long at=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(at,at,MotionEvent.ACTION_DOWN,x,y,0),up=MotionEvent.obtain(at,at+30,MotionEvent.ACTION_UP,x,y,0);frame[0].surface.dispatchTouchEvent(down);frame[0].surface.dispatchTouchEvent(up);down.recycle();up.recycle();});
  deadline=SystemClock.elapsedRealtime()+10000;int after=before;while(after==before&&SystemClock.elapsedRealtime()<deadline){Thread.sleep(200);after=fixtureCount(frame[0]);}check(after>before,"scaled surface touch reaches separate native Activity");
  runOnMainSync(()->{a.prefs.edit().putInt("glass_style",1).apply();check(frame[0]==a.nativePanel&&frame[0].targetConfirmed,"preference alone does not restart native session");a.onStop();check(frame[0].display==null&&!frame[0].wanted&&frame[0].inputThread==null,"background stop releases native resources");a.panelVisible=false;a.render();check(a.nativePanel==null&&frame[0].closed,"hide disposes native host");a.onStart();a.onResume();});
  runOnMainSync(()->{a.prefs.edit().putInt("panel_mode",0).apply();a.panelVisible=true;a.render();check(a.webPanel!=null&&a.webPanel.web==null,"web panel waits for explicit Start");a.panelVisible=false;a.render();check(a.webPanel==null,"hide disposes browser");a.prefs.edit().putInt("glass_style",0).apply();});
  result.putString("stream","PASS: "+count+" iDash panel assertions (real separate-UID fixture; no ARM TizenTube/live web certification)\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable failure){result.putString("stream","FAIL after "+count+": "+failure+"\n");finish(Activity.RESULT_CANCELED,result);}finally{if(a!=null)runOnMainSync(()->a.finish());}}
}
