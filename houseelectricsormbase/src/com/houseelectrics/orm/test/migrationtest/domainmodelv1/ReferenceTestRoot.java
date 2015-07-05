package com.houseelectrics.orm.test.migrationtest.domainmodelv1;

/**
 * Created by roberttodd on 23/06/2015.
 */
public class ReferenceTestRoot
{
    private String fieldA;
    public String getFieldA() {return fieldA;}
    public void setFieldA(String value) {this.fieldA=value;}

    private SubItem subItem;
    public SubItem getSubItem()  { return subItem;  }
    public void setSubItem(SubItem subItem) {this.subItem=subItem;}

}
