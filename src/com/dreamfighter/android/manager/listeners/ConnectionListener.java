package com.dreamfighter.android.manager.listeners;

import java.io.InputStream;

import android.graphics.Bitmap;

import com.dreamfighter.android.enums.RequestInfo;
import com.dreamfighter.android.manager.ConnectionManager;
import com.dreamfighter.android.manager.RequestManager;

public class ConnectionListener{
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
