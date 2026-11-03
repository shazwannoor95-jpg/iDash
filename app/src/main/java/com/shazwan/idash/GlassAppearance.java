package com.shazwan.idash;
import android.content.*;import android.provider.Settings;
/** Immutable, validated visual preferences. A draft never writes to SharedPreferences. */
final class GlassAppearance {
 static final int MIN_TRANSPARENCY=10,MAX_TRANSPARENCY=65;
 final int tone,transparency,quality,tint,reflection;final boolean motion,customized;
 GlassAppearance(int tone,int transparency,int quality,int tint,int reflection,boolean motion,boolean customized){this.tone=clamp(tone,0,2);this.transparency=clamp(transparency,MIN_TRANSPARENCY,MAX_TRANSPARENCY);this.quality=clamp(quality,0,2);this.tint=clamp(tint,0,4);this.reflection=clamp(reflection,0,2);this.motion=motion;this.customized=customized;}
 static int clamp(int n,int lo,int hi){return Math.max(lo,Math.min(hi,n));}
 static int integer(SharedPreferences p,String key,int fallback){try{return p.getInt(key,fallback);}catch(ClassCastException bad){return fallback;}}
 static boolean bool(SharedPreferences p,String key,boolean fallback){try{return p.getBoolean(key,fallback);}catch(ClassCastException bad){return fallback;}}
 static GlassAppearance defaults(){return new GlassAppearance(0,45,1,0,1,true,true);}
 static GlassAppearance read(Context c){SharedPreferences p=c.getSharedPreferences("idash",0);return new GlassAppearance(integer(p,"glass_tone",0),integer(p,"glass_transparency",45),integer(p,"glass_quality",1),integer(p,"glass_tint",0),integer(p,"glass_reflection",1),bool(p,"motion",true),bool(p,"glass_customized",false)&&integer(p,"glass_style",0)==0);}
 static GlassAppearance active(Context c){GlassAppearance s=read(c);return s.customized?s:null;}
 GlassAppearance change(int field,int value){return new GlassAppearance(field==0?value:tone,field==1?value:transparency,field==2?value:quality,field==3?value:tint,field==4?value:reflection,field==5?value!=0:motion,true);}
 boolean same(GlassAppearance b){return tone==b.tone&&transparency==b.transparency&&quality==b.quality&&tint==b.tint&&reflection==b.reflection&&motion==b.motion&&customized==b.customized;}
 void save(Context c){c.getSharedPreferences("idash",0).edit().putBoolean("glass_customized",true).putInt("glass_style",0).putInt("glass_tone",tone).putInt("glass_transparency",transparency).putInt("glass_quality",quality).putInt("glass_tint",tint).putInt("glass_reflection",reflection).putBoolean("motion",motion).apply();}
 boolean animationAllowed(Context c){return motion&&Settings.Global.getFloat(c.getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1)>0;}
 int tintColor(int wallpaper){return tint==1?0xff88929f:tint==2?0xff83b7e7:tint==3?0xffdba7c3:tint==4?0xff9fc8ba:wallpaper;}
}
