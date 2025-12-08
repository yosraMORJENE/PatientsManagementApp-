package clinicmanager.models;

public class Appointment {
    private int id;
    private int patientId;
    private String appointmentDate;
    private String reason;
    private String status;  // scheduled, arrived, in-progress, completed, cancelled, no_show
    private String createdAt;
    private String updatedAt;

    // Constructor (backward compatibility)
    public Appointment(int id, int patientId, String appointmentDate, String reason) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
        this.status = "scheduled";  // default status
        this.createdAt = null;
        this.updatedAt = null;
    }
    
    // Constructor with status and audit fields
    public Appointment(int id, int patientId, String appointmentDate, String reason, 
                      String status, String createdAt, String updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
        this.status = status != null ? status : "scheduled";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }



    
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}