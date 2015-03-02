package com.houseelectrics.orm.test;

import com.houseelectrics.orm.SqliteDatabaseServiceJDBCImpl;
import org.junit.Test;

/**
 * Created by roberttodd on 25/02/2015.
 */
public class SqliteJDBCLazyLoadingTest extends SqliteJDBCTestBase
{
    @Test
    public void testLazyLoading() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =  createCleanTempDb("testLazyLoading");
        (new LazyLoadingTest()).testLazyLoading(asserter, db, new CGLibProxyFactory());
    }
}
