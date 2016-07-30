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
			Log.d("log_info ["+className+":"+lineNumber+"]", log);
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
