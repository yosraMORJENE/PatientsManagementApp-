package clinicmanager.dao;

import clinicmanager.models.Medication;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicationDAO {
    private Connection connection;

    public MedicationDAO(Connection connection) {
        this.connection = connection;
    }

    // add medication
    public void addMedication(Medication medication) throws SQLException {
        String sql = "INSERT INTO medications (patient_id, medication_name, dosage, frequency, start_date, end_date, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, medication.getPatientId());
            stmt.setString(2, medication.getMedicationName());
            stmt.setString(3, medication.getDosage());
            stmt.setString(4, medication.getFrequency());
            
            // handle start date, dont be strict
            if (medication.getStartDate() != null && !medication.getStartDate().trim().isEmpty()) {
                try {
                    stmt.setDate(5, java.sql.Date.valueOf(medication.getStartDate().trim()));
                } catch (IllegalArgumentException e) {
                    // if date is bad just set null
                    stmt.setNull(5, java.sql.Types.DATE);
                }
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            
            // end date too
            if (medication.getEndDate() != null && !medication.getEndDate().trim().isEmpty()) {
                try {
                    stmt.setDate(6, java.sql.Date.valueOf(medication.getEndDate().trim()));
                } catch (IllegalArgumentException e) {
                    // same thing, just set null
                    stmt.setNull(6, java.sql.Types.DATE);
                }
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            
            stmt.setString(7, medication.getStatus() != null ? medication.getStatus() : "active");
            stmt.setString(8, medication.getNotes());
            stmt.executeUpdate();
        }
    }

    // get all medications
    public List<Medication> getMedicationsByPatientId(int patientId) throws SQLException {
        List<Medication> medications = new ArrayList<>();
        String sql = "SELECT * FROM medications WHERE patient_id = ? ORDER BY start_date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date startDate = rs.getDate("start_date");
                    Date endDate = rs.getDate("end_date");
                    Medication med = new Medication(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("medication_name"),
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        startDate != null ? startDate.toString() : "",
                        endDate != null ? endDate.toString() : "",
                        rs.getString("status"),
                        rs.getString("notes")
                    );
                    medications.add(med);
                }
            }
        }
        return medications;
    }

    // get just active medications
    public List<Medication> getActiveMedicationsByPatientId(int patientId) throws SQLException {
        List<Medication> medications = new ArrayList<>();
        String sql = "SELECT * FROM medications WHERE patient_id = ? AND status = 'active' ORDER BY start_date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date startDate = rs.getDate("start_date");
                    Date endDate = rs.getDate("end_date");
                    Medication med = new Medication(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("medication_name"),
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        startDate != null ? startDate.toString() : "",
                        endDate != null ? endDate.toString() : "",
                        rs.getString("status"),
                        rs.getString("notes")
                    );
                    medications.add(med);
                }
            }
        }
        return medications;
    }

    // update medication
    public void updateMedication(Medication medication) throws SQLException {
        String sql = "UPDATE medications SET medication_name = ?, dosage = ?, frequency = ?, start_date = ?, end_date = ?, status = ?, notes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, medication.getMedicationName());
            stmt.setString(2, medication.getDosage());
            stmt.setString(3, medication.getFrequency());
            if (medication.getStartDate() != null && !medication.getStartDate().isEmpty()) {
                stmt.setDate(4, java.sql.Date.valueOf(medication.getStartDate()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            if (medication.getEndDate() != null && !medication.getEndDate().isEmpty()) {
                stmt.setDate(5, java.sql.Date.valueOf(medication.getEndDate()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            stmt.setString(6, medication.getStatus());
            stmt.setString(7, medication.getNotes());
            stmt.setInt(8, medication.getId());
            stmt.executeUpdate();
        }
    }

    // delete medication
    public void deleteMedication(int medicationId) throws SQLException {
        String sql = "DELETE FROM medications WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, medicationId);
            stmt.executeUpdate();
        }
    }

    // get specfic medication
    public Medication getMedicationById(int id) throws SQLException {
        String sql = "SELECT * FROM medications WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date startDate = rs.getDate("start_date");
                    Date endDate = rs.getDate("end_date");
                    return new Medication(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("medication_name"),
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        startDate != null ? startDate.toString() : "",
                        endDate != null ? endDate.toString() : "",
                        rs.getString("status"),
                        rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }
}