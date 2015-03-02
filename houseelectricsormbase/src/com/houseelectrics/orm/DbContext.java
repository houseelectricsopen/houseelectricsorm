package com.houseelectrics.orm;
import com.houseelectrics.util.ReflectionUtil;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by roberttodd on 04/01/2015.
TODO implement cascade delete
 */
public class DbContext implements ProxyFactory.ObjectLoader
{


    protected void warning(String str)
        {
            System.out.println("warning:" + str);
        }

    private SqliteDatabaseService sqliteDBAndroidStyle;
    public SqliteDatabaseService getSqliteDBAndroidStyle() {return  sqliteDBAndroidStyle;}
    public void setSqliteDBAndroidStyle(SqliteDatabaseService value ) {this.sqliteDBAndroidStyle=value;}

    private List<EntityType> entityTypeLeaseToMostDependant;
    public List<EntityType> getEntityTypeLeaseToMostDependant() {return entityTypeLeaseToMostDependant;}
    public void setEntityTypeLeaseToMostDependant(List<EntityType> value) {this.entityTypeLeaseToMostDependant=value;}

    private Map<Class, EntityType> type2EntityType = null;
    public  Map<Class, EntityType> getType2EntityType()
    {
        if (type2EntityType==null)
        {
            type2EntityType = new HashMap<Class, EntityType>();
            for(EntityType entityType : entityTypeLeaseToMostDependant)
            {
                type2EntityType.put(entityType.getType(), entityType);
            }
        }
        return type2EntityType;
    }

    private List<Object> modified = new ArrayList<Object>();
    private List<Object> deleted = new ArrayList<Object>();

    public EntityInstance getEntityInstanceForObject(Object o)
    {
        if (!getType2EntityType().containsKey(o.getClass()))
        {
            return null;
        }
        EntityType et = getType2EntityType().get(o.getClass());
        DbSet dbSet = getDbSetByEntityType(et);
        EntityInstance entityInstance = dbSet.getEntityInstanceForObject(o);
        return entityInstance;
    }


    public EntityInstance getOrCreateEntityInstance(Class domainObjectType, long dbId, SqliteDatabaseService.ObjectCreator objectCreator)
    {
        if (!getType2EntityType().containsKey(domainObjectType))
        {
            return null;
        }
        EntityType et = getType2EntityType().get(domainObjectType);
        DbSet dbSet = getDbSetByEntityType(et);
        EntityInstance entityInstance = dbSet.getOrCreateEntityInstanceForDbId(dbId, objectCreator);
        return entityInstance;
    }


    public void createSchemaIfNotExists() throws Exception
    {
        if (entityTypeLeaseToMostDependant==null)
          {
              throw new RuntimeException("trying to create schema but there are no entities");
          }

        SqliteDatabaseService.TransactionedCode code =
                new SqliteDatabaseService.TransactionedCode()
                {
                    @Override
                    public void run(SqliteDatabaseService db) throws Exception
                    {
                        for (int done = 0; done < entityTypeLeaseToMostDependant.size(); done++)
                        {
                            EntityType entityType = entityTypeLeaseToMostDependant.get(done);
                            for (String listPropName : entityType.getPropertyName2DetailEntityType().keySet())
                            {
                                ListEntityType let = entityType.getPropertyName2DetailEntityType().get(listPropName);
                                createTableIfNotExists(let, db);
                            }
                            for (EntityField entityField : entityType.getFields())
                            {
                                if (entityField.getIsPolymorphic())
                                {
                                    PolymorphicReferenceEntityType pret = entityField.getPolymorphicReferenceEntityType();
                                    createTableIfNotExists(pret, db);
                                }
                            }
                            createTableIfNotExists(entityType, db);
                        }
                    }
                };
        Exception error = sqliteDBAndroidStyle.runInTransaction(code);
        if (error!=null) { throw error;}
    }

    private Map<EntityType, DbSet> entityType2DbSet=new HashMap<EntityType, DbSet>();
    protected DbSet getDbSetByEntityType(EntityType entityType)
    {
        DbSet result=null;
        if (entityType==null)
        {
            return null;
        }
        if (!entityType2DbSet.containsKey(entityType))
        {
            result = new DbSet();
            result.setEntityType(entityType);
            entityType2DbSet.put(entityType, result);
        }
        else
        {
            result = entityType2DbSet.get(entityType);
        }
        return result;
    }

