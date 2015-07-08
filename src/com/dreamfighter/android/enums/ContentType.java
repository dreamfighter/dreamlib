package com.dreamfighter.android.enums;

public enum ContentType {
    APPLICATION_JSON("application/json"),
    APPLICATION_HTML("text/html");
    
    private String value = "";

    /**
     * Creates a new MergeAccountStatus object.
     * 
     * @param value
     *            DOCUMENT ME!
     */
    private ContentType(String value) {
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