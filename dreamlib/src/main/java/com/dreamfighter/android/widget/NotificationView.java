package com.dreamfighter.android.widget;

import com.dreamfighter.android.R;
import com.dreamfighter.android.log.Logger;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationView extends LinearLayoutCompat{
	private TextView textView;
	private ImageView imageView;
	//private android.widget.ImageView ImageView;

	public NotificationView(Context context) {
		this(context, null);
	}
	
	public NotificationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            throw new IllegalStateException("NotificationView is API 8+ only.");
        }
		
		LayoutInflater inflater = (LayoutInflater) context
	                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.df_notification_layout, this, true);
	    textView = (TextView) findViewById(R.id.df_notif_text);
	    imageView = (ImageView) findViewById(R.id.df_notif_icon);
	    
	    int[] imageIds = {android.R.attr.icon};
	    
	    TypedArray a = context.obtainStyledAttributes(attrs,imageIds);
	    int iconId = a.getResourceId(0, -1);
	    Logger.log("iconId=>"+iconId);
	    if(iconId!=-1){
		    imageView.setImageResource(iconId);
	    }
	    a.recycle();
	}
	
	public void setText(String text){
		textView.setText(text);
	}
	
	public void setImageResource(int resId){
		if(resId!=0){
		    imageView.setImageResource(resId);
	    }
	}

}
