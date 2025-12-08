package clinicmanager.models;

public class Visit {
    private int id;
    private int patientId;  // Direct patient reference (required)
    private Integer appointmentId;  // Optional appointment reference (can be null for walk-ins)
    private String visitDate;
    private String notes;
    private String clinicalNotes;
    private String diagnosis;
    private String treatment;
    private String followUpNotes;
    private String status;  // in-progress, completed, cancelled

    // Constructor for backward compatibility (treats appointmentId as legacy)
    public Visit(int id, int appointmentId, String visitDate, String notes) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = 0;  // Will be populated from database
        this.visitDate = visitDate;
        this.notes = notes;
        this.clinicalNotes = "";
        this.diagnosis = "";
        this.treatment = "";
        this.followUpNotes = "";
        this.status = "completed";
    }

    // Constructor with clinical details (backward compatibility)
    public Visit(int id, int appointmentId, String visitDate, String notes, String clinicalNotes, 
                 String diagnosis, String treatment, String followUpNotes) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = 0;  // Will be populated from database
        this.visitDate = visitDate;
        this.notes = notes;
        this.clinicalNotes = clinicalNotes;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.followUpNotes = followUpNotes;
        this.status = "completed";
    }
    
    // New constructor with patient_id (preferred for new code)
    public Visit(int id, int patientId, Integer appointmentId, String visitDate, String notes, 
                 String clinicalNotes, String diagnosis, String treatment, String followUpNotes, String status) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.visitDate = visitDate;
        this.notes = notes;
        this.clinicalNotes = clinicalNotes;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.followUpNotes = followUpNotes;
        this.status = status != null ? status : "completed";
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

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
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

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getFollowUpNotes() {
        return followUpNotes;
    }

    public void setFollowUpNotes(String followUpNotes) {
        this.followUpNotes = followUpNotes;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}