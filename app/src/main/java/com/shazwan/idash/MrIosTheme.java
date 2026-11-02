package com.shazwan.idash;

import android.content.res.AssetManager;
import android.graphics.*;
import java.io.*;
import java.util.*;

/** Portable visual resources from the owner-provided APK; no foreign executable code. */
final class MrIosTheme {
    static String icon(String pkg) {
        String name=null;
        if(pkg.contains("calculator"))name="calculator";
        else if(pkg.contains("calendar"))name="calendar_lg";
        else if(pkg.equals("com.android.deskclock")||pkg.equals("com.google.android.deskclock"))name="clock";
        else if(pkg.contains("contacts"))name="contacts";
        else if(pkg.equals("com.android.settings"))name="settings";
        else if(pkg.contains("documentsui")||pkg.equals("com.android.filemanager"))name="files";
        else if(pkg.contains("gallery")||pkg.equals("com.google.android.apps.photos"))name="photos";
        else if(pkg.equals("com.android.mms")||pkg.equals("com.google.android.apps.messaging"))name="messages";
        else if(pkg.equals("com.android.dialer")||pkg.equals("com.google.android.dialer"))name="phone";
        else if(pkg.equals("com.android.email")||pkg.equals("com.google.android.gm"))name="mail";
        else if(pkg.contains("camera"))name="camera";
        else if(pkg.equals("com.android.browser")||pkg.equals("com.android.chrome"))name="safari";
        else if(pkg.equals("com.google.android.apps.maps"))name="maps";
        else if(pkg.equals("com.acloud.stub.localmusic")||pkg.equals("com.android.music"))name="music";
        else if(pkg.equals("com.android.vending"))name="store";
        return name==null?null:"mrios/icons/ic_ios_"+name+".png";
    }
    static Bitmap decode(AssetManager assets,String path,int maxEdge) throws IOException {
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
        try(InputStream in=assets.open(path)){BitmapFactory.decodeStream(in,null,bounds);}
        if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IOException("Invalid image");
        BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;
        while(Math.max(bounds.outWidth,bounds.outHeight)/options.inSampleSize>maxEdge)options.inSampleSize*=2;
        try(InputStream in=assets.open(path)){Bitmap result=BitmapFactory.decodeStream(in,null,options);if(result==null)throw new IOException("Image decode failed");return result;}
    }
    static String[] wallpapers(AssetManager assets) throws IOException {
        String[] names=assets.list("mrios/wallpapers");if(names==null)return new String[0];
        Arrays.sort(names);return names;
    }
}
