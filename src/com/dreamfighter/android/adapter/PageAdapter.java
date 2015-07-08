package com.dreamfighter.android.adapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamfighter.android.R;
import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.manager.DownloadManager;
import com.dreamfighter.android.utils.Pagging;
import com.dreamfighter.android.webadapter.entity.BaseEntity;

public abstract class PageAdapter implements Runnable {
	protected Context context;
	protected ProgressDialog progressDialog;
	private ImageView imageView;
	private ListView listView;
	private View footerView;
	private List<BaseEntity> items;
	private DownloadManager downloadManager;
	private List<BaseEntity> lastLoadItems;
	private Pagging pagging;
	private String url;
	private boolean firstLoad = false;
	private boolean useImage = false;
	private ImagesAdapter imageAdapter;
	private Integer limit;
	private String pageTag;
	private String limitTag;
	private Handler handler;
	
	public PageAdapter(String basePath, ImageView imageView, String url){
		this.url				= url;
		this.imageView 			= imageView;
		initialize();
	}
	
	public PageAdapter(Context context,String basePath,ListView listView, String url, boolean noProxy){
		this.url				= url;
		this.listView			= listView;
		this.context			= context;
		initialize();
	}
	
	public PageAdapter(Context context, ListView listView, String url){
		Logger.log(this,"initialize!");
		this.url				= url;
		this.listView			= listView;
		this.context			= context;
		initialize();
	}
	
	public PageAdapter(Context context, ListView listView, String url, String pageTag, String limitTag){
		Logger.log(this,"initialize!");
		this.url				= url;
		this.listView			= listView;
		this.context			= context;
		this.limitTag			= limitTag;
		this.pageTag			= pageTag;
		initializeCustomTagPaging(pageTag,limitTag);
	}

	
	public PageAdapter(Context context, ListView listView, String url, String pageTag, String limitTag, int limit){
		Logger.log(this,"initialize!");
		this.url				= url;
		this.listView			= listView;
		this.context			= context;
		this.limit				= limit;
		this.limitTag			= limitTag;
		this.pageTag			= pageTag;
		//initializeCustomTagPaging(pageTag,limitTag);
	}
	
	public PageAdapter(Context context, ListView listView, String url, int limit){
		Logger.log(this,"initialize!");
		this.url				= url;
		this.listView			= listView;
		this.context			= context;
		this.limit				= limit;
		this.limitTag			= "limit";
		this.pageTag			= "index";
		//initializeCustomTagPaging(pageTag,limitTag);
	}
	
	public void showLoading(String comment){
		Logger.log("showLoading");
		progressDialog = new ProgressDialog(context);
		if("".equalsIgnoreCase(comment) || comment==null){
			progressDialog.setMessage("Loading. Please wait...");
		}else{
			progressDialog.setMessage(comment);
		}
		progressDialog.setIndeterminate(false);
		//progressDialog.setCancelable(true);
		progressDialog.show();
	}
	
	public void dismissLoading(){
		Logger.log("dismissLoading");
		if(progressDialog!=null){
			try{
				progressDialog.dismiss();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void initializeCustomTagPaging(String pageTag,String limitTag){
		//showLoading(null);
		Logger.log(this,"initialize!");
		handler = new Handler();
		if(this.items==null){
			this.items 				= new ArrayList<BaseEntity>();
		}else{
			this.items.clear();
		}
		if(this.lastLoadItems==null){
			this.lastLoadItems 		= new ArrayList<BaseEntity>();
		}else{
			this.lastLoadItems.clear();
		}
		//BaseEntity item 		= entity();
		this.downloadManager 	= new DownloadManager(context,this);
    	//item.setName("");
    	//this.items.add(item);
		this.pagging = new Pagging(url,pageTag,1);
		this.pagging.setNextIndex(true);
		this.pagging.setLimitTag(limitTag);
		if(limit != null){
			this.pagging.setLimit(limit);
		}
		Logger.log(this, "donwload video url => " + pagging.getUrl());
		this.downloadManager.download(pagging.getUrl());
    	
    	this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				setOnItemClickListener(adapterView, view, position,id);
				/*if(position == adapterView.getCount()-1){
					nextPage();
				}else{
					setOnItemClickListener(adapterView, view, position,id);
				}*/
			}
		});
	}
	
