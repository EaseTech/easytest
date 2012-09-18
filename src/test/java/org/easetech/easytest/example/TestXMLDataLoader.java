
package org.easetech.easytest.example;

import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.loader.LoaderType;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(org.easetech.easytest.runner.DataDrivenTest.class)
@DataLoader(filePaths = { "input-data.xml" }, loaderType = LoaderType.XML)
public class TestXMLDataLoader {

    @Test
    public void getItemsDataFromXMLLoader(@Param(name = "libraryId")
    String libraryId, @Param(name = "itemId")
    String itemId, @Param(name = "itemType")
    String itemType, @Param(name = "expectedItems")
    String expectedItems) {
        System.out.print("Executing getItemsDataFromXMLLoader :");
        System.out.println("LibraryId :" + libraryId + " itemId : " + itemId + " itemType :" + itemType
            + " expectedItems :" + expectedItems);
    }

}
