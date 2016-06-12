package com.dreamfighter.android.adapter;

import java.util.List;

import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.manager.DownloadManager;
import com.dreamfighter.android.webadapter.entity.BaseEntity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.widget.ListView;

public class ImagesAdapter implements Runnable{
	private List<BaseEntity> items;
	private Integer increment;
	private ListView listView;
	private DownloadManager donwloader;
	private Context context;
	private int defaultImageId;
	private Boolean connection = false;
	
	public ImagesAdapter(Context context, ListView listView,List<BaseEntity> items,Integer increment){
		checkInternetConnection();
		this.increment		= increment;
		this.items			= items;
		this.listView		= listView;
		this.context		= context;
		this.donwloader 	= new DownloadManager(context,items.get(increment).getImageUrl(), this);
		this.donwloader.setConnection(connection);
		this.donwloader.setType(DownloadManager.TYPE_BITMAP);
		this.donwloader.download();
	}
	
	public ImagesAdapter(Context context, int defaultImageId, ListView listView){
		this.listView		= listView;
		this.context		= context;
		this.defaultImageId	= defaultImageId;
	}
	
	public void downloadImages(List<BaseEntity> items,Integer increment){
		checkInternetConnection();
		if(increment<0){
			increment = 0;
		}
		this.increment		= increment;
		this.items			= items;
		this.donwloader 	= new DownloadManager(context,items.get(increment).getImageUrl(), this);
		this.donwloader.setConnection(connection);
		this.donwloader.setType(DownloadManager.TYPE_BITMAP);
		this.donwloader.download();
	}
	
	protected boolean checkInternetConnection() {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    // test for connection
	    if (cm.getActiveNetworkInfo() != null){
	    	if(cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
	    		connection = true;
	    		return true;
		    } else {
		    	connection = false;
		    	return false;
		    }
	    }else{
	    	connection = false;
	    	return false;
	    }
	}
	
	public void run() {
		if(increment < items.size()){
			if(this.donwloader.getBitmap()!=null){
				items.get(increment).setImage(this.donwloader.getBitmap());
			}else{
				Bitmap image = BitmapFactory.decodeResource(context.getResources(), defaultImageId);
				items.get(increment).setImage(image);
			}
		}
		this.increment++;
		Logger.log(this, "image loaded");
		this.listView.invalidateViews();
		if(increment < items.size()){
			this.donwloader = new DownloadManager(context, items.get(increment).getImageUrl(), this);
			this.donwloader.setType(DownloadManager.TYPE_BITMAP);
			this.donwloader.download();
		}
	}

	public DownloadManager getPage() {
		return donwloader;
	}

	public void setPage(DownloadManager page) {
		this.donwloader = page;
	}

	public int getDefaultImageId() {
		return defaultImageId;
	}

	public void setDefaultImageId(int defaultImageId) {
		this.defaultImageId = defaultImageId;
	}
}
