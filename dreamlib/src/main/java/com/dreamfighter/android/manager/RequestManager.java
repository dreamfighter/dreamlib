package com.dreamfighter.android.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.DetailedState;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.dreamfighter.android.R;
import com.dreamfighter.android.entity.ProxyConfiguration;
import com.dreamfighter.android.enums.DownloadInfo;
import com.dreamfighter.android.enums.RequestInfo;
import com.dreamfighter.android.enums.ResponseType;
import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.utils.HttpUtils;

/**
 * this class is for managing request to server
 * can add header, raw post, or upload
 * you can add listener too to callback after you get response from server
 * 
 * @author fitra.bayu
 * @version 1.0
 * 
 *
 */
public class RequestManager {
    public static final String POST_TYPE_RAW= "raw";
    public static final String POST_TYPE_FORM= "form";
    public static final String CONTENT_TYPE_JSON= "application/json";
    public static final String CONTENT_TYPE_HTML= "text/html";
    private String urlString;
    private String rawStringPost;
    private boolean post = false;
    private boolean finish = true;
    private boolean success = false;
    private CookieStore cookieStore;
    private List<NameValuePair> postParams = new ArrayList<NameValuePair>();
    private Boolean connection = true;
    private Bitmap bitmap = null;
    private ResponseType responseType = ResponseType.STRING;
    private String postType = POST_TYPE_FORM;
    private String contentType = CONTENT_TYPE_HTML;
    private String resultString = null;
    private String filename = null;
    private Long filesize = 0l;
    private Long currentDownload = 0l;
    private DownloadInfo downloadInfo;
    private RequestInfo requestInfo;
    private boolean upload;
    private InputStream inputStreamUpload;
    private File fileUpload;
    public static boolean ACTIVATED_PROXY_AUTH = true;
    private boolean printResponseHeader = false;
    
    // listener to specify callback
    private RequestListeners requestListeners;
    private Context context;
    private String fileUploadName = "FILE";
    private HttpUtils httpUtils;
    private Map<String,String> listHeader = new HashMap<String, String>();
    private RequestTask requestTask;
    private static Dialog dialogProxyAuth = null;
    //private Long intervalUpdateProgress = 0l;
    private CustomRequest customRequest;
    private boolean secure = false;
    private long timeout = 60000;

    public interface CustomRequest{
        void onRequest(InputStream is);
    }
    
    public RequestManager(Context context){
        this.context = context;
        requestTask = new RequestTask();
        checkProxyConfiguration();
    }
    
    public RequestManager(Context context, String url){
        this.urlString= url;
        this.context = context;
        requestTask = new RequestTask();
    }
    
    @SuppressLint("NewApi")
    public void request(){
        if(!requestTask.getStatus().equals(Status.RUNNING) && !requestTask.getStatus().equals(Status.FINISHED)){
            //requestTask.execute();
        }else{
            requestTask = new RequestTask();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            requestTask.execute();
        }
    }
    
    @SuppressLint("NewApi")
    public void request(ResponseType responseType){
        this.responseType = responseType;
        if(!requestTask.getStatus().equals(Status.RUNNING) && !requestTask.getStatus().equals(Status.FINISHED)){
            //requestTask.execute();
        }else{
            requestTask = new RequestTask();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            requestTask.execute();
        }
    }
    
