package com.dreamfighter.android.log;

import android.util.Log;

public class Logger {
	public static boolean enable = true;
	
	public static void log(Object o,String log){
		if(enable){
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.d("log_info ["+o.getClass().getName()+":"+lineNumber+"]", log);
		}
	}
	public static void log(String log){
		if(enable){
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			String className = Thread.currentThread().getStackTrace()[3].getClassName();
			int maxLogSize = 1000;
			if(log.length()>maxLogSize) {
				for (int i = 0; i <= log.length() / maxLogSize; i++) {
					int start = i * maxLogSize;
					int end = (i + 1) * maxLogSize;
					end = end > log.length() ? log.length() : end;
					//android.util.Log.d(TAG, message.substring(start, end));
					Log.d("log_info [" + className + ":" + lineNumber + "]", log.substring(start, end));
				}
			}else {
				Log.d("log_info [" + className + ":" + lineNumber + "]", log);
			}
		}
	}


	public static void error(String log){
		int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
		String className = Thread.currentThread().getStackTrace()[3].getClassName();
        Log.e("log_info ["+className+":"+lineNumber+"]", log);

	}
	
	public static void log(Class<?> c,String log){
		if(enable){
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.d("log_info ["+c.getName()+":"+lineNumber+"]", log);
		}
	}
	
	public static void log(String c,String log){
		if(enable){
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.d("log_info ["+c+":"+lineNumber+"]", log);
		}
	}
}
