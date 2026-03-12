package context;

import entity.*;
import entity.Container;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static context.ThreadManager.*;

/**
 * A class that stores all the buffers involved in the system.
 */
public class DataManager {

    // ANSI colours for console output
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";

    // The list for inventory
    public final static Map<Item, Integer> inventoryList = new ConcurrentHashMap<>();

    // The buffers for orders
    public static final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    public static final AtomicInteger createOrderCounter = new AtomicInteger(0);
    public static final Order LAST_ORDER = new Order(null);

    // The buffers for rejected orders
    public static final BlockingQueue<RejectedOrder> firstRejectedOrderList = new LinkedBlockingQueue<>();
    public static final AtomicInteger firstRejectCounter = new AtomicInteger(0);
    public static final BlockingQueue<RejectedOrder> secondRejectedOrderList = new LinkedBlockingQueue<>();
    public static final AtomicInteger secondRejectCounter = new AtomicInteger(0);
    public static final RejectedOrder LAST_REJECTED_ORDER = new RejectedOrder(null, "Terminating order");
    public static final AtomicInteger successfullyCreatedOrderCounter = new AtomicInteger(0);

    // The semaphore for robotic arms
    public static final Semaphore pickingRoboticArmSemaphore = new Semaphore(PICKING_ARM_SIZE);

    // The buffer for order bins
    public static final BlockingQueue<OrderBin> orderBinQueue = new ArrayBlockingQueue<>(ORDER_BIN_SIZE);
    public static final OrderBin LAST_ORDER_BIN = new OrderBin();
    public static final LinkedBlockingQueue<OrderBin> readyToPackBinQueue = new LinkedBlockingQueue<>();

    // The buffer for shipping boxes
    public static final BlockingQueue<ShippingBox> uncheckedShippingBoxQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<ShippingBox> checkedShippingBoxQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<ShippingBox> labelledShippingBoxQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<ShippingBox> checkedLabelShippingBoxQueue = new LinkedBlockingQueue<>();
    public static final ShippingBox LAST_SHIPPING_BOX = new ShippingBox();

    // Different batches
    public static Batch eastBatch = new Batch(Address.Region.EAST_MALAYSIA);
    public static Batch northernBatch = new Batch(Address.Region.NORTHERN_REGION);
    public static Batch centralBatch = new Batch(Address.Region.CENTRAL_REGION);
    public static Batch easternBatch = new Batch(Address.Region.EASTERN_REGION);
    public static Batch southernBatch = new Batch(Address.Region.SOUTHERN_REGION);

    // Buffers for containers
    public static final LinkedBlockingQueue<Container> overallContainerQueue = new LinkedBlockingQueue<>();
    public static final Container EMPTY_CONTAINER = new Container();

    // The queue for containers at loading bay
    public static final Bay bayOne = new Bay("Bay 1", new LinkedBlockingQueue<>(LOADING_BAY_SPACE));
    public static final Bay bayTwo = new Bay("Bay 2", new LinkedBlockingQueue<>(LOADING_BAY_SPACE));

    // The buffer for newly generated trucks
    public static final LinkedBlockingQueue<Truck> truckQueue = new LinkedBlockingQueue<>();
    public static final Truck LAST_TRUCK = new Truck(null);

    // Additional statistics counters as required by assignment
    public static final AtomicInteger boxesPackedCounter = new AtomicInteger(0);
    public static final AtomicInteger containersShippedCounter = new AtomicInteger(0);
    public static final AtomicInteger trucksDispatchedCounter = new AtomicInteger(0);

    // Truck timing statistics for min, max, average calculations
    public static final ConcurrentHashMap<Truck, Long> truckLoadingTimes = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Truck, Long> truckWaitTimes = new ConcurrentHashMap<>();

    // Counters to track AGV breakdown
    public static final AtomicInteger agvBreakdownCount = new AtomicInteger(0);
    public static final AtomicLong totalAgvDowntime = new AtomicLong(0);

    // Statistics for system performance
    public static final AtomicLong systemStartTime = new AtomicLong(System.currentTimeMillis());
    public static final AtomicLong systemEndTime = new AtomicLong(0);

