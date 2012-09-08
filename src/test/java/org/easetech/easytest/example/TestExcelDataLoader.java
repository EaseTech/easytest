package org.easetech.easytest.example;

import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.loader.LoaderType;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(org.easetech.easytest.runner.DataDrivenTest.class)
@DataLoader(filePaths={"testExcelData.xls"} , loaderType=LoaderType.EXCEL)
public class TestExcelDataLoader {

	
	@Test
	public void getExcelTestData(@Param(name="libraryId") Float libraryId , @Param(name="itemId") Float itemId ){
	    System.out.print("Executing getExcelTestData :");
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	@Test
	@DataLoader(filePaths={"overrideExcelData.csv"} , loaderType=LoaderType.CSV)
	public void getExcelTestDataWithDouble(@Param(name="libraryId") Double libraryId , @Param(name="itemId") Double itemId){
	    System.out.print("Executing getExcelTestDataWithDouble :");
//	    if(itemId.equals(11568.0D)){
//	        Assert.fail("ItemId is 11568 but should be 2");
//	    }
		System.out.println("LibraryId Anuj is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	@Test
	public void getExcelTestDataWithString(@Param(name="libraryId") String libraryId , @Param(name="itemId") String itemId){
	    System.out.print("Executing getExcelTestDataWithString :");
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	@Test
	public void getExcelTestDataNumberFormat(){
	    System.out.print("Executing getExcelTestDataNumberFormat :");
		System.out.println("This is a simple test");
	}


}