    protected SqliteDatabaseService.ObjectCreator nonProxyObjectCreator =
            new SqliteDatabaseService.ObjectCreator()
            {
                @Override
                public Object newInstance(Class theClass, Long dbId) throws Exception
                {
                    return theClass.newInstance();
                }
            };

    Object newProxyInstance(final Class theClass, final Long dbId)
    {
        if (proxyFactory==null)
        {
            throw new RuntimeException("no proxyFactory call setProxyFactory");
        }

        return proxyFactory.createProxy(theClass, dbId, this);
    }

    protected boolean isProxy(Object domainObject)
    {
        return proxyFactory!=null && proxyFactory.isProxy(domainObject);
    }

    private ProxyFactory proxyFactory=null;
    public ProxyFactory getProxyFactory() {return proxyFactory;}
    public void setProxyFactory(ProxyFactory value) {this.proxyFactory=value;}

    @Override
    public Object loadObjectForProxy(Class theClass, Long dbId, Object proxy)
    {
        EntityInstance ei = loadEntityInstanceById(theClass, dbId);
        proxyId2EntityInstance.put(System.identityHashCode(proxy) , ei);
        return ei.getData();
    }


    Map<Object, EntityInstance> proxyId2EntityInstance = new HashMap<Object, EntityInstance>();

    protected Object proxy2Object(Object proxyObject)
    {
        int proxyId = System.identityHashCode(proxyObject);
        return proxyId2EntityInstance.containsKey(proxyId)?proxyId2EntityInstance.get(proxyId).getData():null;
    }



    protected SqliteDatabaseService.ObjectCreator proxiedObjectCreator = null;
    protected SqliteDatabaseService.ObjectCreator getOrCreateProxiedObjectCreator()
    {
        if (proxiedObjectCreator==null)
        {
            proxiedObjectCreator = new SqliteDatabaseService.ObjectCreator()
            {
                @Override
                public Object newInstance(Class theClass, Long dbId) throws Exception
                {
                    // here !
                    return newProxyInstance(theClass, dbId);
                }
            };
        }
        return proxiedObjectCreator;
    }

    public void add(Object domainObject) throws Exception
    {
        if (domainObject==null)
        {
            throw new RuntimeException("cant insertInDb null");
        }

        Class theClass = domainObject.getClass();
        if (!getType2EntityType().containsKey(theClass))
        {
            throw new RuntimeException("class " + theClass.getName() + " not in data model !");
        }
        modified.add(domainObject);
    }

    SqliteDatabaseService.ForeignKeyLookup createDbIdLookup()
    {
        final DbContext dbContext = this;
        SqliteDatabaseService.ForeignKeyLookup dbIdLookup = new SqliteDatabaseService.ForeignKeyLookup()
        {
            @Override
            public Long getIdForDomainObject(Object domainObject, EntityField referencingField)
            {
                if (domainObject==null) return null;
                EntityInstance ei = dbContext.getEntityInstanceForObject(domainObject);
                if (ei==null)
                {
                    EntityType entityType = getType2EntityType().get(domainObject.getClass());
                    if (entityType==null)
                    {
                        throw new RuntimeException("unknown entityType for " + domainObject.getClass().getName());
                    }

                    try
                    {
                        ei = insertInDb(entityType, domainObject);
                    }
                    catch (Exception ex)
                    {
                        throw new RuntimeException("error inserting a " + domainObject.getClass().getName() , ex);
                    }
                }
                if (referencingField!=null && referencingField.getIsPolymorphic())
                {
                    //find the reference by classname and dbId
                    String strWhere = "className=? and id=?";
                    String[] whereParams = {domainObject.getClass().getName(), "" + ei.getDbId()};
                    DbSet dbSet = getDbSetByEntityType(referencingField.getPolymorphicReferenceEntityType());
                    SqliteDatabaseService.ObjectCreator creator = nonProxyObjectCreator;
                    List<EntityInstance> refEis = getSqliteDBAndroidStyle().queryRows(dbSet, creator, strWhere, whereParams);
                    if (refEis.size()>1) throw new RuntimeException("found " + refEis.size() + " polymorphic references to  " + domainObject.getClass().getName() + "." + ei.getDbId());
                    EntityInstance refEi=null;
                    if (refEis.size()==1)
                    {
                        refEi = refEis.get(0);
                    }
                    else
                    {
                        PolymorphicReferenceEntityType.PolymorphicReferenceRowData polymorphicReferenceRowData = new PolymorphicReferenceEntityType.PolymorphicReferenceRowData();
                        polymorphicReferenceRowData.setClassName(domainObject.getClass().getName());
                        polymorphicReferenceRowData.setId(ei.getDbId());
                        try
                        {
                            refEi = insertInDb(referencingField.getPolymorphicReferenceEntityType(), polymorphicReferenceRowData);
                        }
                        catch (Exception ex)
                        {
                            throw new RuntimeException("error inserting a reference to " + domainObject.getClass().getName() , ex);
                        }
                    }
                    ei=refEi;
                }

                return ei==null?null:ei.getDbId();
            }
        };
        return dbIdLookup;
    }

