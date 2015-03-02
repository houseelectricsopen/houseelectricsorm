package com.houseelectrics.orm;

import com.houseelectrics.util.ReflectionUtil;

/**
 * Created by roberttodd on 03/01/2015.
 */
public class EntityField
{
    private ReflectionUtil.PropertyReference property;
    public ReflectionUtil.PropertyReference getProperty() {return property;}
    public void setProperty(ReflectionUtil.PropertyReference value) {this.property=value;}
    private boolean isReference = false;
    public boolean getIsReference() {return isReference;}
    public void setIsReference(boolean value) {this.isReference=value;}
    private boolean isPolymorphic = false;
    public boolean getIsPolymorphic() {return isPolymorphic;}
    public void setIsPolymorphic(boolean value) {this.isPolymorphic=value;}
    private PolymorphicReferenceEntityType polymorphicReferenceEntityType;
    public PolymorphicReferenceEntityType getPolymorphicReferenceEntityType() { return polymorphicReferenceEntityType; }
    public void setPolymorphicReferenceEntityType(PolymorphicReferenceEntityType value) {this.polymorphicReferenceEntityType=value;}

}
