package com.dreamfighter.android.manager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dreamfighter.android.entity.BaseEntity;
import com.dreamfighter.android.enums.ActionMethod;
import com.dreamfighter.android.enums.ContentType;
import com.dreamfighter.android.enums.DownloadInfo;
import com.dreamfighter.android.enums.PayloadType;
import com.dreamfighter.android.enums.RequestInfo;
import com.dreamfighter.android.enums.ResponseType;
import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.manager.RequestManager.RequestListeners;
import com.dreamfighter.android.utils.JsonUtils;

/**
 * this class used for request to the server and get the response back from @see ConnectionListener class
 * 
 * @author fitra.adinugraha
 *
 */
public class ConnectionManager{
    private Context context;
    private ConnectionListener connectionListener;
    private ResponseType responseType = ResponseType.STRING;
    private PayloadType payloadType = PayloadType.FORM;
    private ContentType contentType = ContentType.APPLICATION_JSON;
    private ActionMethod actionMethod = ActionMethod.GET;
    
    private String rawPayload;
    private List<NameValuePair> postParams = new ArrayList<NameValuePair>();
    private Map<String,String> listHeader = new HashMap<String, String>();
    private String url;
    private boolean doUpload;
    private File fileUpload;
    private String fileNameUpload;
    private String fileName;
    private boolean secure = true;
    private long totalDownload = 0l;
    private boolean reqquestCancel = false;
    
    public interface ConnectionListener{
        void onRequestBitmapComplete(ConnectionManager connectionManager,int requestCode,Bitmap bitmap);
        void onRequestRawComplete(ConnectionManager connectionManager,int requestCode,Object object);
        void onRequestComplete(ConnectionManager connectionManager,int requestCode,String resultString);
        void onCustomRequest(ConnectionManager connectionManager,int requestCode,InputStream is);
        void onRequestFailed(ConnectionManager connectionManager,int requestCode,RequestInfo info);
       
        void onRequestComplete(RequestManager requestManager,
                Boolean success, Bitmap bitmap, String resultString,
                Object ressultRaw);

        void requestOnProgress(ConnectionManager connectionManager,int requestCode, double currentDownload);
    }
    
    public ConnectionManager(Context context) {
        this.context = context;
    }
    