    @SuppressLint("NewApi")
    public void request(String url){
        this.urlString = url;
        if(!requestTask.getStatus().equals(Status.RUNNING) && !requestTask.getStatus().equals(Status.FINISHED)){
            //requestTask.execute();
        }else{
            requestTask = new RequestTask();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            requestTask.execute();
        }
    }
    

    
    @SuppressWarnings("deprecation")
    public void checkProxyConfiguration(){

        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


        boolean isUsingWifi = false; 
        if( wifi.isAvailable() && wifi.getDetailedState() == DetailedState.CONNECTED){
            isUsingWifi = true;
        }
        if(!ACTIVATED_PROXY_AUTH || !isUsingWifi){
            return;
        }
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
                try{
                    if(dialogProxyAuth==null){
                        dialogProxyAuth = createDialog(proxyAddress,proxyPort);
                        dialogProxyAuth.show();
                    }else if(!dialogProxyAuth.isShowing()){
                        dialogProxyAuth.show();
                    }
                }catch(RuntimeException e){
                    e.printStackTrace();
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
    
    @SuppressLint("InflateParams")
    public Dialog createDialog(final String proxyAddress,final int proxyPort) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(getContext());
        
        View view = inflater.inflate(R.layout.df_proxy_auth_layout, null, false);
        final EditText username = (EditText)view.findViewById(R.id.df_username);
        final EditText password = (EditText)view.findViewById(R.id.df_password);
        
        TextView textViewLabel = (TextView) view.findViewById(R.id.label_textview);
        textViewLabel.setText(getContext().getString(R.string.df_proxy_auth,proxyAddress));
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
     * upload file to server 
     * @param url for upload
     */
    public void upload(String url){
        this.upload = true;
        this.urlString = url;
        if(!requestTask.getStatus().equals(Status.RUNNING) && !requestTask.getStatus().equals(Status.FINISHED)){
            requestTask.execute();
        }else{
            requestTask = new RequestTask();
            requestTask.execute();
        }
    }
    
    public boolean isRunning(){
        return requestTask.getStatus().equals(Status.RUNNING);
    }
    
    /**
     * request using GET 
     * @return InputStream requestResponse
     * @throws IllegalStateException
     * @throws IOException
     */
    public InputStream getContent() throws IllegalStateException, IOException, NullPointerException, UnknownHostException{
        
        Logger.log(this, "DOWNLOAD CONTENT USING GET URL => " + getUrlString());
        
        if(getUrlString() == null){ 
            return null;
        }
        
        DefaultHttpClient httpClient = new DefaultHttpClient();

        /*
         * set htt params for timeout
         */
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, (int)timeout);
        HttpConnectionParams.setSoTimeout(httpParams, (int)timeout);
        
        if(secure){
            
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                DFSSLSocketFactory sf = new DFSSLSocketFactory(trustStore);
                sf.setHostnameVerifier(DFSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                schemeRegistry.register(new Scheme("https", sf, 443));
                ClientConnectionManager cm = new SingleClientConnManager(null, schemeRegistry);
                httpClient = new DefaultHttpClient(cm,null);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
            
            
        }
        
        if(HttpUtils.instance.getCookieStore(context)!=null){
            httpClient.setCookieStore(HttpUtils.instance.getCookieStore(context));
        }
        /*if(HttpUtils.instance.getCookieStore(context)!=null){
            httpClient.setCookieStore(HttpUtils.instance.getCookieStore(context));
        }else{
            HttpUtils.instance.setCookieStore(context,httpClient.getCookieStore());
        }*/
        
        HttpGet httpGet = new HttpGet(getUrlString());
        if(listHeader!=null){
            for(String key:listHeader.keySet()){
                httpGet.addHeader(key, listHeader.get(key));
            }
        }

        Logger.log("using proxy=>"+HttpUtils.instance.isUseProxy());
        
        String hostname = httpGet.getURI().getHost();
        
        Logger.log("host=>"+hostname);
        Logger.log("byPassProxies=>"+HttpUtils.instance.getByPassProxy());
        String[] byPassProxies = HttpUtils.instance.getByPassProxy().split(",");
        boolean isByPassProxy = false;
        for(String byPass:byPassProxies){
            if (hostname.equals(byPass)) {
                isByPassProxy = true;
                break;
            }
        }
        if(HttpUtils.instance.isUseProxy() && !isByPassProxy){
            HttpHost proxy = new HttpHost(HttpUtils.instance.getHostname(), HttpUtils.instance.getPort());
            if(HttpUtils.instance.isUseAuthProxy()){
                httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(HttpUtils.instance.getUsername(), HttpUtils.instance.getPassword()));
            }
            ConnRouteParams.setDefaultProxy(httpClient.getParams(), proxy);
        }
            
        
        
        HttpResponse httpResponse = httpClient.execute(httpGet);
        
        filesize = httpResponse.getEntity().getContentLength();
        return httpResponse.getEntity().getContent();
    }
    
    public void printPostParam(){
        for(NameValuePair valuePair:postParams){
            Logger.log("["+valuePair.getName()+":"+valuePair.getValue()+"]");
        }
    }
    
    /**
     * request url using post
     * @return InputStream requestResponse
     * @throws ClientProtocolException
     * @throws IOException
     */
    public InputStream post() throws ClientProtocolException, IOException, UnknownHostException{
        Logger.log(this, "DOWNLOAD CONTENT USING POST URL => " + getUrlString());

        DefaultHttpClient httpClient = new DefaultHttpClient();

        /*
         * set htt params for timeout
         */
        HttpParams httpParams = new BasicHttpParams();
        //HttpConnectionParams.setConnectionTimeout(httpParams, (int)timeout);
        //HttpConnectionParams.setSoTimeout(httpParams, (int)timeout);

        
        /*if(HttpUtils.instance.getCookieStore(context)!=null){
            httpClient.setCookieStore(HttpUtils.instance.getCookieStore(context));
        }else{
            HttpUtils.instance.setCookieStore(context,httpClient.getCookieStore());
        }*/

        // In a POST request, we don't pass the values in the URL.
        //Therefore we use only the web page URL as the parameter of the HttpPost argument
        HttpPost httpPost = new HttpPost(getUrlString());
        
        if(secure){
            
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                DFSSLSocketFactory sf = new DFSSLSocketFactory(trustStore);
                sf.setHostnameVerifier(DFSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                schemeRegistry.register(new Scheme("https", sf, 443));
                ClientConnectionManager cm = new SingleClientConnManager(null, schemeRegistry);
                httpClient = new DefaultHttpClient(cm,null);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
            
            
        }
        
        if(HttpUtils.instance.getCookieStore(context)!=null){
            httpClient.setCookieStore(HttpUtils.instance.getCookieStore(context));
        }
        
        if(listHeader!=null){
            for(String key:listHeader.keySet()){
                httpPost.addHeader(key, listHeader.get(key));
            }
        }

        Logger.log("using proxy=>"+HttpUtils.instance.isUseProxy());
        String hostname = httpPost.getURI().getHost();
        
        Logger.log("host=>"+hostname);
        Logger.log("byPassProxies=>"+HttpUtils.instance.getByPassProxy());
        String[] byPassProxies = HttpUtils.instance.getByPassProxy().split(",");
        boolean isByPassProxy = false;
        for(String byPass:byPassProxies){
            if (hostname.equals(byPass)) {
                isByPassProxy = true;
                break;
            }
        }
        
        
        if(HttpUtils.instance.isUseProxy() && !isByPassProxy){
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
        }else if(postType.equals(POST_TYPE_RAW)){
            StringEntity se = new StringEntity(rawStringPost);  
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, contentType));
            httpPost.setEntity(se);
        }
        // setEntity() hands the entity (here it is urlEncodedFormEntity) to the request.

        // HttpResponse is an interface just like HttpPost.
        //Therefore we can't initialize them
        HttpResponse httpResponse = httpClient.execute(httpPost);
        // According to the JAVA API, InputStream constructor do nothing. 
        //So we can't initialize InputStream although it is not an interface
        filesize = httpResponse.getEntity().getContentLength();
        
        if(isPrintResponseHeader()){
            CookieStore cookieStore = httpClient.getCookieStore();
            for (Cookie header : cookieStore.getCookies()) {
                Logger.log("["+header.getName()+":"+header.getValue()+"]");
            }
            Logger.log("Header Request");
            for (Header header : httpPost.getAllHeaders()) {
                Logger.log("["+header.getName()+":"+header.getValue()+"]");
            }
            
            Logger.log("Header Response");
            for (Header header : httpResponse.getAllHeaders()) {
                Logger.log("["+header.getName()+":"+header.getValue()+"]");
            }
        }
        
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
    
    public void setPage(String url){
        this.urlString = url;
    }
    
    public String getUrlString() {
        return urlString;
    }
    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public boolean isPost() {
        return post;
    }

    /**
     * set post action
     * @param post
     */
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

    /**
     * set post params for request type post and post type is FORM
     * @param postParams <code>List<NameValuePair></code>
     */
    public void setPostParams(List<NameValuePair> postParams) {
        if(postParams!=null){
            this.postParams = postParams;
        }
    }
    
    /**
     * add post to request if post is true
     * @param key <code>String</code> header key
     * @param value <code>String</code> header value
     */
    public void addPostData(String key,String value){
        BasicNameValuePair newParams = new BasicNameValuePair(key, value);
        this.postParams.add(newParams);
    }
    
    /**
     * add header to request
     * @param key <code>String</code> header key
     * @param value <code>String</code> header value
     */
    public void addHeaderData(String key,String value){
        this.listHeader.put(key,value);
    }
    
    public void addHeadersData(Map<String,String> listHeader){
        this.listHeader.putAll(listHeader);
    }
    
    private class RequestTask extends AsyncTask<String, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            finish = false;
            //intervalUpdateProgress = System.currentTimeMillis();
        }
    
