package task;

import entity.Address;
import entity.Batch;
import entity.Container;
import entity.ShippingBox;
import utility.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static context.DataManager.*;
import static context.ThreadManager.NUMBER_OF_AGV;
import static context.ThreadManager.NUMBER_OF_BOX_PER_BATCH;
import static utility.Logger.Type.DISPATCH_TO_CONTAINER;

/**
 * This class handles the operation of generating containers once a batch is full.
 */
public class ContainerGeneration implements Runnable {

    // Variable to record the current container
    private final ConcurrentLinkedQueue<Container> containerList = new ConcurrentLinkedQueue<>();

    // Predefined batches that represent different regions (will be removed when it reaches the last shipping box)
    private final List<Batch> batchList = new ArrayList<>(List.of(
            eastBatch, northernBatch, centralBatch, easternBatch, southernBatch
    ));

    @Override
    public void run() {
        try {

            // The loop continues when the batches are still being processed
            while (!batchList.isEmpty()) {

                // Get the iterator for the batch list and begin iteration
                Iterator<Batch> iterator = batchList.iterator();
                while (iterator.hasNext()) {

                    // Retrieve the batch
                    Batch batch = iterator.next();

                    // Make sure that one thread accesses only one batch at a time
                    synchronized (batch.getLock()) {

                        // Check for the last shipping box (termination signal)
                        if (batch.contains(LAST_SHIPPING_BOX)) {

                            // Perform three tasks: Remove the last shipping box, process all boxes in the batch, and remove the batch
                            batch.remove(LAST_SHIPPING_BOX);
                            placeToContainer(batch);
                            iterator.remove();
                            continue;
                        }

                        // If the batch is full, dispatch the batch to container
                        if (batch.size() >= NUMBER_OF_BOX_PER_BATCH) placeToContainer(batch);
                    }
                }
            }

        // Error handling
        } catch (InterruptedException _) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " interrupted. Please inspect code.");
            Thread.currentThread().interrupt();

        // Thread termination
        } finally {

            try {

                // If there are containers that are still in the list, put them into the overall queue
                while (!containerList.isEmpty()) {
                    Container container = containerList.poll();
                    if (container != null) overallContainerQueue.put(container);
                }

                // Inform the overall queue that the container generation is finished (for AGVs to pause)
                for (int i = 0; i < NUMBER_OF_AGV; i++) {
                    overallContainerQueue.put(EMPTY_CONTAINER);
                }

            // Error handling for the final phase
            } catch (InterruptedException _) {

                // Print the error message and interrupt the thread
                System.err.println(Thread.currentThread().getName() + " interrupted in the final phase. Please inspect code.");
                Thread.currentThread().interrupt();
            }
        }
    }

    // A helper method to create containers to store all shipping boxes from a batch
    private void placeToContainer(Batch batch) throws InterruptedException {

        // Retrieve the region associated with the batch
        Address.Region region = batch.getRegion();

        // The container to be filled
        Container currentContainer = null;

        // Loop through the list to find a container with the same region
        for (Container container : containerList) {

            // Continue the loop if the region does not match
            if (container.getRegion() != region) continue;

            // Retrieve the container and end the loop when the region matches
            currentContainer = container;
            break;
        }

        // If the appropriate container cannot be found, create a new container
        if (currentContainer == null) {
            currentContainer = new Container(region);
            containerList.add(currentContainer);
        }

        // Loop through all shipping boxes in the batch
        for (ShippingBox shippingBox : batch.getBoxesCopy()) {

            // Check if the container is full
            if (currentContainer.isFull()) {

                // If yes, remove from the list and add the container to the overall queue
                containerList.remove(currentContainer);
                overallContainerQueue.put(currentContainer);

                // Create a new one and add back to the list
                currentContainer = new Container(batch.getRegion());
                containerList.add(currentContainer);
            }

            // Put the boxes into the container and log the action
            currentContainer.put(shippingBox);
            Logger.generateLog(DISPATCH_TO_CONTAINER, shippingBox, currentContainer);
        }

        // Once done, clear all boxes from the batch
        batch.clear();
    }
}

