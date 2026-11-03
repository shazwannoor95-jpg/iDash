package com.shazwan.idash;
import android.graphics.*;import android.view.*;import android.widget.SeekBar;
/** Vertical public-API slider; preserves existing SeekBar listeners and accessibility. */
final class CapsuleSlider extends SeekBar {
 private final Paint paint=new Paint(3);private final RectF bounds=new RectF();private final GlassSurface glass;private final int glyph;private Shader fill;private OnSeekBarChangeListener listener;
 CapsuleSlider(LauncherActivity a,int glyph){super(a);this.glyph=glyph;setPadding(0,0,0,0);setMinimumWidth(a.dp(56));setBackground(null);glass=new GlassSurface(a.getResources().getDisplayMetrics().density,0xff25324d,36,0,false,IosStyle.backdrop(a),IosStyle.quality(a),IosStyle.DARK,true);glass.appearance(GlassAppearance.active(a));glass.setCallback(this);}
 @Override public void setOnSeekBarChangeListener(OnSeekBarChangeListener l){listener=l;super.setOnSeekBarChangeListener(l);}
 @Override protected boolean verifyDrawable(android.graphics.drawable.Drawable who){return who==glass||super.verifyDrawable(who);}
 @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);float width=Math.min(w,72*getResources().getDisplayMetrics().density);bounds.set((w-width)/2,2,(w+width)/2,h-2);glass.setBounds((int)bounds.left,0,(int)bounds.right,h);bounds.inset(3*getResources().getDisplayMetrics().density,2*getResources().getDisplayMetrics().density);fill=new LinearGradient(0,bounds.top,getWidth(),bounds.bottom,new int[]{0xffffffff,0xffd7e8fb},null,Shader.TileMode.CLAMP);}
 @Override protected synchronized void onDraw(Canvas c){
  glass.draw(c);float fraction=getMax()==0?0:getProgress()/(float)getMax();float level=bounds.bottom-bounds.height()*fraction,radius=bounds.width()/2;int save=c.save();
  // Clip only the horizontal waterline; drawRoundRect keeps the curved edge anti-aliased on API27.
  c.clipRect(bounds.left,level,bounds.right,bounds.bottom);paint.setStyle(Paint.Style.FILL);paint.setShader(fill);paint.setAlpha(isEnabled()?255:140);c.drawRoundRect(bounds,radius,radius,paint);c.restoreToCount(save);paint.setShader(null);
  if(fraction>0&&fraction<1){float distance=level<bounds.top+radius?bounds.top+radius-level:level>bounds.bottom-radius?level-(bounds.bottom-radius):0;float half=(float)Math.sqrt(Math.max(0,radius*radius-distance*distance));paint.setColor(isPressed()?0xffffffff:0xffeaf4ff);paint.setStrokeWidth(getResources().getDisplayMetrics().density);paint.setStrokeCap(Paint.Cap.BUTT);c.drawLine(bounds.centerX()-half,level+.5f,bounds.centerX()+half,level+.5f,paint);}
  save=c.save();float size=26*getResources().getDisplayMetrics().density;c.translate(bounds.centerX()-size/2,bounds.bottom-size-14*getResources().getDisplayMetrics().density);c.scale(size/24,size/24);ControlGlyph.draw(c,paint,glyph,fraction>.25?0xff192d46:glass.foregroundColor());c.restoreToCount(save);
 }

 @Override public boolean onTouchEvent(android.view.MotionEvent e){if(!isEnabled())return false;int action=e.getActionMasked();if(action==MotionEvent.ACTION_DOWN){getParent().requestDisallowInterceptTouchEvent(true);setPressed(true);if(listener!=null)listener.onStartTrackingTouch(this);}if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_UP){int value=Math.round(Math.max(0,Math.min(1,1-(e.getY()-bounds.top)/Math.max(1,bounds.height())))*getMax());if(value!=getProgress()){setProgress(value);if(listener!=null)listener.onProgressChanged(this,value,true);}}if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){setPressed(false);getParent().requestDisallowInterceptTouchEvent(false);if(listener!=null)listener.onStopTrackingTouch(this);if(action==MotionEvent.ACTION_UP)performClick();}return true;}
 @Override public boolean performClick(){super.performClick();return true;}
}
