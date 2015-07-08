package com.dreamfighter.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.dreamfighter.android.R;

public class DreamEditText extends EditText{

	public DreamEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initFont(context, attrs, defStyle);
	}

	public DreamEditText(Context context) {
		super(context);
		initFont(context, null, 0);
	}

	public DreamEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFont(context, attrs, 0);
	}
	
	private void initFont(Context context, AttributeSet attrs,int defStyle){
		if(attrs!=null){
			TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.dreamlib, defStyle, 0);
			try{
				String fontAssetsPath = typedArray.getString(R.styleable.dreamlib_customFontFace);
				if(fontAssetsPath!=null){
					setTypeface(Typeface.createFromAsset(context.getAssets(), fontAssetsPath));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				typedArray.recycle();
		    }
		}
	}
}