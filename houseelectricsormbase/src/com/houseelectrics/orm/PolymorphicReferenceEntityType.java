package com.houseelectrics.orm;

/**
 * Created by roberttodd on 09/01/2015.
 */
public class PolymorphicReferenceEntityType extends EntityType
{
@Override
public Class getType() {return PolymorphicReferenceRowData.class;}

public static class PolymorphicReferenceRowData
{
    private String className;
    public String getClassName() {return className;}
    public void setClassName(String value) {this.className=value;}

    private long id;
    public long getId() {return id;}
    public void setId(long value) {this.id=value;}
}

}
