package clinicmanager.gui;

import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.models.Patient;
import clinicmanager.util.ValidationUtil;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PatientPanel extends JPanel implements DataChangeListener {
    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JTextField firstNameField, lastNameField, phoneField, emailField, addressField;
    private JPanel dobField;
    private JTextField searchField;
    private JButton saveButton, updateButton, deleteButton, clearButton, searchButton, refreshButton;
    private int selectedPatientId = -1;

    public PatientPanel(PatientDAO patientDAO, AppointmentDAO appointmentDAO) {
        this.patientDAO = patientDAO;
        this.appointmentDAO = appointmentDAO;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 250, 255));


        JPanel formPanel = createFormPanel();
        

        JPanel tablePanel = createTablePanel();
        

        JPanel buttonPanel = createButtonPanel();

        // add everything
        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        

        DataChangeManager.getInstance().addListener(this);
        

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


        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("First Name *:"), gbc);
        firstNameField = new JTextField(20);
        firstNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);


        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Last Name *:"), gbc);
        lastNameField = new JTextField(20);
        lastNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);


        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date of Birth:"), gbc);
        dobField = createDatePicker();
        gbc.gridx = 1;
        panel.add(dobField, gbc);


        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Phone Number:"), gbc);
        phoneField = new JTextField(20);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 3;
        panel.add(phoneField, gbc);


        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 3;
        panel.add(emailField, gbc);


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


        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(searchField);
        searchButton = MainFrame.createModernButton("Search", 
            new Color(0, 102, 204), new Color(0, 120, 240), 100, 30);
        searchButton.addActionListener(e -> searchPatients());
        searchPanel.add(searchButton);
        
        refreshButton = MainFrame.createModernButton("Refresh", 
            new Color(34, 139, 34), new Color(50, 160, 50), 100, 30);
        refreshButton.addActionListener(e -> refreshTable());
        searchPanel.add(refreshButton);
        panel.add(searchPanel, BorderLayout.NORTH);


        String[] columns = {"ID", "First Name", "Last Name", "Date of Birth", "Phone", "Email", "Address", "Appointment Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        patientTable = new JTable(tableModel);
        patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(70);
        patientTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (column == 7) { // Appointment Status column - show as HTML for multi-line
                    label.setVerticalAlignment(javax.swing.SwingConstants.TOP);
                    String text = value != null ? value.toString().replace("\n", "<br>") : "";
                    label.setText("<html><body>" + text + "</body></html>");
                }
                return label;
            }
        });
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedPatient();
            }
        });


        patientTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        patientTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        patientTable.getColumnModel().getColumn(7).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(245, 250, 255));

        saveButton = MainFrame.createModernButton("Save New Patient", 
            new Color(34, 139, 34), new Color(50, 160, 50), 160, 40);
        saveButton.addActionListener(e -> savePatient());

        updateButton = MainFrame.createModernButton("Update Patient", 
            new Color(0, 102, 204), new Color(0, 120, 240), 160, 40);
        updateButton.addActionListener(e -> updatePatient());

        deleteButton = MainFrame.createModernButton("Delete Patient", 
            new Color(220, 20, 60), new Color(240, 40, 80), 160, 40);
        deleteButton.addActionListener(e -> deletePatient());

        clearButton = MainFrame.createModernButton("Clear Form", 
            new Color(128, 128, 128), new Color(150, 150, 150), 160, 40);
        clearButton.addActionListener(e -> clearForm());

        panel.add(saveButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);

        return panel;
    }

    private void savePatient() {
        String dobString = MainFrame.getDateString(dobField);
        String error = ValidationUtil.validatePatientForm(
            firstNameField.getText(), lastNameField.getText(), dobString,
            phoneField.getText(), emailField.getText(), addressField.getText());
        
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Patient patient = new Patient(0,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                dobString,
                phoneField.getText().trim(),
                emailField.getText().trim(),
                addressField.getText().trim());
            
            patientDAO.addPatient(patient);
            JOptionPane.showMessageDialog(this, "Patient saved", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            // tell everyone patients changed
            DataChangeManager.getInstance().notifyPatientsChanged();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePatient() {
        if (selectedPatientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dobString = MainFrame.getDateString(dobField);
        String error = ValidationUtil.validatePatientForm(
            firstNameField.getText(), lastNameField.getText(), dobString,
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
                    dobString,
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    addressField.getText().trim());
                
                patientDAO.updatePatient(patient);
                JOptionPane.showMessageDialog(this, "Patient updated", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();

                DataChangeManager.getInstance().notifyPatientsChanged();
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
                JOptionPane.showMessageDialog(this, "Patient deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();

                DataChangeManager.getInstance().notifyPatientsChanged();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedPatientId = -1;
        firstNameField.setText("");
        lastNameField.setText("");
        MainFrame.setDateFromString(dobField, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
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
            MainFrame.setDateFromString(dobField, (String) tableModel.getValueAt(row, 3));
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
                String summary = appointmentDAO.getStatusSummaryForPatient(patient.getId());
                tableModel.addRow(new Object[]{
                    patient.getId(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getDateOfBirth(),
                    patient.getPhoneNumber(),
                    patient.getEmail(),
                    patient.getAddress(),
                    summary
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
                String summary = appointmentDAO.getStatusSummaryForPatient(patient.getId());
                tableModel.addRow(new Object[]{
                    patient.getId(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getDateOfBirth(),
                    patient.getPhoneNumber(),
                    patient.getEmail(),
                    patient.getAddress(),
                    summary
                });
            }
            if (patients.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No patients found matching: " + searchTerm, "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching patients: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createDatePicker() {
        return MainFrame.createDatePickerPanel();
    }

    @Override
    public void onPatientsChanged() {

        refreshTable();
    }

    @Override
    public void onAppointmentsChanged() {

    }

    @Override
    public void onMedicalHistoryChanged() {

    }

}