    SqliteDatabaseService.ForeignKeyLookup dbIdLookup = createDbIdLookup();


    protected void saveListProperty(Object parentDomainObject, String listPropertyName, SqliteDatabaseService.ForeignKeyLookup dbIdLookup) throws Exception
    {
        long dbId = dbIdLookup.getIdForDomainObject(parentDomainObject, null);
        EntityType entityType = getType2EntityType().get(parentDomainObject.getClass());
        ListEntityType let = entityType.getPropertyName2DetailEntityType().get(listPropertyName);
        String []whereParams= {""+dbId};
        //clear existing list links
        getSqliteDBAndroidStyle().deleteRows(let, ListEntityType.ListEntityRowData.MasterIdColumnName + "=?", whereParams);
        getDbSetByEntityType(let).clear();
        List<Object> list = (List<Object>) let.getProperty().get(parentDomainObject);
        for (int done=0; list!=null && done<list.size(); done++)
        {
            Object subObject = list.get(done);
            Long subId = dbIdLookup.getIdForDomainObject(subObject, null);
            ListEntityType.ListEntityRowData listLink = let.createEntityRowData(subId, dbId, done, subObject);
            long detailDbId = sqliteDBAndroidStyle.insertRow(let, listLink, dbIdLookup);
            EntityInstance linkEi =getDbSetByEntityType(let).postInsert(detailDbId, listLink);
        }
    }

    public EntityInstance insertInDb(final EntityType entityType, final Object domainObject) throws Exception
    {
        long dbId = sqliteDBAndroidStyle.insertRow(entityType, domainObject, dbIdLookup);
        EntityInstance ei =getDbSetByEntityType(entityType).postInsert(dbId, domainObject);
        for (String listPropertyName : entityType.getPropertyName2DetailEntityType().keySet())
        {
            saveListProperty(domainObject, listPropertyName, dbIdLookup);
        }
        return ei;
    }

    public void delete (Object domainObject)
    {
        if (!getType2EntityType().containsKey(domainObject.getClass()))
        {
            throw new RuntimeException("db delete of unknown type " + domainObject.getClass().getName());
        }
        if (!deleted.contains(domainObject))  deleted.add(domainObject);
    }

    public void update(Object domainObject)
    {
        if (isProxy(domainObject))
        {
            domainObject = proxy2Object(domainObject);
        }
        if (!getType2EntityType().containsKey(domainObject.getClass()))
        {
            StringBuffer sb = new StringBuffer();
            sb.append("Class Tree:" );
            for (Class theClass = domainObject.getClass(); theClass!=null; theClass=theClass.getSuperclass())
            {
                sb.append("/" + theClass.getName());
            }
            sb.append(" Interfaces:");
            for (Class theInt : domainObject.getClass().getInterfaces())
            {
                sb.append("/" + theInt.getName());
            }
            throw new RuntimeException("db update of unknown type " + domainObject.getClass().getName() + " details:" + sb.toString());
        }
        if (!modified.contains(domainObject)) modified.add(domainObject);
    }

