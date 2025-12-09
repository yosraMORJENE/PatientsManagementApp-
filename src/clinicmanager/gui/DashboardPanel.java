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

public class DashboardPanel extends JPanel {
    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;

    public DashboardPanel(PatientDAO patientDAO, AppointmentDAO appointmentDAO) {
        this.patientDAO = patientDAO;
        this.appointmentDAO = appointmentDAO;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 250, 255));
        
        JPanel statsPanel = createStatsPanel();
        JPanel chartsPanel = createChartsPanel();
        
        add(statsPanel, BorderLayout.NORTH);
        add(chartsPanel, BorderLayout.CENTER);
        
        refreshStats();
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(new Color(245, 250, 255));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
            "Quick Statistics", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        
        JPanel totalPatientsCard = createStatCard("Total Patients", "0", new Color(52, 152, 219));
        totalPatientsCard.setName("totalPatients");
        panel.add(totalPatientsCard);
        
        JPanel totalApptsCard = createStatCard("Total Appointments", "0", new Color(46, 204, 113));
        totalApptsCard.setName("totalAppts");
        panel.add(totalApptsCard);
        
        JPanel completedCard = createStatCard("Completed Appointments", "0", new Color(155, 89, 182));
        completedCard.setName("completed");
        panel.add(completedCard);
        
        JPanel missedCard = createStatCard("Missed Appointments", "0", new Color(231, 76, 60));
        missedCard.setName("missed");
        panel.add(missedCard);
        
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setName(title);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 20));
        panel.setBackground(new Color(245, 250, 255));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            "Appointment Status Breakdown", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        
        JPanel statusPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        statusPanel.add(createStatusBar("Scheduled", "0", new Color(52, 152, 219), "scheduled"));
        statusPanel.add(createStatusBar("Completed", "0", new Color(46, 204, 113), "completed_status"));
        statusPanel.add(createStatusBar("Missed", "0", new Color(231, 76, 60), "missed_status"));
        statusPanel.add(createStatusBar("Cancelled", "0", new Color(149, 165, 166), "cancelled"));
        
        panel.add(statusPanel);
        
        JPanel todayPanel = new JPanel(new BorderLayout());
        todayPanel.setBackground(Color.WHITE);
        todayPanel.setBorder(BorderFactory.createTitledBorder("Today's Appointments"));
        
        JTextArea todayArea = new JTextArea();
        todayArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        todayArea.setEditable(false);
        todayArea.setLineWrap(true);
        todayArea.setWrapStyleWord(true);
        todayArea.setName("todayArea");
        todayArea.setText("Loading today's appointments...");
        
        todayPanel.add(new JScrollPane(todayArea), BorderLayout.CENTER);
        
        panel.add(todayPanel);
        
        JButton refreshBtn = MainFrame.createModernButton("Refresh", 
            new Color(52, 152, 219), new Color(41, 128, 185), 100, 30);
        refreshBtn.addActionListener(e -> refreshStats());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 250, 255));
        buttonPanel.add(refreshBtn);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(245, 250, 255));
        wrapperPanel.add(panel, BorderLayout.CENTER);
        wrapperPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return wrapperPanel;
    }

    private JPanel createStatusBar(String label, String value, Color color, String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel labelComponent = new JLabel(label + ": ");
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 11));
        labelComponent.setForeground(color);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueComponent.setForeground(color);
        valueComponent.setName(name);
        
        panel.add(labelComponent, BorderLayout.WEST);
        panel.add(valueComponent, BorderLayout.CENTER);
        
        return panel;
    }

    private void refreshStats() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            int totalPatients = patients.size();
            
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            int totalAppts = appointments.size();
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
            
            updateStatCard("Total Patients", totalPatients);
            updateStatCard("Total Appointments", totalAppts);
            updateStatCard("Completed Appointments", completedAppts);
            updateStatCard("Missed Appointments", missedAppts);
            
            updateStatusBar("scheduled", scheduledAppts);
            updateStatusBar("completed_status", completedAppts);
            updateStatusBar("missed_status", missedAppts);
            updateStatusBar("cancelled", cancelledAppts);
            
            updateTodayAppointments(appointments);
            
        } catch (SQLException e) {
            updateTodayAppointmentsError("Error loading data: " + e.getMessage());
        }
    }

    private void updateTodayAppointments(List<Appointment> appointments) {
        StringBuilder text = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        
        int todayCount = 0;
        for (Appointment apt : appointments) {
            if (apt.getAppointmentDate() != null && apt.getAppointmentDate().startsWith(today)) {
                todayCount++;
                try {
                    Patient patient = patientDAO.getPatientById(apt.getPatientId());
                    String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Unknown";
                    
                    text.append(String.format("[%s] %s - %s\n", 
                        apt.getStatus() != null ? apt.getStatus().toUpperCase() : "SCHEDULED",
                        apt.getAppointmentDate().substring(11),
                        patientName));
                    if (apt.getReason() != null && !apt.getReason().isEmpty()) {
                        text.append("   Reason: ").append(apt.getReason()).append("\n");
                    }
                    text.append("\n");
                } catch (SQLException e) {
                    text.append(String.format("[%s] %s - Patient ID: %d\n\n", 
                        apt.getStatus() != null ? apt.getStatus().toUpperCase() : "SCHEDULED",
                        apt.getAppointmentDate().substring(11),
                        apt.getPatientId()));
                }
            }
        }
        
        if (todayCount == 0) {
            text.append("No appointments scheduled for today.");
        } else {
            text.insert(0, String.format("=== %d Appointment(s) Today ===\n\n", todayCount));
        }
        
        JTextArea todayArea = findTodayArea(this);
        if (todayArea != null) {
            todayArea.setText(text.toString());
        }
    }
    
    private JTextArea findTodayArea(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextArea && "todayArea".equals(comp.getName())) {
                return (JTextArea) comp;
            } else if (comp instanceof Container) {
                JTextArea result = findTodayArea((Container) comp);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void updateStatCard(String title, int value) {
        for (Component comp : ((JPanel) getComponent(0)).getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                for (Component child : card.getComponents()) {
                    if (child instanceof JLabel) {
                        JLabel label = (JLabel) child;
                        if (title.equals(label.getName())) {
                            label.setText(String.valueOf(value));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void updateTodayAppointmentsError(String errorMsg) {
        JTextArea todayArea = findTodayArea(this);
        if (todayArea != null) {
            todayArea.setText("⚠ " + errorMsg);
            todayArea.setForeground(Color.RED);
        }
    }

    private void updateStatusBar(String name, int value) {
        JPanel chartPanel = (JPanel) getComponent(1);
        for (Component comp : chartPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component child : panel.getComponents()) {
                    if (child instanceof JPanel) {
                        JPanel statusPanel = (JPanel) child;
                        for (Component statusComp : statusPanel.getComponents()) {
                            if (statusComp instanceof JLabel) {
                                JLabel label = (JLabel) statusComp;
                                if (name.equals(label.getName())) {
                                    label.setText(String.valueOf(value));
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
