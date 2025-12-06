package clinicmanager.models;

public class Appointment {
    private int id;
    private int patientId;
    private String appointmentDate;
    private String reason;

    // Constructor
    public Appointment(int id, int patientId, String appointmentDate, String reason) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
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
}