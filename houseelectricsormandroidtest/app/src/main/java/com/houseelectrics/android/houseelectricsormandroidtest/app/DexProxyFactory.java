package com.houseelectrics.android.houseelectricsormandroidtest.app;

import com.google.dexmaker.stock.ProxyBuilder;
import com.houseelectrics.orm.ProxyFactory;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by roberttodd on 01/03/2015.
 */
public class DexProxyFactory
{
   public ProxyFactory createProxyFactory(final File dexCacheFile)
    {
        ProxyFactory proxyFactory = new ProxyFactory()
        {
            @Override
            public boolean isProxy(Object domainObject)
            {
                return domainObject.getClass().getName().endsWith("_Proxy");
            }

            @Override
            public Object createProxy(final Class theClass, final Long dbId, final ObjectLoader objectLoader)
            {
                final Object domainObjects[] = {null};
                final Object proxies[] = {null};
                InvocationHandler handler = new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (domainObjects[0]==null)
                        {
                            domainObjects[0] = objectLoader.loadObjectForProxy(theClass, dbId, proxies[0]);
                        }
                        Object result = method.invoke(domainObjects[0], args);
                        return result;
                    }
                };

                try
                {
                    proxies[0] = ProxyBuilder.forClass(theClass)
                            .dexCache(dexCacheFile)
                            .handler(handler)
                            .build();

                    for (Class interfaceClass : proxies[0].getClass().getInterfaces())
                    {
                        String strInterface  = interfaceClass.getName();
                        android.util.Log.i(getClass().getName(),  "proxy interface: " + strInterface);
                    }
                    for (Class aClass = proxies[0].getClass(); aClass!=null; aClass=aClass.getSuperclass())
                    {
                        android.util.Log.i(getClass().getName(),  "proxy superClass: " + aClass.getName());
                    }
                }
                catch (Exception ex)
                {
                    throw new RuntimeException("couldnt create proxy for a " + theClass.getName(), ex);
                }
                return proxies[0];
            }
        };
        return proxyFactory;
    }
}
