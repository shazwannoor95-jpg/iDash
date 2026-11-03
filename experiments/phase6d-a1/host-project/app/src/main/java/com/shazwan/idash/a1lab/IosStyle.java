package com.shazwan.idash.a1lab;
import android.content.*;import android.graphics.*;import android.os.*;import android.view.*;import android.graphics.drawable.*;
/** API26 glass styling and bounded transitions; fonts are extracted MRiOS assets. */
final class IosStyle {
 static final int GLASS_BODY_ALPHA=140;
 static final int DARK_FROST_MULTIPLIER=166;
 static final ColorFilter DARK_FROST_FILTER=new LightingColorFilter(0xffa6a6a6,0);
 static final float GLASS_WALLPAPER_WEIGHT=.26f;
 static final int[] FROST_EDGE={128,224,320},FROST_RADIUS={2,4,6};
 static final float RIM_OUTER_DP=.85f,RIM_INNER_DP=.65f,BEVEL_DP=3f;
 // Control Center-only tokens. Existing dashboard/dock surfaces retain their original material.
 static final int CC_TITLE_SP=22,CC_PRIMARY_SP=20,CC_BODY_SP=14,CC_LABEL_SP=12,CC_VALUE_SP=13;
 static final int CC_TEXT=0xfffbfdff,CC_SECONDARY=0xffe1eaf6,CC_BASE=0xff283c54;
 static final float CC_DISABLED_ALPHA=.70f,CC_ICON_SCALE=.60f;
 static Drawable controlGlass(Context c,int radius,boolean sheet){return new GlassSurface(c.getResources().getDisplayMetrics().density,CC_BASE,radius,0,sheet,backdrop(c),quality(c),GlassAppearance.read(c).tone,true).appearance(GlassAppearance.active(c));}
 static final int INK=0xff172238,PAPER=0xfff2f5fa;
 static final int LIGHT_BASE=0xffe9eff7,DARK_BASE=0xff202832;
 static final int LOW=0,BALANCED=1,HIGH=2,AUTO=0,LIGHT=1,DARK=2;
 static final java.util.WeakHashMap<Context,GlassBackdrop> backdrops=new java.util.WeakHashMap<>();
 static GlassBackdrop backdrop(Context c){GlassBackdrop b=backdrops.get(c);if(b==null){b=new GlassBackdrop();backdrops.put(c,b);}return b;}
 static int quality(Context c){return GlassAppearance.read(c).quality;}
 static int gradientTint(Context c){return c instanceof LauncherActivity&&((LauncherActivity)c).theme==1?0xff704b68:0xff4b5379;}
 static void publishGlass(Context c,GlassBackdrop.Frame frame){if(frame.bitmap==null&&GlassAppearance.active(c)!=null)frame=new GlassBackdrop.Frame(null,gradientTint(c),frame.quality);backdrop(c).publish(frame);if(c instanceof LauncherActivity){AppearanceEditor editor=((LauncherActivity)c).appearanceEditor;if(editor!=null)editor.wallpaperChanged();}}
 static void releaseGlass(Context c){GlassBackdrop b=backdrops.remove(c);if(b!=null)b.publish(new GlassBackdrop.Frame(null,0xff526783));}
 static final java.util.WeakHashMap<android.widget.TextView,Integer> glassText=new java.util.WeakHashMap<>();
 static void contrast(View root,boolean light){
  if(root instanceof android.widget.TextView){android.widget.TextView text=(android.widget.TextView)root;Integer original=glassText.get(text);if(original==null&&Color.alpha(text.getCurrentTextColor())>128&&Color.luminance(text.getCurrentTextColor())>.6){original=text.getCurrentTextColor();glassText.put(text,original);}if(original!=null)text.setTextColor(light?INK:original);}
  if(root instanceof ControlGlyph){((ControlGlyph)root).color=light?INK:Color.WHITE;root.invalidate();}
  if(root instanceof Icon){((Icon)root).foreground=light?INK:Color.WHITE;root.invalidate();}
  if(root instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)root;for(int i=0;i<group.getChildCount();i++){View child=group.getChildAt(i);if(child.getBackground()==null)contrast(child,light);}}
 }
 static Typeface regular,medium,bold;
 static Typeface font(Context c,boolean strong){try{if(regular==null){regular=Typeface.createFromAsset(c.getAssets(),"mrios/fonts/sf_pro_display_regular.ttf");medium=Typeface.createFromAsset(c.getAssets(),"mrios/fonts/sf_pro_display_medium.ttf");bold=Typeface.createFromAsset(c.getAssets(),"mrios/fonts/sf_pro_display_bold.ttf");}return strong?bold:regular;}catch(RuntimeException absent){return strong?Typeface.DEFAULT_BOLD:Typeface.DEFAULT;}}
 static boolean motion(Context c){return GlassAppearance.read(c).animationAllowed(c);}
 static void enter(View v){if(!motion(v.getContext()))return;v.setAlpha(.35f);v.setTranslationY(12*v.getResources().getDisplayMetrics().density);v.animate().alpha(1).translationY(0).setDuration(180).start();}
 static Drawable glass(Context c,int tint,int radius){return glass(c,tint,radius,false);}
 static Drawable glass(Context c,int tint,int radius,boolean sheet){return new GlassSurface(c.getResources().getDisplayMetrics().density,tint,radius,GlassAppearance.clamp(GlassAppearance.integer(c.getSharedPreferences("idash",0),"glass_style",0),0,2),sheet,backdrop(c),quality(c),GlassAppearance.read(c).tone).appearance(GlassAppearance.active(c));}

 static final class Icon extends View {
  int foreground=Color.WHITE;final int type;final Paint p=new Paint(3);final Path path=new Path();
  Icon(Context c,int type){super(c);this.type=type;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  protected void onDraw(Canvas c){c.save();c.scale(getWidth()/48f,getHeight()/48f);p.setColor(foreground);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.6f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);path.reset();
   if(type==0){c.drawArc(6,14,42,44,218,104,false,p);c.drawArc(13,23,35,43,218,104,false,p);p.setStyle(Paint.Style.FILL);c.drawCircle(24,36,2.3f,p);}
   else if(type==1){path.moveTo(15,16);path.lineTo(33,32);path.lineTo(23,41);path.lineTo(23,7);path.lineTo(33,16);path.lineTo(15,32);c.drawPath(path,p);}
   else if(type==2){c.drawCircle(24,24,7,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine(24+(float)Math.cos(a)*12,24+(float)Math.sin(a)*12,24+(float)Math.cos(a)*17,24+(float)Math.sin(a)*17,p);}}
   else if(type==3){path.moveTo(9,19);path.lineTo(17,19);path.lineTo(26,12);path.lineTo(26,36);path.lineTo(17,29);path.lineTo(9,29);path.close();c.drawPath(path,p);c.drawArc(20,13,39,35,-60,120,false,p);c.drawArc(23,7,45,41,-60,120,false,p);}
   else if(type==4){c.drawRoundRect(7,11,41,37,5,5,p);c.drawLine(21,12,21,36,p);}
   else if(type==5){c.drawLine(10,14,38,14,p);c.drawLine(10,25,38,25,p);c.drawLine(10,36,38,36,p);c.drawCircle(19,14,4,p);c.drawCircle(30,25,4,p);c.drawCircle(21,36,4,p);}
   else if(type==6){c.drawLine(9,15,39,15,p);c.drawCircle(18,15,5,p);c.drawLine(9,33,39,33,p);c.drawCircle(31,33,5,p);}
   else{c.drawRoundRect(7,7,41,41,10,10,p);c.drawLine(24,16,24,32,p);c.drawLine(16,24,32,24,p);}
   c.restore();
  }
 }
}
