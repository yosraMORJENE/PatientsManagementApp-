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

public class DashboardPanel extends JPanel implements DataChangeListener {
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
        
        // Register as data change listener
        DataChangeManager.getInstance().addListener(this);
        
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
        JPanel todayPanel = new JPanel(new BorderLayout(10, 10));
        todayPanel.setBackground(new Color(245, 250, 255));
        todayPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            "Today's Appointments", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel appointmentListPanel = new JPanel();
        appointmentListPanel.setLayout(new BoxLayout(appointmentListPanel, BoxLayout.Y_AXIS));
        appointmentListPanel.setBackground(Color.WHITE);
        appointmentListPanel.setName("appointmentListPanel");
        
        JScrollPane scrollPane = new JScrollPane(appointmentListPanel);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        todayPanel.add(contentPanel, BorderLayout.CENTER);
        
        JButton refreshBtn = MainFrame.createModernButton("Refresh", 
            new Color(52, 152, 219), new Color(41, 128, 185), 120, 35);
        refreshBtn.addActionListener(e -> refreshStats());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 250, 255));
        buttonPanel.add(refreshBtn);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(245, 250, 255));
        wrapperPanel.add(todayPanel, BorderLayout.CENTER);
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

    private JPanel createAppointmentCard(String time, String patientName, String reason, String status) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Status indicator color
        Color statusColor = getStatusColor(status);
        
        // Time and status
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timePanel.setBackground(Color.WHITE);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timeLabel.setForeground(new Color(50, 50, 50));
        timePanel.add(timeLabel);
        
        JLabel statusLabel = new JLabel(status.toUpperCase());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBackground(statusColor);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        timePanel.add(statusLabel);
        
        // Patient name
        JLabel patientLabel = new JLabel(patientName);
        patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        patientLabel.setForeground(new Color(0, 102, 204));
        
        // Reason
        JLabel reasonLabel = new JLabel();
        if (reason != null && !reason.isEmpty()) {
            reasonLabel.setText("Reason: " + reason);
        } else {
            reasonLabel.setText("No reason specified");
        }
        reasonLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        reasonLabel.setForeground(new Color(100, 100, 100));
        
        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.add(patientLabel);
        detailsPanel.add(Box.createVerticalStrut(3));
        detailsPanel.add(reasonLabel);
        
        // Left indicator bar
        JPanel indicatorBar = new JPanel();
        indicatorBar.setBackground(statusColor);
        indicatorBar.setPreferredSize(new Dimension(4, 80));
        
        card.add(indicatorBar, BorderLayout.WEST);
        card.add(timePanel, BorderLayout.NORTH);
        card.add(detailsPanel, BorderLayout.CENTER);
        
        return card;
    }

    private Color getStatusColor(String status) {
        if (status == null) return new Color(52, 152, 219);
        switch (status.toLowerCase()) {
            case "completed":
                return new Color(46, 204, 113);
            case "missed":
                return new Color(231, 76, 60);
            case "cancelled":
                return new Color(149, 165, 166);
            case "scheduled":
            default:
                return new Color(52, 152, 219);
        }
    }

    private void refreshStats() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            int totalPatients = patients.size();
            
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            int totalAppts = appointments.size();
            int completedAppts = 0;
            int missedAppts = 0;
            
            for (Appointment apt : appointments) {
                String status = apt.getStatus();
                if (status != null) {
                    if (status.equals("completed")) completedAppts++;
                    else if (status.equals("missed")) missedAppts++;
                }
            }
            
            updateStatCard("Total Patients", totalPatients);
            updateStatCard("Total Appointments", totalAppts);
            updateStatCard("Completed Appointments", completedAppts);
            updateStatCard("Missed Appointments", missedAppts);
            
            updateTodayAppointments(appointments);
            
        } catch (SQLException e) {
            updateTodayAppointmentsError("Error loading data: " + e.getMessage());
        }
    }

    private void updateTodayAppointments(List<Appointment> appointments) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        
        JPanel listPanel = findAppointmentListPanel(this);
        if (listPanel == null) return;
        
        listPanel.removeAll();
        
        boolean hasTodayAppointments = false;
        
        for (Appointment apt : appointments) {
            if (apt.getAppointmentDate() != null && apt.getAppointmentDate().startsWith(today)) {
                hasTodayAppointments = true;
                try {
                    Patient patient = patientDAO.getPatientById(apt.getPatientId());
                    String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Unknown Patient";
                    String time = apt.getAppointmentDate().substring(11);
                    String reason = apt.getReason() != null ? apt.getReason() : "";
                    String status = apt.getStatus() != null ? apt.getStatus() : "scheduled";
                    
                    JPanel card = createAppointmentCard(time, patientName, reason, status);
                    listPanel.add(card);
                } catch (SQLException e) {
                    // Skip this appointment if patient info can't be loaded
                }
            }
        }
        
        if (!hasTodayAppointments) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
            emptyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel emptyLabel = new JLabel("📅 No appointments scheduled for today");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            listPanel.add(emptyPanel);
        }
        
        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }
    
    private JPanel findAppointmentListPanel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel && "appointmentListPanel".equals(comp.getName())) {
                return (JPanel) comp;
            } else if (comp instanceof Container) {
                JPanel result = findAppointmentListPanel((Container) comp);
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
        JPanel listPanel = findAppointmentListPanel(this);
        if (listPanel != null) {
            listPanel.removeAll();
            JLabel errorLabel = new JLabel("⚠ " + errorMsg);
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            errorLabel.setForeground(new Color(200, 50, 50));
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(Color.WHITE);
            errorPanel.add(errorLabel, BorderLayout.NORTH);
            listPanel.add(errorPanel);
            listPanel.revalidate();
            listPanel.repaint();
        }
    }

    @Override
    public void onPatientsChanged() {
        // Refresh statistics when patients change
        refreshStats();
    }

    @Override
    public void onAppointmentsChanged() {
        // Refresh statistics when appointments change
        refreshStats();
    }

    @Override
    public void onMedicalHistoryChanged() {
        // Can be implemented if needed
    }
}
