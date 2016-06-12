package com.dreamfighter.android.manager;

import java.util.Map;

import android.content.Context;

public class Sqlite2Manager extends SqliteManager{

	public Sqlite2Manager(Context context, String databaseName,
			int databaseVersion, Class<?> classDefinition) {
		super(databaseName,databaseVersion, context, classDefinition);
	}

	@Override
	public Map<String, String> colums() {
		return COLUMNS;
	}

}
