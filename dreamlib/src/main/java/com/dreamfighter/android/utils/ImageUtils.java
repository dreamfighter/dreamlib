package com.dreamfighter.android.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import com.dreamfighter.android.log.Logger;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
            double ratio = 1.0 * b.getWidth()/b.getHeight();
            //int srcWidth = b.getWidth();
            //int srcHeight = b.getHeight();
            int destWidth = width;
            int destHeight = (int)(width / ratio); 
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
    
    
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Bitmap blurBitmap(Bitmap bitmap,Context context,float blur){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return bitmap;
        }
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

    public static String saveBitmap(Context context,String filename,Bitmap bmp){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(CommonUtils.getBaseDirectory(context)+filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return CommonUtils.getBaseDirectory(context)+filename;
    }

    public static Bitmap setImageBitmap(String mCurrentPhotoPath,final ImageView mImageView) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        final Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ((Activity)mImageView.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(bitmap);
            }
        });

        return bitmap;
    }

    public static Bitmap resampleBitmap(String mCurrentPhotoPath,final ImageView mImageView) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        final Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        return bitmap;
    }

    public static Bitmap resampleBitmap(String mCurrentPhotoPath,int targetW,int targetH) {
        // Get the dimensions of the View

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        final Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        return bitmap;
    }

    public static void takePicture(Activity context, String filename,int requestCode){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT > 19) {
            galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Uri imageUri = Uri.fromFile(new File(filename));
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose Image");

        Intent[] intentArray = {cameraIntent};
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        context.startActivityForResult(chooser, requestCode);
    }

    public static void takeFile(Activity context, String filename,int requestCode){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("*/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT > 19) {
            galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Uri imageUri = Uri.fromFile(new File(filename));
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose Image");

        Intent[] intentArray = {cameraIntent};
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        context.startActivityForResult(chooser, requestCode);
    }

    /**
     * calculate orientation to adjust picture orientation and aspect ratio
     * @param bitmap
     * @param photoPath
     */
    public static void calculateOrientation(Bitmap bitmap, String photoPath){
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:

                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * rotate image if the image in landscape mode while taking picture
     * @param source
     * @param angle
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bmp = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
        //ImageUtils.saveBitmap(this, filename, bmp);
        source.recycle();
        return bmp;
    }
}
