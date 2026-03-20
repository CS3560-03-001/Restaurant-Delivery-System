public class Customer {
    // Vars
    private final String customerID;
    private final String name;
    private final String phone;
    private final String address;
    private final String email;

    // Accessors
    public String GetCustomerID() {
        return customerID;
    }

    public String GetName() {
        return name;
    }

    public String GetPhone() {
        return phone;
    }

    public String GetAddress() {
        return address;
    }

    public String GetEmail() {
        return email;
    }

    // Methods
    public Customer(String customerID, String name, String phone, String address, String email) {
        this.customerID = customerID;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.email = email;
    }

    public void placeOrder() {

    }

    public void trackOrder() {

    }
}