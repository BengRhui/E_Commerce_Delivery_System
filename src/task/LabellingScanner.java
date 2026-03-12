package task;

import context.DataManager;
import entity.RejectedOrder;
import entity.ShippingBox;
import utility.Logger;

import static context.DataManager.LAST_SHIPPING_BOX;
import static utility.Logger.Type.*;

/**
 * This class checks the labels of shipping boxes and reject those with invalid ID or destination.
 */
public class LabellingScanner implements Runnable {

    @Override
    public void run() {
        try {
            while (true) {

                // Retrieved the labeled shipping box
                ShippingBox retrievedBox = DataManager.labelledShippingBoxQueue.take();

                // End the loop if the last shipping box is retrieved
                if (retrievedBox == LAST_SHIPPING_BOX) break;

                // Check for invalid ID or destination
                if (retrievedBox.getTrackingId() == null || retrievedBox.getDestination() == null) {

                    // Reject the associated order and put it into the final rejected list
                    RejectedOrder rejectedOrder = new RejectedOrder(retrievedBox.getOrder(), "Invalid tracking ID or destination.");
                    DataManager.secondRejectedOrderList.put(rejectedOrder);

                // If everything is valid
                } else {

                    // Put the box into the checked label queue and log the action
                    DataManager.checkedLabelShippingBoxQueue.put(retrievedBox);
                    Logger.generateLog(LABEL_CHECKED_BOX, retrievedBox);
                }
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {

            // Place the last shipping box into the queue
            try {
                DataManager.checkedLabelShippingBoxQueue.put(LAST_SHIPPING_BOX);

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " interrupted at the final phase. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
