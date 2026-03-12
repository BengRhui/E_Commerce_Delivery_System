package task;

import context.DataManager;
import entity.Batch;
import entity.ShippingBox;
import utility.Logger;

import java.util.concurrent.TimeUnit;

import static context.DataManager.*;
import static context.ThreadManager.SORTING_STATION_DURATION_MS;
import static utility.Logger.Type.*;

/**
 * The class to handle the distribution of shipping boxes into batches based on their destination.
 */
public class BatchDistributing implements Runnable {

    @Override
    public void run() {
        try {
            while (true) {

                // After checking the label, retrieve the shipping box
                ShippingBox retrievedBox = DataManager.checkedLabelShippingBoxQueue.take();

                // The thread shall terminate for the last shipping box
                if (retrievedBox == LAST_SHIPPING_BOX) break;

                // Based on the state, select the batch to be added to
                Batch selectedBatch = switch (retrievedBox.getDestination().getRegion()) {
                    case EAST_MALAYSIA   -> eastBatch;
                    case NORTHERN_REGION -> northernBatch;
                    case CENTRAL_REGION  -> centralBatch;
                    case EASTERN_REGION  -> easternBatch;
                    case SOUTHERN_REGION -> southernBatch;
                };

                // Sleep for a while to simulate the distribution time
                TimeUnit.MILLISECONDS.sleep(SORTING_STATION_DURATION_MS);

                // Add the box to the selected batch, increment the counter, and log the action
                selectedBatch.add(retrievedBox);
                successfullyCreatedOrderCounter.incrementAndGet();
                Logger.generateLog(DISPATCH_TO_BATCH, retrievedBox, selectedBatch);
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // When the thread reaches the final shipping box
        } finally {

            try {

                // Add the last shipping box to all batches (to terminate container generation)
                eastBatch.add(LAST_SHIPPING_BOX);
                northernBatch.add(LAST_SHIPPING_BOX);
                centralBatch.add(LAST_SHIPPING_BOX);
                easternBatch.add(LAST_SHIPPING_BOX);
                southernBatch.add(LAST_SHIPPING_BOX);

            // Error handling for the final addition
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " interrupted in the final phase. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
