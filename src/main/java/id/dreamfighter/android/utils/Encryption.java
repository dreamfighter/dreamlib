package id.dreamfighter.android.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Base64;

import id.dreamfighter.android.log.Logger;

public class Encryption {
	  //private static String DIRECTORY = "/Android/data/com.cordoba.android.alqurancordoba/";
	  private static String BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
	  private static String FILANAME ="keystore";
	  
	  private Cipher ecipher;
	  private Cipher dcipher;
	  private Context context;
		
	  private static byte[] iv = {
		    (byte)0xB2, (byte)0x12, (byte)0xD5, (byte)0xB2,
		    (byte)0x44, (byte)0x21, (byte)0xC3, (byte)0xC3
		  };
		 
	  public Encryption(Context context) {
		  this.context = context;
		  call(context);
	  }
		  
		  public SecretKey generateKey() throws NoSuchAlgorithmException {
			    KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
			    keyGenerator.init(256); //128 default; 192 and 256 also possible
			    SecretKey key = keyGenerator.generateKey();
			    return key;
		  }
		  
		  public Keystore saveKey(Context context){
			  	//Logger.log("generate key");
			  	Keystore keystore = null;
			    try{
		    		SecretKey key;
					key = KeyGenerator.getInstance("DES").generateKey();
		    		keystore  = new Keystore(key);
		    	    //use buffering
		    	    OutputStream file = context.openFileOutput(FILANAME, Context.MODE_PRIVATE);
		    	    OutputStream buffer = new BufferedOutputStream(file);
		    	    ObjectOutput output = new ObjectOutputStream( buffer );
		    	    try{
		    	      output.writeObject(keystore);
		    	    }
		    	    finally{
		    	      output.close();
		    	    }
			    }catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
			    }catch(IOException ex){
			    	Logger.log(ex.getMessage());
		    	}
			    return keystore;
		  }
		  
		  public Keystore loadKey(Context context) throws IOException {
			  	//Logger.log("load key");
			    //String filename = BASE_DIR + DIRECTORY + FILANAME;
			    InputStream file;
				Keystore keystore = null;
				
				file = new FileInputStream(context.getFileStreamPath(FILANAME));
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input;
				input = new ObjectInputStream(buffer);
				try{
					keystore = (Keystore) input.readObject();
				} catch (ClassNotFoundException e) {
					Logger.log(e.getMessage());
				}finally{
					input.close();
				}
			    return keystore;
			}
		  	
			public void call(Context context) {
				//SecretKey key = null;
				
				/*for save to sdcard*/
				Keystore keystore = null;
				File file = new File(context.getFilesDir(), FILANAME);
				//Logger.log("Dir keystore=>"+context.getFilesDir().getAbsolutePath() +"/"+ FILANAME);
				if(!file.exists()) {
					//Logger.log("keystore not found!");
					keystore = saveKey(context);
				}else{
					try {
						keystore = loadKey(context);
					} catch (IOException e) {
						e.printStackTrace();
		 				keystore = saveKey(context);
					}
				}
								
				//for save to preference
				/*String hex = QuranSettings.getInstance().getKeystore();
				Logger.log(this, "keystore hex=>"+hex);
			    byte[] encoded = new BigInteger(hex, 16).toByteArray();
			    key = new SecretKeySpec(encoded, "DES");*/
					
			    try {
			    	AlgorithmParameterSpec paramSpec;
			    	String permission = "android.permission.READ_PHONE_STATE";
			        int res = context.checkCallingOrSelfPermission(permission);
			        if(res != PackageManager.PERMISSION_GRANTED){
			        	paramSpec = new IvParameterSpec(iv);
			        }else{
				        Logger.log("getIMEI()=>"+getIMEI().length);
				        paramSpec = new IvParameterSpec(getIMEI());
			        }
			        
			 
			        ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			        dcipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			 
			        ecipher.init(Cipher.ENCRYPT_MODE, keystore.getKey(), paramSpec);
			        dcipher.init(Cipher.DECRYPT_MODE, keystore.getKey(), paramSpec);
			        
			        //ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			        //dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			 
			    }
			    catch (InvalidAlgorithmParameterException e) {
			    	Logger.log("Invalid Alogorithm Parameter:" + e.getMessage());
			        return;
			    }
			    catch (NoSuchAlgorithmException e) {
			    	Logger.log("No Such Algorithm:" + e.getMessage());
			        return;
			    }
			    catch (NoSuchPaddingException e) {
			    	Logger.log("No Such Padding:" + e.getMessage());
			        return;
			    }
			    catch (InvalidKeyException e) {
			    	Logger.log("Invalid Key:" + e.getMessage());
			        return;
			    } catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			
			public String encryptString(String content) throws FileNotFoundException{
				byte[] byteArray;
				try {
				    byteArray = content.getBytes("UTF-8");
				    return new String(Base64.encode(byteArray, Base64.DEFAULT), "UTF-8");
				    //System.out.println(new String(, Base64.DEFAULT)));
				} catch (UnsupportedEncodingException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}
				return content;
			}
			
			public void encrypt(String srcFilename) throws FileNotFoundException{
				encrypt(new FileInputStream(srcFilename), new FileOutputStream(BASE_DIR + context.getPackageName() +srcFilename));
			}
			
			public void encrypt(InputStream srcInputstream,String destFilename) throws FileNotFoundException{
				encryptFromStream(srcInputstream, BASE_DIR + context.getPackageName() + destFilename);
			}
			
			public void encryptFromStream(InputStream srcInputstream,String destFilePath) throws FileNotFoundException{
				encrypt(srcInputstream, new FileOutputStream(destFilePath));
			}
			
			public byte[] getIMEI() throws UnsupportedEncodingException{
				String identifier = null;
				TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm != null){
				      identifier = tm.getDeviceId();
				}
				if (identifier == null || identifier.length() == 0){
				      identifier = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
				}
				Logger.log("identifier.length=>"+identifier.length());
				Logger.log("identifier"+identifier);
				return identifier.substring(0, 8).getBytes("UTF-8");
			}
			
			public static String getIMEI(Context context) throws UnsupportedEncodingException{
				String permission = "android.permission.READ_PHONE_STATE";
		        int res = context.checkCallingOrSelfPermission(permission);
		        if(res != PackageManager.PERMISSION_GRANTED){
		        	return null;
		        }
				String identifier = null;
				TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm != null){
				      identifier = tm.getDeviceId();
				}
				if (identifier == null || identifier.length() == 0){
				      identifier = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
				}
				Logger.log("identifier.length=>"+identifier.length());
				Logger.log("identifier"+identifier);
				return identifier;
			}
			 
			private void encrypt(InputStream is, OutputStream os) {
			 
			    try {
			 
			        //call();
			 
			        byte[] buf = new byte[1024];
			 
			        // bytes at this stream are first encoded
			        os = new CipherOutputStream(os, ecipher);
			 
			        // read in the clear text and write to out to encrypt
			        int numRead = 0;
			        while ((numRead = is.read(buf)) >= 0) {
			            os.write(buf, 0, numRead);
			        }
			 
			        // close all streams
			        os.close();
			 
			    }
			    catch (IOException e) {
			    	Logger.log("I/O Error:" + e.getMessage());
			    }
			 
			}
			
			public String decryptString(String content) throws IOException{
				byte[] byteArray;
				try {
				    byteArray = content.getBytes("UTF-8");
				    //Logger.log("content=>"+new String(Base64.decode(byteArray, Base64.DEFAULT), "UTF-8"));
				    return new String(Base64.decode(byteArray, Base64.DEFAULT), "UTF-8");
				    //System.out.println(new String(, Base64.DEFAULT)));
				} catch (UnsupportedEncodingException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}
				return content;
			}
			
			public void decrypt(String filename, String filenameOut) throws FileNotFoundException{
				decrypt(new FileInputStream(filename), new FileOutputStream(filenameOut));
			}
			
			public InputStream decrypt(String filename) throws IOException,FileNotFoundException{
				//Logger.log(this, "file name to decrypt=>"+filename);
				//return new FileInputStream(filename);
				return decrypt(new FileInputStream(filename));
			}
			
			public InputStream decrypt(InputStream is) throws IOException {
				//Log.d(log_info,"masuk sini cuy");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    
			        //call();
			        byte[] buf = new byte[1024];
			 
			        // bytes read from stream will be decrypted
			        CipherInputStream cis = new CipherInputStream(is, dcipher);
			 
			        // read in the decrypted bytes and write the clear text to out
			        int numRead = 0;
			        while ((numRead = cis.read(buf)) > 0) {
			        	baos.write(buf, 0, numRead);
			        }
			
			       
			        // close all streams
			        cis.close();
			        is.close();

			    // Open new InputStreams using the recorded bytes
			    // Can be repeated as many times as you wish 
				return new ByteArrayInputStream(baos.toByteArray());
			}
			
			public byte[] decryptToBuffer(InputStream is) throws IOException {
				//Log.d(log_info,"masuk sini cuy");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    
			        //call();
			        byte[] buf = new byte[1024];
			 
			        // bytes read from stream will be decrypted
			        CipherInputStream cis = new CipherInputStream(is, dcipher);
			 
			        // read in the decrypted bytes and write the clear text to out
			        int numRead = 0;
			        while ((numRead = cis.read(buf)) > 0) {
			        	baos.write(buf, 0, numRead);
			        }
			
			       
			        // close all streams
			        cis.close();
			        is.close();

			    // Open new InputStreams using the recorded bytes
			    // Can be repeated as many times as you wish 
				return baos.toByteArray();
			}
			
			public void decrypt(InputStream is, OutputStream os) {
			    try {
			        //call();
			        byte[] buf = new byte[1024];
			 
			        // bytes read from stream will be decrypted
			        CipherInputStream cis = new CipherInputStream(is, dcipher);
			 
			        // read in the decrypted bytes and write the clear text to out
			        int numRead = 0;
			        while ((numRead = cis.read(buf)) > 0) {
			            os.write(buf, 0, numRead);
			        }
			
			       
			        // close all streams
			        cis.close();
			        is.close();
			        os.close();
			    }
			    catch (IOException e) {
			    	Logger.log("I/O Error:" + e.getMessage());
			    }
			}
		}