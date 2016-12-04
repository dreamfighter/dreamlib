package com.dreamfighter.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.dreamfighter.android.R;
import com.dreamfighter.android.utils.CommonUtils;

/**
 * Created by dreamfighter on 12/3/16.
 */
public class VersioningView extends TextView{
    public VersioningView(Context context) {
        super(context);
        setText(context.getString(R.string.df_version, CommonUtils.getAppVersionName(context)));
    }

    public VersioningView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText(context.getString(R.string.df_version, CommonUtils.getAppVersionName(context)));
    }

    public VersioningView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setText(context.getString(R.string.df_version, CommonUtils.getAppVersionName(context)));
    }

}
