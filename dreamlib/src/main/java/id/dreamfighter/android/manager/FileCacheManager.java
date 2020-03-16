package id.dreamfighter.android.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import androidx.core.content.FileProvider;
import id.dreamfighter.android.enums.DownloadInfo;
import id.dreamfighter.android.log.Logger;
import id.dreamfighter.android.network.APIService;
import id.dreamfighter.android.network.APIServiceSample;
import id.dreamfighter.android.network.RestClient;
import id.dreamfighter.android.utils.CommonUtils;
import id.dreamfighter.android.utils.FileUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * this cache time to live is in milisecond
 * default set to one hour
 */
public class FileCacheManager{
    
    private static FileCacheManager instance;
    private Context context;
    private long cacheTTL = 3600 * 1000;
    private int index = 0;
    private boolean limitless = false;
    private boolean refresh = false;
    private RestClient requestManager;
    private ConcurrentLinkedQueue<FileRequest> linkedQueue = new ConcurrentLinkedQueue<FileRequest>();
    private CacheListener cacheListener;
    private FileRequest currImgRequest = null;
    private Object currentDisplay = new Object();


    public class FileRequest{
        public Object obj;
        public String url;
        public String dir;
        public String filename;
        public FileRequest(Object obj, String url, String dir, String filename){
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
        void onLoadFailed(Object obj, String message);
        void onProgress(Object obj, long currentLoaded);
        void onAllLoadComplete();
        void onRequest(FileRequest imgRequest);
        void onConnectionError(Object obj);
    }
    
    public static abstract class FileLoaderListener implements CacheListener{
        public void onExpired(int index, long lastUpdate){}
        public void onLoadFailed(){}
        public void onLoadFailed(Object obj, String message){}
        public void onProgress(Object obj, long currentLoaded){}
        public void onAllLoadComplete(){}
        public void onRequest(FileRequest imgRequest){}
        public void onConnectionError(Object obj){}
    }

    public FileCacheManager(Context context) {
        //super(context);
        this.context = context;
        this.requestManager = new RestClient();
        initializeDirectory();
    }
    
    public static FileCacheManager getInstance(Context context){
        if(instance==null){
            instance = new FileCacheManager(context);
        }
        return instance;
    }

    public FileCacheManager(Context context, int index) {
        //super(context);
        this.context = context;
        this.index = index;
        this.requestManager = new RestClient();
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
            FileUtils.mkdirs(context,dirStr);
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

    public void request(Object obj, String url, String dir, String filename) {
        //String dirStr = CommonUtils.getBaseDirectory(context);
        String fullname = dir + filename;
        FileUtils.mkdirs(context, dir);
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
                    cacheListener.onLoadFailed(currImgRequest.obj,"");
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



        if(linkedQueue.isEmpty()){
            currImgRequest = new FileRequest(obj, url, dir, filename);
            //this.requestManager.setFilename(fullname);
            //this.requestManager.request(url);

            RestClient.raw(APIService.class,(bytesRead, contentLength, percentage, done) -> {
                Handler mainHandler = new Handler(context.getMainLooper());

                mainHandler.post(() -> {
                    if(cacheListener!=null){
                        cacheListener.onProgress(currImgRequest.obj,Math.round(percentage));
                    }
                });

            }).get(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(o -> FileUtils.fileObservable(context,o,fullname))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        cacheListener.onLoaded(currImgRequest.obj, file, System.currentTimeMillis());
                        FileRequest imgRequest = linkedQueue.poll();

                        if(imgRequest!=null){
                            currImgRequest = imgRequest;
                            request(imgRequest.obj,imgRequest.url,imgRequest.dir, imgRequest.filename);
                        }else{
                            cacheListener.onAllLoadComplete();
                        }
                    },throwable -> cacheListener.onLoadFailed(currImgRequest.obj,throwable.getMessage()));
        }else{
            linkedQueue.add(new FileRequest(obj, url, dir, filename));
        }
        
        //Logger.log("this.requestManager.isFinish()=>"+this.requestManager.isFinish());
    }

    public void request(Object obj, String url) {
        String fileName = CommonUtils.extractFilenameFromImgUrl(url);
        request(obj,url,fileName);
    }


    public void request(Object obj, String url, String fileName) {
        String dirStr = CommonUtils.getBaseDirectory(context);
        request(obj,url,dirStr,fileName);
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

    public int queueSize(){
        return linkedQueue.size();
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


    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

}
