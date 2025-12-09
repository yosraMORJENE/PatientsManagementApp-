package clinicmanager.dao;

import clinicmanager.models.Prescription;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {
    private Connection connection;

    public PrescriptionDAO(Connection connection) {
        this.connection = connection;
    }

    // Add a new prescription
    public void addPrescription(Prescription prescription) throws SQLException {
        boolean hasVisitId = checkIfVisitIdExists();
        String sql = hasVisitId ?
            "INSERT INTO prescriptions (patient_id, visit_id, medication_name, dosage, quantity, frequency, duration_days, prescribed_date, refill_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO prescriptions (patient_id, medication_name, dosage, quantity, frequency, duration_days, prescribed_date, refill_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setInt(paramIndex++, prescription.getPatientId());
            
            if (hasVisitId) {
                if (prescription.getVisitId() != null) {
                    stmt.setInt(paramIndex++, prescription.getVisitId());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.INTEGER);
                }
            }
            
            stmt.setString(paramIndex++, prescription.getMedicationName());
            stmt.setString(paramIndex++, prescription.getDosage());
            stmt.setInt(paramIndex++, prescription.getQuantity());
            stmt.setString(paramIndex++, prescription.getFrequency());
            stmt.setInt(paramIndex++, prescription.getDurationDays());
            if (prescription.getPrescribedDate() != null && !prescription.getPrescribedDate().isEmpty()) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(prescription.getPrescribedDate()));
            } else {
                stmt.setDate(paramIndex++, new java.sql.Date(System.currentTimeMillis()));
            }
            if (prescription.getRefillDate() != null && !prescription.getRefillDate().isEmpty()) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(prescription.getRefillDate()));
            } else {
                stmt.setNull(paramIndex++, java.sql.Types.DATE);
            }
            stmt.setString(paramIndex++, prescription.getNotes());
            stmt.executeUpdate();
        }
    }
    
    // Check if visit_id column exists
    private boolean checkIfVisitIdExists() {
        try {
            String sql = "SELECT visit_id FROM prescriptions LIMIT 0";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Get all prescriptions for a patient
    public List<Prescription> getPrescriptionsByPatientId(int patientId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        boolean hasVisitId = checkIfVisitIdExists();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? ORDER BY prescribed_date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date prescribedDate = rs.getDate("prescribed_date");
                    Date refillDate = rs.getDate("refill_date");
                    Integer visitId = hasVisitId && rs.getObject("visit_id") != null ? rs.getInt("visit_id") : null;
                    
                    Prescription prescription = new Prescription(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        visitId,
                        rs.getString("medication_name"),
                        rs.getString("dosage"),
                        rs.getInt("quantity"),
                        rs.getString("frequency"),
                        rs.getInt("duration_days"),
                        prescribedDate != null ? prescribedDate.toString() : "",
                        refillDate != null ? refillDate.toString() : "",
                        rs.getString("notes")
                    );
                    prescriptions.add(prescription);
                }
            }
        }
        return prescriptions;
    }

    // Get upcoming refills (within 7 days)
    public List<Prescription> getUpcomingRefills() throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        boolean hasVisitId = checkIfVisitIdExists();
        String sql = "SELECT * FROM prescriptions WHERE refill_date IS NOT NULL AND refill_date <= CURRENT_DATE + INTERVAL '7 days' AND refill_date > CURRENT_DATE ORDER BY refill_date ASC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date prescribedDate = rs.getDate("prescribed_date");
                Date refillDate = rs.getDate("refill_date");
                Integer visitId = hasVisitId && rs.getObject("visit_id") != null ? rs.getInt("visit_id") : null;
                
                Prescription prescription = new Prescription(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    visitId,
                    rs.getString("medication_name"),
                    rs.getString("dosage"),
                    rs.getInt("quantity"),
                    rs.getString("frequency"),
                    rs.getInt("duration_days"),
                    prescribedDate != null ? prescribedDate.toString() : "",
                    refillDate != null ? refillDate.toString() : "",
                    rs.getString("notes")
                );
                prescriptions.add(prescription);
            }
        }
        return prescriptions;
    }

    // Update a prescription
    public void updatePrescription(Prescription prescription) throws SQLException {
        String sql = "UPDATE prescriptions SET medication_name = ?, dosage = ?, quantity = ?, frequency = ?, duration_days = ?, prescribed_date = ?, refill_date = ?, notes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prescription.getMedicationName());
            stmt.setString(2, prescription.getDosage());
            stmt.setInt(3, prescription.getQuantity());
            stmt.setString(4, prescription.getFrequency());
            stmt.setInt(5, prescription.getDurationDays());
            if (prescription.getPrescribedDate() != null && !prescription.getPrescribedDate().isEmpty()) {
                stmt.setDate(6, java.sql.Date.valueOf(prescription.getPrescribedDate()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            if (prescription.getRefillDate() != null && !prescription.getRefillDate().isEmpty()) {
                stmt.setDate(7, java.sql.Date.valueOf(prescription.getRefillDate()));
            } else {
                stmt.setNull(7, java.sql.Types.DATE);
            }
            stmt.setString(8, prescription.getNotes());
            stmt.setInt(9, prescription.getId());
            stmt.executeUpdate();
        }
    }

    // Delete a prescription
    public void deletePrescription(int prescriptionId) throws SQLException {
        String sql = "DELETE FROM prescriptions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, prescriptionId);
            stmt.executeUpdate();
        }
    }

    // Get a specific prescription
    public Prescription getPrescriptionById(int prescriptionId) throws SQLException {
        String sql = "SELECT * FROM prescriptions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, prescriptionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date prescribedDate = rs.getDate("prescribed_date");
                    Date refillDate = rs.getDate("refill_date");
                    return new Prescription(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("medication_name"),
                        rs.getString("dosage"),
                        rs.getInt("quantity"),
                        rs.getString("frequency"),
                        rs.getInt("duration_days"),
                        prescribedDate != null ? prescribedDate.toString() : "",
                        refillDate != null ? refillDate.toString() : "",
                        rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }
}