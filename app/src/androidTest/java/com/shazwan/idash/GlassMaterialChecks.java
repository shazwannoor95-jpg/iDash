package com.shazwan.idash;
import android.app.*;import android.content.*;import android.graphics.*;import android.os.*;
/** Preference compatibility, material continuity, useful slider range and unchanged frost identity. */
public class GlassMaterialChecks extends Instrumentation {
 int count;void check(boolean ok,String why){if(!ok)throw new AssertionError(why);count++;}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;Context isolated=new ContextWrapper(getTargetContext()){@Override public SharedPreferences getSharedPreferences(String n,int m){return super.getSharedPreferences("glass6a_model_fixture",m);}};SharedPreferences prefs=isolated.getSharedPreferences("idash",0);try{
  prefs.edit().clear().commit();check(GlassAppearance.read(isolated).transparency==45&&!prefs.contains("glass_transparency"),"absent preference uses safe new default without writing");
  for(int old:new int[]{10,20,30}){prefs.edit().putInt("glass_transparency",old).putBoolean("glass_customized",true).commit();check(GlassAppearance.read(isolated).transparency==old,"existing percentage retained "+old);}
  check(GlassAppearance.defaults().transparency==45&&GlassAppearance.defaults().quality==1,"explicit Reset default");
  for(int tone=1;tone<=2;tone++){
   Bitmap wall=Bitmap.createBitmap(240,120,Bitmap.Config.ARGB_8888);wall.eraseColor(tone==1?Color.BLACK:Color.WHITE);GlassBackdrop cache=new GlassBackdrop();cache.publish(GlassBackdrop.prepare(wall,1));Bitmap identity=cache.frame.bitmap;Bitmap image=Bitmap.createBitmap(240,120,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);int first=0,last=0;double previous=tone==1?2:-1;
   for(int t:new int[]{10,20,30,45,65}){GlassSurface glass=new GlassSurface(1,IosStyle.CC_BASE,24,0,false,cache,1,tone,true).appearance(new GlassAppearance(tone,t,1,0,1,false,true));glass.setBounds(0,0,240,120);glass.draw(canvas);int pixel=image.getPixel(120,80);double lum=Color.luminance(pixel);check(tone==1?lum<previous:lum>previous,"slider monotonic in unoccupied glass at "+t);previous=lum;if(t==10)first=pixel;if(t==65)last=pixel;check(cache.frame.bitmap==identity,"slider does not replace cache");}
   int distance=Math.abs(Color.red(first)-Color.red(last))+Math.abs(Color.green(first)-Color.green(last))+Math.abs(Color.blue(first)-Color.blue(last));check(distance>90,"slider endpoints visibly distinct");
   Bitmap baseline=Bitmap.createBitmap(240,120,Bitmap.Config.ARGB_8888);GlassSurface plain=new GlassSurface(1,IosStyle.CC_BASE,24,0,false,cache,1,0,true);plain.setBounds(0,0,240,120);plain.draw(new Canvas(baseline));GlassSurface applied=new GlassSurface(1,IosStyle.CC_BASE,24,0,false,cache,1,0,true).appearance(GlassAppearance.defaults());applied.setBounds(0,0,240,120);canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);applied.draw(canvas);check(baseline.sameAs(image),"unconfigured material matches explicitly applied default");
   baseline.recycle();image.recycle();cache.frame.bitmap.recycle();wall.recycle();
  }
  result.putString("stream","PASS: "+count+" glass material/preferences checks\n");code=Activity.RESULT_OK;
 }catch(Throwable t){result.putString("stream","FAIL after "+count+": "+t+"\n");}finally{prefs.edit().clear().commit();}finish(code,result);}
}
