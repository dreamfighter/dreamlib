package id.dreamfighter.android.manager.listeners;

import java.io.InputStream;

import android.graphics.Bitmap;

import id.dreamfighter.android.enums.RequestInfo;
import id.dreamfighter.android.manager.ConnectionManager;
import id.dreamfighter.android.manager.RequestManager;

public class ConnectionListener implements ConnectionManager.ConnectionListener{
    public void onRequestBitmapComplete(ConnectionManager connectionManager,int requestCode,Bitmap bitmap){}
    public void onRequestRawComplete(ConnectionManager connectionManager,int requestCode,Object object){}
    public void onRequestComplete(ConnectionManager connectionManager,int requestCode,String resultString){}
    public void onCustomRequest(ConnectionManager connectionManager,int requestCode,InputStream is){}
    public void onRequestFailed(ConnectionManager connectionManager,int requestCode,RequestInfo info){}
   
    public void onRequestComplete(RequestManager requestManager,
            Boolean success, Bitmap bitmap, String resultString,
            Object ressultRaw){
        
    }

    public void requestOnProgress(ConnectionManager connectionManager,int requestCode, Long currentDownload) {
        // TODO Auto-generated method stub
        
    }
}
