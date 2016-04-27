package id.dreamfighter.android.enums;

public enum PayloadType {
    RAW("raw"),
    FORM("form");
    
    private String value = "";

    /**
     * Creates a new MergeAccountStatus object.
     * 
     * @param value
     *            DOCUMENT ME!
     */
    private PayloadType(String value) {
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