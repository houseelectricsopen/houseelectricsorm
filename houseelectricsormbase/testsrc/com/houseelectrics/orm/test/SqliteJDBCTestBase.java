package com.houseelectrics.orm.test;

import com.houseelectrics.orm.SqliteDatabaseServiceJDBCImpl;
import org.junit.Assert;

import java.io.File;

/**
 * Created by roberttodd on 25/02/2015.
 */
public class SqliteJDBCTestBase
{
    final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));

    public SqliteDatabaseServiceJDBCImpl createCleanTempDb(String name)
    {
        File ftempdir = new File(sysTempDir + File.separator + "crudtest");
        ftempdir.mkdir();
        String dbFilename = ftempdir.getPath() +  File.separator + name ;
        SqliteDatabaseServiceJDBCImpl db = SqliteDatabaseServiceJDBCImpl.createCleanDb  (dbFilename);
        System.out.println("created db " + dbFilename);
        return db;
    }

    Asserter asserter = new Asserter()
    {
        @Override
        public void assertEqual(Object expected, Object actual)
        {
            Assert.assertEquals(expected, actual);
        }

        @Override
        public void assertEqual(String message, Object expected, Object actual)
        {
            Assert.assertEquals(message, expected, actual);
        }
    };
}
