import context.DataManager;
import utility.Initializer;
import context.ThreadManager;

import java.util.Scanner;

/**
 * This is the main class that begins the execution of the warehouse system.
 */
public class Main {

    // Main method to run the program
    public static void main(String[] args) throws InterruptedException {

        // Welcome message
        System.out.println("\nWelcome to SwiftCart Warehouse Management System.");
        System.out.println("\nBefore we start, let's select how you would like to populate the system.\n");

        // Scanner to allow user input for stress level
        Scanner scanner = new Scanner(System.in);

        // Loop until a valid stress level is selected
        while (true) {

            // Display options for stress levels
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("                               Stress Level                               ");
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("1. Low stress level: 100 orders");
            System.out.println("2. Normal stress level: 600 orders");
            System.out.println("3. High stress level: 1000 orders\n");
            System.out.print("Please select the level: ");

            try {

                // Retrieve user input
                String levelSelection = scanner.nextLine();

                // Check for empty
                if (levelSelection.isEmpty()) throw new NumberFormatException();

                // Try to convert input to an integer
                int level = Integer.parseInt(levelSelection);

                // Check the input. If not valid, throw an exception
                if (level < 1 || level > 3) throw new NumberFormatException();

                // Set the order size based on the choice
                if (level == 1)         ThreadManager.setOrderSize(100);
                else if (level == 2)    ThreadManager.setOrderSize(600);
                else                    ThreadManager.setOrderSize(1000);

                // Selection ends. Simulation can begin
                System.out.println("\n" + "-".repeat(120));
                System.out.println(" Now the simulation starts:");
                System.out.println("-".repeat(120));
                break;

            // Error handling for invalid input
            } catch (NumberFormatException _) {

                // Request user to select again
                System.out.println("Invalid input. Please select again.");
            }
        }

        // Stress level is selected, scanner can be closed
        scanner.close();

        // First step: initialize the system with inventory
        Initializer.initializeAll();

        // Second step: start all threads and let them run
        ThreadManager.startAll();

        // Third step: the main thread shall wait for all threads to finish
        ThreadManager.joinAll();

        // Fourth step: print the statistics of the system
        DataManager.printStatistics();
    }
}