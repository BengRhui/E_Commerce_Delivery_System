package task;

import context.DataManager;
import entity.OrderBin;
import entity.ShippingBox;
import utility.Logger;

import java.util.concurrent.TimeUnit;

import static context.DataManager.LAST_ORDER_BIN;
import static context.DataManager.LAST_SHIPPING_BOX;
import static context.ThreadManager.PACKING_STATION_DURATION_MS;
import static utility.Logger.Type.CREATE_SHIPPING_BOX;

/**
 * This class represents the packing arms that take items from order bins and place into shipping boxes.
 */
public class PackingArms implements Runnable {

    // A lock to control the running state of arms
    private static final Object lock = new Object();

    // A flag to mark the state of the arms
    private volatile static boolean isRunning = true;

    @Override
    public void run() {
        try {
            while (true) {

                // Make sure only one thread checks for isRunning at a time
                synchronized (lock) {

                    // If isRunning is false, the threads have to wait until an external thread invokes the startRunning method
                    while (!isRunning) {
                        Logger.generateLog(Logger.Type.PACKING_STOPPED);
                        lock.wait();
                    }
                }

                // If the thread can run, retrieve the order bin from the queue
                OrderBin binRetrieved = DataManager.readyToPackBinQueue.take();

                // Terminate if the last order bin is retrieved
                if (binRetrieved == LAST_ORDER_BIN) break;

                // Sleep for a while to simulate the packing time
                TimeUnit.MILLISECONDS.sleep(PACKING_STATION_DURATION_MS);

                // Create a new shipping box, add to the queue, generate log and increment counter
                ShippingBox newBox = new ShippingBox(binRetrieved.getItemList(), binRetrieved.getOrder());
                DataManager.uncheckedShippingBoxQueue.put(newBox);
                Logger.generateLog(CREATE_SHIPPING_BOX, newBox);
                DataManager.boxesPackedCounter.incrementAndGet();

                // Clear the bin after packing and reinsert it to the queue
                binRetrieved.clear();
                DataManager.orderBinQueue.put(binRetrieved);
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {

            try {

                // Add the last shipping box to the queue
                DataManager.uncheckedShippingBoxQueue.put(LAST_SHIPPING_BOX);

            // Error rejection at the last phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " is interrupted at the final stage. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }

    // Helper method to stop the packing arms from running
    public static void stopRunning() {

        // Make sure only one thread calls either one of the critical sections at a time
        synchronized (lock) {
            isRunning = false;
            lock.notifyAll();
        }
    }

    // Helper method to start the packing arms
    public static void startRunning() {

        // Make sure only one thread calls either one of the critical sections at a time
        synchronized (lock) {

            // Record the previous status
            boolean previousStatus = isRunning;

            // Change the isRunning status to true and notify all waiting threads
            isRunning = true;
            lock.notifyAll();

            // If the previous status was false (thread is stopped), generate log to indicate thread started
            if (!previousStatus) Logger.generateLog(Logger.Type.PACKING_STARTED);
        }
    }
}
