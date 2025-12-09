package clinicmanager.models;

public class MedicalCondition {
    private int id;
    private int patientId;
    private String conditionName;
    private String diagnosisDate;
    private String status;
    private String resolvedDate;  // Date when condition was resolved
    private String notes;

    // No-argument constructor
    public MedicalCondition() {
    }

    // Constructor (backward compatibility - no resolvedDate)
    public MedicalCondition(int id, int patientId, String conditionName, String diagnosisDate, String status, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.conditionName = conditionName;
        this.diagnosisDate = diagnosisDate;
        this.status = status;
        this.resolvedDate = null;
        this.notes = notes;
    }
    
    // New constructor with resolvedDate
    public MedicalCondition(int id, int patientId, String conditionName, String diagnosisDate, 
                           String status, String resolvedDate, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.conditionName = conditionName;
        this.diagnosisDate = diagnosisDate;
        this.status = status;
        this.resolvedDate = resolvedDate;
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

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(String diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(String resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
