package com.dreamfighter.android.manager;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dreamfighter.android.enums.DownloadInfo;
import com.dreamfighter.android.enums.RequestType;
import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.manager.RequestManager.RequestListeners;
import com.dreamfighter.android.utils.CommonUtils;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * this cache time to live is in milisecond
 * default set to one hour
 */
public class FileCacheManager implements RequestListeners{
    
    private static FileCacheManager instance;
    private Context context;
    private long cacheTTL = 3600 * 1000;
    private int index = 0;
    private RequestManager requestManager;
    private ConcurrentLinkedQueue<FileRequest> linkedQueue = new ConcurrentLinkedQueue<FileRequest>();
    private CacheListener cacheListener;
    private FileRequest currImgRequest = null;
    private boolean limitless = false;
    private boolean refresh = false;
    private Object currentDisplay = new Object();


    public class FileRequest{
        public Object obj;
        public String url;
        public String dir;
        public String filename;
        public FileRequest(Object obj,String url,String dir,String filename){
            this.obj = obj;
            this.url = url;
            this.dir = dir;
            this.filename = filename;
        }
    }
    
    public interface CacheListener{
        void onLoaded(Object obj, File file, long lastUpdate);
        void onExpired(int index, long lastUpdate);
        void onLoadFailed();
        void onLoadFailed(String message);
        void onProgress(Object obj,long currentLoaded);
        void onAllLoadComplete();
        void onRequest(FileRequest imgRequest);
    }
    
    public static abstract class FileLoaderListener implements CacheListener{
        public void onExpired(int index, long lastUpdate){}
        public void onLoadFailed(){}
        public void onLoadFailed(String message){}
        public void onProgress(Object obj,long currentLoaded){}
        public void onAllLoadComplete(){}
        public void onRequest(FileRequest imgRequest){}
    }

    public FileCacheManager(Context context) {
        //super(context);
        this.context = context;
        this.requestManager = new RequestManager(context);
        this.requestManager.setRequestType(RequestType.RAW);
        this.requestManager.setRequestListeners(this);
        initializeDirectory();
    }
    
    public static FileCacheManager getInstance(Context context){
        if(instance==null){
            instance = new FileCacheManager(context);
        }
        return instance;
    }

    public FileCacheManager(Context context,int index) {
        //super(context);
        this.context = context;
        this.index = index;
        this.requestManager = new RequestManager(context);
        this.requestManager.setRequestType(RequestType.RAW);
        this.requestManager.setRequestListeners(this);
        initializeDirectory();
    }

    public FileCacheManager(int cacheTTL, Context context) {
        //super(context);
        this.cacheTTL = cacheTTL;
        initializeDirectory();
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
    
    public void removeCache(String url){
        String dirStr = CommonUtils.getBaseDirectory(context);
        String fileName = dirStr + CommonUtils.extractFilenameFromImgUrl(url);
        if(fileName!=null && !"".equals(fileName)){
            File img = new File(fileName);
            if(img.exists()){
                img.delete();
            }
        }
    }

    public void request(Object obj, String url,String dir, String filename) {
        //String dirStr = CommonUtils.getBaseDirectory(context);
        String fullname = dir + filename;
        File directory = new File(dir); 
        if(!directory.exists()){
            directory.mkdirs();
        }
        if(fullname!=null && !"".equals(fullname)){
            File img = new File(fullname);
            if(img.exists()){
                
                if(cacheListener!=null){
                    cacheListener.onLoaded(obj, img,img.lastModified());
                    FileRequest imgRequest = linkedQueue.poll();
                    //currImgRequest = linkedQueue.peek();
                    if(imgRequest!=null){
                        if (cacheListener!=null) {
                            cacheListener.onRequest(imgRequest);
                        }
                        request(imgRequest.obj,imgRequest.url, imgRequest.dir, imgRequest.filename);
                    }else{
                        cacheListener.onAllLoadComplete();
                    }
                }else if(!refresh && cacheListener!=null){
                    cacheListener.onLoadFailed("");
                }
                if(!refresh){
                    if(isLimitless() || img.lastModified() + cacheTTL > System.currentTimeMillis()){
                        return;
                    }
                }
            }
        }
        
        synchronized (currentDisplay) {
            this.currentDisplay = obj;
        }
        
        Logger.log("this.requestManager.isFinish()=>"+this.requestManager.isFinish());
        if(this.requestManager.isFinish()){
            currImgRequest = new FileRequest(obj, url, dir, filename);
            this.requestManager.setFilename(fullname);
            this.requestManager.request(url);
        }else{
            linkedQueue.add(new FileRequest(obj, url, dir, filename));
        }
    }

    public void request(Object obj, String url) {
        String fileName = CommonUtils.extractFilenameFromImgUrl(url);
        request(obj,url,fileName);
    }


    public void request(Object obj, String url, String fileName) {
        String dirStr = CommonUtils.getBaseDirectory(context);
        request(obj,url,dirStr,fileName);
    }

    @Override
    public void onRequestProgress(DownloadInfo requestInfo, Long currentDownload) {
        // TODO Auto-generated method stub
        if(cacheListener!=null){
            cacheListener.onProgress(currImgRequest.obj,(long)(100.0 * currentDownload / requestManager.getFilesize()));
        }
    }
    
    @Override
    public void onRequestComplete(RequestManager requestManager,
            Boolean success, Bitmap bitmap, String resultString,
            Object ressultRaw) {

        Logger.log("linkedQueue["+this.index+"].size()=>"+linkedQueue.size());
        if(cacheListener!=null && currImgRequest!=null && currImgRequest.obj==currentDisplay){
            
            if(requestManager.getFilename()!=null){
                cacheListener.onLoaded(currImgRequest.obj, new File(requestManager.getFilename()), System.currentTimeMillis());
            }else{
                cacheListener.onLoadFailed(requestManager.getRequestInfo().getValue());
            }
        }
        
        if (!success) {
            
        }
        
        FileRequest imgRequest = linkedQueue.poll();
        //currImgRequest = linkedQueue.peek();
        if(imgRequest!=null){
            request(imgRequest.obj,imgRequest.url,imgRequest.dir, imgRequest.filename);
        }else{
            cacheListener.onAllLoadComplete();
        }
    }

    
    public CacheListener getCacheListener() {
        return cacheListener;
    }

    public void setCacheListener(CacheListener cacheListener) {
        this.cacheListener = cacheListener;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getCacheTTL() {
        return cacheTTL;
    }

    public void setCacheTTL(long cacheTTL) {
        this.cacheTTL = cacheTTL;
    }
    
    public boolean isLimitless() {
        return limitless;
    }

    public void setLimitless(boolean limitless) {
        this.limitless = limitless;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

}
