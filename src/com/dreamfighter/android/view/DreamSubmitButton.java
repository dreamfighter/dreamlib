package com.dreamfighter.android.view;

import com.dreamfighter.android.R;
import com.dreamfighter.android.utils.CommonUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class DreamSubmitButton extends LinearLayout{

    public DreamSubmitButton(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public DreamSubmitButton(Context context,AttributeSet attrs) {
        super(context,attrs);
    }
    
    private void initLayout(Context context, AttributeSet attrs,int defStyle){
        if(attrs!=null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.dreamlib, defStyle, 0);
            try{
                String fontAssetsPath = typedArray.getString(R.styleable.dreamlib_customFontFace);
                
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                typedArray.recycle();
            }
        }
    }

}
