#House Electrics ORM

This is an implementation of an object relational mapping for sqlite on android.

This is an implementation of an object relational mapping for sqlite on android.
It contains a minimal set of features to map a java object model to a relational database in an android app but nevertheless places a low burden on the developer.

Here are some of the features:
+ No database ids in domain objects – sqlite rowid is mapped internally to java domain object instances.
+ List Support
+ Polymorphic References
+ Customizeable type to db field mapping
+ Raw query s
+ Lazy Loading

Here is what is planned:
+ Model distinction between contained and non contained relationships - to enable cascade delete
+ More lazy loading tests
+ Data migration / multiple concurrent schema version support

##Support - Appeal for Comments

email support:  [codesupport@houseelectrics.com] (mailto:codesupport@houseelectrics.com)
All queries and suggestions are welcome, particularly in the form of new (failing) unit tests

##Project Organisation</h2>
###Project homeelectricsormbase
+ core code
+ A standard (non android) implementation– so the unit tests can be run easily on a mac or PC
+ Containers for the unit test that run the tests without android – classes SqliteJDBCCrudTest and SqliteJDBCLazyLoadingTest

###Project homeelectricsormandroid
+ android specific code

###Project homeelectricsormandroitest
+ An android app running all the unit tests
+ Click on "run tests" to run the tests

##A basic CRUD example

This is taken from houselectricsormandroidtest/AndroidOrmExamples

####Define the data model - without database id columns

'''

    public static class TestRoot
    {
        private String name;
        public String getName() {return name;}
        public void setName(String value) {this.name=value;}
        private TestDetail testDetail;
        public TestDetail getTestDetail() {return testDetail;}
        public void setTestDetail(TestDetail value) {this.testDetail = value;}
        private List&lt;TestDetail> details = null;
        public List&lt;TestDetail> getDetails() {return details;}
        public void setDetails(List&lt;TestDetail> value) {this.details=value;}
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

'''
####Create or connect to the database
'''
        AndroidDbUtil androidDbUtil = new AndroidDbUtil();
        // determine the database location  (something like  xxxxxx/AndroidOrmExamples/testBasic.db)
        String dbAbsolutePath = androidDbUtil.getOrCreateExternalStorageDbPath("AndroidOrmExamples", "testBasic");
        // create a database service pointing at that location
        SqliteDatabaseAndroidService da = androidDbUtil.createDb(activity, dbAbsolutePath, true);
'''
####Create a database context based on the datamodel
'''
        // assume properties type in this package are references and Lists are references
        DbContext dbContext;
        dbContext = (new ReflectionDbContextFactory()).createFromRootClassAssumeReferencesWithinPackageOrLists(TestRoot.class);
        dbContext.setSqliteDBAndroidStyle(da);
        //create the db schema if necessary
        dbContext.createSchemaIfNotExists();
'''
This results in creation of 3 tables:
+ TestRoot - representing the TestRoot class
+ TestDetail - representing the TestDetail class
+ TestRoot_Details - representing the Details relationship from TestRoot to TestDetail

####Create some data
'''
    TestRoot testRoot = new TestRoot();
    testRoot.setName("Top Cat");
    testRoot.setTestDetail(new TestDetail());
    testRoot.getTestDetail().setDetailName("Underdog");
    testRoot.setDetails(new ArrayList&lt;TestDetail>());
    testRoot.getDetails().add(new TestDetail());
    testRoot.getDetails().get(0).setDetailName("list item 1");
    testRoot.getDetails().get(0).setDetailId(321);
    testRoot.getDetails().add(new TestDetail());
    testRoot.getDetails().get(1).setDetailName("list item 2");
    testRoot.getDetails().get(1).setDetailId(432);
'''
####Add the objects to the context and save to database
'''
        dbContext.add(testRoot);
        // add an extra test object
        dbContext.add(new TestRoot());
        dbContext.saveChanges();
'''
####Create a new context for a clean test !
'''
       dbContext = (new ReflectionDbContextFactory()).createFromRootClassAssumeReferencesWithinPackageOrLists(TestRoot.class);
       dbContext.setSqliteDBAndroidStyle(da);
'''
    <h4>Retrieve the domain object with a query and check values against what was saved</h4>
<pre>
    List&lt;Object> roots = dbContext.getAllByTypeShallow(TestRoot.class);
    asserter.assertEqual(roots.size(), 2);
    TestRoot retrievedTestRoot = (TestRoot) roots.get(0);
    retrievedTestRoot = (TestRoot) dbContext.deepLoad(retrievedTestRoot);

    asserter.assertEqual(retrievedTestRoot.getName(), testRoot.getName());
    asserter.assertEqual(retrievedTestRoot.getTestDetail().getDetailName(), testRoot.getTestDetail().getDetailName());
    asserter.assertEqual(retrievedTestRoot.getDetails().get(1).getDetailId(), testRoot.getDetails().get(1).getDetailId());
</pre>
  <h4>Make some change and update the database</h4>
<pre>
    retrievedTestRoot.getTestDetail().setDetailName("Top Dog");
    dbContext.update(retrievedTestRoot.getTestDetail());
    dbContext.delete(roots.get(1));
    dbContext.saveChanges();
</pre>
    <h4>Create a new context - for a clean test !</h4>
<pre>
    dbContext = (new ReflectionDbContextFactory()).createFromRootClassAssumeReferencesWithinPackageOrLists(TestRoot.class);
    dbContext.setSqliteDBAndroidStyle(da);
</pre>
   <h4>Retrieve objects and check changes have been saved</h4>
<pre>
    roots = dbContext.getAllByTypeShallow(TestRoot.class);
    //check the delete worked
    asserter.assertEqual(roots.size(), 1);
    //check the update worked
    TestRoot retrievedRoot0 = (TestRoot) roots.get(0);
    retrievedRoot0 = (TestRoot) dbContext.deepLoad(retrievedRoot0);
    asserter.assertEqual(retrievedRoot0.getTestDetail().getDetailName(), "Top Dog");
</pre>

See the [user guide](docs/userguide.html) for more detail