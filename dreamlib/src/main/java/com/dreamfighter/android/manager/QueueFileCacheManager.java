package com.dreamfighter.android.manager;

import android.content.Context;

import com.dreamfighter.android.utils.CommonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dreamfighter on 8/21/16.
 */
public class QueueFileCacheManager {
    private static QueueFileCacheManager instance;
    private Context context;
    private int maxConnection = 4;

    private Map<Object,FileCacheManager> fileCaches = new HashMap<Object,FileCacheManager>();
    private Map<Object,FileCacheManager.FileLoaderListener> cacheListener = new HashMap<Object,FileCacheManager.FileLoaderListener>();

    public static QueueFileCacheManager getInstance(Context context){
        if(instance==null){
            instance = new QueueFileCacheManager();
            instance.context = context;
        }
        return instance;
    }

    public FileCacheManager build(Object obj) {
        if(fileCaches.size()<maxConnection){
            FileCacheManager fileCacheManager = new FileCacheManager(context);
            fileCaches.put(obj,fileCacheManager);
            fileCacheManager.setCacheListener(new FileCacheManager.FileLoaderListener() {
                @Override
                public void onLoaded(Object obj, File file, long lastUpdate) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);
                    if(listener!=null) {
                        listener.onLoaded(obj, file, lastUpdate);
                    }
                }

                @Override
                public void onProgress(Object obj, long currentLoaded) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);
                    if(listener!=null) {
                        listener.onProgress(obj, currentLoaded);
                    }
                }

                @Override
                public void onConnectionError(Object obj) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);
                    if(listener!=null) {
                        listener.onConnectionError(obj);
                    }
                }

                @Override
                public void onLoadFailed(Object obj, String message) {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);
                    if(listener!=null) {
                        listener.onLoadFailed(obj,message);
                    }
                }
            });

            return fileCacheManager;
        }else{
            int min = Integer.MAX_VALUE;
            int i = 0;
            FileCacheManager cacheManager = null;
            for (Object objCallback:fileCaches.keySet()){
                if(min>fileCaches.get(objCallback).queueSize()){
                    cacheManager = fileCaches.get(objCallback);
                    min = cacheManager.queueSize();
                }
            }

            return cacheManager;
        }
    }

    public void addFileCacheListener(Object obj,FileCacheManager.FileLoaderListener fileLoaderListener){
        cacheListener.put(obj,fileLoaderListener);
    }

    public void removeFileCacheListener(Object obj){
        cacheListener.remove(obj);
    }
}
