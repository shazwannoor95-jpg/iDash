package dev.idash.mediafixture;
import android.app.*;import android.os.*;import android.media.*;import android.media.session.*;import android.widget.TextView;
/** Separate-UID MediaSession fixture; generates no audio, notifications or artwork. */
public class Main extends Activity {
 boolean toggleOnly;MediaSession session;int state=PlaybackState.STATE_PLAYING;String title="Fixture track one";
 void publish(){session.setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,title).putString(MediaMetadata.METADATA_KEY_ARTIST,"Fixture artist").build());session.setPlaybackState(new PlaybackState.Builder().setActions(toggleOnly?PlaybackState.ACTION_PLAY_PAUSE:PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS).setState(state,0,1).build());}
 public void onCreate(Bundle saved){super.onCreate(saved);session=new MediaSession(this,"iDash third-party fixture");session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS|MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);session.setCallback(new MediaSession.Callback(){public void onPlay(){state=PlaybackState.STATE_PLAYING;publish();}public void onPause(){state=PlaybackState.STATE_PAUSED;publish();}public void onSkipToNext(){title="Fixture track two";publish();}public void onSkipToPrevious(){title="Fixture track one";publish();}});publish();session.setActive(true);TextView label=new TextView(this);label.setText("iDash MediaSession test fixture. No sound is played.");setContentView(label);}
 @Override public void onNewIntent(android.content.Intent intent){super.onNewIntent(intent);toggleOnly=intent.getBooleanExtra("toggleOnly",false);publish();}
 public void onDestroy(){session.release();super.onDestroy();}
}
