public abstract class Employee {
	// Fields
	private final String employeeID;
	private final String name;

	// Constructor
	public Employee(String employeeID, String name) {
		this.employeeID = employeeID;
		this.name = name;
	}

	// Accessors
	public String getEmployeeID() {
		return employeeID;
	}

	public String getName() {
		return name;
	}
}
