package com.dreamfighter.android.sql;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.dreamfighter.android.entity.BaseEntity;
import com.dreamfighter.android.log.Logger;

public abstract class SqliteHelper extends SQLiteOpenHelper{
	public static final String ID = "id";
	public static final String UPDATED_ON = "updatedOn";
	public static final String CREATED_ON = "createdOn";
	protected Map<String,String> COLUMNS = new HashMap<String, String>();
	private SQLiteDatabase database;
	//private boolean idAutoIncrement = true;
	protected String tableName;
	protected Class<?> classDefinition;
	protected Context context;
	
	@SuppressLint("SimpleDateFormat")
	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	

	public SqliteHelper(Context context,String databaseName,int databaseVersion) {
		super(context, databaseName, null, databaseVersion);
		this.context = context;
		try{
			this.database = getWritableDatabase();
			if(!isTableExists()){
				onCreate(database);
			}
		} catch(SQLiteException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			close();
		}
	}
	

	public SqliteHelper(Context context,String databaseName,int databaseVersion, String tableName) {
		super(context, databaseName, null, databaseVersion);
		try{
			this.tableName = tableName;
			this.database = getWritableDatabase();
			if(!isTableExists()){
				onCreate(database);
			}
		} catch(SQLiteException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			close();
		}
	}
	

	protected SqliteHelper(Context context, String databaseName,int databaseVersion, Class<?> classDefinition) {
		super(context, databaseName, null, databaseVersion);
		try{
			
			this.classDefinition = classDefinition;
			this.tableName = classDefinition.getSimpleName();
			COLUMNS = generatingColumns(classDefinition,null,COLUMNS);
			this.database = getWritableDatabase();
			/*List<Field> fields = new ArrayList<Field>();
			
			fields = new LinkedList<Field>(Arrays.asList(classDefinition.getDeclaredFields()));
			if(classDefinition.getSuperclass()!=null){
				fields.addAll(Arrays.asList(classDefinition.getSuperclass().getDeclaredFields()));
			}
			
			for(Field field:fields){
				if(field.getName().equalsIgnoreCase("id")){
					continue;
				}
				String value = "TEXT NULL";
				if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
					value = "INTEGER NULL";
				}else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
					value = "INTEGER NULL";
				}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
					value = "REAL NULL";
				}else if(field.getType().getName().equalsIgnoreCase("java.lang.Float")){
					value = "REAL NULL";
				}else if(field.getType().getName().equalsIgnoreCase("java.util.List")){
					continue;
				}else if(!field.getType().getName().equalsIgnoreCase("java.util.Date")){
					value = "INTEGER NULL";
					String subClassName = field.getClass().getSimpleName();
					if(!isTableExists(field.getClass().getSimpleName())){
						database.execSQL(createDatabase());
					}
				}
				COLUMNS.put(field.getName(), value);
			}*/
			
			if(!isTableExists()){
				onCreate(database);
			}
		} catch(SQLiteException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			close();
		}
	}
	
	protected String[] retrieveColumns(Class<?> classDefinition){
		//String tableName = classDefinition.getSimpleName();
		List<Field> fields = new ArrayList<Field>();
		
		fields = new LinkedList<Field>(Arrays.asList(classDefinition.getDeclaredFields()));
		if(classDefinition.getSuperclass()!=null){
			fields.addAll(Arrays.asList(classDefinition.getSuperclass().getDeclaredFields()));
		}
		List<String> columns = new ArrayList<String>();
		//Logger.log("classDefinition=>"+classDefinition.getName());
		for(int i=0;i<fields.size();i++){
			Field field = fields.get(i);
			if(field.getType().getName().equalsIgnoreCase("java.util.List")){
				continue;
			}
			
			columns.add(field.getName());
		}
		return columns.toArray(new String[columns.size()]);
	}
	
	private Map<String,String> generatingColumns(Class<?> classDefinition,Class<?> parentClassDefinition){
		return generatingColumns(classDefinition,parentClassDefinition, new HashMap<String, String>());
	}
	
