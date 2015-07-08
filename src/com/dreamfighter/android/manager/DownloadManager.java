package com.dreamfighter.android.manager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.dreamfighter.android.R;
import com.dreamfighter.android.entity.ProxyConfiguration;
import com.dreamfighter.android.enums.DownloadInfo;
import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.utils.HttpUtils;

public class DownloadManager extends AsyncTask<String, Integer, Boolean>{
	public static final String TYPE_STRING 	= "string";
	public static final String TYPE_BITMAP 	= "bitmap";
	public static final String TYPE_RAW 	= "raw";
	public static final String POST_TYPE_RAW= "raw";
	public static final String POST_TYPE_FORM= "form";
	public static final String CONTENT_TYPE_JSON= "application/json";
	public static final String CONTENT_TYPE_HTML= "text/html";
	public static final String CONTENT_TYPE_FORM= "application/x-www-form-urlencoded";
	private String urlString;
	private String rawStringPost;
	private static Handler handler;
	private boolean post = false;
	private boolean finish = false;
	private boolean success = false;
	private Runnable runnable;
	private Runnable runableProgress;
	private CookieStore cookieStore;
	private List<NameValuePair> postParams = new ArrayList<NameValuePair>();
	private Boolean connection = true;
	private Bitmap bitmap = null;
	private String type = TYPE_STRING;
	private String postType = POST_TYPE_FORM;
	private String contentType = CONTENT_TYPE_HTML;
	private String resultString = null;
	private String filename = null;
	private Long filesize = 0l;
	private Long currentDownload = 0l;
	private DownloadInfo downloadInfo;
	private boolean upload;
	private InputStream inputStreamUpload;
	private File fileUpload;
	private DownloadListeners downloadListeners;
	private Context context;
	private String fileUploadName = "FILE";
	private HttpUtils httpUtils;
	private Map<String,String> listHeader = new HashMap<String, String>();
	private boolean secure = false;
	private Header[] headers;
	private static Dialog dialogProxyAuth = null;
	private Long intervalUpdateProgress = 0l;

	@Deprecated
	public DownloadManager(){
		handler = new Handler();
	}
	public DownloadManager(Context context){
		handler = new Handler();
		this.context = context;
		Long c1 = System.currentTimeMillis();
		checkProxyConfiguration();
		Long c2 = System.currentTimeMillis();
		Logger.log("time geting proxy configuration=>"+(1.0 * (c2 - c1) / 1000));
	}
	
	public DownloadManager(Context context,Runnable runnable){
		handler = new Handler();
		this.runnable = runnable;
		this.context = context;
	}
	
	public DownloadManager(Runnable runnable){
		handler = new Handler();
		this.runnable = runnable;
	}
	
	public DownloadManager(Context context, String url,Runnable runnable){
		this.urlString= url;
		handler = new Handler();
		this.runnable = runnable;
		this.context = context;
	}
	
	public DownloadManager(String url, Runnable runnable,boolean useProxy){
		this.urlString= url;
		handler = new Handler();
		this.runnable = runnable;
	}
	
	public void download(){
		if(!getStatus().equals(Status.RUNNING) && !getStatus().equals(Status.FINISHED)){
			execute();
		}
	}
	
	public void download(String url){
		handler = new Handler();
		this.urlString = url;
		if(!getStatus().equals(Status.RUNNING) && !getStatus().equals(Status.FINISHED)){
			execute();
		}
	}
	
	public void request(){
		if(!getStatus().equals(Status.RUNNING) && !getStatus().equals(Status.FINISHED)){
			execute();
		}
	}
	
	public void request(String url){
		handler = new Handler();
		this.urlString = url;
		if(!getStatus().equals(Status.RUNNING) && !getStatus().equals(Status.FINISHED)){
			execute();
		}
	}
	
	/**
	 * upload file to server 
	 * @param url for upload
	 */
	public void upload(String url){
		this.upload = true;
		this.urlString = url;
		if(!getStatus().equals(Status.RUNNING) && !getStatus().equals(Status.FINISHED)){
			execute();
		}
	}
	
	public boolean isRunning(){
		return getStatus().equals(Status.RUNNING);
	}
	
