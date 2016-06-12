package com.dreamfighter.android.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


public class IDRNumberFormater extends DecimalFormat{
    /**
     * 
     */
    private static final long serialVersionUID = -4968066986930170024L;

    private static DecimalFormat instance;
    
    //private static DecimalFormat indonesiaCurrency = (DecimalFormat) DecimalFormat.getCurrencyInstance();
    private static DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
    
    public static DecimalFormat getNoSymbolInstance(){
        if (instance==null) {
            instance = (DecimalFormat)IDRNumberFormater.getCurrencyInstance();
        }
        formatRp.setCurrencySymbol("");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        instance.setDecimalFormatSymbols(formatRp);
        return instance;
    }
    
    public static DecimalFormat getIDRInstance(){
        if (instance==null) {
            instance = (DecimalFormat)IDRNumberFormater.getCurrencyInstance();
        }
        formatRp.setCurrencySymbol("IDR ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        instance.setDecimalFormatSymbols(formatRp);
        return instance;
    }
    
}
