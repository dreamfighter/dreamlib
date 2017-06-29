package com.dreamfighter.android.manager;

import android.content.Context;

import java.util.Map;

public abstract class Sqlite3Manager extends SqliteManager{

	public Sqlite3Manager(Context context, String databaseName,
                          int databaseVersion, Class<?> classDefinition) {
		super(databaseName,databaseVersion, context, classDefinition);
	}

	@Override
	public Map<String, String> colums() {
		return COLUMNS;
	}

}
