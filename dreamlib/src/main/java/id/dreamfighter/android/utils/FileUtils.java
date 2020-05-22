package id.dreamfighter.android.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FileUtils {

	public static File mkdirs(Context context,String path){
		File dir = new File(path);
		File realDir = new File(CommonUtils.getRealDirectory(context));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
				boolean found = false;
				String uriStr = CommonUtils.getBaseUri(context);
				for(UriPermission p:permissions){

					if(uriStr!=null && p.getUri().toString().equals(uriStr)){
						found = true;
						break;
					}
				}
				if(!found && !realDir.exists()){
					FileProvider.getUriForFile(context,
							context.getPackageName() + ".provider",
							realDir);
					realDir.mkdirs();
					return dir;
				}
		}else if(!realDir.exists()){
			FileProvider.getUriForFile(context,
					context.getPackageName() + ".provider",
					realDir);
			realDir.mkdirs();
			return dir;
		}
		return dir;
	}

	public static File file(Context context,String fileName){
		File file = new File(fileName);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();

				Uri uri = null;
				for(UriPermission p:permissions){

					Log.d("Uri",p.getUri().getPath());
					Log.d("Uri expected",Uri.fromFile(file.getParentFile()).getPath());

					if(p.getUri().getPath().equals(Uri.fromFile(file.getParentFile()).getPath())){
						uri = p.getUri();
						break;
					}
				}
				if (uri!=null) {
					String l = getPath(context, uri);
					file = new File(l,file.getName());
				}else{
					FileProvider.getUriForFile(context,
							context.getPackageName() + ".provider", file);
				}
		}else{
			FileProvider.getUriForFile(context,
					context.getPackageName() + ".provider", file);
		}

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
				if(!child.isDirectory()) {
					deleteDirectory(child);
				}
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
				copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
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

	// If targetLocation does not exist, it will be created.
	public static void copyDirectory(Context context, File sourceLocation , File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				if(!new File(sourceLocation, children[i]).isDirectory()) {
					copyDirectory(context, new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
				}
			}
		} else {

			// make sure the directory we plan to store the recording in exists
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = null;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

				List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
				Uri uri = null;
				String uriStr = CommonUtils.getBaseUri(context);
				//if(uriStr!=null){
				//	uri = Uri.parse(uriStr);
				//}

				for(UriPermission p:permissions){
					Log.d("DATA","" + p.getUri());
					Log.d("DATA","" + uriStr);
					//Log.d("Uri",p.getUri().getPath());
					//Log.d("Uri expected",Uri.fromFile(targetLocation.getParentFile()).getPath());

					if(uriStr!=null && p.getUri().toString().equals(uriStr)){
						uri = p.getUri();
						break;
					}
				}

				if (uri!=null) {
					DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
					DocumentFile file = pickedDir.findFile(sourceLocation.getName());
					if(file==null) {
						file = pickedDir.createFile("*/*", sourceLocation.getName());
					}

					out = context.getContentResolver().openOutputStream( file.getUri(), "w");
				}

			}

			if(out==null){
				File directory = new File(CommonUtils.getRealDirectory(context),sourceLocation.getName());
				if (directory != null && !directory.exists() && !directory.mkdirs()) {
					throw new IOException("Cannot create dir " + directory.getAbsolutePath());
				}
				out = new FileOutputStream(targetLocation);
			}

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			if(out!=null) {
				out.close();
			}
		}
	}

	public static Observable<File> fileObservable(Context context, Response<ResponseBody> o,
												  String fileName){
		try {
			File f = new File(fileName);
			String dir = CommonUtils.getRealDirectory(context);
			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				byte[] fileReader = new byte[4096];

				//long fileSize = o.body().contentLength();
				//long fileSizeDownloaded = 0;

				inputStream = o.body().byteStream();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					try {
						String uriStr = CommonUtils.getBaseUri(context);
						List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();

						Uri uri = null;
						for(UriPermission p:permissions){
							//Log.d("Uri",p.getUri().getPath());
							//Log.d("Uri expected",Uri.fromFile(f.getParentFile()).getPath());

							if(uriStr!=null && uriStr.equals(p.getUri().toString())){
								uri = p.getUri();
								break;
							}
						}
						if (uri!=null) {
							DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
							DocumentFile file = pickedDir.findFile(f.getName());
							if(file==null) {
								file = pickedDir.createFile("*/*", f.getName());
							}

							outputStream = context.getContentResolver().openOutputStream( file.getUri(), "w");
						}


					}catch (IOException e){
						e.printStackTrace();
					}
				}

				if(outputStream==null){
					File realFile = new File(dir,f.getName());
					FileProvider.getUriForFile(context,
							context.getPackageName() + ".provider", realFile);
					outputStream = new FileOutputStream(realFile);
				}

				while (true) {
					int read = inputStream.read(fileReader);

					if (read == -1) {
						break;
					}

					outputStream.write(fileReader, 0, read);

					//fileSizeDownloaded += read;

					//Log.d("FileUtils", "file download: " + fileSizeDownloaded + " of " + fileSize);
				}

				outputStream.flush();

				return Observable.just(new File(dir,f.getName()));
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

	public static OutputStream createFile(Context context, String fileName){

		File f = new File(fileName);
		String dir = CommonUtils.getRealDirectory(context);
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				try {
					String uriStr = CommonUtils.getBaseUri(context);
					List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();

					Uri uri = null;
					for(UriPermission p:permissions){
						//Log.d("Uri",p.getUri().getPath());
						//Log.d("Uri expected",Uri.fromFile(f.getParentFile()).getPath());

						if(uriStr!=null && uriStr.equals(p.getUri().toString())){
							uri = p.getUri();
							break;
						}
					}
					if (uri!=null) {
						DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
						DocumentFile file = pickedDir.findFile(f.getName());
						if(file==null) {
							file = pickedDir.createFile("*/*", f.getName());
						}

						outputStream = context.getContentResolver().openOutputStream( file.getUri(), "w");
					}


				}catch (IOException e){
					e.printStackTrace();
				}
			}

			if(outputStream==null){
				File realFile = new File(dir,f.getName());
				FileProvider.getUriForFile(context,
						context.getPackageName() + ".provider", realFile);
				outputStream = new FileOutputStream(realFile);
			}
		} catch (IOException e) {
			return null;
		}
		//sink.flush();
		//sink.close();
		//return Observable.just(f);


		return outputStream;
	}

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		Log.d("isDocumentUri",""+DocumentsContract.isDocumentUri(context, uri));
		Log.d("isDocumentUri1",""+DocumentFile.isDocumentUri(context,uri));
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			String docId = DocumentsContract.getTreeDocumentId(uri);
			Log.d("DOCID",docId);
			Uri docUriTree = DocumentsContract.buildDocumentUriUsingTree(uri, docId);
			//Log.d("DOCID",docUriTree.toString());
			String r = getDataColumn(context, docUriTree, null, null);

			if(r==null) {
				String[] treeFile = docId.split(":");
				if(treeFile.length==1){
					return docId;
				}
				if ("primary".equalsIgnoreCase(treeFile[0])) {
					return Environment.getExternalStorageDirectory() + "/" + treeFile[1];
				}else {
					File[] externalDirs = ContextCompat.getExternalFilesDirs(context, treeFile[0]);
					for (File file : externalDirs) {
						Log.d("externalDirs", file.getAbsolutePath());
					}
					for (File file : externalDirs) {
						Log.d("externalDirs",file.getAbsolutePath());
						if (Environment.isExternalStorageRemovable(file)) {
							// Path is in format /storage.../Android....
							// Get everything before /Android
							return file.getPath().split("/Android")[0] + "/" + treeFile[1];

						}
					}
				}
			}else{
				return r;
			}
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {

			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}
