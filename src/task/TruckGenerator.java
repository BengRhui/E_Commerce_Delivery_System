package task;

import context.DataManager;
import entity.Truck;
import utility.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static context.DataManager.LAST_TRUCK;
import static context.ThreadManager.TRUCK_GENERATION_TIME_MS;

/**
 * This class generates trucks and puts them into the truck queue.
 */
public class TruckGenerator implements Runnable {

    // Lock to ensure only one thread can process a truck-related action per time
    private static final Lock lock = new ReentrantLock();
    private static final Condition truckParkingCondition = lock.newCondition();

    // Flags to track the status of bay and system
    private static volatile boolean availableSpace = true;
    private static volatile boolean isRunning = true;

    @Override
    public void run() {
        try {

            // Thread continues to run until the system is stopped
            while (isRunning) {

                // Make sure only one thread can check the available space and create a truck at a time
                lock.lock();
                try {

                    // Request the thread to wait when there is no available space
                    while (!availableSpace && isRunning) truckParkingCondition.await();

                    // If the stop flag is set, break the loop
                    if (!isRunning) break;

                    // Insert trucks into the queue and log it
                    Truck truck = new Truck();
                    DataManager.truckQueue.put(truck);
                    Logger.generateLog(Logger.Type.CREATE_TRUCK, truck);

                } finally {

                    // Release the lock once done
                    lock.unlock();
                }

                // Let the thread sleep for a short duration to simulate truck generation time
                TimeUnit.MILLISECONDS.sleep(TRUCK_GENERATION_TIME_MS);
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Termination status
        } finally {

            try {

                // Add the last truck to the queue to signal termination
                DataManager.truckQueue.put(LAST_TRUCK);

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " is interrupted at the final stage. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }

    // A method to notify that the bay is available for parking
    public static void continueGeneration() {
        lock.lock();
        try {
            availableSpace = true;
            truckParkingCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // A method to notify that the bay is unavailable for parking
    public static void stopGeneration() {
        lock.lock();
        try {
            availableSpace = false;
        } finally {
            lock.unlock();
        }
    }

    // A method to notify the system to stop
    public static void notifyStop() {
        lock.lock();
        try {
            isRunning = false;
            truckParkingCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}