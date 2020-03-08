package id.dreamfighter.android.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

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