	public void checkProxyConfiguration(){
        boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;

        if( IS_ICS_OR_LATER ){
            proxyAddress = System.getProperty( "http.proxyHost" );

            String portStr = System.getProperty( "http.proxyPort" );
            proxyPort = Integer.parseInt( ( portStr != null ? portStr : "-1" ) );
        }else{
            proxyAddress = android.net.Proxy.getHost( getContext() );
            proxyPort = android.net.Proxy.getPort( getContext() );
        }
        
        if(!"".equals(proxyAddress) && proxyAddress!=null){
        	HttpUtils.instance.setUseProxy(true);
        	HttpUtils.instance.setHostname(proxyAddress);
        	HttpUtils.instance.setPort(proxyPort);
        	ProxyConfigurationManager proxyManager = new ProxyConfigurationManager(getContext());
        	ProxyConfiguration proxyConfiguration = proxyManager.getProxyConfigurationByAddressAndPort(proxyAddress, proxyPort);
        	//Logger.log("proxyConfiguration=>"+proxyConfiguration);
        	if(proxyConfiguration==null){
        		if(dialogProxyAuth==null){
        			dialogProxyAuth = createDialog(proxyAddress,proxyPort);
        			dialogProxyAuth.show();
        		}else{
        			dialogProxyAuth.show();
        		}
        	}else if(proxyConfiguration.getUseProxyAuth()){
            	HttpUtils.instance.setUseAuthProxy(proxyConfiguration.getUseProxyAuth());
            	HttpUtils.instance.setUsername(proxyConfiguration.getProxyUserName());
            	HttpUtils.instance.setPassword(proxyConfiguration.getProxyPassword());
        	}else{
        		HttpUtils.instance.setUseAuthProxy(proxyConfiguration.getUseProxyAuth());
        	}
        }else{
        	HttpUtils.instance.setUseProxy(false);
        }
        //Logger.log("proxy address=>"+proxyAddress);
        //Logger.log("proxy port=>"+proxyPort);
	}
	
	public Dialog createDialog(final String proxyAddress,final int proxyPort) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
	    // Get the layout inflater
	    LayoutInflater inflater = LayoutInflater.from(getContext());
	    
