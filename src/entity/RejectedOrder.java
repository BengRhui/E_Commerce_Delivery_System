package entity;

/**
 * A class that extends from the order class to represent a rejected order with reason.
 */
public class RejectedOrder extends Order {

    // The reason for rejection
    private final String rejectedReason;

    // Constructor
    public RejectedOrder(Order order, String rejectedReason) {
        super(order);
        this.rejectedReason = rejectedReason;
    }

    // Getter to retrieve the reason for rejection
    public String getRejectedReason() {
        return rejectedReason;
    }
}
