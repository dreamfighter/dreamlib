package id.dreamfighter.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import id.dreamfighter.android.R;

public class DreamTextView extends AppCompatTextView {

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
                    //setTypeface(CommonUtils.getFont(context, fontAssetsPath));
                    Typeface tf = Typeface.createFromAsset(getContext().getAssets(), fontAssetsPath);
                    setTypeface(tf);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                typedArray.recycle();
            }
        }
    }
}