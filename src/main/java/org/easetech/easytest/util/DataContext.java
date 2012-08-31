package org.easetech.easytest.util;

import java.util.List;
import java.util.Map;

/**
 * Data Context Holder for the test data and the corresponding test method.
 * 
 * @author Anuj Kumar
 *
 */
public class DataContext {
    
    /**
     * Private constructor
     */
    private DataContext(){
        //do nothing
    }
    
    /**
     * DataContext thread local variable that will hold the data for easy consumption by the test cases.
     */
    public static final ThreadLocal<Map<String, List<Map<String , Object>>>> dataContextThreadLocal = new ThreadLocal<Map<String, List<Map<String , Object>>>>();
    
    /**
     * Test Method Name Context thread local variable that will hold the name of the test method currently executing. This name is supplied as part of the @TestName annotation.
     */
    public static final ThreadLocal<String> nameContextThreadLocal = new ThreadLocal<String>();
    
    /**
     * Sets the data
     * 
     * @param  data to set
     */
    public static void setData(Map<String, List<Map<String , Object>>> data) {
        Map<String, List<Map<String , Object>>> testData = dataContextThreadLocal.get();
        if(testData == null || testData.isEmpty()){
            dataContextThreadLocal.set(data);
        }else{  
            for(String key : data.keySet()){
                testData.put(key, data.get(key));
            }
           dataContextThreadLocal.set(testData);
        }
        
    }

    /**
     * Returns the data
     * 
     * @return The data
     */
    public static Map<String, List<Map<String , Object>>> getData() {
        return dataContextThreadLocal.get();
    }

    /**
     * Clears the data
     */
    public static void clearData() {
        dataContextThreadLocal.remove();
    }
    
    public static String getMethodName(){
        return nameContextThreadLocal.get();
    }

    /**
     * Sets the data
     * 
     * @param  name to set
     */
    public static void setMethodName(String name) {
        nameContextThreadLocal.set(name);
    }
    
    /**
     * Clears the data
     */
    public static void clearNameData() {
        nameContextThreadLocal.remove();
    }

}
