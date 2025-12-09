package clinicmanager.models;

public class Medication {
    private int id;
    private int patientId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String startDate;
    private String endDate;
    private String status;
    private String notes;

    // empty constructor
    public Medication() {
    }

    // regular constructor
    public Medication(int id, int patientId, String medicationName, String dosage, String frequency, 
                     String startDate, String endDate, String status, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.notes = notes;
    }

    // getters setters
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

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
