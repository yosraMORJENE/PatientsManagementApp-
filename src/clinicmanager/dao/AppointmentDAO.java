package clinicmanager.dao;

import clinicmanager.models.Appointment;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class AppointmentDAO {
    private Connection connection;

    public AppointmentDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new appointment
    public void addAppointment(Appointment appointment) throws SQLException {
        boolean hasStatusColumn = checkIfStatusExists();
        String sql = hasStatusColumn ?
            "INSERT INTO Appointments (patient_id, appointment_date, reason, status) VALUES (?, ?, ?, ?)" :
            "INSERT INTO Appointments (patient_id, appointment_date, reason) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setInt(paramIndex++, appointment.getPatientId());
            
            // Convert string timestamp to java.sql.Timestamp
            if (appointment.getAppointmentDate() != null && !appointment.getAppointmentDate().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    java.util.Date utilDate = sdf.parse(appointment.getAppointmentDate().trim());
                    stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(utilDate.getTime()));
                } catch (Exception e) {
                    // Try alternative format with seconds
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        java.util.Date utilDate = sdf.parse(appointment.getAppointmentDate().trim());
                        stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(utilDate.getTime()));
                    } catch (Exception e2) {
                        throw new SQLException("Invalid date format. Please use YYYY-MM-DD HH:MM or YYYY-MM-DD HH:MM:SS format.", e2);
                    }
                }
            } else {
                throw new SQLException("Appointment date is required.");
            }
            
            stmt.setString(paramIndex++, appointment.getReason());
            
            if (hasStatusColumn) {
                stmt.setString(paramIndex++, appointment.getStatus() != null ? appointment.getStatus() : "scheduled");
            }
            
            stmt.executeUpdate();
        }
    }
    
    // Check if status column exists
    private boolean checkIfStatusExists() {
        try {
            String sql = "SELECT status FROM Appointments LIMIT 0";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Get appointment status counts for a patient (scheduled/completed/missed)
    public AppointmentStatusCount getStatusCountsForPatient(int patientId) throws SQLException {
        boolean hasStatusColumn = checkIfStatusExists();
        AppointmentStatusCount counts = new AppointmentStatusCount();

        if (!hasStatusColumn) {
            // Backward compatibility: if no status column, treat all as scheduled
            String fallbackSql = "SELECT COUNT(*) AS total FROM Appointments WHERE patient_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(fallbackSql)) {
                stmt.setInt(1, patientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        counts.scheduled = rs.getInt("total");
                    }
                }
            }
            return counts;
        }

        String sql = "SELECT status, COUNT(*) AS cnt FROM Appointments WHERE patient_id = ? GROUP BY status";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int c = rs.getInt("cnt");
                    if ("completed".equalsIgnoreCase(status)) {
                        counts.completed = c;
                    } else if ("missed".equalsIgnoreCase(status)) {
                        counts.missed = c;
                    } else if ("cancelled".equalsIgnoreCase(status)) {
                        counts.cancelled = c;
                    } else { // scheduled or any other
                        counts.scheduled += c;
                    }
                }
            }
        }

        return counts;
    }

    // Simple holder for appointment status counts
    public static class AppointmentStatusCount {
        public int scheduled;
        public int completed;
        public int missed;
        public int cancelled;
    }

    // Summary string with counts and dates per status for a patient
    public String getStatusSummaryForPatient(int patientId) throws SQLException {
        boolean hasStatusColumn = checkIfStatusExists();
        if (!hasStatusColumn) {
            // Backward compatibility: show total as scheduled
            String sql = "SELECT COUNT(*) AS total, MIN(appointment_date) AS first_date FROM Appointments WHERE patient_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total");
                        Timestamp first = rs.getTimestamp("first_date");
                        String firstStr = first != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(first) : "-";
                        return String.format("Scheduled: %d (%s)", total, firstStr);
                    }
                }
            }
            return "Scheduled: 0";
        }

        int scheduled = 0, completed = 0, missed = 0;
        Timestamp firstScheduled = null, lastCompleted = null, lastMissed = null;
        
        // Get scheduled appointments - use MIN (earliest date)
        String scheduledSql = "SELECT COUNT(*) AS cnt, MIN(appointment_date) AS first_date FROM Appointments WHERE patient_id = ? AND (status IS NULL OR status = 'scheduled')";
        try (PreparedStatement stmt = connection.prepareStatement(scheduledSql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    scheduled = rs.getInt("cnt");
                    firstScheduled = rs.getTimestamp("first_date");
                }
            }
        }
        
        // Get completed appointments - use MAX (latest date)
        String completedSql = "SELECT COUNT(*) AS cnt, MAX(appointment_date) AS last_date FROM Appointments WHERE patient_id = ? AND status = 'completed'";
        try (PreparedStatement stmt = connection.prepareStatement(completedSql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    completed = rs.getInt("cnt");
                    lastCompleted = rs.getTimestamp("last_date");
                }
            }
        }
        
        // Get missed appointments - use MAX (latest date)
        String missedSql = "SELECT COUNT(*) AS cnt, MAX(appointment_date) AS last_date FROM Appointments WHERE patient_id = ? AND status = 'missed'";
        try (PreparedStatement stmt = connection.prepareStatement(missedSql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    missed = rs.getInt("cnt");
                    lastMissed = rs.getTimestamp("last_date");
                }
            }
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        String schedStr = scheduled + formatDate(firstScheduled, sdf, "");
        String compStr = completed + formatDate(lastCompleted, sdf, "last: ");
        String missStr = missed + formatDate(lastMissed, sdf, "last: ");

        return String.format("Scheduled: %s\nCompleted: %s\nMissed: %s", schedStr, compStr, missStr);
    }

    private String formatDate(Timestamp ts, java.text.SimpleDateFormat sdf, String label) {
        if (ts == null) return "";
        return " (" + label + sdf.format(ts) + ")";
    }

    // Retrieve all appointments
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        boolean hasStatusColumn = checkIfStatusExists();
        boolean hasAuditColumns = checkIfAuditColumnsExist();
        String sql = "SELECT * FROM Appointments ORDER BY appointment_date";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat auditSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("appointment_date");
                String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                
                String status = hasStatusColumn ? rs.getString("status") : "scheduled";
                String createdAt = null;
                String updatedAt = null;
                
                if (hasAuditColumns) {
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp updated = rs.getTimestamp("updated_at");
                    createdAt = (created != null) ? auditSdf.format(created) : null;
                    updatedAt = (updated != null) ? auditSdf.format(updated) : null;
                }
                
                Appointment appointment = new Appointment(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    dateString,
                    rs.getString("reason"),
                    status,
                    createdAt,
                    updatedAt
                );
                appointments.add(appointment);
            }
        }
        return appointments;
    }
    
    // Check if audit columns exist
    private boolean checkIfAuditColumnsExist() {
        try {
            String sql = "SELECT created_at, updated_at FROM Appointments LIMIT 0";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Get appointments by patient ID
    public List<Appointment> getAppointmentsByPatientId(int patientId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        boolean hasStatusColumn = checkIfStatusExists();
        boolean hasAuditColumns = checkIfAuditColumnsExist();
        String sql = "SELECT * FROM Appointments WHERE patient_id = ? ORDER BY appointment_date";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat auditSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("appointment_date");
                    String dateString = (timestamp != null) ? sdf.format(timestamp) : null;
                    
                    String status = hasStatusColumn ? rs.getString("status") : "scheduled";
                    String createdAt = null;
                    String updatedAt = null;
                    
                    if (hasAuditColumns) {
                        Timestamp created = rs.getTimestamp("created_at");
                        Timestamp updated = rs.getTimestamp("updated_at");
                        createdAt = (created != null) ? auditSdf.format(created) : null;
                        updatedAt = (updated != null) ? auditSdf.format(updated) : null;
                    }
                    
                    Appointment appointment = new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        dateString,
                        rs.getString("reason"),
                        status,
                        createdAt,
                        updatedAt
                    );
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    // Check for appointment conflicts (improved logic)
    // Only checks for conflicts with active appointments (not cancelled or no_show)
    // Returns true if there are too many concurrent appointments (capacity check)
    public boolean hasConflict(String appointmentDate) throws SQLException {
        return hasConflict(appointmentDate, -1, 5); // default: check existing, max 5 concurrent
    }
    
    public boolean hasConflict(String appointmentDate, int excludeAppointmentId, int maxConcurrent) throws SQLException {
        boolean hasStatusColumn = checkIfStatusExists();
        
        // Only count active appointments (exclude cancelled and no_show)
        String sql = hasStatusColumn ?
            "SELECT COUNT(*) FROM Appointments WHERE appointment_date = ? AND id != ? AND status NOT IN ('cancelled', 'no_show')" :
            "SELECT COUNT(*) FROM Appointments WHERE appointment_date = ? AND id != ?";
        
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
            
            stmt.setInt(2, excludeAppointmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count >= maxConcurrent; // Conflict if at or over capacity
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
    
    // ============== JAVA STREAMS EXAMPLES ==============
    
    /**
     * Get today's appointments using streams
     * Example of Stream API with filter for date matching
     */
    public List<Appointment> getTodayAppointmentsStream() throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        String today = LocalDate.now().toString();
        
        return allAppointments.stream()
            .filter(a -> a.getAppointmentDate() != null && 
                        a.getAppointmentDate().startsWith(today))
            .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming appointments using streams
     * Example of Stream API with date filtering
     */
    public List<Appointment> getUpcomingAppointmentsStream() throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        String today = LocalDate.now().toString();
        
        return allAppointments.stream()
            .filter(a -> a.getAppointmentDate() != null && 
                        a.getAppointmentDate().compareTo(today) >= 0)
            .collect(Collectors.toList());
    }
    
    /**
     * Count appointments per patient
     * Example of Stream API with groupingBy and counting
     */
    public Map<Integer, Long> countAppointmentsPerPatientStream() throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        return allAppointments.stream()
            .collect(Collectors.groupingBy(
                Appointment::getPatientId,
                Collectors.counting()
            ));
    }
    
    /**
     * Get appointments filtered by reason keyword using streams
     * Example of Stream API with filter and contains
     */
    public List<Appointment> filterAppointmentsByReasonStream(String keyword) throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        return allAppointments.stream()
            .filter(a -> a.getReason() != null && 
                        a.getReason().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }
}
