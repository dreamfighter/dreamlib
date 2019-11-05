package id.dreamfighter.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ClipDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import id.dreamfighter.android.R;


public class DreamProgressBar extends RelativeLayout {
    private ImageView progressDrawableImageView;
    private ImageView trackDrawableImageView;
    private int max = 100;
    
    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

	public DreamProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                           Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.df_round_progress, this);
        setup(context, attrs);
	}

    protected void setup(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs,
            R.styleable.dreamlib);

        //final String xmlns = "http://schemas.android.com/apk/res/co.id.asyst.android.smile";
        int bgResource = a.getResourceId(R.styleable.dreamlib_progressDrawable, R.drawable.df_red_progress_bar_fill);
        //int bgResource = attrs.get.getAttributeResourceValue(xmlns, "progressDrawable", 0);
        progressDrawableImageView = (ImageView) findViewById(
                R.id.df_progress_drawable_image_view);
        progressDrawableImageView.setBackgroundResource(bgResource);

        //int trackResource = attrs.getAttributeResourceValue(xmlns, "track", 0);
        int trackResource = a.getResourceId(R.styleable.dreamlib_customTrack, R.drawable.df_progress_bar_fill_bg);
        trackDrawableImageView = (ImageView) findViewById(R.id.df_track_image_view);
        trackDrawableImageView.setBackgroundResource(trackResource);

        //int progress = attrs.getAttributeIntValue(xmlns, "progress", 0);
        int progress = a.getInt(R.styleable.dreamlib_progress, 0);
        //int max = attrs.getAttributeIntValue(xmlns, "max", 100);
        int max = a.getInt(R.styleable.dreamlib_max, 100);
        //Logger.log("max=>"+max);
        setMax(max);
        setProgress(progress);

        a.recycle();

        //ProgressBarOutline outline = new ProgressBarOutline(context);
        //addView(outline);
    }

    public void setProgress(Integer value){
        ClipDrawable drawable = (ClipDrawable)
                progressDrawableImageView.getBackground();
        double percent = (double) value / (double)max;
        //Logger.log("value=>"+value);
        //Logger.log("percent=>"+percent);
        int level = (int)Math.floor(percent*10000);
        drawable.setLevel(level);
    }

}
