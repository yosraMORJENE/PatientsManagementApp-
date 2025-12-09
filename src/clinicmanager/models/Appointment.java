package clinicmanager.models;

public class Appointment {
    private int id;
    private int patientId;
    private String appointmentDate;
    private String reason;
    private String status;  // like scheduled, arrived, completed etc
    private String createdAt;
    private String updatedAt;

    // basic constructor
    public Appointment(int id, int patientId, String appointmentDate, String reason) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
        this.status = "scheduled";  // default
        this.createdAt = null;
        this.updatedAt = null;
    }
    
    // constructor with all fields
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

    // getters/setters
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