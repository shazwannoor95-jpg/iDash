package com.shazwan.idash.a1lab.target;
import android.app.*;import android.content.*;import android.content.pm.*;import android.os.*;import android.view.*;import android.view.inputmethod.*;import java.util.ArrayDeque;
/** NexaDash/iDash MIT protocol concept; explicit package, UID and signer authentication per call. */
public final class Bridge extends Service {
 static final String HOST="com.shazwan.idash.a1lab.host",TOKEN="com.shazwan.idash.a1lab.PROBE_CONTROL_V1";final Handler main=new Handler(Looper.getMainLooper());final ArrayDeque<Touch> events=new ArrayDeque<>();boolean closed;
 static final class Touch{final int display;final MotionEvent event;Touch(int d,MotionEvent e){display=d;event=e;}}
 void authenticate(){try{PackageManager pm=getPackageManager();if(pm.getApplicationInfo(HOST,0).uid!=Binder.getCallingUid()||pm.checkSignatures(HOST,getPackageName())!=PackageManager.SIGNATURE_MATCH)throw new SecurityException("Unauthorised A1 host");}catch(PackageManager.NameNotFoundException e){throw new SecurityException("A1 host missing");}}
 static boolean usable(ProbeActivity a,int requested){return a!=null&&!a.isFinishing()&&requested>0&&ProbeActivity.display==requested;}
 final Runnable deliver=()->{while(true){Touch t;synchronized(events){t=events.poll();}if(t==null)return;try{ProbeActivity a=ProbeActivity.current.get();if(usable(a,t.display))a.hostedTouch(t.event);}finally{t.event.recycle();}}};
 void enqueue(int id,MotionEvent e){synchronized(events){if(closed||(events.size()>8&&e.getActionMasked()==MotionEvent.ACTION_MOVE)){e.recycle();return;}if(events.size()>=32){for(Touch t:events)t.event.recycle();events.clear();MotionEvent cancel=MotionEvent.obtain(e);cancel.setAction(MotionEvent.ACTION_CANCEL);events.add(new Touch(id,cancel));}events.add(new Touch(id,e));}main.removeCallbacks(deliver);main.post(deliver);}
 final Binder binder=new Binder(){@Override protected boolean onTransact(int code,Parcel data,Parcel reply,int flags)throws RemoteException{if(code==INTERFACE_TRANSACTION){if(reply!=null)reply.writeString(TOKEN);return true;}authenticate();data.enforceInterface(TOKEN);
  if(code==8){reply.writeNoException();reply.writeInt(1);return true;}if(code==3){reply.writeNoException();reply.writeInt(ProbeActivity.current.get()==null?-1:ProbeActivity.display);return true;}if(code==2){reply.writeNoException();reply.writeInt(ProbeActivity.count.get());return true;}if(code==7){reply.writeNoException();reply.writeString(ProbeActivity.lastInput);return true;}
  if(code==6){int id=data.readInt();MotionEvent e=MotionEvent.CREATOR.createFromParcel(data);enqueue(id,e);return true;}
  if(code==4||code==5||code==9){int id=data.readInt();String text=code==5?data.readString():null;if(text!=null&&text.length()>240)throw new IllegalArgumentException("Input too long");main.post(()->{ProbeActivity a=ProbeActivity.current.get();if(!usable(a,id))return;if(code==9){a.stopAudio();a.finishAndRemoveTask();return;}if(code==4){a.onBackPressed();return;}View focus=a.getCurrentFocus();if(focus==null||!focus.onCheckIsTextEditor())return;InputConnection connection=focus.onCreateInputConnection(new EditorInfo());if(connection!=null){connection.commitText(text,1);connection.finishComposingText();}});return true;}
  return super.onTransact(code,data,reply,flags);
 }};
 @Override public IBinder onBind(Intent intent){return binder;}@Override public void onDestroy(){synchronized(events){closed=true;for(Touch t:events)t.event.recycle();events.clear();}main.removeCallbacksAndMessages(null);super.onDestroy();}
}
