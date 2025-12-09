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

// business logic for reports and exports
public class ReportsController {
    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;

    public ReportsController(AppointmentDAO appointmentDAO, PatientDAO patientDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
    }

    // get all patients for reports
    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAllPatients();
    }

    // get all appointments for reports
    public List<Appointment> getAllAppointments() throws SQLException {
        return appointmentDAO.getAllAppointments();
    }

    // get today's appointments only
    public List<Appointment> getTodaysAppointments() throws SQLException {
        List<Appointment> all = appointmentDAO.getAllAppointments();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        List<Appointment> today_appts = new ArrayList<>();
        for (Appointment apt : all) {
            if (apt.getAppointmentDate() != null && apt.getAppointmentDate().startsWith(today)) {
                today_appts.add(apt);
            }
        }
        return today_appts;
    }

    // get patient name by id
    public String getPatientNameById(int patientId) throws SQLException {
        Patient patient = patientDAO.getPatientById(patientId);
        if (patient == null) return "Unknown";
        return patient.getFirstName() + " " + patient.getLastName();
    }

    // get appointment count by status
    public int getAppointmentCountByStatus(String status) throws SQLException {
        List<Appointment> all = appointmentDAO.getAllAppointments();
        int count = 0;
        for (Appointment apt : all) {
            if (status.equalsIgnoreCase(apt.getStatus() != null ? apt.getStatus() : "")) {
                count++;
            }
        }
        return count;
    }

    // get total appointments count
    public int getTotalAppointments() throws SQLException {
        return appointmentDAO.getAllAppointments().size();
    }

    // get total patients count
    public int getTotalPatients() throws SQLException {
        return patientDAO.getAllPatients().size();
    }

    // format date for reports
    public String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    // get appointment status summary for a patient
    public String getAppointmentSummary(int patientId) throws SQLException {
        return appointmentDAO.getStatusSummaryForPatient(patientId);
    }
}
