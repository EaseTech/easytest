package org.easetech.easytest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.easetech.easytest.converter.Converter;
import org.easetech.easytest.converter.ConverterManager;
import org.easetech.easytest.util.DataContext;





import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;

/**
 * An extension of Junit's {@link ParametersSuppliedBy} annotation that converts the data for Junit to consume. This
 * Annotation gives the ability to get the data per method rather than per class. Also Junit will automatically call the
 * method as many times as there are number of data sets to be run against that particular method. For example, if the
 * user has specified 5 data set for a single junit method, Junit will call the method five times, each time providing
 * the test data that was provided by the user.
 * <br>The annotation is normally used in conjunction with {@link DataLoader} annotation although it can be used with it as well.</br>
 * <br>
 * The annotation contains a single optional field :
 * 
 * <li><B> name</B> : the name of the parameter(OPTIONAL) as is present in the input test data file.
 * <li>In case the param name value is not specified and the Parameter type is Map, {@link DataSupplier} simply
 * provides the HashMap instance that was created while loading the data. This {@link HashMap} represents a 
 * single set of test data for the test method.</li>
 * <li> In case the param name is not specified and the parameter type is a strongly typed Object of the client using this annotation(for eg LibraryId),
 * it will try to search for the parameter with the name which is the class name of the Object parameter.</li>
 * <li>In case the param name is specified the framework will look for the parameter with the specified name in the loaded test data.
 * 
 *  Moreover, the framework supports PropertyEditors support for strongly typed objects.
 *  If you have a custom object and its property editor in the same package, the JUnit framework 
 *  will convert the String value to your specified custom object by calling the right property editor and pass an instance of custom object to your test case.
 *  This provides the users facility to write test cases such as this :
 *  <code>
 *   @Test
 *   @DataLoader(filePaths ={ "getItemsData.csv" })
 *   public void testWithStrongParameters(@Param()
 *   LibraryId id , @Param(name="itemid") ItemId itemId) {
 *      ....
 *
 *   }
 *  </code>
 * <br><li>Example of using Map to get the entire data:</li></br>
 * <br>
 * <code><br>
 *   @Test
 *   @DataLoader(filePaths= {"getItemsData.csv" })<br>
 *   public void testGetItemsWithoutFileType(<B>@Paramr()</B> Map<String, String> inputData) {<br>
 *      ........
 *
 *   }</code>
 * 
 * @author Anuj Kumar
 */
@Retention(RetentionPolicy.RUNTIME)
@ParametersSuppliedBy(Param.DataSupplier.class)
@Target({ElementType.METHOD , ElementType.TYPE , ElementType.PARAMETER})
public @interface Param {

    /** The name of the parameter for which value needs to be fetched from the data set */
    String name() default "";

    /**
     * Static class that overrides the getValueSources method of {@link ParameterSupplier} to return the data in Junit
     * Format which is a list of {@link PotentialAssignment}. This is the place where we can specify what the data type of the returned data would be. We can also
     * specify different return types for different test methods.
     * 
     */
    static class DataSupplier extends ParameterSupplier {

        /**
         * Overridden method to return the list of data for the given Junit method
         * 
         * @param signature the {@link ParameterSignature}
         * @return the list of {@link PotentialAssignment}
         */
        @Override
        public List<PotentialAssignment> getValueSources(ParameterSignature signature) {
            Param provider = signature.getAnnotation(Param.class);
            String value = DataContext.getMethodName();
            if(value == null){
                Assert.fail("The framework could not locate the test data for the test method. If you are using TestData annotation, make sure you specify the test method name in the data file. " +
                		"In case you are using ParametersSuppliedBy annotation, make sure you are using the right ParameterSupplier subclass.");
            }
            List<PotentialAssignment> listOfData = null;
            Map<String, List<Map<String, Object>>> data = DataContext.getData();
            List<Map<String, Object>> methodData = data.get(value);
            if(methodData == null){
                Assert.fail("Data does not exist for the specified method with name :" + value + " .Please check " +
                		"that the Data file contains the data for the given method name. A possible cause could be spelling mismatch.");
            }
            if (signature.getType().isAssignableFrom(Map.class)) {
                listOfData = convert(data.get(value));
            } else {
                listOfData = convert(signature.getType(), provider.name(), data.get(value));
            }
            return listOfData;
        }
        
        /**
         * Method that returns a list of {@link PotentialAssignment} that contains map value. This is the map of values
         * that the user can use to fetch the values it requires on its own.
         * 
         * @param convertFrom the data to convert from
         * @return a list of {@link PotentialAssignment} that contains map value
         */
        private List<PotentialAssignment> convert(List<Map<String, Object>> convertFrom) {
            List<PotentialAssignment> finalData = new ArrayList<PotentialAssignment>();
            for (Map<String, Object> map : convertFrom) {
                finalData.add(PotentialAssignment.forValue("", map));
            }
            return finalData;
        }

        

        /**
         * Method that returns a list of {@link PotentialAssignment} that contains the value as specified by idClass parameter.
         * @param idClass the class object that dictates the type of data that will be present in the list of {@link PotentialAssignment}
         * @param paramName the optional name of the parameter with which to search for the data.
         * @param convertFrom the list of raw data read from the CSV file.
         * @return list of {@link PotentialAssignment}
         */
        private List<PotentialAssignment> convert(Class<?> idClass, String paramName, List<Map<String, Object>> convertFrom) {
            List<PotentialAssignment> finalData = new ArrayList<PotentialAssignment>();
            PropertyEditor editor = PropertyEditorManager.findEditor(idClass);
            if (editor != null) {
                for (Map<String, Object> object : convertFrom) {
                    if (paramName != null && !"".equals(paramName)) {
                        if(getStringValue(paramName, object) != null){
                            editor.setAsText(getStringValue(paramName, object));
                            object.remove(paramName);
                        }
                        
                    } else {
                        editor.setAsText(getStringValue(idClass.getSimpleName(), object));
                        object.remove(idClass.getSimpleName());
                    }
                    if (editor.getValue() != null) {
                        finalData.add(PotentialAssignment.forValue("", editor.getValue()));
                    }
                    
                }

            }else{
                //Try to find the Converter
                Converter<?> converter = ConverterManager.findConverter(idClass);
                if(converter != null){
                    for(Map<String, Object> object : convertFrom){
                        finalData.add(PotentialAssignment.forValue("", converter.convert(object)));
                    }
                }else{
                    Assert.fail("Could not find either Editor or Converter instance for class :"  + idClass);
                }
                
            }
            return finalData;
        }
        
        /**
         * Util method to get the String value
         * @param paramName the name of the parameter to get the String value for
         * @param data the data that contains the include Holdings value
         * @return String value or null if it is not set in the data.
         */
        private static String getStringValue(String paramName , Map<String , Object> data){
            String value = null;
            if(data.get(paramName) != null){
                value = data.get(paramName).toString();
            }          
            return value;
        }
    }
}
