package com.houseelectrics.orm.test;

import com.houseelectrics.orm.*;
import com.houseelectrics.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 25/02/2015.
 */
public class LazyLoadingTest extends DbTestBase
{
    public void testLazyLoading(Asserter Assert, SqliteDatabaseService db, ProxyFactory proxyFactory) throws Exception
    {
        final List<String> sqls = new ArrayList<String>();
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
            };
        };
        db.setLogger(rerootedLogger);

        RootTestType o = new RootTestType();
        o.setName("mr tickle");
        DetailTestType detailTestType = new DetailTestType();
        detailTestType.setDetailName("young master tickle");
        o.setDetail(detailTestType);

        DbContext context;
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(RootTestType.class.getPackage());

        context = (new ReflectionDbContextFactory()).createFromRootClass(RootTestType.class, referenceTypeDetector);
        context.setProxyFactory(proxyFactory);

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

        sqls.clear();

        EntityInstance ei;
        ei = context.getEntityInstanceForObject(o);

        Assert.assertEqual("entity should be known", true, ei!=null);

        long savedId = ei.getDbId();
        ei = context.getEntityInstanceForObject(o.getDetail());
        Assert.assertEqual("detail entity should be known", true, ei!=null);
        long detailSavedId = ei.getDbId();

        context=(new ReflectionDbContextFactory()).createFromRootClass(RootTestType.class, referenceTypeDetector);
        context.setProxyFactory(proxyFactory);
        context.setSqliteDBAndroidStyle(db);
        List<Object> os= context.getAllByTypeLazyLoad(RootTestType.class);

        Assert.assertEqual("only 1 for lazy load ", 1, sqls.size());
        Assert.assertEqual(1, os.size());
        RootTestType retrievedLazy = (RootTestType) os.get(0);
        Assert.assertEqual(retrievedLazy.getName(), o.getName());
        DetailTestType retrievedDetailTestType = retrievedLazy.getDetail();
        List<ReflectionUtil.DeepCompareDifference> diffs = ReflectionUtil.deepCompareViaReadWriteablePropertiesIgnoreTypeDifferences(detailTestType, retrievedDetailTestType);
        for (ReflectionUtil.DeepCompareDifference diff : diffs)
        {
            System.out.println("Diff: " + diff.propertyPath + " " + diff.description);
        }

        Assert.assertEqual("detail is the same as that saved  ", 0, diffs.size());
        Assert.assertEqual("2 total sqls for lazy load after detail ", 2, sqls.size());

        // update
        String currentDetailName = "abcdefg";
        retrievedDetailTestType.setDetailName(currentDetailName);
        context.update(retrievedDetailTestType);
        context.saveChanges();
        context=(new ReflectionDbContextFactory()).createFromRootClass(RootTestType.class, referenceTypeDetector);
        context.setProxyFactory(proxyFactory);
        context.setSqliteDBAndroidStyle(db);
        retrievedLazy = (RootTestType) context.lazyLoadDomainObjectById(RootTestType.class, savedId);
        Assert.assertEqual("detail name lazy load should be", retrievedLazy.getDetail().getDetailName(), currentDetailName);



    }

}
