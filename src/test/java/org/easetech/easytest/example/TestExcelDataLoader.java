package org.easetech.easytest.example;

import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.loader.LoaderType;
import org.easetech.easytest.runner.EasyTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(EasyTestRunner.class)
@DataLoader(filePaths={"sample.xls"} , loaderType=LoaderType.EXCEL)
public class TestExcelDataLoader {

	
	@Test
	public void getTestData(@Param(name="libraryId") Float libraryId , @Param(name="itemId") Float itemId){
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	@Test
	public void getTestDataWithDouble(@Param(name="libraryId") Double libraryId , @Param(name="itemId") Double itemId){
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	@Test
	public void getTestDataWithString(@Param(name="libraryId") String libraryId , @Param(name="itemId") String itemId){
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}
	
	//This will not work unless explicit converters are written to convert from Double to Integer as Excel loader appends all numbers with .0
	public void getTestDataNumberFormat(@Param(name="libraryId") Integer libraryId , @Param(name="itemId") Integer itemId){
		System.out.println("LibraryId is :" + libraryId + " and Item Id is :" + itemId);
	}

}
