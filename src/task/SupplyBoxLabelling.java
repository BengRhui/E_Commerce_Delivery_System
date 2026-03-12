package task;

import context.DataManager;
import entity.ShippingBox;
import utility.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static context.DataManager.LAST_SHIPPING_BOX;
import static context.ThreadManager.LABELLING_STATION_DURATION_MS;
import static context.ThreadManager.WRONG_DETAIL_PROBABILITY;

/**
 * This class provides labels for supply boxes.
 */
public class SupplyBoxLabelling implements Runnable {

    // A random object to simulate errors in labeling boxes
    private static final Random randomObject = new Random();

    @Override
    public void run() {
        try {
            while (true) {

                // Retrieve the shipping box
                ShippingBox retrievedBox = DataManager.checkedShippingBoxQueue.take();

                // If the box is the last shipping box, break the loop
                if (retrievedBox == LAST_SHIPPING_BOX) break;

                // Sleep for a while to simulate the labeling time
                TimeUnit.MILLISECONDS.sleep(LABELLING_STATION_DURATION_MS);

                // Generate a label for the box based on the probability
                if (randomObject.nextDouble() >= WRONG_DETAIL_PROBABILITY) retrievedBox.setLabel();

                // Log the action and add to queue
                Logger.generateLog(Logger.Type.LABEL_SHIPPING_BOX, retrievedBox);
                DataManager.labelledShippingBoxQueue.put(retrievedBox);
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {
            try {

                // Add the last shipping box to the labeled queue
                DataManager.labelledShippingBoxQueue.put(LAST_SHIPPING_BOX);

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " is interrupted at the final stage. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
