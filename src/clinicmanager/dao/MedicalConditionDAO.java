package clinicmanager.dao;

import clinicmanager.models.MedicalCondition;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalConditionDAO {
    private Connection connection;

    public MedicalConditionDAO(Connection connection) {
        this.connection = connection;
    }

    // add condition
    public void addMedicalCondition(MedicalCondition condition) throws SQLException {
        boolean hasResolvedDate = checkIfResolvedDateExists();
        String sql = hasResolvedDate ?
            "INSERT INTO medical_conditions (patient_id, condition_name, diagnosis_date, status, resolved_date, notes) VALUES (?, ?, ?, ?, ?, ?)" :
            "INSERT INTO medical_conditions (patient_id, condition_name, diagnosis_date, status, notes) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setInt(paramIndex++, condition.getPatientId());
            stmt.setString(paramIndex++, condition.getConditionName());
            if (condition.getDiagnosisDate() != null && !condition.getDiagnosisDate().isEmpty()) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(condition.getDiagnosisDate()));
            } else {
                stmt.setNull(paramIndex++, java.sql.Types.DATE);
            }
            stmt.setString(paramIndex++, condition.getStatus() != null ? condition.getStatus() : "active");
            
            if (hasResolvedDate) {
                if (condition.getResolvedDate() != null && !condition.getResolvedDate().isEmpty()) {
                    stmt.setDate(paramIndex++, java.sql.Date.valueOf(condition.getResolvedDate()));
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.DATE);
                }
            }
            
            stmt.setString(paramIndex++, condition.getNotes());
            stmt.executeUpdate();
        }
    }
    
    // check if resolved_date colum exists
    private boolean checkIfResolvedDateExists() {
        try {
            String sql = "SELECT resolved_date FROM medical_conditions LIMIT 0";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // gets all conditions for patient
    public List<MedicalCondition> getMedicalConditionsByPatientId(int patientId) throws SQLException {
        List<MedicalCondition> conditions = new ArrayList<>();
        boolean hasResolvedDate = checkIfResolvedDateExists();
        String sql = "SELECT * FROM medical_conditions WHERE patient_id = ? ORDER BY diagnosis_date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date diagnosisDate = rs.getDate("diagnosis_date");
                    Date resolvedDate = hasResolvedDate ? rs.getDate("resolved_date") : null;
                    
                    MedicalCondition condition = hasResolvedDate ?
                        new MedicalCondition(
                            rs.getInt("id"),
                            rs.getInt("patient_id"),
                            rs.getString("condition_name"),
                            diagnosisDate != null ? diagnosisDate.toString() : "",
                            rs.getString("status"),
                            resolvedDate != null ? resolvedDate.toString() : null,
                            rs.getString("notes")
                        ) :
                        new MedicalCondition(
                            rs.getInt("id"),
                            rs.getInt("patient_id"),
                            rs.getString("condition_name"),
                            diagnosisDate != null ? diagnosisDate.toString() : "",
                            rs.getString("status"),
                            rs.getString("notes")
                        );
                    conditions.add(condition);
                }
            }
        }
        return conditions;
    }

    // update condition
    public void updateMedicalCondition(MedicalCondition condition) throws SQLException {
        String sql = "UPDATE medical_conditions SET condition_name = ?, diagnosis_date = ?, status = ?, notes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, condition.getConditionName());
            if (condition.getDiagnosisDate() != null && !condition.getDiagnosisDate().isEmpty()) {
                stmt.setDate(2, java.sql.Date.valueOf(condition.getDiagnosisDate()));
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }
            stmt.setString(3, condition.getStatus());
            stmt.setString(4, condition.getNotes());
            stmt.setInt(5, condition.getId());
            stmt.executeUpdate();
        }
    }

    // delete condition
    public void deleteMedicalCondition(int conditionId) throws SQLException {
        String sql = "DELETE FROM medical_conditions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, conditionId);
            stmt.executeUpdate();
        }
    }

    // get condition by id
    public MedicalCondition getMedicalConditionById(int id) throws SQLException {
        String sql = "SELECT * FROM medical_conditions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date diagnosisDate = rs.getDate("diagnosis_date");
                    return new MedicalCondition(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("condition_name"),
                        diagnosisDate != null ? diagnosisDate.toString() : "",
                        rs.getString("status"),
                        rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }
}