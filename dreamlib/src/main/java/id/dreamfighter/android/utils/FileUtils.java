package id.dreamfighter.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.content.FileProvider;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Response;

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

	public static Observable<File> fileObservable(Context context, Response<ResponseBody> o,String fileName){
		try {
			File f = FileUtils.file(context, fileName);
			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				byte[] fileReader = new byte[4096];

				long fileSize = o.body().contentLength();
				long fileSizeDownloaded = 0;

				inputStream = o.body().byteStream();
				outputStream = new FileOutputStream(f);

				while (true) {
					int read = inputStream.read(fileReader);

					if (read == -1) {
						break;
					}

					outputStream.write(fileReader, 0, read);

					fileSizeDownloaded += read;

					//Log.d("FileUtils", "file download: " + fileSizeDownloaded + " of " + fileSize);
				}

				outputStream.flush();

				return Observable.just(f);
			} catch (IOException e) {
				return Observable.error(e);
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}

				if (outputStream != null) {
					outputStream.close();
				}
			}
			//sink.flush();
			//sink.close();
			//return Observable.just(f);
		} catch (IOException e) {
			return Observable.error(e);
		}
	}
}
