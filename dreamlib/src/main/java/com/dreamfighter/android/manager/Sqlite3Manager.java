package com.dreamfighter.android.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public abstract class Sqlite3Manager extends SqliteManager{

	public Sqlite3Manager(Context context, String databaseName,
                          int databaseVersion, Class<?> classDefinition) {
		super(databaseName,databaseVersion, context, classDefinition);

		SharedPreferences pref = context.getSharedPreferences("DATABASE_PREF",0);
		if(databaseVersion>pref.getInt(tableName+"-version",1)){
			onUpgrade(pref.getInt(tableName+"-version",1),databaseVersion);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt(tableName+"-version",databaseVersion);
			editor.commit();
		}
	}

	public abstract void onUpgrade(int oldVersion,int newVersion);

	@Override
	public Map<String, String> colums() {
		return COLUMNS;
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
