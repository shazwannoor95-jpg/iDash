package com.shazwan.idash.a1lab;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.nio.file.*;

/** User-selected image stays private to this installation; never uploaded. */
final class CustomWallpaper {
 static final String PATH="custom",NAME="wallpaper-image";
 static final int MAX_BYTES=16*1024*1024;
 static File file(Context context){return new File(context.getFilesDir(),NAME);}
 static Bitmap decode(File image,int maxEdge)throws IOException{
  BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeFile(image.getPath(),bounds);
  if(bounds.outWidth<=0||bounds.outHeight<=0||maxEdge<1)throw new IOException("Not a supported image");
  BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;
  while(Math.max(bounds.outWidth,bounds.outHeight)/options.inSampleSize>maxEdge)options.inSampleSize*=2;
  Bitmap bitmap=BitmapFactory.decodeFile(image.getPath(),options);if(bitmap==null)throw new IOException("Image unavailable");return bitmap;
 }
 static void save(Context context,InputStream input)throws IOException{
  File temporary=File.createTempFile("wallpaper-",".tmp",context.getFilesDir());
  try{
   try(OutputStream output=new FileOutputStream(temporary)){
    byte[] buffer=new byte[16384];int count,total=0;
    while((count=input.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new IOException("Cancelled");total+=count;if(total>MAX_BYTES)throw new IOException("Choose an image smaller than16MiB");output.write(buffer,0,count);}
   }
   Bitmap validated=decode(temporary,1280);validated.recycle();
   Files.move(temporary.toPath(),file(context).toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
  }finally{temporary.delete();}
 }
}
