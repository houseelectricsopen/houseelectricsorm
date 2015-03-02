package com.houseelectrics.orm.test;
import com.houseelectrics.orm.*;
import com.houseelectrics.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by roberttodd on 05/01/2015.
 */
public class CrudTest extends DbTestBase
{

    public static class SimpleTestType
    {
        private String name;
        public String getName() {return name;}
        public void setName(String value) {this.name=value;}

        private boolean boolField;
        public boolean getBoolField() {return boolField;}
        public void setBoolField(boolean value) {this.boolField=value;}

        private short shortField;
        public short getShortField() {return shortField;}
        public void setShortField(short value) {this.shortField=value;}

        private int intField;
        public int getIntField() {return intField;}
        public void setIntField(int value) {this.intField=value;}

        private long longField;
        public long getLongField() {return longField;}
        public void setLongField(long value) {this.longField=value;}

        private float floatField;
        public float getFloatField() {return floatField;}
        public void setFloatField(float value) {this.floatField=value;}

        private double doubleField;
        public double getDoubleField() {return doubleField;}
        public void setDoubleField(double value) {this.doubleField=value;}

        private Date dateField;
        public Date getDateField() {return dateField;}
        public void setDateField(Date value) {this.dateField=value;}

        public final static String propertyPaths[] = {"Name", "BoolField", "ShortField", "IntField", "LongField",  "FloatField", "DoubleField", "DateField"};
    }

    private void compareListPropertyValues(List actualParent, List expectedParent, String[] propertyPaths, Asserter Assert) throws Exception
    {
        Assert.assertEqual("list sizes:", expectedParent.size(), actualParent.size());
        for (int done=0; done<expectedParent.size(); done++)
        {
            comparePropertyValues(actualParent.get(done), expectedParent.get(done), propertyPaths, Assert);
        }
    }

    private void comparePropertyValues(Object actualParent, Object expectedParent, String[] propertyPaths, Asserter Assert) throws Exception
    {
        if (actualParent==null && expectedParent==null) return;
        for (int done=0; done<propertyPaths.length; done++)
        {
            String propertyPath =propertyPaths[done];
            Object expectedValue=null;
            Object actualValue = null;
            ReflectionUtil.PropertyReference ref=null;
            ref = ReflectionUtil.getPropertyReferenceForKeyPathWithIndexes(expectedParent, propertyPath, false, true, null);
            expectedValue = ref.get(expectedParent);
            actualValue = ref.get(actualParent);
            Assert.assertEqual("property:" + expectedParent.getClass().getName() + "." + propertyPath, expectedValue, actualValue);
        }
    }

    public void crudTestSimpleFromEmptyDb(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        SimpleTestType o = new SimpleTestType();
        o.setName("mr tickle");
        o.setBoolField(true);
        o.setShortField((short) 32);
        o.setIntField(321);
        o.setLongField(321456789012l);
        o.setFloatField(12.3f);
        o.setDoubleField(12.3456789);
        o.setDateField(new Date());
        DbContext context;
        context = (new ReflectionDbContextFactory()).createFromRootClass(SimpleTestType.class);
        EntityType et = context.getType2EntityType().get(o.getClass());
        Assert.assertEqual("should be an entity mapped for " + o.getClass().getName(), true, et!=null);

        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        context.add(o);
        context.saveChanges();
        EntityInstance ei;
        ei = context.getEntityInstanceForObject(o);
        long savedId = ei.getDbId();

        context=(new ReflectionDbContextFactory()).createFromRootClass(SimpleTestType.class);
        context.setSqliteDBAndroidStyle(db);
        List<Object> os= context.getAllByTypeShallow(SimpleTestType.class);
        Assert.assertEqual(1, os.size());
        SimpleTestType retrievedO = (SimpleTestType) os.get(0);
        comparePropertyValues(retrievedO, o, SimpleTestType.propertyPaths, Assert);
        List<ReflectionUtil.DeepCompareDifference> diffs = ReflectionUtil.deepCompareViaReadWriteableProperties(retrievedO, o);
        for (int done=0; done<diffs.size(); done++)
        {
            ReflectionUtil.DeepCompareDifference diff = diffs.get(done);
            System.out.println("Difference: " + diff.propertyPath + " " + diff.description);
        }
        Assert.assertEqual(0, diffs.size());

        ei = context.getEntityInstanceForObject(retrievedO);
        long retrievedId = ei.getDbId();
        Assert.assertEqual(retrievedId, savedId);
        retrievedO.setName(retrievedO.getName() + " choonged!!!");
        retrievedO.setBoolField(false);
        context.update(retrievedO);
        context.saveChanges();

        context=(new ReflectionDbContextFactory()).createFromRootClass(SimpleTestType.class);
        context.setSqliteDBAndroidStyle(db);
         os= context.getAllByTypeShallow(SimpleTestType.class);
        Assert.assertEqual(1, os.size());
        SimpleTestType retrievedSecondTimeOver = (SimpleTestType) os.get(0);
        comparePropertyValues(retrievedSecondTimeOver, retrievedO, SimpleTestType.propertyPaths, Assert);

        context.delete(retrievedSecondTimeOver);
        context.saveChanges();
        os= context.getAllByTypeShallow(SimpleTestType.class);
        Assert.assertEqual(0, os.size());

        context=(new ReflectionDbContextFactory()).createFromRootClass(SimpleTestType.class);
        context.setSqliteDBAndroidStyle(db);
        os= context.getAllByTypeShallow(SimpleTestType.class);
        Assert.assertEqual(0, os.size());
    }

