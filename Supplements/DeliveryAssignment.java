import java.time.LocalDateTime;

public class DeliveryAssignment {
    // Vars
    private final String assignmentID;
    private final LocalDateTime assignedAt;
    public LocalDateTime estimatedArrival;
    public LocalDateTime deliveredAt;
    public String currentLocation;

    // Accessors
    public String GetAssignmentID() {
        return assignmentID;
    }

    public LocalDateTime GetAssignedAt() {
        return assignedAt;
    }

    // Methods
    public DeliveryAssignment(String assignmentID, LocalDateTime assignedAt) {
        this.assignmentID = assignmentID;
        this.assignedAt = assignedAt;
    }
}