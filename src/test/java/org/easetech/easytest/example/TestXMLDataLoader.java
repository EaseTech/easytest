package org.easetech.easytest.example;

import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.loader.LoaderType;
import org.easetech.easytest.runner.EasyTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyTestRunner.class)
@DataLoader(filePaths={"input-data.xml"} , loaderType=LoaderType.XML)
public class TestXMLDataLoader {

	@Test
	public void getItemsData(@Param(name="libraryId")String libraryId , @Param(name="itemId")String itemId , @Param(name="itemType")String itemType , @Param(name="expectedItems")String expectedItems){
		System.out.println("LibraryId :" + libraryId + " itemId : " + itemId + " itemType :" + itemType + " expectedItems :" + expectedItems);
	}
}
