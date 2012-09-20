package org.easetech.easytest.loader;

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
     * The key identifying the actual output result that needs to be written to the file.
     */
    String ACTUAL_RESULT = "ActualResult";
    
    /**
     * Method responsible to Load the test data from the list of files passed as parameter
     * @param filePaths the list of files from which to load the data
     * @return a Map consisting of the methodName as key and a List of Key/value pairs as the value of the Map.
     * This is currently not a user friendly way of exposing the test data. 
     */
    Map<String, List<Map<String, Object>>> loadData(String[] filePaths);
    
    /**
     * Method responsible for writing the test data and actual result back to the file
     * @param filePath the path to the file to which data needs to be written
     * @param actualData a Map consisting of the methodName as key and a List of Key/value pairs as the value of the Map. 
     * This Map contains the input as well as output data 
     * This is currently not a user friendly way of exposing the test data. 
     */
    void writeData(String filePath, Map<String, List<Map<String, Object>>> actualData);

}