	    View view = inflater.inflate(R.layout.df_proxy_auth_layout, null);
	    final EditText username = (EditText)view.findViewById(R.id.df_username);
	    final EditText password = (EditText)view.findViewById(R.id.df_password);
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(view)
	    // Add action buttons
	           .setPositiveButton(R.string.df_signin, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   ProxyConfigurationManager proxyManager = new ProxyConfigurationManager(getContext());
	                   
	                   ProxyConfiguration proxyConf = proxyManager.getProxyConfigurationByAddressAndPort(proxyAddress, proxyPort);
	                   if(proxyConf==null){
	                	   proxyConf = new ProxyConfiguration();
		                   proxyConf.setProxyAddress(proxyAddress);
		                   proxyConf.setProxyPort(proxyPort);
		                   if(!username.getText().toString().equals("") && !username.getText().toString().equals(null)){
		                	   proxyConf.setUseProxyAuth(true);
		                	   proxyConf.setProxyUserName(username.getText().toString());
		                	   proxyConf.setProxyPassword(password.getText().toString());
		                   }else{
		                	   proxyConf.setUseProxyAuth(false);
		                	   proxyConf.setProxyUserName("");
		                	   proxyConf.setProxyPassword("");
		                   }
	                	   proxyManager.saveEntity(proxyConf);
	                   }else{
	                	   if(!username.getText().toString().equals("") && !username.getText().toString().equals(null)){
		                	   proxyConf.setUseProxyAuth(true);
		                	   proxyConf.setProxyUserName(username.getText().toString());
		                	   proxyConf.setProxyPassword(password.getText().toString());
		                   }else{
		                	   proxyConf.setUseProxyAuth(false);
		                	   proxyConf.setProxyUserName("");
		                	   proxyConf.setProxyPassword("");
		                   }
	                	   proxyManager.updateEntity(proxyConf);
	                   }
	                   dialog.dismiss();
	               }
	           })
	           .setNegativeButton(R.string.df_cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   
	               }
	           });
	    return builder.create();
	}
	
	
	/**
	 * request using GET 
	 * @return InputStream requestResponse
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public InputStream getContent() throws IllegalStateException, IOException, NullPointerException{
		
		Logger.log(this, "DOWNLOAD CONTENT USING GET URL => " + getUrlString());
		
		if(getUrlString() == null){ 
			return null;
		}
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		if(isSecure()){
			/*SchemeRegistry schemeRegistry = new SchemeRegistry();
			SchemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			HttpParams params = new BasicHttpParams();

			SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);
			httpClient = new DefaultHttpClient(mgr, params);*/
		}
		
		if(HttpUtils.instance.getCookieStore(context)!=null){
			httpClient.setCookieStore(HttpUtils.instance.getCookieStore(context));
		}/*else{
			HttpUtils.instance.setCookieStore(context,httpClient.getCookieStore());
		}*/
		
		HttpGet httpGet = new HttpGet(getUrlString());
		if(listHeader!=null){
			for(String key:listHeader.keySet()){
				httpGet.addHeader(key, listHeader.get(key));
			}
		}

		//Logger.log("using proxy=>"+HttpUtils.instance.isUseProxy());
		//Logger.log("using auth proxy=>"+HttpUtils.instance.isUseAuthProxy());
		//Logger.log("proxy address=>"+HttpUtils.instance.getHostname());
		//Logger.log("proxy port=>"+HttpUtils.instance.getPort());
		//Logger.log("proxy username=>"+HttpUtils.instance.getUsername());
		//Logger.log("proxy password=>"+HttpUtils.instance.getPassword());
		
		if(HttpUtils.instance.isUseProxy()){
				HttpHost proxy = new HttpHost(HttpUtils.instance.getHostname(), HttpUtils.instance.getPort());
				if(HttpUtils.instance.isUseAuthProxy()){
					httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(HttpUtils.instance.getUsername(), HttpUtils.instance.getPassword()));
				}
			    ConnRouteParams.setDefaultProxy(httpClient.getParams(), proxy);
	    }
			
		
		
		HttpResponse httpResponse = httpClient.execute(httpGet);
		headers = httpResponse.getAllHeaders();
		filesize = httpResponse.getEntity().getContentLength();
		return httpResponse.getEntity().getContent();
	}
	
	/**
	 * request url using post
	 * @return InputStream requestResponse
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public InputStream post() throws ClientProtocolException, IOException{
		Logger.log(this, "DOWNLOAD CONTENT USING POST URL => " + getUrlString());

		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		if(HttpUtils.instance.getCookieStore(context)!=null){
			httpClient.setCookieStore(HttpUtils.instance.getCookieStore(context));
		}/*else{
			HttpUtils.instance.setCookieStore(context,httpClient.getCookieStore());
		}*/

		// In a POST request, we don't pass the values in the URL.
		//Therefore we use only the web page URL as the parameter of the HttpPost argument
		HttpPost httpPost = new HttpPost(getUrlString());
		
		if(listHeader!=null){
			for(String key:listHeader.keySet()){
				httpPost.addHeader(key, listHeader.get(key));
			}
		}

		//Logger.log("using proxy=>"+HttpUtils.instance.isUseProxy());
		if(HttpUtils.instance.isUseProxy()){
			HttpHost proxy = new HttpHost(HttpUtils.instance.getHostname(), HttpUtils.instance.getPort());
			if(HttpUtils.instance.isUseAuthProxy()){
				httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(HttpUtils.instance.getUsername(), HttpUtils.instance.getPassword()));
			}
		    ConnRouteParams.setDefaultProxy(httpClient.getParams(), proxy);
        }


		// UrlEncodedFormEntity is an entity composed of a list of url-encoded pairs. 
		//This is typically useful while sending an HTTP POST request.
		if(postType.equals(POST_TYPE_FORM)){
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(postParams);
			httpPost.setEntity(urlEncodedFormEntity);
			urlEncodedFormEntity.setContentType("application/x-www-form-urlencoded");
		}else if(postType.equals(POST_TYPE_RAW)){
			StringEntity se = new StringEntity(rawStringPost);  
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, contentType));
            httpPost.setEntity(se);
		}
		// setEntity() hands the entity (here it is urlEncodedFormEntity) to the request.

		// HttpResponse is an interface just like HttpPost.
		//Therefore we can't initialize them
		HttpResponse httpResponse = httpClient.execute(httpPost);
		headers = httpResponse.getAllHeaders();
		// According to the JAVA API, InputStream constructor do nothing. 
		//So we can't initialize InputStream although it is not an interface
		filesize = httpResponse.getEntity().getContentLength();
		
		return httpResponse.getEntity().getContent();

	}
	
	/**
	 * do upload file usign post
	 * @param fileUpload 
	 * @return InputStream requestResponse
	 * @throws IOException
	 */
	protected InputStream doFileUpload(File fileUpload) throws IOException{
		Logger.log(this, "UPLOAD CONTENT USING POST URL => " + getUrlString());

		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		if(HttpUtils.instance.getCookieStore(context)!=null){
			httpClient.setCookieStore(HttpUtils.instance.getCookieStore(context));
		}else{
			HttpUtils.instance.setCookieStore(context,httpClient.getCookieStore());
		}

		// In a POST request, we don't pass the values in the URL.
		//Therefore we use only the web page URL as the parameter of the HttpPost argument
		HttpPost httpPost = new HttpPost(getUrlString());
		
		if(HttpUtils.instance.isUseProxy()){
			HttpHost proxy = new HttpHost(HttpUtils.instance.getHostname(), HttpUtils.instance.getPort());
			if(HttpUtils.instance.isUseAuthProxy()){
				httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(HttpUtils.instance.getUsername(), HttpUtils.instance.getPassword()));
			}
		    ConnRouteParams.setDefaultProxy(httpClient.getParams(), proxy);
        }
		
		if(fileUpload!=null){
			FileBody bin1 = new FileBody(fileUpload);
			MultipartEntity reqEntity = new MultipartEntity();
	        reqEntity.addPart(fileUploadName, bin1);
	        reqEntity.addPart("user", new StringBody("User"));
	        for (NameValuePair valuePair : postParams) {
	        	reqEntity.addPart(valuePair.getName(), new StringBody(valuePair.getValue()));
			}
	        
	        httpPost.setEntity(reqEntity);
		}

		// UrlEncodedFormEntity is an entity composed of a list of url-encoded pairs. 
		//This is typically useful while sending an HTTP POST request. 
		//UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(postParams);
		// setEntity() hands the entity (here it is urlEncodedFormEntity) to the request.
		//httpPost.setEntity(urlEncodedFormEntity);

		// HttpResponse is an interface just like HttpPost.
		//Therefore we can't initialize them
		HttpResponse httpResponse = httpClient.execute(httpPost);
		// According to the JAVA API, InputStream constructor do nothing. 
		//So we can't initialize InputStream although it is not an interface
		filesize = httpResponse.getEntity().getContentLength();
		
		return httpResponse.getEntity().getContent();
	}
	
	@Deprecated
	protected void doFileUpload(InputStream is) throws IOException{
	    HttpURLConnection conn = null;
	    DataOutputStream dos = null;

	    String lineEnd = "\r\n";
	    String twoHyphens = "--";
	    String boundary =  "*****";
	    int bytesRead, bytesAvailable, bufferSize;
	    byte[] buffer;
	    int maxBufferSize = 1*1024*1024;
	    String urlString = getUrlString();
	    
	        Logger.log("MediaPlayer","Inside second Method");
	        URL url = new URL(urlString);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setDoInput(true);
	        // Allow Outputs
	        conn.setDoOutput(true);
	        // Don't use a cached copy.
	        conn.setUseCaches(false);
	        // Use a post method.
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	        dos = new DataOutputStream( conn.getOutputStream() );
	        dos.writeBytes(twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + filename +"\"" + lineEnd);
	        dos.writeBytes(lineEnd);
	        Logger.log("MediaPlayer","Headers are written");
	        bytesAvailable = is.available();
	        bufferSize = Math.min(bytesAvailable, maxBufferSize);
	        buffer = new byte[bufferSize];
	        bytesRead = is.read(buffer, 0, bufferSize);
	        
	        while (bytesRead > 0){
	            dos.write(buffer, 0, bufferSize);
	            bytesAvailable = is.available();
	            bufferSize = Math.min(bytesAvailable, maxBufferSize);
	            bytesRead = is.read(buffer, 0, bufferSize);
	        }
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        is.close();
	        
	        StringBuilder sb = new StringBuilder();
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	            sb.append(inputLine);
	        }
	        resultString = sb.toString();
	        // close streams
	        dos.flush();
	        dos.close();
	   

	    //------------------ read the SERVER RESPONSE
	   /* try {
	        inStream = new DataInputStream ( conn.getInputStream() );
	        String str;            
	        while (( str = inStream.readLine()) != null)
	        {
	        	Logger.log("MediaPlayer","Server Response"+str);
	        }
	        while((str = inStream.readLine()) !=null ){

	        }
	        inStream.close();
	    }
	    catch (IOException ioex){
	    	Logger.log("MediaPlayer", "error: " + ioex.getMessage());
	    }*/
	}
	
	public void setPage(String url){
		this.urlString = url;
	}
	
	public String getUrlString() {
		return urlString;
	}
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setOnDowbloadComplete(Runnable runnable) {
		this.runnable = runnable;
	}

	public void setOnDowbloadProgress(Runnable runnable) {
		this.runableProgress = runnable;
	}

	public boolean isPost() {
		return post;
	}

	public void setPost(boolean post) {
		this.post = post;
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}

	public List<NameValuePair> getPostParams() {
		return postParams;
	}

	public void setPostParams(List<NameValuePair> postParams) {
		if(postParams!=null){
			this.postParams = postParams;
		}
	}
	
	public void addPostData(String key,String value){
		BasicNameValuePair newParams = new BasicNameValuePair(key, value);
		this.postParams.add(newParams);
	}
	
	public void addHeaderData(String key,String value){
		this.listHeader.put(key,value);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		intervalUpdateProgress = System.currentTimeMillis();
		finish = false;
	}

	// TODO do download process
	/**
	 * core process for DownloadManager
	 */
	@Override
	protected Boolean doInBackground(String... params) {
		setDownloadInfo(DownloadInfo.INFO_ON_PROGRESS);
		publishProgress(0);
		if(connection){
			try {
				InputStream is = null;
				if(isUpload()){
					//doFileUpload(getInputStreamUpload());
					is = doFileUpload(getFileUpload());
				}else if(isPost()){
					is = post();
				}else{
					is = getContent();
				}
				
				if(type.equals(TYPE_STRING)){
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			        StringBuilder sb = new StringBuilder();
			        String line = null;
			        while ((line = reader.readLine()) != null) {
			            sb.append(line + "\n");
			            publishProgress(sb.length());
			            
			        }
			        is.close();
			        resultString = sb.toString();
			        
				}else if(type.equals(TYPE_BITMAP)){
					bitmap = BitmapFactory.decodeStream(is);
					if(filename!=null && bitmap!=null){
						try{
							ByteArrayOutputStream bytes = new ByteArrayOutputStream();
							bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	
							//you can create a new file name "test.jpg" in sdcard folder.
							FileOutputStream f = new FileOutputStream(filename);
							InputStream isBitmap = new ByteArrayInputStream(bytes.toByteArray());

							int readlen = 0;
							Long totalRead = 0l;
							byte[] buf = new byte[1024];
							while ((readlen = isBitmap.read(buf)) > 0){
								f.write(buf, 0, readlen);
								totalRead += readlen;
								publishProgress(totalRead.intValue());
							}
							f.close();
							isBitmap.close();
							bytes.close();
						}catch (IOException e) {
							e.printStackTrace();
						}
					}
				}else if(type.equals(TYPE_RAW)){
					if(filename!=null){
						FileOutputStream f = new FileOutputStream(filename);

						int readlen = 0;
						Long totalRead = 0l;
						byte[] buf = new byte[1024];
						while ((readlen = is.read(buf)) > 0){
							f.write(buf, 0, readlen);
							totalRead += readlen;
							publishProgress(totalRead.intValue());
						}
						is.close();
						f.close();
					}else{
						setDownloadInfo(DownloadInfo.INFO_DOWNLOADED_FILEPATH_NOTFOUND);
						return false;
					}
				}
				setDownloadInfo(DownloadInfo.INFO_COMPLETE);
				return true;
			} catch (ClientProtocolException e) {
				setDownloadInfo(DownloadInfo.INFO_ERROR);
				e.printStackTrace();
			} catch (IOException e) {
				setDownloadInfo(DownloadInfo.INFO_URL_NOT_FOUND);
				e.printStackTrace();
			} catch (IllegalStateException e) {
				setDownloadInfo(DownloadInfo.INFO_ERROR);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				setDownloadInfo(DownloadInfo.INFO_ERROR);
				e.printStackTrace();
			} catch (NullPointerException e) {
				setDownloadInfo(DownloadInfo.INFO_URL_NOT_FOUND);
				e.printStackTrace();
			}
		}else{
			setDownloadInfo(DownloadInfo.INFO_CONNECTION_LOST);
		}
		return false;
	}
	
	protected void onPostExecute(Boolean success){ // bikin methode baru
		Logger.log(this, "GET FROM WEB COMPLETE_STATUS => "+ success);
		
		setSuccess(success);
		
		finish = true;
		if(downloadListeners!=null){
			if(type.equals(TYPE_STRING)){
				downloadListeners.onDownloadComplete(this,success,null,resultString,resultString);
			}else if(type.equals(TYPE_RAW)){
				downloadListeners.onDownloadComplete(this,success,null,null,resultString);
			}else if(type.equals(TYPE_BITMAP)){
				downloadListeners.onDownloadComplete(this,success,bitmap,null,bitmap);
			}
		}
		if(this.runnable!=null){
			Logger.log("handler post=>"+handler.post(this.runnable));
			//this.runnable.run();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		currentDownload = (long)values[0];
		if(runableProgress!=null){
			handler.post(this.runableProgress);
		}
		Long updateTime = System.currentTimeMillis();
		if(downloadListeners!=null && ((updateTime - intervalUpdateProgress) / 1000) > 1){
			intervalUpdateProgress = System.currentTimeMillis();
			downloadListeners.onDownloadProgress(downloadInfo, currentDownload);
		}
	}

	public Boolean getConnection() {
		return connection;
	}

	public void setConnection(Boolean connection) {
		this.connection = connection;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getResultString() {
		return resultString;
	}

	public void setResultString(String resultString) {
		this.resultString = resultString;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public Long getFilesize() {
		return filesize;
	}

	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

	public Long getCurrentDownload() {
		return currentDownload;
	}

	public void setCurrentDownload(Long currentDownload) {
		this.currentDownload = currentDownload;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public DownloadInfo getDownloadInfo() {
		return downloadInfo;
	}

	public void setDownloadInfo(DownloadInfo downloadInfo) {
		this.downloadInfo = downloadInfo;
	}

	public boolean isUpload() {
		return upload;
	}

	public void setUpload(boolean upload) {
		this.upload = upload;
	}

	public InputStream getInputStreamUpload() {
		return inputStreamUpload;
	}

	public void setInputStreamUpload(InputStream inputStreamUpload) {
		this.inputStreamUpload = inputStreamUpload;
	}

	public File getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(File fileUpload) {
		this.fileUpload = fileUpload;
	}
	
	public interface DownloadListeners{
		public void onDownloadProgress(DownloadInfo downloadInfo, Long currentDownload);
		public void onDownloadComplete(DownloadManager downloadManager, Boolean success, Bitmap bitmap, String resultString, Object ressultRaw);
	}

	public DownloadListeners getDownloadListeners() {
		return downloadListeners;
	}

	/**
	 * spesify listeners for download
	 * @param downloadListeners
	 */
	public void setDownloadListeners(DownloadListeners downloadListeners) {
		this.downloadListeners = downloadListeners;
	}
	public String getFileUploadName() {
		return fileUploadName;
	}
	public void setFileUploadName(String fileUploadName) {
		this.fileUploadName = fileUploadName;
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public HttpUtils getHttpUtils() {
		return httpUtils;
	}
	public void setHttpUtils(HttpUtils httpUtils) {
		this.httpUtils = httpUtils;
	}

	public String getPostType() {
		return postType;
	}
	public void setPostType(String postType) {
		this.postType = postType;
	}
	public String getRawStringPost() {
		return rawStringPost;
	}
	public void setRawStringPost(String rawStringPost) {
		this.rawStringPost = rawStringPost;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public boolean isSecure() {
		return secure;
	}
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	public Header[] getHeaders() {
		return headers;
	}
	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}
	
}
