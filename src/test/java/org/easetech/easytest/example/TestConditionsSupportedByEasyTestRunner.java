
package org.easetech.easytest.example;

import java.beans.PropertyEditorManager;
import java.util.Map;
import junit.framework.Assert;

import org.easetech.easytest.example.editors.LibraryIdEditor;
import org.easytest.annotation.DataLoader;
import org.easytest.annotation.Param;
import org.easytest.converter.ConverterManager;
import org.easytest.loader.LoaderType;
import org.easytest.runner.EasyTestRunner;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * 
 * An example test class that tries to list different scenarios of using {@link EasyTestRunner} and its supports annotations and classes.
 * We are loading the test data at the class level, but the user can override the data at the method level as well.
 *
 */
@RunWith(EasyTestRunner.class)
@DataLoader(filePaths = { "getItemsData.csv" }, loaderType = LoaderType.CSV)
public class TestConditionsSupportedByEasyTestRunner {
    
    /**
     * A Junit annotated({@link Rule}) expected Exception rule that gives us the ability to specify 
     * what exception is expected to be be thrown by the test case.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    /**
     * Example showing the usage of propertyEditors for getting custom object.
     */
    @BeforeClass
    public static void setUp(){
        PropertyEditorManager.registerEditor(LibraryId.class, LibraryIdEditor.class);
        ConverterManager.registerConverter(ItemConverter.class);
    }

    /**
     * Test case that uses {@link TestData} annotation with both the available attributes : filepaths and fileTye.
     * The CSV file in this case is present in the src/test/resources folder
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    public void testGetItems(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
    /**
     * Test case that uses {@link TestData} annotation with the available attribute : filepaths only.
     * The CSV file in this case is present in the src/test/resources folder
     * @param inputData a generic map of input test data that contains all the required parameters for the test data.
     */
    @Test
    public void testGetItemsWithoutFileType(@Param()
    Map<String, String> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }
    
    /**
     * Test with Strong Parameters
     * 
     */
    @Test
    public void testGetItemsWithoutFileTypeWithStrongParameters(@Param()
    LibraryId id , @Param(name="itemid") ItemId itemId) {
        System.out.println("library Id : " + id + " and itemId :" + itemId);

    }
    
    @Test
    public void testGetItemsWithoutFileTypeWithStrongParameters(){
        thrown.expect(RuntimeException.class);
        throw new RuntimeException("ExceptionTest"); 

    }
    
    /**
     * Test case showing the use of {@link DataLoader} annotation.
     * This example can also be used as a test to using PropertyEditors 
     * that are registered by the test class itself.
     * @param inputData
     */
    @Test
    @DataLoader(loader = CustomObjectDataLoader.class)
    public void testGetItemsWithCustomLoader(@Param()
    Map<String, Object> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId"));

    }
    
    /**
     * Test case that uses {@link ItemConverter} to convert from a Hashmap to an Item object instance.
     * @param item an instance of Item object that is automatically converted from a map to an Item instance.
     */
    @Test
    public void testConverter(@Param() Item item){
        Assert.assertNotNull(item);
        System.out.println(item.getDescription() + item.getItemId() + item.getItemType());
        
    }
}
