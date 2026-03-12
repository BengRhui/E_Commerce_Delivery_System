package task;

import context.DataManager;
import entity.Bay;
import entity.Truck;
import utility.Logger;

import static context.DataManager.LAST_TRUCK;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The class that handles the parking of trucks in loading bays.
 */
public class TruckParking implements Runnable {

    // A lock object to synchronize access
    private static final Object lock = new Object();

    // Flags to track the status of parking bays
    private static volatile boolean isFull = false;
    private static final AtomicBoolean shouldTerminate = new AtomicBoolean(false);
    private static boolean alternateFlag = true;

    @Override
    public void run() {

        // Variables to record the truck and starting time
        Truck retrievedTruck = null;
        long startWaitingTime = 0;

        try {

            // Thread should continue run before termination
            while (!shouldTerminate.get()) {

                // Retrieve a truck from the queue and begin timing
                retrievedTruck = DataManager.truckQueue.take();
                startWaitingTime = System.currentTimeMillis();

                // If the truck is the LAST_TRUCK, terminate the loop
                if (retrievedTruck.equals(LAST_TRUCK)) {
                    shouldTerminate.set(true);
                    continue;
                }

                // Initialize a parking variable
                boolean parked = false;

                // If the truck is not parked
                while (!parked && !shouldTerminate.get()) {

                    // Make sure that only one thread can access the logic at a time
                    synchronized (lock) {

                        // Retrieve the bay to be parked and switch the flag
                        Bay selectedBay = selectAvailableBay();
                        alternateFlag = !alternateFlag;

                        // If a bay is available
                        if (selectedBay != null) {

                            // Park the truck at the bay and log the action
                            selectedBay.parkTruck(retrievedTruck);
                            Logger.generateLog(Logger.Type.PARKING_TRUCK, selectedBay, retrievedTruck);

                            // Also record the ending waiting time
                            long endWaitingTime = System.currentTimeMillis();
                            DataManager.truckWaitTimes.put(retrievedTruck, endWaitingTime - startWaitingTime);

                            // If the variable isFull is set to true, temporarily reset it to let the generator continue producing trucks
                            if (isFull) {
                                isFull = false;
                                TruckGenerator.continueGeneration();
                            }

                            // Once done, reset the variables and mark the truck as parked
                            retrievedTruck = null;
                            startWaitingTime = 0;
                            parked = true;

                        // If there are no available bays
                        } else {
                            if (!isFull) {

                                // Update the variable and set it to full, then generate the log and update generator to stop producing trucks
                                isFull = true;
                                Logger.generateLog(Logger.Type.NO_PARKING, retrievedTruck);
                                TruckGenerator.stopGeneration();
                            }
                        }
                    }
                }
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.out.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {

            // If there is a truck waiting for parking
            if (retrievedTruck != null && startWaitingTime != 0) {

                // End the waiting time and record it
                long endWaitingTime = System.currentTimeMillis();
                DataManager.truckWaitTimes.put(retrievedTruck, endWaitingTime - startWaitingTime);
            }

            try {

                // Loop through the truck queue
                while (!DataManager.truckQueue.isEmpty()) {

                    // Retrieve the truck from the queue
                    Truck truck = DataManager.truckQueue.take();

                    // Do nothing if the truck is the LAST_TRUCK (should not trigger but included in case)
                    if (truck.equals(LAST_TRUCK)) continue;

                    // If there are trucks that are loaded with container, increment the dispatched counter and log it as dispatched
                    if (!truck.isEmpty()) {
                        DataManager.trucksDispatchedCounter.incrementAndGet();
                        Logger.generateLog(Logger.Type.TRUCK_DEPARTED, truck);

                    // Empty trucks are logged as disposed
                    } else {
                        Logger.generateLog(Logger.Type.DISPOSE_TRUCK, retrievedTruck);
                    }

                }

                // Inform the threads to complete its operations before ending
                synchronized (lock) {
                    lock.notifyAll();
                }

                // If there is a truck that is not parked, log it as disposed too
                if (retrievedTruck != null) Logger.generateLog(Logger.Type.DISPOSE_TRUCK, retrievedTruck);

            // Error handling
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.out.println(Thread.currentThread().getName() + " is interrupted at the final stage. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }

    // A method to select an available bay for parking
    private static Bay selectAvailableBay() {

        // Initially declare the selected bay as null
        Bay selectedBay = null;

        // Check based on the alternate flag before moving to checking for availability
        if (alternateFlag && DataManager.bayOne.isAvailableForTruck()) selectedBay = DataManager.bayOne;
        else if (!alternateFlag && DataManager.bayTwo.isAvailableForTruck()) selectedBay = DataManager.bayTwo;
        else if (DataManager.bayOne.isAvailableForTruck()) selectedBay = DataManager.bayOne;
        else if (DataManager.bayTwo.isAvailableForTruck()) selectedBay = DataManager.bayTwo;

        // Return the selected bay
        return selectedBay;
    }

    // A method to notify that parking is available
    public static void notifyParkingAvailable() {
        synchronized (lock) {
            if (isFull) {
                isFull = false;
                TruckGenerator.continueGeneration();
            }
            lock.notifyAll();
        }
    }

    // A method to stop the thread
    public static void stopParking() {
         synchronized (lock) {
            shouldTerminate.set(true);
            lock.notifyAll();
         }
    }
}

