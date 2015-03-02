package com.houseelectrics.orm;

/**
 * Created by roberttodd on 28/02/2015.
 */
public interface ProxyFactory
{
    public interface ObjectLoader
    {
        public Object loadObjectForProxy(Class theClass, Long dbId, Object proxy);
    }
    public boolean isProxy(Object domainObject);
    public Object createProxy(Class theClass, Long dbId, ObjectLoader objectLoader);
}
