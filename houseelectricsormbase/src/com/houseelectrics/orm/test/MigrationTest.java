package com.houseelectrics.orm.test;

import com.houseelectrics.orm.*;
import com.houseelectrics.orm.test.migrationtest.domainmodelv0.ReferenceTestRoot;
import com.houseelectrics.orm.test.migrationtest.domainmodelv1.Root;
import com.houseelectrics.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 20/06/2015.
 */
public class MigrationTest extends DbTestBase
{
    SqliteDatabaseService.Logger rerouteLoggerToCatchSqls(SqliteDatabaseService db, final List<String> sqls)
    {
        final SqliteDatabaseService.Logger logger =db.getLogger();
        SqliteDatabaseService.Logger rerootedLogger = new SqliteDatabaseService.Logger()
        {
            @Override
            public void info(String message)
            {
                logger.info(message);
            }

            @Override
            public void error(String message)
            {
                logger.error(message);
            }

            @Override
            public void error(String message, Throwable error)
            {
                logger.error(message, error);
            }

            @Override
            public void sql(String sql)
            {
                sqls.add(sql);
                System.out.println(sql);
            };
        };
        db.setLogger(rerootedLogger);
        return rerootedLogger;
    }

    public void testAddField(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        final List<String> sqls = new ArrayList<String>();
        rerouteLoggerToCatchSqls(db, sqls);

        com.houseelectrics.orm.test.migrationtest.domainmodelv0.Root v0Root = new com.houseelectrics.orm.test.migrationtest.domainmodelv0.Root();
        v0Root.setFieldA("fieldA");
        v0Root.setFieldB(321);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(v0Root.getClass().getPackage());

        context = (new ReflectionDbContextFactory()).createFromRootClass(v0Root.getClass(), referenceTypeDetector);

        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();

        context.add(v0Root);
        context.saveChanges();

        context = null;

        Root v1Root = new Root();
        context = (new ReflectionDbContextFactory()).createFromRootClass(v1Root.getClass(), referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();

        Exception theex=null;
        List<Object> roots=null;
        try
        {
            roots = context.getAllByTypeShallow(v1Root.getClass());
        }
        catch (Exception ex)
        {
        theex = ex;
        }

        String newFieldNames[] = {"FieldC", "FieldD"};
        String tableName = Root.class.getSimpleName();

        if (sqls.size()>0)
        {
            System.out.println("last sql " + sqls.get(sqls.size()-1));
        }

        Assert.assertEqual("schema should fail ", theex != null, true);
        int fieldsNamedInEx = 0;
        StringBuffer sbNewFields = new StringBuffer();

        String exMessage = theex.getMessage();
        if (theex.getCause()!=null) exMessage  = exMessage + " " + theex.getCause().getMessage();

        for (int done=0 ; done<newFieldNames.length; done++)
        {
            String fieldName = newFieldNames[done];
            if (exMessage.contains(fieldName)) fieldsNamedInEx++;
            sbNewFields.append(fieldName+ " ");
        }

        Assert.assertEqual("new field name " + sbNewFields.toString() + "should be named in exception: " + exMessage,  fieldsNamedInEx>0, true);
        //Assert.assertEqual("table name " + tableName + " should be named in exception: " + exMessage,  fieldsNamedInEx++);

        context.createSchemaIfNotExistsAddNewFieldsToExistingTables();

        roots = context.getAllByTypeShallow(v1Root.getClass());
        Assert.assertEqual("retrieved modified object", roots.size(), 1);

    }

    public void testAddReference(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        final List<String> sqls = new ArrayList<String>();
        rerouteLoggerToCatchSqls(db, sqls);

        ReferenceTestRoot v0Root = new com.houseelectrics.orm.test.migrationtest.domainmodelv0.ReferenceTestRoot();
        v0Root.setFieldA("fieldA");

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector;
        referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(v0Root.getClass().getPackage());

        context = (new ReflectionDbContextFactory()).createFromRootClass(v0Root.getClass(), referenceTypeDetector);

        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();

        context.add(v0Root);
        context.saveChanges();

        context = null;

        com.houseelectrics.orm.test.migrationtest.domainmodelv1.ReferenceTestRoot v1Root = new com.houseelectrics.orm.test.migrationtest.domainmodelv1.ReferenceTestRoot();
        referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(v1Root.getClass().getPackage());
        context = (new ReflectionDbContextFactory()).createFromRootClass(v1Root.getClass(), referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();

        Exception theex=null;
        List<Object> roots=null;
        try
        {
            roots = context.getAllByTypeShallow(v1Root.getClass());
        }
        catch (Exception ex)
        {
            theex = ex;
        }

        String newReferenceFieldname = "SubItem";


        if (sqls.size()>0)
        {
            System.out.println("last sql " + sqls.get(sqls.size()-1));
        }


        Assert.assertEqual("schema should fail ", theex != null, true);

        String exMessage = theex.getMessage();
        if (theex.getCause()!=null) exMessage = exMessage + " " + theex.getCause().getMessage();

        Assert.assertEqual("new field name " + newReferenceFieldname + "should be named exception: " + exMessage,  exMessage.indexOf(newReferenceFieldname)>=0, true);

        context.createSchemaIfNotExistsAddNewFieldsToExistingTables();

        roots = context.getAllByTypeShallow(v1Root.getClass());
        Assert.assertEqual("retrieved modified object", roots.size(), 1);
        v1Root = (com.houseelectrics.orm.test.migrationtest.domainmodelv1.ReferenceTestRoot) roots.get(0);
        Assert.assertEqual("retrieved modified object", v0Root.getFieldA(), v1Root.getFieldA());


    }


}
