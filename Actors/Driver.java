public class Driver extends Employee {

	// Driver-exclusive fields
	private final String licenseNumber;
	private final String vehicleInfo;
	private final String status;

	// Methods
	public Driver(String employeeID, String name, String licenseNumber, String vehicleInfo, String status) {
		super(employeeID, name);
		this.licenseNumber = licenseNumber;
		this.vehicleInfo = vehicleInfo;
		this.status = status;
	}

	// Logic Methods
	public void acceptDelivery() {

	}

	public void markEnRoute() {

	}

	public void markDelivered() {

	}

	public void returnToRestaurant() {

	}

	// Accessors
	public String getLicenseNumber() {
		return licenseNumber;
	}

	public String getVehicleInfo() {
		return vehicleInfo;
	}

	public String getStatus() {
		return status;
	}
}
