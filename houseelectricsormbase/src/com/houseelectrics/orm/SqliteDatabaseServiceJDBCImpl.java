package com.houseelectrics.orm;
import com.houseelectrics.util.ReflectionUtil;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

/**
 * Created by roberttodd on 06/01/2015.
 */
public class SqliteDatabaseServiceJDBCImpl extends SqliteDatabaseServiceBase
{


    private JdbcSqliteTypeConverter identityKeyJdbcSqliteTypeConverter;

    public SqliteDatabaseServiceJDBCImpl()
    {
        (new JdbcSqliteTypeConverterFactory()).createDefaultTypeConverters(fieldTypeMappings);
        identityKeyJdbcSqliteTypeConverter = (JdbcSqliteTypeConverter)getFieldTypeMappingByPropertyTypes(Long.class, null);
    }


    public void addHighPriorityMapping(JdbcSqliteTypeConverter fieldTypeMapping)
    {
          fieldTypeMappings.add(0, fieldTypeMapping);
    }



    public interface JdbcSqliteTypeConverter extends FieldTypeMapping
    {
        public void write(PreparedStatement ps, Object value, int zeroIndex) throws SQLException;
        public Object read(ResultSet rs, int zeroIndex) throws SQLException;
    }


    @Override
    public void execSQL(final String sql)
    {
        ConnectionRunnable runner = new ConnectionRunnable()
        {
            @Override
            public Object run(Connection connection) throws Exception
            {
                Statement st = connection.createStatement();
                //info(sql);
                sql(sql);
                st.execute(sql);
                return null;
            }
        };
        try
        {
            runOnSqliteDb(runner);
        }
        catch (Exception ex)
        {
            error(sql, ex);
//            throw new RuntimeException("unrecoverable error", ex);
        }
    }

    private void setUpateOrInsertColumns(PreparedStatement ps, EntityType entityType, Object domainObject, ForeignKeyLookup foreignKeyLookup) throws Exception
    {
        for (int done= 0; done < entityType.getFields().size(); done++)
        {
            EntityField entityField  = entityType.getFields().get(done);
            Object value = entityField.getProperty().get(domainObject);
            //Class type = entityField.getProperty().getType();
            JdbcSqliteTypeConverter jdbcSqliteTypeConverter =  (JdbcSqliteTypeConverter) getFieldTypeMappingByField(entityField);
            if (entityField.getIsReference())
            {
                if (value!=null)
                {
                    value = foreignKeyLookup.getIdForDomainObject(value, entityField);
                    if (value == null)
                    {
                        throw new RuntimeException("cant save row for " + entityType.getType().getName() + " because child " + entityField.getProperty().getPropertyName() + " has not been saved");
                    }
                }
                jdbcSqliteTypeConverter = identityKeyJdbcSqliteTypeConverter;
                if (jdbcSqliteTypeConverter==null)
                {
                    throw new RuntimeException("unsupported field type " + entityField.getProperty().getType().getName());
                }
            }
            setUpateOrInsertColumn(ps, done, jdbcSqliteTypeConverter, value);
        }
    }

    private void setUpateOrInsertColumn(PreparedStatement ps, int index, JdbcSqliteTypeConverter jdbcSqliteTypeConverter, Object value) throws SQLException
    {
        jdbcSqliteTypeConverter.write(ps, value, index);
    }

    @Override
    public long insertRow (final EntityType entityType, final Object domainObject,  final ForeignKeyLookup foreignKeyLookup)
    {
                ConnectionRunnable runner = new ConnectionRunnable()
                {
                    @Override
                    public Object run(Connection connection) throws Exception
                    {
                        PreparedStatement ps = getInsertPreparedStatement(entityType, connection);
                        setUpateOrInsertColumns(ps, entityType, domainObject, foreignKeyLookup);
                        int affected =  ps.executeUpdate();
                        if (affected!=1)
                        {
                            throw new RuntimeException("expected 1 insert for table " + entityType.getTablename()+ " but got " + affected);
                        }
                        //sbSql.append(" " + ps.);
                        String strLastidSQL= "SELECT last_insert_rowid()";
                        sql(strLastidSQL);
                        PreparedStatement psRowId = connection.prepareStatement(strLastidSQL);
                        ResultSet rs = psRowId.executeQuery();
                        long id = rs.getLong(1);
                        rs.close();
                        return id;
                    }
                };
Long id = null;
        try
        {
            id = (Long) runOnSqliteDb(runner);
        }
        catch (Exception ex)
        {
            error("insert failed", ex);
//            throw new RuntimeException("unrecoverable error", ex);
        }
        return id;
    }

