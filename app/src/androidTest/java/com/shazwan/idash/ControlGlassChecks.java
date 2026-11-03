package com.shazwan.idash;
import android.app.*;import android.graphics.*;import android.os.*;
/** Scoped material regression and worst-case white wallpaper contrast; software Canvas, not GPU certification. */
public class ControlGlassChecks extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();int checks=0;try{for(int quality=0;quality<3;quality++)for(int color:new int[]{0xff081426,0xff306fab,0xffe9aaca,Color.WHITE}){
  Bitmap wallpaper=Bitmap.createBitmap(360,180,Bitmap.Config.ARGB_8888);wallpaper.eraseColor(color);GlassBackdrop cache=new GlassBackdrop();cache.publish(GlassBackdrop.prepare(wallpaper,quality));Bitmap old=Bitmap.createBitmap(360,180,Bitmap.Config.ARGB_8888),now=Bitmap.createBitmap(360,180,Bitmap.Config.ARGB_8888);Canvas a=new Canvas(old),b=new Canvas(now);a.drawColor(color);b.drawColor(color);
  Phase3GlassSurface legacy=new Phase3GlassSurface(1,0xff25324d,26,0,false,cache,quality,2);GlassSurface same=new GlassSurface(1,0xff25324d,26,0,false,cache,quality,2);legacy.setBounds(0,0,360,180);same.setBounds(0,0,360,180);legacy.draw(a);same.draw(b);if(old.sameAs(now))throw new AssertionError("refined material unchanged from Phase3");checks++;
  GlassSurface premium=new GlassSurface(1,IosStyle.CC_BASE,26,0,false,cache,quality,2,true);premium.setBounds(0,0,360,180);b.drawColor(color);premium.draw(b);double background=Color.luminance(now.getPixel(180,70)),ink=Color.luminance(IosStyle.CC_TEXT);if(GlassTextFixture.contrast(getTargetContext(),premium,color,360,180)<4.5)throw new AssertionError("premium primary text contrast");checks++;Bitmap identity=cache.frame.bitmap;for(int i=0;i<40;i++)premium.draw(b);if(identity!=cache.frame.bitmap)throw new AssertionError("draw replaced frost bitmap");checks++;old.recycle();now.recycle();wallpaper.recycle();
 }result.putString("stream","PASS: "+checks+" scoped glass/contrast/cache checks\n");finish(Activity.RESULT_OK,result);}catch(Throwable error){result.putString("stream","FAIL after "+checks+": "+error+"\n");finish(Activity.RESULT_CANCELED,result);}}
}
