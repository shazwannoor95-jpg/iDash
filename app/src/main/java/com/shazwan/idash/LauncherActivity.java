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
    TextView clock, date, statusTime, batteryLabel, drawerCount;
    android.content.SharedPreferences prefs;
    String page = "home", query = "";
    boolean loaded, destroyed, resumed;
    int theme;
    final Runnable ticker = new Runnable() { public void run() { updateClock(); if (resumed) main.postDelayed(this, 1000); } };

    static final class App {
        String label, component; Bitmap icon;
        App(String l, String c, Bitmap i) { label=l; component=c; icon=i; }
    }
    int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); prefs=getSharedPreferences("idash",MODE_PRIVATE); theme=prefs.getInt("theme",0); migrateFavorites();
        if(state!=null) { page=state.getString("page","home"); query=state.getString("query",""); }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        immersive(); render(); loadApps();
    }
    void immersive() { getWindow().getDecorView().setSystemUiVisibility(5894); }
    @Override protected void onResume() { super.onResume(); resumed=true; main.removeCallbacks(ticker); ticker.run(); immersive(); }
    @Override protected void onPause() { resumed=false; main.removeCallbacks(ticker); super.onPause(); }
    @Override protected void onDestroy() { destroyed=true; main.removeCallbacksAndMessages(null); worker.shutdownNow(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle out) { out.putString("page",page); out.putString("query",query); super.onSaveInstanceState(out); }
    @Override public void onConfigurationChanged(Configuration config) { super.onConfigurationChanged(config); render(); }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); page="home"; query=""; render(); }
    @Override public void onBackPressed() { if(!"home".equals(page)) { page="home"; query=""; hideKeyboard(); render(); } }
    void hideKeyboard() { ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(shell.getWindowToken(),0); }

    GradientDrawable glass(int color, int radius) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{color, (color & 0x00ffffff)|0x27000000});
        d.setCornerRadius(dp(radius)); d.setStroke(dp(1),0x32ffffff); return d;
    }
    LinearLayout row() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    TextView text(String value, float size, int color) { TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); t.setFontFeatureSettings("tnum"); return t; }
    TextView button(String label, Runnable action) {
        TextView t=text(label,14,Color.WHITE); t.setGravity(Gravity.CENTER); t.setMinHeight(dp(48)); t.setPadding(dp(16),0,dp(16),0);
        t.setBackground(glass(0x39ffffff,22)); t.setContentDescription(label); t.setOnClickListener(v->action.run()); return t;
    }
    void title(LinearLayout target,String value,String subtitle) { TextView t=text(value,26,Color.WHITE); t.setTypeface(null,Typeface.BOLD); target.addView(t); TextView sub=text(subtitle,12,0xcce6eaff); sub.setPadding(0,dp(3),0,dp(12)); target.addView(sub); }
    void render() {
        clock=null;date=null;batteryLabel=null;search=null;grid=null;
        stage=new FrameLayout(this); stage.addView(new Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        shell=column(); shell.setPadding(dp(28),dp(12),dp(28),dp(10));
        stage.addView(shell,new FrameLayout.LayoutParams(-1,-1)); setContentView(stage);
        // API35 edge-to-edge insets, also works on older devices with immersive bars.
        stage.setOnApplyWindowInsetsListener((v,insets)-> { shell.setPadding(dp(28)+insets.getSystemWindowInsetLeft(),dp(12)+insets.getSystemWindowInsetTop(),dp(28)+insets.getSystemWindowInsetRight(),dp(10)+insets.getSystemWindowInsetBottom()); return insets; });
        LinearLayout status=row(); statusTime=text("",13,Color.WHITE); statusTime.setTypeface(null,Typeface.BOLD); status.addView(statusTime,new LinearLayout.LayoutParams(0,dp(48),1)); statusTime.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("iDash",13,0xcce6eaff); brand.setGravity(Gravity.CENTER); status.addView(brand,new LinearLayout.LayoutParams(dp(90),dp(48)));
        TextView controls=button("◉  Control Center",this::controlCenter); status.addView(controls); shell.addView(status);
        body=column(); shell.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        if("apps".equals(page)) buildDrawer(); else buildHome();
        buildDock(); updateClock();
    }
    void buildHome() {
        LinearLayout heading=row(); LinearLayout words=column();
        title(words,"Make yourself at home.","Your drive. Your space."); heading.addView(words,new LinearLayout.LayoutParams(0,-2,1));
        heading.addView(button("Customize",this::customize)); LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.bottomMargin=dp(6);body.addView(heading,hp);
        LinearLayout content=row(); content.setGravity(Gravity.TOP); body.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView widgetScroll=new ScrollView(this); widgetScroll.setFillViewport(false); widgetScroll.setVerticalScrollBarEnabled(false);
        LinearLayout widgets=column(); widgetScroll.addView(widgets); LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,-1,0.37f);wp.rightMargin=dp(24); content.addView(widgetScroll,wp);
        LinearLayout timeCard=column(); timeCard.setPadding(dp(22),dp(14),dp(22),dp(14)); timeCard.setBackground(glass(0x7c20395d,28));
        TextView eyebrow=text("LOCAL TIME",10,0xffbaceeb); eyebrow.setLetterSpacing(.16f);timeCard.addView(eyebrow);
        clock=text("",58,Color.WHITE); clock.setTypeface(android.graphics.Typeface.create("sans-serif-light",Typeface.NORMAL));timeCard.addView(clock);
        date=text("",14,0xffe0eaff); timeCard.addView(date); timeCard.setContentDescription("Clock widget"); timeCard.setOnClickListener(v->openSettings(Settings.ACTION_DATE_SETTINGS)); widgets.addView(timeCard,new LinearLayout.LayoutParams(-1,-2));
        if(prefs.getBoolean("calendar",true)) {
            LinearLayout calendar=column(); calendar.setPadding(dp(20),dp(14),dp(20),dp(14)); calendar.setBackground(glass(0x65ffffff,24));
            TextView label=text("TODAY",10,0xffe3cbff); label.setLetterSpacing(.16f); calendar.addView(label);
            TextView day=text(new SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(new Date()),19,Color.WHITE); calendar.addView(day);
            TextView hint=text("Tap to view your calendar",12,0xcce6eaff);calendar.addView(hint); calendar.setContentDescription("Calendar widget"); calendar.setOnClickListener(v->showCalendar());
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(12);widgets.addView(calendar,cp);
        }
        if(prefs.getBoolean("battery",true)) {
            LinearLayout battery=row(); battery.setPadding(dp(20),dp(8),dp(20),dp(8)); battery.setBackground(glass(0x40ffffff,24));
            batteryLabel=text(batteryText(),14,Color.WHITE); battery.addView(batteryLabel,new LinearLayout.LayoutParams(0,dp(48),1));batteryLabel.setGravity(Gravity.CENTER_VERTICAL);
            battery.setContentDescription("Power widget");battery.setOnClickListener(v->openSettings(Settings.ACTION_SETTINGS)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(12);widgets.addView(battery,bp);
        }
        LinearLayout right=column(); content.addView(right,new LinearLayout.LayoutParams(0,-1,.63f));
        LinearLayout quickTitle=row();TextView label=text("FAVORITES",11,0xffe0e6ff); label.setLetterSpacing(.16f);quickTitle.addView(label,new LinearLayout.LayoutParams(0,dp(28),1));quickTitle.addView(button("All apps  ›",()->showApps()));right.addView(quickTitle);
        LinearLayout appRow=row(); appRow.setGravity(Gravity.CENTER);right.addView(appRow,new LinearLayout.LayoutParams(-1,0,1));
        for(int i=0;i<slots.length;i++) { final int slot=i; appRow.addView(shortcut(slot,false),new LinearLayout.LayoutParams(0,-1,1)); }
        TextView hint=text("Touch and hold a favorite to change its app",11,0xbbbfcdec);hint.setGravity(Gravity.CENTER);right.addView(hint,new LinearLayout.LayoutParams(-1,dp(24)));
    }
    View shortcut(int slot,boolean dock) {
        LinearLayout tile=column(); tile.setGravity(Gravity.CENTER);tile.setPadding(dp(4),dp(5),dp(4),dp(5));
        App app=assigned(slot); int size=dock?46:62;
        FrameLayout icon=new FrameLayout(this);
        int[] tops={0xff36b4ff,0xff48baff,0xffff4c6c};
        int[] bottoms={0xff0055ee,0xff0660ed,0xffe60027};
        GradientDrawable tileFill=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{tops[slot],bottoms[slot]});
        tileFill.setCornerRadius(dp(dock?15:19));tileFill.setStroke(dp(1),0x65ffffff);icon.setBackground(tileFill);icon.setElevation(dp(dock?2:5));
        if(slot==1){ImageView files=new ImageView(this);files.setImageResource(R.drawable.mrios_files);files.setScaleType(ImageView.ScaleType.FIT_CENTER);icon.setClipToOutline(true);icon.addView(files,new FrameLayout.LayoutParams(-1,-1));}
        else icon.addView(new FavoriteGlyph(this,slot),new FrameLayout.LayoutParams(-1,-1));
        tile.addView(icon,new LinearLayout.LayoutParams(dp(size),dp(size)));
        TextView label=text(slots[slot],dock?10:12,Color.WHITE); label.setMaxLines(1);label.setEllipsize(TextUtils.TruncateAt.END);label.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(22));lp.topMargin=dp(3);tile.addView(label,lp);
        tile.setContentDescription("Favorite "+slots[slot]);tile.setOnClickListener(v->{if(app==null)pick(slot);else launch(app);});tile.setOnLongClickListener(v->{pick(slot);return true;});return tile;
    }
    void buildDock() {
        LinearLayout outer=row();outer.setGravity(Gravity.CENTER);LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-1,dp(91));op.topMargin=dp(8);shell.addView(outer,op);
        LinearLayout dock=row();dock.setGravity(Gravity.CENTER);dock.setPadding(dp(12),dp(3),dp(12),dp(3));dock.setBackground(glass(0x75e5eaff,28));dock.setElevation(dp(8));outer.addView(dock,new LinearLayout.LayoutParams(-2,-1));
        for(int i=0;i<slots.length;i++) dock.addView(shortcut(i,true),new LinearLayout.LayoutParams(dp(72),-1));
        View divider=new View(this);divider.setBackgroundColor(0x40ffffff);LinearLayout.LayoutParams d=new LinearLayout.LayoutParams(dp(1),dp(42));d.setMargins(dp(9),0,dp(9),0);dock.addView(divider,d);
        dock.addView(dockAction("⚙","Settings",()->openSettings(Settings.ACTION_SETTINGS)),new LinearLayout.LayoutParams(dp(72),-1));
        dock.addView(dockAction("▦","Apps",()-> {if("apps".equals(page)){page="home";query="";hideKeyboard();render();}else showApps();}),new LinearLayout.LayoutParams(dp(72),-1));
    }
    View dockAction(String glyph,String label,Runnable action) {
        LinearLayout l=column();l.setGravity(Gravity.CENTER);View icon;
        if("Settings".equals(label)){ImageView image=new ImageView(this);image.setImageResource(R.drawable.mrios_settings);icon=image;}
        else {TextView mark=text(glyph,27,Color.WHITE);mark.setGravity(Gravity.CENTER);mark.setBackground(glass(0x70ffffff,15));icon=mark;}
        l.addView(icon,new LinearLayout.LayoutParams(dp(46),dp(46)));TextView name=text(label,10,Color.WHITE);name.setGravity(Gravity.CENTER);l.addView(name,new LinearLayout.LayoutParams(-1,dp(25)));l.setContentDescription(label);l.setOnClickListener(v->action.run());return l;
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
    void launch(App a) {try{startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(ComponentName.unflattenFromString(a.component)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));}catch(RuntimeException e){Toast.makeText(this,"App unavailable. Hold the favorite to choose another.",Toast.LENGTH_LONG).show();loadApps();}}
    void loadApps() {
        if(worker.isShutdown())return;
        worker.execute(()->{
            PackageManager pm=getPackageManager();ArrayList<App> found=new ArrayList<>();Set<String> seen=new HashSet<>();
            for(ResolveInfo info:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)) {
                if(Thread.currentThread().isInterrupted())return;if(info.activityInfo.packageName.equals(getPackageName()))continue;
                String key=new ComponentName(info.activityInfo.packageName,info.activityInfo.name).flattenToString();if(!seen.add(key))continue;
                try{Drawable d=info.loadIcon(pm);Bitmap bitmap=Bitmap.createBitmap(dp(48),dp(48),Bitmap.Config.ARGB_8888);d.setBounds(0,0,bitmap.getWidth(),bitmap.getHeight());d.draw(new Canvas(bitmap));found.add(new App(info.loadLabel(pm).toString(),key,bitmap));}catch(RuntimeException unavailable){/* Removed packages are skipped. */}
            }
            found.sort((a,b)->a.label.compareToIgnoreCase(b.label));
            main.post(()->{if(destroyed)return;apps.clear();apps.addAll(found);loaded=true;seedFavorites();if("apps".equals(page))filter();else render();});
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
    void seedFavorites() {
        String[][] hints={{"com.autochips.bluetooth"},{"com.android.documentsui","com.google.android.documentsui"},{"com.google.android.youtube","com.shazwan.nexadash.tizentube.next"}};
        android.content.SharedPreferences.Editor e=prefs.edit();for(int slot=0;slot<slots.length;slot++){if(prefs.contains("favorite_"+slots[slot]))continue;boolean done=false;for(String pkg:hints[slot]){for(App a:apps)if(ComponentName.unflattenFromString(a.component).getPackageName().equals(pkg)){e.putString("favorite_"+slots[slot],a.component);done=true;break;}if(done)break;}}e.apply();
    }
    void openSettings(String action) {try{startActivity(new Intent(action));}catch(RuntimeException unavailable){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(RuntimeException absent){Toast.makeText(this,"Settings unavailable on this device",Toast.LENGTH_SHORT).show();}}}
    void controlCenter() {
        LinearLayout panel=column();panel.setPadding(dp(20),dp(10),dp(20),dp(12));
        LinearLayout links=row();links.addView(button("Wi-Fi",()->openSettings(Settings.ACTION_WIFI_SETTINGS)),new LinearLayout.LayoutParams(0,dp(56),1));links.addView(button("Bluetooth",()->openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)),new LinearLayout.LayoutParams(0,dp(56),1));panel.addView(links);
        LinearLayout links2=row();links2.addView(button("Display",()->openSettings(Settings.ACTION_DISPLAY_SETTINGS)),new LinearLayout.LayoutParams(0,dp(56),1));links2.addView(button("Sound",()->openSettings(Settings.ACTION_SOUND_SETTINGS)),new LinearLayout.LayoutParams(0,dp(56),1));panel.addView(links2);
        TextView volume=text("Media volume",16,Color.WHITE);volume.setPadding(0,dp(18),0,0);panel.addView(volume);AudioManager audio=(AudioManager)getSystemService(AUDIO_SERVICE);SeekBar slider=new SeekBar(this);slider.setContentDescription("Media volume");slider.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));slider.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));slider.setMinimumHeight(dp(56));slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){if(user)try{audio.setStreamVolume(AudioManager.STREAM_MUSIC,n,0);}catch(SecurityException denied){Toast.makeText(LauncherActivity.this,"Use the head unit volume control",Toast.LENGTH_SHORT).show();}}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});panel.addView(slider);
        panel.addView(button("Choose default launcher",()->openSettings(Settings.ACTION_HOME_SETTINGS)));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Control Center").setView(panel).setPositiveButton("Done",null).create();dialog.show();if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawable(glass(0xf02b355d,28));dialog.getWindow().setLayout(dp(440),-2);}
    }
    void customize() {
        String[] options={"Calendar widget","Power widget","24-hour clock","Rose wallpaper"};boolean[] values={prefs.getBoolean("calendar",true),prefs.getBoolean("battery",true),prefs.getBoolean("24h",true),theme==1};
        new AlertDialog.Builder(this).setTitle("Make it yours").setMultiChoiceItems(options,values,(d,index,checked)->values[index]=checked).setPositiveButton("Apply",(d,w)->{prefs.edit().putBoolean("calendar",values[0]).putBoolean("battery",values[1]).putBoolean("24h",values[2]).putInt("theme",values[3]?1:0).apply();theme=values[3]?1:0;render();}).setNegativeButton("Cancel",null).show();
    }
    void showCalendar() {Calendar c=Calendar.getInstance();DatePickerDialog dialog=new DatePickerDialog(this,(v,y,m,d)->{},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));dialog.setTitle("Calendar");dialog.show();}
    String batteryText() {Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(i==null)return "Power · unavailable";int scale=i.getIntExtra(BatteryManager.EXTRA_SCALE,0),level=i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);return scale<=0||level<0?"Power · unavailable":"Power  "+(level*100/scale)+"%"+(i.getIntExtra(BatteryManager.EXTRA_PLUGGED,0)>0?"  ·  Connected":"");}
    void updateClock() {Date now=new Date();boolean h24=prefs.getBoolean("24h",true);if(clock!=null)clock.setText(new SimpleDateFormat(h24?"HH:mm":"h:mm a",Locale.getDefault()).format(now));if(date!=null)date.setText(new SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(now));if(statusTime!=null)statusTime.setText(new SimpleDateFormat(h24?"HH:mm  ·  EEE, d MMM":"h:mm a  ·  EEE, d MMM",Locale.getDefault()).format(now));}
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
