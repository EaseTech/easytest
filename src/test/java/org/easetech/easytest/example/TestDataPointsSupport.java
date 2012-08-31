package org.easetech.easytest.example;

import org.junit.experimental.theories.ParametersSuppliedBy;

import java.util.Map;

import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.runner.EasyTestRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;


import org.junit.runner.RunWith;

import org.junit.experimental.theories.DataPoints;

import org.junit.Test;

@RunWith(EasyTestRunner.class)
public class TestDataPointsSupport {
    
    /**
     * Test Data Points support
     */
    @Test
    public void testGetItemsWithoutTestData(LibraryId id) {
        System.out.println("library Id : " + id + " and item type : "
            );

    }
    
    @DataPoints
    public static Object[] data(){
        return new Object[]{
            new LibraryId(1L),
            new LibraryId(2L),
            new LibraryId(3L)
        };
    }
    
    @Test
    public void testGetItemsWithCustomLoader(@ParametersSuppliedBy(GetItemsDataSupplier.class)
    Map<String, Object> inputData) {
        System.out.println("library Id : " + inputData.get("LibraryId"));

    }
    
    /**
     * 
     * A static {@link ParameterSupplier} class for providing data to testGetItemsWithoutTestData method
     *
     */
    public static class GetItemsDataSupplier extends ParameterSupplier {
        
                @Override
                public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
                    List<PotentialAssignment> list = new ArrayList<PotentialAssignment>();
                    HashMap<String, Object> inputData = new HashMap<String, Object>();
                    inputData.put("LibraryId", new LibraryId(1L));
                    inputData.put("itemType", "ebook");
                    inputData.put("searchText", new String[]{"potter" , "poppins" , "superman"});
                    list.add(PotentialAssignment.forValue("", inputData));
                    HashMap<String, Object> inputData1 = new HashMap<String, Object>();
                    inputData1.put("LibraryId", new LibraryId(1L));
                    inputData1.put("itemType", "book");
                    inputData1.put("searchText", new String[]{"spiderman"});
                    list.add(PotentialAssignment.forValue("", inputData1));
                    return list;
                }
                
            }
        
    


}
