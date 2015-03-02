package com.houseelectrics.orm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by roberttodd on 03/01/2015.
 */
public class EntityType
{
    private String tableName;
    public String getTablename() {return tableName;}
    public void setTableName(String value) {this.tableName = value;}
    private Class type;
    public Class getType() {return type;}
    public void setType(Class value) {this.type=value;}
    private List<EntityField> fields;
    public  List<EntityField> getFields() {return fields;}
    public void setFields(List<EntityField> value) {this.fields=value;}
    public final static String DefaultIDColumn="rowid";

    private Map<String, ListEntityType> propertyName2DetailEntityType = new HashMap<String, ListEntityType>();
    public  Map<String, ListEntityType> getPropertyName2DetailEntityType() {return propertyName2DetailEntityType;}
    public void setPropertyName2DetailEntityType(Map<String, ListEntityType> value) {this.propertyName2DetailEntityType = value;}

    public EntityField findFieldByPropertyName(String name)
    {
        for (EntityField field : getFields())
        {
            if (field.getProperty().getPropertyName().equals(name))
            {
                return field;
            }
        }
        return null;
    }

    public ListEntityType findEntityListTypeByPropertyName(String detailPropertyName)
    {
        return propertyName2DetailEntityType.containsKey(detailPropertyName)? propertyName2DetailEntityType.get(detailPropertyName):null;
    }


}




