package entity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to represent a shipping box that is packed from an order.
 */
public class ShippingBox {

    // An atomic integer to track tracking ID
    private final static AtomicInteger idIndex = new AtomicInteger(1);

    // Attributes for the shipping box
    private volatile String trackingId;
    private volatile Address destination;
    private final Map<Item, Integer> itemList;
    private final Order order;

    // Constructor for a new shipping box
    public ShippingBox(Map<Item, Integer> itemList, Order order) {
        this.trackingId = null;
        this.destination = null;
        this.itemList = itemList != null ? Map.copyOf(itemList) : null;
        this.order = order;
    }

    // Constructor for an existing shipping box
    public ShippingBox() {
        this.trackingId = null;
        this.destination = null;
        this.itemList = null;
        this.order = null;
    }

    // Generate a unique tracking ID for the boxes
    private String generateId() {
        int index = idIndex.getAndIncrement();
        return String.format("TRACK%04d", index);
    }

    // Getter to get the tracking ID
    public String getTrackingId() {
        return trackingId;
    }

    // Getter to get the destination
    public Address getDestination() {
        return destination;
    }

    // Getter to get the item list
    public Map<Item, Integer> getItemList() {
        return itemList;
    }

    // Getter to get the order associated with the shipping box
    public Order getOrder() {
        return order;
    }

    // A method to set the label for the shipping box
    public synchronized void setLabel() {
        if (order != null && trackingId == null) {
            this.trackingId = generateId();
            this.destination = order.getAddress();
        }
    }
}
