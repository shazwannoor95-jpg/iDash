package com.shazwan.idash;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.Rect;import android.view.*;import android.widget.*;
/** Runs against the actual UI on a disposable API27 emulator; no OEM fixtures. */
public class SmokeChecks extends Instrumentation {
 int count;LauncherActivity a;
 void check(boolean ok,String why){if(!ok)throw new AssertionError(why);count++;}
 View find(View v,String description){if(description.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View f=find(g.getChildAt(i),description);if(f!=null)return f;}}return null;}
 void tap(String description){runOnMainSync(()->{View v=find(a.stage,description);if(v==null)throw new AssertionError("Missing "+description);v.performClick();});waitForIdleSync();}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle r=new Bundle();try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();long end=SystemClock.elapsedRealtime()+20000;while(!a.loaded&&SystemClock.elapsedRealtime()<end)Thread.sleep(100);waitForIdleSync();
  runOnMainSync(()->{check(a.loaded,"app discovery completes");check(a.apps.size()>0,"real installed activities discovered");check(a.clock.getText().length()>=4,"clock populated");check(find(a.stage,"Calendar widget")!=null,"calendar exists");check(find(a.stage,"Power widget")!=null,"power exists");Rect screen=new Rect(0,0,a.stage.getWidth(),a.stage.getHeight());for(String name:new String[]{"Clock widget","Favorite Maps","Favorite Bluetooth","Apps","Settings"}){View v=find(a.stage,name);int[] xy=new int[2];v.getLocationOnScreen(xy);check(screen.contains(xy[0],xy[1],xy[0]+v.getWidth(),xy[1]+v.getHeight()),name+" fits screen");check(v.getHeight()>=a.dp(48),name+" touch target");}});
  tap("Apps");runOnMainSync(()->{check(a.page.equals("apps"),"dock opens library");check(a.grid.getChildCount()>0,"drawer populated");a.search.setText("zzzz_idash_missing_app");check(a.drawerCount.getText().toString().equals("0 apps"),"search filters actual list");check(a.grid.getChildCount()==1,"empty result message");a.search.setText("");check(a.grid.getChildCount()>0,"search clears");a.onBackPressed();check(a.page.equals("home"),"Back returns home");});
  runOnMainSync(()->{LauncherActivity.App chosen=a.apps.get(0);a.assign(0,chosen);check(a.assigned(0).component.equals(chosen.component),"favorite assigned");String saved=a.prefs.getString("slot0","");a.render();check(a.assigned(0).component.equals(saved),"favorite survives UI rebuild");a.prefs.edit().putBoolean("calendar",false).apply();a.render();check(find(a.stage,"Calendar widget")==null,"widget visibility saved");a.prefs.edit().putBoolean("calendar",true).apply();a.render();check(find(a.stage,"Calendar widget")!=null,"widget restored");});
  String expected=a.prefs.getString("slot0","");runOnMainSync(()->a.finish());waitForIdleSync();a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));end=SystemClock.elapsedRealtime()+20000;while(!a.loaded&&SystemClock.elapsedRealtime()<end)Thread.sleep(100);waitForIdleSync();runOnMainSync(()->check(a.assigned(0)!=null&&a.assigned(0).component.equals(expected),"favorite survives Activity restart"));
  tap("◉  Control Center");
  end=SystemClock.elapsedRealtime()+15000;boolean volumeSeen=false;
  while(SystemClock.elapsedRealtime()<end){android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();if(root!=null&&!root.findAccessibilityNodeInfosByText("Media volume").isEmpty()){volumeSeen=true;break;}Thread.sleep(200);}
  check(volumeSeen,"Control Center exposes media volume");
  sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);waitForIdleSync();
  runOnMainSync(()->a.openSettings(android.provider.Settings.ACTION_SETTINGS));end=SystemClock.elapsedRealtime()+15000;boolean settingsSeen=false;
  while(SystemClock.elapsedRealtime()<end){android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();if(root!=null&&"com.android.settings".contentEquals(root.getPackageName())){settingsSeen=true;break;}Thread.sleep(200);}
  check(settingsSeen,"actual Android Settings opens outside launcher");check(!a.resumed,"launcher pauses behind external app");
  getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);end=SystemClock.elapsedRealtime()+15000;while(!a.resumed&&SystemClock.elapsedRealtime()<end)Thread.sleep(200);check(a.resumed,"launcher resumes after external app Back");
  r.putString("stream","PASS: "+count+" iDash API27 UI assertions\n");finish(Activity.RESULT_OK,r);
 }catch(Throwable e){r.putString("stream","FAIL after "+count+": "+e+"\n");finish(Activity.RESULT_CANCELED,r);}finally{if(a!=null)runOnMainSync(()->a.finish());}}
}