    public void updateProperty(Object domainObject, String propertyName)
    {
        if (!getType2EntityType().containsKey(domainObject.getClass()))
        {
            throw new RuntimeException("db update property of unknown type " + domainObject.getClass().getName());
        }
        EntityType entityType = getType2EntityType().get(domainObject.getClass());
        if (!entityType.getPropertyName2DetailEntityType().containsKey(propertyName))
        {
            throw new RuntimeException("db update of non list type property" + domainObject.getClass().getName() + "." + propertyName);
        }
        ListPropertyModification lpm = new ListPropertyModification();
        lpm.parent=domainObject;
        lpm.propertyName=propertyName;
        modified.add(lpm);
    }

    static class PropertyModification
    {
        Object parent;
        String propertyName;
    }

    static class ListPropertyModification extends PropertyModification
    {
    }

    public void saveListPropertyChange(ListPropertyModification listPropertyModification) throws Exception
    {
         saveListProperty(listPropertyModification.parent, listPropertyModification.propertyName, dbIdLookup);
    }

    public void saveDomainObjectChange(Object domainObject) throws Exception
    {
        EntityType entityType = getType2EntityType().get(domainObject.getClass());

        DbSet dbSet = getDbSetByEntityType(entityType);
        EntityInstance ei = dbSet.getEntityInstanceForObject(domainObject);
        if (ei == null)
        {
            ei = insertInDb(entityType, domainObject);
        } else
        {
            sqliteDBAndroidStyle.updateRow(entityType, ei, dbIdLookup);
        }
    }

    public void saveChanges() throws Exception
    {
        for (;modified.size()>0;)
        {
            Object domainObject = modified.get(0);
            if (domainObject instanceof ListPropertyModification)
            {
                saveListPropertyChange((ListPropertyModification) domainObject);
            }
            else
            {
                saveDomainObjectChange(domainObject);
            }

            modified.remove(domainObject);
        }
        for (;deleted.size()>0;)
        {
            Object domainObject = deleted.get(0);
            EntityType entityType = getType2EntityType().get(domainObject.getClass());

            DbSet dbSet = getDbSetByEntityType(entityType);
            EntityInstance ei = dbSet.getEntityInstanceForObject(domainObject);
            if (ei == null)
            {
                warning("deleted untracked item of type " + domainObject.getClass().getName());
            } else
            {
                // TODO determine if this should be cascading relationships will need to be defined as owning or not !
                String strWhereParams[]={""+ei.getDbId()};
                int updateCount = sqliteDBAndroidStyle.deleteRows(entityType, EntityType.DefaultIDColumn + "=?", strWhereParams);
                if (updateCount>1)
                {
                    throw new RuntimeException("delete " +  entityType.getTablename() + "." + ei.getDbId() + " resulted in " + updateCount + "updates");
                }
                else if (updateCount==0)
                {
                    warning("delete " +  entityType.getTablename() + "." + ei.getDbId() + " resulted in no updates");
                }
            }
            dbSet.postDelete(ei);
            //TODO check for hanging references to deleted item !
            deleted.remove(domainObject);
        }
    }

    public List<Object> getAllByTypeLazyLoad(Class type) throws Exception
    {
        if (!getType2EntityType().containsKey(type))
        {
            throw new RuntimeException("type not in database: " + type.getName());
        }
        final EntityType entityType = getType2EntityType().get(type);
        DbSet dbSet = getDbSetByEntityType(entityType);
        List<EntityInstance> eis = sqliteDBAndroidStyle.queryRows(dbSet, nonProxyObjectCreator, null, null);
        List<Object> result = new ArrayList<Object>();
        for (EntityInstance ei : eis)
        {
            dbSet.postRetrieve(ei);
            result.add(ei.getData());
            createProxiesForReferenceFields(ei, entityType);
        }
        return result;
    }

    protected void createProxiesForReferenceFields(EntityInstance ei, EntityType entityType) throws Exception
    {
        Map<String, Long> propertyName2ForeignKey = ei.getPropertyName2ForeignKey();
        List<EntityField> entityFields = entityType.getFields();

        for (EntityField entityField : entityFields)
        {
            if (!entityField.getIsReference()) continue;
            Long foreignKey = null;
            Object value=null;
            ReflectionUtil.PropertyReference propertyReference = entityField.getProperty();
            String propertyName = propertyReference.getPropertyName();
            if (propertyName2ForeignKey.containsKey(propertyName))
            {
                foreignKey = propertyName2ForeignKey.get(propertyName);
            }
            if (foreignKey!=null)
            {
                value = getOrCreateProxiedObjectCreator().newInstance(propertyReference.getType(), foreignKey);
            }
            propertyReference.set(ei.getData(), value);
        }
    }

