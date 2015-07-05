package com.houseelectrics.orm.android;

import android.content.ContentValues;
import android.database.Cursor;
import com.houseelectrics.orm.SqliteDatabaseService;


import java.io.File;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by roberttodd on 09/01/2015.
 */
public class AndroidSqliteTypeConverterFactory
{

    protected void createDefaultTypeConverters(List<SqliteDatabaseService.FieldTypeMapping>  fieldTypeMappings)
    {
        SqliteDatabaseAndroidService.AndroidSqliteTypeConverter converter;
        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                contentValues.put(propertyName, (Boolean) value);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                boolean val = !rs.isNull(zeroIndex) && rs.getInt(zeroIndex)>0;
                return val;
            }

            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }

            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==Boolean.class || aClass==Boolean.TYPE;  }
        };
        fieldTypeMappings.add(converter);

        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                contentValues.put(propertyName, (Short) value);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                short val = rs.isNull(zeroIndex)?0:rs.getShort(zeroIndex);
                return val;
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==Short.class || aClass==Short.TYPE;  }
        };
        fieldTypeMappings.add(converter);


        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                contentValues.put(propertyName, (Integer) value);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                int val = rs.isNull(zeroIndex)?0:rs.getInt(zeroIndex);
                return val;
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==Integer.class || aClass==Integer.TYPE;  }
        };
        fieldTypeMappings.add(converter);


        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                contentValues.put(propertyName, (Long) value);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                Long val = rs.isNull(zeroIndex)?null:rs.getLong(zeroIndex);
                return val;
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==Long.class || aClass==Long.TYPE;  }

        };
        fieldTypeMappings.add(converter);

        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                contentValues.put(propertyName, (Float) value);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                float val = rs.isNull(zeroIndex)?0:rs.getFloat(zeroIndex);
                return val;
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==Float.class || aClass==Float.TYPE;  }
        };
        fieldTypeMappings.add(converter);


        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                contentValues.put(propertyName, (Double) value);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                double val = rs.isNull(zeroIndex)?0:rs.getDouble(zeroIndex);
                return val;
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==Double.class || aClass==Double.TYPE;  }
        };
        fieldTypeMappings.add(converter);

        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                contentValues.put(propertyName, (String) value);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                String val = rs.isNull(zeroIndex)?null:rs.getString(zeroIndex);
                return val;
            }
            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==String.class;  }
        };
        fieldTypeMappings.add(converter);

        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override   public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                File file = (File)value;
                String str = file==null?null:file.getPath();
                contentValues.put(propertyName, str);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                File file = null;
                String str = rs.isNull(zeroIndex)?null:rs.getString(zeroIndex);
                if (str!=null) file = new File(str);
                return file;
            }
            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==File.class;  }
        };
        fieldTypeMappings.add(converter);
           /*
        stores dates as epoch millisecond time - view in db with:
        SELECT  datetime(DateField/1000, 'unixepoch')
         */
        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override  public void write(ContentValues contentValues, String propertyName,  Object value)
            {
                long time = ((Date)value).getTime();
                contentValues.put(propertyName, time);
            }
            @Override  public Object read(Cursor rs, int zeroIndex)
            {
                Long lDate = rs.isNull(zeroIndex)?null:rs.getLong(zeroIndex);
                Date val = lDate==null?null:new Date(lDate);
                return val;
            }
            @Override
            public String getSqlitePropertyType()  {  return "INT"; }
            @Override
            public boolean matches(Class aClass, Type[] types)    { return aClass==Date.class;  }
        };
        fieldTypeMappings.add(converter);

        converter = new SqliteDatabaseAndroidService.AndroidSqliteTypeConverter()
        {
            @Override
            public void write(ContentValues contentValues, String propertyName, Object value)
            {
                Class clValue = (Class) value;
                String strValue = null;
                if (value!=null)
                {
                    strValue = clValue.getName();
                }
                contentValues.put(propertyName, (String)strValue);
            }

            @Override
            public Object read(Cursor cursor, int zeroIndex)
            {
                String strClass =  cursor.getString(zeroIndex);
                if (strClass==null || strClass.trim().length()==0)
                {
                    return null;
                }
                else
                {
                    Class result;
                    try
                    {
                        result = Class.forName(strClass);
                    } catch (Exception ex)
                    {
                        throw new RuntimeException("unable to read class ", ex);
                    }

                    return result;
                }
            }
            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }

            @Override
            public boolean matches(Class type, Type[] genericTypeParameters)
            {
                return  type==Class.class;
            }

        };
        fieldTypeMappings.add(converter);




    }
}
