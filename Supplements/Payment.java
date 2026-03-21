// to handle payment processing for customer orders

public class Payment {

    // variables
    private final String paymentID;
    private final Float amount;
    private final String method;
    private final String status;
    private final String timestamp;

    // accessors
    public String GetPaymentID() {
        return paymentID;
    }

    public Float GetAmount() {
        return amount;
    }

    public String GetMethod() {
        return method;
    }

    public String GetStatus() {
        return status;
    }

    public String GetTimestamp() {
        return timestamp;
    }

    // methods
    public Payment(String paymentID, Float amount, String method, String status, String timestamp) {
        this.paymentID = paymentID;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.timestamp = timestamp;
    }

    public void processPayment() {

    }

    public void confirmPayment() {

    }
}