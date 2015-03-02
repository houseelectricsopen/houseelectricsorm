package com.houseelectrics.orm.test;
import com.houseelectrics.orm.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 04/01/2015.
 */
public class SqliteJDBCCrudTest extends SqliteJDBCTestBase
{

    @Test
    public void crudTestSimple() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =  createCleanTempDb("crudTestSimple");
        (new CrudTest()).crudTestSimpleFromEmptyDb(asserter, db);
    }

    @Test
    public void crudTestWithOrderFromEmptyDb() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("crudTestWithOrderFromEmptyDb");
        (new CrudTest()).crudTestWithOrderFromEmptyDb(asserter, db);
    }

    @Test
    public void crudTestDetail() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("crudTestDetail");
        (new CrudTest()).crudWithDetailTestFromEmptyDb(asserter, db);
    }

    @Test
    public void crudTestList() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("crudTestList");
        (new CrudTest()).crudWithListProperty(asserter, db);
    }

    @Test
    public void testRawSelect() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testRawSelect");
        (new CrudTest()).testRawSelect(asserter, db);
    }

    @Test
    public void testRawSelectSingleColumn() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testRawSelectSingleColumn");
        (new CrudTest()).testRawSelectSingleColumn(asserter, db);
    }

    @Test
    public void testRawSelectDeep() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testRawSelectDeep");
        (new CrudTest()).testRawSelectDeep(asserter, db);
    }

    @Test
    public void testSubClass() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testSubClass");
        (new CrudTest()).testSubClass(asserter, db);
    }

    @Test
    public void testPropertyPersistenceFilter() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testPropertyPersistenceFilter");
        (new CrudTest()).testPropertyPersistenceFilter(asserter, db);
    }

    @Test
    public void testCustomTypeMappings() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testCustomTypeMappings");
        SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter  converter;
        converter = createStringArrayMapping();
        db.addHighPriorityMapping(converter);
        converter = createIntListMapping();
        db.addHighPriorityMapping(converter);
        converter = createStringListMapping();
        db.addHighPriorityMapping(converter);

        (new CrudTest()).testCustomTypeMappings(asserter, db);
    }

    @Test
    public void testDeepLoadWithEmptyDetail() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testDeepLoadWithEmptyDetails");
        (new CrudTest()).testDeepLoadWithEmptyDetail(asserter, db);
    }

    @Test
    public void testRepeatedShallowLoad() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testRepeatedShallowLoad");
        (new CrudTest()).testRepeatedShallowLoad(asserter, db);
    }

    @Test
    public void testDeepLoadAlreadyPopulated() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testDeepLoadWithEmptyDetails");
        (new CrudTest()).testDeepLoadAlreadyPopulated(asserter, db);
    }

    @Test
    public void testMultipleRoots() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testMultipleRoots");
        (new CrudTest()).testMultipleRoots(asserter, db);
    }

    @Test
    public void testPolymorphicReference() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testPolymorphicReference");
        (new CrudTest()).testPolymorphicReference(asserter, db);
    }

    @Test
    public void testPolymorphicNullReference() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testPolymorphicNullReference");
        (new CrudTest()).testPolymorphicNullReference(asserter, db);
    }

    @Test
    public void testPolymorphicListReferences() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testPolymorphicListReferences");
        (new CrudTest()).testPolymorphicListReferences(asserter, db);
    }

    @Test
    public void testPolymorphicStackReferences() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =createCleanTempDb("testPolymorphicListReferences");
        (new CrudTest()).testPolymorphicStackReferences(asserter, db);
    }

    SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter createStringArrayMapping()
    {
        SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter
                converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null)
                {
                    String strs[]=null;
                    int stringCount=0;

                        strs = (String[])value;
                        stringCount=strs.length;
                    StringBuffer sb = new StringBuffer();
                    for (int done=0; done<stringCount; done++)
                    {
                        if (done!=0) sb.append(',');
                        sb.append(strs[done]);
                    }
                    ps.setString(zeroIndex + 1, sb.toString());
                }
                else ps.setNull(zeroIndex+1, Types.VARCHAR);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                String str = rs.getString(zeroIndex+1);
                return str==null?null:str.split(",");
            }
            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }

            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)
            {
                return type==String[].class;
            }
        };
        return converter;
    }

    SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter createIntListMapping()
    {
        SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter
                converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null)
                {
                    List<Integer> l = null;
                        l = (List<Integer>)value;

                    StringBuffer sb = new StringBuffer();
                    for (int done=0; done<l.size(); done++)
                    {
                        if (done!=0) sb.append(',');
                        sb.append(l.get(done));
                    }
                    ps.setString(zeroIndex + 1, sb.toString());
                }
                else ps.setNull(zeroIndex+1, Types.VARCHAR);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                String str = rs.getString(zeroIndex+1);
                if (str==null) {return null;}
                String []strs = str.split(",");
                List<Integer> result = new ArrayList<Integer>();
                for (int done=0; done<strs.length; result.add(Integer.parseInt(strs[done])), done++) {}
                return result;
            }
            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }

            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)
            {
                return  type==List.class && genericTypeParamers!=null
                        && genericTypeParamers.length>0 && genericTypeParamers[0]==Integer.class;
            }
        };
        return converter;
    }

    SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter createStringListMapping()
    {
        SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter
                converter = new SqliteDatabaseServiceJDBCImpl.JdbcSqliteTypeConverter()
        {
            @Override  public void write(PreparedStatement ps, Object value, int zeroIndex)  throws SQLException
            {
                if (value!=null)
                {
                    List<String> l = null;
                    l = (List<String>)value;

                    StringBuffer sb = new StringBuffer();
                    for (int done=0; done<l.size(); done++)
                    {
                        if (done!=0) sb.append(',');
                        sb.append(l.get(done));
                    }
                    ps.setString(zeroIndex + 1, sb.toString());
                }
                else ps.setNull(zeroIndex+1, Types.VARCHAR);
            }
            @Override  public Object read(ResultSet rs, int zeroIndex)  throws SQLException
            {
                String str = rs.getString(zeroIndex+1);
                if (str==null) {return null;}
                String []strs = str.split(",");
                List<String> result = new ArrayList<String>();
                for (int sd=0; sd<strs.length; result.add(strs[sd]), sd++) {}
                return result;
            }
            @Override
            public String getSqlitePropertyType()  {  return "TEXT"; }

            @Override
            public boolean matches(Class type, Type[] genericTypeParamers)
            {
                return  type==List.class && genericTypeParamers!=null
                        && genericTypeParamers.length>0 && genericTypeParamers[0]==String.class;
            }
        };
        return converter;
    }


}
