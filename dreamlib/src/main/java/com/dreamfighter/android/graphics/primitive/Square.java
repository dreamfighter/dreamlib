package com.dreamfighter.android.graphics.primitive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;

/**
 * This class is an object representation of 
 * a Square containing the vertex information
 * and drawing functionality, which is called 
 * by the renderer.
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class Square {
		
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private ByteBuffer indexBuffer;
	private int[] textures = new int[1];
	private float rotation = 0.0f;
	private boolean rotating = false;
	private boolean canTouch = false;
	private boolean usingColor = false;
	private float x = 0.0f;
	private float y = 0.0f;
	private float speed = 0.1f;
	//private int speedAlpha = 1;
	private int id = 0;
	
	private static Point TOP_LEFT = new Point(0,1);
	private static Point TOP_RIGHT = new Point(3,4);
	private static Point BOTTOM_LEFT = new Point(6,7);
	//private static Point BOTTOM_RIGHT = new Point(6,7);
	
	/** The initial vertex definition */
	private float vertices[] = { 
						0.0f, -1.0f, 0.0f, 	//Bottom Left
						2.0f, -1.0f, 0.0f, 		//Bottom Right
						0.0f, 1.0f, 0.0f, 		//Top Left
						2.0f, 1.0f, 0.0f 		//Top Right
										};
		
	private float texture[] = {    		
    		//Mapping coordinates for the vertices
            0.0f, 0.0f, //represented by 2
            1.0f, 0.0f, //represented by 3
			0.0f, 1.0f, //represented by 0
            1.0f, 1.0f  //represented by 1
    							};
	
    /** The initial indices definition */	
    private byte indices[] = {
    					//Faces definition
    					0,1,3, 0,3,2 			//Face front 	
    										};
	/**
	 * The Square constructor.
	 * 
	 * Initiate the buffers.
	 */
	public Square(float[] verticlesP) {
		if(verticlesP.length!=0){
			this.vertices = verticlesP;
		}
		initialize();
	}
	
	public Square(int id, float left, float top, float width, float height, boolean canTouch) {
		this.id = id;
		this.canTouch = canTouch;
		vertices[0] = left;
		vertices[1] = top;
		vertices[3] = left + width;
		vertices[4] = top;
		vertices[6] = left;
		vertices[7] = top + height;
		vertices[9] = left + width;
		vertices[10] = top + height;

		initialize();
	}
	
	public void updateVertex(float left,float top,float width,float height) {
		vertices[0] = left;
		vertices[1] = top;
		vertices[3] = left + width;
		vertices[4] = top;
		vertices[6] = left;
		vertices[7] = top + height;
		vertices[9] = left + width;
		vertices[10] = top + height;
	}
	
	public boolean isTouched(float x,float y){
		if(isCanTouch()){
			if(
					x < vertices[TOP_RIGHT.x] + this.x  && 
					x > vertices[TOP_LEFT.x] + this.x 
					&& 
					y < vertices[TOP_LEFT.y] + this.y && 
					y > vertices[BOTTOM_LEFT.y] + this.y 
							){
				return true;
			}
		}
		return false;
	}
	
	public boolean isPointInRegion(float x,float y){
		/*Logger.log("vertices[TOP_RIGHT.x]=>"+(vertices[TOP_RIGHT.x] + this.x));
		Logger.log("vertices[TOP_LEFT.x]=>"+(vertices[TOP_LEFT.x]+ this.x));
		Logger.log("vertices[TOP_LEFT.y]=>"+(vertices[TOP_LEFT.y]+ this.y));
		Logger.log("vertices[BOTTOM_LEFT.y]=>"+(vertices[BOTTOM_LEFT.y]+ this.y));
		Logger.log("x=>"+x);
		Logger.log("y=>"+y);
		Logger.log("x < vertices[TOP_RIGHT.x] + this.x=>"+(x < vertices[TOP_RIGHT.x] + this.x));
		Logger.log("x > vertices[TOP_LEFT.x] + this.x=>"+(x > vertices[TOP_LEFT.x] + this.x));
		Logger.log("y < vertices[TOP_LEFT.y] + this.y=>"+(y < vertices[TOP_LEFT.y] + this.y));
		Logger.log("y > vertices[BOTTOM_LEFT.y] + this.y=>"+(y > vertices[BOTTOM_LEFT.y] + this.y));*/
		if(
				x < vertices[TOP_RIGHT.x] + this.x  && 
				x > vertices[TOP_LEFT.x] + this.x 
				&& 
				y < vertices[TOP_LEFT.y] + this.y && 
				y > vertices[BOTTOM_LEFT.y] + this.y 
						){
			return true;
		}
		
		return false;
	}
	
	protected void initialize(){
		//for pointer
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		
		//for texture
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
		

		//
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	/**
	 * The object own drawing function.
	 * Called from the renderer to redraw this instance
	 * with possible changes in values.
	 * 
	 * @param gl - The GL Context
	 */
	public void draw(GL10 gl) {		
		//Set the face rotation
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glFrontFace(GL10.GL_CW);
		
		//Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		
		//Enable vertex buffer
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		if(isUsingColor()){
			//Set The Color To Blue
			gl.glColor4f(0.450f, 0.450f, 0.450f, 0.60f);
			//gl.glColor4f(0.607f, 0.084f, 0.784f, 0.38f);//red gren blue alpha
		}
		
		//Draw the vertices as triangle strip
		//gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
		gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	
	public void loadGLTexture(GL10 gl, Bitmap bitmap) {
		//Get the texture from the Android resource directory
		

		//Generate one texture pointer...
		gl.glGenTextures(1, textures, 0);
		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		//Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		//Clean up
		bitmap.recycle();
		System.gc();
	}
	
	public void drawText(GL10 gl, Context context, int backgroundId){
		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);
		// get a background image from resources
		// note the image format must match the bitmap format
		Drawable background = context.getResources().getDrawable(backgroundId);
		background.setBounds(0, 0, 256, 256);
		background.draw(canvas); // draw the background to our bitmap
		// Draw the text
		Paint textPaint = new Paint();
		textPaint.setTextSize(32);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(255, 255, 0, 0);
		// draw the text centered
		canvas.drawText("Hello World", 0,0, textPaint);

		//Generate one texture pointer...
		gl.glGenTextures(1, textures, 0);
		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		//Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	}
	
	public void loadGLTexture(GL10 gl, Context context, int id) {
		//Get the texture from the Android resource directory
		InputStream is = context.getResources().openRawResource(id);
		Bitmap bitmap = null;
		try {
			//BitmapFactory is an Android graphics utility for images
			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			//Always clear and close
			try {
				is.close();
				is = null;
			} catch (IOException e) {
			}
		}

		//Generate one texture pointer...
		gl.glGenTextures(1, textures, 0);
		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		//Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		//Clean up
		bitmap.recycle();
		System.gc();
	}
	
	public void translateX(float x){
		this.x = x;
	}
	
	public void translateY(float y){
		this.y = y;
	}
	
	public void rotate(GL10 gl,float angle){
		gl.glLoadIdentity();					//Reset The Current Modelview Matrix
		gl.glTranslatef(x, y, -6.0f);	//Move down 1.0 Unit And Into The Screen 6.0
		gl.glRotatef(rotation, 0.0f, 0.1f, 0.0f);	//Rotate The Square On The X axis ( NEW )
		//gl.glColor4f(0.607f, 0.584f, 0.684f, 1.0f/speedAlpha); //alpha blue
		draw(gl);
		if(isRotating()){
			rotation -= (angle + speed);
//			/speedAlpha ++;
			speed +=2.85f;
		}
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public boolean isRotating() {
		return rotating;
	}

	public void setRotating(boolean rotating) {
		this.rotating = rotating;
	}

	public boolean isCanTouch() {
		return canTouch;
	}

	public void setCanTouch(boolean canTouch) {
		this.canTouch = canTouch;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isUsingColor() {
		return usingColor;
	}

	public void setUsingColor(boolean usingColor) {
		this.usingColor = usingColor;
	}
}