    public boolean isOnline(){
        try {
            ConnectivityManager __cm = (ConnectivityManager)context.getSystemService((Context.CONNECTIVITY_SERVICE));
            NetworkInfo __ni = __cm.getActiveNetworkInfo();
            int __netType = __ni.getType();
            if (__ni!=null && __ni.isConnected()){
                if (__netType==ConnectivityManager.TYPE_WIFI 
                        || __netType==ConnectivityManager.TYPE_MOBILE){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean isOnline(Context context){
        try {
            ConnectivityManager __cm = (ConnectivityManager)context.getSystemService((Context.CONNECTIVITY_SERVICE));
            NetworkInfo __ni = __cm.getActiveNetworkInfo();
            int __netType = __ni.getType();
            if (__ni!=null && __ni.isConnected()){
                if (__netType==ConnectivityManager.TYPE_WIFI 
                        || __netType==ConnectivityManager.TYPE_MOBILE){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * print all post parameter in the request
     * 
     */
    public void printPostParam(){
        for(NameValuePair valuePair:postParams){
            Logger.log("["+valuePair.getName()+":"+valuePair.getValue()+"]");
        }
    }

    public void cancel(){

    }
    public long getTotalDownload() {
        return totalDownload;
    }

    /**
     * request to the server with url and request code for get specific response if there is more than one 
     * request in time
     * 
     * @param <code>url</code> server url that you want to request
     * @param <code>requestCode</code> request code for specific request
     */
    public RequestManager request(String url,final int requestCode){
        final RequestManager req = new RequestManager(context);
        if(actionMethod.equals(ActionMethod.POST) && !doUpload){
            req.setPost(true);
            req.setPostType(payloadType.getValue());
            if(payloadType.equals(PayloadType.RAW)){
                req.setRawStringPost(getRawPayload());
            }else{
                req.setPostParams(postParams);
            }
        }
        req.setSecure(secure);
        req.setContentType(contentType.getValue());
        req.setResponseType(responseType);
        req.addHeadersData(listHeader);

        if(responseType.equals(ResponseType.RAW) && fileName==null){
            Logger.error("WARNING: file name for raw response is null");
        }

        req.setFilename(fileName);
        
        if(doUpload){
            req.setFileUploadName(fileNameUpload);
            req.setPostParams(postParams);
            req.setPost(true);
            req.setFileUpload(fileUpload);
            req.setUpload(true);
        }
        if(responseType.equals(ResponseType.CUSTOM)){
            req.setCustomRequest(new RequestManager.CustomRequest() {
                @Override
                public void onRequest(InputStream is) {
                    if(connectionListener!=null){
                        totalDownload = req.getFilesize();
                        connectionListener.onCustomRequest(ConnectionManager.this,requestCode,is);
                    }
                }
            });
        }
        req.setRequestListeners(new RequestListeners() {
            
            @Override
            public void onRequestProgress(DownloadInfo requestInfo, Long currentDownload) {
                if(reqquestCancel){
                    req.setRequestCancel(true);
                }
                if(connectionListener!=null){
                    double total = (100.0 * currentDownload) / req.getFilesize();
                    connectionListener.requestOnProgress(ConnectionManager.this,requestCode, total);
                }
            }


            
            @Override
            public void onRequestComplete(RequestManager requestManager,
                    Boolean success, Bitmap bitmap, String resultString,
                    Object ressultRaw) {
                ResponseType type = requestManager.getResponseType();
                if(connectionListener!=null){
                    if(success){
                        if(type.equals(ResponseType.BITMAP)){
                            connectionListener.onRequestBitmapComplete(ConnectionManager.this,requestCode,bitmap);
                        }else if(type.equals(ResponseType.STRING)){
                            connectionListener.onRequestComplete(ConnectionManager.this,requestCode,resultString);
                        }else if(type.equals(ResponseType.RAW)){
                            connectionListener.onRequestRawComplete(ConnectionManager.this,requestCode,ressultRaw);
                        }
                    }else{
                        connectionListener.onRequestFailed(ConnectionManager.this,requestCode, requestManager.getRequestInfo());
                    }
                    connectionListener.onRequestComplete(requestManager, success, bitmap, resultString, ressultRaw);
                }
            }
        });
        req.request(url);
        return req;
    }
    
    /**
     * request to the server with url
     * 
     * 
     * @param <code>url</code> server url that you want to request
     */
    public RequestManager request(String url){
        return request(url, this.hashCode());
    }
    
    public RequestManager request(){
        if(url!=null){
            return request(this.url, this.hashCode());
        }
        return null;
    }
    
    public void upload(String url,String fileNameUpload,File fileUpload){
        if(url!=null){
            this.fileNameUpload = fileNameUpload;
            this.url = url;
            doUpload = true;
            this.fileUpload = fileUpload;
            request(this.url, this.hashCode());
        }
    }
    
    public RequestManager request(int requestCode){
        if(url!=null){
            return request(this.url, requestCode);
        }
        return null;
    }
    /**
     * add post to request if post is true
     * @param key <code>String</code> header key
     * @param value <code>long</code> header value
     */
    public void addPostData(String key,long value){
        addPostData(key,String.valueOf(value));
    }

    /**
     * add post to request if post is true
     * @param key <code>String</code> header key
     * @param value <code>double</code> header value
     */
    public void addPostData(String key,double value){
        addPostData(key,String.valueOf(value));
    }

    /**
     * add post to request if post is true
     * @param key <code>String</code> header key
     * @param value <code>float</code> header value
     */
    public void addPostData(String key,float value){
        addPostData(key,String.valueOf(value));
    }
    /**
     * add post to request if post is true
     * @param key <code>String</code> header key
     * @param value <code>int</code> header value
     */
    public void addPostData(String key,int value){
        addPostData(key,String.valueOf(value));
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
    
    public void setHeadersData(Map<String,String> listHeader){
        this.listHeader.putAll(listHeader);
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    /**
     * call this method to get response from server
     * @param connectionListener
     */
    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public ActionMethod getActionMethod() {
        return actionMethod;
    }

    /**
     * set protocol type
     * @see ActionMethod.POST will request with protocol POST
     * or @see ActionMethod.GET will request with protocol GET
     * @param actionMethod
     */
    public void setActionMethod(ActionMethod actionMethod) {
        this.actionMethod = actionMethod;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    /**
     * call this method if you set the @see MethodType.POST and @see PayloadType.RAW
     * 
     * 
     * @param <code>raw</code> the payload content it can be plain text, xml, or json
     */
    public void setRawPayload(String raw) {
        this.rawPayload = raw;
    }

    public void setEntity(BaseEntity baseEntity){
        setRawPayload(JsonUtils.build(baseEntity));
        Logger.log(rawPayload);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setReqquestCancel(boolean reqquestCancel) {
        this.reqquestCancel = reqquestCancel;
    }
}
