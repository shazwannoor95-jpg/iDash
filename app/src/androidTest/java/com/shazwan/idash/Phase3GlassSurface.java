package com.shazwan.idash;
import android.graphics.*;import android.graphics.drawable.Drawable;import android.view.View;
import java.lang.ref.WeakReference;
/** Original API27 glass: wallpaper frost, cached shaders, rim/reflection and inset shadow bands. */
final class Phase3GlassSurface extends Drawable {
 private final Paint paint=new Paint(3);private final RectF box=new RectF(),rim=new RectF(),shadow=new RectF();private final Path clip=new Path();private final Matrix matrix=new Matrix();private final int[] position=new int[2];
 private final float radius,density;private final int tint,style,quality,tone;private final boolean sheet;private final GlassBackdrop backdrop;
 private final Path specular=new Path();private final RectF inner=new RectF();private Shader bevel,specularFade;private boolean light,contrastQueued;
 private Shader body,sheen,edge,reflection;private BitmapShader frost;private int opacity=255,revision=-1;
 Phase3GlassSurface(float d,int tint,int radius,int style,boolean sheet){this(d,tint,radius,style,sheet,null,1,0);}
 Phase3GlassSurface(float d,int tint,int radius,int style,boolean sheet,GlassBackdrop backdrop,int quality,int tone){this.density=d;this.radius=radius*d;this.tint=tint;this.style=style;this.sheet=sheet;this.backdrop=backdrop;this.quality=quality;this.tone=tone;}

