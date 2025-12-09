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
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 250, 255));
        
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(245, 250, 255));
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        
        // Button panel with better spacing
        JPanel buttonPanel = createButtonPanel();
        
        mainContent.add(titlePanel, BorderLayout.NORTH);
        mainContent.add(buttonPanel, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.NORTH);
        
        // Register as data change listener
        DataChangeManager.getInstance().addListener(this);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 250, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Reports and Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        JLabel descLabel = new JLabel("Export clinic data to CSV format for analysis and record keeping");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 25, 25));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Export Options", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        
        JPanel patientCard = createReportCard("Export Patient List", 
            "Export all patient records to CSV", new Color(46, 204, 113), e -> exportPatientList());
        panel.add(patientCard);
        
        JPanel appointmentCard = createReportCard("Export Appointments", 
            "Export all appointment records to CSV", new Color(52, 152, 219), e -> exportAppointments());
        panel.add(appointmentCard);
        
        JPanel statsCard = createReportCard("Export Statistics", 
            "Export clinic statistics and metrics to CSV", new Color(155, 89, 182), e -> exportStatistics());
        panel.add(statsCard);

        JPanel todayApptCard = createReportCard("Export Today's Appointments", 
            "Export only today's appointments to CSV", new Color(241, 196, 15), e -> exportTodaysAppointments());
        panel.add(todayApptCard);

        JPanel folderCard = createReportCard("Open Reports Folder", 
            "Open the reports directory in file explorer", new Color(230, 126, 34), e -> openReportsFolder());
        panel.add(folderCard);

        return panel;
    }

    private void exportTodaysAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            java.io.File reportsDir = new java.io.File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
            java.io.File file = new java.io.File("reports/TodaysAppointments_" + timestamp + ".csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);

            writer.write("ID,Patient ID,Patient Name,Date & Time,Reason,Status\n");

            int count = 0;
            for (Appointment apt : appointments) {
                if (apt.getAppointmentDate() != null && apt.getAppointmentDate().startsWith(today)) {
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
                        count++;
                    } catch (SQLException e) {
                        System.err.println("Error getting patient for appointment: " + e.getMessage());
                    }
                }
            }

            writer.close();
            JOptionPane.showMessageDialog(this, 
                "Successfully exported " + count + " appointments for today!\nFile: " + file.getAbsolutePath(), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting today's appointments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    // removed extra closing brace here

    }

    private JPanel createReportCard(String title, String description, Color bgColor, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(220, 220, 220));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.SOUTH);
        
        // Add click listener for the card
        java.awt.event.MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
            private Color originalColor = bgColor;
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    action.actionPerformed(null);
                } catch (Exception ex) {
                }
            }
        };
        
        card.addMouseListener(mouseAdapter);
        titleLabel.addMouseListener(mouseAdapter);
        descLabel.addMouseListener(mouseAdapter);
        
        return card;
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
                String dob = p.getDateOfBirth();
                if (dob == null || dob.isEmpty()) {
                    dob = "";
                }
                
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                    p.getId(),
                    p.getFirstName(),
                    p.getLastName(),
                    p.getPhoneNumber() != null ? p.getPhoneNumber() : "",
                    p.getEmail() != null ? p.getEmail() : "",
                    p.getAddress() != null ? p.getAddress().replace("\"", "\"\"") : "",
                    dob
                ));
            }
            
            writer.close();
            JOptionPane.showMessageDialog(this, 
                "Successfully exported " + patients.size() + " patients!\nFile: " + file.getAbsolutePath(), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
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
                    System.err.println("Error getting patient for appointment: " + e.getMessage());
                }
            }
            
            writer.close();
            JOptionPane.showMessageDialog(this, 
                "Successfully exported " + appointments.size() + " appointments!\nFile: " + file.getAbsolutePath(), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
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
    }

    @Override
    public void onAppointmentsChanged() {
    }

    @Override
    public void onMedicalHistoryChanged() {
    }
}
