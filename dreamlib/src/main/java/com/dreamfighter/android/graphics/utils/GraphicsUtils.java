package com.dreamfighter.android.graphics.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import com.dreamfighter.android.log.Logger;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;
import android.util.Log;

public class GraphicsUtils {
	
	public static synchronized Bitmap convertBitmap(Resources res, int width, int height, Bitmap bitmap, int index) {
		//int normalizeHeight = width * bitmap.getHeight() / bitmap.getWidth();
		Bitmap b = Bitmap.createBitmap(width, height,bitmap.getConfig());
		//Bitmap b = bitmap;
		b.eraseColor(0xFFFFFFFF);
		Canvas c = new Canvas(b);
		Drawable d = new BitmapDrawable(res, bitmap);
        
		int marginTop = 7;
		int marginBottom = 7;
		int marginLeft = 7;
		int marginRight = 7;
		
		int border = 7;
		int borderLeft = 0;
		int borderRight = 0;
		
		if(index % 2==1){
			borderLeft = 7;
			borderRight = 7;
		}else{
			borderLeft = 7;
			borderRight = 7;
		}
		
		//Rect rBackgroud;
		
		Rect r;
		if(index % 2==1){
			r = new Rect(0, marginTop, width - marginRight, height - marginBottom);
		}else{
			r = new Rect(marginLeft, marginTop, width, height - marginBottom);
		}

		int imageWidth = r.width() - marginLeft;		
		
		int imageHeight = 0;
		try{
			imageHeight = imageWidth * d.getIntrinsicHeight() / d.getIntrinsicWidth();
		}catch (Exception e) {
			e.printStackTrace();
		}
		try{
			if (imageHeight > r.height() - (border * 2)) {
				
				imageHeight = r.height() - (border * 2);
				imageWidth = imageHeight * d.getIntrinsicWidth() / d.getIntrinsicHeight();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		/*if(index % 2==1){
			r.left = borderLeft;
			r.right = imageWidth ;
		}else{
			r.left = borderLeft;
			r.right = r.left + imageWidth + borderRight;
		}
		*/
		
		r.top += ((r.height() - imageHeight) / 2);
		r.bottom = r.top + imageHeight - marginBottom;

		Paint p = new Paint();
		//p.setColor(0xFFC00000);
		//p.setColor(0xFFD8D5B6);
		//p.setColor(0xFFFFFFFF);
		p.setColor(0xFF555555);
		//p.setColor(0xFFD8D5B6);
		
		//Shader shader;
		
		
		if(index % 2==1){
			/*if(index<=2){
				rBackgroud = new Rect(borderLeft-marginRight, 0, width-marginRight, height - marginBottom);
				c.drawRect(rBackgroud, p);
			}*/
			Rect rect = new Rect(r.right, 0, width-5, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			
			rect = new Rect(r.right+4, 0, width-1, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			//c.drawLine(c.getWidth()-1, 0, c.getWidth()-1, height, p);
			//shader = new LinearGradient(r.right-borderRight, 0, width-marginRight, 0, Color.BLACK, Color.WHITE, TileMode.CLAMP);  
		}else{
			/*if(index<=2){
				rBackgroud = new Rect(borderLeft-marginRight, 0, width, height - marginBottom);
				c.drawRect(rBackgroud, p);
			}*/
			Rect rect = new Rect(1, 0, 3, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			
			rect = new Rect(5, 0, 7, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			//c.drawLine(c.getWidth()-1, 0, c.getWidth()-1, height, p);
			//shader = new LinearGradient(borderLeft-marginRight, 0, r.left + borderLeft, 0, Color.WHITE, Color.BLACK, TileMode.CLAMP);
		}
		//p.setShader(shader);
		//c.drawRect(rect, p);
		
		p = new Paint();
		if(index % 2==1){
			r.right -= borderRight;
		}else{
			r.left += borderLeft;
		}
		
		r.top += border;
		r.bottom = r.bottom - border;
		
		d.setBounds(r);
		d.draw(c);

		p.setColor(0xFF555555);
		//p.setColor(0xFFD8D500);
		if(index % 2==1){
			c.drawLine(0, 0, 0, height, p);
		}else{
			c.drawLine(c.getWidth()-1, 0, c.getWidth()-1, height, p);
		}
		bitmap.recycle();
		bitmap = null;
		System.gc();
		return b;
	}
	
	public static synchronized Bitmap rescaleBitmap(InputStream in) {
		
		try {
			//InputStream in = new FileInputStream(path);
		    final int IMAGE_MAX_SIZE = 1000000; // 1.2MP

		    // Decode image size
		    BitmapFactory.Options o = new BitmapFactory.Options();
		    o.inJustDecodeBounds = true;
		    Bitmap bmp = BitmapFactory.decodeStream(in, null, o);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
		    //in.close();



		    int scale = 1;
		    while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > 
		          IMAGE_MAX_SIZE) {
		       scale++;
		    }
		    Logger.log("scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

		    Bitmap b = null;
			
			bmp.compress(CompressFormat.JPEG, 90, stream);
			InputStream isOverlay = new ByteArrayInputStream(stream.toByteArray());
			
		    in = new ByteArrayInputStream(stream.toByteArray());
		    Logger.log("in=>"+in);
	        bmp.recycle();
	        bmp = null;
		    if (scale > 1) {
		        scale--;
		        // scale to max possible inSampleSize that still yields an image
		        // larger than target
		        o = new BitmapFactory.Options();
		        o.inSampleSize = scale;
		        b = BitmapFactory.decodeStream(in, null, o);

		        // resize to desired dimensions
		        int height = b.getHeight();
		        int width = b.getWidth();
		        Logger.log("1th scale operation dimenions - width: " + width + ", height: " + height);

		        double y = Math.sqrt(IMAGE_MAX_SIZE
		                / (((double) width) / height));
		        double x = (y / height) * width;

		        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, 
		           (int) y, true);
		        b.recycle();
		        b = scaledBitmap;
		        bmp.recycle();
		        bmp = null;
		        System.gc();
		    } else {
		        b = BitmapFactory.decodeStream(in);
		    }
		    in.close();

		    Logger.log("bitmap size - width: " +b.getWidth() + ", height: " + b.getHeight());
		    return b;
		} catch (IOException e) {
			e.printStackTrace();
		    Logger.log(e.getMessage());
		    return null;
		}
	}
	
	public static synchronized Canvas convertBitmap(Resources res, Canvas c,Bitmap bitmap, int index) {
		//int normalizeHeight = width * bitmap.getHeight() / bitmap.getWidth();
		//Bitmap b = Bitmap.createBitmap(width, height,bitmap.getConfig());
		//Bitmap b = bitmap;
		//b.eraseColor(0xFFFFFFFF);
		//Canvas c = new Canvas(b);
		Drawable d = new BitmapDrawable(res, bitmap);
		
        int width = c.getWidth();
        int height = c.getHeight();
        
        
		int marginTop = 7;
		int marginBottom = 7;
		int marginLeft = 7;
		int marginRight = 7;
		
		int border = 7;
		int borderLeft = 0;
		int borderRight = 0;
		
		if(index % 2==1){
			borderLeft = 7;
			borderRight = 7;
		}else{
			borderLeft = 7;
			borderRight = 7;
		}
		
		//Rect rBackgroud;
		
		Rect r;
		if(index % 2==1){
			r = new Rect(0, marginTop, width - marginRight, height - marginBottom);
		}else{
			r = new Rect(marginLeft, marginTop, width, height - marginBottom);
		}

		int imageWidth = r.width() - marginLeft;		
		
		int imageHeight = 0;
		try{
			imageHeight = imageWidth * d.getIntrinsicHeight() / d.getIntrinsicWidth();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try{
			if (imageHeight > r.height() - (border * 2)) {
				
				imageHeight = r.height() - (border * 2);
				imageWidth = imageHeight * d.getIntrinsicWidth() / d.getIntrinsicHeight();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		/*if(index % 2==1){
			r.left = borderLeft;
			r.right = imageWidth ;
		}else{
			r.left = borderLeft;
			r.right = r.left + imageWidth + borderRight;
		}
		*/
		
		r.top += ((r.height() - imageHeight) / 2);
		r.bottom = r.top + imageHeight - marginBottom;

		Paint p = new Paint();
		//p.setColor(0xFFC00000);
		//p.setColor(0xFFD8D5B6);
		//p.setColor(0xFFFFFFFF);
		p.setColor(0xFF555555);
		//p.setColor(0xFFD8D5B6);
		
		//Shader shader;
		
		
		if(index % 2==1){
			/*if(index<=2){
				rBackgroud = new Rect(borderLeft-marginRight, 0, width-marginRight, height - marginBottom);
				c.drawRect(rBackgroud, p);
			}*/
			Rect rect = new Rect(r.right, 0, width-5, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			
			rect = new Rect(r.right+4, 0, width-1, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			//c.drawLine(c.getWidth()-1, 0, c.getWidth()-1, height, p);
			//shader = new LinearGradient(r.right-borderRight, 0, width-marginRight, 0, Color.BLACK, Color.WHITE, TileMode.CLAMP);  
		}else{
			/*if(index<=2){
				rBackgroud = new Rect(borderLeft-marginRight, 0, width, height - marginBottom);
				c.drawRect(rBackgroud, p);
			}*/
			Rect rect = new Rect(1, 0, 3, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			
			rect = new Rect(5, 0, 7, height);
			//p.setColor(0xFF555555);
			c.drawRect(rect, p);
			//c.drawLine(c.getWidth()-1, 0, c.getWidth()-1, height, p);
			//shader = new LinearGradient(borderLeft-marginRight, 0, r.left + borderLeft, 0, Color.WHITE, Color.BLACK, TileMode.CLAMP);
		}
		//p.setShader(shader);
		//c.drawRect(rect, p);
		
		p = new Paint();
		if(index % 2==1){
			r.right -= borderRight;
		}else{
			r.left += borderLeft;
		}
		
		r.top += border;
		r.bottom = r.bottom - border;
		
		d.setBounds(r);
		d.draw(c);

		p.setColor(0xFF555555);
		//p.setColor(0xFFD8D500);
		if(index % 2==1){
			c.drawLine(0, 0, 0, height, p);
		}else{
			c.drawLine(c.getWidth()-1, 0, c.getWidth()-1, height, p);
		}
		//bitmap.recycle();
		//bitmap = null;
		//System.gc();
		return c;
	}
	/**
	    * Calculates the transform from screen coordinate
	    * system to world coordinate system coordinates
	    * for a specific point, given a camera position.
	    *
	    * @param touch Vec2 point of screen touch, the
	      actual position on physical screen (ej: 160, 240)
	    * @param cam camera object with x,y,z of the
	      camera and screenWidth and screenHeight of
	      the device.
	    * @return position in WCS.
	    */
	   public Vec2 GetWorldCoords(GL10 gl, Vec2 touch, float screenWidth, float screenHeight){  
	       // Initialize auxiliary variables.
	       Vec2 worldPos = new Vec2();
	       
	       // SCREEN height & width (ej: 320 x 480)
	       float screenW = screenWidth;
	       float screenH = screenHeight;
	              
	       // Auxiliary matrix and vectors
	       // to deal with ogl.
	       float[] invertedMatrix, transformMatrix,
	           normalizedInPoint, outPoint;
	       invertedMatrix = new float[16];
	       transformMatrix = new float[16];
	       normalizedInPoint = new float[4];
	       outPoint = new float[4];
	 
	       // Invert y coordinate, as android uses
	       // top-left, and ogl bottom-left.
	       int oglTouchY = (int) (screenH - touch.Y());
	       
	       /* Transform the screen point to clip
	       space in ogl (-1,1) */       
	       normalizedInPoint[0] =
	        (float) ((touch.X()) * 2.0f / screenW - 1.0);
	       normalizedInPoint[1] =
	        (float) ((oglTouchY) * 2.0f / screenH - 1.0);
	       normalizedInPoint[2] = - 1.0f;
	       normalizedInPoint[3] = 1.0f;
	 
	       /* Obtain the transform matrix and
	       then the inverse. */
	       //Print("Proj", getCurrentProjection(gl));
	       //Print("Model", getCurrentModelView(gl));
	       Matrix.multiplyMM(
	           transformMatrix, 0,
	           getCurrentProjection(gl), 0,
	           getCurrentModelView(gl), 0);
	       Matrix.invertM(invertedMatrix, 0,
	           transformMatrix, 0);       
	 
	       /* Apply the inverse to the point
	       in clip space */
	       Matrix.multiplyMV(
	           outPoint, 0,
	           invertedMatrix, 0,
	           normalizedInPoint, 0);
	       
	       if (outPoint[3] == 0.0)
	       {
	           // Avoid /0 error.
	           Log.e("World coords", "ERROR!");
	           return worldPos;
	       }
	       
	       // Divide by the 3rd component to find
	       // out the real position.
	       worldPos.Set(
	           outPoint[0] / outPoint[3],
	           outPoint[1] / outPoint[3]);
	         
	       return worldPos;       
	   }
	   
	   /**
	    * Record the current modelView matrix
	    * state. Has the side effect of
	    * setting the current matrix state
	    * to GL_MODELVIEW
	    * @param gl context
	    */
	   public float[] getCurrentModelView(GL10 gl)
	   {
	        float[] mModelView = new float[16];
	        getMatrix(gl, GL10.GL_MODELVIEW, mModelView);
	        return mModelView;
	   }
	 
	   /**
	    * Record the current projection matrix
	    * state. Has the side effect of
	    * setting the current matrix state
	    * to GL_PROJECTION
	    * @param gl context
	    */
	   public float[] getCurrentProjection(GL10 gl)
	   {
	       float[] mProjection = new float[16];
	       getMatrix(gl, GL10.GL_PROJECTION, mProjection);
	       return mProjection;
	   }
	 
	   /**
	    * Fetches a specific matrix from opengl
	    * @param gl context
	    * @param mode of the matrix
	    * @param mat initialized float[16] array
	    * to fill with the matrix
	    */
	   private void getMatrix(GL10 gl, int mode, float[] mat)
	   {
	       MatrixTrackingGL gl2 = new MatrixTrackingGL(gl);
	       gl2.glMatrixMode(mode);
	       gl2.getMatrix(mat, 0);
	   }
}
