package task;

import context.DataManager;
import entity.Bay;
import entity.Container;
import entity.Truck;
import utility.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static context.DataManager.*;
import static context.ThreadManager.TRUCK_LOADING_DURATION_MS;

/**
 * The class that handles the loading of containers onto trucks.
 */
public class TruckLoading implements Runnable {

    // A flag to indicate the bay to be selected
    private static final AtomicBoolean alternateFlag = new AtomicBoolean(true);

    @Override
    public void run() {
        try {

            // Flag variables to determine if the bays have ended
            boolean bayOneEnd = false;
            boolean bayTwoEnd = false;

            // Continue the thread if both bays have not ended
            while (!(bayOneEnd && bayTwoEnd)) {

                // Variables to store the retrieved bay and truck
                Bay retrievedBay;
                Truck retrievedTruck;

                // Select bay using the alternate flag
                boolean currentFlag = alternateFlag.getAndSet(!alternateFlag.get());
                retrievedBay = currentFlag ?
                        !bayOneEnd ? bayOne : bayTwo :
                        !bayTwoEnd ? bayTwo : bayOne;

                // Sleep for a short duration to simulate loading time
                TimeUnit.MILLISECONDS.sleep(TRUCK_LOADING_DURATION_MS);

                // Based on the retrieved bay, retrieve the truck and check its capacity
                retrievedTruck = retrievedBay.getCurrentTruck();
                if (retrievedTruck == null || retrievedTruck.getRemainingCapacity() == 0) continue;

                // Retrieve a container from the bay
                Container retrievedContainer = retrievedBay.takeContainer();

                // If the container is the empty container, mark the bay as ended
                if (retrievedContainer == EMPTY_CONTAINER) {
                    if (retrievedBay == bayOne) bayOneEnd = true;
                    else bayTwoEnd = true;
                    continue;
                }

                // Add the container to the truck and calculate the timing
                long startLoadTime = System.nanoTime();
                retrievedTruck.addContainer(retrievedContainer);
                long endLoadTime = System.nanoTime();

                // Log the action, increment the counter, and record the loading time
                Logger.generateLog(Logger.Type.LOAD_CONTAINER_TO_TRUCK, retrievedTruck, retrievedContainer);
                DataManager.containersShippedCounter.incrementAndGet();
                DataManager.truckLoadingTimes.put(retrievedTruck, endLoadTime - startLoadTime);
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {

            // Inform the other threads to stop their processes
            TruckGenerator.notifyStop();
            TruckParking.stopParking();
            TruckDispatch.stopRunning();
        }
    }
}