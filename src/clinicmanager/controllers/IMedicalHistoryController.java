package clinicmanager.controllers;

import clinicmanager.models.Patient;
import clinicmanager.models.MedicalCondition;
import clinicmanager.models.Allergy;
import clinicmanager.models.Medication;
import java.sql.SQLException;
import java.util.List;

public interface IMedicalHistoryController {
    List<Patient> getAllPatients() throws SQLException;
    Patient getPatientById(int patientId) throws SQLException;
    String getPatientName(Patient patient);
    String getPatientNameById(int patientId) throws SQLException;
    
    List<MedicalCondition> getMedicalConditions(int patientId) throws SQLException;
    void addMedicalCondition(MedicalCondition condition) throws SQLException;
    void updateMedicalCondition(MedicalCondition condition) throws SQLException;
    void deleteMedicalCondition(int conditionId) throws SQLException;
    
    List<Allergy> getAllergies(int patientId) throws SQLException;
    void addAllergy(Allergy allergy) throws SQLException;
    void updateAllergy(Allergy allergy) throws SQLException;
    void deleteAllergy(int allergyId) throws SQLException;
    
    List<Medication> getMedications(int patientId) throws SQLException;
    void addMedication(Medication medication) throws SQLException;
    void updateMedication(Medication medication) throws SQLException;
    void deleteMedication(int medicationId) throws SQLException;
    
    String validateCondition(String conditionName);
    String validateAllergy(String allergyName);
    String validateMedication(String medicationName);
}
