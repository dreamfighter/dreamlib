package com.dreamfighter.android.manager;

import java.util.List;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.graphics.Bitmap;

import com.dreamfighter.android.enums.DownloadInfo;
import com.dreamfighter.android.webadapter.entity.BaseEntity;

public abstract class LoginManager extends DownloadManager{
	private LoginListeners loginListeners;
	
	public LoginManager(Context context){
		super(context);
	}
	
	public interface LoginListeners{
		public void onLoginSuccess(BaseEntity entity);
		public void onLoginFailed(BaseEntity entity);
	}

	public LoginListeners getLoginListeners() {
		return loginListeners;
	}

	public void setLoginListeners(LoginListeners loginListeners) {
		this.loginListeners = loginListeners;
	}
	
	public void login(String url,List<NameValuePair> postParams){
		if(isPost()){
			setPostParams(postParams);
		}
		setDownloadListeners(new DownloadManager.DownloadListeners() {
			
			@Override
			public void onDownloadProgress(DownloadInfo downloadInfo,
					Long currentDownload) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDownloadComplete(DownloadManager downloadManager,
					Boolean success, Bitmap bitmap, String resultString,
					Object ressultRaw) {
				if(success){
					BaseEntity entity = decodeLoginSucess(resultString);
					if(entity!=null){
						onLoginSuccess(entity);
						if(loginListeners!=null){
							loginListeners.onLoginSuccess(entity);
						}
					}else {
						if(loginListeners!=null){
							loginListeners.onLoginFailed(entity);
						}
						onLoginFailed(entity);
					}
				}else{
					if(loginListeners!=null){
						loginListeners.onLoginFailed(null);
					}
					onLoginFailed(null);
				}
			}
		});
		download(url);
	}
	
	public abstract BaseEntity decodeLoginSucess(String json);
	public abstract void onLoginSuccess(BaseEntity entity);
	public abstract void onLoginFailed(BaseEntity entity);
}
