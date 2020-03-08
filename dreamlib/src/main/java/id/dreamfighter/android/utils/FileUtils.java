package id.dreamfighter.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import androidx.core.content.FileProvider;

public class FileUtils {

	public static File mkdirs(Context context,String path){
		File dir = new File(path);

		if(!dir.exists()){
			FileProvider.getUriForFile(context,
					context.getPackageName() + ".provider",
					dir);
			dir.mkdirs();
			return dir;
		}

		return dir;
	}

	public static File file(Context context,String fileName){
		File file = new File(fileName);
		FileProvider.getUriForFile(context,
				context.getPackageName() + ".provider",
				file);

		return file;
	}

	public static void writeStringToFile(String str, String filePath) throws IOException{
		FileOutputStream fos = new FileOutputStream(
				new File(filePath));
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		osw.write(str);
		osw.flush();
		osw.close();
		fos.flush();
		fos.close();
	}
	
	public static String readFileToString(String filePath) throws IOException{
		FileInputStream fis = new FileInputStream(new File(filePath));
    	StringBuffer fileContent = new StringBuffer("");

    	byte[] buffer = new byte[1024];

    	while (fis.read(buffer) != -1) {
    	    fileContent.append(new String(buffer));
    	}
    	fis.close();
    	return new String(fileContent);
	}
	
	public static boolean isExist(String filePath){
	    File file = new File(filePath);
	    return file.exists();
	}
	
	public static void writeBitmapToFile(Bitmap bitmap, String filePath) throws IOException{
		File file = new File(filePath);
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.PNG, 100, fos);
		fos.flush();
		fos.close();
	}
	
	public static Bitmap getBitmap(String filePath){
		Bitmap bitmap = BitmapFactory.decodeFile(filePath);
		return bitmap;
	}
}
