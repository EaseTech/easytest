
package org.easetech.easytest.example;

import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.loader.LoaderType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(org.easetech.easytest.runner.DataDrivenTest.class)
@DataLoader(filePaths = { "testExcelData.xls" }, loaderType = LoaderType.EXCEL)
public class TestExcelDataLoader {

    /**
     * An instance of logger associated with the test framework.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(TestExcelDataLoader.class);

    @Test
    public void getExcelTestData(@Param(name = "libraryId")
    Float libraryId, @Param(name = "itemId")
    Float itemId) {
        System.out.print("Executing getExcelTestData :");
        System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
    }

    @Test
    // @DataLoader(filePaths={"overrideExcelData.csv"} , loaderType=LoaderType.CSV)
    public void getExcelTestDataWithDouble(@Param(name = "libraryId")
    Double libraryId, @Param(name = "itemId")
    Double itemId) {
        System.out.print("Executing getExcelTestDataWithDouble :");
        // if(itemId.equals(11568.0D)){
        // Assert.fail("ItemId is 11568 but should be 2");
        // }
        System.out.println("LibraryId Anuj is :" + libraryId + " and Item Id is :" + itemId);
    }

    @Test
    public void getExcelTestDataWithString(@Param(name = "libraryId")
    String libraryId, @Param(name = "itemId")
    String itemId) {
        System.out.print("Executing getExcelTestDataWithString :");
        System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
    }

    @Test
    public void getExcelTestDataNumberFormat() {
        System.out.print("Executing getExcelTestDataNumberFormat :");
        System.out.println("This is a simple test");
    }

    @Test
    @DataLoader(filePaths = { "testExcelData.xls" }, loaderType = LoaderType.EXCEL)
    public Item getExcelTestDataWithReturnType(@Param(name = "libraryId")
    Float libraryId, @Param(name = "itemId")
    Float itemId) {
        LOG.debug("Executing  getExcelTestDataWithReturnType : ");
        LOG.debug("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
        ItemService itemService = new RealItemService();
        Item item = itemService.findItem(new LibraryId(Long.valueOf(libraryId.longValue())),
            new ItemId(Long.valueOf(itemId.longValue())));
        LOG.debug("return item: " + item.toString());
        return item;
    }

}
