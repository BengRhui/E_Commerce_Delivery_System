package entity;

import context.DataManager;

/**
 * A class that represents a shelf in the warehouse.
 */
public class Shelf {

    // Method to remove items from the shelf
    public static void removeItems(Item item, int quantity) {

        // Check for invalid inputs
        if (item == null || quantity <= 0) throw new IllegalArgumentException("Invalid item or quantity.");

        // Reduce the number of items in the inventory list
        DataManager.inventoryList.compute(item, (_, currentStock) -> {

            // If the number of stocks is null, the item is not found
            if (currentStock == null) throw new IllegalArgumentException("Item does not exist.");

            // If the number is lower than quantity, stock is not enough
            if (currentStock < quantity) throw new IllegalArgumentException("Insufficient stock.");

            // If all condition is fulfilled, return the new quantity
            return currentStock - quantity;
        });
    }
}
