package clinicmanager.controllers;

import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.models.Appointment;
import clinicmanager.models.Patient;
import java.sql.SQLException;
import java.util.List;

// business logic for appointments
public class AppointmentController implements IAppointmentController {
    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;

    public AppointmentController(AppointmentDAO appointmentDAO, PatientDAO patientDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
    }

    // validate appointment data before save/update
    public String validateAppointment(int patientId, String dateTime, String status) {
        if (patientId == -1) {
            return "Please select a patient.";
        }
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return "Appointment date is required.";
        }
        if (status == null || status.trim().isEmpty()) {
            return "Status is required.";
        }
        return null;
    }

    // check if another appointment exists at same time
    public boolean hasConflict(String dateTime) throws SQLException {
        return appointmentDAO.hasConflict(dateTime);
    }

    // save new appointment
    public void saveAppointment(Appointment appointment) throws SQLException {
        appointmentDAO.addAppointment(appointment);
    }

    // update existing appointment
    public void updateAppointment(Appointment appointment) throws SQLException {
        appointmentDAO.updateAppointment(appointment);
    }

    // get all appointments
    public List<Appointment> getAllAppointments() throws SQLException {
        return appointmentDAO.getAllAppointments();
    }

    // get patient by id for display
    public Patient getPatientById(int patientId) throws SQLException {
        return patientDAO.getPatientById(patientId);
    }

    // get today's appointments only
    public List<Appointment> getTodaysAppointments() throws SQLException {
        List<Appointment> all = appointmentDAO.getAllAppointments();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        List<Appointment> today_appts = new java.util.ArrayList<>();
        for (Appointment apt : all) {
            if (apt.getAppointmentDate() != null && apt.getAppointmentDate().startsWith(today)) {
                today_appts.add(apt);
            }
        }
        return today_appts;
    }

    // get all patients for dropdown
    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAllPatients();
    }

    // format patient name from object
    public String getPatientName(Patient patient) {
        if (patient == null) return "Unknown";
        return patient.getFirstName() + " " + patient.getLastName();
    }

    // get patient name by id
    public String getPatientNameById(int patientId) throws SQLException {
        Patient patient = patientDAO.getPatientById(patientId);
        return getPatientName(patient);
    }
}
