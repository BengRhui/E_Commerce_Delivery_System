package task;

import context.DataManager;
import entity.*;
import utility.Logger;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static context.DataManager.LAST_ORDER_BIN;
import static context.DataManager.orderQueue;
import static context.ThreadManager.PICKING_STATION_DURATION_MS;
import static context.ThreadManager.WRONG_DETAIL_PROBABILITY;
import static utility.Logger.Type.PICKING_FROM_SHELF;
import static utility.Logger.Type.TRANSFER_BIN;

/**
 * The class that simulates the picking arms, where items are picked and placed into order bins for packing.
 */
public class PickingArms implements Runnable {

    // A random object to simulate errors in picking items
    private static final Random randomObject = new Random();

    @Override
    public void run() {
        try {
            while (true) {

                // Retrieve an order from the queue
                Order order = orderQueue.take();

                // Terminate the order if it is the last order
                if (order == DataManager.LAST_ORDER) break;

                // If item quantity exceeds inventory, reject the order
                if (!order.isInventorySufficient()) {

                    // Reject the order, add to the list, and start the next iteration
                    RejectedOrder orderRejected = new RejectedOrder(order, "Insufficient inventory.");
                    DataManager.secondRejectedOrderList.put(orderRejected);
                    continue;
                }

                // Variable to hold the bin to be used
                OrderBin binToUse = DataManager.orderBinQueue.take();

                // If the order is valid, first acquire semaphore (simulating picking arms)
                DataManager.pickingRoboticArmSemaphore.acquire();

                try {

                    // Create a map to hold items picked from the shelf
                    Map<Item, Integer> orderItemList = new ConcurrentHashMap<>();

                    // Iterate through the items in the order
                    for (Map.Entry<Item, Integer> orderItem : order.getItemList().entrySet()) {

                        // Retrieve the item and the quantity
                        Item item = orderItem.getKey();
                        int quantity = orderItem.getValue();

                        // Possibility of having the items not picked up
                        if (randomObject.nextDouble() < WRONG_DETAIL_PROBABILITY) continue;

                        // For usual cases, items are picked from the shelf
                        Shelf.removeItems(item, quantity);
                        orderItemList.put(item, quantity);
                        Logger.generateLog(PICKING_FROM_SHELF, item, quantity, order);
                    }

                    // If the item list is not empty
                    if (!orderItemList.isEmpty()) {

                        // Sleep for a while to simulate picking duration
                        TimeUnit.MILLISECONDS.sleep(PICKING_STATION_DURATION_MS);

                        // Add the order and item list to the bin and log the action
                        binToUse.setOrder(order);
                        binToUse.setItemList(orderItemList);
                        DataManager.readyToPackBinQueue.put(binToUse);
                        Logger.generateLog(TRANSFER_BIN, binToUse);

                    // For invalid orders (empty orders)
                    } else {

                        // Return the bin to the queue without processing
                        binToUse.clear();
                        DataManager.orderBinQueue.put(binToUse);

                        // Mark the order as rejected and put it to the final rejected order list
                        RejectedOrder orderRejected = new RejectedOrder(order, "Empty orders. No items picked.");
                        DataManager.secondRejectedOrderList.put(orderRejected);
                    }

                // Error handling
                } catch (IllegalArgumentException _) {

                    // Create the rejected order and put it into the final rejected list
                    RejectedOrder orderRejected = new RejectedOrder(order, "Issues when picking from inventory.");
                    DataManager.secondRejectedOrderList.put(orderRejected);

                // After an order is packed / disposed
                } finally {

                    // The semaphore is released before proceeding to the next iteration
                    DataManager.pickingRoboticArmSemaphore.release();
                }
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is terminated. Please inspect code.");
            Thread.currentThread().interrupt();

        } finally {
            try {

                // Add the last order bin to the next queue
                DataManager.readyToPackBinQueue.put(LAST_ORDER_BIN);

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " is terminated at the last phase. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }
}