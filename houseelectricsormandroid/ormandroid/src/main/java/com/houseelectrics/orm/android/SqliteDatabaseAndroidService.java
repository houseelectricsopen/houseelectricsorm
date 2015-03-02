package com.houseelectrics.orm.android;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import com.houseelectrics.orm.*;
import com.houseelectrics.util.ReflectionUtil;
//import com.houseelectrics.orm.android.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by roberttodd on 06/01/2015.
 */
public class SqliteDatabaseAndroidService extends SqliteDatabaseServiceBase
{
    private SQLiteDatabase database;
    public SQLiteDatabase getDatabase() {return database;}
    public void setDatabase(SQLiteDatabase value) {this.database=value;}

    private AndroidSqliteTypeConverter identityFieldMapping;
    /*protected void sqlLog(String str)
    {
        android.util.Log.i(getClass().getName() + ".sql", str);
    }
*/
    public void addHighPriorityMapping(AndroidSqliteTypeConverter fieldTypeMapping)
    {
        fieldTypeMappings.add(0, fieldTypeMapping);
    }

    public SqliteDatabaseAndroidService()
    {
        (new AndroidSqliteTypeConverterFactory()).createDefaultTypeConverters(fieldTypeMappings);
        identityFieldMapping = (AndroidSqliteTypeConverter) getFieldTypeMappingByPropertyTypes(Long.class, null);
    }

    @Override
    public void execSQL(String s)
    {
            sql(s);
            getDatabase().execSQL(s);
    }

    public interface AndroidSqliteTypeConverter extends FieldTypeMapping
    {
        public void write(ContentValues contentValues, String propertyName,  Object value);
        public Object read(Cursor rs, int zeroIndex);
    }

