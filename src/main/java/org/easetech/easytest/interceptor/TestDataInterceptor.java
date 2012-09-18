
package org.easetech.easytest.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class TestDataInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.println("Method interceptor Called");
        System.out.println("Arguments to method are : " + args[0].toString() + " and " + args[1].toString());
        Object result = proxy.invokeSuper(obj, args);
        System.out.println("Result type is : " + result.getClass());
        System.out.println("Result is : " + result.toString());
        return result;

    }

}
