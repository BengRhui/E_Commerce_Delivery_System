package task;

import context.DataManager;
import utility.Initializer;
import entity.Order;
import entity.RejectedOrder;
import utility.Logger;

import java.util.concurrent.TimeUnit;

import static context.DataManager.LAST_REJECTED_ORDER;
import static context.ThreadManager.*;
import static utility.Logger.Type.CREATE_ORDER;

/**
 * This class generates random orders and puts them into the order queue.
 */
public class OrderGeneration implements Runnable {

    @Override
    public void run() {
        try {

            // Loop through the number of orders to generate
            for (int i = 0; i < ORDER_LIST_SIZE; i++) {

                // Generate a random order, increase the counter and log the action
                Order generatedOrder = Initializer.createRandomOrder();
                DataManager.createOrderCounter.incrementAndGet();
                Logger.generateLog(CREATE_ORDER, generatedOrder);

                // Check if the order is valid by looking for a reject reason
                String rejectReason = generateRejectReason(generatedOrder);

                // If there is a reject reason
                if (rejectReason != null) {

                    // Create a rejected order
                    RejectedOrder orderRejected = new RejectedOrder(generatedOrder, rejectReason);

                    // Check if the order is a first try or a retry
                    if (orderRejected.isFirstTry()) DataManager.firstRejectedOrderList.put(orderRejected);
                    else DataManager.secondRejectedOrderList.put(orderRejected);

                // If there is no reject reason
                } else {

                    // Order is created successfully, put it into the order queue
                    DataManager.orderQueue.put(generatedOrder);
                }

                // Order generated at a fixed rate
                TimeUnit.MILLISECONDS.sleep(ORDER_CREATE_RATE_MS);
            }

        // Error handling for the order generation thread
        } catch (InterruptedException _) {

            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {

            try {

                // Add the last order to the order queue (for picking arms to terminate)
                for (int i = 0; i < PICKING_ARM_SIZE; i++) DataManager.orderQueue.put(DataManager.LAST_ORDER);

                // Add the last rejected order to different rejected order lists
                DataManager.firstRejectedOrderList.put(LAST_REJECTED_ORDER);
                DataManager.secondRejectedOrderList.put(LAST_REJECTED_ORDER);

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " interrupted in the final phase. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }

    // Helper method to generate a reject reason for an order
    private static String generateRejectReason(Order order) {

        // If the order is erroneous, return the reject reason
        if (order.isEmpty())              return "Order does not have any item.";
        else if (!order.isPaymentValid()) return "Invalid payment channel.";
        else if (!order.isAddressValid()) return "Invalid address.";

        // If the order is valid, return null
        else return null;
    }
}
