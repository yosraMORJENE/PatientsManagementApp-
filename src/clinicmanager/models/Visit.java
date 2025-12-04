package clinicmanager.models;

public class Visit {
    private int id;
    private int appointmentId;
    private String visitDate;
    private String notes;

    // Constructor
    public Visit(int id, int appointmentId, String visitDate, String notes) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.visitDate = visitDate;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}