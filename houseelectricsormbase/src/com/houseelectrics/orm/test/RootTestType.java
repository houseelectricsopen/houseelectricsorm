package com.houseelectrics.orm.test;

/**
* Created by roberttodd on 25/02/2015.
*/
public class RootTestType
{
    private String name;
    public String getName() {return name;}
    public void setName(String value) {this.name = value;}

    private DetailTestType detail;
    public DetailTestType getDetail() {return detail;}
    public void setDetail(DetailTestType value) {this.detail=value;}
}
