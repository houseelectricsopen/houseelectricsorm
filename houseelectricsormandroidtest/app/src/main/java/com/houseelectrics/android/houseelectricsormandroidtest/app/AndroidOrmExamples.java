package com.houseelectrics.android.houseelectricsormandroidtest.app;

import android.app.Activity;
import com.houseelectrics.orm.DbContext;
import com.houseelectrics.orm.ReferenceTypeDetectorUtil;
import com.houseelectrics.orm.ReflectionDbContextFactory;
import com.houseelectrics.orm.android.AndroidDbUtil;
import com.houseelectrics.orm.android.SqliteDatabaseAndroidService;
import com.houseelectrics.orm.test.Asserter;
import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 01/03/2015.
 */
public class AndroidOrmExamples
{
    public static class TestRoot
    {
        private String name;
        public String getName() {return name;}
        public void setName(String value) {this.name=value;}
        private TestDetail testDetail;
        public TestDetail getTestDetail() {return testDetail;}
        public void setTestDetail(TestDetail value) {this.testDetail = value;}
        private List<TestDetail> details = null;
        public List<TestDetail> getDetails() {return details;}
        public void setDetails(List<TestDetail> value) {this.details=value;}
    }
    public static class TestDetail
    {
        private String detailName;
        public String getDetailName() {return detailName;}
        public  void setDetailName(String value) {this.detailName=value;}
        private int detailId;
        public int getDetailId() {return detailId;}
        public void setDetailId(int value) {this.detailId=value;}
    }

    public void testBasicCrud(final Activity activity, final Asserter asserter) throws Exception
    {
        AndroidDbUtil androidDbUtil = new AndroidDbUtil();
        // determine the database location  (something like  xxxxxx/AndroidOrmExamples/testBasic.db)
        String dbAbsolutePath = androidDbUtil.getOrCreateExternalStorageDbPath("AndroidOrmExamples", "testBasicCrud.db");
        // create a database service pointing at that location
        SqliteDatabaseAndroidService da = androidDbUtil.createDb(activity, dbAbsolutePath, true);
        // create a database context reflecting the java domain model
        // table names are inferred from class names
        // column names are inferred from getter setter names

        // assume properties type in this package are references and Lists are references
        DbContext dbContext;
        dbContext = (new ReflectionDbContextFactory()).createFromRootClassAssumeReferencesWithinPackageOrLists(TestRoot.class);
        dbContext.setSqliteDBAndroidStyle(da);
        //create the db schema if necessary
        dbContext.createSchemaIfNotExists();

        //create a test domain object
        TestRoot testRoot = new TestRoot();
        testRoot.setName("Top Cat");
        testRoot.setTestDetail(new TestDetail());
        testRoot.getTestDetail().setDetailName("Underdog");
        testRoot.setDetails(new ArrayList<TestDetail>());
        testRoot.getDetails().add(new TestDetail());
        testRoot.getDetails().get(0).setDetailName("list item 1");
        testRoot.getDetails().get(0).setDetailId(321);
        testRoot.getDetails().add(new TestDetail());
        testRoot.getDetails().get(1).setDetailName("list item 2");
        testRoot.getDetails().get(1).setDetailId(432);

        //add the domain object to the context
        dbContext.add(testRoot);
        // add an extra test object
        dbContext.add(new TestRoot());
        // save to the database
        dbContext.saveChanges();

        //create a new context - for a clean test !
        dbContext = (new ReflectionDbContextFactory()).createFromRootClassAssumeReferencesWithinPackageOrLists(TestRoot.class);
        dbContext.setSqliteDBAndroidStyle(da);
        //retrieve the domain object with a query
        List<Object> roots = dbContext.getAllByTypeShallow(TestRoot.class);
        asserter.assertEqual(roots.size(), 2);
        TestRoot retrievedTestRoot = (TestRoot) roots.get(0);
        retrievedTestRoot = (TestRoot) dbContext.deepLoad(retrievedTestRoot);

        //check it matches the original
        asserter.assertEqual(retrievedTestRoot.getName(), testRoot.getName());
        asserter.assertEqual(retrievedTestRoot.getTestDetail().getDetailName(), testRoot.getTestDetail().getDetailName());
        asserter.assertEqual(retrievedTestRoot.getDetails().get(1).getDetailId(), testRoot.getDetails().get(1).getDetailId());

        retrievedTestRoot.getTestDetail().setDetailName("Top Dog");

        // force the dbContext to update the database
        dbContext.update(retrievedTestRoot.getTestDetail());
        // delete the empty root
        dbContext.delete(roots.get(1));
        dbContext.saveChanges();

        //create a new context - for a clean test !
        dbContext = (new ReflectionDbContextFactory()).createFromRootClassAssumeReferencesWithinPackageOrLists(TestRoot.class);
        dbContext.setSqliteDBAndroidStyle(da);
        roots = dbContext.getAllByTypeShallow(TestRoot.class);
        //check the delete worked
        asserter.assertEqual(roots.size(), 1);
        //check the update worked
        TestRoot retrievedRoot0 = (TestRoot) roots.get(0);
        retrievedRoot0 = (TestRoot) dbContext.deepLoad(retrievedRoot0);
        asserter.assertEqual(retrievedRoot0.getTestDetail().getDetailName(), "Top Dog");

    }



}
