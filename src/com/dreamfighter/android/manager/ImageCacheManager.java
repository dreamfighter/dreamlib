package com.dreamfighter.android.manager;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dreamfighter.android.enums.DownloadInfo;
import com.dreamfighter.android.enums.RequestType;
import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.manager.RequestManager.RequestListeners;
import com.dreamfighter.android.utils.CommonUtils;

/**
 * used for caching image from server, manage it with time to life cache
 * the default time to life is one hour (3600 x 1000) millisecond
 * 
 * it used listener so set listener first if you want get the bitmap
 * 
 * @author fitra.adinugraha
 *
 */

public class ImageCacheManager implements RequestListeners{
    /*
     * this cache time to live is in milisecond
     * default set to one hour
     */
    private Context context;
    private long cacheTTL = 3600 * 1000;
    private int index = 0;
    private RequestManager requestManager;
    private ConcurrentLinkedQueue<ImageRequest> linkedQueue = new ConcurrentLinkedQueue<ImageRequest>();
    private ImageCacheListener imageCacheListener;
    private ImageRequest currImgRequest = null;
    private boolean limited = true;
    private boolean refresh = false;
    private Object currentDisplay = new Object();

    private class ImageRequest{
        public Object obj;
        public String url;
        public ImageRequest(Object obj,String url){
            this.obj = obj;
            this.url = url;
        }
    }
    
    /**
     * listener interface create implements from this interface to get callback from this class
     * @author fitra.adinugraha
     *
     */
    public interface ImageCacheListener{
        void onLoaded(Object obj, Bitmap bitmap, long lastUpdate);
        void onExpired(int index, long lastUpdate);
    }

    public ImageCacheManager(Context context) {
        //super(context);
        this.context = context;
        this.requestManager = new RequestManager(context);
        this.requestManager.setRequestType(RequestType.BITMAP);
        this.requestManager.setRequestListeners(this);
        initializeDirectory();
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
    
    /**
     * initialize directory in sdcard  
     */
    private void initializeDirectory(){
        String dirStr = CommonUtils.getBaseDirectory(context);
        if(dirStr!=null && !"".equals(dirStr)){
            File dir = new File(dirStr);
            if(!dir.exists()){
                dir.mkdirs();
            }
        }
    }
    
    /**
     * removing cache by its server url
     * @param url
     */
    public boolean removeCache(String url){
        return removeCacheByLocalUrl(CommonUtils.extractFilenameFromImgUrl(url));
    }
    
    /**
     * removing cache by its local url
     * @param url
     */
    public boolean removeCacheByLocalUrl(String localUrl){
        String dirStr = CommonUtils.getBaseDirectory(context);
        String fileName = dirStr + localUrl;
        if(fileName!=null && !"".equals(fileName)){
            File img = new File(fileName);
            if(img.exists()){
                return img.delete();
            }
        }
        return false;
    }
    
    /**
     * requesting image url with obj index for callback listener
     * @param obj
     * @param url
     */
    public void request(Object obj, String url) {
        String dirStr = CommonUtils.getBaseDirectory(context);
        String fileName = dirStr + CommonUtils.extractFilenameFromImgUrl(url);
        if(fileName!=null && !"".equals(fileName) && !isRefresh()){
            File img = new File(fileName);
            if(!isLimited()){
                Bitmap bitmap = BitmapFactory.decodeFile(fileName);
                if(imageCacheListener!=null){
                    imageCacheListener.onLoaded(obj, bitmap,img.lastModified());
                }
                return;
            }else if(img.exists() && img.lastModified() + cacheTTL > System.currentTimeMillis()){
                Bitmap bitmap = BitmapFactory.decodeFile(fileName);
                if(imageCacheListener!=null){
                    imageCacheListener.onLoaded(obj, bitmap,img.lastModified());
                }
                return;
            }else{
                Bitmap bitmap = BitmapFactory.decodeFile(fileName);
                if(imageCacheListener!=null){
                    imageCacheListener.onLoaded(obj, bitmap,img.lastModified());
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

    @Override
    public void onRequestProgress(DownloadInfo requestInfo, Long currentDownload) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onRequestComplete(RequestManager requestManager,
            Boolean success, Bitmap bitmap, String resultString,
            Object ressultRaw) {

        Logger.log("linkedQueue["+this.index+"].size()=>"+linkedQueue.size());
        if(imageCacheListener!=null && currImgRequest!=null && currImgRequest.obj==currentDisplay){
            imageCacheListener.onLoaded(currImgRequest.obj, bitmap, System.currentTimeMillis());
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
    
    public boolean isLimited() {
        return limited;
    }

    public void setLimited(boolean limited) {
        this.limited = limited;
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
