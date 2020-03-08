package id.dreamfighter.android.manager;

import android.content.Context;
import android.database.Cursor;

import id.dreamfighter.android.utils.SqliteHelper;

import java.util.Map;

public abstract class SqliteManager extends SqliteHelper{
	
	public SqliteManager(Context context, String databaseName,
                         int databaseVersion, String tableName, Class<?> classDefinition) {
		super(context, databaseName, databaseVersion,tableName);
		this.classDefinition = classDefinition;
	}
	
	public SqliteManager(Context context, String databaseName,
                         int databaseVersion, Class<?> classDefinition) {
		super(context, databaseName, databaseVersion, classDefinition.getSimpleName());
		this.classDefinition = classDefinition;
	}
	
	protected SqliteManager(String databaseName,
                            int databaseVersion, Context context, Class<?> classDefinition) {
		super(context, databaseName, databaseVersion, classDefinition);
	}

	@Override
	public String tableName() {
		return tableName;
	}

	@Override
	public abstract Map<String, String> colums();

	@Override
	public Object cursorToImpl(Cursor cursor) {
			return cursorToImpl(cursor, classDefinition);
	}

	public Object cursorToImpl(Cursor cursor, Class<?> classDefinition) {
		try {
			return cursorToObject(cursor,classDefinition);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
