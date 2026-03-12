package task;

import context.DataManager;
import entity.RejectedOrder;
import utility.Logger;

import static context.DataManager.secondRejectCounter;
import static utility.Logger.Type.SECOND_TIME_REJECT;

public class OrderRejection implements Runnable {

    @Override
    public void run() {
        try {

            // Begin the loop until the last rejected order is processed
            while (true) {

                // Retrieve the rejected order from the queue
                RejectedOrder rejectedOrder = DataManager.secondRejectedOrderList.take();

                // If it reaches the last rejected order, the thread shall terminate
                if (rejectedOrder.equals(DataManager.LAST_REJECTED_ORDER)) break;

                // Record the rejection in the log and increment the counter
                Logger.generateLog(SECOND_TIME_REJECT, rejectedOrder);
                secondRejectCounter.incrementAndGet();
            }

        // Error handling
        } catch (InterruptedException e) {

            // Print the error message and interrupt the thread
            System.err.println(Thread.currentThread().getName() + " is interrupted. Please inspect code.");
            Thread.currentThread().interrupt();
        }
    }
}
