package com.shazwan.idash;
import android.app.*;import android.os.*;import android.graphics.*;import java.io.*;
/** Software Canvas fixture; no Activity geometry or device settings changed. */
public class GlassRenderChecks extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();int count=0;try{
  Bitmap comparison=Bitmap.createBitmap(900,960,Bitmap.Config.ARGB_8888);Canvas all=new Canvas(comparison);all.drawColor(0xff10151f);Paint label=new Paint(3);label.setColor(Color.WHITE);label.setTextSize(17);
  for(int row=0;row<6;row++){
   int base=row%2==0?0xffbfd2f4:0xff251b36,accent=row<2?0xff2766d8:row<4?0xffe582bf:0xff808b9b;
   Bitmap wp=Bitmap.createBitmap(450,150,Bitmap.Config.ARGB_8888);Canvas wall=new Canvas(wp);Paint p=new Paint(3);p.setShader(new LinearGradient(0,0,450,150,base,accent,Shader.TileMode.CLAMP));wall.drawPaint(p);p.setShader(null);p.setColor(row%2==0?0xffffffff:0xff100b20);for(int x=0;x<450;x+=40)wall.drawCircle(x,75,17,p);
   GlassBackdrop backdrop=new GlassBackdrop();backdrop.publish(GlassBackdrop.prepare(wp,1));if(Math.max(backdrop.frame.bitmap.getWidth(),backdrop.frame.bitmap.getHeight())>224)throw new AssertionError("cache bound");count++;
   for(int column=0;column<2;column++){
    int saved=all.save();all.translate(column*450,row*160+25);all.drawBitmap(wp,0,0,null);
    android.graphics.drawable.Drawable glass=column==0?new Phase2GlassSurface(1,0xff25324d,24,0,false,backdrop,1,row%2==0?1:2):new GlassSurface(1,0xff25324d,24,0,false,backdrop,1,row%2==0?1:2);glass.setBounds(25,15,425,135);glass.draw(all);if(column==1){int background=comparison.getPixel(column*450+250,row*160+25+95);double b=Color.luminance(background),ink=Color.luminance(((GlassSurface)glass).foregroundColor());if(GlassTextFixture.contrast(getTargetContext(),(GlassSurface)glass,base,450,150)<4.5)throw new AssertionError("sample contrast below4.5");count++;}label.setColor(column==1?((GlassSurface)glass).foregroundColor():Color.WHITE);all.drawText(column==0?"Phase 2 · 12:45":"Phase 6A · 12:45",55,80,label);
    if(column==1){Bitmap identity=backdrop.frame.bitmap;Bitmap buffer=Bitmap.createBitmap(450,150,Bitmap.Config.ARGB_8888);Canvas repeated=new Canvas(buffer);for(int frame=0;frame<20;frame++)glass.draw(repeated);buffer.recycle();if(identity!=backdrop.frame.bitmap)throw new AssertionError("cache replaced during draw");count++;}
    all.restoreToCount(saved);
   }
   label.setColor(Color.WHITE);all.drawText((row%2==0?"Light":"Dark")+(row<2?" / Blue":row<4?" / Pink":" / Neutral"),5,row*160+19,label);
   for(int quality=0;quality<3;quality++){GlassBackdrop.Frame f=GlassBackdrop.prepare(wp,quality);if(Math.max(f.bitmap.getWidth(),f.bitmap.getHeight())>IosStyle.FROST_EDGE[quality]||f.bitmap.getAllocationByteCount()>IosStyle.FROST_EDGE[quality]*IosStyle.FROST_EDGE[quality]*4)throw new AssertionError("quality bound");count++;}
  }
  try(OutputStream out=new FileOutputStream(new File(getTargetContext().getFilesDir(),"glass-phase25-comparison.png"))){comparison.compress(Bitmap.CompressFormat.PNG,100,out);}result.putString("stream","PASS: "+count+" glass cache/render checks; before left, after right\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable failure){result.putString("stream","FAIL: "+failure);finish(Activity.RESULT_CANCELED,result);}}
}
