package clinicmanager.dao;

import clinicmanager.models.Visit;
import java.sql.*;
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
            stmt.setString(2, visit.getVisitDate());
            stmt.setString(3, visit.getNotes());
            stmt.executeUpdate();
        }
    }

    // Retrieve all visits
    public List<Visit> getAllVisits() throws SQLException {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT * FROM Visits";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Visit visit = new Visit(
                    rs.getInt("id"),
                    rs.getInt("appointment_id"),
                    rs.getString("visit_date"),
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
            stmt.setString(2, visit.getVisitDate());
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
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Visit(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        rs.getString("visit_date"),
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
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Visit visit = new Visit(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        rs.getString("visit_date"),
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
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Visit visit = new Visit(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        rs.getString("visit_date"),
                        rs.getString("notes")
                    );
                    visits.add(visit);
                }
            }
        }
        return visits;
    }
}