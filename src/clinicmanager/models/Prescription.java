package clinicmanager.models;

public class Prescription {
    private int id;
    private int patientId;
    private Integer visitId;  // Optional reference to the visit where prescription was written
    private String medicationName;
    private String dosage;
    private int quantity;
    private String frequency;
    private int durationDays;
    private String prescribedDate;
    private String refillDate;
    private String notes;

    // Constructor (backward compatibility - no visitId)
    public Prescription(int id, int patientId, String medicationName, String dosage, int quantity, 
                       String frequency, int durationDays, String prescribedDate, String refillDate, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.visitId = null;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.quantity = quantity;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.prescribedDate = prescribedDate;
        this.refillDate = refillDate;
        this.notes = notes;
    }
    
    // New constructor with visitId
    public Prescription(int id, int patientId, Integer visitId, String medicationName, String dosage, int quantity, 
                       String frequency, int durationDays, String prescribedDate, String refillDate, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.visitId = visitId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.quantity = quantity;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.prescribedDate = prescribedDate;
        this.refillDate = refillDate;
        this.notes = notes;
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
    
    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getPrescribedDate() {
        return prescribedDate;
    }

    public void setPrescribedDate(String prescribedDate) {
        this.prescribedDate = prescribedDate;
    }

    public String getRefillDate() {
        return refillDate;
    }

    public void setRefillDate(String refillDate) {
        this.refillDate = refillDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
