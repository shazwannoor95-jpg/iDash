package com.shazwan.idash;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/** Official zone data; date parsing adapted from NexaDash JakimSchedule (MIT). */
final class JakimCalendar {
    static final TimeZone MY=TimeZone.getTimeZone("Asia/Kuala_Lumpur");
    static final String[] ZONES={"SGR01","SGR02","SGR03","WLY01"};
    static final String[] LABELS={"SGR01 · Hulu Langat / Gombak / Sepang / Hulu Selangor / Rawang / Petaling","SGR02 · Klang / Kuala Selangor","SGR03 · Kuala Langat / Sabak Bernam","WLY01 · Kuala Lumpur / Putrajaya"};
    static final String[] KEYS={"fajr","syuruk","dhuhr","asr","maghrib","isha"};
    static final String[] NAMES={"Subuh","Syuruk","Zohor","Asar","Maghrib","Isyak"};
    static final String[] MS={"Jan","Feb","Mac","Apr","Mei","Jun","Jul","Ogos","Sep","Okt","Nov","Dis"};
    static final String[] EN={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    static final int LIMIT=2*1024*1024;
    static boolean validZone(String z){return z!=null&&z.matches("(JHR|KDH|KTN|MLK|NGS|PHG|PLS|PNG|PRK|SBH|SGR|SWK|TRG|WLY)\\d{2}");}
    static String key(long millis){SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US);f.setTimeZone(MY);return f.format(new Date(millis));}
    static String date(String raw)throws Exception{
        String[] bits=raw.split("-");if(bits.length!=3)throw new IOException("Invalid date");
        for(int i=0;i<12;i++)if(bits[1].equalsIgnoreCase(MS[i])){bits[1]=EN[i];break;}
        String normalized=bits[0]+"-"+bits[1]+"-"+bits[2];SimpleDateFormat f=new SimpleDateFormat("dd-MMM-yyyy",Locale.US);f.setTimeZone(MY);f.setLenient(false);Date d=f.parse(normalized);
        if(d==null||!f.format(d).equalsIgnoreCase(normalized))throw new IOException("Invalid date");return key(d.getTime());
    }
    static final class Day {
        final String date,hijri;final String[] times=new String[6];
        Day(JSONObject o)throws Exception{date=JakimCalendar.date(o.getString("date"));hijri=o.getString("hijri");if(!hijri.matches("\\d{4}-\\d{2}-\\d{2}"))throw new IOException("Invalid Hijri date");
            int last=-1;for(int i=0;i<6;i++){String v=o.getString(KEYS[i]);if(!v.matches("\\d{2}:\\d{2}(:\\d{2})?"))throw new IOException("Invalid time");int h=Integer.parseInt(v.substring(0,2)),m=Integer.parseInt(v.substring(3,5));if(h>23||m>59||(v.length()==8&&Integer.parseInt(v.substring(6))>59)||h*60+m<=last)throw new IOException("Invalid timetable");last=h*60+m;times[i]=v.substring(0,5);}}
    }
    static String nextPrayer(Map<String,Day> days,long now){
        String[] names={"Subuh","Syuruk","Zohor","Asar","Maghrib","Isyak"};
        Calendar candidate=Calendar.getInstance(MY);candidate.setTimeInMillis(now);
        for(int offset=0;offset<2;offset++){
            Calendar date=(Calendar)candidate.clone();date.add(Calendar.DAY_OF_MONTH,offset);Day day=days.get(key(date.getTimeInMillis()));if(day==null)continue;
            for(int index:new int[]{0,2,3,4,5}){
                Calendar at=(Calendar)date.clone();at.set(Calendar.HOUR_OF_DAY,Integer.parseInt(day.times[index].substring(0,2)));at.set(Calendar.MINUTE,Integer.parseInt(day.times[index].substring(3,5)));at.set(Calendar.SECOND,0);at.set(Calendar.MILLISECOND,0);
                long remaining=at.getTimeInMillis()-now;if(remaining<0)continue;long minutes=(remaining+59999)/60000;
                return names[index]+" · "+day.times[index]+" · "+(minutes>=60?minutes/60+"j ":"")+minutes%60+"m lagi";
            }
        }
        return "Tiada waktu seterusnya dalam jadual";
    }
    static Map<String,Day> parse(String raw)throws Exception{JSONObject root=new JSONObject(raw);JSONArray rows=root.getJSONArray("prayerTime");if(rows.length()==0||rows.length()>800)throw new IOException("Invalid coverage");Map<String,Day> result=new TreeMap<>();for(int i=0;i<rows.length();i++){Day d=new Day(rows.getJSONObject(i));if(result.put(d.date,d)!=null)throw new IOException("Duplicate date");}return result;}
    static String read(InputStream in)throws IOException{try(InputStream source=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] bytes=new byte[8192];int n;while((n=source.read(bytes))!=-1){if(out.size()+n>LIMIT)throw new IOException("Timetable too large");out.write(bytes,0,n);}return out.toString("UTF-8");}}
    static Map<String,Day> load(Context c,String zone)throws Exception{
        if(!validZone(zone))return Collections.emptyMap();Map<String,Day> result=new TreeMap<>();
        try{result.putAll(parse(read(c.getAssets().open("jakim/"+zone+".json"))));}catch(FileNotFoundException absent){}
        File cache=new File(c.getFilesDir(),"jakim-"+zone+".json");if(cache.isFile())try{result.putAll(parse(read(new FileInputStream(cache))));}catch(Exception invalid){/* Preserve valid bundled data. */}
        return result;
    }
    static void refresh(Context c,String zone)throws Exception{
        if(!validZone(zone))throw new IOException("Invalid zone");
        URL url=new URL("https://www.e-solat.gov.my/index.php?r=esolatApi/takwimsolat&period=year&zone="+zone);HttpURLConnection conn=(HttpURLConnection)url.openConnection();conn.setConnectTimeout(8000);conn.setReadTimeout(8000);conn.setInstanceFollowRedirects(false);
        try{if(conn.getResponseCode()!=200)throw new IOException("JAKIM HTTP "+conn.getResponseCode());String raw=read(conn.getInputStream());JSONObject root=new JSONObject(raw);if(!zone.equals(root.optString("zone"))||!root.optString("status").startsWith("OK"))throw new IOException("Zone/status mismatch");parse(raw);
            File dest=new File(c.getFilesDir(),"jakim-"+zone+".json");android.util.AtomicFile atomic=new android.util.AtomicFile(dest);FileOutputStream out=null;try{out=atomic.startWrite();out.write(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));atomic.finishWrite(out);}catch(IOException failed){if(out!=null)atomic.failWrite(out);throw failed;}
        }finally{conn.disconnect();}
    }
}
