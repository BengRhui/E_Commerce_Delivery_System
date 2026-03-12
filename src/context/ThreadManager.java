package context;

import task.*;

import java.util.Random;
import java.util.concurrent.*;

public class ThreadManager {

    // Random object to populate values
    private static final Random random = new Random();

    // Variables that can be controlled to control order size and parameters
    public static int ORDER_LIST_SIZE = 600;
    public final static double WRONG_DETAIL_PROBABILITY = 0.05;
    public final static int PICKING_ARM_SIZE = 4;
    public final static int ORDER_BIN_SIZE = 4;
    public final static int NUMBER_OF_BOX_PER_BATCH = 6;
    public final static int NUMBER_OF_BATCH_PER_CONTAINER = 5;
    public final static int LOADING_BAY_SPACE = 5;
    public final static int NUMBER_OF_AGV = 3;
    public final static double AGV_BREAKDOWN_PROBABILITY = 0.1;
    public final static int MAX_CONTAINERS_PER_TRUCK = 18;

    public final static int ORDER_CREATE_RATE_MS = 500;
    public final static int PICKING_STATION_DURATION_MS = 200;
    public final static int PACKING_STATION_DURATION_MS = 250;
    public final static int LABELLING_STATION_DURATION_MS = 150;
    public final static int SORTING_STATION_DURATION_MS = 100;
    public final static int AGV_BREAKDOWN_DURATION_MS = random.nextInt(1000, 3000);
    public final static int AGV_WAITING_TIME_MS = 400;
    public final static int TRUCK_GENERATION_TIME_MS = 500;
    public final static int TRUCK_LOADING_DURATION_MS = 500;

    // Thread to populate an order
    private static final Thread populateOrderThread = new Thread(new OrderGeneration(), "Order-Generation-Thread");

    // Thread to reject an order
    private static final Thread firstRejectOrderThread = new Thread(new RetryOrdering(), "Order-Reject-First-Time-Thread");
    private static final Thread secondRejectOrderThread = new Thread(new OrderRejection(), "Order-Final-Rejected-Thread");

    // Thread for packing arms
    private static final Thread packingArmThread = new Thread(new PackingArms(), "Packing-Arm-Thread");

    // Thread for packing scanner
    private static final Thread packingScannerThread = new Thread(new PackingScanner(), "Packing-Scanner-Thread");

    // Thread for labeling supply box
    private static final Thread supplyBoxLabellingThread = new Thread(new SupplyBoxLabelling(), "Labelling-Thread");

    // Thread for checking label
    private static final Thread boxLabelCheckThread = new Thread(new LabellingScanner(), "Label-Check-Thread");

    // Thread for collecting boxes into batches
    private static final Thread collectBoxesToBatches = new Thread(new BatchDistributing(), "Batch-Distribute-Thread");

    // Thread for generating containers
    private static final Thread containerGenerationThread = new Thread(new ContainerGeneration(), "Container-Generation-Thread");

    // Thread for truck generation
    private static final Thread truckGenerationThread = new Thread(new TruckGenerator(), "Truck-Generation-Thread");

    // Thread for truck parking
    private static final Thread truckParkingThread = new Thread(new TruckParking(), "Truck-Parking-Thread");

    // Thread for truck loading and dispatch
    private static final Thread truckLoadingThread = new Thread(new TruckLoading(), "Truck-Loading-Thread");
    private static final Thread truckDispatchThread = new Thread(new TruckDispatch(), "Truck-Dispatch-Thread");

    // Executor services
    private static ExecutorService pickingArms;
    private static ExecutorService agvArms;

    // A method to set the order size to be populated
    public static void setOrderSize(int size) {
        ORDER_LIST_SIZE = size;
    }

    // Main method to initialize all threads
    public static void startAll() {

        // Initialize the executor service for picking arms
        pickingArms = Executors.newFixedThreadPool(PICKING_ARM_SIZE, new ThreadFactory() {

            // Counter to record picking arm index
            private int counter = 1;

            // Customize the thread generated when tasks are submitted
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Picking-Arm-" + counter++);
                t.setDaemon(false);
                return t;
            }
        });

        // Initialize the executor service for AGV arms
        agvArms = Executors.newFixedThreadPool(NUMBER_OF_AGV, new ThreadFactory() {

            // Counter to record AGV index
            private int counter = 1;

            // Customize thread generated when tasks are submitted
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "AGV-" + counter++);
                t.setDaemon(false);
                return t;
            }
        });

        // Submit picking arms and AGV based on their sizes
        for (int i = 0; i < PICKING_ARM_SIZE; i++) pickingArms.submit(new PickingArms());
        for (int i = 0; i < NUMBER_OF_AGV; i++)    agvArms.submit(new TransferToBay());

        // Start all threads in the system
        populateOrderThread.start();
        firstRejectOrderThread.start();
        secondRejectOrderThread.start();
        packingArmThread.start();
        packingScannerThread.start();
        supplyBoxLabellingThread.start();
        boxLabelCheckThread.start();
        collectBoxesToBatches.start();
        containerGenerationThread.start();
        truckGenerationThread.start();
        truckParkingThread.start();
        truckLoadingThread.start();
        truckDispatchThread.start();
    }

    // Method to join all threads (to make sure threads finish before moving to statistics)
    public static void joinAll() throws InterruptedException {

        // Join all threads
        populateOrderThread.join();
        firstRejectOrderThread.join();
        secondRejectOrderThread.join();
        packingArmThread.join();
        packingScannerThread.join();
        supplyBoxLabellingThread.join();
        boxLabelCheckThread.join();
        collectBoxesToBatches.join();
        containerGenerationThread.join();
        truckGenerationThread.join();
        truckParkingThread.join();
        truckLoadingThread.join();
        truckDispatchThread.join();

        // Divider to show that threads are completed
        System.out.println("\n" + "~".repeat(120));

        // Shutdown executor services gracefully
        shutdownExecutorService(pickingArms, "Picking Arms");
        shutdownExecutorService(agvArms, "AGV Loaders");

        // Set system end time for statistics
        DataManager.setSystemEndTime();

        // Print the final termination message
        System.out.println("All threads have been terminated successfully!");
        System.out.println("~".repeat(120));
    }

    // Helper method to shut down executor services
    private static void shutdownExecutorService(ExecutorService executorService, String serviceName) {

        // Check if the executor service is null and do nothing if null
        if (executorService == null) return;

        // Shut down executor service
        executorService.shutdown();

        try {

            // Force the executor service to shut down if it cannot terminate within the specified time
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown of " + serviceName + "...");
                executorService.shutdownNow();

                // If the executor service still cannot terminate, print an error message
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println(serviceName + " did not terminate cleanly");
                }
            }

            // Print the shutdown message
            System.out.println(serviceName + " executor service shutdown completed.");

        } catch (InterruptedException _) {

            // If any errors take place, force shutdown immediately on the executor service and thread
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
