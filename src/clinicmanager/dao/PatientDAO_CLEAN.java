package clinicmanager.dao;

import clinicmanager.models.Patient;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

// clean version - essential CRUD operations only
public class PatientDAO_CLEAN {
    private Connection connection = null;

    public PatientDAO_CLEAN(Connection connection) {
        this.connection = connection;
    }

    // add new patient
    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patients (first_name, last_name, date_of_birth, phone_number, email, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            
            if (patient.getDateOfBirth() != null && !patient.getDateOfBirth().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date utilDate = sdf.parse(patient.getDateOfBirth().trim());
                    stmt.setDate(3, new java.sql.Date(utilDate.getTime()));
                } catch (Exception e) {
                    throw new SQLException("Invalid date format. Use YYYY-MM-DD.", e);
                }
            } else {
                stmt.setDate(3, null);
            }
            
            stmt.setString(4, patient.getPhoneNumber());
            stmt.setString(5, patient.getEmail());
            stmt.setString(6, patient.getAddress());
            stmt.executeUpdate();
        }
    }

    // get all patients
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date dob = rs.getDate("date_of_birth");
                Patient patient = new Patient(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    rs.getString("address"),
                    dob != null ? sdf.format(dob) : ""
                );
                patients.add(patient);
            }
        }
        return patients;
    }

    // get patient by id
    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM Patients WHERE id = ?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date dob = rs.getDate("date_of_birth");
                    return new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address"),
                        dob != null ? sdf.format(dob) : ""
                    );
                }
            }
        }
        return null;
    }

    // update patient
    public void updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE Patients SET first_name = ?, last_name = ?, date_of_birth = ?, phone_number = ?, email = ?, address = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            
            if (patient.getDateOfBirth() != null && !patient.getDateOfBirth().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date utilDate = sdf.parse(patient.getDateOfBirth().trim());
                    stmt.setDate(3, new java.sql.Date(utilDate.getTime()));
                } catch (Exception e) {
                    throw new SQLException("Invalid date format. Use YYYY-MM-DD.", e);
                }
            } else {
                stmt.setDate(3, null);
            }
            
            stmt.setString(4, patient.getPhoneNumber());
            stmt.setString(5, patient.getEmail());
            stmt.setString(6, patient.getAddress());
            stmt.setInt(7, patient.getId());
            stmt.executeUpdate();
        }
    }

    // delete patient
    public void deletePatient(int id) throws SQLException {
        String sql = "DELETE FROM Patients WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // search patients by search term (name, phone, email)
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients WHERE first_name LIKE ? OR last_name LIKE ? OR phone_number LIKE ? OR email LIKE ?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String term = "%" + searchTerm + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);
            stmt.setString(3, term);
            stmt.setString(4, term);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date dob = rs.getDate("date_of_birth");
                    Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address"),
                        dob != null ? sdf.format(dob) : ""
                    );
                    patients.add(patient);
                }
            }
        }
        return patients;
    }
}