    // Method to generate statistics
    public static void printStatistics() {

        // Print header
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SWIFT CART E-COMMERCE CENTRE - FINAL STATISTICS REPORT");
        System.out.println("=".repeat(60));

        // Overall system statistics
        System.out.println("\n" + "-".repeat(60));
        System.out.println("PROCESSING SUMMARY");
        System.out.println("-".repeat(60));
        System.out.println("- Orders received & processed  : " + createOrderCounter.get());
        System.out.println("- Orders given second chance   : " + firstRejectCounter.get());
        System.out.println("- Boxes sent to packing        : " + boxesPackedCounter.get());
        System.out.println("- Orders completed & shipped   : " + successfullyCreatedOrderCounter.get());
        System.out.println("- Orders ultimately rejected   : " + secondRejectCounter.get());
        System.out.println("- Containers shipped           : " + containersShippedCounter.get());
        System.out.println("- Trucks completed delivery    : " + trucksDispatchedCounter.get());

        // Statistics for truck performance
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TRUCK PERFORMANCE METRICS");
        System.out.println("-".repeat(60));

        // Stats for truck loading
        if (!truckLoadingTimes.isEmpty()) {

            // Directly extract the minimum and maximum loading times to calculate average
            long minLoadingTime = truckLoadingTimes.values().stream().min(Long::compareTo).orElse(0L);
            long maxLoadingTime = truckLoadingTimes.values().stream().max(Long::compareTo).orElse(0L);
            double avgLoadingTime = truckLoadingTimes.values().stream().mapToLong(Long::longValue).average().orElse(0.0);

            System.out.println("- Minimum loading time         : " + minLoadingTime + " ns");
            System.out.println("- Maximum loading time         : " + maxLoadingTime + " ns");
            System.out.println("- Average loading time         : " + String.format("%.2f", avgLoadingTime) + " ns");

        // No loading times recorded
        } else System.out.println("No truck-loading metrics recorded.");

        // Stats for truck waiting times
        if (!truckWaitTimes.isEmpty()) {

            // Extract minimum and maximum again to calculate average
            long minWaitTime = truckWaitTimes.values().stream().min(Long::compareTo).orElse(0L);
            long maxWaitTime = truckWaitTimes.values().stream().max(Long::compareTo).orElse(0L);
            double avgWaitTime = truckWaitTimes.values().stream().mapToLong(Long::longValue).average().orElse(0.0);

            System.out.println("- Minimum wait time            : " + minWaitTime + " ms");
            System.out.println("- Maximum wait time            : " + maxWaitTime + " ms");
            System.out.println("- Average wait time            : " + String.format("%.2f", avgWaitTime) + " ms");

        // No waiting times recorded
        } else System.out.println("No truck-waiting metrics recorded.");

        // AGV performance
        System.out.println("\n" + "-".repeat(60));
        System.out.println("AGV PERFORMANCE");
        System.out.println("-".repeat(60));
        System.out.println("- Total AGV breakdowns         : " + agvBreakdownCount.get());
        System.out.println("- Total downtime               : " + totalAgvDowntime.get() + " ms");

        // Overall system performance
        long totalRuntime = systemEndTime.get() - systemStartTime.get();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("SYSTEM PERFORMANCE");
        System.out.println("-".repeat(60));
        System.out.println("- Total runtime                : " + totalRuntime + " ms (" + String.format("%.2f", totalRuntime / 1000.0) + " seconds)");
        System.out.println("- Orders processed per second  : " + String.format("%.2f", (createOrderCounter.get() * 1000.0) / totalRuntime));

        // Information for different queues and buffers
        System.out.println("\n" + "-".repeat(60));
        System.out.println("SYSTEM CLEARANCE VERIFICATION");
        System.out.println("-".repeat(60));
        System.out.println("- Orders in queue              : " + orderQueue.size());
        System.out.println("- Order bins in queue          : " + orderBinQueue.size());
        System.out.println("- Unchecked shipping boxes     : " + uncheckedShippingBoxQueue.size());
        System.out.println("- Checked shipping boxes       : " + checkedShippingBoxQueue.size());
        System.out.println("- Labelled shipping boxes      : " + labelledShippingBoxQueue.size());
        System.out.println("- Checked label shipping boxes : " + checkedLabelShippingBoxQueue.size());
        System.out.println("- Containers in overall queue  : " + overallContainerQueue.size());
        System.out.println("- Containers in Bay 1          : " + bayOne.getQueueSize());
        System.out.println("- Containers in Bay 2          : " + bayTwo.getQueueSize());
        System.out.println("- Trucks in queue              : " + truckQueue.size());

        // Perform checking to see if everything is cleared
        boolean systemCleared = orderQueue.isEmpty() &&
                uncheckedShippingBoxQueue.isEmpty() && checkedShippingBoxQueue.isEmpty() &&
                labelledShippingBoxQueue.isEmpty() && checkedLabelShippingBoxQueue.isEmpty() &&
                overallContainerQueue.isEmpty() && bayOne.isQueueEmpty() &&
                bayTwo.isQueueEmpty() && truckQueue.isEmpty();

        // Print system clearance status
        System.out.println("\n" + "=".repeat(60));
        System.out.println("System cleared: " + (systemCleared ?
                ANSI_GREEN + "YES" + ANSI_RESET :
                ANSI_RED + "NO" + ANSI_RESET)
        );
        System.out.println("=".repeat(60));
    }

    // Helper method to set system end time
    public static void setSystemEndTime() {
        systemEndTime.set(System.currentTimeMillis());
    }
}