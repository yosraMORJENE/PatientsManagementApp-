package clinicmanager.controllers;

import clinicmanager.dao.PatientDAO;
import clinicmanager.models.Patient;
import java.sql.SQLException;
import java.util.List;

// business logic for patients
public class PatientController {
    private final PatientDAO patientDAO;

    public PatientController(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    // validate patient data
    public String validatePatient(String firstName, String lastName, String phone, String email) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return "First name is required.";
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return "Last name is required.";
        }
        return null;
    }

    // save new patient
    public void savePatient(Patient patient) throws SQLException {
        patientDAO.addPatient(patient);
    }

    // update patient
    public void updatePatient(Patient patient) throws SQLException {
        patientDAO.updatePatient(patient);
    }

    // delete patient
    public void deletePatient(int patientId) throws SQLException {
        patientDAO.deletePatient(patientId);
    }

    // get all patients
    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAllPatients();
    }

    // search patient by search term
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        return patientDAO.searchPatients(searchTerm);
    }

    // get patient by id
    public Patient getPatientById(int id) throws SQLException {
        return patientDAO.getPatientById(id);
    }

    // get patient full name
    public String getPatientName(Patient patient) {
        if (patient == null) return "Unknown";
        return patient.getFirstName() + " " + patient.getLastName();
    }

    // get patient name by id
    public String getPatientNameById(int patientId) throws SQLException {
        Patient patient = patientDAO.getPatientById(patientId);
        return getPatientName(patient);
    }
}
