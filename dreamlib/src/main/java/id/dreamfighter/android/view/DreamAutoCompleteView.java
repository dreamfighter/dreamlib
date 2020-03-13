package id.dreamfighter.android.view;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import id.dreamfighter.android.R;

public class DreamAutoCompleteView extends AppCompatAutoCompleteTextView {
	private CloseButtonListener closeButtonListener;
	private Drawable imgCloseButton = getResources().getDrawable(R.drawable.df_cross);
	private boolean justCleared = false;
	
	public interface CloseButtonListener{
		public void onCloseButtonClick();
	}

	public DreamAutoCompleteView(Context context) {
		super(context);
		init();
	}

	public DreamAutoCompleteView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DreamAutoCompleteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		// Set the bounds of the button
		  this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgCloseButton, null);

		  // button should be hidden on first draw
		  clearButtonHandler();

		  //if the clear button is pressed, clear it. Otherwise do nothing
		  this.setOnTouchListener(new OnTouchListener(){
			  
			   @Override
			   public boolean onTouch(View v, MotionEvent event){
	
				   DreamAutoCompleteView et = DreamAutoCompleteView.this;
	
				   if (et.getCompoundDrawables()[2] == null){
					   return false;
				   }
			     
	
				   if (event.getAction() != MotionEvent.ACTION_UP){
					     return false;
				   }
	
				   if (event.getX() > et.getWidth() - et.getPaddingRight() - imgCloseButton.getIntrinsicWidth()){
					   et.setText("");
					   clearButtonHandler();
					   justCleared = true;
				   }
				   return true;
			   }
		  });
	}
	
	private void clearButtonHandler(){
	  if (this == null || this.getText().toString().equals("") || this.getText().toString().length() == 0){
	   //Log.d("CLRBUTTON", "=cleared");
	   //remove clear button
	   this.setCompoundDrawables(null, null, null, null);
	  } else {
	   //Log.d("CLRBUTTON", "=not_clear");
	   //add clear button
	   this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgCloseButton, null);
	  }
	}

	public CloseButtonListener getCloseButtonListener() {
		return closeButtonListener;
	}

	public void setCloseButtonListener(CloseButtonListener closeButtonListener) {
		this.closeButtonListener = closeButtonListener;
	}

	public boolean isJustCleared() {
		return justCleared;
	}

	public void setJustCleared(boolean justCleared) {
		this.justCleared = justCleared;
	}
	

}
