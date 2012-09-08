package org.easetech.easytest.loader;

import java.util.Collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataConverter {
    
    public static Map<String , List<Map<String , Object>>> convert(Map<String , List<Map<String , Object>>> from , Class<?> currentTestClass){
        Map<String , List<Map<String , Object>>> result = new HashMap<String, List<Map<String,Object>>>();
        for(String method : from.keySet()){
            List<Map<String , Object>> value = from.get(method);
            for(Map<String , Object> singleTestMethod : value){
                result.put(getFullyQualifiedTestName(method, currentTestClass).concat(singleTestMethod.toString()), Collections.singletonList(singleTestMethod));
            }
            
        }
        return result;
    }
    
    public static String getFullyQualifiedTestName(String testMethod, Class testClass){
    	return testClass == null ? testMethod : testClass.getName().concat(":").concat(testMethod);
    }
    
    public static String getTruncatedMethodName(String testMethod, Class testClass){
    	String methodName = testMethod;
    	if(testMethod.startsWith(testClass.getName())){
    		methodName = testMethod.replaceAll(testClass.getName(), "");
    	}
    	return methodName;
    }
    
    public static Map<String , List<Map<String , Object>>> appendClassName(Map<String , List<Map<String , Object>>> from , Class<?> currentTestClass){
        Map<String , List<Map<String , Object>>> result = new HashMap<String, List<Map<String,Object>>>();
        for(String method : from.keySet()){
            result.put(getFullyQualifiedTestName(method, currentTestClass),from.get(method));         
        }
        return result;
    }

}
