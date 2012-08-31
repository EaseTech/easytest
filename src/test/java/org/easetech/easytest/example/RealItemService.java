package org.easetech.easytest.example;

import java.util.Collections;
import java.util.List;

public class RealItemService implements ItemService {

    @Override
    public List<Item> getItems(LibraryId libraryId, String searchText, String itemType) {
        System.out.println("getItems Called");
        return Collections.EMPTY_LIST;
    }

    @Override
    public Item findItem(LibraryId libraryId, ItemId itemId) {
    	System.out.println("findItems Called");
    	Item item = new Item();
    	item.setDescription("Item Description");
    	item.setItemId(itemId.toString());
    	item.setItemType("BOOK");
        return item;
    }

    

}
