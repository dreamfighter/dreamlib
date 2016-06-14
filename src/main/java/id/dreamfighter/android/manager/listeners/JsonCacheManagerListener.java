package id.dreamfighter.android.manager.listeners;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class JsonCacheManagerListener {
    public void onLoadedObject(Object object){}
    public void onLoaded(JSONObject jsonObject){}
    public void onLoaded(JSONArray jsonArray){}
    public void onLoaded(String json){}
    public void onFailed(String messge){}
    
    public boolean validateJson(JSONObject jsonObject){
        return true;
    }
}
