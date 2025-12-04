package clinicmanager.gui;

import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.dao.VisitDAO;
import clinicmanager.database.DatabaseConnection;
import clinicmanager.models.Appointment;
import clinicmanager.models.Patient;
import clinicmanager.models.Visit;
import clinicmanager.util.ValidationUtil;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private PatientDAO patientDAO;
    private AppointmentDAO appointmentDAO;
    private VisitDAO visitDAO;
    private Connection connection;

    public MainFrame() {
        try {
            connection = DatabaseConnection.getConnection();
            patientDAO = new PatientDAO(connection);
            appointmentDAO = new AppointmentDAO(connection);
            visitDAO = new VisitDAO(connection);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Clinic Manager - Patient Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |IllegalAccessException | UnsupportedLookAndFeelException e) {
        }

        // Create tabbed pane with custom styling
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(new Color(240, 248, 255));
        
        // Add tabs
        tabbedPane.addTab("Patients", new PatientPanel(patientDAO));
        tabbedPane.addTab("Appointments", new AppointmentPanel(appointmentDAO, patientDAO));
        tabbedPane.addTab("Visits", new VisitPanel(visitDAO, appointmentDAO, patientDAO));

        // Add tabbed pane to frame
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add status bar
        JLabel statusBar = new JLabel(" Ready");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(statusBar, BorderLayout.SOUTH);
    }

    @Override
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
        }
        super.dispose();
    }

    // Patient Panel
    class PatientPanel extends JPanel {
        private final PatientDAO patientDAO;
        private JTable patientTable;
        private DefaultTableModel tableModel;
        private JTextField firstNameField, lastNameField, dobField, phoneField, emailField, addressField;
        private JTextField searchField;
        private JButton saveButton, updateButton, deleteButton, clearButton, searchButton, refreshButton;
        private int selectedPatientId = -1;

        public PatientPanel(PatientDAO patientDAO) {
            this.patientDAO = patientDAO;
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(new Color(245, 250, 255));

            // Create form panel
            JPanel formPanel = createFormPanel();
            
            // Create table panel
            JPanel tablePanel = createTablePanel();
            
            // Create button panel
            JPanel buttonPanel = createButtonPanel();

            // Add components
            add(formPanel, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            
            // Load initial data
            refreshTable();
        }

        private JPanel createFormPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Patient Information",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
            panel.setBackground(Color.WHITE);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // First Name
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("First Name *:"), gbc);
            firstNameField = new JTextField(20);
            firstNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 1;
            panel.add(firstNameField, gbc);

            // Last Name
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Last Name *:"), gbc);
            lastNameField = new JTextField(20);
            lastNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 1;
            panel.add(lastNameField, gbc);

            // Date of Birth
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
            dobField = new JTextField(20);
            dobField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 1;
            panel.add(dobField, gbc);

            // Phone Number
            gbc.gridx = 2; gbc.gridy = 0;
            panel.add(new JLabel("Phone Number:"), gbc);
            phoneField = new JTextField(20);
            phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 3;
            panel.add(phoneField, gbc);

            // Email
            gbc.gridx = 2; gbc.gridy = 1;
            panel.add(new JLabel("Email:"), gbc);
            emailField = new JTextField(20);
            emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 3;
            panel.add(emailField, gbc);

            // Address
            gbc.gridx = 2; gbc.gridy = 2;
            panel.add(new JLabel("Address:"), gbc);
            addressField = new JTextField(20);
            addressField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 3;
            panel.add(addressField, gbc);

            return panel;
        }

        private JPanel createTablePanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Patient List",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));

            // Search panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.add(new JLabel("Search:"));
            searchField = new JTextField(25);
            searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            searchPanel.add(searchField);
            searchButton = new JButton("Search");
            searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            searchButton.setBackground(new Color(0, 102, 204));
            searchButton.setForeground(Color.WHITE);
            searchButton.addActionListener(e -> searchPatients());
            searchPanel.add(searchButton);
            refreshButton = new JButton("Refresh");
            refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            refreshButton.setBackground(new Color(34, 139, 34));
            refreshButton.setForeground(Color.WHITE);
            refreshButton.addActionListener(e -> refreshTable());
            searchPanel.add(refreshButton);
            panel.add(searchPanel, BorderLayout.NORTH);

            // Table
            String[] columns = {"ID", "First Name", "Last Name", "Date of Birth", "Phone", "Email", "Address"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            patientTable = new JTable(tableModel);
            patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            patientTable.setRowHeight(25);
            patientTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedPatient();
                }
            });

            // Set column widths
            patientTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            patientTable.getColumnModel().getColumn(1).setPreferredWidth(120);
            patientTable.getColumnModel().getColumn(2).setPreferredWidth(120);
            patientTable.getColumnModel().getColumn(3).setPreferredWidth(120);
            patientTable.getColumnModel().getColumn(4).setPreferredWidth(120);
            patientTable.getColumnModel().getColumn(5).setPreferredWidth(180);
            patientTable.getColumnModel().getColumn(6).setPreferredWidth(200);

            JScrollPane scrollPane = new JScrollPane(patientTable);
            scrollPane.setPreferredSize(new Dimension(0, 300));
            panel.add(scrollPane, BorderLayout.CENTER);

            return panel;
        }

        private JPanel createButtonPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            panel.setBackground(new Color(245, 250, 255));

            saveButton = new JButton("Save New Patient");
            saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            saveButton.setBackground(new Color(34, 139, 34));
            saveButton.setForeground(Color.WHITE);
            saveButton.setPreferredSize(new Dimension(150, 35));
            saveButton.addActionListener(e -> savePatient());

            updateButton = new JButton("Update Patient");
            updateButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            updateButton.setBackground(new Color(0, 102, 204));
            updateButton.setForeground(Color.WHITE);
            updateButton.setPreferredSize(new Dimension(150, 35));
            updateButton.addActionListener(e -> updatePatient());

            deleteButton = new JButton("Delete Patient");
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setPreferredSize(new Dimension(150, 35));
            deleteButton.addActionListener(e -> deletePatient());

            clearButton = new JButton("Clear Form");
            clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            clearButton.setBackground(new Color(128, 128, 128));
            clearButton.setForeground(Color.WHITE);
            clearButton.setPreferredSize(new Dimension(150, 35));
            clearButton.addActionListener(e -> clearForm());

            panel.add(saveButton);
            panel.add(updateButton);
            panel.add(deleteButton);
            panel.add(clearButton);

            return panel;
        }

        private void savePatient() {
            String error = ValidationUtil.validatePatientForm(
                firstNameField.getText(), lastNameField.getText(), dobField.getText(),
                phoneField.getText(), emailField.getText(), addressField.getText());
            
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Patient patient = new Patient(0,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    dobField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    addressField.getText().trim());
                
                patientDAO.addPatient(patient);
                JOptionPane.showMessageDialog(this, "Patient saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updatePatient() {
            if (selectedPatientId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a patient to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String error = ValidationUtil.validatePatientForm(
                firstNameField.getText(), lastNameField.getText(), dobField.getText(),
                phoneField.getText(), emailField.getText(), addressField.getText());
            
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to update this patient?", 
                "Confirm Update", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Patient patient = new Patient(selectedPatientId,
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        dobField.getText().trim(),
                        phoneField.getText().trim(),
                        emailField.getText().trim(),
                        addressField.getText().trim());
                    
                    patientDAO.updatePatient(patient);
                    JOptionPane.showMessageDialog(this, "Patient updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    refreshTable();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error updating patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void deletePatient() {
            if (selectedPatientId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a patient to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this patient? This action cannot be undone.", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    patientDAO.deletePatient(selectedPatientId);
                    JOptionPane.showMessageDialog(this, "Patient deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    refreshTable();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void clearForm() {
            selectedPatientId = -1;
            firstNameField.setText("");
            lastNameField.setText("");
            dobField.setText("");
            phoneField.setText("");
            emailField.setText("");
            addressField.setText("");
            patientTable.clearSelection();
        }

        private void loadSelectedPatient() {
            int row = patientTable.getSelectedRow();
            if (row >= 0) {
                selectedPatientId = (Integer) tableModel.getValueAt(row, 0);
                firstNameField.setText((String) tableModel.getValueAt(row, 1));
                lastNameField.setText((String) tableModel.getValueAt(row, 2));
                dobField.setText((String) tableModel.getValueAt(row, 3));
                phoneField.setText((String) tableModel.getValueAt(row, 4));
                emailField.setText((String) tableModel.getValueAt(row, 5));
                addressField.setText((String) tableModel.getValueAt(row, 6));
            }
        }

        private void refreshTable() {
            try {
                List<Patient> patients = patientDAO.getAllPatients();
                tableModel.setRowCount(0);
                for (Patient patient : patients) {
                    tableModel.addRow(new Object[]{
                        patient.getId(),
                        patient.getFirstName(),
                        patient.getLastName(),
                        patient.getDateOfBirth(),
                        patient.getPhoneNumber(),
                        patient.getEmail(),
                        patient.getAddress()
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void searchPatients() {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                refreshTable();
                return;
            }

            try {
                List<Patient> patients = patientDAO.searchPatients(searchTerm);
                tableModel.setRowCount(0);
                for (Patient patient : patients) {
                    tableModel.addRow(new Object[]{
                        patient.getId(),
                        patient.getFirstName(),
                        patient.getLastName(),
                        patient.getDateOfBirth(),
                        patient.getPhoneNumber(),
                        patient.getEmail(),
                        patient.getAddress()
                    });
                }
                if (patients.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No patients found matching: " + searchTerm, "Search Results", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error searching patients: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Appointment Panel
    class AppointmentPanel extends JPanel {
        private final AppointmentDAO appointmentDAO;
        private final PatientDAO patientDAO;
        private JTable appointmentTable;
        private DefaultTableModel tableModel;
        private JComboBox<PatientComboItem> patientCombo;
        private JTextField dateField, reasonField;
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
            patientCombo.setPreferredSize(new Dimension(300, 25));
            gbc.gridx = 1;
            panel.add(patientCombo, gbc);

            // Appointment Date
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Date & Time (YYYY-MM-DD HH:MM) *:"), gbc);
            dateField = new JTextField(20);
            dateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 1;
            panel.add(dateField, gbc);

            // Reason
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Reason:"), gbc);
            reasonField = new JTextField(20);
            reasonField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 1;
            panel.add(reasonField, gbc);

            return panel;
        }

        private JPanel createTablePanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Appointments",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));

            refreshButton = new JButton("Refresh");
            refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            refreshButton.setBackground(new Color(34, 139, 34));
            refreshButton.setForeground(Color.WHITE);
            refreshButton.addActionListener(e -> refreshTable());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.add(refreshButton);
            panel.add(buttonPanel, BorderLayout.NORTH);

            String[] columns = {"ID", "Patient ID", "Patient Name", "Date & Time", "Reason"};
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
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            panel.setBackground(new Color(245, 250, 255));

            saveButton = new JButton("Schedule Appointment");
            saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            saveButton.setBackground(new Color(34, 139, 34));
            saveButton.setForeground(Color.WHITE);
            saveButton.setPreferredSize(new Dimension(180, 35));
            saveButton.addActionListener(e -> saveAppointment());

            updateButton = new JButton("Update Appointment");
            updateButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            updateButton.setBackground(new Color(0, 102, 204));
            updateButton.setForeground(Color.WHITE);
            updateButton.setPreferredSize(new Dimension(180, 35));
            updateButton.addActionListener(e -> updateAppointment());

            deleteButton = new JButton("Cancel Appointment");
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setPreferredSize(new Dimension(180, 35));
            deleteButton.addActionListener(e -> deleteAppointment());

            clearButton = new JButton("Clear Form");
            clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            clearButton.setBackground(new Color(128, 128, 128));
            clearButton.setForeground(Color.WHITE);
            clearButton.setPreferredSize(new Dimension(180, 35));
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

            if (!ValidationUtil.isNotEmpty(dateField.getText())) {
                JOptionPane.showMessageDialog(this, "Appointment date is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Check for conflicts
                if (appointmentDAO.hasConflict(dateField.getText().trim())) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "Another appointment exists at this time. Do you want to schedule anyway?",
                        "Conflict Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                Appointment appointment = new Appointment(0,
                    selected.getId(),
                    dateField.getText().trim(),
                    reasonField.getText().trim());
                
                appointmentDAO.addAppointment(appointment);
                JOptionPane.showMessageDialog(this, "Appointment scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
                loadPatients();
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

            if (!ValidationUtil.isNotEmpty(dateField.getText())) {
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
                        dateField.getText().trim(),
                        reasonField.getText().trim());
                    
                    appointmentDAO.updateAppointment(appointment);
                    JOptionPane.showMessageDialog(this, "Appointment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    refreshTable();
                    loadPatients();
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
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void clearForm() {
            selectedAppointmentId = -1;
            patientCombo.setSelectedIndex(0);
            dateField.setText("");
            reasonField.setText("");
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
                
                dateField.setText((String) tableModel.getValueAt(row, 3));
                reasonField.setText((String) tableModel.getValueAt(row, 4));
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
                            appointment.getReason()
                        });
                    } catch (SQLException e) {
                        tableModel.addRow(new Object[]{
                            appointment.getId(),
                            appointment.getPatientId(),
                            "Error loading",
                            appointment.getAppointmentDate(),
                            appointment.getReason()
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
    }

    // Visit Panel
    class VisitPanel extends JPanel {
        private final VisitDAO visitDAO;
        private final AppointmentDAO appointmentDAO;
        private final PatientDAO patientDAO;
        private JTable visitTable;
        private DefaultTableModel tableModel;
        private JComboBox<AppointmentComboItem> appointmentCombo;
        private JTextField dateField;
        private JTextArea notesArea;
        private JButton saveButton, updateButton, deleteButton, clearButton, refreshButton;
        private int selectedVisitId = -1;

        public VisitPanel(VisitDAO visitDAO, AppointmentDAO appointmentDAO, PatientDAO patientDAO) {
            this.visitDAO = visitDAO;
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

            refreshTable();
            loadAppointments();
        }

        private JPanel createFormPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Visit Information",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
            panel.setBackground(Color.WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Appointment Selection
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Appointment *:"), gbc);
            appointmentCombo = new JComboBox<>();
            appointmentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            appointmentCombo.setPreferredSize(new Dimension(300, 25));
            gbc.gridx = 1;
            panel.add(appointmentCombo, gbc);

            // Visit Date
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Visit Date (YYYY-MM-DD) *:"), gbc);
            dateField = new JTextField(20);
            dateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gbc.gridx = 1;
            panel.add(dateField, gbc);

            // Notes
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Notes:"), gbc);
            notesArea = new JTextArea(4, 30);
            notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(notesArea);
            gbc.gridx = 1;
            panel.add(notesScroll, gbc);

            return panel;
        }

        private JPanel createTablePanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Visit History",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));

            refreshButton = new JButton("Refresh");
            refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            refreshButton.setBackground(new Color(34, 139, 34));
            refreshButton.setForeground(Color.WHITE);
            refreshButton.addActionListener(e -> refreshTable());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.add(refreshButton);
            panel.add(buttonPanel, BorderLayout.NORTH);

            String[] columns = {"ID", "Appointment ID", "Patient", "Visit Date", "Notes"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            visitTable = new JTable(tableModel);
            visitTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            visitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            visitTable.setRowHeight(25);
            visitTable.getColumnModel().getColumn(4).setPreferredWidth(300);
            visitTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedVisit();
                }
            });

            JScrollPane scrollPane = new JScrollPane(visitTable);
            scrollPane.setPreferredSize(new Dimension(0, 300));
            panel.add(scrollPane, BorderLayout.CENTER);

            return panel;
        }

        private JPanel createButtonPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            panel.setBackground(new Color(245, 250, 255));

            saveButton = new JButton("Record Visit");
            saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            saveButton.setBackground(new Color(34, 139, 34));
            saveButton.setForeground(Color.WHITE);
            saveButton.setPreferredSize(new Dimension(150, 35));
            saveButton.addActionListener(e -> saveVisit());

            updateButton = new JButton("Update Visit");
            updateButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            updateButton.setBackground(new Color(0, 102, 204));
            updateButton.setForeground(Color.WHITE);
            updateButton.setPreferredSize(new Dimension(150, 35));
            updateButton.addActionListener(e -> updateVisit());

            deleteButton = new JButton("Delete Visit");
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setPreferredSize(new Dimension(150, 35));
            deleteButton.addActionListener(e -> deleteVisit());

            clearButton = new JButton("Clear Form");
            clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            clearButton.setBackground(new Color(128, 128, 128));
            clearButton.setForeground(Color.WHITE);
            clearButton.setPreferredSize(new Dimension(150, 35));
            clearButton.addActionListener(e -> clearForm());

            panel.add(saveButton);
            panel.add(updateButton);
            panel.add(deleteButton);
            panel.add(clearButton);

            return panel;
        }

        private void loadAppointments() {
            try {
                List<Appointment> appointments = appointmentDAO.getAllAppointments();
                appointmentCombo.removeAllItems();
                appointmentCombo.addItem(new AppointmentComboItem(-1, "Select Appointment"));
                for (Appointment appointment : appointments) {
                    try {
                        Patient patient = patientDAO.getPatientById(appointment.getPatientId());
                        String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Unknown";
                        appointmentCombo.addItem(new AppointmentComboItem(appointment.getId(), 
                            "Appt #" + appointment.getId() + " - " + patientName + " (" + appointment.getAppointmentDate() + ")"));
                    } catch (SQLException e) {
                        appointmentCombo.addItem(new AppointmentComboItem(appointment.getId(), 
                            "Appt #" + appointment.getId() + " - " + appointment.getAppointmentDate()));
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void saveVisit() {
            AppointmentComboItem selected = (AppointmentComboItem) appointmentCombo.getSelectedItem();
            if (selected == null || selected.getId() == -1) {
                JOptionPane.showMessageDialog(this, "Please select an appointment.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isNotEmpty(dateField.getText())) {
                JOptionPane.showMessageDialog(this, "Visit date is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Visit visit = new Visit(0,
                    selected.getId(),
                    dateField.getText().trim(),
                    notesArea.getText().trim());
                
                visitDAO.addVisit(visit);
                JOptionPane.showMessageDialog(this, "Visit recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
                loadAppointments();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error recording visit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updateVisit() {
            if (selectedVisitId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a visit to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AppointmentComboItem selected = (AppointmentComboItem) appointmentCombo.getSelectedItem();
            if (selected == null || selected.getId() == -1) {
                JOptionPane.showMessageDialog(this, "Please select an appointment.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isNotEmpty(dateField.getText())) {
                JOptionPane.showMessageDialog(this, "Visit date is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to update this visit?", 
                "Confirm Update", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Visit visit = new Visit(selectedVisitId,
                        selected.getId(),
                        dateField.getText().trim(),
                        notesArea.getText().trim());
                    
                    visitDAO.updateVisit(visit);
                    JOptionPane.showMessageDialog(this, "Visit updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    refreshTable();
                    loadAppointments();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error updating visit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void deleteVisit() {
            if (selectedVisitId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a visit to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this visit?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    visitDAO.deleteVisit(selectedVisitId);
                    JOptionPane.showMessageDialog(this, "Visit deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    refreshTable();
                    loadAppointments();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting visit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void clearForm() {
            selectedVisitId = -1;
            appointmentCombo.setSelectedIndex(0);
            dateField.setText("");
            notesArea.setText("");
            visitTable.clearSelection();
        }

        private void loadSelectedVisit() {
            int row = visitTable.getSelectedRow();
            if (row >= 0) {
                selectedVisitId = (Integer) tableModel.getValueAt(row, 0);
                int appointmentId = (Integer) tableModel.getValueAt(row, 1);
                
                // Set appointment in combo
                for (int i = 0; i < appointmentCombo.getItemCount(); i++) {
                    AppointmentComboItem item = appointmentCombo.getItemAt(i);
                    if (item.getId() == appointmentId) {
                        appointmentCombo.setSelectedIndex(i);
                        break;
                    }
                }
                
                dateField.setText((String) tableModel.getValueAt(row, 3));
                notesArea.setText((String) tableModel.getValueAt(row, 4));
            }
        }

        private void refreshTable() {
            try {
                List<Visit> visits = visitDAO.getAllVisits();
                tableModel.setRowCount(0);
                for (Visit visit : visits) {
                    try {
                        Appointment appointment = appointmentDAO.getAppointmentById(visit.getAppointmentId());
                        String patientName = "Unknown";
                        if (appointment != null) {
                            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
                            patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Unknown";
                        }
                        tableModel.addRow(new Object[]{
                            visit.getId(),
                            visit.getAppointmentId(),
                            patientName,
                            visit.getVisitDate(),
                            visit.getNotes()
                        });
                    } catch (SQLException e) {
                        tableModel.addRow(new Object[]{
                            visit.getId(),
                            visit.getAppointmentId(),
                            "Error loading",
                            visit.getVisitDate(),
                            visit.getNotes()
                        });
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading visits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        class AppointmentComboItem {
            private final int id;
            private final String name;

            public AppointmentComboItem(int id, String name) {
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
    }
}
