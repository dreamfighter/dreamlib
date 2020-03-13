package id.dreamfighter.android.manager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import id.dreamfighter.android.network.APIService;
import id.dreamfighter.android.network.RestClient;
import id.dreamfighter.android.utils.CommonUtils;
import id.dreamfighter.android.utils.FileUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by dreamfighter on 8/25/16.
 */
public class FileCache2Manager {
    private static FileCache2Manager instance;
    private static final int DOWNLOAD = 0;
    private static final int LOADED = 1;
    private static final int FAILED = 2;
    private static final int MAX_CONNECTION = 5;
    private int timeout = 3000;

    private Context context;
    private ConcurrentLinkedQueue<FileRequest> linkedQueue = new ConcurrentLinkedQueue<FileRequest>();
    private Map<String, Integer> state = new HashMap<String, Integer>();

    private Map<Object, Observable> fileCaches = new HashMap<>();
    private Map<Object,FileCacheManager.FileLoaderListener> cacheListener = new ConcurrentHashMap<Object,FileCacheManager.FileLoaderListener>();
    //private FileCacheManager.FileLoaderListener listener;

    public class FileRequest{
        public Object obj;
        public String url;
        public String dir;
        public String filename;
        public boolean refresh = false;
        public FileRequest(Object obj, String url, String dir, String filename, boolean refresh){
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
        FileUtils.mkdirs(context,dirStr);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public FileCache2Manager addListener(Object obj, FileCacheManager.FileLoaderListener listener){
        cacheListener.put(obj,listener);
        return this;
    }

    public FileCache2Manager removeListener(Object obj){
        cacheListener.remove(obj);
        return this;
    }

    public void reset(String fileName){
        state.put(fileName,FAILED);
    }

    public void request(int obj, String url, final String fileName, final boolean refresh) {
        Integer localState = state.get(fileName);
        String dirStr = CommonUtils.getBaseDirectory(context);
        final String fullName = dirStr + fileName;
        File file = FileUtils.file(context, fullName);

        if(!refresh) {

            if (localState!=null && localState == LOADED && file.exists()) {
                FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);

                cacheListener.remove(obj);
                if(listener!=null){

                    listener.onLoaded(obj,file,file.lastModified());
                }
                return;
            }
            if (localState!=null && localState == DOWNLOAD) {
                //IGNORE

                return;
            }
            if (file.exists()) {
                state.put(fileName, LOADED);
                FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);

                cacheListener.remove(obj);
                if(listener!=null){

                    listener.onLoaded(obj,file,file.lastModified());
                }
                return;
            }
        }
        Log.d("FileCache2Manager",""+fileCaches.size());
        if(fileCaches.size()<MAX_CONNECTION){

            state.put(fileName,DOWNLOAD);
            Observable<Response<ResponseBody>> obverable = RestClient.raw(APIService.class,(bytesRead, contentLength, percentage, done) -> {
                Handler mainHandler = new Handler(context.getMainLooper());

                mainHandler.post(() -> {
                    FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);
                    if(listener!=null){
                        //Log.d("PROGRESS",""+percentage);
                        listener.onProgress(obj,(long)percentage);
                    }
                });

            }).get(url);

            fileCaches.put(obj,obverable);

            obverable
                    .subscribeOn(Schedulers.io())
                    .flatMap(o -> FileUtils.fileObservable(context,o,fullName))
                    .observeOn(AndroidSchedulers.mainThread())
                    //.subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(f -> {
                        FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);
                        state.put(fileName,LOADED);

                        if(listener!=null){
                            listener.onLoaded(obj,f,new Date().getTime());
                        }
                        fileCaches.remove(obj);
                        cacheListener.remove(obj);
                        FileRequest fileRequest = linkedQueue.poll();
                        if(fileRequest!=null){
                            request((int)fileRequest.obj, fileRequest.url, fileRequest.filename,refresh);
                        }
            },throwable -> {
                        throwable.printStackTrace();
                FileCacheManager.FileLoaderListener listener = cacheListener.get(obj);
                if(listener!=null){
                    listener.onLoadFailed(obj,throwable.getMessage());
                }
                state.put(fileName,FAILED);
                fileCaches.remove(obj);
                cacheListener.remove(obj);
                FileRequest fileRequest = linkedQueue.poll();
                if(fileRequest!=null){
                    request((int)fileRequest.obj, fileRequest.url, fileRequest.filename,refresh);
                }
            });
        }else{
            linkedQueue.add(new FileRequest(obj,url,dirStr,fileName,refresh));
        }
    }
}
