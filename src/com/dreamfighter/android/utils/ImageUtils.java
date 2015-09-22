package com.dreamfighter.android.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.dreamfighter.android.log.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

public class ImageUtils {
    
    public static String getImageTempPath(Context context, String url){
        String[] imgUrl = url.split("/");
        String filename = imgUrl[imgUrl.length-1]; 
        String packageName = context.getPackageName();
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/data/" + packageName + "/cache/");
        if(!dir.exists()){
            dir.mkdirs();
        }
        //Log.d("Thumb","tempPath=>" + dir.getAbsolutePath() + "/" + filename);
        return dir.getAbsolutePath() + "/" + filename;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, String resUrl,
                                                         int reqWidth, int reqHeight) {

        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //BitmapFactory.decodeResource(res, resId, options);
            BitmapFactory.decodeStream(new URL(resUrl).openStream());

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(new URL(resUrl).openStream());

        }catch (IOException ioe){
            //Url not available
            //ioe.printStackTrace();
            Log.w("CardThumbnailView","Error while retrieving image",ioe);
        }
        return null;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqWidth == 0 || reqHeight == 0) return inSampleSize;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
    
    public static Bitmap resizeBitmap(Bitmap b,int width){
        if(b!=null){
            int srcWidth = b.getWidth();
            int srcHeight = b.getHeight();
            int destWidth = width;
            int destHeight = (int)1.0 * (destWidth / srcWidth) * srcHeight; 
            return Bitmap.createScaledBitmap(b, destWidth, destHeight, true);
        }
        return b;
    }
    
    public static Bitmap blurBitmap(Context context,Bitmap bitmap){
        return blurBitmap(bitmap,context,25.f);
    }

    
    public static Bitmap blurBitmap(Context context,Bitmap bitmap,float blur){
        return blurBitmap(bitmap,context,blur);
    }
    
    public static Bitmap blurBitmap(Bitmap bitmap,Context context){
        return blurBitmap(bitmap,context,25.f);
    }
    
    @SuppressLint("NewApi")
    public static Bitmap blurBitmap(Bitmap bitmap,Context context,float blur){
        if(bitmap==null){
            return bitmap;
        }
        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        
        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(context);
        
        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        
        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        
        //Set the radius of the blur
        blurScript.setRadius(blur);
        
        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        
        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);
        
        //recycle the original bitmap
        //bitmap.recycle();
        
        //After finishing everything, we destroy the Renderscript.
        rs.destroy();
        
        return outBitmap;
    }
    
    public static Bitmap blurBitmap(Bitmap bitmap,int x,int y,int width,int height, Context context){
        if(bitmap!=null && bitmap.getHeight() > height){
            
            Bitmap back = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(back);
            Logger.log("bitmap.getHeight()=>"+bitmap.getHeight());
            Logger.log("height=>"+height);
            
            Bitmap newBmp1 = Bitmap.createBitmap(bitmap, 0, 0, width, bitmap.getHeight() - height);
            Bitmap newBmp = blurBitmap(Bitmap.createBitmap(bitmap, x, y, width, height),context);
            canvas.drawBitmap(newBmp1,0,0,null);
            canvas.drawBitmap(newBmp,0,newBmp1.getHeight(),null);
            //bitmap.recycle();
            return back;
        }
        return bitmap;
    }

}
