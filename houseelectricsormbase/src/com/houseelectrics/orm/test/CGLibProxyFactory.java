package com.houseelectrics.orm.test;

import com.houseelectrics.orm.DbContext;
import com.houseelectrics.orm.EntityInstance;
import com.houseelectrics.orm.ProxyFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.LazyLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roberttodd on 28/02/2015.
 */
public class CGLibProxyFactory implements ProxyFactory
{

    @Override
    public boolean isProxy(Object domainObject)
    {
        return domainObject instanceof Factory;
    }

    /*
        Object newProxyInstance(final Class theClass, final Long dbId)
        {
            final Object proxies[] = {null};
            final LazyLoader lazyLoader = new LazyLoader()
            {
                @Override
                public Object loadObject() throws Exception
                {
                    EntityInstance ei = loadEntityInstanceById(theClass, dbId);
                    proxyId2EntityInstance.put(System.identityHashCode(proxies[0]) , ei);
                    return ei.getData();
                }
            };
            proxies[0] = Enhancer.create(theClass, lazyLoader);
            return proxies[0];
        }

        public boolean isProxy(Object domainObject)
        {
            return domainObject instanceof Factory;
        }
    */
    @Override
    public Object createProxy(final Class theClass, final Long dbId, final ObjectLoader objectLoader)
    {
        final Object proxies[] = {null};
        final LazyLoader lazyLoader = new LazyLoader()
        {
            @Override
            public Object loadObject() throws Exception
            {
                return objectLoader.loadObjectForProxy(theClass, dbId, proxies[0]);
            }
        };
        proxies[0] = Enhancer.create(theClass, lazyLoader);
        return proxies[0];
    }




}