	private Map<String,String> generatingColumns(Class<?> classDefinition,Class<?> parentClassDefinition, Map<String,String> columns){
		//String tableName = classDefinition.getSimpleName();
		//Map<String,String> columns = new HashMap<String, String>();
		List<Field> fields = new ArrayList<Field>();
		
		fields = new LinkedList<Field>(Arrays.asList(classDefinition.getDeclaredFields()));
		if(classDefinition.getSuperclass()!=null){
			fields.addAll(Arrays.asList(classDefinition.getSuperclass().getDeclaredFields()));
		}
		Map<String, Field> listType = new HashMap<String, Field>();
		
		for(Field field:fields){
			//Logger.log("[DDL] field.getName()=>"+field.getName());
			if(field.getName().equalsIgnoreCase("id")){
				continue;
			}
			String value = "TEXT NULL";

			if(field.getType().getName().equalsIgnoreCase("java.util.Date")){
				value = "TEXT NULL";
			}else if(field.getType().getName().equalsIgnoreCase("java.lang.String")){
				value = "TEXT NULL";
			}else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
				value = "INTEGER NULL";
			}else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
				value = "INTEGER NULL";
			}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
				value = "REAL NULL";
			}else if(field.getType().getName().equalsIgnoreCase("java.lang.Float")){
				value = "REAL NULL";
			}else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")){
				value = "INTEGER NULL";
			}else if(field.getType().getName().equalsIgnoreCase("java.util.List")){
				listType.put(field.getName(), field);
				continue;
			}else{
				value = "INTEGER NULL";
				if(!isTableExists(field.getType().getSimpleName()) && (parentClassDefinition==null || !field.getType().getName().equalsIgnoreCase(parentClassDefinition.getName()))){
					database.execSQL(createDatabase(field.getType().getSimpleName(),generatingColumns(field.getType(),classDefinition)));
				}
			}
			columns.put(field.getName(), value);
		}
		for(String key:listType.keySet()){
			Field field = listType.get(key);
			ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
	        Class<?> listTypeClass = (Class<?>) stringListType.getActualTypeArguments()[0];
	        if(!isTableExists(listTypeClass.getSimpleName())){
	        	Map<String,String> subColumns = generatingColumns(listTypeClass,classDefinition,new HashMap<String, String>());
	        	subColumns.put(classDefinition.getSimpleName().toLowerCase(), "INTEGER NULL");
				database.execSQL(createDatabase(listTypeClass.getSimpleName(),subColumns));
			}
		}
		return columns;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(createDatabase());
	}
	
	private boolean isOpen(){
		return database.isOpen();
	}
	
	private boolean isTableExists(){
		return isTableExists(tableName());
	}
	
	private boolean isTableExists(String tableName) {
        if(database == null || !database.isOpen()) {
        	database = getReadableDatabase();
        }

	    Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
	    if(cursor!=null) {
	        if(cursor.getCount()>0) {
	            cursor.close();
	            return true;
	        }
	        cursor.close();
	    }
		//close();
	    return false;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.log(this, "Upgrading database from version " + oldVersion + " to "
			+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + tableName());
		onCreate(db);
	}
	
	public abstract String tableName();
	
	public abstract Map<String,String> colums();
	
	public abstract Object cursorToImpl(Cursor cursor);
	
	private String createDatabase(){
		return createDatabase(tableName(),colums());
	}
	
	private synchronized String createDatabase(String localTableName,Map<String,String> localColumns){
		String ddl = "CREATE TABLE " + localTableName + "( ";
		ddl += ID + " INTEGER PRIMARY KEY AUTOINCREMENT ";
		
		if(localColumns!=null){
			Set<String> keys = localColumns.keySet();
			Iterator<String> i = keys.iterator();
			while (i.hasNext()){
		       String key = (String) i.next();
		       String value = (String) localColumns.get(key);
		       ddl += ", " + key + " " + value;
			}
		}
		
		ddl += ");";
		
		Logger.log(this,"[SQL_DDL] = "+ddl);
		
		return ddl;
	}
	
	protected String[] allColums(){
		String[] colums = new String[colums().size()+1];
		colums[0] = "id";
		int index = 1;
		if(colums()!=null){
			Set<String> keys = colums().keySet();
			Iterator<String> i = keys.iterator();
			while (i.hasNext()){
		       String key = (String) i.next();
		       colums[index] = key;
		       index++;
			}
			return colums;
		}
		return null;
	}

	public void open(boolean isTableExist) throws SQLException {
		if(!isTableExist){
			onCreate(database);
		}else{
			open();
		}
	}

	public void open() throws SQLException {
		this.database = getWritableDatabase();
	}
	

	public Object saveEntity(Object object) {
		return saveEntity(object, true);
	}
	
	public Object saveEntity(Object object, boolean immediately) {
		return saveEntity(object, immediately, tableName(),allColums());
	}
	
	private Object saveEntity(Object object, boolean immediately, String tableName, String[] columns){
		return saveEntity(object, immediately, tableName, columns, new ContentValues());
	}

	private Object saveEntity(Object object, boolean immediately, String tableName, String[] columns, ContentValues values) {
		
		if(object !=null){
			//ContentValues values = new ContentValues();
			
			List<Field> fields = new ArrayList<Field>();
			Map<String, Field> listType = new HashMap<String, Field>();
			
			fields = new LinkedList<Field>(Arrays.asList(object.getClass().getDeclaredFields()));
			if(object.getClass().getSuperclass()!=null){
				fields.addAll(Arrays.asList(object.getClass().getSuperclass().getDeclaredFields()));
			}
			
			for(Field field:fields){
				try {
					field.setAccessible(true);
					//ignore object id becouse it willbe auto generated
					if(field.getName().equalsIgnoreCase(ID) && field.get(object)==null){
						continue;
					
					//if object has id not null then just update that object 
					}else if(field.getName().equalsIgnoreCase(ID) && field.get(object)!=null){
						return updateEntity(object);
					}
					
					if(field.getName().equalsIgnoreCase(CREATED_ON)){
						field.set(object, new Date());
					}else if(field.getName().equalsIgnoreCase(UPDATED_ON) || field.get(object)==null){
						continue;
					}
					
					String columName = field.getName();
					if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
						//Logger.log(this, object.getClass().getSimpleName() + "columName ="+columName+" => INTEGER");
						Integer value = (Integer)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
						Long value = (Long)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Float")){
						Float value = (Float)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.String")){
						String value = (String)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.util.Date")){
						String value = dateFormat((Date)field.get(object));
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
						Double value = (Double)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")){
						Boolean value = (Boolean)field.get(object);
						if(value){
							values.put(columName,1);
						}else{
							values.put(columName,0);
						}
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.util.List")){
						listType.put(columName, field);
					}else{
						BaseEntity value = (BaseEntity)field.get(object);
						if(value.getId()==null){
							value = (BaseEntity)saveEntity(field.get(object), true, field.get(object).getClass().getSimpleName(), retrieveColumns(field.get(object).getClass()), new ContentValues());
						}
						values.put(columName,value.getId());
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value.getId()+"]");
						
					}
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			if(!isOpen() && immediately){
				open();
			}
			Long insertId = database.insert(tableName, null, values);
			/*Cursor cursor = database.query(tableName,
					columns, ID + " = " + insertId, null, null, null, null);
			cursor.moveToFirst();*/
			
			//try {
				//BaseEntity impl = (BaseEntity)cursorToObject(cursor, object.getClass(), object.getClass());
				//((BaseEntity)object).setId(impl.getId());
				((BaseEntity)object).setId(insertId.intValue());
				
				//cursor.close();
				
				for(String key:listType.keySet()){
					Field field = listType.get(key);
					try {
						List<?> value = (List<?>)field.get(object);
						for(Object val: value){
							ContentValues valuesChild = new ContentValues();
							valuesChild.put(classDefinition.getSimpleName().toLowerCase(), insertId);
							saveEntity(val, true, value.get(0).getClass().getSimpleName(), retrieveColumns(val.getClass()),valuesChild);
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				
				
				if(immediately){
					close();
				}
				//return impl;
				return object;
			/*} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}*/
		}
		if(immediately){
			close();
		}
		return null;
	}
	
	public Object getEntity(int id){
		return getEntity(classDefinition, allColums(), id);
	}
	
	@SuppressWarnings("unchecked")
	private Object getEntity(Class<?> classDefinition, String[] columns, int id){
		return getEntity(null, classDefinition, columns, id);
	}
	
	@SuppressWarnings("unchecked")
	private Object getEntity(Class<?> parentClassDefinition, Class<?> classDefinition, String[] columns, int id){
		List<Object> list = queryHelper(parentClassDefinition, classDefinition, columns,"id=?",new String[]{String.valueOf(id)});
		if(list.size()==1){
			return list.get(0);
		}
		return null;
	}
	
	protected Object cursorToObject(Cursor cursor, Class<?> classDefinition) throws InstantiationException, IllegalAccessException{
		return cursorToObject(cursor, classDefinition, null);
	}
	
	protected Object cursorToObject(Cursor cursor, Class<?> classDefinition, Class<?> parentClassDefinition) throws InstantiationException, IllegalAccessException{
		Object object = classDefinition.newInstance();
		
		if(object !=null && cursor!=null){
			//ContentValues values = new ContentValues();
			List<Field> fields = new ArrayList<Field>();
			Map<String, Field> listType = new HashMap<String, Field>();
			
			fields = new LinkedList<Field>(Arrays.asList(object.getClass().getDeclaredFields()));
			if(object.getClass().getSuperclass()!=null){
				fields.addAll(new LinkedList<Field>(Arrays.asList(object.getClass().getSuperclass().getDeclaredFields())));
			}
			
			for(Field field:fields){
				field.setAccessible(true);
				String columName = field.getName();
				//TODO: load list relationship
				if(cursor.isNull(cursor.getColumnIndex(columName)) && !field.getType().getName().equalsIgnoreCase("java.util.List")){
					continue;
				}
					
				if(field.getType().getName().equalsIgnoreCase("java.lang.String")){
					String value = cursor.getString(cursor.getColumnIndex(columName));
					//Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
					Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					field.set(object, value);
				}else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
					Long value = cursor.getLong(cursor.getColumnIndex(columName));
					//Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
					Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					field.set(object, value);
				}else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
					int value = cursor.getInt(cursor.getColumnIndex(columName));
					//Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
					Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					field.set(object, value);
				}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
					double value = cursor.getDouble(cursor.getColumnIndex(columName));
					//Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
					Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					field.set(object, value);
				}else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")){
					Boolean value = null;
					if(cursor.getInt(cursor.getColumnIndex(columName))==1){
						value = true;
					}else{
						value = false;
					}
					//Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
					Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					field.set(object, value);
				}else if(field.getType().getName().equalsIgnoreCase("java.util.Date")){
					String value = cursor.getString(cursor.getColumnIndex(columName));
					try {
						Date dateValue = dateFormat.parse(value);
						
						//Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+dateValue.toString()+"]");
						field.set(object, dateValue);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else if(field.getType().getName().equalsIgnoreCase("java.util.List")){
					listType.put(columName, field);
				}else {
					//if(parent!=null){
						Logger.log("["+parentClassDefinition+","+field.getType().getName()+"]");
					//}
					if(parentClassDefinition==null || !parentClassDefinition.getName().equalsIgnoreCase(field.getType().getName())){
						
						Integer value = cursor.getInt(cursor.getColumnIndex(columName));
						field.set(object, getEntity(classDefinition, field.getType(), retrieveColumns(field.getType()),value));
					}
				}
				field.setAccessible(false);
			}
			
			for(String key:listType.keySet()){
				Field field = listType.get(key);
				field.setAccessible(true);
				Integer id = ((BaseEntity)object).getId();
				ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
		        Class<?> listTypeClass = (Class<?>) stringListType.getActualTypeArguments()[0];
		        
				field.set(object, queryHelper(classDefinition, listTypeClass, retrieveColumns(listTypeClass), classDefinition.getSimpleName().toLowerCase() + "=?",new String[]{id.toString()}));
				field.setAccessible(false);
				Logger.log(this, object.getClass().getSimpleName() + " => ["+key+","+id+"]");
			}
		}
		return object;
	}
	
	private static String dateFormat(Date date){
		return dateFormat.format(date);
	}
	
	public Object updateEntity(Object object){
		
		int	rowsUpdated = 0;
		if(object !=null){
			ContentValues values = new ContentValues();
			Long id = 0l;
			
			List<Field> fields = new ArrayList<Field>();
			
			fields = new LinkedList<Field>(Arrays.asList(object.getClass().getDeclaredFields()));
			if(object.getClass().getSuperclass()!=null){
				fields.addAll(Arrays.asList(object.getClass().getSuperclass().getDeclaredFields()));
			}
			
			for(Field field:fields){
				field.setAccessible(true);
				try {
					String columName = field.getName();
					if(field.getName().equalsIgnoreCase(ID) && field.get(object)!=null){
						if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
							//Logger.log(this, object.getClass().getSimpleName() + "columName ="+columName+" => INTEGER");
							Integer value = (Integer)field.get(object);
							id = 1l * value;
						}else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
							id = (Long)field.get(object);
						}
						continue;
					}

					if(field.getName().equalsIgnoreCase(CREATED_ON)){
						continue;
					}

					if(field.getName().equalsIgnoreCase(UPDATED_ON)){
						field.set(object, new Date());
					}
					
					if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
						//Logger.log(this, object.getClass().getSimpleName() + "columName ="+columName+" => INTEGER");
						Integer value = (Integer)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
						Long value = (Long)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Float")){
						Float value = (Float)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.String")){
						String value = (String)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.util.Date")){
						String value = dateFormat((Date)field.get(object));
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
						Double value = (Double)field.get(object);
						values.put(columName,value);
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")){
						Boolean value = (Boolean)field.get(object);
						if(value){
							values.put(columName,1);
						}else{
							values.put(columName,0);
						}
						Logger.log(this, object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
					}
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			if(!isOpen()){
				open();
			}
			Logger.log("update id=>"+id);
			rowsUpdated = database.update(tableName(),values,ID + "=" + id , null);
			close();
		}
		close();
		Logger.log(this, "update row count => "+rowsUpdated);
		return object;
	}
	
	public void deleteEntity(Object object){
		Field field = null;
		Long id = 0l;
		try {
			try{
				field = object.getClass().getDeclaredField(ID);
			}catch(NoSuchFieldException e){
				e.printStackTrace();
			}
			if(field==null && object.getClass().getSuperclass()!=null){
				field = object.getClass().getSuperclass().getDeclaredField(ID);
			}
			field.setAccessible(true);
			if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
				//Logger.log(this, object.getClass().getSimpleName() + "columName ="+columName+" => INTEGER");
				id = (Integer)field.get(object) * 1l;
			}else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
				id = (Long)field.get(object);
			}
			//id = field.getLong(object); 
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if(!isOpen()){
			open();
		}
		database.delete(tableName(), ID + " = " + id, null);
		close();
	}
	
	public int deleteEntity(String whereClause,String[] whereArgs){
		if(!isOpen()){
			open();
		}
		int result = database.delete(tableName(), whereClause, whereArgs);
		close();
		return result; 
	}
	
	public int deleteAll(){
		if(!isOpen()){
			open();
		}
		int result = database.delete(tableName(), null, null);
		close();
		return result; 
	}
	
	public Long queryHelperCount(){		
		return queryHelperCount("");
	}
	
	public Long queryHelperCount(String selection){
		if(selection.equals(null)){
			selection = "";
		}
		if(!isOpen()){
			open();
		}
		Long count = DatabaseUtils.longForQuery(database, "SELECT COUNT(*) FROM " + tableName() + " " + selection, null);
		close();
		return count;
	}
	
	public Long queryHelperMax(String columnName,String selection){
		if(selection==null){
			selection = "";
		}
		if(!isOpen()){
			open();
		}
		Long count = DatabaseUtils.longForQuery(database, "SELECT MAX("+columnName+") FROM " + tableName() + " " + selection, null);
		close();
		return count;
	}
	
	public Long queryHelperSum(String columnName,String selection){
		if(selection==null){
			selection = "";
		}
		if(!isOpen()){
			open();
		}
		Long count = DatabaseUtils.longForQuery(database, "SELECT SUM(?) FROM " + tableName() + " " + selection, new String[]{columnName});
		close();
		return count;
	}
	
	@SuppressWarnings("rawtypes")
	public List queryHelper(String[] colunms){
		return queryHelper(tableName(),colunms);
	}
	
	@SuppressWarnings("rawtypes")
	public List queryHelper(String tableName,String[] colunms){
		if(!isOpen()){
			open();
		}
		Cursor cursor = database.query(tableName, colunms, null, null, null, null, null);
		List<Object> list = new ArrayList<Object>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			list.add(cursorToImpl(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		
		return list;
	}
	
	/*public List retievingCOllection(List list){
		try{
			for(Object object:list){
				List<Field> fields = new ArrayList<Field>();
				
				fields = new LinkedList<Field>(Arrays.asList(object.getClass().getDeclaredFields()));
				if(object.getClass().getSuperclass()!=null){
					fields.addAll(Arrays.asList(object.getClass().getSuperclass().getDeclaredFields()));
				}
				
				for(Field field:fields){
					field.setAccessible(true);
					if(field.getType().getName().equalsIgnoreCase("java.util.List")){
						Integer id = ((BaseEntity)object).getId();
						ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
				        Class<?> listTypeClass = (Class<?>) stringListType.getActualTypeArguments()[0];
				        Logger.log(this, object.getClass().getSimpleName() + " => ["+field.getName()+","+id+"] "+listTypeClass.getSimpleName());
						field.set(object, queryHelper(listTypeClass, retrieveColumns(listTypeClass), object.getClass().getSimpleName().toLowerCase() + "=?",new String[]{id.toString()}));
						field.setAccessible(false);
						break;
					}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return list;
	}*/
	
	@SuppressWarnings("rawtypes")
	public List queryHelper(String[] columns, String selection, String[] selectionArgs){
		return queryHelper(classDefinition, columns, selection, selectionArgs);
	}
	
	@SuppressWarnings("rawtypes")
	private List queryHelper(Class<?> classDefinition, String[] columns, String selection, String[] selectionArgs){
		return queryHelper(null, classDefinition, columns, selection, selectionArgs);
	}
	
	@SuppressWarnings("rawtypes")
	private List queryHelper(Class<?> parentClassDefinition, Class<?> classDefinition, String[] columns, String selection, String[] selectionArgs){
		
		if(!isOpen()){
			open();
		}
		Logger.log("select * from "+classDefinition.getSimpleName() + " where "+selection);
		Cursor cursor = database.query(classDefinition.getSimpleName(), columns, selection, selectionArgs, null, null, null);
		
		List<Object> list = new ArrayList<Object>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			try {
				list.add(cursorToObject(cursor,classDefinition,parentClassDefinition));
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return list;
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	private List queryHelperUniqResult(Class<?> parentClassDefinition, Class<?> classDefinition, String[] columns, String selection, String[] selectionArgs){
		
		if(!isOpen()){
			open();
		}
		Logger.log("select * from "+classDefinition.getSimpleName() + " where "+selection);
		Cursor cursor = database.query(classDefinition.getSimpleName(), columns, selection, selectionArgs, null, null, null);
		
		List<Object> list = new ArrayList<Object>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			try {
				list.add(cursorToObject(cursor,classDefinition,parentClassDefinition));
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return list;
	}
	
	@SuppressWarnings("rawtypes")
	public List queryHelper(String[] columns, String selection, String[] selectionArgs,String orderBy){
		if(!isOpen()){
			open();
		}
		Cursor cursor = database.query(tableName(), columns, selection, selectionArgs, null, orderBy, null);
		List<Object> list = new ArrayList<Object>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			list.add(cursorToImpl(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return list;
	}
	
	public Boolean truncate(){
		if(!isOpen()){
			open();
		}
		Integer result = 0;
		try{
			result = database.delete(tableName(), null, null);
		}catch (NullPointerException e) {
			e.printStackTrace();
		}
		close();
		return result ==1 ? true : false;
	}
	
	@Override
	public synchronized void close(){
		try{
			database.close();
			super.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	public SQLiteDatabase getDatabase() {
		return database;
	}


	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

}
