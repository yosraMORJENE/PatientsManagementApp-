package clinicmanager.gui;

import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.models.Appointment;
import clinicmanager.models.Patient;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;

public class ReportsPanel extends JPanel implements DataChangeListener {
    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;

    public ReportsPanel(PatientDAO patientDAO, AppointmentDAO appointmentDAO) {
        this.patientDAO = patientDAO;
        this.appointmentDAO = appointmentDAO;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 250, 255));
        
        JPanel buttonPanel = createButtonPanel();
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // Register as data change listener
        DataChangeManager.getInstance().addListener(this);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            "Export Reports", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        panel.setBackground(Color.WHITE);
        
        JButton patientListBtn = MainFrame.createModernButton("Export Patient List (CSV)", 
            new Color(46, 204, 113), new Color(39, 174, 96), 200, 50);
        patientListBtn.addActionListener(e -> exportPatientList());
        panel.add(patientListBtn);
        
        JButton appointmentBtn = MainFrame.createModernButton("Export Appointments (CSV)", 
            new Color(52, 152, 219), new Color(41, 128, 185), 200, 50);
        appointmentBtn.addActionListener(e -> exportAppointments());
        panel.add(appointmentBtn);
        
        JButton statsBtn = MainFrame.createModernButton("Export Statistics (CSV)", 
            new Color(155, 89, 182), new Color(142, 68, 173), 200, 50);
        statsBtn.addActionListener(e -> exportStatistics());
        panel.add(statsBtn);
        
        JButton openFolderBtn = MainFrame.createModernButton("Open Reports Folder", 
            new Color(230, 126, 34), new Color(209, 109, 25), 200, 50);
        openFolderBtn.addActionListener(e -> openReportsFolder());
        panel.add(openFolderBtn);
        
        return panel;
    }

    private void exportPatientList() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            
            java.io.File reportsDir = new java.io.File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
            java.io.File file = new java.io.File("reports/PatientList_" + timestamp + ".csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            
            writer.write("ID,First Name,Last Name,Phone,Email,Address,Date of Birth\n");
            
            for (Patient p : patients) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    p.getId(),
                    p.getFirstName(),
                    p.getLastName(),
                    p.getPhoneNumber() != null ? p.getPhoneNumber() : "",
                    p.getEmail() != null ? p.getEmail() : "",
                    p.getAddress() != null ? p.getAddress() : "",
                    p.getDateOfBirth() != null ? p.getDateOfBirth() : ""
                ));
            }
            
            writer.close();
            JOptionPane.showMessageDialog(this, "Patient list exported successfully!\nFile: " + file.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting patient list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            
            java.io.File reportsDir = new java.io.File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
            java.io.File file = new java.io.File("reports/Appointments_" + timestamp + ".csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            
            writer.write("ID,Patient ID,Patient Name,Date & Time,Reason,Status\n");
            
            for (Appointment apt : appointments) {
                try {
                    Patient patient = patientDAO.getPatientById(apt.getPatientId());
                    String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Unknown";
                    
                    writer.write(String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        apt.getId(),
                        apt.getPatientId(),
                        patientName,
                        apt.getAppointmentDate(),
                        apt.getReason() != null ? apt.getReason() : "",
                        apt.getStatus() != null ? apt.getStatus() : "scheduled"
                    ));
                } catch (SQLException e) {
                }
            }
            
            writer.close();
            JOptionPane.showMessageDialog(this, "Appointments exported successfully!\nFile: " + file.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting appointments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportStatistics() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            
            int completedAppts = 0;
            int missedAppts = 0;
            int scheduledAppts = 0;
            int cancelledAppts = 0;
            
            for (Appointment apt : appointments) {
                String status = apt.getStatus();
                if (status != null) {
                    if (status.equals("completed")) completedAppts++;
                    else if (status.equals("missed")) missedAppts++;
                    else if (status.equals("scheduled")) scheduledAppts++;
                    else if (status.equals("cancelled")) cancelledAppts++;
                }
            }
            
            java.io.File reportsDir = new java.io.File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
            java.io.File file = new java.io.File("reports/Statistics_" + timestamp + ".csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            
            writer.write("Metric,Value\n");
            writer.write("Total Patients," + patients.size() + "\n");
            writer.write("Total Appointments," + appointments.size() + "\n");
            writer.write("Scheduled Appointments," + scheduledAppts + "\n");
            writer.write("Completed Appointments," + completedAppts + "\n");
            writer.write("Missed Appointments," + missedAppts + "\n");
            writer.write("Cancelled Appointments," + cancelledAppts + "\n");
            
            if (appointments.size() > 0) {
                double completionRate = (completedAppts * 100.0) / appointments.size();
                writer.write("Completion Rate (%)," + String.format("%.2f", completionRate) + "\n");
            }
            
            writer.close();
            JOptionPane.showMessageDialog(this, "Statistics exported successfully!\nFile: " + file.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting statistics: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openReportsFolder() {
        try {
            java.io.File reportsDir = new java.io.File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }
            
            String path = reportsDir.getAbsolutePath();
            if (System.getProperty("os.name").startsWith("Windows")) {
                Runtime.getRuntime().exec("explorer.exe /select," + path);
            } else {
                Runtime.getRuntime().exec(new String[]{"open", path});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening folder: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onPatientsChanged() {
        // Data has changed, reports can use latest data on next export
    }

    @Override
    public void onAppointmentsChanged() {
        // Data has changed, reports can use latest data on next export
    }

    @Override
    public void onMedicalHistoryChanged() {
        // Can be implemented if needed
    }
}
