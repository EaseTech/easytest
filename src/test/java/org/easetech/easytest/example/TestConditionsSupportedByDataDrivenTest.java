
package org.easetech.easytest.example;

import org.easetech.easytest.runner.DataDrivenTest;

import java.beans.PropertyEditorManager;
import java.util.Map;
import junit.framework.Assert;
import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.converter.ConverterManager;
import org.easetech.easytest.example.editors.LibraryIdEditor;
import org.easetech.easytest.loader.LoaderType;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * 
 * An example test class that tries to list different scenarios of using {@link DataDrivenTest} and its supports
 * annotations and classes. We are loading the test data at the class level, but the user can override the data at the
 * method level as well.
 * 
 */
@RunWith(org.easetech.easytest.runner.DataDrivenTest.class)
@DataLoader(filePaths = { "getDDTData.csv" }, loaderType = LoaderType.CSV)
public class TestConditionsSupportedByDataDrivenTest {

    /**
     * A Junit annotated({@link Rule}) expected Exception rule that gives us the ability to specify what exception is
     * expected to be be thrown by the test case.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Example showing the usage of propertyEditors for getting custom object.
     */
    @BeforeClass
    public static void setUp() {
        PropertyEditorManager.registerEditor(LibraryId.class, LibraryIdEditor.class);
        ConverterManager.registerConverter(ItemConverter.class);
    }

    /**
     * Test DDT runner with a generic MAP parameter
     * 
     * @param inputData
     */
    @Test
    public void testDDTGetItem(@Param()
    Map<String, String> inputData) {
        System.out.print("Executing testDDTGetItem :");
        System.out.println("library Id : " + inputData.get("LibraryId") + " and item type : "
            + inputData.get("itemType") + " and search text array :" + inputData.get("searchText"));

    }

    /**
     * Test DDT with no parameter and Rule annotation
     */
    @Test
    public void testDDTGetItemsWithoutFileTypeWithStrongParameters() {
        System.out.print("Executing testDDTGetItemsWithoutFileTypeWithStrongParameters :");
        thrown.expect(RuntimeException.class);
        throw new RuntimeException("ExceptionTest");

    }

    /**
     * Test case showing the use of {@link DataLoader} annotation. This example can also be used as a test to using
     * PropertyEditors that are registered by the test class itself.
     * 
     * @param inputData
     */
    @Test
    @DataLoader(loader = CustomObjectDataLoader.class)
    public void testDDTGetItemsWithCustomLoader(@Param()
    Map<String, Object> inputData) {
        System.out.print("Executing testDDTGetItemsWithCustomLoader :");
        System.out.println("library Id : " + inputData.get("LibraryId"));

    }

    /**
     * Test case that uses {@link ItemConverter} to convert from a Hashmap to an Item object instance.
     * 
     * @param item an instance of Item object that is automatically converted from a map to an Item instance.
     */
    @Test
    public void testDDTConverter(@Param()
    Item item) {
        System.out.print("Executing testDDTConverter :");
        Assert.assertNotNull(item);
        System.out.println(item.getDescription() + item.getItemId() + item.getItemType());

    }
}
