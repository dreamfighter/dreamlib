package com.dreamfighter.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class FileUtils {

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

	public static void deleteDirectory(File target){
        File dir = target;
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                File child = new File(dir, children[i]);
                deleteDirectory(child);
            }
        }else{
            dir.delete();
        }
    }

	// If targetLocation does not exist, it will be created.
	public static void copyDirectory(File sourceLocation , File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists() && !targetLocation.mkdirs()) {
				throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			// make sure the directory we plan to store the recording in exists
			File directory = targetLocation.getParentFile();
			if (directory != null && !directory.exists() && !directory.mkdirs()) {
				throw new IOException("Cannot create dir " + directory.getAbsolutePath());
			}

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
