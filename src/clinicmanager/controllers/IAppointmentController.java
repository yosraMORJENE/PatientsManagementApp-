package clinicmanager.controllers;

import clinicmanager.models.Appointment;
import clinicmanager.models.Patient;
import java.sql.SQLException;
import java.util.List;

public interface IAppointmentController {
    String validateAppointment(int patientId, String dateTime, String status);
    boolean hasConflict(String dateTime) throws SQLException;
    void saveAppointment(Appointment appointment) throws SQLException;
    void updateAppointment(Appointment appointment) throws SQLException;
    List<Appointment> getAllAppointments() throws SQLException;
    Patient getPatientById(int patientId) throws SQLException;
    List<Appointment> getTodaysAppointments() throws SQLException;
    List<Patient> getAllPatients() throws SQLException;
    String getPatientName(Patient patient);
    String getPatientNameById(int patientId) throws SQLException;
}
