package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static context.ThreadManager.NUMBER_OF_BOX_PER_BATCH;

/**
 * A class representing a batch of a fixed number of shipping boxes to be dispatched into a container.
 */
public class Batch {

    // Attributes for batch
    private final ArrayBlockingQueue<ShippingBox> queue;
    private final Address.Region region;

    // The lock to control concurrency (avoid simultaneous access to the queue)
    private final Object lock = new Object();

    // Constructor
    public Batch(Address.Region region) {
        this.region = region;
        this.queue = new ArrayBlockingQueue<>(NUMBER_OF_BOX_PER_BATCH);
    }

    // Getter to retrieve the region of the batch
    public Address.Region getRegion() {
        return region;
    }

    // Getter to retrieve the lock for the batch
    public Object getLock() {
        return lock;
    }

    // Getter to retrieve a copy of the batch queue (to avoid external modification)
    public List<ShippingBox> getBoxesCopy() {
        synchronized (lock) {
            return new ArrayList<>(queue);
        }
    }

    // A method to check if the queue contains a specific box
    public boolean contains(ShippingBox box) {
        synchronized (lock) {
            return queue.contains(box);
        }
    }

    // A method to remove a specific box from the queue
    public boolean remove(ShippingBox box) {
        synchronized (lock) {
            return queue.remove(box);
        }
    }

    // A method to determine the size of the batch queue
    public int size() {
        synchronized (lock) {
            return queue.size();
        }
    }

    // A method to remove all elements from the batch queue
    public void clear() {
        synchronized (lock) {
            queue.clear();
        }
    }

    // A method to add a shipping box to the batch queue
    public void add(ShippingBox box) throws InterruptedException {
        synchronized (lock) {
            queue.put(box);
        }
    }

    // A method to check if the batch queue is empty
    public boolean isEmpty() {
        synchronized (lock) {
            return queue.isEmpty();
        }
    }
}
