package com.houseelectrics.orm;
import com.houseelectrics.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.List;
/**
 * Created by roberttodd on 06/01/2015.
 * these are a subset of android SQLiteDatabase
 */
public interface SqliteDatabaseService
{
    public interface ObjectCreator
    {
        public Object newInstance(Class theClass, Long dbId) throws Exception;
    }

    public void execSQL(String sql);

    public interface ForeignKeyLookup
    {
        public Long getIdForDomainObject(Object domainObject, EntityField referencingField);
    }
    public long insertRow (EntityType entityType, Object domainObject,  ForeignKeyLookup foreignKeyLookup);
    public void updateRow (EntityType entityType, EntityInstance entityInstance,  ForeignKeyLookup foreignKeyLookup);
    public int deleteRows (EntityType entityType, String where, String []whereParams);

    // note the idea of Cursor has been parked !
    public List<EntityInstance> queryRows (DbSet dbSet, ObjectCreator objectCreator, String where, String[] whereParams/*, String groupBy, String having, String orderBy, String limit*/);
    public List<Object> executeRawQuery(final Class resultClass, final String strSelection,  ObjectCreator objectCreator) throws Exception;
    public List<Object> excecuteRawQuerySingleColumn(final String strSelection) throws Exception;

    public void beginTransaction ();
    public void setTransactionSuccessful ();
    public void endTransaction ();
    public Exception runInTransaction(TransactionedCode code);

    public interface TransactionedCode
    {
        public void run(SqliteDatabaseService db) throws Exception;
    }

    public String getSqliteType(EntityField field);

    public interface FieldTypeMapping
    {
        public String getSqlitePropertyType();
        public boolean matches(Class type, Type[] genericTypeParamers);
    }

   public interface Logger
   {
       public void info(String message);
       public void error(String message);
       public void error(String message, Throwable error);
       public void sql(String sql);

   }

   public void setLogger(Logger value);
   public Logger getLogger();

}
