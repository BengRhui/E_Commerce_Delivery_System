package entity;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class representing a bay that can hold a truck and accommodate containers.
 */
public class Bay {

    // Elements in the bay
    private final String name;
    private Truck currentTruck;
    private final LinkedBlockingQueue<Container> containerQueue;

    // Constructor
    public Bay(String name, LinkedBlockingQueue<Container> containerQueue) {
        this.name = name;
        this.currentTruck = null;
        this.containerQueue = containerQueue;
    }

    // Getter to retrieve bay name
    public String getName() {
        return name;
    }

    // Getter to retrieve the current truck
    public synchronized Truck getCurrentTruck() {
        return currentTruck;
    }

    // Getter to check if the bay is available for parking a truck
    public synchronized boolean isAvailableForTruck() {
        return currentTruck == null;
    }

    // Method to park a truck in the bay
    public synchronized void parkTruck(Truck truck) {
        currentTruck = truck;
    }

    // Method to remove a truck from the bay
    public synchronized void truckDeparted() {
        currentTruck = null;
    }

    // Method to place a container in the bay's queue
    public void placeContainer(Container container) throws InterruptedException {
        containerQueue.put(container);
    }

    // Method to take a container from the bay's queue
    public Container takeContainer() throws InterruptedException {
        return containerQueue.take();
    }

    // Method to retrieve the size of the container queue
    public int getQueueSize() {
        return containerQueue.size();
    }

    // Method to check if the container queue is empty
    public boolean isQueueEmpty() {
        return containerQueue.isEmpty();
    }

    // Method to retrieve the remaining capacity of the container queue
    public int getRemainingCapacity() {
        return containerQueue.remainingCapacity();
    }

    // Method to check if the container queue is full
    public boolean isQueueFull() {
        return containerQueue.remainingCapacity() == 0;
    }
}