    public void crudTestWithOrderFromEmptyDb(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        DbContext context;
        context = (new ReflectionDbContextFactory()).createFromRootClass(SimpleTestType.class);

        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();


        SimpleTestType o;

        o = new SimpleTestType();

        EntityType et = context.getType2EntityType().get(o.getClass());
        Assert.assertEqual("should be an entity mapped for " + o.getClass().getName(), true, et!=null);
        o.setName("mr tickle");
        o.setBoolField(true);
        o.setShortField((short) 32);
        o.setIntField(321);
        o.setLongField(321456789012l);
        o.setFloatField(12.3f);
        o.setDoubleField(12.3456789);
        o.setDateField(new Date());
        context.add(o);

        o = new SimpleTestType();
        context.add(o);

        context.delete(o);
        context.saveChanges();

        o = new SimpleTestType();
        o.setName("mr tickle");
        o.setBoolField(true);
        o.setShortField((short) 32);
        o.setIntField(321);
        o.setLongField(321456789012l);
        o.setFloatField(12.3f);
        o.setDoubleField(12.3456789);
        o.setDateField(new Date());
        context.add(o);
        context.saveChanges();
    }

    public void crudWithDetailTestFromEmptyDb(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        RootTestType o = new RootTestType();
        o.setName("mr tickle");
        DetailTestType detailTestType = new DetailTestType();
        detailTestType.setDetailName("young master tickle");
        o.setDetail(detailTestType);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(RootTestType.class.getPackage());
        context = (new ReflectionDbContextFactory()).createFromRootClassAssumeReferencesWithinPackageOrLists(RootTestType.class);
        EntityType et =  context.getType2EntityType().get(o.getClass());
        Assert.assertEqual("entity type should be known for " + o.getClass().getName(), true, et!=null);
        String detailPropertyName = "Detail";
        EntityField ef = et.findFieldByPropertyName(detailPropertyName);
        Assert.assertEqual("field " + detailPropertyName +" should be known", true, ef!=null);
        Assert.assertEqual("field " + detailPropertyName +" should be reference property", true, ef.getIsReference());

        et = context.getType2EntityType().get(detailTestType.getClass());
        Assert.assertEqual("entity for class" + detailTestType.getClass().getName() +" should be known", true, et!=null);

        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        context.add(o);
        context.saveChanges();

        EntityInstance ei;
        ei = context.getEntityInstanceForObject(o);

        Assert.assertEqual("entity should be known", true, ei!=null);

        long savedId = ei.getDbId();
        ei = context.getEntityInstanceForObject(o.getDetail());
        Assert.assertEqual("detail entity should be known", true, ei!=null);

        long detailSavedId = ei.getDbId();

        context=(new ReflectionDbContextFactory()).createFromRootClass(RootTestType.class, referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        List<Object> os= context.getAllByTypeShallow(RootTestType.class);
        Assert.assertEqual(1, os.size());
        RootTestType retrievedO = (RootTestType) os.get(0);
        Assert.assertEqual(retrievedO.getName(), o.getName());
        Assert.assertEqual("detail not present for shallow load ", null, retrievedO.getDetail());

        RootTestType retrievedDeep = (RootTestType) context.deepLoad(retrievedO);

        Assert.assertEqual("detail  present for deep load ", true, retrievedDeep.getDetail()!=null);
        Assert.assertEqual("detail property should be available", retrievedDeep.getDetail().getDetailName(), o.getDetail().getDetailName() );

        ei = context.getEntityInstanceForObject(retrievedDeep);
        long deepLoadedId = ei.getDbId();
        Assert.assertEqual("saved id should be same as deep loaded", deepLoadedId, savedId);
        long retrievedDetailId = context.getEntityInstanceForObject(retrievedDeep.getDetail()).getDbId();
        Assert.assertEqual("detailSaved Id should be same deep loaded detail", retrievedDetailId, detailSavedId);

        context.delete(retrievedDeep);
        context.saveChanges();
        os= context.getAllByTypeShallow(RootTestType.class);
        Assert.assertEqual(0, os.size());

        context=(new ReflectionDbContextFactory()).createFromRootClass(RootTestType.class, referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        os= context.getAllByTypeShallow(RootTestType.class);
        Assert.assertEqual(0, os.size());

        context=(new ReflectionDbContextFactory()).createFromRootClass(RootTestType.class, referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        context.add(o);
        context.saveChanges();
        savedId = context.getEntityInstanceForObject(o).getDbId();
        context=(new ReflectionDbContextFactory()).createFromRootClass(RootTestType.class, referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);

        //check load by type and od
        RootTestType rtt= (RootTestType) context.deepLoad(RootTestType.class, savedId);
        List<ReflectionUtil.DeepCompareDifference> diffs = ReflectionUtil.deepCompareViaReadWriteableProperties(rtt, o);
        for (ReflectionUtil.DeepCompareDifference diff : diffs)
        {
            System.out.println("Difference: " + diff.propertyPath + ":" + diff.description);
        }
        Assert.assertEqual(diffs.size(), 0);
        //check there is an entity instance
        EntityInstance eiDeep = context.getEntityInstanceForObject(rtt);
        Assert.assertEqual("there should be an instance", eiDeep!=null, true);
        Assert.assertEqual("ei data should be retrieved data",eiDeep.getData(), rtt);

        //TODO test cascading delete
    }

    public static class ListContainingType
    {
        private String name;
        public String getName() {return name;}
        public void setName(String value) {this.name = value;}

        private List<SimpleTestType> details;
        public List<SimpleTestType> getDetails() {return details;}
        public void setDetails(List<SimpleTestType> value) {this.details = value;}
    }

   // ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences()

    public void crudWithListProperty(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        ListContainingType o = new ListContainingType();
        o.setName("mr tickle");
        o.setDetails(new ArrayList<SimpleTestType>());

        for (int done=0; done<3; done++)
        {
            SimpleTestType detail;
            detail = new SimpleTestType();
            detail.setIntField(done);
            detail.setName("Mr " + done);
            o.getDetails().add(detail);
        }

        o.getDetails().add(null);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(ListContainingType.class.getPackage());
        context = (new ReflectionDbContextFactory()).createFromRootClass(ListContainingType.class, referenceTypeDetector);
        EntityType et =  context.getType2EntityType().get(o.getClass());
        Assert.assertEqual("entity type should be known for " + o.getClass().getName(), true, et!=null);
        String detailsPropertyName = "Details";
        ListEntityType detailEf = et.findEntityListTypeByPropertyName(detailsPropertyName);
        Assert.assertEqual("detail type   " + detailsPropertyName +" should be known", true, detailEf!=null);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        context.add(o);
        context.saveChanges();

        EntityInstance ei;
        ei = context.getEntityInstanceForObject(o);
        long dbId = ei.getDbId();

        context = createContext(ListContainingType.class, db);

        List<Object> lcontainers = context.getAllByTypeShallow(ListContainingType.class);
        ListContainingType retrievedO = (ListContainingType) lcontainers.get(0);
        retrievedO = (ListContainingType) context.deepLoad(retrievedO);

        Assert.assertEqual("list items should be there ", true, o.getDetails()!=null && o.getDetails().size()>0);
        compareListPropertyValues( retrievedO.getDetails(),  o.getDetails(), SimpleTestType.propertyPaths, Assert);

        Collections.reverse(retrievedO.getDetails());
        context.updateProperty(retrievedO, "Details");
        Collections.reverse(o.getDetails());
        compareListPropertyValues(retrievedO.getDetails(), o.getDetails(), SimpleTestType.propertyPaths, Assert);

        context.saveChanges();
        context = createContext(ListContainingType.class, db);

        List<Object> lcontainers1 = context.getAllByTypeShallow(ListContainingType.class);
        ListContainingType retrieved1 = (ListContainingType) lcontainers1.get(0);
        retrieved1 = (ListContainingType) context.deepLoad(retrieved1);
        compareListPropertyValues( retrieved1.getDetails(),  o.getDetails(), SimpleTestType.propertyPaths, Assert);
    }

    public static class RawSelectTestResult
    {
        public RawSelectTestResult() {}

        private String name;
        public void setName(String value) {this.name=value;}
        public String getName() {return name;}

        private int theInt;
        public void setTheInt(int value) {this.theInt=value;}
        public int getTheInt() {return theInt;}

    }

    public void testRawSelect(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        String select =
        " SELECT lc.Name, s.IntField as TheInt FROM  ListContainingType_Details ld JOIN " +
        " ListContainingType lc ON lc.rowid=ld.masterid " +
        " JOIN SimpleTestType s ON s.rowid = ld.ChildId ";
        DbContext context = createContext(ListContainingType.class, db );
        ListContainingType o = new ListContainingType();
        o.setName("mr tickle");
        o.setDetails(new ArrayList<SimpleTestType>());

        for (int done=0; done<3; done++)
        {
            SimpleTestType detail;
            detail = new SimpleTestType();
            detail.setIntField(done);
            detail.setName("Mr " + done);
            o.getDetails().add(detail);
        }

        context.add(o);
        context.saveChanges();

        List<Object> items = context.excecuteRawQuery(RawSelectTestResult.class, select);
        Assert.assertEqual(items.size(), o.getDetails().size());
//        RawSelectTestResult result = (RawSelectTestResult) items.get(0);

        for (int done=0; done<o.getDetails().size(); done++)
        {
            RawSelectTestResult result = (RawSelectTestResult) items.get(done);
            Assert.assertEqual(result.getName(), o.getName());
            Assert.assertEqual(result.getTheInt(), o.getDetails().get(done).getIntField());
        }
    }

    public void testRawSelectSingleColumn(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        String select =
                " SELECT lc.Name FROM  ListContainingType_Details ld JOIN " +
                        " ListContainingType lc ON lc.rowid=ld.masterid " +
                        " JOIN SimpleTestType s ON s.rowid = ld.ChildId ";
        DbContext context = createContext(ListContainingType.class, db );
        ListContainingType o = new ListContainingType();
        o.setName("mr tickle");
        o.setDetails(new ArrayList<SimpleTestType>());

        for (int done=0; done<3; done++)
        {
            SimpleTestType detail;
            detail = new SimpleTestType();
            detail.setIntField(done);
            detail.setName("Mr " + done);
            o.getDetails().add(detail);
        }

        context.add(o);
        context.saveChanges();

        List<Object> items = context.excecuteRawQuerySingleColumn(select);
        Assert.assertEqual(items.size(), o.getDetails().size());
//        RawSelectTestResult result = (RawSelectTestResult) items.get(0);

        for (int done=0; done<o.getDetails().size(); done++)
        {
            String result = (String) items.get(done);
            Assert.assertEqual(result, o.getName());
        }
    }


    public static class RawSelectDeepTestResult
    {
        private String name;
        public String getName() {return name;}
        public void setName(String value) {this.name=value;}

        private RawSelectTestResult detail;
        public RawSelectTestResult getDetail() {return detail;}
        public void setDetail(RawSelectTestResult value) {this.detail = value;}

    }

    public void testRawSelectDeep(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        String select =
                " SELECT lc.Name, lc.Name as [Detail.Name], s.IntField as [Detail.TheInt] FROM  ListContainingType_Details ld JOIN " +
                        " ListContainingType lc ON lc.rowid=ld.masterid " +
                        " JOIN SimpleTestType s ON s.rowid = ld.ChildId ";
        DbContext context = createContext(ListContainingType.class, db );
        ListContainingType o = new ListContainingType();
        o.setName("mr tickle");
        o.setDetails(new ArrayList<SimpleTestType>());

        for (int done=0; done<3; done++)
        {
            SimpleTestType detail;
            detail = new SimpleTestType();
            detail.setIntField(done);
            detail.setName("Mr " + done);
            o.getDetails().add(detail);
        }

        context.add(o);
        context.saveChanges();

        List<Object> items = context.excecuteRawQuery(RawSelectDeepTestResult.class, select);
        Assert.assertEqual(items.size(), o.getDetails().size());
//        RawSelectTestResult result = (RawSelectTestResult) items.get(0);

        for (int done=0; done<o.getDetails().size(); done++)
        {
            RawSelectDeepTestResult result = (RawSelectDeepTestResult) items.get(done);
            Assert.assertEqual(result.getName(), o.getName());
            Assert.assertEqual(result.getDetail().getName(), o.getName());
            Assert.assertEqual(result.getDetail().getTheInt(), o.getDetails().get(done).getIntField());
        }
    }

    public static class SimpleTestSubClass extends  SimpleTestType
    {
        private String subClassField;
        public String getSubClassField() {return subClassField;}
        public void setSubClassField(String value) {this.subClassField=value;}
    }

    public void testSubClass(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        Class theClass = SimpleTestSubClass.class;

        SimpleTestSubClass o = new SimpleTestSubClass();
        o.setName("mr tickle");
        o.setSubClassField("sub class value");

        DbContext context;
        context = createContext(theClass, db);

        context.add(o);
        context.saveChanges();
        EntityInstance ei = context.getEntityInstanceForObject(o);

        context = createContext(theClass, db);
        List<Object> retrievedOs = context.getAllByTypeShallow(theClass);
        Assert.assertEqual(1, retrievedOs.size());
        SimpleTestSubClass retrievedO = (SimpleTestSubClass) retrievedOs.get(0);
        Assert.assertEqual(retrievedO.getName(), o.getName());
        Assert.assertEqual(retrievedO.getSubClassField(), o.getSubClassField());
    }

    public void testDeepLoadWithEmptyDetail(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        Class theClass = RootTestType.class;

        RootTestType o = new RootTestType();
        o.setName("mr tickle");
        o.setDetail(null);

        DbContext context;
        context = createContext(theClass, db);

        context.add(o);
        context.saveChanges();
        EntityInstance ei = context.getEntityInstanceForObject(o);
        long savedDbId = ei.getDbId();

        context = createContext(theClass, db);
        RootTestType retrievedO = (RootTestType)  context.deepLoad(RootTestType.class, savedDbId);
        Assert.assertEqual(retrievedO!=null, true);
        Assert.assertEqual(retrievedO.getName(), o.getName());
    }

    public void testDeepLoadAlreadyPopulated(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        Class theClass = RootTestType.class;

        RootTestType o = new RootTestType();
        o.setName("mr tickle");

        DbContext context;
        context = createContext(theClass, db);

        context.add(o);
        context.saveChanges();
        EntityInstance ei = context.getEntityInstanceForObject(o);
        long savedDbId = ei.getDbId();

        context = createContext(theClass, db);
        List<Object> os = context.getAllByTypeShallow(RootTestType.class);
        Assert.assertEqual(os.size(), 1);
        RootTestType rootTestType = (RootTestType) os.get(0);
        //here is the population bit !
        rootTestType.setDetail(new DetailTestType());
        context.update(rootTestType);
        context.saveChanges();

        RootTestType retrievedO = (RootTestType)  context.deepLoad(RootTestType.class, savedDbId);
        Assert.assertEqual(retrievedO!=null, true);
        Assert.assertEqual(retrievedO.getName(), o.getName());
    }

    public static class CustomTypeContainer
    {
        private String[] theStrings;
        public void setTheStrings(String []value ) {this.theStrings=value;}
        public String[] getTheStrings() {return this.theStrings;}

        private List<String> stringList;
        public void setStringList(List<String> value) {this.stringList=value;}
        public List<String> getStringList() {return stringList;}

        private List<Integer> integerList;
        public void setIntegerList(List<Integer> value) {this.integerList=value;}
        public List<Integer> getIntegerList() {return this.integerList;}

        private List<SimpleTestType> details;
        public List<SimpleTestType> getDetails() {return details;}
        public void setDetails(List<SimpleTestType> value) {this.details = value;}
    }

    public void testCustomTypeMappings(Asserter Assert, SqliteDatabaseServiceBase db) throws Exception
    {
        // map String Lists to a single db field
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = new ReflectionDbContextFactory.ReferenceTypeDetector()
        {
            @Override
            public boolean isReferenceType(Class type, Type[] genericTypeParameters)
            {
                boolean isList = List.class.isAssignableFrom(type);
                boolean isInThisPackage = type.getPackage()!=null && type.getPackage().equals(this.getClass().getPackage());
                boolean isStringList = isList && genericTypeParameters!=null && genericTypeParameters.length>0 && genericTypeParameters[0]==String.class;
                boolean isIntegerList = isList && genericTypeParameters!=null && genericTypeParameters.length>0 && genericTypeParameters[0]==Integer.class;
                return  (!isIntegerList && !isStringList && isList) || isInThisPackage;
            }

            @Override
            public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
            {
                return false;
            }
        };

        Class rootType = CustomTypeContainer.class;
        DbContext context;
        context = (new ReflectionDbContextFactory()).createFromRootClass(rootType, referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();

        //check there is only entity mapped and its for the rootClass
        Assert.assertEqual("only 2 root types should be mapped", 2, context.getType2EntityType().size());
        Assert.assertEqual("rootType should be mapped", true, context.getType2EntityType().containsKey(rootType));
        EntityType et =  context.getType2EntityType().get(rootType);
        EntityField field= et.findFieldByPropertyName("TheStrings");
        Assert.assertEqual("custom type field should be mapped as a field", true, field!=null);

        CustomTypeContainer o = new CustomTypeContainer();
        String theStrings[] = {"a", "two", "gamma", "3"};
        o.setTheStrings(theStrings);

        List<String> lString = new ArrayList<String>();
        lString.add("ooh");
        lString.add("ahh");
        lString.add("mmm");
        o.setStringList(lString);

        List<Integer> lInt = new ArrayList<Integer>();
        lInt.add(3);
        lInt.add(2);
        lInt.add(1);
        o.setIntegerList(lInt);

        SimpleTestType sT = new SimpleTestType();
        sT.setName("abc345");
        o.setDetails(new ArrayList<SimpleTestType>());
        o.getDetails().add(sT);

        context.add(o);
        context.saveChanges();

        context = (new ReflectionDbContextFactory()).createFromRootClass(rootType, referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        List<Object> retrievedOs = context.getAllByTypeShallow(rootType);
        Assert.assertEqual(1, retrievedOs.size());
        CustomTypeContainer retrievedO = (CustomTypeContainer) retrievedOs.get(0);
        Assert.assertEqual(retrievedO.getTheStrings().length, o.getTheStrings().length);
        for (int done=0; done<retrievedO.getTheStrings().length; done++)
        {
            Assert.assertEqual(retrievedO.getTheStrings()[done], o.getTheStrings()[done]);
        }
    }


    public static class PersistentPropertyFilterTestClass
    {
        int saveMeInt;
        public int getSaveMeInt() {return saveMeInt;}
        public void setSaveMeInt(int value) {this.saveMeInt=value;}

        int transientInt;
        public int getTransientInt() {return transientInt;}
        public void setTransientInt(int value) {this.transientInt=value;}
    }

    public void testPropertyPersistenceFilter(Asserter Assert, SqliteDatabaseServiceBase db) throws Exception
    {
        Class rootType = PersistentPropertyFilterTestClass.class;
        DbContext context;
        context = (new ReflectionDbContextFactory()).createFromRootClass(rootType);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();

        PersistentPropertyFilterTestClass o = new PersistentPropertyFilterTestClass();
        o.setSaveMeInt(321);
        o.setTransientInt(321);

        context.add(o);
        context.saveChanges();

        ReflectionDbContextFactory.PropertyPersistenceFilter ppf = new ReflectionDbContextFactory.PropertyPersistenceFilter()
        {
            @Override
            public boolean isPersistent(ReflectionUtil.PropertyReference propertyReference)
            {
                if (propertyReference.getPropertyName().toLowerCase().indexOf("transient")>=0 )
                return false;
                else return true;
            }
        };

        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(rootType.getPackage());
        context = (new ReflectionDbContextFactory()).createFromRootClass(rootType, referenceTypeDetector, ppf);
        context.setSqliteDBAndroidStyle(db);
        List<Object> retrievedOs = context.getAllByTypeShallow(rootType);
        Assert.assertEqual(1, retrievedOs.size());
        PersistentPropertyFilterTestClass retrievedO = (PersistentPropertyFilterTestClass) retrievedOs.get(0);
        Assert.assertEqual(retrievedO.getSaveMeInt(), o.getSaveMeInt());
        Assert.assertEqual(retrievedO.getTransientInt(), 0);

    }

    public void testRepeatedShallowLoad(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        Class theClass = SimpleTestSubClass.class;

        DbContext context;
        context = createContext(theClass, db);
        SimpleTestSubClass o;

        o = new SimpleTestSubClass();
        o.setName("mr tickle");
        o.setSubClassField("sub class value");
        context.add(o);
        context.saveChanges();

        List<Object> os;
        os = context.getAllByTypeShallow(SimpleTestSubClass.class);
        EntityInstance ei = context.getEntityInstanceForObject(o);
        Assert.assertEqual(os.size(), 1);
        Assert.assertEqual(os.get(0)==o, true);

        System.out.println("mmmm");
    }

    public static class SeparateRootTestClass
    {
        private String name;
        public String getName() {return name;}
        public void setName(String value) {this.name=value;}
    }

    public void testMultipleRoots(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        Class theClass = SimpleTestSubClass.class;
        List<Class> rootClasses = new ArrayList<Class>();
        rootClasses.add(SimpleTestSubClass.class);
        rootClasses.add( SeparateRootTestClass.class);

        DbContext context;
        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, null, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        SimpleTestSubClass o;

        o = new SimpleTestSubClass();
        o.setName("mr tickle");
        o.setSubClassField("sub class value");
        context.add(o);

        SeparateRootTestClass o2 = new SeparateRootTestClass();
        o2.setName("abcdefg1234");
        context.add(o2);

        context.saveChanges();

        List<Object> os;
        os = context.getAllByTypeShallow(SimpleTestSubClass.class);
        EntityInstance ei = context.getEntityInstanceForObject(o);
        Assert.assertEqual(os.size(), 1);
        Assert.assertEqual(os.get(0)==o, true);

        os = context.getAllByTypeShallow(SeparateRootTestClass.class);
        ei = context.getEntityInstanceForObject(o2);
        Assert.assertEqual(os.size(), 1);
        Assert.assertEqual(os.get(0)==o2, true);

    }

    public static class PolymorphicContainer
    {
        private Object theObject;
        public Object getTheObject() {return theObject;}
        public void setTheObject(Object value) {this.theObject=value;}
    }

    public void testPolymorphicReference(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        List<Class> rootClasses = new ArrayList<Class>();
        rootClasses.add(PolymorphicContainer.class);
        rootClasses.add(SimpleTestSubClass.class);
        rootClasses.add(SeparateRootTestClass.class);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector =
                new ReflectionDbContextFactory.ReferenceTypeDetector()
                {
                    @Override
                    public boolean isReferenceType(Class type, Type[] genericParameters)
                    {
                        return type == Object.class;
                    }

                    @Override
                    public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
                    {
                        return type == Object.class;
                    }
                };
        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        SimpleTestSubClass o;

        PolymorphicContainer container;

        container= new PolymorphicContainer();
        context.add(container);

        o = new SimpleTestSubClass();
        o.setName("mr tickle");
        o.setSubClassField("sub class value");
        container.setTheObject(o);

        SeparateRootTestClass o2 = new SeparateRootTestClass();
        o2.setName("separate root");

        PolymorphicContainer container2 = new PolymorphicContainer();
        container2.setTheObject(o2);

        context.add(container2);
        context.saveChanges();

        EntityInstance ei = context.getEntityInstanceForObject(container);
        Assert.assertEqual(ei!=null, true);
        long dbId = ei.getDbId();
        EntityInstance ei2 = context.getEntityInstanceForObject(container2);
        Assert.assertEqual(ei2!=null, true);
        long dbId2 = ei2.getDbId();

        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);

        PolymorphicContainer retrievedContainer;
        retrievedContainer = (PolymorphicContainer) context.deepLoad(PolymorphicContainer.class, dbId);
        PolymorphicContainer retrievedContainer2;
        retrievedContainer2 = (PolymorphicContainer) context.deepLoad(PolymorphicContainer.class, dbId2);

        List<ReflectionUtil.DeepCompareDifference> diffs;
        diffs=  ReflectionUtil.deepCompareViaReadWriteableProperties(retrievedContainer, container);
        for (int done=0; done<diffs.size(); done++)
        {
            ReflectionUtil.DeepCompareDifference diff = diffs.get(done);
            System.out.println("" + diff.description + " " + diff.propertyPath);
        }
        Assert.assertEqual(0, diffs.size());
        diffs=  ReflectionUtil.deepCompareViaReadWriteableProperties(retrievedContainer2, container2);
        for (int done=0; done<diffs.size(); done++)
        {
            ReflectionUtil.DeepCompareDifference diff = diffs.get(done);
            System.out.println("" + diff.description + " " + diff.propertyPath);
        }
        Assert.assertEqual(0, diffs.size());
    }


    public void testPolymorphicNullReference(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        List<Class> rootClasses = new ArrayList<Class>();
        rootClasses.add(PolymorphicContainer.class);
        rootClasses.add(SimpleTestSubClass.class);
        rootClasses.add(SeparateRootTestClass.class);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector =
                new ReflectionDbContextFactory.ReferenceTypeDetector()
                {
                    @Override
                    public boolean isReferenceType(Class type, Type[] genericParameters)
                    {
                        return type == Object.class;
                    }

                    @Override
                    public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
                    {
                        return type == Object.class;
                    }
                };
        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();

        PolymorphicContainer container;
        container= new PolymorphicContainer();
        context.add(container);

        container.setTheObject(null);

        context.saveChanges();

        EntityInstance ei = context.getEntityInstanceForObject(container);
        Assert.assertEqual(ei!=null, true);
        long dbId = ei.getDbId();

        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);

        PolymorphicContainer retrievedContainer;
        retrievedContainer = (PolymorphicContainer) context.deepLoad(PolymorphicContainer.class, dbId);

        List<ReflectionUtil.DeepCompareDifference> diffs;
        diffs=  ReflectionUtil.deepCompareViaReadWriteableProperties(retrievedContainer, container);
        for (int done=0; done<diffs.size(); done++)
        {
            ReflectionUtil.DeepCompareDifference diff = diffs.get(done);
            System.out.println("" + diff.description + " " + diff.propertyPath);
        }
        Assert.assertEqual(0, diffs.size());
    }

    public static class PolymorphicListContainer
    {
        private int version=0;
        public int getVersion() {return version;}
        public void setVersion(int value) {this.version=value;}

        private List<Object> theObjects;
        public List<Object> getTheObjects() {return theObjects;}
        public void setTheObjects(List<Object> value) {this.theObjects=value;}
    }

    public void testPolymorphicListReferences(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        List<Class> rootClasses = new ArrayList<Class>();
        rootClasses.add(PolymorphicListContainer.class);
        rootClasses.add(SimpleTestSubClass.class);
        rootClasses.add(SeparateRootTestClass.class);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector =
                new ReflectionDbContextFactory.ReferenceTypeDetector()
                {
                    @Override
                    public boolean isReferenceType(Class type, Type[] genericParameters)
                    {
                        boolean isList = List.class.isAssignableFrom(type);
                        return type == Object.class || isList;
                    }

                    @Override
                    public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
                    {
                        return type == Object.class;
                    }
                };
        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        SimpleTestSubClass o;

        PolymorphicListContainer container;

        container= new PolymorphicListContainer();
        context.add(container);

        List<Object> theObjects = new ArrayList<Object>();
        container.setTheObjects(theObjects);

        theObjects.add(null);

        o = new SimpleTestSubClass();
        o.setName("mr tickle");
        o.setSubClassField("sub class value");
        theObjects.add(o);

        SeparateRootTestClass o2;
        o2 = new SeparateRootTestClass();
        o2.setName("separate root");
        theObjects.add(o2);

        o2 = new SeparateRootTestClass();
        o2.setName("separate root 2");
        theObjects.add(o2);

        o = new SimpleTestSubClass();
        o.setName("mr tickle 2");
        o.setSubClassField("sub class value 2");
        theObjects.add(o);

        context.saveChanges();

        EntityInstance ei = context.getEntityInstanceForObject(container);
        Assert.assertEqual(ei!=null, true);
        long dbId = ei.getDbId();

        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);

        PolymorphicListContainer retrievedContainer;
        retrievedContainer = (PolymorphicListContainer) context.deepLoad(PolymorphicListContainer.class, dbId);

        List<ReflectionUtil.DeepCompareDifference> diffs;
        diffs=  ReflectionUtil.deepCompareViaReadWriteableProperties(retrievedContainer, container);
        for (int done=0; done<diffs.size(); done++)
        {
            ReflectionUtil.DeepCompareDifference diff = diffs.get(done);
            System.out.println("" + diff.description + " " + diff.propertyPath);
        }
        Assert.assertEqual(0, diffs.size());

    }

    public static class PolymorphicStackContainer
    {
        private int version=0;
        public int getVersion() {return version;}
        public void setVersion(int value) {this.version=value;}

        private Stack<Object> theObjects;
        public Stack<Object> getTheObjects() {return theObjects;}
        public void setTheObjects(Stack<Object> value) {this.theObjects=value;}
    }


    public void testPolymorphicStackReferences(Asserter Assert, SqliteDatabaseService db) throws Exception
    {
        List<Class> rootClasses = new ArrayList<Class>();
        rootClasses.add(PolymorphicStackContainer.class);
        rootClasses.add(SimpleTestSubClass.class);
        rootClasses.add(SeparateRootTestClass.class);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector =
                new ReflectionDbContextFactory.ReferenceTypeDetector()
                {
                    @Override
                    public boolean isReferenceType(Class type, Type[] genericParameters)
                    {
                        boolean isList = List.class.isAssignableFrom(type);
                        return type == Object.class || isList;
                    }

                    @Override
                    public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
                    {
                        return type == Object.class;
                    }
                };
        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        SimpleTestSubClass o;

        PolymorphicStackContainer container;

        container= new PolymorphicStackContainer();
        context.add(container);

        Stack<Object> theObjects = new Stack<Object>();
        container.setTheObjects(theObjects);

        o = new SimpleTestSubClass();
        o.setName("mr tickle");
        o.setSubClassField("sub class value");
        theObjects.add(o);

        SeparateRootTestClass o2;
        o2 = new SeparateRootTestClass();
        o2.setName("separate root");
        theObjects.add(o2);

        o2 = new SeparateRootTestClass();
        o2.setName("separate root 2");
        theObjects.add(o2);

        o = new SimpleTestSubClass();
        o.setName("mr tickle 2");
        o.setSubClassField("sub class value 2");
        theObjects.add(o);

        context.saveChanges();

        EntityInstance ei = context.getEntityInstanceForObject(container);
        Assert.assertEqual(ei!=null, true);
        long dbId = ei.getDbId();

        context = (new ReflectionDbContextFactory()).createFromRootClasses(rootClasses, referenceTypeDetector, null/*, referenceTypeDetector*/);
        context.setSqliteDBAndroidStyle(db);

        PolymorphicStackContainer retrievedContainer;
        retrievedContainer = (PolymorphicStackContainer) context.deepLoad(PolymorphicStackContainer.class, dbId);

        List<ReflectionUtil.DeepCompareDifference> diffs;
        diffs=  ReflectionUtil.deepCompareViaReadWriteableProperties(retrievedContainer, container);
        for (int done=0; done<diffs.size(); done++)
        {
            ReflectionUtil.DeepCompareDifference diff = diffs.get(done);
            System.out.println("" + diff.description + " " + diff.propertyPath);
        }
        Assert.assertEqual(0, diffs.size());
    }
}