    protected void transferToContentValues(EntityType entityType, Object o, ContentValues contentValues, ForeignKeyLookup foreignKeyLookup)
    {
        for (EntityField entityField :  entityType.getFields())
        {
            ReflectionUtil.PropertyReference prop = entityField.getProperty();
            Object value=null;
            try
            {
                value = prop.get(o);
                if (entityField.getIsReference())
                {
                    if (value!=null)
                    {
                        value = foreignKeyLookup.getIdForDomainObject(value, entityField);
                        if (value == null)
                        {
                            error("cant save row for " + entityType.getType().getName() + " because child " + entityField.getProperty().getPropertyName() + " has not been saved");
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                error("unrecoverable error getting property " + o.getClass().getName() + "." + prop.getPropertyName(), ex);
                //throw new RuntimeException(, ex);
            }
            if (value == null)
            {
                contentValues.putNull(prop.getPropertyName());
            }
            else
            {
                AndroidSqliteTypeConverter mapping = (entityField.getIsReference())?identityFieldMapping: (AndroidSqliteTypeConverter) getFieldTypeMappingByField(entityField);
                if (mapping==null)
                {
                    error( "insertRow with column type " + prop.getType().getName() + " not implemented");
                }
                mapping.write(contentValues, prop.getPropertyName(), value);
            }
        }
    }

    @Override
    public long insertRow(EntityType entityType, Object domainObject, ForeignKeyLookup foreignKeyLookup)
    {
        ContentValues contentValues = new ContentValues();
        transferToContentValues(entityType, domainObject, contentValues, foreignKeyLookup);
        long rowId = database.insert(entityType.getTablename(), null, contentValues);
        sql("insertRow " + entityType.getTablename() + "." + rowId);
        return rowId;
    }

    @Override
    public void updateRow(EntityType entityType, EntityInstance entityInstance, ForeignKeyLookup foreignKeyLookup)
    {
        ContentValues contentValues = new ContentValues();
        transferToContentValues(entityType, entityInstance.getData(), contentValues, foreignKeyLookup);
        String whereArgs[] = {""+entityInstance.getDbId()} ;
        sql("update " + entityType.getTablename() + "." + whereArgs[0]);
        database.update(entityType.getTablename(), contentValues, EntityType.DefaultIDColumn + "=?", whereArgs);
    }

    @Override
    public int deleteRows(EntityType entityType, String where, String []whereParams)
    {
        sql("delete " + entityType.getTablename());
        return getDatabase().delete(entityType.getTablename(), where, whereParams);
    }



    public EntityInstance readEntityRow(final DbSet dbSet, ObjectCreator objectCreator, final Cursor rs) throws Exception
    {
        final EntityType entityType = dbSet.getEntityType();
        long dbId = rs.getLong(entityType.getFields().size());
        EntityInstance ei = dbSet.getOrCreateEntityInstanceForDbId(dbId, objectCreator);
        //ei.setData(entityType.getType().ne);
        for (int done=0; done<entityType.getFields().size(); done++)
        {
            EntityField field = entityType.getFields().get(done);
            ReflectionUtil.PropertyReference propertyRef = field.getProperty();
            String columnName = propertyRef.getPropertyName();
            if (field.getIsReference())
            {
                Long refId = rs.isNull(done)?null:rs.getLong(done);
                ei.getPropertyName2ForeignKey().put(columnName, refId);
                continue;
            }
            Object value = null;
            Class type = propertyRef.getType();
            AndroidSqliteTypeConverter mapping = (AndroidSqliteTypeConverter) getFieldTypeMappingByField(field);
            if (mapping!=null)
            {
                value = mapping.read(rs, done);
            }
            else
            {
                error("unsupported column type " +  propertyRef.getType().getName());
            }
            propertyRef.set(ei.getData(), value);
        }
        return ei;
    }


    @Override
    public List<EntityInstance> queryRows(DbSet dbSet, ObjectCreator objectCreator, String where, String[] whereParams)
    {
        final EntityType entityType = dbSet.getEntityType();
        String columns[] = new String[entityType.getFields().size() + 1];
        List<EntityField> fields = entityType.getFields();
        int done=0;
        for (; done < (columns.length-1); done++)
        {
            columns[done] = fields.get(done).getProperty().getPropertyName();
        }
        columns[done]=EntityType.DefaultIDColumn;

        StringBuffer sb = new StringBuffer();
        sb.append("queryRows " + entityType.getTablename() + " " + where + " ");
        for (int wpd=0; whereParams!=null && wpd<whereParams.length; wpd++) {sb.append(whereParams[wpd] + " ");}
        sql(sb.toString());
        Cursor resultSet = database.query(entityType.getTablename(), columns, where, whereParams/*selectionArgs*/, null/*groupBy*/, null/*having*/, null/*orderBy*/);
        List<EntityInstance> result = new ArrayList<EntityInstance>();
        for (resultSet.moveToFirst(); !resultSet.isAfterLast(); resultSet.moveToNext() )
        {
            try
            {
                EntityInstance ei = readEntityRow(dbSet, objectCreator, resultSet);
                result.add(ei);
            }
            catch (Exception ex)
            {
                error("queryRows" , ex);
            }
        }
        return result;
    }

    @Override
    public List<Object> executeRawQuery(final Class resultClass, final String strSelection, final ObjectCreator objectCreator) throws Exception
    {
        final ReflectionUtil.ObjectCreator objectCreator1 = new ReflectionUtil.ObjectCreator()
        {
            @Override
            public Object newInstance(Object parentContext, String propertyName, Class theClass)
            {
                try
                {
                    return objectCreator.newInstance(theClass, null);
                }
                catch (Exception ex)
                {
                    throw new RuntimeException("cant create an instance of " + theClass.getName());
                }

            }
        };

        List<Object> results = new ArrayList<Object>();
        sql(strSelection);
        Cursor rs = getDatabase().rawQuery(strSelection, null);
        for (rs.moveToFirst(); !rs.isAfterLast(); rs.moveToNext())
        {
            Object item =  objectCreator.newInstance(resultClass, null);//  resultClass.newInstance();
            for (int colsDone=0; colsDone<rs.getColumnCount(); colsDone++)
            {
                Object value = null;
                String path = rs.getColumnName(colsDone);
                ReflectionUtil.PropertyReference prop = ReflectionUtil.getPropertyReferenceForKeyPathWithIndexes(item, path, false, true, objectCreator1);
                prop.setParametrisedTypes(ReflectionUtil.getParameterizedTypesForProperty(resultClass, prop.getPropertyName()));

                AndroidSqliteTypeConverter mapping = (AndroidSqliteTypeConverter) getFieldTypeMappingByPropertyTypes(prop.getType(), prop.getParametrisedTypes());
                if (mapping==null)
                {
                    error("unsupported column type " + prop.getType().getName());
                }
                value = mapping.read(rs, colsDone);
                prop.set(value);
            }
            results.add(item);
        }
        return results;
    }

    @Override
    public List<Object> excecuteRawQuerySingleColumn(String strSelection) throws Exception
    {
        List<Object> results = new ArrayList<Object>();
        sql(strSelection);
        Cursor rs = getDatabase().rawQuery(strSelection, null);
        for (rs.moveToFirst(); !rs.isAfterLast(); rs.moveToNext())
        {
            Object value;
            CursorWindow cursorWindow = ((SQLiteCursor)rs).getWindow();
            int pos = rs.getPosition();
            if (cursorWindow.isNull(pos, 0)) {
                value=null;
            } else if (cursorWindow.isLong(pos, 0)) {
                value = rs.getInt(0);
            } else if (cursorWindow.isFloat(pos, 0)) {
                value=rs.getDouble(0);
            } else if (cursorWindow.isString(pos, 0)) {
                value = rs.getString(0);
            } else if (cursorWindow.isBlob(pos, 0)) {
                value = rs.getBlob(0);
            }
            else value = rs.getString(0);
            /*
            note this version only works after sdk version 11
            int fieldType = rs.getType(0);
            switch (fieldType)
            {
              case Cursor.FIELD_TYPE_BLOB:
                  value=rs.getBlob(0);
              break;
                case Cursor.FIELD_TYPE_FLOAT:
                    value=rs.getDouble(0);
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    value=rs.getLong(0);
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    value=null;
                    break;
              case Cursor.FIELD_TYPE_STRING:
              default:
                  value = rs.getString(0);
            };*/

            results.add(value);
        }
        return results;
    }

    @Override
    public void beginTransaction()
    {
        getDatabase().beginTransaction();
    }

    @Override
    public void setTransactionSuccessful()
    {
       getDatabase().setTransactionSuccessful();
    }

    @Override
    public void endTransaction()
    {
        getDatabase().endTransaction();
    }


    }
