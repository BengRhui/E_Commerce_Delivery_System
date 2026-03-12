package utility;

import entity.*;

/**
 * The logger class that provides different logging methods to document the changes occurring in the system.
 */
public class Logger {

    // Colour codes for console output
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE   = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN   = "\u001B[36m";

    // Enums representing the different types of messages to be generated
    public enum Type {

        // Enum for orders
        CREATE_ORDER, RECREATE_ORDER, REJECT_ORDER, SECOND_TIME_REJECT,

        // Enum for picking items
        PICKING_FROM_SHELF,

        // Enum for order bins
        NO_BINS_AVAILABLE, TRANSFER_BIN,

        // Enum for shipping boxes
        CREATE_SHIPPING_BOX, CHECK_SHIPPING_BOX, LABEL_SHIPPING_BOX, LABEL_CHECKED_BOX, PACKING_STOPPED, PACKING_STARTED,

        // Enum for dispatching to batch
        DISPATCH_TO_BATCH,

        // Enum for dispatching to container
        DISPATCH_TO_CONTAINER,

        // Enum for loading to bay
        LOAD_TO_BAY, BAY_FULL,

        // Enum for trucks
        CREATE_TRUCK, PARKING_TRUCK, NO_PARKING, LOAD_CONTAINER_TO_TRUCK, TRUCK_DEPARTED, DISPOSE_TRUCK,

        // Enum for AGV operations
        AGV_BREAKDOWN, AGV_RESTORED
    }

    // Method to help format and display log messages
    private static synchronized void log(String ansiColour, String message) {
        System.out.printf(
                "[%-35s]%s %s%n" + ANSI_RESET,
                Thread.currentThread().getName(),
                ansiColour,
                message
        );
    }

