package com.houseelectrics.orm;

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
public class JdbcSqliteTypeConverterFactory
{
    protected void createDefaultTypeConverters(List<SqliteDatabaseService.FieldTypeMapping> typeConverters)
    {
        SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter converter;

        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setBoolean(zeroIndex+1, (Boolean)value);
                else ps.setNull(zeroIndex+1, Types.BOOLEAN);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                return rs.getBoolean(zeroIndex+1);
            }

            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }

            @Override
            public boolean matches(Class type, Type[] genericTypeParamers) { return type==Boolean.class || type==Boolean.TYPE;    }

        };

        typeConverters.add(converter);

        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setLong(zeroIndex + 1, (Long) value);
                else ps.setNull(zeroIndex+1, Types.INTEGER);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)
                    throws SQLException
            {
                Long result = rs.getLong(zeroIndex + 1);
                return rs.wasNull()?null:result;
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class type, Type[] genericTypeParamers) {  return type==Long.class || type==Long.TYPE; }

        };
        typeConverters.add(converter);


        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setInt(zeroIndex + 1, (Integer) value);
                else ps.setNull(zeroIndex+1, Types.INTEGER);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                return rs.getInt(zeroIndex + 1);
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)
            {         return type==Integer.class || type==Integer.TYPE;            }
        };
        typeConverters.add(converter);

        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setShort(zeroIndex + 1, (Short) value);
                else ps.setNull(zeroIndex+1, Types.INTEGER);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                return rs.getShort(zeroIndex + 1);
            }
            @Override
            public String getSqlitePropertyType()  {  return "INTEGER"; }
            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)   { return type==Short.class || type==Short.TYPE; }
        };
        typeConverters.add(converter);

        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setFloat(zeroIndex + 1, (Float) value);
                else ps.setNull(zeroIndex+1, Types.FLOAT);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                return rs.getFloat(zeroIndex + 1);
            }
            @Override
            public String getSqlitePropertyType()  {  return "REAL"; }
            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)   { return type==Float.class || type==Float.TYPE; }

        };
        typeConverters.add(converter);

        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setDouble(zeroIndex + 1, (Double) value);
                else ps.setNull(zeroIndex+1, Types.DOUBLE);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                return rs.getDouble(zeroIndex + 1);
            }
            @Override
            public String getSqlitePropertyType()  {  return "REAL"; }
            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)   { return type==Double.class || type==Double.TYPE; }
        };
        typeConverters.add(converter);

        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setString(zeroIndex + 1, (String) value);
                else ps.setNull(zeroIndex+1, Types.VARCHAR);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                return rs.getString(zeroIndex + 1);
            }
            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }
            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)   { return type==String.class ; }
        };
        typeConverters.add(converter);

        /*
        stores dates as epoch millisecond time - view in db with:
        SELECT  datetime(DateField/1000, 'unixepoch')
         */
        converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null) ps.setDate(zeroIndex + 1, new java.sql.Date(((Date)value).getTime()));
                else ps.setNull(zeroIndex+1, Types.VARCHAR);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                Date sqlDate = rs.getDate(zeroIndex + 1);
                return sqlDate==null?null:new Date(sqlDate.getTime());
            }
            @Override
            public String getSqlitePropertyType()  {  return "INT"; }
            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)   { return type==Date.class; }
        };
        typeConverters.add(converter);

    }
}
