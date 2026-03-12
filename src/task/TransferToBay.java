package task;

import context.DataManager;
import entity.Bay;
import entity.Container;
import utility.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static context.DataManager.*;
import static context.ThreadManager.*;
import static utility.Logger.Type.*;

/**
 * This class handles the transfer of containers to the loading bays.
 */
public class TransferToBay implements Runnable {

    // Flags to track the status of the bays
    private static final AtomicBoolean bayOneTerminated = new AtomicBoolean(false);
    private static final AtomicBoolean bayTwoTerminated = new AtomicBoolean(false);
    private static final AtomicBoolean alternateFlag = new AtomicBoolean(true);

    // A random object to simulate random events
    private static final Random randomObject = new Random();

    @Override
    public void run() {
        try {
            while (true) {

                // Simulate AGV breakdown
                if (randomObject.nextDouble() < AGV_BREAKDOWN_PROBABILITY) {

                    // Log the breakdown and let the AGV stop for a while
                    Logger.generateLog(AGV_BREAKDOWN);
                    TimeUnit.MILLISECONDS.sleep(AGV_BREAKDOWN_DURATION_MS);

                    // Increase the counter and input the downtime
                    agvBreakdownCount.incrementAndGet();
                    totalAgvDowntime.addAndGet(AGV_BREAKDOWN_DURATION_MS);

                    // Log that the AGV is restored
                    Logger.generateLog(AGV_RESTORED);
                }

                // Extreme condition: both bays are full
                if (bayOne.isQueueFull() && bayTwo.isQueueFull()) {

                    // Pause the packing arms
                    PackingArms.stopRunning();

                    // Wait for a while before checking again
                    TimeUnit.MILLISECONDS.sleep(AGV_WAITING_TIME_MS);
                    continue;

                // Restore status when one of the bays is not full
                } else PackingArms.startRunning();

                // Get bays with their respective capacity
                Bay selectedBay;
                int bayOneCapacity = bayOne.getRemainingCapacity();
                int bayTwoCapacity = bayTwo.getRemainingCapacity();

                // Check if both bays have capacity
                if (bayOneCapacity > 0 && bayTwoCapacity > 0) {

                    // If yes, toggle an alternate flag to select a bay
                    boolean useFirst = alternateFlag.getAndSet(!alternateFlag.get());
                    selectedBay = useFirst ? bayOne : bayTwo;

                // When bay 2 does not have capacity
                } else if (bayOneCapacity > 0) {

                    // Log that bay 2 is full and select bay 1
                    Logger.generateLog(BAY_FULL, bayTwo);
                    selectedBay = bayOne;

                // When bay 1 does not have capacity
                } else if (bayTwoCapacity > 0) {

                    // Log that bay 1 is full and select bay 2
                    Logger.generateLog(BAY_FULL, bayOne);
                    selectedBay = bayTwo;

                // When both bays are full
                } else {
                    Logger.generateLog(BAY_FULL, bayOne);
                    Logger.generateLog(BAY_FULL, bayTwo);
                    continue;
                }

                // Retrieve a container
                Container container = DataManager.overallContainerQueue.take();

                // Check if the container is a sentinel
                if (container == EMPTY_CONTAINER) break;

                // Mark the container as loaded to the bay (returns true), then add it to the bay and log it
                if (container.markLoadedToBay()) {
                    selectedBay.placeContainer(container);
                    Logger.generateLog(LOAD_TO_BAY, selectedBay, container);
                }
            }

        // Error handling
        } catch (InterruptedException e) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {
            try {

                // Send termination sentinel containers to bay 1
                if (bayOneTerminated.compareAndSet(false, true)) {
                    bayOne.placeContainer(EMPTY_CONTAINER);
                }

                // Also send termination sentinel to bay 2
                if (bayTwoTerminated.compareAndSet(false, true)) {
                    bayTwo.placeContainer(EMPTY_CONTAINER);
                }

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " is interrupted at final stage. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
