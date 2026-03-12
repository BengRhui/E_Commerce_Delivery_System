package entity;

import context.DataManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the order object that enters the system.
 */
public class Order {

    // An atomic integer to track order ID
    private static final AtomicInteger orderIndex = new AtomicInteger(1);

    // Declare an enum to store the payment type
    public enum PaymentType {
        CARD, E_WALLET, ONLINE_BANKING, CASH_ON_DELIVERY
    }

    // Information about order
    private final String orderId;
    private final PaymentType paymentType;
    private final Map<Item, Integer> itemList;
    private final Address address;
    private volatile boolean firstTry = true;

    // Constructor for new orders
    public Order(PaymentType paymentType, Map<Item, Integer> itemList, Address address) {
        this.orderId = generateId();
        this.paymentType = paymentType;
        this.itemList = itemList;
        this.address = address;
    }

    // Constructor for existing orders
    public Order(String orderId, PaymentType paymentType, Map<Item, Integer> itemList, Address address) {
        this.orderId = orderId;
        this.paymentType = paymentType;
        this.itemList = itemList;
        this.address = address;
    }

    // Copy constructor to create a new order from an existing one
    public Order(Order order) {

        // When the parameter is null, an empty order is created
        if (order == null) {
            this.orderId = null;
            this.paymentType = null;
            this.itemList = null;
            this.address = null;

        // Creates a new order with the same attributes
        } else {
            this.orderId = order.getOrderId();
            this.paymentType = order.getPaymentType();
            this.itemList = order.getItemList() != null ? new HashMap<>(order.getItemList()) : null;
            this.address = order.getAddress();
            this.firstTry = order.isFirstTry();
        }
    }

    // Method to generate a new order ID
    private String generateId() {
        int index = orderIndex.getAndIncrement();
        return String.format("ORD%04d", index);
    }

    // Method to mark the order as the second try after first rejection
    public synchronized void markNotFirstTry() {
        this.firstTry = false;
    }

    // Method to check if this is the first try of the order
    public synchronized boolean isFirstTry() {
        return firstTry;
    }

    // Method to check if an order is empty
    public boolean isEmpty() {
        return itemList == null || itemList.isEmpty();
    }

    // Method to check if inventory is enough for an order
    public boolean isInventorySufficient() {

        // If the order is empty, return false to indicate an error
        if (this.isEmpty()) return false;

        // Make sure that the inventory list is accessed in a thread-safe manner
        synchronized (DataManager.inventoryList) {

            // Loop through each item in the order
            for (Map.Entry<Item, Integer> entry : itemList.entrySet()) {
                Item item = entry.getKey();
                int quantity = entry.getValue();
                Integer available = DataManager.inventoryList.get(item);

                // Perform checking. If the item cannot be found or quantity is insufficient, return false
                if (available == null || available < quantity) return false;
            }
        }

        // Return true if all items are available in sufficient quantity
        return true;
    }

    // Method to check if payment is valid
    public boolean isPaymentValid() {
        return paymentType != null;
    }

    // Method to check if the address is valid
    public boolean isAddressValid() {
        return address != null;
    }

    // Getter to retrieve order ID
    public String getOrderId() {
        return orderId;
    }

    // Getter to retrieve the payment type
    public PaymentType getPaymentType() {
        return paymentType;
    }

    // Getter to retrieve the item list
    public Map<Item, Integer> getItemList() {
        return itemList != null ? Collections.unmodifiableMap(itemList) : null;
    }

    // Getter to retrieve the address
    public Address getAddress() {
        return address;
    }
}
