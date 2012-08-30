package org.easytest.loader;

import java.util.List;
import java.util.Map;

/**
 * An interface for different types of loader. 
 * This would ultimately be used by the users of JUnit as well to provide their custom Loaders.
 * The work for that will begin soon. 
 * 
 * @author Anuj Kumar
 *
 */
public interface Loader {
    
    /**
     * Method responsible to Load the test data from the list of files passed as parameter
     * @param filePaths the list of files from which to load the data
     * @return a Map consisting of the methodName as key and a List of Key/value pairs as the value of the Map.
     * This is currently not a user friendly way of exposing the test data. 
     * TODO:This may change in future to something thats easier for the users to follow and supply.
     */
    Map<String, List<Map<String, Object>>> loadData(String[] filePaths);

}
