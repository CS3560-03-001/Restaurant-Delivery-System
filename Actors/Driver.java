public class Driver {

    // Vars
    private final String employeeID;
    private final String name;
    private final String licenseNumber;
    private final String vehicleInfo;
    private final String status;

    // Accessors
    public String GetEmployeeID() {
        return employeeID;
    }

    public String GetName() {
        return name;
    }

    public String GetLicenseNumber() {
        return licenseNumber;
    }

    public String GetVehicleInfo() {
        return vehicleInfo;
    }

    public String GetStatus() {
        return status;
    }

    // Methods
    public Driver(String employeeID, String name, String licenseNumber, String vehicleInfo, String status) {
        this.employeeID = employeeID;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.vehicleInfo = vehicleInfo;
        this.status = status;
    }

    public void acceptDelivery() {

    }

    public void markEnRoute() {

    }

    public void  markDelivered() {

    }

    public void returnToRestaurant() {

    }
}