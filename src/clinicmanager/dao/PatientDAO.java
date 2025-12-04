package clinicmanager.dao;

import clinicmanager.models.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private Connection connection = null;

    public PatientDAO(Connection connection) {
        this.connection = connection;
    }

    public PatientDAO() {
    }

    // Create a new patient
    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patients (first_name, last_name, date_of_birth, phone_number, email, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setString(3, patient.getDateOfBirth());
            stmt.setString(4, patient.getPhoneNumber());
            stmt.setString(5, patient.getEmail());
            stmt.setString(6, patient.getAddress());
            stmt.executeUpdate();
        }
    }

    // Retrieve all patients
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("date_of_birth"),
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    rs.getString("address")
                );
                patients.add(patient);
            }
        }
        return patients;
    }

    // Update an existing patient
    public void updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE Patients SET first_name = ?, last_name = ?, date_of_birth = ?, phone_number = ?, email = ?, address = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setString(3, patient.getDateOfBirth());
            stmt.setString(4, patient.getPhoneNumber());
            stmt.setString(5, patient.getEmail());
            stmt.setString(6, patient.getAddress());
            stmt.setInt(7, patient.getId());
            stmt.executeUpdate();
        }
    }

    // Delete a patient
    public void deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM Patients WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.executeUpdate();
        }
    }

    // Get patient by ID
    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM Patients WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("date_of_birth"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address")
                    );
                }
            }
        }
        return null;
    }

    // Search patients by name
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR phone_number LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("date_of_birth"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address")
                    );
                    patients.add(patient);
                }
            }
        }
        return patients;
    }
}