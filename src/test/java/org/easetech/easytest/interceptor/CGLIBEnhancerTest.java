
package org.easetech.easytest.interceptor;

import org.easetech.easytest.annotation.Intercept;
import org.easetech.easytest.example.ItemId;
import org.easetech.easytest.example.LibraryId;
import org.easetech.easytest.example.RealItemService;
import org.easetech.easytest.runner.DataDrivenTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataDrivenTestRunner.class)
public class CGLIBEnhancerTest {

    @Intercept() public static RealItemService itemService = new RealItemService();

    @Test
    public void testGetItemEnh() {
        System.out.println("testGetItemEnh called");
         itemService.findItem(new LibraryId(1L), new ItemId(2L));

    }
}
