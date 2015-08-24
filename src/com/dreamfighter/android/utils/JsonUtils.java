package com.dreamfighter.android.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.dreamfighter.android.log.Logger;


public class JsonUtils {
        
        public static <T> T parse(JSONObject jsonObject, Class<T> classDefinition) 
                throws ClassNotFoundException, InstantiationException, IllegalAccessException, JSONException{
            Object o = jsonToClassMapping(jsonObject,classDefinition);
            return classDefinition.cast(o) ;
        }
        
        public static Object jsonToClassMapping(JSONObject jsonObject, Class<?> classDefinition) 
                throws ClassNotFoundException, InstantiationException, IllegalAccessException, JSONException{
            //Class<?> classDefinition = Class.forName(className.getName());
            Object object = classDefinition.newInstance();
            
            if(object !=null){
                //ContentValues values = new ContentValues();
                List<Field> fields = new ArrayList<Field>();
                
                fields = new LinkedList<Field>(Arrays.asList(classDefinition.getDeclaredFields()));
                if(classDefinition.getSuperclass()!=null){
                    fields.addAll(Arrays.asList(classDefinition.getSuperclass().getDeclaredFields()));
                }
                
                for(Field field:fields){
                    field.setAccessible(true);
                    String columName = field.getName();
                    if(jsonObject.isNull(columName)){
                        continue;
                    }
                        
                    if(field.getType().getName().equalsIgnoreCase("java.lang.String")){
                        String value = jsonObject.get(columName).toString();
                        //Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        field.set(object, value);
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
                        int value = jsonObject.getInt(columName);
                        //Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        field.set(object, value);
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
                        double value = jsonObject.getDouble(columName);
                        //Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        field.set(object, value);
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
                        Long value = jsonObject.getLong(columName);
                        //Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        field.set(object, value);
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")){
                        boolean value = jsonObject.getBoolean(columName);
                        //Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => String");
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        field.set(object, value);
                    }else if(field.getType().getDeclaredFields().length>0){
                        //Logger.log("JsonUtils", object.getClass().getSimpleName() + " columName = "+columName+" => " + field.getType());
                        Object value = jsonToClassMapping(jsonObject.getJSONObject(columName),field.getType());
                        field.set(object, value);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                    }
                    field.setAccessible(false);
                }
            }
            return object;
        }
        
        public static String objectToJson(Object object) 
                throws IllegalAccessException{
            //Class<?> classDefinition = Class.forName(className.getName());
            StringBuilder json = new StringBuilder();
            json.append("{");
            if(object !=null){
                int i = 0;
                //ContentValues values = new ContentValues();
                for(Field field:object.getClass().getDeclaredFields()){
                    field.setAccessible(true);
                    String columName = field.getName();
                    if(i!=0){
                        json.append(",");
                    }
                    if(field.getType().getName().equalsIgnoreCase("java.lang.String")){
                        Object value = field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        json.append("\""+columName+"\":\""+value+"\"");
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
                        Integer value = (Integer)field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        json.append("\""+columName+"\":\""+value+"\"");
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
                        double value = (Double)field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        json.append("\""+columName+"\":\""+value+"\"");
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Long")){
                        Long value = (Long)field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        json.append("\""+columName+"\":\""+value+"\"");
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")){
                        boolean value = (Boolean)field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        json.append("\""+columName+"\":\""+value+"\"");
                    }else if(field.getType().getDeclaredFields().length>0){
                        String value = objectToJson(field.get(object));
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        json.append("\""+columName+"\":"+value);
                    }
                    i++;                
                }
            }
            json.append("}");
            return json.toString();
        }
        
        public static JSONObject updateJsonObject(JSONObject jsonObject,Object object) 
                throws IllegalAccessException, JSONException{
            //Class<?> classDefinition = Class.forName(className.getName());
           
            if(object !=null){
                //ContentValues values = new ContentValues();
                for(Field field:object.getClass().getDeclaredFields()){
                    field.setAccessible(true);
                    String columName = field.getName();
                    if(field.getType().getName().equalsIgnoreCase("java.lang.String")){
                        Object value = field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        jsonObject.put(columName, value);
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")){
                        int value = (Integer)field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        jsonObject.put(columName, value);
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")){
                        double value = (Double)field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        jsonObject.put(columName, value);
                    }else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")){
                        boolean value = (Boolean)field.get(object);
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        jsonObject.put(columName, value);
                    }else if(field.getType().getDeclaredFields().length>0){
                        JSONObject value = updateJsonObject(jsonObject.getJSONObject(columName),field.get(object));
                        Logger.log("JsonUtils", object.getClass().getSimpleName() + " => ["+columName+","+value+"]");
                        jsonObject.put(columName, value);
                    }           
                }
            }
            
            return jsonObject;
        }
}
