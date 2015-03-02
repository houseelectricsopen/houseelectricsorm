package com.houseelectrics.orm;

import com.houseelectrics.util.ReflectionUtil;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 04/01/2015.
 */
public class ReflectionDbContextFactory
{

    public interface ReferenceTypeDetector
    {
        public boolean isReferenceType(Class type, Type[] genericParameters);
        public boolean isPolymorphicProperty(Class type, Type[] genericParameters);
    }

    public interface PropertyPersistenceFilter
    {
        public boolean isPersistent(ReflectionUtil.PropertyReference propertyReference);
    }

    public DbContext createFromRootClass(Class rootClass)
    {
        return createFromRootClass(rootClass, null);
    }

    public DbContext createFromRootClassAssumeReferencesWithinPackageOrLists(Class rootClass)
    {
        final String packageName = rootClass.getPackage().getName();
        ReflectionDbContextFactory.ReferenceTypeDetector referenceTypeDetector = new ReflectionDbContextFactory.ReferenceTypeDetector()
        {
            @Override
            public boolean isReferenceType(Class type, Type[] genericTypeParameters)
            {
                boolean isList = List.class.isAssignableFrom(type);
                boolean isInThisPackage = type.getPackage() != null && type.getPackage().getName().startsWith(packageName);
                return isList || isInThisPackage;
            }

            @Override
            public boolean isPolymorphicProperty(Class type, Type[] genericParameters)
            {
                return false;
            }
        };
        return createFromRootClass(rootClass, referenceTypeDetector);
    }


    public DbContext createFromRootClass(Class rootClass, ReferenceTypeDetector referenceTypeDetector)
    {
       return createFromRootClass(rootClass, referenceTypeDetector, null);
    }

    public DbContext createFromRootClass(Class rootClass, ReferenceTypeDetector referenceTypeDetector, PropertyPersistenceFilter propertyPersistenceFilter)
    {
        List<Class> rootClasses = new ArrayList<Class>();
        rootClasses.add(rootClass);
        return createFromRootClasses(rootClasses, referenceTypeDetector, propertyPersistenceFilter);
    }


    public DbContext createFromRootClasses(List<Class> rootClasses, ReferenceTypeDetector referenceTypeDetector, PropertyPersistenceFilter propertyPersistenceFilter)
    {
        DbContext context = new DbContext();
        List<EntityType> entityTypes = new ArrayList<EntityType>();
        context.setEntityTypeLeaseToMostDependant(entityTypes);

        List<Class> classes2Process = new ArrayList<Class>();
        List<Class> classesProcessed = new ArrayList<Class>();
        for (Class rootClass : rootClasses) {classes2Process.add(rootClass);}
        for (;classes2Process.size()>0;)
        {
            Class theClass = classes2Process.get(0);
            EntityType entityType = classToEntityType(theClass, referenceTypeDetector, propertyPersistenceFilter);
            entityTypes.add(entityType);
            for (EntityField field : entityType.getFields())
            {
                if (!field.getIsReference()) {continue;}
                Class refType = field.getProperty().getType();
                if (!field.getIsPolymorphic() && !classes2Process.contains(refType) && !classesProcessed.contains(refType))
                {
                    classes2Process.add(refType);
                }
            }
            for (ListEntityType let : entityType.getPropertyName2DetailEntityType().values())
            {
                Class detailType = let.getDetailType();
                boolean isPolymorphicType =  detailType==Object.class || referenceTypeDetector.isPolymorphicProperty(detailType, null) ;
                if (!isPolymorphicType && !classes2Process.contains(detailType) && !classesProcessed.contains(detailType))
                {
                    classes2Process.add(detailType);
                }
            }
            classes2Process.remove(theClass);
            classesProcessed.add(theClass);
        }

        return context;
    }

    EntityType classToEntityType(Class theClass, ReferenceTypeDetector referenceTypeDetector, PropertyPersistenceFilter propertyPersistenceFilter)
    {
        EntityType entityType;
        entityType = new EntityType();
        entityType.setTableName(theClass.getSimpleName());
        entityType.setType(theClass);
        List<ReflectionUtil.PropertyReference> properties = ReflectionUtil.getPublicReadWriteableProperties(theClass);
        List<EntityField> fields = new ArrayList<EntityField>();
        entityType.setFields(fields);
        for (int done=0; done<properties.size(); done++)
        {
            ReflectionUtil.PropertyReference prop = properties.get(done);
            prop.setParametrisedTypes(ReflectionUtil.getParameterizedTypesForPropertyByGetter(prop.getGetterMethod()));
            if (propertyPersistenceFilter!=null && !propertyPersistenceFilter.isPersistent(prop))
                {
                    continue;
                }
            //prop.setParametrisedTypes();
            boolean isList = List.class.isAssignableFrom(prop.getType());
            boolean isReferenceType = referenceTypeDetector != null && referenceTypeDetector.isReferenceType(prop.getType(), prop.getParametrisedTypes());
            boolean isPolymorphicType = referenceTypeDetector != null && referenceTypeDetector.isPolymorphicProperty(prop.getType(), prop.getParametrisedTypes());
            if (isPolymorphicType && !isReferenceType)
            {
                throw new RuntimeException("error in ReferenceTypeDetector: field:" + theClass.getName() +
                           "." + prop.getPropertyName()+ ":" + prop.getType().getSimpleName() + " isPolymorphicType==true but isReferenceType==false - but polymorphism is a quality of references ");
            }

//TODO check here whether this is a mapped type
            if (isList && isReferenceType)
            {
                Type []pts = prop.getParametrisedTypes();
                Class paramType0 = (pts==null || pts.length<1) ? null: (Class) pts[0];
                boolean isPolymorphicItemType = paramType0==null || referenceTypeDetector != null && paramType0!=null
                        && referenceTypeDetector.isPolymorphicProperty(paramType0, null) ;
                ListEntityType let = isPolymorphicItemType? (new PolymorphicListEntityType()) : (new ListEntityType());
                let.setTableName(entityType.getTablename() + "_" + prop.getPropertyName());
                entityType.getPropertyName2DetailEntityType().put(prop.getPropertyName(), let);
                let.setProperty(prop);
                populateFromClass(let, let.getType());
            }
            else
            {
                EntityField field = new EntityField();
                field.setProperty(prop);
                fields.add(field);

                if (isReferenceType)
                {
                    field.setIsReference(true);
                }
                if (isPolymorphicType)
                {
                    field.setIsPolymorphic(true);
                    PolymorphicReferenceEntityType pret = new PolymorphicReferenceEntityType();
                    pret.setTableName(entityType.getTablename() + "_" + prop.getPropertyName());
                    field.setPolymorphicReferenceEntityType(pret);
                    populateFromClass(pret, PolymorphicReferenceEntityType.PolymorphicReferenceRowData.class);
                }
            }
        }
        return entityType;
    }

    void populateFromClass(EntityType entityType, Class fromClass)
    {
        List<EntityField> fields = new ArrayList<EntityField>();
        entityType.setFields(fields);
        List<ReflectionUtil.PropertyReference> properties = ReflectionUtil.getPublicReadWriteableProperties(fromClass);
        for (int done=0; done<properties.size(); done++)
        {
            ReflectionUtil.PropertyReference prop = properties.get(done);

            EntityField field = new EntityField();
            field.setProperty(prop);
            fields.add(field);
        }
    }

}
