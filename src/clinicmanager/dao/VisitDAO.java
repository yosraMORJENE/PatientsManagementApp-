package clinicmanager.dao;

import clinicmanager.models.Visit;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

public class VisitDAO {
    private Connection connection;

    public VisitDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new visit
    public void addVisit(Visit visit) throws SQLException {
        // Check if new columns exist
        boolean hasNewColumns = checkIfColumnsExist();
        boolean hasPatientId = checkIfPatientIdExists();
        
        String sql;
        if (hasPatientId && hasNewColumns) {
            sql = "INSERT INTO Visits (patient_id, appointment_id, visit_date, notes, clinical_notes, diagnosis, treatment, follow_up_notes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else if (hasPatientId) {
            sql = "INSERT INTO Visits (patient_id, appointment_id, visit_date, notes, status) VALUES (?, ?, ?, ?, ?)";
        } else if (hasNewColumns) {
            sql = "INSERT INTO Visits (appointment_id, visit_date, notes, clinical_notes, diagnosis, treatment, follow_up_notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO Visits (appointment_id, visit_date, notes) VALUES (?, ?, ?)";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            
            // Set patient_id if column exists
            if (hasPatientId) {
                stmt.setInt(paramIndex++, visit.getPatientId());
            }
            
            // Set appointment_id (can be null now)
            if (visit.getAppointmentId() != null) {
                stmt.setInt(paramIndex++, visit.getAppointmentId());
            } else {
                stmt.setNull(paramIndex++, java.sql.Types.INTEGER);
            }
            
            // Convert string timestamp to java.sql.Timestamp
            if (visit.getVisitDate() != null && !visit.getVisitDate().trim().isEmpty()) {
                try {
                    // Try date format first (YYYY-MM-DD)
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date utilDate = sdf.parse(visit.getVisitDate().trim());
                    stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(utilDate.getTime()));
                } catch (Exception e) {
                    // Try timestamp format (YYYY-MM-DD HH:MM)
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        java.util.Date utilDate = sdf.parse(visit.getVisitDate().trim());
                        stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(utilDate.getTime()));
                    } catch (Exception e2) {
                        // Try with seconds
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            java.util.Date utilDate = sdf.parse(visit.getVisitDate().trim());
                            stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(utilDate.getTime()));
                        } catch (Exception e3) {
                            throw new SQLException("Invalid date format. Please use YYYY-MM-DD or YYYY-MM-DD HH:MM format.", e3);
                        }
                    }
                }
            } else {
                throw new SQLException("Visit date is required.");
            }
            
            stmt.setString(paramIndex++, visit.getNotes());
            
            if (hasNewColumns) {
                stmt.setString(paramIndex++, visit.getClinicalNotes());
                stmt.setString(paramIndex++, visit.getDiagnosis());
                stmt.setString(paramIndex++, visit.getTreatment());
                stmt.setString(paramIndex++, visit.getFollowUpNotes());
            }
            
            if (hasPatientId) {
                stmt.setString(paramIndex++, visit.getStatus() != null ? visit.getStatus() : "completed");
            }
            
            stmt.executeUpdate();
        }
    }
    
    // Check if patient_id column exists
    private boolean checkIfPatientIdExists() {
        try {
            String sql = "SELECT patient_id FROM Visits LIMIT 0";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Check if new clinical columns exist in the database
    private boolean checkIfColumnsExist() {
        try {
            String sql = "SELECT clinical_notes FROM Visits LIMIT 0";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Retrieve all visits
    public List<Visit> getAllVisits() throws SQLException {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT * FROM Visits";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean hasNewColumns = checkIfColumnsExist();
        boolean hasPatientId = checkIfPatientIdExists();
        
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("visit_date");
                String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                
                Visit visit;
                if (hasPatientId && hasNewColumns) {
                    // New schema with patient_id and clinical fields
                    Integer appointmentId = rs.getObject("appointment_id") != null ? rs.getInt("appointment_id") : null;
                    visit = new Visit(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        appointmentId,
                        dateString,
                        rs.getString("notes"),
                        rs.getString("clinical_notes"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment"),
                        rs.getString("follow_up_notes"),
                        rs.getString("status")
                    );
                } else if (hasNewColumns) {
                    // Old schema with clinical fields but no patient_id
                    visit = new Visit(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        dateString,
                        rs.getString("notes"),
                        rs.getString("clinical_notes"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment"),
                        rs.getString("follow_up_notes")
                    );
                } else {
                    // Original schema
                    visit = new Visit(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        dateString,
                        rs.getString("notes")
                    );
                }
                visits.add(visit);
            }
        }
        return visits;
    }

    // Update an existing visit
    public void updateVisit(Visit visit) throws SQLException {
        boolean hasNewColumns = checkIfColumnsExist();
        String sql = hasNewColumns ?
            "UPDATE Visits SET appointment_id = ?, visit_date = ?, notes = ?, clinical_notes = ?, diagnosis = ?, treatment = ?, follow_up_notes = ? WHERE id = ?" :
            "UPDATE Visits SET appointment_id = ?, visit_date = ?, notes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, visit.getAppointmentId());
            
            // Convert string timestamp to java.sql.Timestamp
            if (visit.getVisitDate() != null && !visit.getVisitDate().trim().isEmpty()) {
                try {
                    // Try date format first (YYYY-MM-DD)
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date utilDate = sdf.parse(visit.getVisitDate().trim());
                    stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                } catch (Exception e) {
                    // Try timestamp format (YYYY-MM-DD HH:MM)
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        java.util.Date utilDate = sdf.parse(visit.getVisitDate().trim());
                        stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                    } catch (Exception e2) {
                        // Try with seconds
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            java.util.Date utilDate = sdf.parse(visit.getVisitDate().trim());
                            stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                        } catch (Exception e3) {
                            throw new SQLException("Invalid date format. Please use YYYY-MM-DD or YYYY-MM-DD HH:MM format.", e3);
                        }
                    }
                }
            } else {
                throw new SQLException("Visit date is required.");
            }
            
            stmt.setString(3, visit.getNotes());
            if (hasNewColumns) {
                stmt.setString(4, visit.getClinicalNotes());
                stmt.setString(5, visit.getDiagnosis());
                stmt.setString(6, visit.getTreatment());
                stmt.setString(7, visit.getFollowUpNotes());
                stmt.setInt(8, visit.getId());
            } else {
                stmt.setInt(4, visit.getId());
            }
            stmt.executeUpdate();
        }
    }

    // Delete a visit
    public void deleteVisit(int visitId) throws SQLException {
        String sql = "DELETE FROM Visits WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, visitId);
            stmt.executeUpdate();
        }
    }

    // Get visit by ID
    public Visit getVisitById(int id) throws SQLException {
        String sql = "SELECT * FROM Visits WHERE id = ?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean hasNewColumns = checkIfColumnsExist();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("visit_date");
                    String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                    
                    if (hasNewColumns) {
                        return new Visit(
                            rs.getInt("id"),
                            rs.getInt("appointment_id"),
                            dateString,
                            rs.getString("notes"),
                            rs.getString("clinical_notes"),
                            rs.getString("diagnosis"),
                            rs.getString("treatment"),
                            rs.getString("follow_up_notes")
                        );
                    } else {
                        return new Visit(
                            rs.getInt("id"),
                            rs.getInt("appointment_id"),
                            dateString,
                            rs.getString("notes")
                        );
                    }
                }
            }
        }
        return null;
    }

    // Get visits by appointment ID
    public List<Visit> getVisitsByAppointmentId(int appointmentId) throws SQLException {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT * FROM Visits WHERE appointment_id = ? ORDER BY visit_date DESC";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean hasNewColumns = checkIfColumnsExist();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("visit_date");
                    String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                    
                    Visit visit;
                    if (hasNewColumns) {
                        visit = new Visit(
                            rs.getInt("id"),
                            rs.getInt("appointment_id"),
                            dateString,
                            rs.getString("notes"),
                            rs.getString("clinical_notes"),
                            rs.getString("diagnosis"),
                            rs.getString("treatment"),
                            rs.getString("follow_up_notes")
                        );
                    } else {
                        visit = new Visit(
                            rs.getInt("id"),
                            rs.getInt("appointment_id"),
                            dateString,
                            rs.getString("notes")
                        );
                    }
                    visits.add(visit);
                }
            }
        }
        return visits;
    }

    // Get visits by patient ID (through appointments)
    public List<Visit> getVisitsByPatientId(int patientId) throws SQLException {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT v.* FROM Visits v INNER JOIN Appointments a ON v.appointment_id = a.id WHERE a.patient_id = ? ORDER BY v.visit_date DESC";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("visit_date");
                    String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                    
                    Visit visit = new Visit(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        dateString,
                        rs.getString("notes")
                    );
                    visits.add(visit);
                }
            }
        }
        return visits;
    }
    
    // ============== JAVA STREAMS EXAMPLES ==============
    
    /**
     * Get visits sorted by date using streams
     * Example of Stream API with sorted
     */
    public List<Visit> getVisitsSortedByDateStream() throws SQLException {
        List<Visit> allVisits = getAllVisits();
        return allVisits.stream()
            .sorted(Comparator.comparing(Visit::getVisitDate, 
                                        Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter visits by notes keyword using streams
     * Example of Stream API with filter
     */
    public List<Visit> filterVisitsByNotesStream(String keyword) throws SQLException {
        List<Visit> allVisits = getAllVisits();
        return allVisits.stream()
            .filter(v -> v.getNotes() != null && 
                        v.getNotes().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Count visits per appointment
     * Example of Stream API with groupingBy and counting
     */
    public Map<Integer, Long> countVisitsPerAppointmentStream() throws SQLException {
        List<Visit> allVisits = getAllVisits();
        return allVisits.stream()
            .collect(Collectors.groupingBy(
                Visit::getAppointmentId,
                Collectors.counting()
            ));
    }
    
    /**
     * Get recent visits (limit using streams)
     * Example of Stream API with limit
     */
    public List<Visit> getRecentVisitsStream(int limit) throws SQLException {
        List<Visit> allVisits = getAllVisits();
        return allVisits.stream()
            .sorted(Comparator.comparing(Visit::getVisitDate, 
                                        Comparator.nullsLast(String::compareTo)).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
}