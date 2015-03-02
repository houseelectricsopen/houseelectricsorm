package com.houseelectrics.orm;

import com.houseelectrics.util.ReflectionUtil;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 06/01/2015.
 */
public abstract class SqliteDatabaseServiceBase implements SqliteDatabaseService
{
    protected List<FieldTypeMapping> fieldTypeMappings = new ArrayList<FieldTypeMapping>();
    protected List<FieldTypeMapping> getFieldTypeMappings() {return fieldTypeMappings;}
    protected void setFieldTypeMappings(List<FieldTypeMapping> value) {this.fieldTypeMappings=value;}

    protected void info(String str)  { if (getLogger()!=null) getLogger().info(str); }
    protected void sql(String str)
    {
        if (getLogger()!=null) getLogger().sql(str);
    }

    protected void error(String str, Throwable error)
    {
        if (getLogger()!=null) getLogger().error(str, error);
    }

    protected void error(String str)
    {
        if (getLogger()!=null) getLogger().error(str);
    }

    public String getSqliteType(EntityField field)
    {
        if (field.getIsReference() || field.getIsPolymorphic())
        {
            return "INTEGER";
        }
        FieldTypeMapping fieldTypeMapping = getFieldTypeMappingByField(field);
        if (fieldTypeMapping==null)
        {
            throw new RuntimeException("unable to detemine sql type for java type " + field.getProperty().getType().getName() +
                    " for field " + field.getProperty().getParentType().getName() + "." + field.getProperty().getPropertyName() +
                    ". try calling getType2Converter().put to add a JdbcTypeConverter for " +  field.getProperty().getType().getName() +
             " or alternatively map that type as a reference not a field"
            );
        }
        return fieldTypeMapping.getSqlitePropertyType();
    }

    public FieldTypeMapping getFieldTypeMappingByField(EntityField entityField)
    {
        return getFieldTypeMappingByPropertyTypes(entityField.getProperty().getType(), entityField.getProperty().getParametrisedTypes());
    }

    public FieldTypeMapping getFieldTypeMappingByPropertyTypes(Class type, Type[] parametrisedTypes)
    {
        for (int done=0; done<fieldTypeMappings.size(); done++)
        {
            FieldTypeMapping mapping = fieldTypeMappings.get(done);
            boolean isMatch = mapping.matches(type, parametrisedTypes);
            if (isMatch)
            {
                 return mapping;
            }
        }
        return null;
    }


   /* protected ReflectionUtil.ObjectCreator objectCreator = new ReflectionUtil.ObjectCreator()
    {
        @Override
        public Object newInstance(Object parentContext, String propertyName, Class theClass)
        {
            Object result = null;
            try {
                result = theClass.newInstance();
            }
            catch (Exception ex) {throw new RuntimeException("failed to create a " + theClass.getName(), ex);}
            return result;
        }

    };
*/
    @Override
    public Exception runInTransaction(SqliteDatabaseService.TransactionedCode code)
    {
        Exception error=null;
        try {
            beginTransaction();
            code.run(this);
            setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            error=ex;
            error("transaction failed ", ex);
        }
        finally
        {
            endTransaction();
        }
        return error;
    }
    public String getSqliteIdType() {return "INTEGER";}

    private Logger logger;
    public void setLogger(Logger value) {this.logger = value;}
    public Logger getLogger() {return logger;}

  }
