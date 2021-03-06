package com.dreamfighter.android.enums;

public enum ResponseType {
	STRING("string"),
	BITMAP("bitmap"),
	CUSTOM("custom"),
	RAW("raw");
	
	private String value = "";

	/**
	 * Creates a new MergeAccountStatus object.
	 * 
	 * @param value
	 *            DOCUMENT ME!
	 */
	private ResponseType(String value) {
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
