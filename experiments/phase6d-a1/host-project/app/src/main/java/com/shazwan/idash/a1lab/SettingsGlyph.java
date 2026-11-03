package com.shazwan.idash.a1lab;
import android.content.Context;import android.graphics.*;import android.view.*;
/** Original Settings vectors. Reads the existing parent material's foreground; no new glass colors. */
final class SettingsGlyph extends View {
 final int type;final Paint p=new Paint(3);final Path path=new Path();
 SettingsGlyph(Context c,int type){super(c);this.type=type;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 protected void onDraw(Canvas canvas){int color=Color.WHITE;ViewParent parent=getParent();while(parent instanceof View){android.graphics.drawable.Drawable bg=((View)parent).getBackground();if(bg instanceof GlassSurface){color=((GlassSurface)bg).foregroundColor();break;}parent=parent.getParent();}int save=canvas.save();float side=Math.min(getWidth(),getHeight())*.72f;canvas.translate((getWidth()-side)/2,(getHeight()-side)/2);canvas.scale(side/24,side/24);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);path.reset();
  if(type==0){canvas.drawRoundRect(3,3,21,21,6,6,p);canvas.drawArc(6,6,18,18,210,120,false,p);canvas.drawCircle(12,15,2,p);}
  else if(type==1){path.moveTo(3,11);path.lineTo(12,3);path.lineTo(21,11);path.lineTo(21,21);path.lineTo(15,21);path.lineTo(15,14);path.lineTo(9,14);path.lineTo(9,21);path.lineTo(3,21);path.close();canvas.drawPath(path,p);}
  else if(type==2){canvas.drawArc(3,3,21,21,45,270,false,p);canvas.drawArc(9,0,24,16,95,175,false,p);canvas.drawLine(12,8,12,13,p);canvas.drawLine(12,13,16,15,p);}
  else if(type==3){canvas.drawRoundRect(2,4,22,20,3,3,p);canvas.drawLine(11,5,11,19,p);canvas.drawLine(15,9,19,12,p);canvas.drawLine(19,12,15,15,p);}
  else{canvas.drawCircle(12,12,9,p);canvas.drawLine(12,10,12,17,p);canvas.drawPoint(12,7,p);}canvas.restoreToCount(save);
 }
}
