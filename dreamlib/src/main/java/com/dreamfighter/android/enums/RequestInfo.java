package com.dreamfighter.android.enums;

public enum RequestInfo {
	INFO_ON_PROGRESS("download_on_progress"),
	INFO_CONNECTION_LOST("connection_lost"), 
	INFO_ERROR("error"), 
	INFO_FAILED("failed"), 
	INFO_FILE_NOT_FOUND("file_not_found"), 
	INFO_URL_NOT_FOUND("url_not_found"), 
	INFO_DOWNLOADED_FILEPATH_NOTFOUND("downloaded_filepath_notfound"),
	INFO_PERMISSION_DENIED("permission_denied"),
	INFO_COMPLETE("complete");
	
	private String value = "";

	/**
	 * Creates a new MergeAccountStatus object.
	 * 
	 * @param value
	 *            DOCUMENT ME!
	 */
	private RequestInfo(String value) {
		this.value = value;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getValue() {
		return value;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param value
	 *            DOCUMENT ME!
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
