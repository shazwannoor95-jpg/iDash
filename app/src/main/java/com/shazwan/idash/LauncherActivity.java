package com.shazwan.idash;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.AudioManager;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

/** Original, dependency-free Android launcher. No privileged hosting or vehicle commands. */
public class LauncherActivity extends Activity {
    final Handler main = new Handler(Looper.getMainLooper());
    final ExecutorService worker = Executors.newSingleThreadExecutor();
    final ArrayList<App> apps = new ArrayList<>();
    final String[] slots = {"Bluetooth", "Files", "YouTube"};
    final int[] colors = {0xff007aff,0xff168cff,0xffff163d};
    final String[] symbols = {"ᛒ", "▤", "▶"};
    LinearLayout shell, body, grid; FrameLayout stage; EditText search;
    TextView clock, date, statusTime, drawerCount, jakimText;
    Map<String,JakimCalendar.Day> jakimDays=Collections.emptyMap();String loadedZone="",jakimDayKey=""; boolean jakimLoading;
    android.content.SharedPreferences prefs;
    String page = "home", query = "";
    boolean loaded, destroyed, resumed, foregroundVisible; ImageView wallpaperView; View wallpaperShade;
    int theme;
    boolean panelVisible; LinearLayout panelBox; DashNativeFrame nativePanel; DashWebPlayer webPanel; DashFmPanel fmPanel; SagaAngleView carPanel;
    Bitmap wallpaperBitmap; String requestedWallpaper; int wallpaperGeneration;
    final Runnable ticker = new Runnable() { public void run() { updateClock(); if (foregroundVisible) main.postDelayed(this, 1000); } };