    @Override
    public void updateRow(final EntityType entityType, final EntityInstance ei,  final ForeignKeyLookup foreignKeyLookup)
    {
        ConnectionRunnable runner = new ConnectionRunnable()
        {
            @Override
            public Object run(Connection connection) throws Exception
            {
                PreparedStatement ps = getUpdatePreparedStatement(entityType, connection);
                setUpateOrInsertColumns(ps, entityType, ei.getData(), foreignKeyLookup);
                ps.setLong(entityType.getFields().size()+1, ei.getDbId());
                int altered = ps.executeUpdate();
                if (altered!=1)
                {
                    throw new RuntimeException("expected 1 update for id " + entityType.getTablename()+ ":" + ei.getDbId() + " but got " + altered);
                }
                return altered;
            }
        };

        try
        {
            runOnSqliteDb(runner);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("unrecoverable error", ex);
        }
    }

    @Override
    public int deleteRows (final EntityType entityType, final String where, final String []whereParams)
    {
        final String rowDescription = entityType.getTablename() + " " + where;
        ConnectionRunnable runnable = new ConnectionRunnable()
        {
            @Override
            public Object run(Connection connection) throws Exception
            {
                String strSql = "delete from " + entityType.getTablename() + " where " + where;
                sql(strSql);
                PreparedStatement ps = connection.prepareStatement(strSql);
                for (int wi=0; whereParams!=null &&  wi<whereParams.length; wi++)
                {
                    ps.setString(wi+1, whereParams[wi]);
                }
                int updateCount = ps.executeUpdate();
            /*    if (updateCount>1)
                {
                    throw new RuntimeException("delete " +  rowDescription+ " resulted in " + updateCount + "updates");
                }
                else if (updateCount==0)
                {
                    sqlWarn("delete " + rowDescription + " resulted in no updates");
                }*/
                return updateCount;
            }
        };
        Integer updateCount = null;
        try
        {
            updateCount = (Integer) runOnSqliteDb(runnable);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("failed to delete " + rowDescription, ex);
        }
        return updateCount==null ? 0: updateCount.intValue();
    }




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

