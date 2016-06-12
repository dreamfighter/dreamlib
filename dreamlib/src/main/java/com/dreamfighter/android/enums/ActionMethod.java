package com.dreamfighter.android.enums;

public enum ActionMethod {
    GET("get"),
    POST("post");
    
    private String value = "";

    /**
     * Creates a new MergeAccountStatus object.
     * 
     * @param value
     *            DOCUMENT ME!
     */
    private ActionMethod(String value) {
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
