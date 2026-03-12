package task;

import context.DataManager;
import utility.Initializer;
import entity.Order;
import utility.Logger;

import java.util.Random;

import static context.DataManager.LAST_REJECTED_ORDER;
import static context.DataManager.firstRejectCounter;
import static context.ThreadManager.WRONG_DETAIL_PROBABILITY;
import static utility.Logger.Type.*;

/**
 * This class handles the retrying of rejected orders from the order generation phase.
 */
public class RetryOrdering implements Runnable {

    @Override
    public void run() {

        // A random object to simulate errors in retrying orders
        Random randomObject = new Random();

        try {
            while (true) {

                // Retrieve the rejected order (with a second chance)
                Order rejectedOrder = DataManager.firstRejectedOrderList.take();

                // If the order is the last rejected order, break the loop
                if (rejectedOrder.equals(LAST_REJECTED_ORDER)) break;

                // Log the rejected order
                Logger.generateLog(REJECT_ORDER, rejectedOrder);

                // Attempt to retry the order
                if (randomObject.nextDouble() >= WRONG_DETAIL_PROBABILITY) {

                    // Generate a new random order with the same ID
                    String orderId = rejectedOrder.getOrderId();
                    rejectedOrder = Initializer.createRandomOrder(orderId);
                }

                // Mark the order as not the first try
                rejectedOrder.markNotFirstTry();

                // Try to requeue the order into the order queue, log the action and increase the counter
                DataManager.orderQueue.put(rejectedOrder);
                Logger.generateLog(RECREATE_ORDER, rejectedOrder);
                firstRejectCounter.incrementAndGet();
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();
        }
    }
}
