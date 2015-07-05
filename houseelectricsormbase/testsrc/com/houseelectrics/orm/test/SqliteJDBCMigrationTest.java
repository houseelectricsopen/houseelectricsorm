package com.houseelectrics.orm.test;

import com.houseelectrics.orm.SqliteDatabaseServiceJDBCImpl;
import org.junit.Test;

/**
 * Created by roberttodd on 21/06/2015.
 */
public class SqliteJDBCMigrationTest extends SqliteJDBCTestBase
{
    @Test
    public void testAddField() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =  createCleanTempDb("testAddField");
        (new MigrationTest()).testAddField(asserter, db);
    }

    @Test
    public void testAddReference() throws Exception
    {
        SqliteDatabaseServiceJDBCImpl db =  createCleanTempDb("testAddReference");
        (new MigrationTest()).testAddReference(asserter, db);


    }


}
