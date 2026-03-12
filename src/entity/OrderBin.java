package entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to represent the order bin that holds an order with the item set.
 */
public class OrderBin {

    // An atomic integer to track bin ID
    private static final AtomicInteger idIndex = new AtomicInteger(0);

    // Attributes for the bin
    private final String binId;
    private Order order;
    private Map<Item, Integer> itemList;

    // Constructor
    public OrderBin() {
        this.binId = generateId();
        this.order = null;
        this.itemList = null;
    }

    // Getter to retrieve bin ID
    public String getBinId() {
        return binId;
    }

    // Getter to retrieve order
    public synchronized Order getOrder() {
        return order;
    }

    // Getter to retrieve item list
    public synchronized Map<Item, Integer> getItemList() {
        return itemList != null ? Collections.unmodifiableMap(itemList) : null;
    }

    // Setter to update the order in the bin
    public synchronized void setOrder(Order order) {
        this.order = order;
    }

    // Setter to update the item list in the bin
    public synchronized void setItemList(Map<Item, Integer> itemList) {
        this.itemList = itemList != null ? new HashMap<>(itemList) : null;
    }

    // Method to check if the bin is empty
    public synchronized boolean isEmpty() {
        return order == null || itemList == null;
    }

    // Method to remove all items from the bin
    public synchronized void clear() {
        this.order = null;
        this.itemList = null;
    }

    // Method to generate a unique bin ID
    private String generateId() {
        int index = idIndex.getAndIncrement();
        return String.format("BIN%03d", index);
    }
}
