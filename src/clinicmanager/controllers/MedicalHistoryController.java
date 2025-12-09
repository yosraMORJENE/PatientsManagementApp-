package clinicmanager.controllers;

import clinicmanager.dao.MedicalConditionDAO;
import clinicmanager.dao.AllergyDAO;
import clinicmanager.dao.MedicationDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.models.Patient;
import clinicmanager.models.MedicalCondition;
import clinicmanager.models.Allergy;
import clinicmanager.models.Medication;
import java.sql.SQLException;
import java.util.List;

// business logic for medical history management
public class MedicalHistoryController implements IMedicalHistoryController {
    private final PatientDAO patientDAO;
    private final MedicalConditionDAO medicalConditionDAO;
    private final AllergyDAO allergyDAO;
    private final MedicationDAO medicationDAO;

    public MedicalHistoryController(PatientDAO patientDAO, MedicalConditionDAO medicalConditionDAO, 
                                   AllergyDAO allergyDAO, MedicationDAO medicationDAO) {
        this.patientDAO = patientDAO;
        this.medicalConditionDAO = medicalConditionDAO;
        this.allergyDAO = allergyDAO;
        this.medicationDAO = medicationDAO;
    }

    // get all patients for dropdown
    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAllPatients();
    }

    // get patient by id
    public Patient getPatientById(int patientId) throws SQLException {
        return patientDAO.getPatientById(patientId);
    }

    // get patient full name
    public String getPatientName(Patient patient) {
        if (patient == null) return "Unknown";
        return patient.getFirstName() + " " + patient.getLastName();
    }

    // get patient name by id
    public String getPatientNameById(int patientId) throws SQLException {
        Patient patient = getPatientById(patientId);
        return getPatientName(patient);
    }

    // medical conditions operations
    public List<MedicalCondition> getMedicalConditions(int patientId) throws SQLException {
        return medicalConditionDAO.getMedicalConditionsByPatientId(patientId);
    }

    public void addMedicalCondition(MedicalCondition condition) throws SQLException {
        medicalConditionDAO.addMedicalCondition(condition);
    }

    public void updateMedicalCondition(MedicalCondition condition) throws SQLException {
        medicalConditionDAO.updateMedicalCondition(condition);
    }

    public void deleteMedicalCondition(int conditionId) throws SQLException {
        medicalConditionDAO.deleteMedicalCondition(conditionId);
    }

    // allergies operations
    public List<Allergy> getAllergies(int patientId) throws SQLException {
        return allergyDAO.getAllergiesByPatientId(patientId);
    }

    public void addAllergy(Allergy allergy) throws SQLException {
        allergyDAO.addAllergy(allergy);
    }

    public void updateAllergy(Allergy allergy) throws SQLException {
        allergyDAO.updateAllergy(allergy);
    }

    public void deleteAllergy(int allergyId) throws SQLException {
        allergyDAO.deleteAllergy(allergyId);
    }

    // medications operations
    public List<Medication> getMedications(int patientId) throws SQLException {
        return medicationDAO.getMedicationsByPatientId(patientId);
    }

    public void addMedication(Medication medication) throws SQLException {
        medicationDAO.addMedication(medication);
    }

    public void updateMedication(Medication medication) throws SQLException {
        medicationDAO.updateMedication(medication);
    }

    public void deleteMedication(int medicationId) throws SQLException {
        medicationDAO.deleteMedication(medicationId);
    }

    // validate condition name
    public String validateCondition(String conditionName) {
        if (conditionName == null || conditionName.trim().isEmpty()) {
            return "Condition name is required.";
        }
        return null;
    }

    // validate allergy name
    public String validateAllergy(String allergyName) {
        if (allergyName == null || allergyName.trim().isEmpty()) {
            return "Allergy name is required.";
        }
        return null;
    }

    // validate medication name
    public String validateMedication(String medicationName) {
        if (medicationName == null || medicationName.trim().isEmpty()) {
            return "Medication name is required.";
        }
        return null;
    }
}
