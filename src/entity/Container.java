package entity;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static context.ThreadManager.NUMBER_OF_BATCH_PER_CONTAINER;
import static context.ThreadManager.NUMBER_OF_BOX_PER_BATCH;

/**
 * A class representing a container that holds a list of shipping boxes.
 */
public class Container {

    // A counter to record ID index
    private static final AtomicInteger indexNum = new AtomicInteger(1);

    // Attributes for a container
    private final ArrayBlockingQueue<ShippingBox> shippingBoxList;
    private final String name;
    private final Address.Region region;
    private final AtomicBoolean loadedToBay = new AtomicBoolean(false);

    // Default constructor to create an empty or sentinel container
    public Container() {
        this.shippingBoxList = null;
        this.name = null;
        this.region = null;
    }

    // Main constructor to initialize all fields
    public Container(Address.Region region) {
        this.name = generateName();
        this.shippingBoxList = new ArrayBlockingQueue<>(NUMBER_OF_BATCH_PER_CONTAINER * NUMBER_OF_BOX_PER_BATCH);
        this.region = region;
    }

    // Getter to retrieve the name of the container
    public String getName() {
        return name;
    }

    // Getter to retrieve the region of the container
    public Address.Region getRegion() {
        return region;
    }

    // Method to check if the list is empty
    public boolean isEmpty() {
        return shippingBoxList == null || shippingBoxList.isEmpty();
    }

    // Method to check if the list is full
    public boolean isFull() {
        return shippingBoxList != null && shippingBoxList.remainingCapacity() == 0;
    }

    // Method to add a shipping box to the container
    public void put(ShippingBox box) throws InterruptedException {
        if (shippingBoxList == null) {
            throw new IllegalStateException("Container is not initialized.");
        }
        shippingBoxList.put(box);
    }

    // Method to indicate that the container is already loaded to the bay
    public boolean markLoadedToBay() {
        return loadedToBay.compareAndSet(false, true);
    }

    // Method to generate the name of the container
    private String generateName() {
        return String.format("C%03d", indexNum.getAndIncrement());
    }

    // Method to print the details of the container
    @Override
    public String toString() {
        return String.format("%s (%s)", name, region);
    }
}
