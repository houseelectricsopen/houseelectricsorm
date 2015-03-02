package com.houseelectrics.orm;

/**
 * Created by roberttodd on 04/02/2015.
 */
public class PolymorphicListEntityType extends ListEntityType
{
    @Override
    public boolean isPolymorphic() {return true;}

    @Override
    public Class getType() {return PolymorphicListEntityRowData.class;}

    public static class PolymorphicListEntityRowData extends ListEntityRowData
    {
        private String className;
        public String getClassName() {return className;}
        public void setClassName(String value) {this.className=value;}
    }

    public Class getDetailType(PolymorphicListEntityRowData prd) throws Exception
    {
        try
        {
            return Class.forName(prd.getClassName());
        }
        catch (Exception ex)
        {
            throw new RuntimeException("cant create polymorphic detail type for class " + prd.getClassName() +
                    " in " + getTablename());
        }
    }

    public ListEntityRowData createEntityRowData(Long childId, Long masterDbId, int orderNumber, Object subObject)
    {
        PolymorphicListEntityType.PolymorphicListEntityRowData listLink = new PolymorphicListEntityType.PolymorphicListEntityRowData();
        listLink.setChildId(childId);
        listLink.setMasterId(masterDbId);
        listLink.setOrderNumber(orderNumber);
        listLink.setClassName(subObject==null?null:subObject.getClass().getName());
        return listLink;
    }

}
