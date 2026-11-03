package com.shazwan.idash;
import android.graphics.*;import android.graphics.drawable.Drawable;import android.view.View;import android.view.ViewGroup;import android.widget.TextView;
import java.lang.ref.WeakReference;
/** Original API27 glass: wallpaper frost, cached shaders, rim/reflection and inset shadow bands. */
final class GlassSurface extends Drawable {
 private final Paint paint=new Paint(3);private final RectF box=new RectF(),rim=new RectF(),shadow=new RectF();private final Path clip=new Path();private final Matrix matrix=new Matrix();private final int[] position=new int[2];
 private final float radius,density;private final int tint,style,tone;private final int baseQuality;private int quality;private GlassAppearance appearance;private final boolean sheet,premium;private final GlassBackdrop backdrop;
 private final java.util.ArrayList<Protection> protections=new java.util.ArrayList<>();private int protectionCount;private int protectionGeometry;private boolean protectionDirty=true;private int bodyRgb,bodyAlpha;
 private final Path specular=new Path();private final RectF inner=new RectF();private Shader bevel,specularFade;private boolean light,contrastQueued;
 private Shader body,sheen,edge,reflection;private ColorFilter externalFilter;private BitmapShader frost;private int opacity=255,revision=-1;
 GlassSurface(float d,int tint,int radius,int style,boolean sheet){this(d,tint,radius,style,sheet,null,1,0);}
 GlassSurface(float d,int tint,int radius,int style,boolean sheet,GlassBackdrop backdrop,int quality,int tone){this(d,tint,radius,style,sheet,backdrop,quality,tone,false);}
 GlassSurface(float d,int tint,int radius,int style,boolean sheet,GlassBackdrop backdrop,int quality,int tone,boolean premium){this.premium=premium;this.density=d;this.radius=radius*d;this.tint=tint;this.style=style;this.sheet=sheet;this.backdrop=backdrop;this.quality=quality;this.baseQuality=quality;this.tone=tone;if(backdrop!=null){if(backdrop.surfaces.size()>128)for(int i=backdrop.surfaces.size()-1;i>=0;i--)if(backdrop.surfaces.get(i).get()==null)backdrop.surfaces.remove(i);backdrop.surfaces.add(new WeakReference<>(this));}}
 GlassSurface appearance(GlassAppearance value){if(appearance==value||(appearance!=null&&value!=null&&appearance.same(value)))return this;appearance=value;quality=value==null?baseQuality:value.quality;if(!box.isEmpty())rebuild();invalidateSelf();return this;}
 private static int mix(int a,int b,float f){return Color.rgb(Math.round(Color.red(a)*f+Color.red(b)*(1-f)),Math.round(Color.green(a)*f+Color.green(b)*(1-f)),Math.round(Color.blue(a)*f+Color.blue(b)*(1-f)));}
 @Override protected void onBoundsChange(Rect bounds){box.set(bounds);box.inset(2*density,2*density);rim.set(box);rim.inset(density*.5f,density*.5f);clip.reset();clip.addRoundRect(box,radius,radius,Path.Direction.CW);rebuild();}
 private void rebuild(){
  GlassBackdrop.Frame frame=backdrop==null?null:backdrop.frame;
  revision=backdrop==null?0:backdrop.revision;
  int sampled=frame==null?tint:frame.tint;
  int selectedTone=appearance==null?tone:appearance.tone;
  light=selectedTone==IosStyle.LIGHT||(selectedTone==IosStyle.AUTO&&Color.luminance(sampled)>.35);
  int chosen=appearance==null?sampled:appearance.tintColor(sampled);
  bodyRgb=mix(chosen,light?IosStyle.LIGHT_BASE:IosStyle.DARK_BASE,IosStyle.GLASS_WALLPAPER_WEIGHT);
  // Existing percentages keep their absolute meaning. Only absent/reset defaults change.
  bodyAlpha=style==2?255:appearance!=null?Math.round(255*(1-appearance.transparency/100f)):style==1?65:IosStyle.GLASS_BODY_ALPHA;
  int bottom=style==2?255:style==1&&appearance==null?108:Math.min(245,bodyAlpha+8);
  body=new LinearGradient(0,box.top,0,box.bottom,new int[]{(bodyAlpha<<24)|(bodyRgb&0xffffff),(bottom<<24)|(bodyRgb&0xffffff)},null,Shader.TileMode.CLAMP);
  float strength=quality==IosStyle.LOW?.55f:appearance==null?1:appearance.reflection==0?.55f:appearance.reflection==2?1.25f:1;
  sheen=new LinearGradient(0,box.top,0,box.bottom,new int[]{scaled(0x14ffffff,strength),0x02ffffff,0x00ffffff},new float[]{0,.25f,1},Shader.TileMode.CLAMP);
  edge=new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{scaled(0xa0ffffff,strength),scaled(0x25ffffff,strength),0x08ffffff,scaled(0x45ffffff,strength)},new float[]{0,.24f,.72f,1},Shader.TileMode.CLAMP);
  reflection=new RadialGradient(box.left+radius,box.top-radius,Math.max(1,box.width()*.65f),new int[]{scaled(0x10ffffff,strength),0x00ffffff},null,Shader.TileMode.CLAMP);
  inner.set(rim);inner.inset(density*2,density*2);
  bevel=new LinearGradient(0,box.top,0,box.bottom,new int[]{0x18081120,0x00000000,0x0b000000,0x12ffffff},new float[]{0,.14f,.85f,1},Shader.TileMode.CLAMP);
  specularFade=new LinearGradient(box.left,box.top,box.left+box.width()*.55f,box.top+radius,new int[]{scaled(0x68ffffff,strength),scaled(0x20ffffff,strength),0x00ffffff},new float[]{0,.45f,1},Shader.TileMode.CLAMP);
  specular.reset();specular.moveTo(box.left+density*2,box.top+radius);
  specular.cubicTo(box.left+density*2,box.top+radius*.42f,box.left+radius*.42f,box.top+density*2,box.left+radius,box.top+density*2);
  specular.lineTo(box.left+Math.max(radius,box.width()*.46f),box.top+density*2);
  protectionDirty=true;contrastQueued=false;
  frost=frame==null||frame.bitmap==null||style==2?null:new BitmapShader(frame.bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
 }
 private static int scaled(int color,float value){return (Math.min(255,Math.round(Color.alpha(color)*value))<<24)|(color&0xffffff);}
 int foregroundColor(){return light?IosStyle.INK:Color.WHITE;}
 @Override public void draw(Canvas canvas){
  if(box.isEmpty())return;if(body==null||(backdrop!=null&&revision!=backdrop.revision))rebuild();Drawable.Callback ownerCallback=getCallback();for(int depth=0;depth<4&&ownerCallback instanceof Drawable;depth++)ownerCallback=((Drawable)ownerCallback).getCallback();if(!contrastQueued&&ownerCallback instanceof View){contrastQueued=true;View owner=(View)ownerCallback;final boolean currentLight=light;final int generation=revision;final Drawable.Callback attached=getCallback();owner.post(()->{if(getCallback()==attached&&revision==generation)IosStyle.contrast(owner,currentLight);});}if(!(ownerCallback instanceof View))protectionCount=0;if(ownerCallback instanceof View){View owner=(View)ownerCallback;owner.getLocationOnScreen(position);int geometry=31*(31*geometryHash(owner)+position[0])+position[1];if(protectionDirty||geometry!=protectionGeometry){protectionCount=0;protectionDirty=false;collectProtection(owner,0,0);protectionGeometry=geometry;}}paint.setShader(null);paint.setStyle(Paint.Style.FILL);
  shadow.set(box);shadow.offset(0,density*1.5f);paint.setColor(0x26000000);paint.setAlpha(opacity*38/255);canvas.drawRoundRect(shadow,radius,radius,paint);shadow.offset(0,-density*.8f);paint.setAlpha(opacity*24/255);canvas.drawRoundRect(shadow,radius,radius,paint);
  int save=canvas.save();canvas.clipPath(clip);
  if(frost!=null){GlassBackdrop.Frame f=backdrop.frame;Drawable.Callback callback=getCallback();if(callback instanceof View){View view=(View)callback;view.getLocationOnScreen(position);android.util.DisplayMetrics metrics=view.getResources().getDisplayMetrics();float scale=Math.max(metrics.widthPixels/(float)f.bitmap.getWidth(),metrics.heightPixels/(float)f.bitmap.getHeight());matrix.setScale(scale,scale);matrix.postTranslate((metrics.widthPixels-f.bitmap.getWidth()*scale)/2-position[0],(metrics.heightPixels-f.bitmap.getHeight()*scale)/2-position[1]);}else{float scale=Math.max(box.width()/f.bitmap.getWidth(),box.height()/f.bitmap.getHeight());matrix.setScale(scale,scale);matrix.postTranslate(box.centerX()-f.bitmap.getWidth()*scale/2,box.centerY()-f.bitmap.getHeight()*scale/2);}frost.setLocalMatrix(matrix);paint.setShader(frost);paint.setColorFilter(externalFilter!=null?externalFilter:light?null:IosStyle.DARK_FROST_FILTER);paint.setAlpha(opacity*(style==0||appearance!=null?255:(style==1?125:210))/255);canvas.drawRect(box,paint);paint.setColorFilter(externalFilter);}
  paint.setAlpha(opacity);paint.setShader(body);canvas.drawRect(box,paint);if(style!=2){paint.setShader(sheen);canvas.drawRect(box,paint);if(quality>0){paint.setShader(reflection);canvas.drawRect(box,paint);}}
  if(style!=2&&quality>0){
   paint.setStyle(Paint.Style.STROKE);paint.setShader(bevel);paint.setStrokeWidth((style==0?2:IosStyle.BEVEL_DP)*density);canvas.drawRoundRect(inner,Math.max(0,radius-2*density),Math.max(0,radius-2*density),paint);
   paint.setShader(specularFade);paint.setStrokeWidth((style==0?(quality==2?1.8f:1.2f):(quality==2?5.5f:3.5f))*density);paint.setStrokeCap(Paint.Cap.ROUND);canvas.drawPath(specular,paint);paint.setStrokeCap(Paint.Cap.BUTT);
  }
  if(style!=2){paint.setStyle(Paint.Style.FILL);paint.setAlpha(opacity);for(int i=0;i<protectionCount;i++){Protection p=protections.get(i);paint.setShader(p.shader);canvas.drawOval(p.bounds,paint);}}
  canvas.restoreToCount(save);
  paint.setShader(edge);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(IosStyle.RIM_OUTER_DP*density);paint.setAlpha(opacity);canvas.drawRoundRect(rim,Math.max(0,radius-density*.5f),Math.max(0,radius-density*.5f),paint);if(style!=2&&quality>0){paint.setShader(specularFade);paint.setStrokeWidth(IosStyle.RIM_INNER_DP*density);canvas.drawRoundRect(inner,Math.max(0,radius-2*density),Math.max(0,radius-2*density),paint);}
  paint.setShader(null);
 }
 // Geometry and nine low-resolution samples are cached per material/bounds revision.
 // This protects content locally rather than forcing the complete glass panel opaque.
 private void collectProtection(View view,float x,float y){
  if(!view.isEnabled())return;
  if(view.getWidth()==0||view.getHeight()==0){protectionDirty=true;return;}
  boolean text=view instanceof TextView, glyph=view instanceof ControlGlyph||view instanceof IosStyle.Icon;
  if(text){TextView label=(TextView)view;android.text.Layout layout=label.getLayout();if(layout!=null){
   int available=label.getHeight()-label.getExtendedPaddingTop()-label.getExtendedPaddingBottom();int gravity=label.getGravity()&android.view.Gravity.VERTICAL_GRAVITY_MASK;
   float dy=label.getExtendedPaddingTop()+(gravity==android.view.Gravity.CENTER_VERTICAL?Math.max(0,available-layout.getHeight())/2f:gravity==android.view.Gravity.BOTTOM?Math.max(0,available-layout.getHeight()):0);
   for(int line=0;line<layout.getLineCount();line++){
    float left=x+label.getCompoundPaddingLeft()+layout.getLineLeft(line),right=x+label.getCompoundPaddingLeft()+layout.getLineRight(line),top=y+dy+layout.getLineTop(line),bottom=y+dy+layout.getLineBottom(line);
    if(right>left&&bottom<=y+view.getHeight()+1 ){int fg=textInk(label);int alpha=protectionAlpha(left,top,Math.round(right-left),Math.round(bottom-top),fg,4.9);if(alpha>0)addProtection(left,top,right,bottom,fg,alpha);}
   }
  }}else if(glyph){int fg=foregroundColor(),alpha=protectionAlpha(x,y,view.getWidth(),view.getHeight(),fg,3.8);if(alpha>0)addProtection(x,y,x+view.getWidth(),y+view.getHeight(),fg,alpha);}
  if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){View child=group.getChildAt(i);if(child.getVisibility()!=View.GONE&&(child.getBackground()==null||child instanceof android.widget.Spinner||child instanceof android.widget.Switch))collectProtection(child,x+child.getLeft()+child.getTranslationX()-group.getScrollX(),y+child.getTop()+child.getTranslationY()-group.getScrollY());}}
 }
 private int textInk(TextView text){Integer original=IosStyle.glassText.get(text);int color=original==null?text.getCurrentTextColor():original;return (original!=null||(Color.alpha(color)>128&&Color.luminance(color)>.6))&&light?IosStyle.INK:color;}
 private static final class Protection {
  final RectF bounds=new RectF();final Matrix matrix=new Matrix();Shader shader;int color;
  void update(float l,float t,float r,float b,float padding,int argb){
   float dx=(r-l)*.3f+padding,dy=(b-t)*.6f+padding;bounds.set(l-dx,t-dy,r+dx,b+dy);
   if(shader==null||color!=argb){color=argb;shader=new RadialGradient(0,0,1,new int[]{argb,argb,argb&0xffffff},new float[]{0,.8f,1},Shader.TileMode.CLAMP);}
   matrix.setScale(bounds.width()/2,bounds.height()/2);matrix.postTranslate(bounds.centerX(),bounds.centerY());shader.setLocalMatrix(matrix);
  }
 }
 private void addProtection(float l,float t,float r,float b,int fg,int alpha){
  Protection region;if(protectionCount==protections.size()){region=new Protection();protections.add(region);}else region=protections.get(protectionCount);protectionCount++;
  int rgb=Color.luminance(fg)>.4?0x07101c:0xffffff;region.update(l,t,r,b,10*density,(alpha<<24)|rgb);
 }
 private int geometryHash(View view){int h=31*(31*(31*view.getLeft()+view.getTop())+view.getWidth())+view.getHeight();h=31*h+view.getScrollX();h=31*h+view.getScrollY();h=31*h+(view.isEnabled()?1:0);h=31*h+Float.floatToIntBits(view.getAlpha());h=31*h+Float.floatToIntBits(view.getTranslationX());h=31*h+Float.floatToIntBits(view.getTranslationY());if(view instanceof TextView){android.text.Layout layout=((TextView)view).getLayout();if(layout!=null){h=31*h+layout.getLineCount();for(int i=0;i<layout.getLineCount();i++){h=31*h+Float.floatToIntBits(layout.getLineRight(i));h=31*h+Float.floatToIntBits(layout.getLineLeft(i));}}}if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++){View child=g.getChildAt(i);if(child.getBackground()==null||child instanceof android.widget.Spinner||child instanceof android.widget.Switch)h=31*h+geometryHash(child);}}return h;}
 private static double contrast(int fg,int bg){float a=Color.alpha(fg)/255f;int ink=mix(fg,bg,a);double l1=Color.luminance(ink),l2=Color.luminance(bg);return (Math.max(l1,l2)+.05)/(Math.min(l1,l2)+.05);}
 private int protectionAlpha(float x,float y,int w,int h,int foreground,double target){
  int maximum=0,scrim=Color.luminance(foreground)>.4?0xff07101c:Color.WHITE;
  if(backdrop==null||backdrop.frame.bitmap==null){int worst=Color.luminance(foreground)>.4?Color.WHITE:Color.BLACK;return requiredAlpha(foreground,mix(bodyRgb,worst,bodyAlpha/255f),scrim,target);}
  Bitmap image=backdrop.frame.bitmap;Drawable.Callback owner=getCallback();while(owner instanceof Drawable)owner=((Drawable)owner).getCallback();
  int screenW=Math.max(1,(int)box.width()),screenH=Math.max(1,(int)box.height());
  if(owner instanceof View){View v=(View)owner;v.getLocationOnScreen(position);screenW=v.getResources().getDisplayMetrics().widthPixels;screenH=v.getResources().getDisplayMetrics().heightPixels;}else{position[0]=position[1]=0;}
  float scale=Math.max(screenW/(float)image.getWidth(),screenH/(float)image.getHeight());
  for(int row=1;row<=3;row++)for(int col=1;col<=3;col++){
   int px=Math.max(0,Math.min(image.getWidth()-1,(int)((position[0]+x+w*((col-1)*.4f+.1f)-(screenW-image.getWidth()*scale)/2)/scale)));
   int py=Math.max(0,Math.min(image.getHeight()-1,(int)((position[1]+y+h*((row-1)*.4f+.1f)-(screenH-image.getHeight()*scale)/2)/scale)));
   int substrate=image.getPixel(px,py);if(!light){float transmission=IosStyle.DARK_FROST_MULTIPLIER/255f;substrate=Color.rgb(Math.round(Color.red(substrate)*transmission),Math.round(Color.green(substrate)*transmission),Math.round(Color.blue(substrate)*transmission));}int bg=mix(bodyRgb,substrate,bodyAlpha/255f);
   maximum=Math.max(maximum,requiredAlpha(foreground,bg,scrim,target));
  }return maximum;
 }
 private static int requiredAlpha(int foreground,int bg,int scrim,double target){if(Color.luminance(foreground)>.4)bg=mix(Color.WHITE,bg,.08f);if(contrast(foreground,bg)>=target)return 0;int lo=0,hi=160;while(hi-lo>4){int mid=(lo+hi)/2;if(contrast(foreground,mix(scrim,bg,mid/255f))>=target)hi=mid;else lo=mid;}return hi;}
 @Override public void getOutline(Outline outline){outline.setRoundRect((int)box.left,(int)box.top,(int)box.right,(int)box.bottom,radius);outline.setAlpha(style==2?1:.45f);}
 @Override public void setAlpha(int alpha){opacity=Math.max(0,Math.min(255,alpha));invalidateSelf();}
 @Override public void setColorFilter(ColorFilter filter){externalFilter=filter;paint.setColorFilter(filter);invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