    public List<Object> getAllByTypeShallow(Class type) throws Exception
    {
        SqliteDatabaseService.ObjectCreator  creator = nonProxyObjectCreator;
        return getAllByTypeShallow(type, creator);
    }

    public List<Object> getAllByTypeShallow(Class type, SqliteDatabaseService.ObjectCreator  creator) throws Exception
    {
        if (!getType2EntityType().containsKey(type))
        {
            throw new RuntimeException("type not in database: " + type.getName());
        }
        final EntityType entityType = getType2EntityType().get(type);
        DbSet dbSet = getDbSetByEntityType(entityType);
        List<EntityInstance> eis = sqliteDBAndroidStyle.queryRows(dbSet, creator, null, null);
        List<Object> result = new ArrayList<Object>();
        for (EntityInstance ei : eis)
        {
            dbSet.postRetrieve(ei);
            result.add(ei.getData());
        }
        return result;
    }

    public Object lazyLoadDomainObjectById(Class domainObjectType, long dbId) throws Exception
    {
        EntityInstance ei;
        ei= loadEntityInstanceById(domainObjectType, dbId);
        if (ei==null)
        {
            return null;
        }
        else
        {
            EntityType entityType = getType2EntityType().get(domainObjectType);
            createProxiesForReferenceFields(ei, entityType);
        }
        return ei.getData();
    }

    protected EntityInstance loadEntityInstanceById(Class domainObjectType, long dbId)
    {
        String whereParams[] = {""+dbId};
        EntityType et=null;
        if (!getType2EntityType().containsKey(domainObjectType))
        {
            throw new RuntimeException("cant load non entity type " + domainObjectType.getName());
        }
        et = getType2EntityType().get(domainObjectType);
        DbSet dbSet = getDbSetByEntityType(et);
        SqliteDatabaseService.ObjectCreator  creator = nonProxyObjectCreator;
        List<EntityInstance> eis = getSqliteDBAndroidStyle().queryRows(dbSet, creator, EntityType.DefaultIDColumn + "=?", whereParams);
        if (eis.size()==0)
        {
            throw new RuntimeException("db does not contain " + et.getTablename() + ":" + domainObjectType.getName() + ":" + dbId);
        }
        EntityInstance rootEi = eis.get(0);
        getDbSetByEntityType(et).postRetrieve(rootEi);
        return rootEi;
    }


    public Object deepLoad(Class domainObjectType, long dbId) throws Exception
    {
        EntityInstance rootEi = loadEntityInstanceById(domainObjectType, dbId);
        return deepLoad(rootEi);
    }

    public Object deepLoad(Object domainObject) throws Exception
    {
        EntityInstance rootEi = getEntityInstanceForObject(domainObject);
        if (rootEi == null)
        {
            return null;
        }
        return deepLoad(rootEi);
    }

    public List createNonAbstractListInstance(ReflectionUtil.PropertyReference prop)
    {
        Object list=null;
        Class destinationType = prop.getType();
        if (Modifier.isAbstract(destinationType.getModifiers()))
        {
            destinationType=ArrayList.class;
        }
        try
        {
            list = destinationType.newInstance();
        }
        catch (Exception ex)
        {
           list=null;
        }
        if (list==null || !destinationType.isAssignableFrom(list.getClass()))
        {
            throw new RuntimeException("failed to create a non abstract instance of " + prop.getParentType().getName() + "." + prop.getPropertyName() + ":" + destinationType.getName());
        }
        return (List) list;
    }

