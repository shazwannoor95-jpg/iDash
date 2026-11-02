package com.shazwan.idash;
import android.content.*;import android.graphics.*;import android.os.*;import android.view.*;import android.graphics.drawable.*;
/** API26 glass styling and bounded transitions; fonts are extracted MRiOS assets. */
final class IosStyle {
 static Typeface regular,medium,bold;
 static Typeface font(Context c,boolean strong){try{if(regular==null){regular=Typeface.createFromAsset(c.getAssets(),"mrios/fonts/sf_pro_display_regular.ttf");medium=Typeface.createFromAsset(c.getAssets(),"mrios/fonts/sf_pro_display_medium.ttf");bold=Typeface.createFromAsset(c.getAssets(),"mrios/fonts/sf_pro_display_bold.ttf");}return strong?bold:regular;}catch(RuntimeException absent){return strong?Typeface.DEFAULT_BOLD:Typeface.DEFAULT;}}
 static boolean motion(Context c){return c.getSharedPreferences("idash",0).getBoolean("motion",true)&&android.provider.Settings.Global.getFloat(c.getContentResolver(),android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,1)>0;}
 static void enter(View v){if(!motion(v.getContext()))return;v.setAlpha(.35f);v.setTranslationY(12*v.getResources().getDisplayMetrics().density);v.animate().alpha(1).translationY(0).setDuration(180).start();}
 static Drawable glass(Context c,int tint,int radius){return glass(c,tint,radius,false);}
 static Drawable glass(Context c,int tint,int radius,boolean sheet){return new GlassSurface(c.getResources().getDisplayMetrics().density,tint,radius,c.getSharedPreferences("idash",0).getInt("glass_style",0),sheet);}

 static final class Icon extends View {
  final int type;final Paint p=new Paint(3);final Path path=new Path();
  Icon(Context c,int type){super(c);this.type=type;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  protected void onDraw(Canvas c){c.save();c.scale(getWidth()/48f,getHeight()/48f);p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.6f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);path.reset();
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