	public void initializePaging(){
		showLoading(null);

		handler = new Handler();
		Logger.log(this,"initialize!");
		if(this.items==null){
			this.items 			= new ArrayList<BaseEntity>();
		}else{
			this.items.clear();
		}
		if(this.lastLoadItems==null){
			this.lastLoadItems 	= new ArrayList<BaseEntity>();
		}else{
			this.lastLoadItems.clear();
		}
		if(listView!=null){
			addFooter();
			refreshAdapter();
		}
		Logger.log("lastLoadItems.size=>"+lastLoadItems.size());
		//BaseEntity item 		= entity();
		this.downloadManager 	= new DownloadManager(context,this);
    	//item.setName("");
    	//this.items.add(item);
		this.pagging = new Pagging(url,pageTag,0);
		this.pagging.setNextIndex(true);
		this.pagging.setLimitTag(limitTag);
		if(limit != null){
			this.pagging.setLimit(limit);
		}
		Logger.log(this, "donwload video url => " + pagging.getUrl());
		this.downloadManager.download(pagging.getUrl());
    	
    	this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				setOnItemClickListener(adapterView, view, position,id);
				/*if(position == adapterView.getCount()-1){
					nextPage();
				}else{
					setOnItemClickListener(adapterView, view, position,id);
				}*/
			}
		});
	}
	
	public void initialize(){
		Logger.log(this,"initialize!");

		handler = new Handler();
		this.items 				= new ArrayList<BaseEntity>();
		this.lastLoadItems 		= new ArrayList<BaseEntity>();
		//BaseEntity item 		= entity();
		this.downloadManager 	= new DownloadManager(this);
    	//item.setName("");
    	//this.items.add(item);
		this.pagging = new Pagging(url,"page",0);
		if(limit != null){
			this.pagging.setLimit(limit);
		}
		Logger.log(this, "donwload video url => " + pagging.getUrl());
		this.downloadManager.download(pagging.getUrl());
    	
    	this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				
				/*Logger.log("footerView==view=>"+(footerView==view));
				if(position == adapterView.getCount()-1 && footerView==view){
					nextPage();
				}else{
				}*/
				setOnItemClickListener(adapterView, view, position,id);
			}
		});
	}
	
	public void initialize(String url){

		handler = new Handler();
		this.items 				= new ArrayList<BaseEntity>();
		this.lastLoadItems 		= new ArrayList<BaseEntity>();
		BaseEntity item 		= entity();
		this.downloadManager 	= new DownloadManager(context,this);
    	this.url				= url;
    	this.items.add(item);
		this.pagging.setLast(false);
		this.downloadManager.download(pagging.getUrl());
	}
	
	public void nextPage(){

		handler = new Handler();
		if(!downloadManager.isRunning()){
			if(pagging.next()){
				Toast.makeText(context, "No more page!", Toast.LENGTH_SHORT).show();
				Logger.log(this,"no more page");
			}else{
				this.downloadManager = new DownloadManager(context,this);
				this.downloadManager.download(pagging.getUrl());
			}
		}else{
			Toast.makeText(context, "Loading Please Wait!", Toast.LENGTH_SHORT).show();
		}
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}
	
	public abstract BaseAdapter getAdapter(Context context, List<BaseEntity> items);
	
	public abstract List<BaseEntity> decode(InputStream result);
	
	public abstract List<BaseEntity> decode(String result);
	
	public abstract void setOnItemClickListener(AdapterView<?> adapterView, View view, int position, long id);
	
	public abstract BaseEntity entity();

	public void run() {
		Logger.log(this,"last page =>" + pagging.isLast());
		
		if(downloadManager.isSuccess()){
			dismissLoading();
			if(!pagging.isLast()){
				if(this.downloadManager.getResultString()!=null){
					List<BaseEntity> tempItems = decode(this.downloadManager.getResultString());
					Logger.log(this,"tempItems=>" + tempItems.size());
					Logger.log(this,"lastLoadItems=>" + lastLoadItems.size());
					if(lastLoadItems.size()>0){
						Logger.log(this,"last load items name=>" + lastLoadItems.get(0).getName());
						if(tempItems.size()>0 && lastLoadItems.get(0).getId()==tempItems.get(0).getId()){
							
							pagging.setLast(true);
							Toast.makeText(this.context, "No more page!", Toast.LENGTH_SHORT).show();
							Logger.log(this,"no more page");
							//listView.removeFooterView(footerView);
							footerView.setVisibility(View.GONE);
							/*if(lastLoadItems.get(0).getId()==tempItems.get(0).getId()){
								pagging.setLast(true);
								Toast.makeText(this.context, "No more page!", Toast.LENGTH_SHORT).show();
								Logger.log(this,"no more page");
							}*/
						}else if(tempItems.isEmpty()){
							pagging.setLast(true);
							Toast.makeText(this.context, "No more page!", Toast.LENGTH_SHORT).show();
							Logger.log(this,"no more page");
							//listView.removeFooterView(footerView);
							footerView.setVisibility(View.GONE);
						}
						
						/*else{
							Logger.log("listView.getFooterViewsCount()=>"+listView.getFooterViewsCount());
							if(listView.getFooterViewsCount()==0){
								if(footerView==null){
									footerView = LayoutInflater.from(context).inflate(
											R.layout.footer_view, null, false);
									View loadmore = footerView.findViewById(R.id.text_footer);
									((TextView) loadmore).setText(R.string.df_load_more);
									View progressbar = footerView.findViewById(R.id.progressBar1);
									
									((ProgressBar) progressbar).setVisibility(View.GONE);
								}
								listView.addFooterView(footerView);
							}
							pagging.setLast(true);
							Toast.makeText(this.context, "No more page!", Toast.LENGTH_SHORT).show();
							Logger.log(this,"no more page");
						}*/
					}
					
					
					
				
					if(!pagging.isLast()){
						lastLoadItems.addAll(tempItems);
						
						int idx = listView.getFirstVisiblePosition();
						View vfirst = listView.getChildAt(0);
						int pos = 0;
						if (vfirst != null) {
							pos = vfirst.getTop();
						}
						
						int firstLoadImage	= this.items.size();
						/*this.items.addAll(this.items.size()-1, tempItems);*/
						
						this.items.addAll(tempItems);
						
						/*if(listView.getAdapter() instanceof HeaderViewListAdapter){
							BaseAdapter adapter = (BaseAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
							if(adapter==null){
								listView.setAdapter(new HeaderViewListAdapter(null, null, getAdapter(this.context, items)));
								//listView.setAdapter(getAdapter(this.context, items));
							}else{
								adapter.notifyDataSetChanged();
							}
						}else{
							BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
							if(adapter==null){
								listView.setAdapter(getAdapter(this.context, items));
							}else{
								adapter.notifyDataSetChanged();
							}
						}*/
						
						refreshAdapter();

						//Restore the position
						listView.setSelectionFromTop(idx, pos);
						//listView.setSelection(firstLoadImage);
						//Logger.log(this,"getScrollY()" + listView.getScrollY());
						if(imageAdapter!=null && !items.isEmpty()){
							imageAdapter.downloadImages(this.items, firstLoadImage);
						}
					}
				}
			}
		}else{
			dismissLoading();
			Toast.makeText(this.context, "Load Failed tyr again!", Toast.LENGTH_LONG).show();
			Logger.log(this,"Load Failed tyr again!");
			pagging.prev();
		}
	}
	
	public void refreshAdapter(){
		if(listView.getAdapter() instanceof HeaderViewListAdapter){
			BaseAdapter adapter = (BaseAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
			if(adapter==null){
				listView.setAdapter(new HeaderViewListAdapter(null, null, getAdapter(this.context, items)));
				//listView.setAdapter(getAdapter(this.context, items));
			}else{
				adapter.notifyDataSetChanged();
			}
		}else{
			BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
			if(adapter==null){
				listView.setAdapter(getAdapter(this.context, items));
			}else{
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	public void addFooter(){
		if(listView.getFooterViewsCount()==0){
			if(footerView==null){
				footerView = LayoutInflater.from(context).inflate(
						R.layout.df_footer_view, null, false);
				View loadmore = footerView.findViewById(R.id.text_footer);
				((TextView) loadmore).setText(R.string.df_load_more);
				View progressbar = footerView.findViewById(R.id.progressBar1);
				
				((ProgressBar) progressbar).setVisibility(View.GONE);
				footerView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						nextPage();
					}
				});
			}
			listView.addFooterView(footerView);
		}
	}
	
	
	public DownloadManager getPage() {
		return downloadManager;
	}
	public void setPage(DownloadManager page) {
		this.downloadManager = page;
	}

	public Pagging getPagging() {
		return pagging;
	}

	public void setPagging(Pagging pagging) {
		this.pagging = pagging;
	}

	public boolean isFirstLoad() {
		return firstLoad;
	}

	public void setFirstLoad(boolean firstLoad) {
		this.firstLoad = firstLoad;
	}

	public boolean isUseImage() {
		return useImage;
	}

	public void setUseImage(boolean useImage) {
		this.useImage = useImage;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public ImagesAdapter getImageAdapter() {
		return imageAdapter;
	}

	public void setImageAdapter(ImagesAdapter imageAdapter) {
		this.imageAdapter = imageAdapter;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public List<BaseEntity> getItems() {
		return items;
	}

	public void setItems(List<BaseEntity> items) {
		this.items = items;
	}

	public String getPageTag() {
		return pageTag;
	}

	public void setPageTag(String pageTag) {
		this.pageTag = pageTag;
	}

	public String getLimitTag() {
		return limitTag;
	}

	public void setLimitTag(String limitTag) {
		this.limitTag = limitTag;
	}
}
