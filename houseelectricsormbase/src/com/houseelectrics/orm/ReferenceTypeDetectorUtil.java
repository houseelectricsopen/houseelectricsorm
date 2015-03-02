package com.houseelectrics.orm;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by roberttodd on 01/03/2015.
 */
public class ReferenceTypeDetectorUtil
{
    public static ReflectionDbContextFactory.ReferenceTypeDetector thisPackageAndListsAsReferences(final Package thePackage)
    {
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = new ReflectionDbContextFactory.ReferenceTypeDetector()
        {
            @Override
            public boolean isReferenceType(Class type, Type[] genericTypeParameters)
            {
                boolean isList = List.class.isAssignableFrom(type);
                boolean isInThisPackage = type.getPackage() != null && type.getPackage().getName().startsWith(thePackage.getName());
                return isList || isInThisPackage;
            }

            @Override
            public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
            {
                return false;
            }
        };
        return referenceTypeDetector;
    }
}
