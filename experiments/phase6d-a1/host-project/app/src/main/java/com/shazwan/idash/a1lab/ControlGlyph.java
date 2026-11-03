package com.shazwan.idash.a1lab;
import android.content.Context;import android.graphics.*;import android.view.View;
/** Original geometric glyphs, normalized to24 units; no external icon assets. */
final class ControlGlyph extends View {
 int type;final Paint p=new Paint(3);int color=Color.WHITE;
 ControlGlyph(Context c,int type){super(c);this.type=type;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 protected void onDraw(Canvas c){int save=c.save();float size=Math.min(getWidth(),getHeight())*IosStyle.CC_ICON_SCALE;c.translate((getWidth()-size)/2,(getHeight()-size)/2);c.scale(size/24,size/24);draw(c,p,type,color);c.restoreToCount(save);}
 static void draw(Canvas c,Paint p,int type,int color){p.setShader(null);p.setAlpha(255);p.setColor(color);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
 switch(type){
 case 15:c.drawLine(8,5,8,19,p);c.drawLine(16,5,16,19,p);break;
 case 0:c.drawArc(2,5,22,25,218,104,false,p);c.drawArc(6,10,18,22,220,100,false,p);c.drawCircle(12,19,1,p);break;
 case 1:c.drawLine(8,7,17,15,p);c.drawLine(17,15,12,21,p);c.drawLine(12,21,12,3,p);c.drawLine(12,3,17,8,p);c.drawLine(17,8,8,17,p);break;
 case 2:c.drawCircle(12,12,4,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine(12+(float)Math.cos(a)*7,12+(float)Math.sin(a)*7,12+(float)Math.cos(a)*10,12+(float)Math.sin(a)*10,p);}break;
 case 3:c.drawRect(3,9,7,15,p);c.drawLine(7,9,12,5,p);c.drawLine(12,5,12,19,p);c.drawLine(12,19,7,15,p);c.drawArc(10,5,22,19,-60,120,false,p);break;
 case 4:c.drawLine(3,13,21,7,p);c.drawLine(11,10,7,3,p);c.drawLine(14,9,19,2,p);c.drawLine(7,12,4,18,p);break;
 case 5:for(int i=0;i<4;i++)c.drawLine(5+i*5,20,5+i*5,16-i*4,p);break;
 case 6:c.drawLine(5,5,5,19,p);c.drawLine(18,5,8,12,p);c.drawLine(8,12,18,19,p);c.drawLine(18,19,18,5,p);break;
 case 7:c.drawLine(7,4,20,12,p);c.drawLine(20,12,7,20,p);c.drawLine(7,20,7,4,p);break;
 case 8:c.save();c.rotate(180,12,12);draw(c,p,6,color);c.restore();break;
 case 9:c.drawArc(3,3,21,21,45,270,false,p);c.drawArc(9,0,24,16,95,175,false,p);break;
 case 10:c.drawRect(7,3,17,7,p);c.drawLine(7,7,10,12,p);c.drawLine(17,7,14,12,p);c.drawRect(10,12,14,21,p);break;
 case 11:c.drawCircle(12,12,7,p);c.drawCircle(12,12,2.5f,p);for(int i=0;i<8;i++){c.save();c.rotate(i*45,12,12);c.drawLine(12,2,12,5,p);c.restore();}break;
 case 12:c.drawRoundRect(2,7,22,20,3,3,p);c.drawCircle(12,13,4,p);c.drawLine(7,7,9,3,p);c.drawLine(9,3,16,3,p);break;
 case 13:c.drawCircle(12,13,8,p);c.drawLine(9,2,15,2,p);c.drawLine(12,7,12,13,p);c.drawLine(12,13,16,15,p);break;
 case 14:c.drawRoundRect(5,2,19,22,3,3,p);c.drawRect(8,5,16,9,p);for(int y=13;y<21;y+=4)for(int x=8;x<18;x+=4)c.drawPoint(x,y,p);break;
 } }
}
