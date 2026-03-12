package task;

import context.DataManager;
import entity.Bay;
import entity.Truck;
import utility.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class handles the dispatch of trucks from the loading bays after they are fully loaded.
 */
public class TruckDispatch implements Runnable {

    // Flag to indicate halting operations
    public static volatile boolean stopFlag = false;

    // Flag to switch between two bays
    private static final AtomicBoolean alternateFlag = new AtomicBoolean(true);

    @Override
    public void run() {
        try {

            // Continue the loop when the system is still running
            while (!stopFlag) {

                // Retrieve the corresponding bays and switch the flag
                boolean currentFlag = alternateFlag.getAndSet(!alternateFlag.get());
                Bay retrievedBay = currentFlag ? DataManager.bayOne : DataManager.bayTwo;

                // Check if the current bay has a truck ready for dispatch
                if (retrievedBay.getCurrentTruck() != null && retrievedBay.getCurrentTruck().getRemainingCapacity() == 0) {

                    // The truck departs, actions logged, counter is increased, and parking notified
                    retrievedBay.truckDeparted();
                    Logger.generateLog(Logger.Type.TRUCK_DEPARTED, retrievedBay.getCurrentTruck());
                    DataManager.trucksDispatchedCounter.incrementAndGet();
                    TruckParking.notifyParkingAvailable();
                }
            }

        // Termination
        } finally {

            // Check for the truck in the first bay
            Truck truckOne = DataManager.bayOne.getCurrentTruck();
            if (truckOne != null) {

                // If the truck has containers, increase the counter
                if (!truckOne.isEmpty()) DataManager.trucksDispatchedCounter.incrementAndGet();

                // Dispatch the truck and log the action
                DataManager.bayOne.truckDeparted();
                Logger.generateLog(Logger.Type.TRUCK_DEPARTED, truckOne);
            }

            // Check for the truck in the second bay
            Truck truckTwo = DataManager.bayTwo.getCurrentTruck();
            if (truckTwo != null) {

                // Increment the counter if the truck has containers
                if (!truckTwo.isEmpty()) DataManager.trucksDispatchedCounter.incrementAndGet();

                // Truck is dispatched and the action is logged
                DataManager.bayTwo.truckDeparted();
                Logger.generateLog(Logger.Type.TRUCK_DEPARTED, truckTwo);
            }
        }
    }

    // A method to inform the thread to halt its operation
    public static void stopRunning() {
        stopFlag = true;
    }
}