        ConnectionRunnable runnable = new ConnectionRunnable()
        {
            @Override
            public Object run(Connection connection) throws Exception
            {
                List<Object> results = new ArrayList<Object>();
                sql(strSelection);
                PreparedStatement preparedStatement = connection.prepareStatement(strSelection);
                ResultSet rs = preparedStatement.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                for (;rs.next();)
                {
                    Object item = resultClass.newInstance();
                    for (int colsDone=0; colsDone<rsmd.getColumnCount(); colsDone++)
                        {
                        String path = rsmd.getColumnName(colsDone+1);
                        ReflectionUtil.PropertyReference prop = ReflectionUtil.getPropertyReferenceForKeyPathWithIndexes(item, path, false, true, objectCreator1);
                        Type[] parametizedTypes = ReflectionUtil.getParameterizedTypesForProperty(prop.getParent().getClass(), prop.getPropertyName());
                        prop.setParametrisedTypes(parametizedTypes);
                        Object value = null;

                        JdbcSqliteTypeConverter jdbcSqliteTypeConverter = (JdbcSqliteTypeConverter) getFieldTypeMappingByPropertyTypes(prop.getType(), prop.getParametrisedTypes());
                        if (jdbcSqliteTypeConverter==null)
                        {
                            throw new RuntimeException("unsupported column type " + prop.getType().getName());
                        }
                        value = jdbcSqliteTypeConverter.read(rs, colsDone);
                        prop.set(value);
                        }
                    results.add(item);
                }
                return results;
            }
        };
        List<Object> results = (List<Object>) runOnSqliteDb(runnable);
        return results;
    }

    public List<Object> excecuteRawQuerySingleColumn(final String strSelection) throws Exception
    {
        ConnectionRunnable runnable = new ConnectionRunnable()
        {
            @Override
            public Object run(Connection connection) throws Exception
            {
                List<Object> results = new ArrayList<Object>();
                sql(strSelection);
                PreparedStatement preparedStatement = connection.prepareStatement(strSelection);
                ResultSet rs = preparedStatement.executeQuery();
                for (;rs.next();)
                {
                    Object value = rs.getObject(1);
                    results.add(value);
                }
                return results;
            }
        };
        List<Object> results = (List<Object>) runOnSqliteDb(runnable);
        return results;
    }

    @Override
    public List<EntityInstance> queryRows (final DbSet dbSet, final ObjectCreator objectCreator, String where, final String[] whereParams/*, String groupBy, String having, String orderBy, String limit*/)
    {
        EntityType entityType = dbSet.getEntityType();
        StringBuffer sbSelect = new StringBuffer();
        sbSelect.append("select ");
        for (int done=0; done<entityType.getFields().size(); done++)
        {
            EntityField field = entityType.getFields().get(done);
            sbSelect.append(field.getProperty().getPropertyName());
            sbSelect.append(",");
        }
        sbSelect.append(EntityType.DefaultIDColumn);
        sbSelect.append(" from ");
        sbSelect.append(entityType.getTablename());
        if (where != null)
        {
            sbSelect.append(" where ");
            sbSelect.append(where);
        }

        final String strSelect = sbSelect.toString();
        final List<EntityInstance> eis = new ArrayList<EntityInstance>();
        ConnectionRunnable runnable = new ConnectionRunnable()
        {
            @Override
            public Object run(Connection connection) throws Exception
            {
                sql(strSelect);
                PreparedStatement ps = connection.prepareStatement(strSelect);

                for (int done=0; whereParams!=null && done<whereParams.length; done++)
                {
                    ps.setString(done+1, whereParams[done]);
                }
                ResultSet rs = ps.executeQuery();
                for (;rs.next();)
                {
                    EntityInstance ei = readEntity(dbSet, objectCreator, rs);
                    eis.add(ei);
                }
                rs.close();
                return null;
            }
        };
        try
        {
            runOnSqliteDb(runnable);
        }
            catch (Exception ex)
            {
                throw new RuntimeException("unrecoverable error", ex);
            }
        return eis;
    }

    public EntityInstance readEntity(final DbSet dbSet, ObjectCreator objectCreator, final ResultSet rs) throws Exception
    {
        EntityInstance ei = null;//new EntityInstance();
        EntityType entityType = dbSet.getEntityType();
        long dbId = rs.getInt(entityType.getFields().size()+1);
        ei = dbSet.getOrCreateEntityInstanceForDbId(dbId, objectCreator);

        for (int done=0; done<entityType.getFields().size(); done++)
        {
            EntityField field = entityType.getFields().get(done);
            ReflectionUtil.PropertyReference propertyRef = field.getProperty();
            String columnName = propertyRef.getPropertyName();
            if (field.getIsReference())
            {
                Long refId = rs.getLong(columnName);
                if (rs.wasNull()) refId = null;
                ei.getPropertyName2ForeignKey().put(columnName, refId);
                continue;
            }
            Object value = null;
            JdbcSqliteTypeConverter jdbcSqliteTypeConverter = (JdbcSqliteTypeConverter) getFieldTypeMappingByField(field);
            if (jdbcSqliteTypeConverter==null)
            {
                throw new RuntimeException("unsupported column type " +  propertyRef.getType().getName());
            }
            value = jdbcSqliteTypeConverter.read(rs, done);
            propertyRef.set(ei.getData(), value);
        }
        //Long dbId = rs.getLong(entityType.getFields().size()+1);
        //ei.setDbId(dbId);
        return ei;
    }

    // single threaded !
    private boolean inTransaction=false;
    private Connection connection4Transaction = null;
    private boolean transactionSucccessful=false;

    @Override
    public void beginTransaction()
    {
       if (inTransaction) {throw new RuntimeException("transaction already running ! single threaded single transaction only !");}
       inTransaction=true;
       transactionSucccessful=false;
        connection4Transaction=null;
    }

    @Override
    public void setTransactionSuccessful()
    {
        transactionSucccessful=true;
    }

    @Override
    public void endTransaction()
    {
        try
        {
            if (transactionSucccessful) connection4Transaction.commit();
            else connection4Transaction.rollback();
        }
        catch (Exception ex)
        {
            throw new RuntimeException("unable to endTransaction transactionSuccessful==" + transactionSucccessful, ex);
        }
        try {connection4Transaction.close();}
        catch (Exception ex)     {  }
        inTransaction=false;
        connection4Transaction=null;
    }

