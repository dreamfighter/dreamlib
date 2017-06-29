package com.dreamfighter.android.manager;

import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Sqlite2Manager extends SqliteManager{

	public Sqlite2Manager(Context context, String databaseName,
			int databaseVersion, Class<?> classDefinition) {
		super(databaseName,databaseVersion, context, classDefinition);
	}

	@Override
	public Map<String, String> colums() {
		return COLUMNS;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
