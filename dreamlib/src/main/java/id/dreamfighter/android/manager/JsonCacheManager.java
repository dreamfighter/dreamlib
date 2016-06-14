package id.dreamfighter.android.manager;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import id.dreamfighter.android.log.Logger;
import id.dreamfighter.android.manager.listeners.ConnectionListener;
import id.dreamfighter.android.manager.listeners.JsonCacheManagerListener;
import id.dreamfighter.android.utils.FileUtils;
import id.dreamfighter.android.utils.JsonUtils;

public class JsonCacheManager {
    private String directory;
    private long cacheTTL = 3600 * 1000;
    private boolean unlimited = false;
    private boolean enableCache = true;
    private boolean refreshImmediately = false;
    protected String fileName;
    private ConnectionManager connectionManager;
    private Class<?> classDefinition;
    private JsonCacheManagerListener jsonCacheManagerListener;

    public JsonCacheManager(Context context, Class<?> classDefinition) {
        this.directory = context.getApplicationInfo().dataDir + File.separator;
        this.fileName = classDefinition.getSimpleName();
        this.classDefinition = classDefinition;
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }public JsonCacheManager(Context context,String fileName) {
        this.directory = context.getApplicationInfo().dataDir + File.separator;
        this.fileName = fileName;
        
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public JSONObject loadJSONObjectFromCache() {
        
        try {
            File file = new File(directory + fileName);
            if (file.exists() && enableCache) {
                String json = FileUtils.readFileToString(directory + fileName);
                
                
                return new JSONObject(json);
                
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
        
    }

    public void loadJSONObject() {
        String message = null;
        try {
            File file = new File(directory + fileName);
            if (file.exists() && enableCache) {
                String json = FileUtils.readFileToString(directory + fileName);
                
                if (jsonCacheManagerListener != null && json != null && !"".equals(json)) {
                    JSONObject jsonObject = new JSONObject(json);
                    jsonCacheManagerListener.onLoaded(jsonObject);
                }
                if (!refreshImmediately && (file.lastModified() + cacheTTL > System.currentTimeMillis() || unlimited)) {
                    return;
                }
            }
            getFromServer();

        } catch (IOException e) {
            e.printStackTrace();
            message = e.getMessage();
            getFromServer();
        } catch (JSONException e) {
            e.printStackTrace();
            message = e.getMessage();
            getFromServer();
        }

        if (message != null && jsonCacheManagerListener != null) {
            jsonCacheManagerListener.onFailed(message);
        }
    }

    public void loadJSONString() {
        String message = null;
        try {
            File file = new File(directory + fileName);
            if (file.exists() && enableCache) {
                String json = FileUtils.readFileToString(directory + fileName);
                if (jsonCacheManagerListener != null && json != null
                        && !"".equals(json)) {
                    jsonCacheManagerListener.onLoaded(json);
                }
                if (!refreshImmediately && (file.lastModified() + cacheTTL > System.currentTimeMillis()
                        || unlimited)) {
                    return;
                }
            }
            getFromServer();

        } catch (IOException e) {
            getFromServer();
            message = e.getMessage();
            e.printStackTrace();
        }

        if (message != null && jsonCacheManagerListener != null) {
            jsonCacheManagerListener.onFailed(message);
        }
    }

    public void loadJSONArray() {
        String message = null;
        try {
            File file = new File(directory + fileName);
            if (file.exists() && enableCache) {
                String json = FileUtils.readFileToString(directory + fileName);
                if (jsonCacheManagerListener != null && json != null
                        && !"".equals(json)) {
                    JSONArray jsonArray = new JSONArray(json);
                    jsonCacheManagerListener.onLoaded(jsonArray);
                }
                if (!refreshImmediately && (file.lastModified() + cacheTTL > System.currentTimeMillis()
                        || unlimited)) {
                    return;
                }
            }
            getFromServer();
        } catch (IOException e) {
            getFromServer();
            message = e.getMessage();
            e.printStackTrace();
        } catch (JSONException e) {
            getFromServer();
            message = e.getMessage();
            e.printStackTrace();
        }

        if (message != null && jsonCacheManagerListener != null) {
            jsonCacheManagerListener.onFailed(message);
        }
    }

    public void loadObject() {

        String message = null;
        if (classDefinition != null) {
            try {
                File file = new File(directory + fileName);
                if (file.exists() && enableCache) {

                    String json = FileUtils.readFileToString(directory
                            + fileName);
                    if (jsonCacheManagerListener != null && json != null
                            && !"".equals(json)) {
                        
                        JSONObject jsonObject = new JSONObject(json);
                        jsonCacheManagerListener
                                .onLoadedObject(JsonUtils.jsonToClassMapping(
                                        jsonObject, classDefinition));
                    }
                    if (!refreshImmediately && (file.lastModified() + cacheTTL > System
                            .currentTimeMillis())) {
                        return;
                    }
                }
                getFromServer();
            } catch (InstantiationException e) {
                getFromServer();
                message = e.getMessage();
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                getFromServer();
                message = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                getFromServer();
                message = e.getMessage();
                e.printStackTrace();
            } catch (JSONException e) {
                getFromServer();
                message = e.getMessage();
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                getFromServer();
                message = e.getMessage();
                e.printStackTrace();
            }
        }else{
            message = "class definition cannot be null";
        }

        if (message != null && jsonCacheManagerListener != null) {
            jsonCacheManagerListener.onFailed(message);
        }
    }

    public void getFromServer() {
        connectionManager.setConnectionListener(new ConnectionListener() {
            @Override
            public void onRequestComplete(ConnectionManager connectionManager,
                    int requestCode, String resultString) {
                String message = null;
                try {
                    Logger.log(resultString);
                    try {
                        if (jsonCacheManagerListener != null
                                && resultString != null
                                && !"".equals(resultString)) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(resultString);
                                if(classDefinition!=null){
                                jsonCacheManagerListener
                                        .onLoadedObject(JsonUtils
                                                .jsonToClassMapping(jsonObject,
                                                        classDefinition));
                                }

                                if(jsonCacheManagerListener==null || (jsonCacheManagerListener!=null && 
                                        jsonCacheManagerListener.validateJson(jsonObject))){
                                    FileUtils.writeStringToFile(resultString, directory + fileName);
                                }
                                jsonCacheManagerListener.onLoaded(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            jsonCacheManagerListener.onLoaded(resultString);
                        }
                    } catch (InstantiationException e) {
                        getFromServer();
                        message = e.getMessage();
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        getFromServer();
                        message = e.getMessage();
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        getFromServer();
                        message = e.getMessage();
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    message = e.getMessage();
                    e.printStackTrace();
                }
                if (message != null && jsonCacheManagerListener != null) {
                    jsonCacheManagerListener.onFailed(message);
                }
            }
        });
        connectionManager.request();
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public JsonCacheManagerListener getJsonCacheManagerListener() {
        return jsonCacheManagerListener;
    }

    public void setJsonCacheManagerListener(
            JsonCacheManagerListener jsonCacheManagerListener) {
        this.jsonCacheManagerListener = jsonCacheManagerListener;
    }
    public long getCacheTTL() {
        return cacheTTL;
    }
    public void setCacheTTL(long cacheTTL) {
        this.cacheTTL = cacheTTL;
    }
    public boolean isEnableCache() {
        return enableCache;
    }
    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }
    public boolean isRefreshImmediately() {
        return refreshImmediately;
    }
    public void setRefreshImmediately(boolean refreshImmediately) {
        this.refreshImmediately = refreshImmediately;
    }

}
