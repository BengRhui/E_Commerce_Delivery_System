package entity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class to represent items on the shelf.
 */
public class Item {

    // Atomic integer to record ID
    private static final AtomicInteger idIndex = new AtomicInteger(0);

    // Attributes for item objects
    private final String itemId;
    private final String itemName;

    // Constructor to generate a new item
    public Item(String itemName) {
        this.itemId = generateNewId();
        this.itemName = itemName;
    }

    // Getter to retrieve the name of the item
    public String getItemName() {
        return itemName;
    }

    // A utility method to generate new item ID
    private String generateNewId() {
        int index = idIndex.getAndIncrement();
        return String.format("ITEM%03d", index);
    }

    // The equals method is override to make sure items can be compared
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return Objects.equals(itemId, item.itemId);
    }

    // Overriding the hashCode method to allow items to be stored in hash maps
    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
}
