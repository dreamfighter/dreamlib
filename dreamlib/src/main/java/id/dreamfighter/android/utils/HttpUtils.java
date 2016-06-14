package id.dreamfighter.android.utils;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.content.Context;

public class HttpUtils {
	public static HttpUtils instance = new HttpUtils();
	private CookieStore cookieStore = null;
	private PersistentCookieStore persistentCookieStore = null;
	private boolean useProxy = false;
	private boolean useAuthProxy = true;
	private String username = "u900187";
	private int port = 3128;
	private String password = "dreamasyst";
	private String hostname = "172.25.33.77";
	private String byPassProxy = "127.0.0.1,localhost";
	
	public CookieStore getCookieStore(Context context) {
		if(context==null){
			return cookieStore;
		}
		if(persistentCookieStore==null){
			persistentCookieStore = new PersistentCookieStore(context);
		}
		/*for(Cookie c :persistentCookieStore.getCookies()){
			Logger.log("cookieStore=>"+c.getName()+":"+c.getValue());
		}*/
		return persistentCookieStore;
	}
	
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public void setCookieStore(Context context, CookieStore cookieStore) {
		if(cookieStore instanceof PersistentCookieStore){
			persistentCookieStore = (PersistentCookieStore)cookieStore; 
		}else if(context!=null){
			//Logger.log("persistentCookieStore=>"+persistentCookieStore);
			if(persistentCookieStore==null){
				persistentCookieStore = new PersistentCookieStore(context);
			}
			//persistentCookieStore = (PersistentCookieStore) getCookieStore(context);
			for(Cookie c :cookieStore.getCookies()){
				//Logger.log("cookieStore=>"+c.getName());
				persistentCookieStore.addCookie(c);
			}
		}
		this.cookieStore = cookieStore;
	}
	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}
	public boolean isUseProxy() {
		return useProxy;
	}
	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}
	public boolean isUseAuthProxy() {
		return useAuthProxy;
	}
	public void setUseAuthProxy(boolean useAuthProxy) {
		this.useAuthProxy = useAuthProxy;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

    public String getByPassProxy() {
        return byPassProxy;
    }

    public void setByPassProxy(String byPassProxy) {
        this.byPassProxy = byPassProxy;
    }
    
    public void addByPassProxy(String byPassProxy) {
        this.byPassProxy += "," + byPassProxy;
    }

}
