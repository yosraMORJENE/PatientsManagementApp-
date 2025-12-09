package clinicmanager.controllers;

import clinicmanager.models.Patient;
import java.sql.SQLException;
import java.util.List;

public interface IPatientController {
    String validatePatient(String firstName, String lastName, String phone, String email);
    void savePatient(Patient patient) throws SQLException;
    void updatePatient(Patient patient) throws SQLException;
    void deletePatient(int patientId) throws SQLException;
    List<Patient> getAllPatients() throws SQLException;
    List<Patient> searchPatients(String searchTerm) throws SQLException;
    Patient getPatientById(int id) throws SQLException;
    String getPatientName(Patient patient);
    String getPatientNameById(int patientId) throws SQLException;
}
