package clinicmanager.controllers;

import java.sql.SQLException;
import java.util.List;

public interface IDashboardController {
    int getTotalPatients() throws SQLException;
    int getTotalAppointments() throws SQLException;
    int getCompletedAppointments() throws SQLException;
    int getMissedAppointments() throws SQLException;
    int getCancelledAppointments() throws SQLException;
    List<DashboardController.AppointmentInfo> getTodayAppointments() throws SQLException;
}
