package com.dreamfighter.android.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dreamfighter.android.enums.ActionMethod;
import com.dreamfighter.android.enums.ContentType;
import com.dreamfighter.android.enums.RequestInfo;
import com.dreamfighter.android.enums.ResponseType;
import com.dreamfighter.android.manager.listeners.ConnectionListener;
import com.dreamfighter.android.utils.CommonUtils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by dreamfighter on 8/25/16.
 */
public class FileCache2Manager {
    private static FileCache2Manager instance;
    private static final int DOWNLOAD = 0;
    private static final int LOADED = 1;
    private static final int MAX_CONNECTION = 5;

    private Context context;
    private ConcurrentLinkedQueue<FileRequest> linkedQueue = new ConcurrentLinkedQueue<FileRequest>();
    private Map<String,Integer> state = new HashMap<String,Integer>();

    private Map<Object,RequestManager> fileCaches = new HashMap<Object,RequestManager>();
    private Map<Object,FileCacheManager.FileLoaderListener> cacheListener = new HashMap<Object,FileCacheManager.FileLoaderListener>();
    //private FileCacheManager.FileLoaderListener listener;

    public class FileRequest{
        public Object obj;
        public String url;
        public String dir;
        public String filename;
        public boolean refresh = false;
        public FileRequest(Object obj,String url,String dir,String filename,boolean refresh){
            this.obj = obj;
            this.url = url;
            this.dir = dir;
            this.filename = filename;
            this.refresh = refresh;
        }
    }

    public static FileCache2Manager getInstance(Context context){
        if(instance==null){
            instance = new FileCache2Manager();
            instance.context = context;
            instance.initializeDirectory();
        }
        return instance;
    }

    public void initializeDirectory(){
        String dirStr = CommonUtils.getBaseDirectory(context);
        if(dirStr!=null && !"".equals(dirStr)){
            File dir = new File(dirStr);
            if(!dir.exists()){
                dir.mkdirs();
            }
        }
    }

    public FileCache2Manager addListener(Object obj,FileCacheManager.FileLoaderListener listener){
        cacheListener.put(obj,listener);
        return this;
    }

    public FileCache2Manager removeListener(Object obj){
        cacheListener.remove(obj);
        return this;
    }

    public void request(int obj, String url, final String fileName, final boolean refresh) {
        Integer localState = state.get(fileName);
        String dirStr = CommonUtils.getBaseDirectory(context);
        final String fullName = dirStr + fileName;
        File file = new File(fullName);

        if(!refresh) {
            if (localState!=null && localState == LOADED && file.exists()) {
                FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);

                if(listener!=null){

                    listener.onLoaded(obj,file,file.lastModified());
                }
                cacheListener.remove(obj);
                return;
            }
            if (localState!=null && localState == DOWNLOAD) {
                //IGNORE
                return;
            }
            if (file.exists()) {
                state.put(fileName, LOADED);
                FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);

                if(listener!=null){

                    listener.onLoaded(obj,file,file.lastModified());
                }
                cacheListener.remove(obj);
                return;
            }
        }

        if(fileCaches.size()<MAX_CONNECTION){
            state.put(fileName,DOWNLOAD);
            ConnectionManager conn = new ConnectionManager(context);
            conn.setActionMethod(ActionMethod.GET);
            conn.setFileName(fullName);
            conn.setResponseType(ResponseType.RAW);

            conn.setConnectionListener(new ConnectionListener(){

                /*
                @Override
                public void onRequestBitmapComplete(ConnectionManager connectionManager, int requestCode, Bitmap bitmap) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(requestCode);
                    state.put(fileName,LOADED);
                    if(listener!=null){
                        listener.onLoaded(requestCode,bitmap,new Date().getTime());
                    }
                    fileCaches.remove(requestCode);
                    cacheListener.remove(requestCode);
                    FileRequest fileRequest = linkedQueue.poll();
                    if(fileRequest!=null){
                        request((int)fileRequest.obj, fileRequest.url, fileRequest.filename,refresh);
                    }
                }
                */

                @Override
                public void onRequestRawComplete(ConnectionManager connectionManager, int requestCode, Object object) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(requestCode);
                    state.put(fileName,LOADED);
                    if(listener!=null){
                        listener.onLoaded(requestCode,new File(fullName),new Date().getTime());
                    }
                    fileCaches.remove(requestCode);
                    cacheListener.remove(requestCode);
                    FileRequest fileRequest = linkedQueue.poll();
                    if(fileRequest!=null){
                        request((int)fileRequest.obj, fileRequest.url, fileRequest.filename,refresh);
                    }
                }

                @Override
                public void requestOnProgress(ConnectionManager connectionManager, int requestCode, double currentDownload) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(requestCode);
                    if(listener!=null){
                        listener.onProgress(requestCode,(long)currentDownload);
                    }
                }

                @Override
                public void onRequestFailed(ConnectionManager connectionManager, int requestCode, RequestInfo info) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(requestCode);
                    if(listener!=null){
                        listener.onLoadFailed(requestCode,info.name());
                    }
                    fileCaches.remove(requestCode);
                    cacheListener.remove(requestCode);
                    FileRequest fileRequest = linkedQueue.poll();
                    if(fileRequest!=null){
                        request((int)fileRequest.obj, fileRequest.url, fileRequest.filename,refresh);
                    }
                }


            });
            fileCaches.put(obj,conn.request(url,obj));

        }else{
            linkedQueue.add(new FileRequest(obj,url,dirStr,fileName,refresh));
        }
    }
}
