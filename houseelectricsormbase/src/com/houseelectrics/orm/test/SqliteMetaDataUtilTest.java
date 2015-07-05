package com.houseelectrics.orm.test;

import com.houseelectrics.orm.DbContext;
import com.houseelectrics.orm.ReflectionDbContextFactory;
import com.houseelectrics.orm.SqliteDatabaseService;
import com.houseelectrics.orm.SqliteMetaDataUtil;
import junit.framework.Assert;
import org.junit.Test;

import java.text.ParseException;

/**
 * Created by roberttodd on 21/06/2015.
 */
public class SqliteMetaDataUtilTest extends DbTestBase
{
    public void testExtractFromSqliteMasterSql(Asserter asserter) throws ParseException
    {
        SqliteMetaDataUtil util = new SqliteMetaDataUtil();
        String strInfo = "CREATE TABLE Root(   \r\nFieldB INTEGER,   \r\nFieldA TEXT)";
        SqliteMetaDataUtil.TableMetaData tmd =  util.extractTableMetadataFromSqliteMasterSql(strInfo);
        asserter.assertEqual("table name ", tmd.name, "Root");
        asserter.assertEqual("FieldB", tmd.name2Column.containsKey("FieldB"), true);
        asserter.assertEqual("FieldA", tmd.name2Column.containsKey("FieldA"), true);
    }

    public void testExtractTableMetaData(Asserter asserter, SqliteDatabaseService db) throws Exception
    {
        SqliteMetaDataUtil util = new SqliteMetaDataUtil();
        DbContext context = createContext(CrudTest.SimpleTestType.class, db);
        context.createSchemaIfNotExists();
        SqliteMetaDataUtil.TableMetaData tmd;
        tmd = util.extractTableMetadataNullIfNotExists(db, "nonExistant");
        asserter.assertEqual("table data should not be available", tmd, null);

        //util.extractTableMetadataNullIfNotExists("")

    }


}