 private static int mix(int a,int b,float f){return Color.rgb(Math.round(Color.red(a)*f+Color.red(b)*(1-f)),Math.round(Color.green(a)*f+Color.green(b)*(1-f)),Math.round(Color.blue(a)*f+Color.blue(b)*(1-f)));}
 @Override protected void onBoundsChange(Rect bounds){box.set(bounds);box.inset(2*density,2*density);rim.set(box);rim.inset(density*.5f,density*.5f);clip.reset();clip.addRoundRect(box,radius,radius,Path.Direction.CW);rebuild();}
 private void rebuild(){
  GlassBackdrop.Frame f=backdrop==null?null:backdrop.frame;revision=backdrop==null?0:backdrop.revision;int sampled=f==null?tint:f.tint;
  light=tone==1||(tone==0&&(Color.red(sampled)*.2126+Color.green(sampled)*.7152+Color.blue(sampled)*.0722)<90);
  int rgb=mix(sampled,light?IosStyle.LIGHT_BASE:IosStyle.DARK_BASE,.14f)&0xffffff;
  int top=style==2?255:sheet?220:style==1?65:light?190:170,bottom=style==2?255:sheet?239:style==1?108:light?212:202;
  body=new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{(top<<24)|rgb,(bottom<<24)|rgb},null,Shader.TileMode.CLAMP);
  sheen=new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{light?0x58ffffff:0x30ffffff,0x0dffffff,0x00ffffff},new float[]{0,.32f,1},Shader.TileMode.CLAMP);
  edge=new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{0xb0ffffff,0x25ffffff,0x16ffffff,0x65ffffff},new float[]{0,.28f,.75f,1},Shader.TileMode.CLAMP);
  reflection=new RadialGradient(box.left+box.width()*.13f,box.top-box.height()*.25f,Math.max(1,box.width()*.9f),new int[]{0x25ffffff,0x00ffffff},null,Shader.TileMode.CLAMP);
  inner.set(rim);inner.inset(density*2,density*2);
  bevel=new LinearGradient(0,box.top,0,box.bottom,new int[]{light?0x18071630:0x38040b18,0x00000000,0x10000000,0x18ffffff},new float[]{0,.15f,.8f,1},Shader.TileMode.CLAMP);
  specularFade=new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{0x38ffffff,0x14ffffff,0x00ffffff},new float[]{0,.24f,1},Shader.TileMode.CLAMP);
  specular.reset();specular.moveTo(box.left+radius*.4f,box.top+radius*.8f);specular.cubicTo(box.left+box.width()*.12f,box.top-density*4,box.left+box.width()*.35f,box.top+box.height()*.14f,box.right-radius*.5f,box.top+box.height()*.24f);
  contrastQueued=false;
  frost=f==null||f.bitmap==null||style==2?null:new BitmapShader(f.bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
 }
 int foregroundColor(){return light?IosStyle.INK:Color.WHITE;}
 @Override public void draw(Canvas canvas){
  if(box.isEmpty())return;if(body==null||(backdrop!=null&&revision!=backdrop.revision))rebuild();Drawable.Callback ownerCallback=getCallback();for(int depth=0;depth<4&&ownerCallback instanceof Drawable;depth++)ownerCallback=((Drawable)ownerCallback).getCallback();if(!contrastQueued&&ownerCallback instanceof View){contrastQueued=true;View owner=(View)ownerCallback;final boolean currentLight=light;final int generation=revision;final Drawable.Callback attached=getCallback();owner.post(()->{if(getCallback()==attached&&revision==generation)IosStyle.contrast(owner,currentLight);});}paint.setShader(null);paint.setStyle(Paint.Style.FILL);
  shadow.set(box);shadow.offset(0,density*1.5f);paint.setColor(0x26000000);paint.setAlpha(opacity*38/255);canvas.drawRoundRect(shadow,radius,radius,paint);shadow.offset(0,-density*.8f);paint.setAlpha(opacity*24/255);canvas.drawRoundRect(shadow,radius,radius,paint);
  int save=canvas.save();canvas.clipPath(clip);
  if(frost!=null){GlassBackdrop.Frame f=backdrop.frame;Drawable.Callback callback=getCallback();if(callback instanceof View){View view=(View)callback;view.getLocationOnScreen(position);android.util.DisplayMetrics metrics=view.getResources().getDisplayMetrics();float scale=Math.max(metrics.widthPixels/(float)f.bitmap.getWidth(),metrics.heightPixels/(float)f.bitmap.getHeight());matrix.setScale(scale,scale);matrix.postTranslate((metrics.widthPixels-f.bitmap.getWidth()*scale)/2-position[0],(metrics.heightPixels-f.bitmap.getHeight()*scale)/2-position[1]);}else{float scale=Math.max(box.width()/f.bitmap.getWidth(),box.height()/f.bitmap.getHeight());matrix.setScale(scale,scale);matrix.postTranslate(box.centerX()-f.bitmap.getWidth()*scale/2,box.centerY()-f.bitmap.getHeight()*scale/2);}frost.setLocalMatrix(matrix);paint.setShader(frost);paint.setAlpha(opacity*(style==1?125:210)/255);canvas.drawRect(box,paint);}
  paint.setAlpha(opacity);paint.setShader(body);canvas.drawRect(box,paint);if(style!=2){paint.setShader(sheen);canvas.drawRect(box,paint);if(quality>0){paint.setShader(reflection);canvas.drawRect(box,paint);}}
  if(style!=2&&quality>0){
   paint.setStyle(Paint.Style.STROKE);paint.setShader(bevel);paint.setStrokeWidth(IosStyle.BEVEL_DP*density);canvas.drawRoundRect(inner,Math.max(0,radius-2*density),Math.max(0,radius-2*density),paint);
   paint.setShader(specularFade);paint.setStrokeWidth((quality==2?5.5f:3.5f)*density);paint.setStrokeCap(Paint.Cap.ROUND);canvas.drawPath(specular,paint);paint.setStrokeCap(Paint.Cap.BUTT);
  }canvas.restoreToCount(save);
  paint.setShader(edge);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(IosStyle.RIM_OUTER_DP*density);paint.setAlpha(opacity);canvas.drawRoundRect(rim,Math.max(0,radius-density*.5f),Math.max(0,radius-density*.5f),paint);if(style!=2&&quality>0){paint.setShader(specularFade);paint.setStrokeWidth(IosStyle.RIM_INNER_DP*density);canvas.drawRoundRect(inner,Math.max(0,radius-2*density),Math.max(0,radius-2*density),paint);}
  paint.setShader(null);
 }
 @Override public void getOutline(Outline outline){outline.setRoundRect((int)box.left,(int)box.top,(int)box.right,(int)box.bottom,radius);outline.setAlpha(style==2?1:.45f);}
 @Override public void setAlpha(int alpha){opacity=Math.max(0,Math.min(255,alpha));invalidateSelf();}
 @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
