package clinicmanager.dao;

import clinicmanager.models.Visit;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class VisitDAO {
    private Connection connection;

    public VisitDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new visit
    public void addVisit(Visit visit) throws SQLException {
        String sql = "INSERT INTO Visits (appointment_id, visit_date, notes) VALUES (?, ?, ?)";
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
            stmt.executeUpdate();
        }
    }

    // Retrieve all visits
    public List<Visit> getAllVisits() throws SQLException {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT * FROM Visits";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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
        return visits;
    }

    // Update an existing visit
    public void updateVisit(Visit visit) throws SQLException {
        String sql = "UPDATE Visits SET appointment_id = ?, visit_date = ?, notes = ? WHERE id = ?";
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
            stmt.setInt(4, visit.getId());
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
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("visit_date");
                    String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                    
                    return new Visit(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        dateString,
                        rs.getString("notes")
                    );
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
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
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
}