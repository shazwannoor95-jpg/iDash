package com.shazwan.idash;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.*;
import java.io.*;
import java.util.concurrent.*;

/** Disposable emulator measurements; never a physical ARM32 performance certification. */
public class DeviceReadinessChecks extends Instrumentation {
    LauncherActivity a;
    android.content.SharedPreferences preferences;java.util.Map<String,?> preferencesBefore;
    int count;
    final StringBuilder measurements = new StringBuilder();
    void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
        count++;
    }
    void ui(Runnable work) {
        Throwable[] error = {null};
        runOnMainSync(() -> { try { work.run(); } catch (Throwable t) { error[0] = t; } });
        waitForIdleSync();
        if (error[0] != null) throw new AssertionError(error[0]);
    }
    void measure(String stage) {
        Debug.MemoryInfo memory = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memory);
        Runtime runtime = Runtime.getRuntime();
        GlassBackdrop backdrop = IosStyle.backdrop(a);
        Bitmap frost = backdrop.frame.bitmap;
        int live = 0;
        for (java.lang.ref.WeakReference<GlassSurface> surface : backdrop.surfaces)
            if (surface.get() != null) live++;
        java.util.Set<Bitmap> known = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Bitmap, Boolean>());
        if (frost != null) known.add(frost);
        if (a.wallpaperBitmap != null) known.add(a.wallpaperBitmap);
        for (LauncherActivity.App app : a.apps) if (app.icon != null) known.add(app.icon);
        long knownBytes = 0;
        for (Bitmap bitmap : known) if (!bitmap.isRecycled()) knownBytes += bitmap.getAllocationByteCount();
        measurements.append(stage).append(" pss_kib=").append(memory.getTotalPss())
            .append(" java_used_bytes=").append(runtime.totalMemory() - runtime.freeMemory())
            .append(" native_allocated_bytes=").append(Debug.getNativeHeapAllocatedSize())
            .append(" frost_bytes=").append(frost == null ? 0 : frost.getAllocationByteCount())
            .append(" surface_slots=").append(backdrop.surfaces.size()).append(" surface_live=").append(live)
            .append(" known_bitmap_count=").append(known.size()).append(" known_bitmap_bytes=").append(knownBytes)
            .append(" quality=").append(IosStyle.quality(a)).append(" transparency=").append(GlassAppearance.read(a).transparency)
            .append(" wallpaper_bytes=").append(a.wallpaperBitmap == null ? 0 : a.wallpaperBitmap.getAllocationByteCount())
            .append('\n');
    }
    void screenshot(String name) throws Exception {
        Bitmap shot = getUiAutomation().takeScreenshot();
        check(shot != null, "real screenshot " + name);
        try (OutputStream out = new FileOutputStream(new File(a.getFilesDir(), name))) {
            shot.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally { shot.recycle(); }
    }
    void shellCapture(String command, String name) throws Exception {
        try (ParcelFileDescriptor fd = getUiAutomation().executeShellCommand(command);
             InputStream in = new FileInputStream(fd.getFileDescriptor());
             OutputStream out = new FileOutputStream(new File(a.getFilesDir(), name))) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        }
    }
    public void onCreate(Bundle args) { super.onCreate(args); start(); }
    public void onStart() {
        Bundle result = new Bundle();
        int code = Activity.RESULT_CANCELED;
        try {
            preferences=getTargetContext().getSharedPreferences("idash",0);preferencesBefore=new java.util.HashMap<>(preferences.getAll());
            a = (LauncherActivity) startActivitySync(new Intent(getTargetContext(), LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            ui(() -> {GlassAppearance.defaults().save(a);a.render();});
            CountDownLatch loaded = new CountDownLatch(1);
            a.worker.execute(() -> a.main.post(loaded::countDown));
            check(loaded.await(40, TimeUnit.SECONDS), "discovery/cache barrier");
            waitForIdleSync();
            Thread.sleep(1000);
            measure("dashboard"); screenshot("readiness-dashboard.png");
            shellCapture("dumpsys gfxinfo com.shazwan.idash reset", "readiness-gfx-reset.txt");
            Bitmap frost = IosStyle.backdrop(a).frame.bitmap;
            for (int i = 0; i < 20; i++) {
                ui(a::controlCenter);
                check(a.quickControls.observing, "open observes " + i);
                if (i == 0) { Thread.sleep(500); measure("control_open"); screenshot("readiness-control-center.png"); }
                ui(a.quickControls::close);
                QuickControls q = a.quickControls;
                check(!q.observing && !q.mediaControl.active && !q.mediaControl.listening
                    && !q.mediaControl.registered && !q.utilitiesControl.active
                    && !q.utilitiesControl.receiverRegistered, "close releases callbacks " + i);
                check(IosStyle.backdrop(a).frame.bitmap == frost, "shared frost survives open/close " + i);
                if ((i + 1) % 5 == 0) measure("closed_cycle_" + (i + 1));
            }
            measure("closed_after_20_cycles");
            Runtime.getRuntime().gc(); Thread.sleep(1000);
            measure("closed_after_gc_request");
            shellCapture("dumpsys gfxinfo com.shazwan.idash framestats", "readiness-gfxinfo.txt");
            shellCapture("dumpsys meminfo com.shazwan.idash", "readiness-meminfo.txt");
            ui(a::showApps); screenshot("readiness-app-drawer.png");
            ui(a::onBackPressed); check("home".equals(a.page), "drawer Back returns home");
            ui(() -> new DashSettings(a).open(""));
            Thread.sleep(1000); screenshot("readiness-settings-home.png");
            ui(() -> new DashSettings(a).open("Home & Dock"));
            Thread.sleep(1000); screenshot("readiness-settings-home-dock.png");
            ui(a::onBackPressed); ui(a::onBackPressed);
            getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME);
            Thread.sleep(500);
            shellCapture("am start -n com.shazwan.idash/.LauncherActivity", "readiness-home-return.txt");
            long deadline = SystemClock.elapsedRealtime() + 10000;
            while (!a.resumed && SystemClock.elapsedRealtime() < deadline) Thread.sleep(100);
            check(a.resumed, "return after system Home");
            try (Writer out = new FileWriter(new File(a.getFilesDir(), "readiness-memory.txt"))) {
                ActivityManager.MemoryInfo device = new ActivityManager.MemoryInfo();
                ((ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(device);
                out.write("API27 x86_64 TCG/SwiftShader emulator; total_ram_bytes=" + device.totalMem + "; instrumentation included.\n");
                out.write(measurements.toString());
            }
            result.putString("stream", "PASS: " + count + " device-readiness assertions\n" + measurements);
            code = Activity.RESULT_OK;
        } catch (Throwable t) { result.putString("stream", "FAIL after " + count + ": " + t + "\n" + measurements); }
        finally { if(preferencesBefore!=null)AppearanceChecks.restore(preferences,preferencesBefore);if (a != null) ui(() -> { if (a.quickControls != null) a.quickControls.close(); a.finish(); }); }
        finish(code, result);
    }
}
