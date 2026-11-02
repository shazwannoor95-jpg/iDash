package com.shazwan.nexapanel;
import android.app.*;import android.content.*;import android.database.Cursor;import android.net.Uri;import android.content.ContentValues;import android.os.*;import android.view.*;import java.lang.ref.WeakReference;import java.util.*;
/** Metadata-only observation. Never reads view text, pixels, accounts or coordinates. */
public class Init extends ContentProvider implements Application.ActivityLifecycleCallbacks {
 static volatile WeakReference<Activity> active=new WeakReference<>(null);
 static final java.util.concurrent.ConcurrentHashMap<Integer,WeakReference<Activity>> displays=new java.util.concurrent.ConcurrentHashMap<>();
 static final WeakHashMap<Activity,String> phases=new WeakHashMap<>();
 static final Handler main=new Handler(Looper.getMainLooper());static Context context;
 static Activity onDisplay(int id){WeakReference<Activity> ref=displays.get(id);return ref==null?null:ref.get();}
 public boolean onCreate(){context=getContext().getApplicationContext();((Application)context).registerActivityLifecycleCallbacks(this);return true;}
 void event(Activity a,String phase){if(a instanceof RendererReport)return;phases.put(a,phase);int id=a.getWindowManager().getDefaultDisplay().getDisplayId();if("resumed".equals(phase))displays.put(id,new WeakReference<>(a));else if("created".equals(phase)||"started".equals(phase))displays.putIfAbsent(id,new WeakReference<>(a));save(a);}
 static String snapshot(Activity a){
  if(a==null)return "No Activity on requested display";
  View root=a.getWindow().getDecorView();StringBuilder out=new StringBuilder("renderer-probe-v1\n");out.append("capturedUptimeMs=").append(SystemClock.elapsedRealtime()).append("\n");
  out.append("activity=").append(a.getClass().getName()).append("\ndisplay=").append(a.getWindowManager().getDefaultDisplay().getDisplayId()).append("\nphase=").append(phases.get(a));
  out.append("\nwindowFocus=").append(root.hasWindowFocus()).append(" shown=").append(root.isShown()).append(" attached=").append(root.isAttachedToWindow()).append(" accelerated=").append(root.isHardwareAccelerated());
  out.append("\nroot=").append(root.getWidth()).append('x').append(root.getHeight()).append(" dpi=").append(a.getResources().getDisplayMetrics().densityDpi);
  ArrayDeque<View> queue=new ArrayDeque<>();queue.add(root);int visited=0,surfaces=0,textures=0,editors=0,details=0;
  while(!queue.isEmpty()&&visited<256){View v=queue.remove();visited++;if(v.onCheckIsTextEditor())editors++;
   if(v instanceof SurfaceView){surfaces++;if(details++<8){SurfaceView s=(SurfaceView)v;out.append("\nSurfaceView ").append(v.getWidth()).append('x').append(v.getHeight()).append(" visible=").append(v.getVisibility()).append(" shown=").append(v.isShown()).append(" valid=").append(s.getHolder().getSurface().isValid());}}
   if(v instanceof TextureView){textures++;if(details++<8)out.append("\nTextureView ").append(v.getWidth()).append('x').append(v.getHeight()).append(" shown=").append(v.isShown()).append(" available=").append(((TextureView)v).isAvailable());}
   if(v instanceof ViewGroup){ViewGroup group=(ViewGroup)v;for(int i=0;i<group.getChildCount()&&queue.size()<256;i++)queue.add(group.getChildAt(i));}
  }
  out.append("\nviews=").append(visited).append(" truncated=").append(!queue.isEmpty()).append(" surfaces=").append(surfaces).append(" textures=").append(textures).append(" editorCount=").append(editors);
  out.append("\nNo view text, map pixels, destination, account, GPS or network data collected. Surface validity is not proof of map content.");return out.toString();
 }
 static void save(Activity a){if(context==null)return;try{int id=a.getWindowManager().getDefaultDisplay().getDisplayId();WeakReference<Activity> current=displays.get(id);if(current!=null&&current.get()!=null&&current.get()!=a)return;android.content.SharedPreferences.Editor edit=context.getSharedPreferences("nexapanel_renderer",0).edit();String value=snapshot(a);edit.putString(id==0?"outside":"hosted",value);if("resumed".equals(phases.get(a)))edit.putString(id==0?"outside_resumed":"hosted_resumed",value);edit.apply();}catch(RuntimeException unavailable){/* A diagnostic must not crash the original app. */}}
 static String cachedReport(){if(context==null)return "No cached metadata";android.content.SharedPreferences p=context.getSharedPreferences("nexapanel_renderer",0);return "Last resumed hosted snapshot\n"+p.getString("hosted_resumed","Not observed")+"\nLast resumed outside snapshot\n"+p.getString("outside_resumed","Not observed")+"\nLast outside-panel snapshot\n"+p.getString("outside","Not observed")+"\nLast hosted snapshot\n"+p.getString("hosted","Not observed");}
 static String report(int id){Activity a=onDisplay(id);if(a!=null)save(a);return "Requested display snapshot\n"+snapshot(a)+"\n\n"+cachedReport();}

 public void onActivityResumed(Activity a){if(a instanceof RendererReport)return;active=new WeakReference<>(a);event(a,"resumed");WeakReference<Activity> ref=new WeakReference<>(a);for(long delay:new long[]{750,4000})main.postDelayed(()->{Activity live=ref.get();if(live!=null&&!live.isDestroyed())save(live);},delay);}
 public void onActivityCreated(Activity a,Bundle b){event(a,"created");}public void onActivityStarted(Activity a){event(a,"started");}public void onActivityPaused(Activity a){event(a,"paused");}public void onActivityStopped(Activity a){event(a,"stopped");}
 public void onActivityDestroyed(Activity a){if(a instanceof RendererReport)return;phases.put(a,"destroyed");save(a);phases.remove(a);if(active.get()==a)active=new WeakReference<>(null);int id=a.getWindowManager().getDefaultDisplay().getDisplayId();WeakReference<Activity> ref=displays.get(id);if(ref!=null&&ref.get()==a)displays.remove(id);}
 public void onActivitySaveInstanceState(Activity a,Bundle b){}
 public Cursor query(Uri u,String[] p,String s,String[] a,String sort){return null;}public String getType(Uri u){return null;}public Uri insert(Uri u,ContentValues v){return null;}public int delete(Uri u,String s,String[] a){return 0;}public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
}
