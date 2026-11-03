package com.shazwan.idash.a1lab;
import android.content.Context;import android.util.TypedValue;import android.view.*;import android.widget.*;import java.util.ArrayList;
/** A bounded clock -> prayers -> calendar column. Fits during layout, never during drawing. */
final class HomeWidgets extends LinearLayout {
 private final ArrayList<Label> labels=new ArrayList<>();private final android.text.TextPaint measure=new android.text.TextPaint();private int lastWidth,lastHeight,fit;
 private static final float[] SCALE={1,.94f,.80f,.80f};
 private static final class Label {final TextView view;final float size,min;final boolean single;Label(TextView v,float s,float m){view=v;size=s;min=m;single=v.getAutoSizeTextType()!=TextView.AUTO_SIZE_TEXT_TYPE_NONE;}}
 HomeWidgets(Context c){super(c);setOrientation(VERTICAL);setContentDescription("Dashboard widgets");}
 void fitText(TextView view,float size,float min){Label label=new Label(view,size,min);labels.add(label);if(label.single)view.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);view.setIncludeFontPadding(false);}
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private void textSize(TextView view,float sp){float px=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,getResources().getDisplayMetrics());if(Math.abs(view.getTextSize()-px)>.01f)view.setTextSize(TypedValue.COMPLEX_UNIT_SP,sp);}
 private void profile(int selected,int width){int horizontal=dp(selected>=2?16:12),vertical=dp(selected==0?6:selected==1?4:selected==2?2:0);
  // The final fallback only reclaims spacing; keep every label's existing size floor.
  for(int i=0;i<getChildCount();i++){View child=getChildAt(i);if(child.getPaddingLeft()!=horizontal||child.getPaddingTop()!=vertical)child.setPadding(horizontal,vertical,horizontal,vertical);LayoutParams lp=(LayoutParams)child.getLayoutParams();lp.topMargin=i==0||selected==3?0:dp(selected==0?6:selected==1?4:1);}
  // Native autosize defers part of its work to layout. Measure bounded single-line text
  // synchronously so the natural height belongs to the selected profile immediately.
  for(Label label:labels){float size=Math.max(label.min,label.size*SCALE[selected]);if(label.single){float available=Math.max(1,width-2*horizontal-label.view.getCompoundPaddingLeft()-label.view.getCompoundPaddingRight());CharSequence value=label.view.getText();measure.set(label.view.getPaint());measure.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,size,getResources().getDisplayMetrics()));while(size>label.min&&measure.measureText(value,0,value.length())>available){size=Math.max(label.min,size-1);measure.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,size,getResources().getDisplayMetrics()));}}textSize(label.view,size);}
 }
 @Override protected void onMeasure(int widthSpec,int heightSpec){int width=MeasureSpec.getSize(widthSpec),height=MeasureSpec.getSize(heightSpec),mode=MeasureSpec.getMode(heightSpec);int selected=width==lastWidth&&height==lastHeight?fit:0;
  for(;selected<SCALE.length;selected++){profile(selected,width);super.onMeasure(widthSpec,MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));if(mode==MeasureSpec.UNSPECIFIED||getMeasuredHeight()<=height||selected==SCALE.length-1)break;}
  fit=selected;lastWidth=width;lastHeight=height;setMeasuredDimension(getMeasuredWidthAndState(),resolveSizeAndState(getMeasuredHeight(),heightSpec,getMeasuredState()<<MEASURED_HEIGHT_STATE_SHIFT));
 }
}
