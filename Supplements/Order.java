// represents a customer's delivery order and lifecycle

public class Order {

    // variables
    private final String orderID;
    private final String timestamp;
    private final String status;
    private final String deliveryAddress;
    private final Float totalAmount;
    private final String paymentMethod;

    // accessors
    public String GetOrderID() {
        return orderID;
    }

    public String GetTimestamp() {
        return timestamp;
    }

    public String GetStatus() {
        return status;
    }

    public String GetDeliveryAddress() {
        return deliveryAddress;
    }

    public Float GetTotalAmount() {
        return totalAmount;
    }

    public String GetPaymentMethod() {
        return paymentMethod;
    }

    // methods
    public Order(String orderID, String timestamp, String status, String deliveryAddress, Float totalAmount, String paymentMethod) {
        this.orderID = orderID;
        this.timestamp = timestamp;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public void calculateTotal() {

    }

    public void updateStatus() {

    }
}