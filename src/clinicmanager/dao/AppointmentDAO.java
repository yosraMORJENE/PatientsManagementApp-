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

    // add new apointment
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
    
    // check if theres a status colum or not
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

    // gets all the appointment counts per patient
    public AppointmentStatusCount getStatusCountsForPatient(int patientId) throws SQLException {
        boolean hasStatusColumn = checkIfStatusExists();
        AppointmentStatusCount counts = new AppointmentStatusCount();

        if (!hasStatusColumn) {
            // if no status just count everything as scheduled lol
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
        
        // grabs scheduled ones, using earliest date
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
        
        // completed ones, get latest date
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
        
        // missed ones too
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

    // gets all appointments
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
    
    // check if audit colums exist
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

    // get appts for specific patient
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

    // checks if theres too many appointments at same time
    // skips cancelled stuff
    // returns true if maxed out
    public boolean hasConflict(String appointmentDate) throws SQLException {
        return hasConflict(appointmentDate, -1, 5); // i set max to 5 appointments
    }
    
    public boolean hasConflict(String appointmentDate, int excludeAppointmentId, int maxConcurrent) throws SQLException {
        boolean hasStatusColumn = checkIfStatusExists();
        
        // dont count canceled ones
        String sql = hasStatusColumn ?
            "SELECT COUNT(*) FROM Appointments WHERE appointment_date = ? AND id != ? AND status NOT IN ('cancelled', 'no_show')" :
            "SELECT COUNT(*) FROM Appointments WHERE appointment_date = ? AND id != ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // covert the date string
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                java.util.Date utilDate = sdf.parse(appointmentDate.trim());
                stmt.setTimestamp(1, new java.sql.Timestamp(utilDate.getTime()));
            } catch (Exception e) {
                // trying with seconds too
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
                    return count >= maxConcurrent; // if over limit return true
                }
            }
        }
        return false;
    }

    // update appointment
    public void updateAppointment(Appointment appointment) throws SQLException {
        String sql = "UPDATE Appointments SET patient_id = ?, appointment_date = ?, reason = ?, status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointment.getPatientId());
            
            // gotta convert this too
            if (appointment.getAppointmentDate() != null && !appointment.getAppointmentDate().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    java.util.Date utilDate = sdf.parse(appointment.getAppointmentDate().trim());
                    stmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                } catch (Exception e) {
                    // trying alt format
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
            stmt.setString(4, appointment.getStatus() != null ? appointment.getStatus() : "scheduled");
            stmt.setInt(5, appointment.getId());
            stmt.executeUpdate();
        }
    }

    // delete appointment
    public void deleteAppointment(int appointmentId) throws SQLException {
        String sql = "DELETE FROM Appointments WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
        }
    }

    // get one apointment by id
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
    
    //  Exaemppp of Stream API with filter for date matching
    
    public List<Appointment> getTodayAppointmentsStream() throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        String today = LocalDate.now().toString();
        
        return allAppointments.stream()
            .filter(a -> a.getAppointmentDate() != null && 
                        a.getAppointmentDate().startsWith(today))
            .collect(Collectors.toList());
    }
    
 //yjibli upcoming appointments using stream
    public List<Appointment> getUpcomingAppointmentsStream() throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        String today = LocalDate.now().toString();
        
        return allAppointments.stream()
            .filter(a -> a.getAppointmentDate() != null && 
                        a.getAppointmentDate().compareTo(today) >= 0)
            .collect(Collectors.toList());
    }
    
    
     //ehseb 9dh appoin lel patient
    public Map<Integer, Long> countAppointmentsPerPatientStream() throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        return allAppointments.stream()
            .collect(Collectors.groupingBy(
                Appointment::getPatientId,
                Collectors.counting()
            ));
    }
    
   //filtri il appointments bel reason using stream
    public List<Appointment> filterAppointmentsByReasonStream(String keyword) throws SQLException {
        List<Appointment> allAppointments = getAllAppointments();
        return allAppointments.stream()
            .filter(a -> a.getReason() != null && 
                        a.getReason().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }
}
