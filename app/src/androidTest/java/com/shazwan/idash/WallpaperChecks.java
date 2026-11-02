package com.shazwan.idash;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.view.*;import java.io.*;import java.nio.file.*;import java.util.*;
/** Disposable-device image import and real gallery/lifecycle checks. */
public class WallpaperChecks extends Instrumentation {
 int count;LauncherActivity a;
 void check(boolean value,String message){if(!value)throw new AssertionError(message);count++;}
 View find(View v,String name){if(name.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View result=find(g.getChildAt(i),name);if(result!=null)return result;}}return null;}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
  Bitmap image=Bitmap.createBitmap(1600,900,Bitmap.Config.ARGB_8888);image.eraseColor(0xffe33d18);ByteArrayOutputStream bytes=new ByteArrayOutputStream();image.compress(Bitmap.CompressFormat.PNG,100,bytes);image.recycle();
  CustomWallpaper.save(a,new ByteArrayInputStream(bytes.toByteArray()));byte[] saved=Files.readAllBytes(CustomWallpaper.file(a).toPath());
  check(Arrays.equals(saved,bytes.toByteArray()),"user image copied without recompression");Bitmap decoded=CustomWallpaper.decode(CustomWallpaper.file(a),1280);check(Math.max(decoded.getWidth(),decoded.getHeight())<=1280,"custom decode bounded");check(decoded.getPixel(20,20)==0xffe33d18,"decoded image retains content");decoded.recycle();
  boolean rejected=false;try{CustomWallpaper.save(a,new ByteArrayInputStream(new byte[]{1,2,3}));}catch(IOException expected){rejected=true;}check(rejected,"corrupt image rejected");check(Arrays.equals(saved,Files.readAllBytes(CustomWallpaper.file(a).toPath())),"failed import preserves previous image");
  rejected=false;try{CustomWallpaper.save(a,new InputStream(){int remaining=CustomWallpaper.MAX_BYTES+1;public int read(){return remaining-->0?0:-1;}public int read(byte[] buffer,int offset,int length){if(remaining==0)return -1;int n=Math.min(remaining,length);Arrays.fill(buffer,offset,offset+n,(byte)0);remaining-=n;return n;}});}catch(IOException expected){rejected=true;}check(rejected,"oversized input rejected");check(Arrays.equals(saved,Files.readAllBytes(CustomWallpaper.file(a).toPath())),"oversized import preserves previous image");
  runOnMainSync(()->{a.selectWallpaper(CustomWallpaper.PATH);check(CustomWallpaper.PATH.equals(a.prefs.getString("wallpaper","")),"custom selection saved");});
  long end=SystemClock.elapsedRealtime()+20000;while((a.wallpaperBitmap==null||a.wallpaperBitmap.getPixel(20,20)!=0xffe33d18||!CustomWallpaper.PATH.equals(a.requestedWallpaper))&&SystemClock.elapsedRealtime()<end)Thread.sleep(100);waitForIdleSync();
  runOnMainSync(()->{check(a.wallpaperBitmap!=null&&a.wallpaperBitmap.getPixel(20,20)==0xffe33d18,"custom wallpaper reaches actual backdrop");a.panelVisible=true;a.prefs.edit().putInt("panel_mode",0).apply();a.render();DashWebPlayer panel=a.webPanel;a.selectWallpaper("mrios/wallpapers/47.jpg");check(a.webPanel==panel,"wallpaper selection preserves existing panel view");a.wallpaperPicker();check(a.wallpaperDialog!=null&&a.wallpaperDialog.isShowing(),"wallpaper gallery opens");check(find(a.wallpaperDialog.getWindow().getDecorView(),"MRiOS wallpaper 47.jpg")!=null&&find(a.wallpaperDialog.getWindow().getDecorView(),"MRiOS wallpaper 70.webp")!=null,"gallery contains first and last collection entries");a.wallpaperDialog.dismiss();a.panelVisible=false;a.prefs.edit().putInt("glass_style",0).apply();a.render();});
  waitForIdleSync();runOnMainSync(()->check(a.wallpaperDialog==null,"gallery resources dismissed after dismissal callback"));
  result.putString("stream","PASS: "+count+" iDash wallpaper assertions\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable failure){result.putString("stream","FAIL after "+count+": "+failure+"\n");finish(Activity.RESULT_CANCELED,result);}finally{if(a!=null)runOnMainSync(()->a.finish());}}
}
