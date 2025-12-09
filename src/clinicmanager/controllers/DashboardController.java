package clinicmanager.controllers;

import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.models.Appointment;
import clinicmanager.models.Patient;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// business logic for dashboard
public class DashboardController {
    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;

    public DashboardController(PatientDAO patientDAO, AppointmentDAO appointmentDAO) {
        this.patientDAO = patientDAO;
        this.appointmentDAO = appointmentDAO;
    }

    // get total patients count
    public int getTotalPatients() throws SQLException {
        return patientDAO.getAllPatients().size();
    }

    // get all appointments count
    public int getTotalAppointments() throws SQLException {
        return appointmentDAO.getAllAppointments().size();
    }

    // get completed appointments count
    public int getCompletedAppointments() throws SQLException {
        List<Appointment> all = appointmentDAO.getAllAppointments();
        int count = 0;
        for (Appointment apt : all) {
            if ("completed".equals(apt.getStatus())) {
                count++;
            }
        }
        return count;
    }

    // get missed appointments count
    public int getMissedAppointments() throws SQLException {
        List<Appointment> all = appointmentDAO.getAllAppointments();
        int count = 0;
        for (Appointment apt : all) {
            if ("missed".equals(apt.getStatus())) {
                count++;
            }
        }
        return count;
    }

    // get cancelled appointments count
    public int getCancelledAppointments() throws SQLException {
        List<Appointment> all = appointmentDAO.getAllAppointments();
        int count = 0;
        for (Appointment apt : all) {
            if ("cancelled".equals(apt.getStatus())) {
                count++;
            }
        }
        return count;
    }

    // get today's appointments that are not cancelled
    public List<AppointmentInfo> getTodayAppointments() throws SQLException {
        List<Appointment> all = appointmentDAO.getAllAppointments();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        List<AppointmentInfo> result = new ArrayList<>();

        for (Appointment apt : all) {
            String status = apt.getStatus() != null ? apt.getStatus() : "scheduled";
            // dont show cancelled appointments
            if ("cancelled".equalsIgnoreCase(status)) {
                continue;
            }

            if (apt.getAppointmentDate() != null && apt.getAppointmentDate().startsWith(today)) {
                try {
                    Patient patient = patientDAO.getPatientById(apt.getPatientId());
                    String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Unknown";
                    String time = apt.getAppointmentDate().substring(11);
                    
                    result.add(new AppointmentInfo(
                        time, patientName, apt.getReason() != null ? apt.getReason() : "", status
                    ));
                } catch (SQLException e) {
                    // skip if cant load patient
                }
            }
        }
        return result;
    }

    // simple wrapper class for appointment display data
    public static class AppointmentInfo {
        public String time;
        public String patientName;
        public String reason;
        public String status;

        public AppointmentInfo(String time, String patientName, String reason, String status) {
            this.time = time;
            this.patientName = patientName;
            this.reason = reason;
            this.status = status;
        }
    }
}
