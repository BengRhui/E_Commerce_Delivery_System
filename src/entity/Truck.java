package entity;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static context.ThreadManager.MAX_CONTAINERS_PER_TRUCK;

/**
 * A class to represent a truck that can hold containers.
 */
public class Truck {

    // Atomic integer to track truck ID
    private static final AtomicInteger truckCounter = new AtomicInteger(1);

    // Attribute for the truck
    private final String truckId;
    private final LinkedBlockingQueue<Container> loadedContainer;

    // Constructor for sentinel truck
    public Truck(Object object) {

        // If the argument is not null, it's an invalid usage
        if (object != null) throw new IllegalArgumentException("Invalid argument: Sentinel truck cannot be initialized");

        // Initialize truck with null values
        this.truckId = null;
        this.loadedContainer = null;
    }

    // Constructor for a new truck
    public Truck() {
        this.truckId = String.format("TRUCK%03d", truckCounter.getAndIncrement());
        this.loadedContainer = new LinkedBlockingQueue<>(MAX_CONTAINERS_PER_TRUCK);
    }

    // Getter to retrieve the truck ID
    public String getTruckId() {
        return truckId;
    }

    // Getter to retrieve remaining capacity of the truck
    public int getRemainingCapacity() {
        return loadedContainer == null ? 0 : loadedContainer.remainingCapacity();
    }

    // Getter to check if the truck has available capacity
    public boolean isEmpty() {
        return loadedContainer == null || loadedContainer.isEmpty();
    }

    // Getter to add containers into the truck
    public void addContainer(Container container) throws InterruptedException {

        // Invalid input
        if (loadedContainer == null) throw new IllegalStateException("Truck is not initialized with container queue.");

        // Add container to queue
        loadedContainer.put(container);
    }
}
