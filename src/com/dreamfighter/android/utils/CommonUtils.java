package com.dreamfighter.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class CommonUtils {
    private static Map<String, Typeface> font = new HashMap<String, Typeface>();
    
    public static Typeface getFont(Context context,String path){
        if(font.get(path)==null){
            Typeface typeFace = Typeface.createFromAsset(context.getAssets(), path);
            font.put(path, typeFace);
        }
        return font.get(path);
    }
    
    public static String extractFilenameFromImgUrl(String url){
        String[] names = url.split("/");
        if(names.length>1){
            return names[names.length-1];
        }
        return url; 
    }
    
    /**
     * capitalize firs character in the word
     * @param str
     * @return
     */
    public static String capitalizeFirstChar(String str){
        if(str!=null & !"".equals(str)){
            str = str.toLowerCase();
            final StringBuilder result = new StringBuilder(str.length());
            String[] words = str.split("\\s");
            for(int i=0,l=words.length;i<l;++i) {
              if(i>0) result.append(" ");      
              result.append(Character.toUpperCase(words[i].charAt(0)))
                    .append(words[i].substring(1));

            }
            return result.toString();
        }
        return str;
    }
    

    /**
     * get base application directory
     * @param context
     * @return url directory
     */
    public static String getBaseDirectory(Context context){
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)){
            return Environment.getExternalStorageDirectory() + "/Android/data/"+context.getPackageName()+"/cache/";
        }
        else return null;
    }
    
    /**
     * retrieve application version code
     * 
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    /**
     * get json as a string from the assets by its path url
     * @param context
     * @param path
     * @return
     */
    public static String getJsonFromAssets(Context context,String path){
        try {
            InputStream is = context.getAssets().open(path);
            
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "{}";
    }
    

    /**
     * get IMEI or android ID from device
     * @param context
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] getIMEI(Context context) throws UnsupportedEncodingException{
        String identifier = null;
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null){
              identifier = tm.getDeviceId();
        }
        if (identifier == null || identifier.length() == 0){
              identifier = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
        }
        return identifier.getBytes("UTF-8");
    }
}