    public Object deepLoad(EntityInstance rootEi) throws Exception
        {
        SqliteDatabaseService.ObjectCreator objectCreator = nonProxyObjectCreator;
        List<EntityInstance> entityInstances2Explore = new ArrayList<EntityInstance>();
        entityInstances2Explore.add(rootEi);
        for (;entityInstances2Explore.size()>0;)
        {
            EntityInstance ei = entityInstances2Explore.get(0);
            EntityType entityType = getType2EntityType().get(ei.getData().getClass());
            for (int done=0; done<entityType.getFields().size(); done++)
            {
                EntityField field = entityType.getFields().get(done);
                if (field.getIsReference())
                {
                    ReflectionUtil.PropertyReference fProp = field.getProperty();
                    Long fieldId = null;
                    fieldId = ei.getPropertyName2ForeignKey().get(fProp.getPropertyName());

                    if (fieldId==null)
                       {
                           fProp.set(ei.getData(), null);
                       }
                    else
                    {
                        EntityInstance eiField = loadAndSetReferenceField(field, fieldId, ei);
                        if (eiField!=null) entityInstances2Explore.add(eiField);
                    }
                }
            }
            for (String propertyName : entityType.getPropertyName2DetailEntityType().keySet())
            {
                ListEntityType let = entityType.getPropertyName2DetailEntityType().get(propertyName);
                List<Object> list = createNonAbstractListInstance(let.getProperty());
                let.getProperty().set(ei.getData(), list);
                String listWhereParams[] = {""+ei.getDbId()};
                DbSet listDbSet = getDbSetByEntityType(let);
                List<EntityInstance> listEis = getSqliteDBAndroidStyle().queryRows(listDbSet, objectCreator, ListEntityType.ListEntityRowData.MasterIdColumnName + "=?", listWhereParams);
                EntityType detailEntityType = null;
                if (!let.isPolymorphic())
                {
                    detailEntityType = this.getType2EntityType().get(let.getDetailType());
                }
                for (int itemsDone=0; itemsDone<listEis.size(); itemsDone++)
                {
                    EntityInstance listLinkEi = listEis.get(itemsDone);
                    listDbSet.postRetrieve(listLinkEi);
                    ListEntityType.ListEntityRowData lerd = null;

                  if (let.isPolymorphic())
                    {
                        PolymorphicListEntityType.PolymorphicListEntityRowData prd = (PolymorphicListEntityType.PolymorphicListEntityRowData) listLinkEi.getData();

                        Class polymorphicDetailType = prd.getChildId() == null ? null : ((PolymorphicListEntityType)let).getDetailType(prd);
                        detailEntityType = this.getType2EntityType().get(polymorphicDetailType);
                        lerd = prd;
                    }
                    else
                    {
                        lerd = (ListEntityType.ListEntityRowData) listLinkEi.getData();
                    }

                    if (detailEntityType==null && lerd.getChildId()!=null)
                    {
                        throw new RuntimeException("unknown detailEntityType for list " + entityType.getTablename() + "." + lerd.getMasterId() +"." + let.getTablename() + "." + lerd.getChildId());
                    }

                    Object listItem = null;
                    if (lerd.getChildId()!=null)
                    {
                        String[] detailWhereParams = {"" + lerd.getChildId()};
                        DbSet detailDbSet = getDbSetByEntityType(detailEntityType);
                        List<EntityInstance> detailEis = getSqliteDBAndroidStyle().queryRows(detailDbSet, objectCreator, EntityType.DefaultIDColumn + "=?", detailWhereParams);
                        EntityInstance detailEi = detailEis.get(0);
                        listItem = detailEi.getData();
                        getDbSetByEntityType(detailEntityType).postRetrieve(detailEi);
                        entityInstances2Explore.add(detailEi);
                    }
                    list.add(listItem);
                }
            }

            entityInstances2Explore.remove(ei);
        }
        return rootEi.getData();
    }

   /* private EntityInstance loadAndSetReferenceField(String propertyName, Long fieldId, EntityInstance ei) throws Exception
    {

    }*/

