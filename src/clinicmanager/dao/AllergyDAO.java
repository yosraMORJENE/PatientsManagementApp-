package clinicmanager.dao;

import clinicmanager.models.Allergy;
import clinicmanager.util.ValidationUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllergyDAO {
    private Connection connection;

    public AllergyDAO(Connection connection) {
        this.connection = connection;
    }

    // Add a new allergy
    public void addAllergy(Allergy allergy) throws SQLException {
        // Validate and normalize severity before inserting
        String severity = ValidationUtil.normalizeSeverity(allergy.getSeverity());
        
        String sql = "INSERT INTO allergies (patient_id, allergen, reaction, severity, notes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, allergy.getPatientId());
            stmt.setString(2, allergy.getAllergen());
            stmt.setString(3, allergy.getReaction());
            stmt.setString(4, severity);
            stmt.setString(5, allergy.getNotes());
            stmt.executeUpdate();
        }
    }

    // Get all allergies for a patient
    public List<Allergy> getAllergiesByPatientId(int patientId) throws SQLException {
        List<Allergy> allergies = new ArrayList<>();
        String sql = "SELECT * FROM allergies WHERE patient_id = ? ORDER BY severity DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Allergy allergy = new Allergy(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("allergen"),
                        rs.getString("reaction"),
                        rs.getString("severity"),
                        rs.getString("notes")
                    );
                    allergies.add(allergy);
                }
            }
        }
        return allergies;
    }

    // Update an allergy
    public void updateAllergy(Allergy allergy) throws SQLException {
        String sql = "UPDATE allergies SET allergen = ?, reaction = ?, severity = ?, notes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, allergy.getAllergen());
            stmt.setString(2, allergy.getReaction());
            stmt.setString(3, allergy.getSeverity());
            stmt.setString(4, allergy.getNotes());
            stmt.setInt(5, allergy.getId());
            stmt.executeUpdate();
        }
    }

    // Delete an allergy
    public void deleteAllergy(int allergyId) throws SQLException {
        String sql = "DELETE FROM allergies WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, allergyId);
            stmt.executeUpdate();
        }
    }

    // Get a specific allergy
    public Allergy getAllergyById(int allergyId) throws SQLException {
        String sql = "SELECT * FROM allergies WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, allergyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Allergy(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("allergen"),
                        rs.getString("reaction"),
                        rs.getString("severity"),
                        rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }
}
