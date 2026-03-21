public class Customer {
	// Customer fields
	private final String customerID;
	private final String name;
	private final String phone;
	private final String address;
	private final String email;

	// Constructor
	public Customer(String customerID, String name, String phone, String address, String email) {
		this.customerID = customerID;
		this.name = name;
		this.phone = phone;
		this.address = address;
		this.email = email;
	}

	// Logic methods
	public void placeOrder() {

	}

	public void trackOrder() {

	}

	// Accessors
	public String getCustomerID() {
		return customerID;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}
}
