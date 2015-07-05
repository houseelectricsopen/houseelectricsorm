package com.houseelectrics.orm.test;

import com.houseelectrics.orm.SqliteDatabaseServiceJDBCImpl;
import org.junit.Test;

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
public class SqliteJDBCMetaDataUtilTest extends SqliteJDBCTestBase
{

    @Test
    public void testExtractFromSqliteMasterSql() throws Exception
    {
        //SqliteDatabaseServiceJDBCImpl db =  createCleanTempDb("metdataUtil");
        (new SqliteMetaDataUtilTest()).testExtractFromSqliteMasterSql(asserter);
    }

    @Test
    public void testExtractFromTableMetaData() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =  createCleanTempDb("metdataUtil");
        (new SqliteMetaDataUtilTest()).testExtractTableMetaData(asserter, db);
    }


}
