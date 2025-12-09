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
import javax.swing.table.DefaultTableModel;

public class AppointmentPanel extends JPanel implements DataChangeListener {
    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JComboBox<PatientComboItem> patientCombo;
    private JPanel dateField;
    private JTextField reasonField;
    private JComboBox<String> statusCombo;
    private JButton saveButton, updateButton, deleteButton, clearButton, refreshButton;
    private int selectedAppointmentId = -1;

    public AppointmentPanel(AppointmentDAO appointmentDAO, PatientDAO patientDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 250, 255));

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Register as data change listener
        DataChangeManager.getInstance().addListener(this);

        refreshTable();
        loadPatients();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Appointment Information",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Patient Selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Patient *:"), gbc);
        patientCombo = new JComboBox<>();
        patientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        patientCombo.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1;
        panel.add(patientCombo, gbc);
        
        // Refresh patients button
        JButton refreshPatientsBtn = new JButton("↻");
        refreshPatientsBtn.setPreferredSize(new Dimension(35, 25));
        refreshPatientsBtn.setToolTipText("Refresh patient list");
        refreshPatientsBtn.addActionListener(e -> loadPatients());
        gbc.gridx = 2;
        panel.add(refreshPatientsBtn, gbc);

        // Appointment Date
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Date & Time *:"), gbc);
        dateField = MainFrame.createDateTimePickerPanel();
        gbc.gridx = 1;
        panel.add(dateField, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Reason:"), gbc);
        reasonField = new JTextField(20);
        reasonField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        panel.add(reasonField, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Status *:"), gbc);
        statusCombo = new JComboBox<>(new String[]{"scheduled", "completed", "missed", "cancelled"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCombo.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1;
        panel.add(statusCombo, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Appointments",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));

        refreshButton = MainFrame.createModernButton("Refresh", 
            new Color(34, 139, 34), new Color(50, 160, 50), 100, 30);
        refreshButton.addActionListener(e -> refreshTable());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Patient ID", "Patient Name", "Date & Time", "Reason", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(25);
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedAppointment();
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(245, 250, 255));

        saveButton = MainFrame.createModernButton("Schedule Appointment", 
            new Color(34, 139, 34), new Color(50, 160, 50), 180, 40);
        saveButton.addActionListener(e -> saveAppointment());

        updateButton = MainFrame.createModernButton("Update Appointment", 
            new Color(0, 102, 204), new Color(0, 120, 240), 180, 40);
        updateButton.addActionListener(e -> updateAppointment());

        deleteButton = MainFrame.createModernButton("Cancel Appointment", 
            new Color(220, 20, 60), new Color(240, 40, 80), 180, 40);
        deleteButton.addActionListener(e -> deleteAppointment());

        clearButton = MainFrame.createModernButton("Clear Form", 
            new Color(128, 128, 128), new Color(150, 150, 150), 180, 40);
        clearButton.addActionListener(e -> clearForm());

        panel.add(saveButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);

        return panel;
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            patientCombo.removeAllItems();
            patientCombo.addItem(new PatientComboItem(-1, "Select Patient"));
            for (Patient patient : patients) {
                patientCombo.addItem(new PatientComboItem(patient.getId(), 
                    patient.getFirstName() + " " + patient.getLastName()));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAppointment() {
        PatientComboItem selected = (PatientComboItem) patientCombo.getSelectedItem();
        if (selected == null || selected.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dateTimeString = MainFrame.getDateTimeString(dateField);
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Appointment date is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Check for conflicts
            if (appointmentDAO.hasConflict(dateTimeString)) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Another appointment exists at this time. Do you want to schedule anyway?",
                    "Conflict Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            Appointment appointment = new Appointment(0,
                selected.getId(),
                dateTimeString,
                reasonField.getText().trim(),
                (String) statusCombo.getSelectedItem(),
                null,
                null);
            
            appointmentDAO.addAppointment(appointment);
            JOptionPane.showMessageDialog(this, "Appointment scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            loadPatients();
            // Notify other panels that appointments have changed
            DataChangeManager.getInstance().notifyAppointmentsChanged();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error scheduling appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAppointment() {
        if (selectedAppointmentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PatientComboItem selected = (PatientComboItem) patientCombo.getSelectedItem();
        if (selected == null || selected.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dateTimeString = MainFrame.getDateTimeString(dateField);
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Appointment date is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to update this appointment?", 
            "Confirm Update", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Appointment appointment = new Appointment(selectedAppointmentId,
                    selected.getId(),
                    dateTimeString,
                    reasonField.getText().trim(),
                    (String) statusCombo.getSelectedItem(),
                    null,
                    null);
                
                appointmentDAO.updateAppointment(appointment);
                JOptionPane.showMessageDialog(this, "Appointment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
                loadPatients();
                // Notify other panels that appointments have changed
                DataChangeManager.getInstance().notifyAppointmentsChanged();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAppointment() {
        if (selectedAppointmentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel this appointment?", 
            "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                appointmentDAO.deleteAppointment(selectedAppointmentId);
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
                loadPatients();
                // Notify other panels that appointments have changed
                DataChangeManager.getInstance().notifyAppointmentsChanged();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedAppointmentId = -1;
        patientCombo.setSelectedIndex(0);
        MainFrame.setDateTimeFromString(dateField, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        reasonField.setText("");
        statusCombo.setSelectedIndex(0);
        appointmentTable.clearSelection();
    }

    private void loadSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row >= 0) {
            selectedAppointmentId = (Integer) tableModel.getValueAt(row, 0);
            int patientId = (Integer) tableModel.getValueAt(row, 1);
            
            // Set patient in combo
            for (int i = 0; i < patientCombo.getItemCount(); i++) {
                PatientComboItem item = patientCombo.getItemAt(i);
                if (item.getId() == patientId) {
                    patientCombo.setSelectedIndex(i);
                    break;
                }
            }
            
            MainFrame.setDateTimeFromString(dateField, (String) tableModel.getValueAt(row, 3));
            reasonField.setText((String) tableModel.getValueAt(row, 4));
            String status = (String) tableModel.getValueAt(row, 5);
            if (status != null) {
                statusCombo.setSelectedItem(status);
            }
        }
    }

    private void refreshTable() {
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            tableModel.setRowCount(0);
            for (Appointment appointment : appointments) {
                try {
                    Patient patient = patientDAO.getPatientById(appointment.getPatientId());
                    String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Unknown";
                    tableModel.addRow(new Object[]{
                        appointment.getId(),
                        appointment.getPatientId(),
                        patientName,
                        appointment.getAppointmentDate(),
                        appointment.getReason(),
                        appointment.getStatus()
                    });
                } catch (SQLException e) {
                    tableModel.addRow(new Object[]{
                        appointment.getId(),
                        appointment.getPatientId(),
                        "Error loading",
                        appointment.getAppointmentDate(),
                        appointment.getReason(),
                        appointment.getStatus()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class PatientComboItem {
        private final int id;
        private final String name;

        public PatientComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public void onPatientsChanged() {
        // Reload patient list when patients change
        loadPatients();
    }

    @Override
    public void onAppointmentsChanged() {
        // Refresh appointment table when appointments change
        refreshTable();
    }

    @Override
    public void onMedicalHistoryChanged() {
        // Can be implemented if needed
    }
}
