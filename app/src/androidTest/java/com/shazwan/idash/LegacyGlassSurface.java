package com.shazwan.idash;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Static translucent material: cached shaders, no backdrop capture or blur buffers. */
final class LegacyGlassSurface extends Drawable {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box=new RectF(),rim=new RectF();
    private final float radius,density;
    private final int tint,style;
    private final boolean sheet;
    private Shader body,sheen,edge;
    private int opacity=255;
    LegacyGlassSurface(float density,int tint,int radius,int style,boolean sheet){
        this.density=density;this.radius=radius*density;this.tint=tint;this.style=style;this.sheet=sheet;
    }
    private static int blend(int tint,int base,float amount){
        return Color.rgb(Math.round(Color.red(tint)*amount+Color.red(base)*(1-amount)),Math.round(Color.green(tint)*amount+Color.green(base)*(1-amount)),Math.round(Color.blue(tint)*amount+Color.blue(base)*(1-amount)));
    }
    @Override protected void onBoundsChange(Rect bounds){
        box.set(bounds);float width=Math.max(1,box.width()),height=Math.max(1,box.height());
        int rgb=blend(tint,0xff172338,.18f)&0xffffff;
        int top=style==2?255:sheet?(style==1?185:226):(style==1?68:144);
        int bottom=style==2?255:sheet?(style==1?211:242):(style==1?94:179);
        body=new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{(top<<24)|rgb,(bottom<<24)|rgb},null,Shader.TileMode.CLAMP);
        sheen=new LinearGradient(box.left,box.top,box.left+width*.65f,box.bottom,new int[]{style==2?0x08ffffff:style==1?0x20ffffff:0x38ffffff,0x09ffffff,0x00ffffff},new float[]{0,.35f,1},Shader.TileMode.CLAMP);
        edge=new LinearGradient(box.left,box.top,box.right,box.top+height,new int[]{0xb3ffffff,0x27ffffff,0x18ffffff,0x70ffffff},new float[]{0,.28f,.72f,1},Shader.TileMode.CLAMP);
        rim.set(box);rim.inset(density*.65f,density*.65f);
    }
    @Override public void draw(Canvas canvas){
        if(body==null||box.isEmpty())return;
        paint.setAlpha(opacity);paint.setStyle(Paint.Style.FILL);paint.setShader(body);canvas.drawRoundRect(box,radius,radius,paint);
        paint.setShader(sheen);canvas.drawRoundRect(box,radius,radius,paint);
        paint.setShader(edge);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(density);canvas.drawRoundRect(rim,Math.max(0,radius-density*.65f),Math.max(0,radius-density*.65f),paint);paint.setShader(null);
    }
    @Override public void getOutline(Outline outline){outline.setRoundRect(getBounds(),radius);outline.setAlpha(style==2?1:.5f);}
    @Override public void setAlpha(int alpha){opacity=Math.max(0,Math.min(255,alpha));invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
