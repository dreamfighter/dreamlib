package com.dreamfighter.android.manager;

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

import com.dreamfighter.android.enums.ActionMethod;
import com.dreamfighter.android.enums.ContentType;
import com.dreamfighter.android.enums.DownloadInfo;
import com.dreamfighter.android.enums.PayloadType;
import com.dreamfighter.android.enums.RequestType;
import com.dreamfighter.android.manager.RequestManager.RequestListeners;
import com.dreamfighter.android.manager.listeners.ConnectionListener;
/**
 * this class used for request to the server and get the response back from @see ConnectionListener class
 * 
 * @author fitra.adinugraha
 *
 */
public class ConnectionManager{
    private Context context;
    private ConnectionListener connectionListener;
    private RequestType requestType = RequestType.STRING;
    private PayloadType payloadType = PayloadType.FORM;
    private ContentType contentType = ContentType.APPLICATION_JSON;
    private ActionMethod actionMethod = ActionMethod.GET;
    
    private String rawPayload;
    private List<NameValuePair> postParams = new ArrayList<NameValuePair>();
    private Map<String,String> listHeader = new HashMap<String, String>();
    private String url;
    
    
    public ConnectionManager(Context context) {
        this.context = context;
    }
    
    public boolean isOnline(Context __context){
        try {
            ConnectivityManager __cm = (ConnectivityManager)__context.getSystemService((Context.CONNECTIVITY_SERVICE));
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
     * request to the server with url and request code for get specific response if there is more than one 
     * request in time
     * 
     * @param <code>url</code> server url that you want to request
     * @param <code>requestCode</code> request code for specific request
     */
    public void request(String url,final int requestCode){
        RequestManager req = new RequestManager(context);
        if(actionMethod.equals(ActionMethod.POST)){
            req.setPost(true);
            req.setPostType(payloadType.getValue());
            if(payloadType.equals(PayloadType.RAW)){
                req.setRawStringPost(getRawPayload());
            }else{
                req.setPostParams(postParams);
            }
        }
        req.setSecure(true);
        req.setContentType(contentType.getValue());
        req.setRequestType(requestType);
        req.addHeadersData(listHeader);
        
        req.setRequestListeners(new RequestListeners() {
            
            @Override
            public void onRequestProgress(DownloadInfo requestInfo, Long currentDownload) {
                if(connectionListener!=null){
                    connectionListener.requestOnProgress(ConnectionManager.this,requestCode, currentDownload);
                }
            }
            
            @Override
            public void onRequestComplete(RequestManager requestManager,
                    Boolean success, Bitmap bitmap, String resultString,
                    Object ressultRaw) {
                RequestType type = requestManager.getRequestType();
                if(connectionListener!=null){
                    if(success){
                        if(type.equals(RequestType.BITMAP)){
                            connectionListener.onRequestBitmapComplete(ConnectionManager.this,requestCode,bitmap);
                        }else if(type.equals(RequestType.STRING)){
                            connectionListener.onRequestComplete(ConnectionManager.this,requestCode,resultString);
                        }else if(type.equals(RequestType.RAW)){
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
    }
    
    /**
     * request to the server with url
     * 
     * 
     * @param <code>url</code> server url that you want to request
     */
    public void request(String url){
        request(url, this.hashCode());
    }
    
    public void request(){
        if(url!=null){
            request(this.url, this.hashCode());
        }
    }
    
    public void request(int requestCode){
        if(url!=null){
            request(this.url, requestCode);
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

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