/*    @Override
    public void error(String message, Throwable tw)
    {
        System.err.println(message);
        tw.printStackTrace();
    }
*/
    private String dbName;
    public String getDbName() {return dbName;}
    public void setDbName(String value) {this.dbName=value;}

    public Object runOnSqliteDb(ConnectionRunnable runnable) throws Exception
    {
        if (dbName==null)
        {
            throw new RuntimeException("dbName not set");
        }
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try
        {
            if (!inTransaction || connection4Transaction==null)
            {
                // create a database connection
                /*
                private final Properties connectionProperties = new Properties();
SQLiteConfig config = new SQLiteConfig();
config.enforceForeignKeys(true);
connectionProperties = config.toProperties();
private final String connectionString = String.format("jdbc:sqlite:%s", absolute_path_to_sqlite_db);
Connection connection = DriverManager.getConnection(connectionString, connectionProperties);
                 */
                Properties connectionProperties = new Properties();
                SQLiteConfig config = new SQLiteConfig();
                config.enforceForeignKeys(true);
                connectionProperties = config.toProperties();

                connection = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".db");
                connection4Transaction=connection;
                if (inTransaction) connection4Transaction.setAutoCommit(false);
            }
            if (inTransaction)
            {
                connection=connection4Transaction;
            }
            return runnable.run(connection);
        }
        finally
        {
            try
            {
                if (connection != null && !inTransaction)
                    connection.close();
            } catch (SQLException e)
            {
                // connection close failed.
                System.err.println(e);
            }
        }
    }

    interface ConnectionRunnable
    {
        public Object run(Connection connection) throws Exception;
    }

    PreparedStatement getUpdatePreparedStatement(EntityType entityType, Connection connection) throws Exception
    {
        StringBuffer sbUpdate  = new StringBuffer();
        sbUpdate.append("update ");
        sbUpdate.append(entityType.getTablename());
        sbUpdate.append(" ");
        for (int done=0; done<entityType.getFields().size(); done++)
        {
            EntityField field = entityType.getFields().get(done);
            if (done==0) sbUpdate.append("SET ");
            else       sbUpdate.append(",");
            sbUpdate.append(field.getProperty().getPropertyName());
            sbUpdate.append("=? ");
        }
        sbUpdate.append(" where " + EntityType.DefaultIDColumn +"=?");
        String strUpdate = sbUpdate.toString();
        sql(strUpdate);
        PreparedStatement ps = connection.prepareStatement(strUpdate);
        return ps;

    }

    PreparedStatement getInsertPreparedStatement(EntityType entityType, Connection connection) throws Exception
    {
        StringBuffer sbInsert  = new StringBuffer();
        sbInsert.append("insert into ");
        sbInsert.append(entityType.getTablename());
        sbInsert.append(" (");
        for (int done=0; done<entityType.getFields().size(); done++)
        {
            EntityField field = entityType.getFields().get(done);
            if (done>0) sbInsert.append(",");
            sbInsert.append(field.getProperty().getPropertyName());
        }
        sbInsert.append(") values (");
        for (int done=0; done<entityType.getFields().size(); done++)
        {
            if (done>0) sbInsert.append(",");
            sbInsert.append("?");
        }
        sbInsert.append(")");
        String strInsert = sbInsert.toString();
        sql(strInsert);
        PreparedStatement ps = connection.prepareStatement(strInsert);
        return ps;

    }

    public static SqliteDatabaseServiceJDBCImpl createCleanDb(String dbPath)
    {
        return createOrConnectToDb(dbPath, true);
    }


    public static SqliteDatabaseServiceJDBCImpl createOrConnectToDb(String dbPath, boolean clean)
    {
        SqliteDatabaseServiceJDBCImpl db = new SqliteDatabaseServiceJDBCImpl();
        db.setDbName(dbPath);
        File file = new File(dbPath + ".db");
        if (file.exists() && clean)
        {
            file.delete();
        }
        Logger logger = new Logger()
        {
            @Override
            public void info(String message)
            {
                System.out.println(message);
            }

            @Override
            public void error(String message)
            {
                RuntimeException ex = new RuntimeException(message);
                ex.fillInStackTrace();
                throw ex;
            }
            @Override
            public void error(String message, Throwable error)
            {
                 error.printStackTrace();
                throw new RuntimeException("unrecoverable error: " + message, error);
            }

            @Override
            public void sql(String sql)
            {

            }
        };
        db.setLogger(logger);
        return db;
    }

}