    static final class App {
        String label, component; Bitmap icon;
        App(String l, String c, Bitmap i) { label=l; component=c; icon=i; }
    }
    int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); prefs=getSharedPreferences("idash",MODE_PRIVATE); theme=prefs.getInt("theme",0); migrateFavorites(); migratePanelPreferences();
        if(!prefs.contains("wallpaper"))prefs.edit().putString("wallpaper",theme==1?"":"mrios/wallpapers/47.jpg").apply();
        if(state!=null) { panelVisible=state.getBoolean("panel",false); page=state.getString("page","home"); query=state.getString("query",""); }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        immersive(); render(); loadApps();
    }
    void immersive() { getWindow().getDecorView().setSystemUiVisibility(5894); }
    @Override protected void onStart(){super.onStart();foregroundVisible=true;}
    @Override protected void onStop(){foregroundVisible=false;if(nativePanel!=null)nativePanel.pause();if(webPanel!=null)webPanel.pause();if(fmPanel!=null)fmPanel.pause();if(carPanel!=null)carPanel.pause();main.removeCallbacks(ticker);super.onStop();}
    @Override protected void onResume() { super.onResume(); resumed=true; main.removeCallbacks(ticker); ticker.run(); immersive();if(nativePanel!=null)nativePanel.resume();if(webPanel!=null)webPanel.resume();if(fmPanel!=null)fmPanel.resume();if(carPanel!=null)carPanel.resume(); }
    @Override protected void onPause() { resumed=false;if(nativePanel==null||!nativePanel.wanted){if(nativePanel!=null)nativePanel.pause();main.removeCallbacks(ticker);}if(webPanel!=null)webPanel.pause();if(fmPanel!=null)fmPanel.pause();if(carPanel!=null)carPanel.pause(); super.onPause(); }
    @Override protected void onDestroy() { destroyed=true;disposePanel(); main.removeCallbacksAndMessages(null); worker.shutdownNow(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle out) { out.putBoolean("panel",panelVisible);out.putString("page",page); out.putString("query",query); super.onSaveInstanceState(out); }
    @Override public void onConfigurationChanged(Configuration config) { super.onConfigurationChanged(config); render(); }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); page="home"; query=""; render(); }
    @Override public void onBackPressed() { if(webPanel!=null&&webPanel.exitFullscreen())return;if(!"home".equals(page)) { page="home"; query=""; hideKeyboard(); render(); }else if(panelVisible){panelVisible=false;render();} }
    void hideKeyboard() { ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(shell.getWindowToken(),0); }

    GradientDrawable glass(int color, int radius) {
        return IosStyle.glass(this,color,radius);
    }
    LinearLayout row() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    TextView text(String value, float size, int color) { TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); t.setTypeface(IosStyle.font(this,false));t.setFontFeatureSettings("tnum"); return t; }
    TextView button(String label, Runnable action) {
        TextView t=text(label,14,Color.WHITE); t.setGravity(Gravity.CENTER); t.setMinHeight(dp(48)); t.setPadding(dp(16),0,dp(16),0);
        t.setBackground(glass(0x39ffffff,22)); t.setContentDescription(label); t.setOnClickListener(v->action.run()); return t;
    }
    void title(LinearLayout target,String value,String subtitle) { TextView t=text(value,26,Color.WHITE); t.setTypeface(IosStyle.font(this,true)); target.addView(t); TextView sub=text(subtitle,12,0xcce6eaff); sub.setPadding(0,dp(3),0,dp(12)); target.addView(sub); }
    void render() {
        disposePanel();clock=null;date=null;jakimText=null;search=null;grid=null;
        stage=new FrameLayout(this); stage.addView(new Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        wallpaperView=new ImageView(this);wallpaperView.setScaleType(ImageView.ScaleType.CENTER_CROP);stage.addView(wallpaperView,new FrameLayout.LayoutParams(-1,-1));wallpaperShade=new View(this);wallpaperShade.setBackgroundColor(0x35000000);stage.addView(wallpaperShade,new FrameLayout.LayoutParams(-1,-1));updateBackdrop();
        ensureWallpaper();
        shell=column(); shell.setPadding(dp(28),dp(12),dp(28),dp(10));
        stage.addView(shell,new FrameLayout.LayoutParams(-1,-1)); setContentView(stage);
        // API35 edge-to-edge insets, also works on older devices with immersive bars.
        stage.setOnApplyWindowInsetsListener((v,insets)-> { shell.setPadding(dp(28)+insets.getSystemWindowInsetLeft(),dp(12)+insets.getSystemWindowInsetTop(),dp(28)+insets.getSystemWindowInsetRight(),dp(10)+insets.getSystemWindowInsetBottom()); return insets; });
        LinearLayout status=row(); statusTime=text("",13,Color.WHITE); statusTime.setTypeface(IosStyle.font(this,true)); status.addView(statusTime,new LinearLayout.LayoutParams(0,dp(48),1)); statusTime.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("iDash",13,0xcce6eaff); brand.setGravity(Gravity.CENTER); status.addView(brand,new LinearLayout.LayoutParams(dp(90),dp(48)));
        status.addView(headerAction("Panel",4,()->{panelVisible=!panelVisible;page="home";render();}));
        status.addView(headerAction("Customize",5,this::customize));
        status.addView(headerAction("Control Center",6,this::controlCenter)); shell.addView(status);
        body=column(); shell.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        if("apps".equals(page)) buildDrawer(); else buildHome();
        buildDock(); updateClock();IosStyle.enter(body);
    }
    void buildHome() {
        LinearLayout heading=row(); LinearLayout words=column();
        title(words,"Make yourself at home.","Your drive. Your space."); heading.addView(words,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.bottomMargin=dp(6);
        LinearLayout content=row(); content.setGravity(Gravity.TOP); body.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView widgetScroll=new ScrollView(this); widgetScroll.setFillViewport(false); widgetScroll.setVerticalScrollBarEnabled(false);
        LinearLayout widgets=column(); widgetScroll.addView(widgets); LinearLayout left=column();LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,-1,0.37f);wp.rightMargin=dp(24);content.addView(left,wp);left.addView(heading,hp);left.addView(widgetScroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout timeCard=column(); timeCard.setPadding(dp(22),dp(14),dp(22),dp(14)); timeCard.setBackground(glass(0x7c20395d,28));
        TextView eyebrow=text("LOCAL TIME",10,0xffbaceeb); eyebrow.setLetterSpacing(.16f);timeCard.addView(eyebrow);
        clock=text("",50,Color.WHITE); clock.setTypeface(IosStyle.font(this,false));timeCard.addView(clock);
        date=text("",14,0xffe0eaff); timeCard.addView(date); timeCard.setContentDescription("Clock widget"); timeCard.setOnClickListener(v->openSettings(Settings.ACTION_DATE_SETTINGS)); widgets.addView(timeCard,new LinearLayout.LayoutParams(-1,-2));
        if(prefs.getBoolean("calendar",true)) {
            LinearLayout calendar=column(); calendar.setPadding(dp(20),dp(14),dp(20),dp(14)); calendar.setBackground(glass(0x65ffffff,24));
            TextView label=text("TODAY",10,0xffe3cbff); label.setLetterSpacing(.16f); calendar.addView(label);
            TextView day=text(new SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(new Date()),19,Color.WHITE); calendar.addView(day);
            TextView hint=text("Tap to view your calendar",12,0xcce6eaff);calendar.addView(hint); calendar.setContentDescription("Calendar widget"); calendar.setOnClickListener(v->showCalendar());
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(12);widgets.addView(calendar,cp);
        }
        if(prefs.getBoolean("jakim",true)) {
            LinearLayout card=column();card.setPadding(dp(18),dp(12),dp(18),dp(12));card.setBackground(glass(0x682b426a,24));card.setContentDescription("JAKIM calendar widget");
            jakimText=text("",13,Color.WHITE);card.addView(jakimText);card.setOnClickListener(v->jakimDialog());LinearLayout.LayoutParams jp=new LinearLayout.LayoutParams(-1,-2);jp.topMargin=dp(12);widgets.addView(card,jp);updateJakim();loadJakim(false);
        }
        if(panelVisible){panelBox=column();panelBox.setPadding(dp(10),dp(8),dp(10),dp(8));panelBox.setBackground(glass(0x77324461,24));panelBox.setContentDescription("Optional media panel");LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,-1,.63f);pp.topMargin=dp(24);content.addView(panelBox,pp);buildPanel();}
        else content.addView(new View(this),new LinearLayout.LayoutParams(0,-1,.63f));
    }
    View shortcut(int slot,boolean dock) {
        LinearLayout tile=column(); tile.setGravity(Gravity.CENTER);tile.setPadding(dp(4),dp(5),dp(4),dp(5));
        App app=assigned(slot); int size=dock?42:54;
        FrameLayout icon=new FrameLayout(this);
        int[] tops={0xff36b4ff,0xff48baff,0xffff4c6c};
        int[] bottoms={0xff0055ee,0xff0660ed,0xffe60027};
        GradientDrawable tileFill=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{tops[slot],bottoms[slot]});
        tileFill.setCornerRadius(dp(dock?15:19));tileFill.setStroke(dp(1),0x65ffffff);icon.setBackground(tileFill);icon.setElevation(dp(dock?2:5));
        if(slot==1){ImageView files=new ImageView(this);files.setImageResource(R.drawable.mrios_files);files.setScaleType(ImageView.ScaleType.FIT_CENTER);icon.setClipToOutline(true);icon.addView(files,new FrameLayout.LayoutParams(-1,-1));}
        else icon.addView(new FavoriteGlyph(this,slot),new FrameLayout.LayoutParams(-1,-1));
        tile.addView(icon,new LinearLayout.LayoutParams(dp(size),dp(size)));
        TextView label=text(slots[slot],dock?10:12,Color.WHITE); label.setMaxLines(1);label.setEllipsize(TextUtils.TruncateAt.END);label.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(22));lp.topMargin=dp(3);tile.addView(label,lp);
        tile.setContentDescription("Favorite "+slots[slot]);tile.setOnClickListener(v->{App current=assigned(slot);if(current==null)pick(slot);else launch(current);});tile.setOnLongClickListener(v->{pick(slot);return true;});return tile;
    }
    void buildDock() {
        LinearLayout outer=row();outer.setGravity(Gravity.CENTER);LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-1,dp(83));op.topMargin=dp(8);shell.addView(outer,op);
        LinearLayout dock=row();dock.setGravity(Gravity.CENTER);dock.setPadding(dp(12),dp(3),dp(12),dp(3));dock.setBackground(glass(0x75e5eaff,28));dock.setElevation(dp(8));outer.addView(dock,new LinearLayout.LayoutParams(-2,-1));
        for(int i=0;i<slots.length;i++) dock.addView(shortcut(i,true),new LinearLayout.LayoutParams(dp(66),-1));
        View divider=new View(this);divider.setBackgroundColor(0x40ffffff);LinearLayout.LayoutParams d=new LinearLayout.LayoutParams(dp(1),dp(42));d.setMargins(dp(9),0,dp(9),0);dock.addView(divider,d);
        dock.addView(dockAction("⚙","Settings",()->openSettings(Settings.ACTION_SETTINGS)),new LinearLayout.LayoutParams(dp(66),-1));
        dock.addView(dockAction("▦","Apps",()-> {if("apps".equals(page)){page="home";query="";hideKeyboard();render();}else showApps();}),new LinearLayout.LayoutParams(dp(66),-1));
    }
    View dockAction(String glyph,String label,Runnable action) {
        LinearLayout l=column();l.setGravity(Gravity.CENTER);View icon;
        if("Settings".equals(label)){ImageView image=new ImageView(this);image.setImageResource(R.drawable.mrios_settings);icon=image;}
        else {TextView mark=text(glyph,27,Color.WHITE);mark.setGravity(Gravity.CENTER);mark.setBackground(glass(0x70ffffff,15));icon=mark;}
        l.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView name=text(label,10,Color.WHITE);name.setGravity(Gravity.CENTER);l.addView(name,new LinearLayout.LayoutParams(-1,dp(25)));l.setContentDescription(label);l.setOnClickListener(v->action.run());return l;
    }
    void showApps() {page="apps";render();}
    void buildDrawer() {
        LinearLayout header=row();LinearLayout words=column();title(words,"App Library","Everything, in one place.");header.addView(words,new LinearLayout.LayoutParams(0,-2,1));header.addView(button("Refresh",this::loadApps));header.addView(button("Done",()-> {page="home";query="";hideKeyboard();render();}));body.addView(header);
        search=new EditText(this);search.setSingleLine();search.setTextSize(16);search.setTextColor(Color.WHITE);search.setHintTextColor(0xffd0d8ec);search.setHint("Search apps");search.setContentDescription("Search apps");search.setPadding(dp(20),0,dp(20),0);search.setBackground(glass(0x55ffffff,18));search.setText(query);body.addView(search,new LinearLayout.LayoutParams(-1,dp(48)));
        drawerCount=text("",11,0xcce6eaff);drawerCount.setPadding(dp(6),dp(6),0,0);body.addView(drawerCount,new LinearLayout.LayoutParams(-1,dp(26)));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);grid=column();scroll.addView(grid);body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){query=s.toString();filter();}public void afterTextChanged(Editable e){}});filter();
    }
    void filter() {
        if(grid==null)return; grid.removeAllViews();String q=query.toLowerCase(Locale.ROOT).trim();ArrayList<App> filtered=new ArrayList<>();for(App a:apps)if(a.label.toLowerCase(Locale.ROOT).contains(q))filtered.add(a);
        drawerCount.setText(!loaded?"Loading installed apps…":filtered.size()+" apps");
        if(loaded&&filtered.isEmpty()){TextView empty=text("No apps found. Try another search.",17,Color.WHITE);empty.setPadding(dp(16),dp(24),0,0);grid.addView(empty);return;}
        int columns=Math.max(3,Math.min(8,(getResources().getDisplayMetrics().widthPixels/dp(1)-56)/115));
        for(int offset=0;offset<filtered.size();offset+=columns){LinearLayout r=row();r.setGravity(Gravity.TOP);grid.addView(r,new LinearLayout.LayoutParams(-1,dp(98)));for(int j=0;j<columns;j++){int index=offset+j;if(index>=filtered.size()){r.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));continue;}App app=filtered.get(index);LinearLayout tile=column();tile.setGravity(Gravity.CENTER);ImageView icon=new ImageView(this);icon.setImageBitmap(app.icon);tile.addView(icon,new LinearLayout.LayoutParams(dp(52),dp(52)));TextView name=text(app.label,12,Color.WHITE);name.setMaxLines(1);name.setEllipsize(TextUtils.TruncateAt.END);name.setGravity(Gravity.CENTER);tile.addView(name,new LinearLayout.LayoutParams(-1,dp(30)));tile.setContentDescription("Open "+app.label);tile.setOnClickListener(v->launch(app));tile.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Add "+app.label+" to favorites").setItems(slots,(d,which)->assign(which,app)).show();return true;});r.addView(tile,new LinearLayout.LayoutParams(0,-1,1));}}
    }
    App assigned(int slot) {String saved=prefs.getString("favorite_"+slots[slot],null);for(App a:apps)if(a.component.equals(saved))return a;return null;}
    void assign(int slot,App app) {prefs.edit().putString("favorite_"+slots[slot],app.component).apply();hideKeyboard();render();}
    void pick(int slot) {
        if(!loaded){Toast.makeText(this,"Apps are loading…",Toast.LENGTH_SHORT).show();return;}
        String[] labels=new String[apps.size()+1];labels[0]="Clear favorite";for(int i=0;i<apps.size();i++)labels[i+1]=apps.get(i).label;
        new AlertDialog.Builder(this).setTitle("Choose "+slots[slot]+" app").setItems(labels,(d,which)->{if(which==0){prefs.edit().putString("favorite_"+slots[slot],"").apply();render();}else assign(slot,apps.get(which-1));}).setNegativeButton("Cancel",null).show();
    }
    void launch(App a) {try{startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(ComponentName.unflattenFromString(a.component)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));if(IosStyle.motion(this))overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(RuntimeException e){Toast.makeText(this,"App unavailable. Hold the favorite to choose another.",Toast.LENGTH_LONG).show();loadApps();}}
    void loadApps() {
        if(worker.isShutdown())return;
        worker.execute(()->{
            PackageManager pm=getPackageManager();ArrayList<App> found=new ArrayList<>();Set<String> seen=new HashSet<>();
            for(ResolveInfo info:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)) {
                if(Thread.currentThread().isInterrupted())return;if(info.activityInfo.packageName.equals(getPackageName()))continue;
                String key=new ComponentName(info.activityInfo.packageName,info.activityInfo.name).flattenToString();if(!seen.add(key))continue;
                try{Drawable d=info.loadIcon(pm);Bitmap bitmap=Bitmap.createBitmap(dp(48),dp(48),Bitmap.Config.ARGB_8888);d.setBounds(0,0,bitmap.getWidth(),bitmap.getHeight());d.draw(new Canvas(bitmap));
                    String themed=MrIosTheme.icon(info.activityInfo.packageName);
                    if(prefs.getBoolean("mrios_icons",true)&&themed!=null)try{bitmap=MrIosTheme.decode(getAssets(),themed,dp(64));}catch(java.io.IOException ignored){}
                    found.add(new App(info.loadLabel(pm).toString(),key,bitmap));}catch(RuntimeException unavailable){/* Removed packages are skipped. */}
            }
            found.sort((a,b)->a.label.compareToIgnoreCase(b.label));
            main.post(()->{if(destroyed)return;apps.clear();apps.addAll(found);loaded=true;seedFavorites();if("apps".equals(page))filter();else if(!panelVisible)render();});
        });
    }
    void migrateFavorites() {
        if(prefs.getBoolean("three_favorites",false))return;
        android.content.SharedPreferences.Editor edit=prefs.edit();
        if(prefs.contains("slot5"))edit.putString("favorite_Bluetooth",prefs.getString("slot5",""));
        if(prefs.contains("slot4"))edit.putString("favorite_Files",prefs.getString("slot4",""));
        String oldVideo=prefs.getString("slot3","");
        ComponentName video=ComponentName.unflattenFromString(oldVideo);
        if(video!=null&&(video.getPackageName().equals("com.google.android.youtube")||video.getPackageName().equals("com.shazwan.nexadash.tizentube.next")))edit.putString("favorite_YouTube",oldVideo);
        edit.putBoolean("three_favorites",true).apply();
    }
    void migratePanelPreferences(){
        if(prefs.getBoolean("panel_modes_v2",false))return;
        android.content.SharedPreferences.Editor edit=prefs.edit().putInt("panel_mode",prefs.getInt("panel_mode",0)==1?3:0).putBoolean("panel_modes_v2",true);
        ComponentName previous=ComponentName.unflattenFromString(prefs.getString("favorite_YouTube",""));
        if(previous!=null&&"com.shazwan.idash.tizentube".equals(previous.getPackageName()))edit.putString("favorite_YouTube",new ComponentName("com.shazwan.idash.smarttube","com.liskovsoft.smartyoutubetv2.tv.ui.main.SplashActivity").flattenToString());
        edit.apply();
    }
    void seedFavorites() {
        String[][] hints={{"com.autochips.bluetooth"},{"com.android.documentsui","com.google.android.documentsui"},{"com.google.android.youtube","com.shazwan.idash.smarttube","org.smarttube.stable"}};
        android.content.SharedPreferences.Editor e=prefs.edit();for(int slot=0;slot<slots.length;slot++){if(prefs.contains("favorite_"+slots[slot]))continue;boolean done=false;for(String pkg:hints[slot]){for(App a:apps)if(ComponentName.unflattenFromString(a.component).getPackageName().equals(pkg)){e.putString("favorite_"+slots[slot],a.component);done=true;break;}if(done)break;}}e.apply();
    }
    void openSettings(String action) {try{startActivity(new Intent(action));}catch(RuntimeException unavailable){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(RuntimeException absent){Toast.makeText(this,"Settings unavailable on this device",Toast.LENGTH_SHORT).show();}}}
    void controlCenter() {
        LinearLayout panel=column();panel.setPadding(dp(20),dp(10),dp(20),dp(12));
        LinearLayout links=row();links.addView(controlTile("Wi-Fi",0,0xff007aff,()->openSettings(Settings.ACTION_WIFI_SETTINGS)),new LinearLayout.LayoutParams(0,dp(96),1));links.addView(controlTile("Bluetooth",1,0xff007aff,()->openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)),new LinearLayout.LayoutParams(0,dp(96),1));panel.addView(links);
        LinearLayout links2=row();links2.addView(controlTile("Display",2,0xff9b65ed,()->openSettings(Settings.ACTION_DISPLAY_SETTINGS)),new LinearLayout.LayoutParams(0,dp(96),1));links2.addView(controlTile("Sound",3,0xffef4878,()->openSettings(Settings.ACTION_SOUND_SETTINGS)),new LinearLayout.LayoutParams(0,dp(96),1));panel.addView(links2);
        TextView volume=text("Media volume",16,Color.WHITE);volume.setPadding(0,dp(18),0,0);panel.addView(volume);AudioManager audio=(AudioManager)getSystemService(AUDIO_SERVICE);SeekBar slider=new SeekBar(this);slider.setContentDescription("Media volume");slider.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));slider.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));slider.setMinimumHeight(dp(56));slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){if(user)try{audio.setStreamVolume(AudioManager.STREAM_MUSIC,n,0);}catch(SecurityException denied){Toast.makeText(LauncherActivity.this,"Use the head unit volume control",Toast.LENGTH_SHORT).show();}}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});panel.addView(slider);
        panel.addView(button("Choose default launcher",()->openSettings(Settings.ACTION_HOME_SETTINGS)));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Control Center").setView(panel).setPositiveButton("Done",null).create();dialog.show();if(dialog.getWindow()!=null){GradientDrawable backdrop=new GradientDrawable();int glassStyle=prefs.getInt("glass_style",0);backdrop.setColor(glassStyle==1?0x9025324d:glassStyle==2?0xff25324d:0xe025324d);backdrop.setCornerRadius(dp(28));backdrop.setStroke(dp(1),0x70ffffff);dialog.getWindow().setBackgroundDrawable(backdrop);IosStyle.enter(dialog.getWindow().getDecorView());dialog.getWindow().setLayout(dp(440),-2);}
    }
    void customize() {
        String[] options={"Calendar widget","Kalendar JAKIM","24-hour clock","Rose wallpaper","MRiOS icon theme","Smooth transitions"};boolean[] values={prefs.getBoolean("calendar",true),prefs.getBoolean("jakim",true),prefs.getBoolean("24h",true),theme==1,prefs.getBoolean("mrios_icons",true),prefs.getBoolean("motion",true)};
        new AlertDialog.Builder(this).setTitle("Make it yours").setNeutralButton("Appearance",(d,w)->appearancePicker()).setMultiChoiceItems(options,values,(d,index,checked)->values[index]=checked).setPositiveButton("Apply",(d,w)->{prefs.edit().putBoolean("calendar",values[0]).putBoolean("jakim",values[1]).putBoolean("24h",values[2]).putInt("theme",values[3]?1:0).putBoolean("mrios_icons",values[4]).putBoolean("motion",values[5]).apply();theme=values[3]?1:0;render();loadApps();}).setNegativeButton("Cancel",null).show();
    }
    View headerAction(String label,int icon,Runnable action){LinearLayout item=row();item.setGravity(Gravity.CENTER);item.setPadding(dp(10),0,dp(10),0);item.setBackground(glass(0x55e1eaff,20));item.setContentDescription(label);item.setMinimumHeight(dp(48));item.addView(new IosStyle.Icon(this,icon),new LinearLayout.LayoutParams(dp(22),dp(22)));TextView title=text(label,12,Color.WHITE);title.setPadding(dp(5),0,0,0);item.addView(title);item.setOnClickListener(v->action.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(48));lp.leftMargin=dp(6);item.setLayoutParams(lp);return item;}
    View controlTile(String label,int icon,int color,Runnable action){LinearLayout tile=column();tile.setGravity(Gravity.CENTER);FrameLayout disc=new FrameLayout(this);GradientDrawable fill=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{color,0xcc000000|(color&0xffffff)});fill.setCornerRadius(dp(18));disc.setBackground(fill);disc.addView(new IosStyle.Icon(this,icon),new FrameLayout.LayoutParams(-1,-1));tile.addView(disc,new LinearLayout.LayoutParams(dp(48),dp(48)));TextView caption=text(label,13,Color.WHITE);caption.setGravity(Gravity.CENTER);tile.addView(caption,new LinearLayout.LayoutParams(-1,dp(28)));tile.setContentDescription(label+" settings");tile.setOnClickListener(v->action.run());return tile;}
    void appearancePicker(){new AlertDialog.Builder(this).setTitle("Appearance").setItems(new String[]{"Glass","Transparent","Solid · high contrast","Wallpapers"},(d,index)->{if(index==3){wallpaperPicker();return;}prefs.edit().putInt("glass_style",index).apply();render();}).setNegativeButton("Cancel",null).show();}
    void disposePanel(){if(nativePanel!=null){nativePanel.close();nativePanel=null;}if(webPanel!=null){webPanel.close();webPanel=null;}if(fmPanel!=null){fmPanel.close();fmPanel=null;}if(carPanel!=null){carPanel.pause();carPanel=null;}panelBox=null;}
    void buildPanel(){
        LinearLayout header=row();TextView title=text("Media panel",15,Color.WHITE);header.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));header.addView(button("Mode",()->new AlertDialog.Builder(this).setTitle("Panel mode").setItems(new String[]{"YouTube web","Radio FM · antenna","Kereta · Saga","SmartTube · native companion"},(d,i)->{prefs.edit().putInt("panel_mode",i).apply();render();}).show()));header.addView(button("Close",()->{panelVisible=false;render();}));panelBox.addView(header);
        if(prefs.getInt("panel_mode",0)==3){nativePanel=new DashNativeFrame(this,"standalone");panelBox.addView(nativePanel,new LinearLayout.LayoutParams(-1,0,1));if(resumed)nativePanel.resume();}
        else if(prefs.getInt("panel_mode",0)==1){fmPanel=new DashFmPanel(this,getResources().getDisplayMetrics().density,()->new AlertDialog.Builder(this).setTitle("FM diagnostics").setMessage(DashFmController.report(this)).setPositiveButton("Close",null).show());panelBox.addView(fmPanel,new LinearLayout.LayoutParams(-1,0,1));if(resumed)fmPanel.resume();}
        else if(prefs.getInt("panel_mode",0)==2){carPanel=new SagaAngleView(this,worker,getSharedPreferences("vehicle",MODE_PRIVATE));carPanel.setContentDescription("Saga preview; drag to rotate, pinch to zoom");panelBox.addView(carPanel,new LinearLayout.LayoutParams(-1,0,1));TextView hint=text("Drag to rotate · Pinch to zoom · Double tap to reset",12,0xffc7d8ed);hint.setGravity(Gravity.CENTER);panelBox.addView(hint);if(resumed)carPanel.resume();}
        else {webPanel=new DashWebPlayer(this);panelBox.addView(webPanel,new LinearLayout.LayoutParams(-1,0,1));if(resumed)webPanel.resume();}
    }
    void wallpaperPicker() {
        try {String[] files=MrIosTheme.wallpapers(getAssets());String[] labels=new String[files.length+1];labels[0]="iDash gradient";for(int i=0;i<files.length;i++)labels[i+1]="MRiOS · "+files[i].replaceFirst("\\.[^.]+$","");
            new AlertDialog.Builder(this).setTitle("MRiOS wallpapers").setItems(labels,(d,index)->{prefs.edit().putString("wallpaper",index==0?"":"mrios/wallpapers/"+files[index-1]).apply();ensureWallpaper();render();}).setNegativeButton("Cancel",null).show();
        }catch(java.io.IOException unavailable){Toast.makeText(this,"Wallpaper gallery unavailable",Toast.LENGTH_SHORT).show();}
    }
    void updateBackdrop(){if(wallpaperView!=null){wallpaperView.setImageBitmap(wallpaperBitmap);wallpaperView.setVisibility(wallpaperBitmap==null?View.GONE:View.VISIBLE);}if(wallpaperShade!=null)wallpaperShade.setVisibility(wallpaperBitmap==null?View.GONE:View.VISIBLE);}
    void ensureWallpaper() {
        String path=prefs.getString("wallpaper","");if(path.equals(requestedWallpaper))return;requestedWallpaper=path;int generation=++wallpaperGeneration;
        if(path.isEmpty()){wallpaperBitmap=null;updateBackdrop();return;}
        if(worker.isShutdown())return;
        worker.execute(()->{Bitmap image=null;try{image=MrIosTheme.decode(getAssets(),path,1280);}catch(java.io.IOException ignored){}final Bitmap decoded=image;
            main.post(()->{if(destroyed||generation!=wallpaperGeneration)return;wallpaperBitmap=decoded;updateBackdrop();});});
    }
    void showCalendar() {Calendar c=Calendar.getInstance();DatePickerDialog dialog=new DatePickerDialog(this,(v,y,m,d)->{},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));dialog.setTitle("Calendar");dialog.show();}
    void updateClock() {Date now=new Date();if(!JakimCalendar.key(now.getTime()).equals(jakimDayKey))updateJakim();boolean h24=prefs.getBoolean("24h",true);if(clock!=null)clock.setText(new SimpleDateFormat(h24?"HH:mm":"h:mm a",Locale.getDefault()).format(now));if(date!=null)date.setText(new SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(now));if(statusTime!=null)statusTime.setText(new SimpleDateFormat(h24?"HH:mm  ·  EEE, d MMM":"h:mm a  ·  EEE, d MMM",Locale.getDefault()).format(now));}
    void loadJakim(boolean refresh) {
        String zone=prefs.getString("jakim_zone","");if(zone.isEmpty()||jakimLoading||(!refresh&&zone.equals(loadedZone)))return;jakimLoading=true;
        worker.execute(()->{String error="";Map<String,JakimCalendar.Day> days=Collections.emptyMap();try{if(refresh)JakimCalendar.refresh(this,zone);days=JakimCalendar.load(this,zone);}catch(Exception failed){error="Muat turun gagal; cache sedia ada dikekalkan";try{days=JakimCalendar.load(this,zone);}catch(Exception ignored){}}
            final Map<String,JakimCalendar.Day> result=days;final String message=error;
            main.post(()->{if(destroyed)return;jakimLoading=false;if(!zone.equals(prefs.getString("jakim_zone",""))){loadJakim(false);return;}loadedZone=zone;jakimDays=result;updateJakim();if(!message.isEmpty())Toast.makeText(this,message,Toast.LENGTH_LONG).show();});});
    }
    void updateJakim() {
        jakimDayKey=JakimCalendar.key(System.currentTimeMillis());if(jakimText==null)return;String zone=prefs.getString("jakim_zone","");
        if(zone.isEmpty()){jakimText.setText("KALENDAR JAKIM\nPilih zon waktu solat\nTekan untuk tetapan");return;}
        JakimCalendar.Day d=zone.equals(loadedZone)?jakimDays.get(jakimDayKey):null;
        if(d==null){jakimText.setText("JAKIM · "+zone+"\n"+jakimDayKey+" · MYT\n"+(jakimLoading?"Memuat jadual…":"Tiada jadual tarikh ini · tekan untuk kemas kini"));return;}
        jakimText.setText("JAKIM · "+zone+" · "+d.hijri+" H\nSubuh "+d.times[0]+"   Syuruk "+d.times[1]+"   Zohor "+d.times[2]+"\nAsar "+d.times[3]+"   Maghrib "+d.times[4]+"   Isyak "+d.times[5]+"\nTarikh peranti · MYT · Tekan untuk kalendar");
    }
    void jakimDialog() {
        String zone=prefs.getString("jakim_zone","");if(zone.isEmpty()){chooseJakimZone();return;}
        new AlertDialog.Builder(this).setTitle("Kalendar JAKIM · "+zone).setItems(new String[]{"Pilih tarikh / lihat jadual penuh","Tukar zon","Kemas kini daripada JAKIM"},(d,which)->{if(which==1)chooseJakimZone();else if(which==2){loadJakim(true);Toast.makeText(this,"Mengemas kini jadual…",Toast.LENGTH_SHORT).show();}else{Calendar c=Calendar.getInstance(JakimCalendar.MY);new DatePickerDialog(this,(picker,y,m,day)->{Calendar chosen=Calendar.getInstance(JakimCalendar.MY);chosen.clear();chosen.set(y,m,day,12,0);String key=JakimCalendar.key(chosen.getTimeInMillis());JakimCalendar.Day info=jakimDays.get(key);String message=info==null?"Tiada jadual rasmi untuk tarikh ini. Kemas kini jadual JAKIM diperlukan.":"Hijri: "+info.hijri+" H\n\n";if(info!=null)for(int i=0;i<6;i++)message+=JakimCalendar.NAMES[i]+"  "+info.times[i]+"\n";new AlertDialog.Builder(this).setTitle(zone+" · "+key).setMessage(message+"\nSumber: e-Solat JAKIM · MYT").setPositiveButton("Tutup",null).show();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}}).setNegativeButton("Tutup",null).show();
    }
    void chooseJakimZone() {
        String[] labels=Arrays.copyOf(JakimCalendar.LABELS,5);labels[4]="Zon lain (muat turun diperlukan)";
        new AlertDialog.Builder(this).setTitle("Pilih zon JAKIM").setItems(labels,(d,index)->{if(index<4)setJakimZone(JakimCalendar.ZONES[index]);else{EditText input=new EditText(this);input.setSingleLine();input.setHint("Contoh: JHR01");new AlertDialog.Builder(this).setTitle("Kod zon JAKIM").setView(input).setPositiveButton("Pilih",(dialog,w)->{String z=input.getText().toString().trim().toUpperCase(Locale.ROOT);if(JakimCalendar.validZone(z))setJakimZone(z);else Toast.makeText(this,"Kod zon tidak sah",Toast.LENGTH_LONG).show();}).setNegativeButton("Batal",null).show();}}).setNegativeButton("Batal",null).show();
    }
    void setJakimZone(String zone){prefs.edit().putString("jakim_zone",zone).apply();loadedZone="";jakimDays=Collections.emptyMap();updateJakim();loadJakim(false);}
    /** Original vector marks; no assets from the reference APK. */
    final class FavoriteGlyph extends View {
        final Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG);final Path path=new Path(); final int slot;
        FavoriteGlyph(Context context,int slot){super(context);this.slot=slot;}
        protected void onDraw(Canvas canvas){
            canvas.save();canvas.scale(getWidth()/64f,getHeight()/64f);ink.setColor(Color.WHITE);ink.setStyle(Paint.Style.FILL);path.reset();
            if(slot==0){ink.setStyle(Paint.Style.STROKE);ink.setStrokeWidth(3.6f);ink.setStrokeCap(Paint.Cap.ROUND);ink.setStrokeJoin(Paint.Join.ROUND);path.moveTo(22,23);path.lineTo(42,41);path.lineTo(31,50);path.lineTo(31,14);path.lineTo(42,23);path.lineTo(22,41);canvas.drawPath(path,ink);}
            else if(slot==1){ink.setColor(0xffc9eaff);canvas.drawRoundRect(13,18,32,30,3,3,ink);ink.setColor(Color.WHITE);canvas.drawRoundRect(11,24,53,48,4,4,ink);ink.setColor(0xffe6f4ff);canvas.drawRoundRect(13,27,51,46,2,2,ink);}
            else {canvas.drawRoundRect(10,18,54,46,8,8,ink);ink.setColor(0xfff00832);path.moveTo(28,25);path.lineTo(28,39);path.lineTo(40,32);path.close();canvas.drawPath(path,ink);}
            canvas.restore();
        }
    }
    final class Wallpaper extends View {
        final Paint p=new Paint(3); final Path ribbon=new Path(); Shader base, glow, wave;
        Wallpaper(Context c){super(c);}
        protected void onSizeChanged(int width,int height,int oldWidth,int oldHeight){
            float w=width,h=height;if(w<=0||h<=0)return;
            base=new LinearGradient(0,0,w,h,theme==1?new int[]{0xff442c59,0xff883e6a,0xff243954}:new int[]{0xff153855,0xff42447c,0xff6c427e},null,Shader.TileMode.CLAMP);
            glow=new RadialGradient(w*.88f,h*.04f,w*.67f,new int[]{theme==1?0x99ffccac:0x9961cbd5,0x00213762},null,Shader.TileMode.CLAMP);
            ribbon.reset();ribbon.moveTo(-w*.2f,h);ribbon.cubicTo(w*.1f,-h*.2f,w*.6f,h*1.3f,w*1.2f,h*.03f);ribbon.lineTo(w*1.2f,h*1.2f);ribbon.lineTo(-w*.2f,h*1.2f);ribbon.close();
            wave=new LinearGradient(0,h*.2f,w,h,new int[]{0x169dccff,0x957c7cda,0x44ebb9e3},null,Shader.TileMode.CLAMP);
        }
        protected void onDraw(Canvas c){p.setShader(base);c.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(glow);c.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(wave);c.drawPath(ribbon,p);p.setShader(null);}
    }
}
