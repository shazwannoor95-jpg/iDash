package com.shazwan.idash.a1lab;
import android.service.notification.NotificationListenerService;import android.content.Intent;
/** System-bound only. Does not inspect, store or forward notification contents. */
public final class MediaAccessService extends NotificationListenerService {
 static final String CHANGED="com.shazwan.idash.a1lab.MEDIA_ACCESS_CHANGED";
 private void changed(){sendBroadcast(new Intent(CHANGED).setPackage(getPackageName()));}
 @Override public void onListenerConnected(){changed();}
 @Override public void onListenerDisconnected(){changed();}
}
