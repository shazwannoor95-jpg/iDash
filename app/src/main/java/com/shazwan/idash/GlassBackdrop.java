package com.shazwan.idash;
import android.graphics.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
/** One wallpaper-derived, bounded frost cache per Activity. Prepared off the UI thread. */
final class GlassBackdrop {
 static final class Frame {final Bitmap bitmap;final int tint,quality;Frame(Bitmap b,int t){this(b,t,-1);}Frame(Bitmap b,int t,int q){bitmap=b;tint=t;quality=q;}}
 Frame frame=new Frame(null,0xff526783);int revision;
 final ArrayList<WeakReference<GlassSurface>> surfaces=new ArrayList<>();
 void publish(Frame next){frame=next;revision++;for(int i=surfaces.size()-1;i>=0;i--){GlassSurface s=surfaces.get(i).get();if(s==null)surfaces.remove(i);else s.invalidateSelf();}}
 static Frame prepare(Bitmap source,int quality){
  quality=GlassAppearance.clamp(quality,0,2);
  if(source==null)return new Frame(null,0xff526783);
  int edge=IosStyle.FROST_EDGE[quality],radius=IosStyle.FROST_RADIUS[quality];
  float scale=Math.min(1,edge/(float)Math.max(source.getWidth(),source.getHeight()));int w=Math.max(1,Math.round(source.getWidth()*scale)),h=Math.max(1,Math.round(source.getHeight()*scale));
  Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(out);c.drawColor(Color.BLACK);c.drawBitmap(source,null,new Rect(0,0,w,h),new Paint(Paint.FILTER_BITMAP_FLAG));
  int[] a=new int[w*h],b=new int[w*h];out.getPixels(a,0,w,0,0,w,h);long red=0,green=0,blue=0;for(int pixel:a){red+=Color.red(pixel);green+=Color.green(pixel);blue+=Color.blue(pixel);}int tint=Color.rgb((int)(red/a.length),(int)(green/a.length),(int)(blue/a.length));
  for(int pass=0;pass<3;pass++){blur(a,b,w,h,radius,true);blur(b,a,w,h,radius,false);}out.setPixels(a,0,w,0,0,w,h);return new Frame(out,tint,quality);
 }
 private static void blur(int[] src,int[] dst,int w,int h,int radius,boolean horizontal){
  int outer=horizontal?h:w,inner=horizontal?w:h;
  for(int line=0;line<outer;line++)for(int at=0;at<inner;at++){
   int r=0,g=0,b=0;for(int k=-radius;k<=radius;k++){int p=Math.max(0,Math.min(inner-1,at+k));int v=src[horizontal?line*w+p:p*w+line];r+=Color.red(v);g+=Color.green(v);b+=Color.blue(v);}int n=radius*2+1;dst[horizontal?line*w+at:at*w+line]=Color.rgb(r/n,g/n,b/n);
  }
 }
}
