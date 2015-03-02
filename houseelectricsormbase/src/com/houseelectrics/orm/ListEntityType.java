package com.houseelectrics.orm;

import com.houseelectrics.util.ReflectionUtil;
import java.lang.reflect.Type;

/**
 * Created by roberttodd on 09/01/2015.
 */
public class ListEntityType extends EntityType
{
@Override
public Class getType() {return ListEntityRowData.class;}

public boolean isPolymorphic() {return false;}

private ReflectionUtil.PropertyReference property;
public ReflectionUtil.PropertyReference getProperty() {return property;}
public void setProperty(ReflectionUtil.PropertyReference value) {this.property=value;}

public Class getDetailType()
    {
        Type[] types = getProperty().getParametrisedTypes();
        return (Class) types[0];
    }

public ListEntityRowData createEntityRowData(Long childId, Long masterDbId, int orderNumber, Object subObject)
{
    ListEntityType.ListEntityRowData listLink = new ListEntityType.ListEntityRowData();
    listLink.setChildId(childId);
    listLink.setMasterId(masterDbId);
    listLink.setOrderNumber(orderNumber);
    return listLink;
}

public static class ListEntityRowData
{
    public final static String MasterIdColumnName = "MasterId";
    private Long masterId;
    public Long getMasterId() {return masterId;}
    public void setMasterId(Long value) {this.masterId=value;}

    private Long childId;
    public Long getChildId() {return childId;}
    public void setChildId(Long value) {this.childId=value;}

    private int orderNumber;
    public int getOrderNumber() {return orderNumber;}
    public void setOrderNumber(int value) {this.orderNumber=value;}

}


}
