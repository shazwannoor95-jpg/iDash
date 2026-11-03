package com.shazwan.idash;
import android.content.Context;import android.graphics.*;import android.view.*;import android.widget.*;
/** Local text backdrop measurement; translucent empty panel areas need not meet text contrast. */
final class GlassTextFixture {
 static double contrast(Context context,GlassSurface glass,int wall,int w,int h){
  android.graphics.drawable.Drawable.Callback oldCallback=glass.getCallback();Rect oldBounds=new Rect(glass.getBounds());
  FrameLayout root=new FrameLayout(context);root.setBackground(glass);TextView text=new TextView(context);text.setText("Glass text");text.setTextSize(14);text.setGravity(Gravity.CENTER);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(w-32,h/3,Gravity.CENTER);root.addView(text,lp);root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);glass.setBounds(0,0,w,h);int fg=glass.foregroundColor();text.setTextColor(fg);
  Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(wall);root.draw(canvas);text.setTextColor(Color.TRANSPARENT);canvas.drawColor(wall);root.draw(canvas);double a=Color.luminance(fg),b=Color.luminance(image.getPixel(w/2,h/2));image.recycle();glass.setCallback(oldCallback);glass.setBounds(0,0,1,1);glass.setBounds(oldBounds);return (Math.max(a,b)+.05)/(Math.min(a,b)+.05);
 }
}