    // Main function to generate logs
    public static void generateLog(Type type, Object... object) {

        try {

            // First, retrieve the number of parameters
            int numOfParameters = object.length;

            // Proceed based on different cases of parameters
            switch (numOfParameters) {

                // General logs
                case 0 -> {

                    // Check the types
                    switch (type) {
                        case NO_BINS_AVAILABLE -> log(ANSI_YELLOW, "No available bins.");
                        case AGV_BREAKDOWN     -> log(ANSI_RED,    "AGV is broken down.");
                        case AGV_RESTORED      -> log(ANSI_GREEN,  "AGV is restored.");
                        case PACKING_STARTED   -> log(ANSI_BLUE,   "Packing started.");
                        case PACKING_STOPPED   -> log(ANSI_RED,   "Packing stopped.");
                    }
                }

                // Logs with only one parameter
                case 1 -> {

                    // Retrieve the first object
                    Object firstObject = object[0];

                    // For order-related logs
                    if (firstObject instanceof Order order) {
                        switch (type) {
                            case CREATE_ORDER       -> log(ANSI_GREEN, "Order (" + order.getOrderId() + ") is created successfully.");
                            case RECREATE_ORDER     -> log(ANSI_GREEN, "Order (" + order.getOrderId() + ") is recreated successfully.");
                            case REJECT_ORDER       -> log(ANSI_RED,   "Order (" + order.getOrderId() + ") is rejected with reason: " + ((RejectedOrder) order).getRejectedReason() + " Try again.");
                            case SECOND_TIME_REJECT -> log(ANSI_RED,   order.isFirstTry() ?
                                                               "Order (" + order.getOrderId() + ") is rejected with reason: " + ((RejectedOrder) order).getRejectedReason() :
                                                               "Order (" + order.getOrderId() + ") is rejected again with reason: " + ((RejectedOrder) order).getRejectedReason());

                            default                 -> throw new IllegalArgumentException();
                        }
                    }

                    // For order-bin logs
                    if (firstObject instanceof OrderBin bin) {
                        switch (type) {
                            case TRANSFER_BIN -> log(ANSI_CYAN, "Items from order " + bin.getOrder().getOrderId() + " placed into bin " + bin.getBinId() + ".");
                            default -> throw new IllegalArgumentException();
                        }
                    }

                    // For shipping box logs
                    if (firstObject instanceof ShippingBox box) {
                        switch (type) {
                            case CREATE_SHIPPING_BOX -> log(ANSI_PURPLE, "Shipping box of order (" + box.getOrder().getOrderId() + ") is created.");
                            case CHECK_SHIPPING_BOX  -> log(ANSI_PURPLE, "Contents from the shipping box of order (" + box.getOrder().getOrderId() + ") is checked.");
                            case LABEL_SHIPPING_BOX  -> log(ANSI_PURPLE, "Shipping box of order (" + box.getOrder().getOrderId() + ") is labelled.");
                            case LABEL_CHECKED_BOX  -> log(ANSI_PURPLE, "Label of shipping box of order (" + box.getOrder().getOrderId() + ") is valid and checked.");
                            default -> throw new IllegalArgumentException();
                        }
                    }

                    // For truck logs
                    if (firstObject instanceof Truck truck) {
                        switch (type) {
                            case CREATE_TRUCK   -> log(ANSI_GREEN, "Truck (" + truck.getTruckId() + ") is created.");
                            case TRUCK_DEPARTED -> log(ANSI_GREEN, "Truck (" + truck.getTruckId() + ") departed.");
                            case DISPOSE_TRUCK  -> log(ANSI_BLUE,  "Truck (" + truck.getTruckId() + ") departed without parking.");
                            case NO_PARKING     -> log(ANSI_RED,   "No available bay for parking for truck (" + truck.getTruckId() + ").");
                            default -> throw new IllegalArgumentException();
                        }
                    }

                    // For bay logs
                    if (firstObject instanceof Bay bay) {
                        switch (type) {
                            case BAY_FULL -> log(ANSI_RED, "Bay (" + bay.getName() + ") is full.");
                            default -> throw new IllegalArgumentException();
                        }
                    }
                }

                // Logs with two parameters
                case 2 -> {

                    // Retrieve the first and second objects
                    Object firstObject = object[0];
                    Object secondObject = object[1];

                    // For dispatch logs
                    if (firstObject instanceof ShippingBox box) {

                        // Check if dispatch to batch
                        if (secondObject instanceof Batch batch) {
                            switch (type) {
                                case DISPATCH_TO_BATCH -> log(ANSI_BLUE, "Shipping box (" + box.getTrackingId() + ") disposed to batch " + batch.getRegion() + ".");
                                default -> throw new IllegalArgumentException();
                            }

                        // Check if dispatch to container
                        } else if (secondObject instanceof Container container) {
                            switch (type) {
                                case DISPATCH_TO_CONTAINER -> log(ANSI_BLUE, "Batch with order (" + box.getTrackingId() + ") is loaded to Container " + container.getName() + ".");
                                default -> throw new IllegalArgumentException();
                            }
                        }
                    }

                    // For bay-related logs
                    if (firstObject instanceof Bay bay) {

                        // For loading to bay
                        if (secondObject instanceof Container container) {
                            switch (type) {
                                case LOAD_TO_BAY -> log(ANSI_BLUE, "Container " + container.getName() + " is loaded to Bay (" + bay.getName() + ").");
                                default -> throw new IllegalArgumentException();
                            }

                        // For parking truck
                        } else if (secondObject instanceof Truck truck) {
                            switch (type) {
                                case PARKING_TRUCK -> log(ANSI_BLUE, "Truck (" + truck.getTruckId() + ") is parked at Bay (" + bay.getName() + ").");
                                default -> throw new IllegalArgumentException();
                            }
                        }
                    }

                    // For truck-related logs
                    if (firstObject instanceof Truck truck) {

                        // For loading container to truck
                        if (secondObject instanceof Container container) {
                            switch (type) {
                                case LOAD_CONTAINER_TO_TRUCK -> log(ANSI_BLUE, "Container " + container.getName() + " is loaded to Truck (" + truck.getTruckId() + ").");
                                default -> throw new IllegalArgumentException();
                            }
                        }
                    }
                }

                // Logs with three parameters
                case 3 -> {

                    // Retrieve the first, second, and third objects
                    Object firstObject = object[0];
                    Object secondObject = object[1];
                    Object thirdObject = object[2];

                    // For picking items from the shelf
                    if (firstObject instanceof Item item && secondObject instanceof Integer quantity && thirdObject instanceof Order order) {
                        switch (type) {
                            case PICKING_FROM_SHELF -> log(ANSI_YELLOW, "Picking " + item.getItemName() + " with quantity of " + quantity + " from the shelf for order (" + order.getOrderId() + ").");
                            default -> throw new IllegalArgumentException();
                        }
                    }
                }

                // Erroneous logs
                default -> throw new IllegalArgumentException();
            }

        // Handle invalid arguments
        } catch (IllegalArgumentException _) {
            System.err.println("Logger: Invalid parameters for log generation. Please inspect code.");
        }
    }
}
