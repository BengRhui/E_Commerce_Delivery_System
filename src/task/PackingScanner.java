package task;

import context.DataManager;
import entity.Item;
import entity.RejectedOrder;
import entity.ShippingBox;
import utility.Logger;

import java.util.Map;

import static context.DataManager.LAST_SHIPPING_BOX;
import static utility.Logger.Type.*;

public class PackingScanner implements Runnable {

    @Override
    public void run() {
        try {

            // Begin looping until the last shipping box is processed
            while (true) {

                // Retrieve the shipping box
                ShippingBox retrievedBox = DataManager.uncheckedShippingBoxQueue.take();

                // Thread begins to terminate after the last shipping box is retrieved
                if (retrievedBox == LAST_SHIPPING_BOX) break;

                // Perform checking: Retrieve the order content and box content
                Map<Item, Integer> orderContent = retrievedBox.getOrder().getItemList();
                Map<Item, Integer> boxContent = retrievedBox.getItemList();

                // Compare the items
                if (orderContent != null && boxContent != null &&
                    orderContent.size() == boxContent.size() &&
                    orderContent.entrySet().stream().allMatch(entry -> entry.getValue().equals(boxContent.get(entry.getKey())))) {

                    // Order matches. Add to queue and log the action
                    DataManager.checkedShippingBoxQueue.put(retrievedBox);
                    Logger.generateLog(CHECK_SHIPPING_BOX, retrievedBox);

                // If items do not match
                } else {

                    // Orders are declared as rejected and put into the final rejected list
                    RejectedOrder rejectedOrder = new RejectedOrder(retrievedBox.getOrder(), Thread.currentThread().getName() + " detects different match.");
                    DataManager.secondRejectedOrderList.put(rejectedOrder);
                }
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {

            try {

                // Add the last shipping box to the checked queue
                DataManager.checkedShippingBoxQueue.put(LAST_SHIPPING_BOX);

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " is interrupted at the final stage. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
