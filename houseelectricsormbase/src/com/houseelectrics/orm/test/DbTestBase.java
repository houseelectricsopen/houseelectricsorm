package com.houseelectrics.orm.test;

import com.houseelectrics.orm.DbContext;
import com.houseelectrics.orm.ReferenceTypeDetectorUtil;
import com.houseelectrics.orm.ReflectionDbContextFactory;
import com.houseelectrics.orm.SqliteDatabaseService;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by roberttodd on 25/02/2015.
 */
public class DbTestBase
{

/*
            new ReflectionDbContextFactory.ReferenceTypeDetector()
    {
        @Override
        public boolean isReferenceType(Class type, Type[] genericTypeParameters)
        {
            boolean isList = List.class.isAssignableFrom(type);
            boolean isInThisPackage = type.getPackage()!=null && type.getPackage().equals(this.getClass().getPackage());
            return  isList || isInThisPackage;
        }

        @Override
        public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
        {
            return false;
        }
    };
*/
    protected DbContext createContext(Class rootClass, SqliteDatabaseService db) throws Exception
    {
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = ReferenceTypeDetectorUtil.thisPackageAndListsAsReferences(rootClass.getPackage());
        DbContext context = null;
        context = (new ReflectionDbContextFactory()).createFromRootClass(rootClass, referenceTypeDetector);
        context.setSqliteDBAndroidStyle(db);
        context.createSchemaIfNotExists();
        return context;
    }

}