    private EntityInstance loadAndSetReferenceField(EntityField field, Long fieldId, EntityInstance ei) throws Exception
    {
        SqliteDatabaseService.ObjectCreator creator = nonProxyObjectCreator;
        Class fieldPropertyType=null;
        EntityType entityType = getType2EntityType().get(ei.getData().getClass());

        ReflectionUtil.PropertyReference fProp = field.getProperty();
        EntityType fieldEntityType = null;

        if (field.getIsPolymorphic() )
        {
            //todo refactor this !
            DbSet polyfieldDbSet = getDbSetByEntityType(field.getPolymorphicReferenceEntityType());
            String []polyWhereParams = {"" + fieldId};
            List<EntityInstance> polyEis = getSqliteDBAndroidStyle().queryRows(polyfieldDbSet, creator, EntityType.DefaultIDColumn + "=?", polyWhereParams);
            if (polyEis.size()==1)
            {
                EntityInstance eiPolyField = polyEis.get(0);
                polyfieldDbSet.postRetrieve(eiPolyField);
                PolymorphicReferenceEntityType.PolymorphicReferenceRowData rowData = (PolymorphicReferenceEntityType.PolymorphicReferenceRowData) eiPolyField.getData();
                fieldId = rowData.getId();
                String fieldPropertyTypeName = rowData.getClassName();
                try
                {
                    fieldPropertyType = Class.forName(fieldPropertyTypeName);
                }
                catch (Exception ex)
                {
                    throw new RuntimeException("cant load class " + fieldPropertyTypeName+ " for polymorphic reference field " + entityType.getTablename() + "." + ei.getDbId() +
                            "." + field.getProperty().getPropertyName());
                }
            } else
            {
                throw new RuntimeException("got " + polyEis.size() + " match for polymorphic reference field " + entityType.getTablename() + "." + ei.getDbId() +
                        "." + field.getProperty().getPropertyName());
            }
        }
        else
        {
            fieldPropertyType = fProp.getType();
        }
        fieldEntityType = getType2EntityType().get(fieldPropertyType);
        DbSet fieldDbSet = getDbSetByEntityType(fieldEntityType);
        Object currentRef = fProp.get(ei.getData());
        if (currentRef!=null && null==fieldDbSet.getEntityInstanceForObject(currentRef))
        {
            throw new RuntimeException("dealing with deep load when property populated but not saved " + entityType.getTablename() +
                    "." + field.getProperty().getPropertyName());
        }
        String []whereParams = {""+fieldId};
        List<EntityInstance> eis = getSqliteDBAndroidStyle().queryRows(fieldDbSet, creator, EntityType.DefaultIDColumn + "=?", whereParams);
        EntityInstance eiField = null;
        if (eis.size()==1)
        {
            eiField = eis.get(0);
            fProp.set(ei.getData(), eiField.getData());
            getDbSetByEntityType(fieldEntityType).postRetrieve(eiField);
        } else if (eis.size()>1)
        {
            throw new RuntimeException("got " + eis.size() + " match for detail field " + entityType.getTablename() + "." + ei.getDbId() +
                    "." + field.getProperty().getPropertyName());
        }
        else
        {
            fProp.set(ei.getData(), null);
        }
        return eiField;
    }

    public void  printCreateTableIfNotExists(EntityType entityType, StringBuffer sb)
    {
        sb.append("create table if not exists ");
        sb.append(/*entityType.getType().getSimpleName()*/entityType.getTablename());
        sb.append("(");
        String newLine = "\r\n";
        for (int done = 0; done < entityType.getFields().size(); done++)
        {
            EntityField field = entityType.getFields().get(done);

            //TODO implement foreign keys
            ReflectionUtil.PropertyReference prop =field.getProperty();
            String sqliteType = getSqliteDBAndroidStyle().getSqliteType(field);
            if (done>0) sb.append(",");
            sb.append(newLine);
            sb.append(prop.getPropertyName());
            sb.append(" ");
            sb.append(sqliteType);
        }
        sb.append(");");
    }

    public void createTableIfNotExists(EntityType entityType, SqliteDatabaseService db) throws Exception
    {
        StringBuffer sb = new StringBuffer();

        printCreateTableIfNotExists(entityType, sb);

        String createTableStatement = sb.toString();
        db.execSQL(createTableStatement);
    }

    public List<Object> excecuteRawQuery(Class resultClass, String strSelection) throws Exception
    {
        return getSqliteDBAndroidStyle().executeRawQuery(resultClass, strSelection, this.nonProxyObjectCreator);
    }

    public List<Object> excecuteRawQuerySingleColumn(String strSelection) throws Exception
    {
        return getSqliteDBAndroidStyle().excecuteRawQuerySingleColumn(strSelection);
    }


}
