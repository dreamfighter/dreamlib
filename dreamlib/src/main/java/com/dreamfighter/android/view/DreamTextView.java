package com.dreamfighter.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.dreamfighter.android.R;
import com.dreamfighter.android.utils.CommonUtils;

/**
 * create custom text view 
 * @author zeger
 *
 */
public class DreamTextView extends TextView{

	public DreamTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initFont(context, attrs, defStyle);
	}

	public DreamTextView(Context context) {
		super(context);
		initFont(context, null, 0);
	}

	public DreamTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFont(context, attrs, 0);
	}
	
	private void initFont(Context context, AttributeSet attrs,int defStyle){
		if(attrs!=null){
			TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.dreamlib, defStyle, 0);
			try{
				String fontAssetsPath = typedArray.getString(R.styleable.dreamlib_customFontFace);
				if(fontAssetsPath!=null){
					setTypeface(CommonUtils.getFont(context, fontAssetsPath));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				typedArray.recycle();
		    }
		}
	}
	

}
