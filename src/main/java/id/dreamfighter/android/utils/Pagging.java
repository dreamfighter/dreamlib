package id.dreamfighter.android.utils;

import id.dreamfighter.android.log.Logger;

public class Pagging {
	private Integer page = 0;
	private String url;
	private String tag;
	private boolean last = false;
	private final String SEPARATOR_TAG = "&";
	private final String EQUAL_TAG = "=";
	private String LIMIT_TAG = "limit";
	private Integer limit = 10;
	private boolean nextIndex = false;
	
	public Pagging(String url){
		this.url=url;
	}
	
	public Pagging(String url, String tag, Integer page){
		this.url = url;
		this.page = page;
		this.tag = tag;
	}
	
	public boolean next(){
		if(!last){
			if(isNextIndex()){
				this.page += limit;
			}else{
				this.page++;
			}
		}
		Logger.log(this, "pagging next => "+page);
		return last;
	}
	
	public boolean nextIndex(){
		if(!last){
				
			this.page += limit;
		}
		Logger.log(this, "pagging next => "+page);
		return last;
	}
	
	public void prev(){
		if(page>1){
			if(isNextIndex()){
				this.page -= limit;
			}else{
				this.page--;
			}
		}
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public String getUrl() {
		return url + SEPARATOR_TAG + tag + EQUAL_TAG + page + SEPARATOR_TAG + LIMIT_TAG + EQUAL_TAG + limit;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setLimitTag(String tag) {
		LIMIT_TAG = tag;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public boolean isNextIndex() {
		return nextIndex;
	}

	public void setNextIndex(boolean nextIndex) {
		this.nextIndex = nextIndex;
	}
}
