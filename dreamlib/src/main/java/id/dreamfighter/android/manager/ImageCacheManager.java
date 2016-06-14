package id.dreamfighter.android.manager;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import id.dreamfighter.android.enums.DownloadInfo;
import id.dreamfighter.android.enums.RequestType;
import id.dreamfighter.android.log.Logger;
import id.dreamfighter.android.manager.RequestManager.RequestListeners;
import id.dreamfighter.android.utils.CommonUtils;
import id.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

/**
 * this cache time to live is in milisecond
 * default set to one hour
 */
public class ImageCacheManager implements RequestListeners{
    
    private static ImageCacheManager instance;
    private Context context;
    private long cacheTTL = 3600 * 1000;
    private int index = 0;
    private RequestManager requestManager;
    private ConcurrentLinkedQueue<ImageRequest> linkedQueue = new ConcurrentLinkedQueue<ImageRequest>();
    private ImageCacheListener imageCacheListener;
    private ImageCacheListener cacheListener;
    private ImageRequest currImgRequest = null;
    private boolean limitless = false;
    private boolean refresh = false;
    private Object currentDisplay = new Object();
    private int tryingCounter = 0;

    private class ImageRequest{
        public Object obj;
        public String url;
        public ImageRequest(Object obj,String url){
            this.obj = obj;
            this.url = url;
        }
    }
    
    public interface ImageCacheListener{
        void onLoaded(Object obj, Bitmap bitmap, long lastUpdate);
        void onExpired(int index, long lastUpdate);
        void onLoadImageFailed();
        void onImageProgress(long currentLoaded);
        void onConnectionError();
    }
    
    public interface CacheListener{
        void onLoaded(Object obj, String fileName, long lastUpdate);
    }
    
    public static abstract class ImageLoaderListener implements ImageCacheListener{
        public void onLoaded(Object obj, Bitmap bitmap, long lastUpdate){}
        public void onExpired(int index, long lastUpdate){}
        public void onLoadImageFailed(){}
        public void onImageProgress(long currentLoaded){}
        public void onConnectionError(){}
    }

    public ImageCacheManager(Context context) {
        //super(context);
        this.context = context;
        this.requestManager = new RequestManager(context);
        this.requestManager.setRequestType(RequestType.BITMAP);
        this.requestManager.setRequestListeners(this);
        initializeDirectory();
    }
    
    public static ImageCacheManager getInstance(Context context){
        if(instance==null){
            instance = new ImageCacheManager(context);
        }
        return instance;
    }

    public ImageCacheManager(Context context,int index) {
        //super(context);
        this.context = context;
        this.index = index;
        this.requestManager = new RequestManager(context);
        this.requestManager.setRequestType(RequestType.BITMAP);
        this.requestManager.setRequestListeners(this);
        initializeDirectory();
    }

    public ImageCacheManager(int cacheTTL, Context context) {
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

    public void request(Object obj, String url,String filename) {
        String dirStr = CommonUtils.getBaseDirectory(context);
        String fileName = dirStr + filename;
        if(fileName!=null && !"".equals(fileName)){
            File img = new File(fileName);
            if(img.exists()){
                Bitmap bitmap = null;
                try{
                    bitmap = BitmapFactory.decodeFile(fileName);
                }catch(OutOfMemoryError e){
                    e.printStackTrace();
                    System.gc();
                    tryingCounter++;
                    if(tryingCounter<3){
                        request(obj, url);
                        return;
                    }
                    imageCacheListener.onLoadImageFailed();
                    tryingCounter = 0;
                    return;
                }

                if(imageCacheListener!=null && bitmap!=null){
                    imageCacheListener.onLoaded(obj, bitmap,img.lastModified());
                }else if(!refresh && imageCacheListener!=null){
                    imageCacheListener.onLoadImageFailed();
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
        
        Logger.log("this.requestManager.isRunning()=>"+this.requestManager.isRunning());
        if(!this.requestManager.isRunning()){
            currImgRequest = new ImageRequest(obj, url);
            this.requestManager.setFilename(fileName);
            this.requestManager.request(url);
        }else{
            linkedQueue.add(new ImageRequest(obj, url));
        }
    }

    public void request(Object obj, String url) {
        String fileName = CommonUtils.extractFilenameFromImgUrl(url);
        request(obj,url,fileName);
    }

    @Override
    public void onRequestProgress(DownloadInfo requestInfo, Long currentDownload) {
        // TODO Auto-generated method stub
        if(imageCacheListener!=null){
            imageCacheListener.onImageProgress((long)(100.0 * currentDownload / requestManager.getFilesize()));
        }
    }

    @Override
    public void onRequestComplete(RequestManager requestManager,
            Boolean success, Bitmap bitmap, String resultString,
            Object ressultRaw) {
        if (!success) {
            if (imageCacheListener!=null) {
                imageCacheListener.onConnectionError();
            }
            return;
        }
        Logger.log("linkedQueue["+this.index+"].size()=>"+linkedQueue.size());
        if(imageCacheListener!=null && currImgRequest!=null && currImgRequest.obj==currentDisplay){
            if(bitmap!=null){
                imageCacheListener.onLoaded(currImgRequest.obj, bitmap, System.currentTimeMillis());
            }else{
                imageCacheListener.onLoadImageFailed();
            }
        }
        
        ImageRequest imgRequest = linkedQueue.poll();
        //currImgRequest = linkedQueue.peek();
        if(imgRequest!=null){
            request(imgRequest.obj,imgRequest.url);
        }
    }

    public ImageCacheListener getImageCacheListener() {
        return imageCacheListener;
    }

    public void setImageCacheListener(ImageCacheListener imageCacheListener) {
        this.imageCacheListener = imageCacheListener;
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
