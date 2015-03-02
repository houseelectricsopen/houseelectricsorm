package com.houseelectrics.orm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roberttodd on 05/01/2015.
 */
public class EntityInstance
{
    private long dbId;
    public long getDbId() {return dbId;}
    public void setDbId(long value) {this.dbId =value;}
    private Object data;
    public Object getData() {return data;}
    public void setData(Object value) {this.data=value;}
    private Map<String, Long> propertyName2ForeignKey = new HashMap<String, Long>();
    public Map<String, Long> getPropertyName2ForeignKey() {return propertyName2ForeignKey;}
    public void setPropertyName2ForeignKey(Map<String, Long> value) {this.propertyName2ForeignKey=value;}
}
