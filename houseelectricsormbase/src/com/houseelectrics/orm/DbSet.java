package com.houseelectrics.orm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roberttodd on 04/01/2015.
 */
public class DbSet
{
    private EntityType entityType;
    public void setEntityType(EntityType value) {this.entityType = value;}
    public EntityType getEntityType() {return entityType; }

    private Map<Integer, EntityInstance> identityHashCodeToEntityInstance = new HashMap<Integer, EntityInstance>();
    private Map<Long, EntityInstance> dbId2EntityInstance = new HashMap<Long, EntityInstance>();

    public EntityInstance postInsert(long dbId, Object o)
    {
        EntityInstance ei = new EntityInstance();
        ei.setDbId(dbId);
        ei.setData(o);
        int identityHashCode= System.identityHashCode(o);
        if (identityHashCodeToEntityInstance.containsKey(identityHashCode))
        {
            EntityInstance eiOld = identityHashCodeToEntityInstance.get(identityHashCode);
            throw new RuntimeException("inserted object " + entityType.getType().getName() + " for dbID " + dbId +
             " but it was already known for dbId " + eiOld.getDbId());
        }
        if (dbId2EntityInstance.containsKey(dbId))
        {
            EntityInstance eiOld = dbId2EntityInstance.get(dbId);
            throw new RuntimeException("inserted object " + entityType.getType().getName() + " for dbID " + dbId +
                    " but that id is already known for an object  " + eiOld.getData());

        }

        identityHashCodeToEntityInstance.put(identityHashCode, ei);
        dbId2EntityInstance.put(dbId, ei);
        return ei;
    }

    public void postRetrieve(EntityInstance ei)
    {

        int identityHashCode= System.identityHashCode(ei.getData());

        if (identityHashCodeToEntityInstance.containsKey(identityHashCode))
        {
            //overwrite what is there - need to be careful !
            identityHashCodeToEntityInstance.remove(identityHashCode);
        }
        if (dbId2EntityInstance.containsKey(ei.getDbId()))
        {
            //overwrite what is there - need to be careful !
            dbId2EntityInstance.remove(ei.getDbId());
        }

        identityHashCodeToEntityInstance.put(identityHashCode, ei);
        dbId2EntityInstance.put(ei.getDbId(), ei);
    }

    public void postDelete(EntityInstance ei)
    {

        int identityHashCode= System.identityHashCode(ei.getData());

        if (identityHashCodeToEntityInstance.containsKey(identityHashCode))
        {
            //overwrite what is there - need to be careful !
            identityHashCodeToEntityInstance.remove(identityHashCode);
        }
        if (dbId2EntityInstance.containsKey(ei.getDbId()))
        {
            //overwrite what is there - need to be careful !
            dbId2EntityInstance.remove(ei.getDbId());
        }
    }


    public EntityInstance getOrCreateEntityInstanceForDbId(long dbId, SqliteDatabaseService.ObjectCreator objectCreator)
    {
        if (!dbId2EntityInstance.containsKey(dbId))
        {
            EntityInstance ei = new EntityInstance();
            ei.setDbId(dbId);
            try
            {
                Object data = objectCreator.newInstance(getEntityType().getType(), dbId);
                ei.setData(data) ;
            }
            catch (Exception ex)
            {
                throw new RuntimeException("failed to create domain object of type" + getEntityType().getType(), ex);
            }
            identityHashCodeToEntityInstance.put(System.identityHashCode(ei.getData()), ei);
            dbId2EntityInstance.put(ei.getDbId(), ei);

            return ei;
        }
        else
        {
            return dbId2EntityInstance.get(dbId);
        }
    }

    public EntityInstance getEntityInstanceForObject(Object o)
    {
        int identityHashCode= System.identityHashCode(o);
        if (!identityHashCodeToEntityInstance.containsKey(identityHashCode))
        {
            return null;
        }
        else
        {
            return identityHashCodeToEntityInstance.get(identityHashCode);
        }
    }

    public void clear()
    {
        identityHashCodeToEntityInstance.clear();
        dbId2EntityInstance.clear();
    }

}
