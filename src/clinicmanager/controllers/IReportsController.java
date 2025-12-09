package clinicmanager.controllers;

import clinicmanager.models.Appointment;
import clinicmanager.models.Patient;
import java.sql.SQLException;
import java.util.List;

public interface IReportsController {
    List<Patient> getAllPatients() throws SQLException;
    List<Appointment> getAllAppointments() throws SQLException;
    List<Appointment> getTodaysAppointments() throws SQLException;
    String getPatientNameById(int patientId) throws SQLException;
    int getAppointmentCountByStatus(String status) throws SQLException;
    int getTotalAppointments() throws SQLException;
    int getTotalPatients() throws SQLException;
    String formatDate(String dateString);
    String getAppointmentSummary(int patientId) throws SQLException;
}
