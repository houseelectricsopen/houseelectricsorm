package com.houseelectrics.orm.test;

/**
* Created by roberttodd on 25/02/2015.
*/
public interface Asserter
{
    public void assertEqual(Object expected, Object actual);
    public void assertEqual(String message, Object expected, Object actual);
}
