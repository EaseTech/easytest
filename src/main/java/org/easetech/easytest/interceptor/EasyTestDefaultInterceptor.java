
package org.easetech.easytest.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 
 * A default interceptor that simply prints the time taken by a method in nano seconds to the console.
 *
 */
public class EasyTestDefaultInterceptor implements MethodInterceptor {

    /**
     * Invoke the method with the advice
     * @param invocation
     * @return result of invoking the method
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        long startTime = System.nanoTime();
        Object result = invocation.getMethod().invoke(invocation.getThis(), invocation.getArguments());
        long duration = System.nanoTime() - startTime;
//        long timeInMilliSeconds = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);
//        System.out.println("Time taken by Method "+ invocation.getMethod().getName() + " : " + timeInMilliSeconds + " ms");
        System.out.println("Time taken by Method "+ invocation.getMethod().getName() + " : " + duration + " nanoseconds");
        return result;

    }

}