        // TODO do download process
        /**
         * core process for DownloadManager
         */
        @Override
        protected Boolean doInBackground(String... params) {
            setDownloadInfo(DownloadInfo.INFO_ON_PROGRESS);
            requestInfo = RequestInfo.INFO_ON_PROGRESS;
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
                    
                    if(responseType.equals(ResponseType.CUSTOM) && customRequest!=null){
                        customRequest.onRequest(is);
                        
                    }else if(responseType.equals(ResponseType.STRING)){
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                            publishProgress(sb.length());
                            
                        }
                        is.close();
                        resultString = sb.toString();
                        
                    }else if(responseType.equals(ResponseType.BITMAP)){
                        
                        if(filename!=null){
                            try{
                                //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                
                                //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                
                                //you can create a new file name "test.jpg" in sdcard folder.
                                FileOutputStream f = new FileOutputStream(filename);
                                //InputStream isBitmap = new ByteArrayInputStream(bytes.toByteArray());
    
                                int readlen = 0;
                                Long totalRead = 0l;
                                byte[] buf = new byte[1024];
                                while ((readlen = is.read(buf)) > 0){
                                    f.write(buf, 0, readlen);
                                    totalRead += readlen;
                                    publishProgress(totalRead.intValue());
                                }
                                f.close();
                                is.close();
                                //isBitmap.close();
                                //bytes.close();
                                bitmap = BitmapFactory.decodeFile(filename);
                            }catch (IOException e) {
                                e.printStackTrace();
                            }catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            }
                        }else{
                            bitmap = BitmapFactory.decodeStream(is);
                        }
                    }else if(responseType.equals(ResponseType.RAW)){
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
                            requestInfo = RequestInfo.INFO_DOWNLOADED_FILEPATH_NOTFOUND;
                            return false;
                        }
                    }
                    setDownloadInfo(DownloadInfo.INFO_COMPLETE);
                    requestInfo = RequestInfo.INFO_COMPLETE;
                    return true;
                } catch (ClientProtocolException e) {
                    setDownloadInfo(DownloadInfo.INFO_ERROR);
                    requestInfo = RequestInfo.INFO_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    setDownloadInfo(DownloadInfo.INFO_URL_NOT_FOUND);
                    requestInfo = RequestInfo.INFO_URL_NOT_FOUND;
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    setDownloadInfo(DownloadInfo.INFO_ERROR);
                    requestInfo = RequestInfo.INFO_ERROR;
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    setDownloadInfo(DownloadInfo.INFO_ERROR);
                    requestInfo = RequestInfo.INFO_ERROR;
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    setDownloadInfo(DownloadInfo.INFO_URL_NOT_FOUND);
                    requestInfo = RequestInfo.INFO_URL_NOT_FOUND;
                    e.printStackTrace();
                }
            }else{
                setDownloadInfo(DownloadInfo.INFO_CONNECTION_LOST);
                requestInfo = RequestInfo.INFO_CONNECTION_LOST;
            }
            return false;
        }
        
        protected void onPostExecute(Boolean success){ // bikin methode baru
            Logger.log(this, "GET FROM WEB COMPLETE_STATUS => "+ success);
            
            setSuccess(success);
            
            finish = true;
            if(requestListeners!=null){
                if(responseType.equals(ResponseType.STRING)){
                    requestListeners.onRequestComplete(RequestManager.this,success,null,resultString,resultString);
                }else if(responseType.equals(ResponseType.RAW)){
                    requestListeners.onRequestComplete(RequestManager.this,success,null,null,resultString);
                }else if(responseType.equals(ResponseType.BITMAP)){
                    requestListeners.onRequestComplete(RequestManager.this,success,bitmap,null,bitmap);
                }
            }
        }
    
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            currentDownload = (long)values[0];
            //Long updateTime = System.currentTimeMillis();
            if(requestListeners!=null){
                //intervalUpdateProgress = System.currentTimeMillis();
                requestListeners.onRequestProgress(downloadInfo, currentDownload);
            }
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

    public ResponseType getResponseType() {
        return responseType;
    }

    /**
     * set request type if you want request result string set STRING
     * BITMAP if you want request image and RAW for another type
     * @see ResponseType
     * @param responseType
     */
    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public String getResultString() {
        return resultString;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * if the request has been finished
     * @return
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * get response size that will be retrieve
     * @return response size will be retrieve
     */
    public Long getFilesize() {
        return filesize;
    }

    public Long getCurrentDownload() {
        return currentDownload;
    }

    public boolean isSuccess() {
        return success;
    }

    private void setSuccess(boolean success) {
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

    /**
     * set file want to upload to server
     * @param fileUpload <code>File</code>
     */
    public void setFileUpload(File fileUpload) {
        this.fileUpload = fileUpload;
    }
    
    
    /**
     * interface request listener for callback if response has completed
     * @author fitra.bayu
     *
     */
    public interface RequestListeners{
        /**
         * this method will be called if request/download in progress
         * @param requestInfo <code>DownloadInfo</code>
         * @param currentDownload <code>Long</code>
         */
        public void onRequestProgress(DownloadInfo requestInfo, Long currentDownload);
        
        /**
         * this method will called if response has been completed retrieve
         * 
         * @param requestManager
         * @param success <code>Boolean</code> true if request completely retrieve and false if there is any problem in request
         * @param bitmap <code>Bitmap</code> if you set ResponseType BITMAP result will be in the bitmap parameter
         * @param resultString <code>String</code>  if you set ResponseType STRING result will be in the resultString
         * @param ressultRaw <code>Object</code> if you set ResponseType RAW result will be in the resultRaw parameter
         */
        public void onRequestComplete(RequestManager requestManager, Boolean success, Bitmap bitmap, String resultString, Object ressultRaw);
    }

    public RequestListeners getRequestListeners() {
        return requestListeners;
    }

    /**
     * specify listeners for download
     * @param requestListeners <code>RequestListeners</code>
     */
    public void setRequestListeners(RequestListeners requestListeners) {
        this.requestListeners = requestListeners;
    }
    
    @Deprecated
    public String getFileUploadName() {
        return fileUploadName;
    }
    
    public void setFileUploadName(String fileUploadName) {
        this.fileUploadName = fileUploadName;
    }
    public Context getContext() {
        return context;
    }
    public HttpUtils getHttpUtils() {
        return httpUtils;
    }
    public String getPostType() {
        return postType;
    }
    
    /**
     * set post type if you want form or raw
     * if you set form you must add post data @see setPostData()
     * if you set raw you must set raw string @see setRawStringPost() 
     * @param postType
     */
    public void setPostType(String postType) {
        this.postType = postType;
    }
    public String getRawStringPost() {
        return rawStringPost;
    }
    /**
     * set string raw post if you want to add raw post like application/json
     * @param rawStringPost <code>String</code>
     */
    public void setRawStringPost(String rawStringPost) {
        this.rawStringPost = rawStringPost;
    }
    public String getContentType() {
        return contentType;
    }
    
    /**
     * set content type or mime result if you want example text/html or application/json
     * @see ${CONTENT_TYPE_JSON} ${CONTENT_TYPE_HTML} 
     * @param contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public CustomRequest getCustomRequest() {
        return customRequest;
    }

    public void setCustomRequest(CustomRequest customRequest) {
        this.customRequest = customRequest;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public boolean isPrintResponseHeader() {
        return printResponseHeader;
    }

    public void setPrintResponseHeader(boolean printResponseHeader) {
        this.printResponseHeader = printResponseHeader;
    }
}
