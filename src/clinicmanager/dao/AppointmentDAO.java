package clinicmanager.dao;

import clinicmanager.models.Appointment;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    private Connection connection;

    public AppointmentDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new appointment
    public void addAppointment(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO Appointments (patient_id, appointment_date, reason) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointment.getPatientId());
            
            // Convert string timestamp to java.sql.Timestamp
            if (appointment.getAppointmentDate() != null && !appointment.getAppointmentDate().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    java.util.Date utilDate = sdf.parse(appointment.getAppointmentDate().trim());
                    stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                } catch (Exception e) {
                    // Try alternative format with seconds
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        java.util.Date utilDate = sdf.parse(appointment.getAppointmentDate().trim());
                        stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                    } catch (Exception e2) {
                        throw new SQLException("Invalid date format. Please use YYYY-MM-DD HH:MM or YYYY-MM-DD HH:MM:SS format.", e2);
                    }
                }
            } else {
                throw new SQLException("Appointment date is required.");
            }
            
            stmt.setString(3, appointment.getReason());
            stmt.executeUpdate();
        }
    }

    // Retrieve all appointments
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM Appointments ORDER BY appointment_date";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("appointment_date");
                String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                
                Appointment appointment = new Appointment(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    dateString,
                    rs.getString("reason")
                );
                appointments.add(appointment);
            }
        }
        return appointments;
    }

    // Get appointments by patient ID
    public List<Appointment> getAppointmentsByPatientId(int patientId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM Appointments WHERE patient_id = ? ORDER BY appointment_date";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("appointment_date");
                    String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                    
                    Appointment appointment = new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        dateString,
                        rs.getString("reason")
                    );
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    // Check for appointment conflicts (same date/time)
    public boolean hasConflict(String appointmentDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Appointments WHERE appointment_date = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Convert string timestamp to java.sql.Timestamp
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                java.util.Date utilDate = sdf.parse(appointmentDate.trim());
                stmt.setTimestamp(1, new java.sql.Timestamp(utilDate.getTime()));
            } catch (Exception e) {
                // Try alternative format with seconds
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date utilDate = sdf.parse(appointmentDate.trim());
                    stmt.setTimestamp(1, new java.sql.Timestamp(utilDate.getTime()));
                } catch (Exception e2) {
                    throw new SQLException("Invalid date format.", e2);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Update an existing appointment
    public void updateAppointment(Appointment appointment) throws SQLException {
        String sql = "UPDATE Appointments SET patient_id = ?, appointment_date = ?, reason = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointment.getPatientId());
            
            // Convert string timestamp to java.sql.Timestamp
            if (appointment.getAppointmentDate() != null && !appointment.getAppointmentDate().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    java.util.Date utilDate = sdf.parse(appointment.getAppointmentDate().trim());
                    stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                } catch (Exception e) {
                    // Try alternative format with seconds
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        java.util.Date utilDate = sdf.parse(appointment.getAppointmentDate().trim());
                        stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                    } catch (Exception e2) {
                        throw new SQLException("Invalid date format. Please use YYYY-MM-DD HH:MM or YYYY-MM-DD HH:MM:SS format.", e2);
                    }
                }
            } else {
                throw new SQLException("Appointment date is required.");
            }
            
            stmt.setString(3, appointment.getReason());
            stmt.setInt(4, appointment.getId());
            stmt.executeUpdate();
        }
    }

    // Delete an appointment
    public void deleteAppointment(int appointmentId) throws SQLException {
        String sql = "DELETE FROM Appointments WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
        }
    }

    // Get appointment by ID
    public Appointment getAppointmentById(int id) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE id = ?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("appointment_date");
                    String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                    
                    return new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        dateString,
                        rs.getString("reason")
                    );
                }
            }
        }
        return null;
    }
}
