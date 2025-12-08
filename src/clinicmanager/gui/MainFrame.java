package clinicmanager.gui;

import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.dao.VisitDAO;
import clinicmanager.dao.MedicalConditionDAO;
import clinicmanager.dao.AllergyDAO;
import clinicmanager.dao.MedicationDAO;
import clinicmanager.dao.PrescriptionDAO;
import clinicmanager.database.DatabaseConnection;
import clinicmanager.models.Appointment;
import clinicmanager.models.Patient;
import clinicmanager.models.Visit;
import clinicmanager.util.ValidationUtil;
import clinicmanager.util.DatePickerPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private PatientDAO patientDAO;
    private AppointmentDAO appointmentDAO;
    private VisitDAO visitDAO;
    private MedicalConditionDAO medicalConditionDAO;
    private AllergyDAO allergyDAO;
    private MedicationDAO medicationDAO;
    private PrescriptionDAO prescriptionDAO;
    private Connection connection;

    public MainFrame() {
        try {
            connection = DatabaseConnection.getConnection();
            patientDAO = new PatientDAO(connection);
            appointmentDAO = new AppointmentDAO(connection);
            visitDAO = new VisitDAO(connection);
            medicalConditionDAO = new MedicalConditionDAO(connection);
            allergyDAO = new AllergyDAO(connection);
            medicationDAO = new MedicationDAO(connection);
            prescriptionDAO = new PrescriptionDAO(connection);
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
        tabbedPane.addTab("Dashboard", new DashboardPanel(patientDAO, appointmentDAO));
        tabbedPane.addTab("Patients", new PatientPanel(patientDAO));
        tabbedPane.addTab("Appointments", new AppointmentPanel(appointmentDAO, patientDAO));
        tabbedPane.addTab("Medical History", new MedicalHistoryPanel(patientDAO, medicalConditionDAO, allergyDAO, medicationDAO));
        tabbedPane.addTab("Reports", new ReportsPanel(patientDAO, appointmentDAO));

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

    // Helper method to create date picker with calendar dialog
    private JPanel createDatePicker() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(180, 25));
        
        JTextField dateField = new JTextField();
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);
        
        // Set default date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(sdf.format(new Date()));
        
        // Store reference to text field in panel for easy retrieval
        panel.putClientProperty("dateField", dateField);
        
        JButton calendarBtn = new JButton("📅");
        calendarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        calendarBtn.setPreferredSize(new Dimension(30, 25));
        calendarBtn.setFocusPainted(false);
        calendarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarBtn.addActionListener(e -> showDatePickerDialog(dateField));
        
        // Make text field clickable
        dateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDatePickerDialog(dateField);
            }
        });
        
        panel.add(dateField, BorderLayout.CENTER);
        panel.add(calendarBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    // Helper method to create date and time picker with dialogs
    private JPanel createDateTimePicker() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 25));
        
        JTextField dateTimeField = new JTextField();
        dateTimeField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateTimeField.setEditable(false);
        dateTimeField.setBackground(Color.WHITE);
        
        // Set default date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateTimeField.setText(sdf.format(new Date()));
        
        // Store reference to text field in panel for easy retrieval
        panel.putClientProperty("dateTimeField", dateTimeField);
        
        JButton pickerBtn = new JButton("📅");
        pickerBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pickerBtn.setPreferredSize(new Dimension(30, 25));
        pickerBtn.setFocusPainted(false);
        pickerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pickerBtn.addActionListener(e -> showDateTimePickerDialog(dateTimeField));
        
        // Make text field clickable
        dateTimeField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDateTimePickerDialog(dateTimeField);
            }
        });
        
        panel.add(dateTimeField, BorderLayout.CENTER);
        panel.add(pickerBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    // Show date picker dialog
    private void showDatePickerDialog(JTextField dateField) {
        Frame parentFrame = null;
        try {
            parentFrame = (Frame) SwingUtilities.getWindowAncestor(dateField);
        } catch (Exception e) {
            parentFrame = null;
        }
        
        JDialog dialog = new JDialog(parentFrame, "Select Date", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(dateField);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Parse current date
        java.util.Date selectedDate = new java.util.Date();
        String currentDate = dateField.getText();
        if (currentDate != null && !currentDate.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                selectedDate = sdf.parse(currentDate);
            } catch (Exception e) {
                // Use current date if parsing fails
            }
        }
        
        // Create date picker panel with modern design
        DatePickerPanel datePickerPanel = new DatePickerPanel(selectedDate, date -> {
            dateField.setText(date);
            dialog.dispose();
        });
        
        dialog.add(datePickerPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    // Show date and time picker dialog
    private void showDateTimePickerDialog(JTextField dateTimeField) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(dateTimeField), "Select Date & Time", true);
        dialog.setSize(350, 450);
        dialog.setLocationRelativeTo(dateTimeField);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Date picker panel
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder("Select Date"));
        
        JTextField dateField = new JTextField();
        dateField.setEditable(false);
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateField.setBackground(Color.WHITE);
        
        // Parse existing date/time if available
        String currentDateTime = dateTimeField.getText();
        Calendar cal = Calendar.getInstance();
        int defaultHour = cal.get(Calendar.HOUR_OF_DAY);
        int defaultMinute = cal.get(Calendar.MINUTE);
        String defaultDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        
        if (currentDateTime != null && !currentDateTime.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date dt = sdf.parse(currentDateTime.trim());
                cal.setTime(dt);
                defaultHour = cal.get(Calendar.HOUR_OF_DAY);
                defaultMinute = cal.get(Calendar.MINUTE);
                defaultDate = new SimpleDateFormat("yyyy-MM-dd").format(dt);
            } catch (Exception e) {
                // Use defaults if parsing fails
            }
        }
        
        // Initialize dateField with default date
        dateField.setText(defaultDate);
        
        JButton dateBtn = new JButton("📅");
        dateBtn.addActionListener(e -> {
            showDatePickerDialog(dateField);
        });
        
        dateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDatePickerDialog(dateField);
            }
        });
        
        JPanel dateInputPanel = new JPanel(new BorderLayout());
        dateInputPanel.add(dateField, BorderLayout.CENTER);
        dateInputPanel.add(dateBtn, BorderLayout.EAST);
        datePanel.add(dateInputPanel, BorderLayout.NORTH);
        
        // Time picker panel
        JPanel timePanel = new JPanel(new GridBagLayout());
        timePanel.setBorder(BorderFactory.createTitledBorder("Select Time"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        SpinnerNumberModel hourModel = new SpinnerNumberModel(defaultHour, 0, 23, 1);
        SpinnerNumberModel minuteModel = new SpinnerNumberModel(defaultMinute, 0, 59, 1);
        
        JSpinner hourSpinner = new JSpinner(hourModel);
        JSpinner minuteSpinner = new JSpinner(minuteModel);
        
        hourSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        minuteSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hourSpinner.setPreferredSize(new Dimension(60, 30));
        minuteSpinner.setPreferredSize(new Dimension(60, 30));
        
        timePanel.add(new JLabel("Hour:"), gbc);
        gbc.gridx = 1;
        timePanel.add(hourSpinner, gbc);
        gbc.gridx = 2;
        timePanel.add(new JLabel(":"), gbc);
        gbc.gridx = 3;
        timePanel.add(new JLabel("Minute:"), gbc);
        gbc.gridx = 4;
        timePanel.add(minuteSpinner, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okBtn = createModernButton("OK", new Color(34, 139, 34), new Color(50, 160, 50), 80, 30);
        JButton cancelBtn = createModernButton("Cancel", new Color(128, 128, 128), new Color(150, 150, 150), 80, 30);
        
        okBtn.addActionListener(e -> {
            String dateStr = dateField.getText();
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                int hour = (Integer) hourSpinner.getValue();
                int minute = (Integer) minuteSpinner.getValue();
                String timeStr = String.format("%02d:%02d", hour, minute);
                dateTimeField.setText(dateStr + " " + timeStr);
            }
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        
        mainPanel.add(datePanel, BorderLayout.NORTH);
        mainPanel.add(timePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    // Helper method to get date string from date picker panel
    private String getDateString(JPanel datePickerPanel) {
        // First try to get from client property
        Object field = datePickerPanel.getClientProperty("dateField");
        if (field instanceof JTextField) {
            return ((JTextField) field).getText();
        }
        
        // Fallback: search components
        Component[] components = datePickerPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                return ((JTextField) comp).getText();
            }
        }
        return "";
    }
    
    // Helper method to get date-time string from date-time picker panel
    private String getDateTimeString(JPanel dateTimePickerPanel) {
        // First try to get from client property
        Object field = dateTimePickerPanel.getClientProperty("dateTimeField");
        if (field instanceof JTextField) {
            return ((JTextField) field).getText();
        }
        
        // Fallback: search components
        Component[] components = dateTimePickerPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                return ((JTextField) comp).getText();
            }
        }
        return "";
    }
    
    // Helper method to set date picker from string
    private void setDateFromString(JPanel datePickerPanel, String dateString) {
        // First try to get from client property
        Object field = datePickerPanel.getClientProperty("dateField");
        if (field instanceof JTextField) {
            JTextField textField = (JTextField) field;
            if (dateString != null && !dateString.trim().isEmpty()) {
                textField.setText(dateString);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                textField.setText(sdf.format(new Date()));
            }
            return;
        }
        
        // Fallback: search components
        Component[] components = datePickerPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                if (dateString != null && !dateString.trim().isEmpty()) {
                    textField.setText(dateString);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    textField.setText(sdf.format(new Date()));
                }
                return;
            }
        }
    }
    
    // Helper method to set date-time picker from string
    private void setDateTimeFromString(JPanel dateTimePickerPanel, String dateTimeString) {
        // First try to get from client property
        Object field = dateTimePickerPanel.getClientProperty("dateTimeField");
        if (field instanceof JTextField) {
            JTextField textField = (JTextField) field;
            if (dateTimeString != null && !dateTimeString.trim().isEmpty()) {
                textField.setText(dateTimeString);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                textField.setText(sdf.format(new Date()));
            }
            return;
        }
        
        // Fallback: search components
        Component[] components = dateTimePickerPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                if (dateTimeString != null && !dateTimeString.trim().isEmpty()) {
                    textField.setText(dateTimeString);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    textField.setText(sdf.format(new Date()));
                }
                return;
            }
        }
    }

    // Helper method to create modern styled buttons
    private JButton createModernButton(String text, Color bgColor, Color hoverColor, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color currentBg = getModel().isPressed() ? bgColor.darker() : 
                                  getModel().isRollover() ? hoverColor : bgColor;
                
                // Draw rounded rectangle background
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Draw border
                g2.setColor(currentBg.darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }

    // Patient Panel
    class PatientPanel extends JPanel {
        private final PatientDAO patientDAO;
        private JTable patientTable;
        private DefaultTableModel tableModel;
        private JTextField firstNameField, lastNameField, phoneField, emailField, addressField;
        private JPanel dobField;
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
            panel.add(new JLabel("Date of Birth:"), gbc);
            dobField = createDatePicker();
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
            searchButton = createModernButton("Search", 
                new Color(0, 102, 204), new Color(0, 120, 240), 100, 30);
            searchButton.addActionListener(e -> searchPatients());
            searchPanel.add(searchButton);
            
            refreshButton = createModernButton("Refresh", 
                new Color(34, 139, 34), new Color(50, 160, 50), 100, 30);
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
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            panel.setBackground(new Color(245, 250, 255));

            saveButton = createModernButton("Save New Patient", 
                new Color(34, 139, 34), new Color(50, 160, 50), 160, 40);
            saveButton.addActionListener(e -> savePatient());

            updateButton = createModernButton("Update Patient", 
                new Color(0, 102, 204), new Color(0, 120, 240), 160, 40);
            updateButton.addActionListener(e -> updatePatient());

            deleteButton = createModernButton("Delete Patient", 
                new Color(220, 20, 60), new Color(240, 40, 80), 160, 40);
            deleteButton.addActionListener(e -> deletePatient());

            clearButton = createModernButton("Clear Form", 
                new Color(128, 128, 128), new Color(150, 150, 150), 160, 40);
            clearButton.addActionListener(e -> clearForm());

            panel.add(saveButton);
            panel.add(updateButton);
            panel.add(deleteButton);
            panel.add(clearButton);

            return panel;
        }

        private void savePatient() {
            String dobString = getDateString(dobField);
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

            String dobString = getDateString(dobField);
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
            setDateFromString(dobField, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
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
                setDateFromString(dobField, (String) tableModel.getValueAt(row, 3));
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
            dateField = createDateTimePicker();
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

            refreshButton = createModernButton("Refresh", 
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

            saveButton = createModernButton("Schedule Appointment", 
                new Color(34, 139, 34), new Color(50, 160, 50), 180, 40);
            saveButton.addActionListener(e -> saveAppointment());

            updateButton = createModernButton("Update Appointment", 
                new Color(0, 102, 204), new Color(0, 120, 240), 180, 40);
            updateButton.addActionListener(e -> updateAppointment());

            deleteButton = createModernButton("Cancel Appointment", 
                new Color(220, 20, 60), new Color(240, 40, 80), 180, 40);
            deleteButton.addActionListener(e -> deleteAppointment());

            clearButton = createModernButton("Clear Form", 
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

            String dateTimeString = getDateTimeString(dateField);
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

            String dateTimeString = getDateTimeString(dateField);
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
            setDateTimeFromString(dateField, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
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
                
                setDateTimeFromString(dateField, (String) tableModel.getValueAt(row, 3));
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
    }

    // Medical History Panel - Full Implementation with Input Forms
    class MedicalHistoryPanel extends JPanel {
        private final PatientDAO patientDAO;
        private final MedicalConditionDAO medicalConditionDAO;
        private final AllergyDAO allergyDAO;
        private final MedicationDAO medicationDAO;
        private JComboBox<PatientComboItem> patientCombo;
        private JTabbedPane historyTabs;
        private int selectedPatientId = -1;

        public MedicalHistoryPanel(PatientDAO patientDAO, MedicalConditionDAO medicalConditionDAO, 
                                  AllergyDAO allergyDAO, MedicationDAO medicationDAO) {
            this.patientDAO = patientDAO;
            this.medicalConditionDAO = medicalConditionDAO;
            this.allergyDAO = allergyDAO;
            this.medicationDAO = medicationDAO;
            
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(new Color(245, 250, 255));

            // Patient selector
            JPanel patientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            patientPanel.add(new JLabel("Select Patient:"));
            patientCombo = new JComboBox<>();
            patientCombo.setPreferredSize(new Dimension(250, 25));
            patientCombo.addActionListener(e -> {
                PatientComboItem selected = (PatientComboItem) patientCombo.getSelectedItem();
                if (selected != null) {
                    selectedPatientId = selected.getId();
                    loadMedicalHistory();
                }
            });
            patientPanel.add(patientCombo);
            
            JButton refreshPatientsBtn = new JButton("↻");
            refreshPatientsBtn.setPreferredSize(new Dimension(35, 25));
            refreshPatientsBtn.setToolTipText("Refresh patient list");
            refreshPatientsBtn.addActionListener(e -> loadPatients());
            patientPanel.add(refreshPatientsBtn);
            
            // History tabs with input forms
            historyTabs = new JTabbedPane();
            historyTabs.addTab("Medical Conditions", new JPanel());
            historyTabs.addTab("Allergies", new JPanel());
            historyTabs.addTab("Medications", new JPanel());
            
            add(patientPanel, BorderLayout.NORTH);
            add(historyTabs, BorderLayout.CENTER);
            
            loadPatients();
        }

        private void loadPatients() {
            try {
                patientCombo.removeAllItems();
                patientCombo.addItem(new PatientComboItem(-1, "Select a patient..."));
                for (Patient p : patientDAO.getAllPatients()) {
                    patientCombo.addItem(new PatientComboItem(p.getId(), p.getFirstName() + " " + p.getLastName()));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage());
            }
        }

        private void loadMedicalHistory() {
            if (selectedPatientId == -1) return;
            try {
                historyTabs.setComponentAt(0, createConditionsPanel());
                historyTabs.setComponentAt(1, createAllergiesPanel());
                historyTabs.setComponentAt(2, createMedicationsPanel());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading medical history: " + e.getMessage());
            }
        }

        private JPanel createConditionsPanel() throws SQLException {
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Input form
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Add Medical Condition"));
            formPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField conditionField = new JTextField(20);
            JTextField dateField = new JTextField(20);
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"active", "resolved", "inactive"});
            JTextArea notesArea = new JTextArea(3, 20);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);

            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Condition Name *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(conditionField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Diagnosis Date (yyyy-MM-dd):"), gbc);
            gbc.gridx = 1;
            formPanel.add(dateField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            formPanel.add(statusCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            formPanel.add(new JScrollPane(notesArea), gbc);

            JButton addBtn = createModernButton("Add Condition", new Color(34, 139, 34), new Color(50, 160, 50), 120, 30);
            gbc.gridx = 0; gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(addBtn, gbc);

            // Display panel
            JPanel displayPanel = new JPanel(new BorderLayout());
            displayPanel.setBorder(BorderFactory.createTitledBorder("Medical Conditions History"));
            JTextArea displayArea = new JTextArea(12, 50);
            displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            displayArea.setEditable(false);
            displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

            addBtn.addActionListener(e -> {
                if (conditionField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a condition name");
                    return;
                }
                try {
                    clinicmanager.models.MedicalCondition cond = new clinicmanager.models.MedicalCondition(
                        0, selectedPatientId, conditionField.getText(), dateField.getText(),
                        (String) statusCombo.getSelectedItem(), notesArea.getText()
                    );
                    medicalConditionDAO.addCondition(cond);
                    JOptionPane.showMessageDialog(this, "Condition added successfully!");
                    conditionField.setText("");
                    dateField.setText("");
                    notesArea.setText("");
                    loadMedicalHistory();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });

            // Refresh display
            try {
                StringBuilder text = new StringBuilder();
                for (clinicmanager.models.MedicalCondition cond : medicalConditionDAO.getConditionsByPatientId(selectedPatientId)) {
                    text.append("• ").append(cond.getConditionName()).append(" (").append(cond.getStatus()).append(")\n");
                    text.append("  Diagnosed: ").append(cond.getDiagnosisDate()).append("\n");
                    text.append("  Notes: ").append(cond.getNotes()).append("\n\n");
                }
                displayArea.setText(text.toString());
            } catch (SQLException ex) {
                displayArea.setText("Error loading conditions");
            }

            mainPanel.add(formPanel, BorderLayout.NORTH);
            mainPanel.add(displayPanel, BorderLayout.CENTER);
            return mainPanel;
        }

        private JPanel createAllergiesPanel() throws SQLException {
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Input form
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Add Allergy"));
            formPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField allergenField = new JTextField(20);
            JTextField reactionField = new JTextField(20);
            JComboBox<String> severityCombo = new JComboBox<>(new String[]{"mild", "moderate", "severe"});
            JTextArea notesArea = new JTextArea(3, 20);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);

            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Allergen *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(allergenField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Reaction:"), gbc);
            gbc.gridx = 1;
            formPanel.add(reactionField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Severity:"), gbc);
            gbc.gridx = 1;
            formPanel.add(severityCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            formPanel.add(new JScrollPane(notesArea), gbc);

            JButton addBtn = createModernButton("Add Allergy", new Color(34, 139, 34), new Color(50, 160, 50), 120, 30);
            gbc.gridx = 0; gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(addBtn, gbc);

            // Display panel
            JPanel displayPanel = new JPanel(new BorderLayout());
            displayPanel.setBorder(BorderFactory.createTitledBorder("Patient Allergies"));
            JTextArea displayArea = new JTextArea(12, 50);
            displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            displayArea.setEditable(false);
            displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

            addBtn.addActionListener(e -> {
                if (allergenField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter an allergen");
                    return;
                }
                try {
                    clinicmanager.models.Allergy allergy = new clinicmanager.models.Allergy(
                        0, selectedPatientId, allergenField.getText(), reactionField.getText(),
                        (String) severityCombo.getSelectedItem(), notesArea.getText()
                    );
                    allergyDAO.addAllergy(allergy);
                    JOptionPane.showMessageDialog(this, "Allergy added successfully!");
                    allergenField.setText("");
                    reactionField.setText("");
                    notesArea.setText("");
                    loadMedicalHistory();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });

            // Refresh display
            try {
                StringBuilder text = new StringBuilder();
                for (clinicmanager.models.Allergy allergy : allergyDAO.getAllergiesByPatientId(selectedPatientId)) {
                    text.append("⚠ ").append(allergy.getAllergen()).append(" [").append(allergy.getSeverity()).append("]\n");
                    text.append("  Reaction: ").append(allergy.getReaction()).append("\n");
                    text.append("  Notes: ").append(allergy.getNotes()).append("\n\n");
                }
                displayArea.setText(text.toString());
            } catch (SQLException ex) {
                displayArea.setText("Error loading allergies");
            }

            mainPanel.add(formPanel, BorderLayout.NORTH);
            mainPanel.add(displayPanel, BorderLayout.CENTER);
            return mainPanel;
        }

        private JPanel createMedicationsPanel() throws SQLException {
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Input form
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Add Medication"));
            formPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField medNameField = new JTextField(20);
            JTextField dosageField = new JTextField(20);
            JTextField frequencyField = new JTextField(20);
            JTextField startDateField = new JTextField(20);
            JTextField endDateField = new JTextField(20);
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"active", "discontinued", "paused"});
            JTextArea notesArea = new JTextArea(3, 20);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);

            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Medication Name *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(medNameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Dosage:"), gbc);
            gbc.gridx = 1;
            formPanel.add(dosageField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Frequency:"), gbc);
            gbc.gridx = 1;
            formPanel.add(frequencyField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Start Date (yyyy-MM-dd):"), gbc);
            gbc.gridx = 1;
            formPanel.add(startDateField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("End Date (yyyy-MM-dd):"), gbc);
            gbc.gridx = 1;
            formPanel.add(endDateField, gbc);

            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            formPanel.add(statusCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 6;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            formPanel.add(new JScrollPane(notesArea), gbc);

            JButton addBtn = createModernButton("Add Medication", new Color(34, 139, 34), new Color(50, 160, 50), 120, 30);
            gbc.gridx = 0; gbc.gridy = 7;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(addBtn, gbc);

            // Display panel
            JPanel displayPanel = new JPanel(new BorderLayout());
            displayPanel.setBorder(BorderFactory.createTitledBorder("Current Medications"));
            JTextArea displayArea = new JTextArea(12, 50);
            displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            displayArea.setEditable(false);
            displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

            addBtn.addActionListener(e -> {
                if (medNameField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a medication name");
                    return;
                }
                
                // Validate date format if provided
                String startDate = startDateField.getText().trim();
                String endDate = endDateField.getText().trim();
                
                if (!startDate.isEmpty() && !isValidDateFormat(startDate)) {
                    JOptionPane.showMessageDialog(this, "Invalid start date format. Use yyyy-MM-dd (e.g., 2024-01-15) or leave empty");
                    return;
                }
                
                if (!endDate.isEmpty() && !isValidDateFormat(endDate)) {
                    JOptionPane.showMessageDialog(this, "Invalid end date format. Use yyyy-MM-dd (e.g., 2024-01-15) or leave empty");
                    return;
                }
                
                try {
                    clinicmanager.models.Medication med = new clinicmanager.models.Medication(
                        0, selectedPatientId, medNameField.getText(), dosageField.getText(),
                        frequencyField.getText(), startDate, endDate,
                        (String) statusCombo.getSelectedItem(), notesArea.getText()
                    );
                    medicationDAO.addMedication(med);
                    JOptionPane.showMessageDialog(this, "Medication added successfully!");
                    medNameField.setText("");
                    dosageField.setText("");
                    frequencyField.setText("");
                    startDateField.setText("");
                    endDateField.setText("");
                    notesArea.setText("");
                    loadMedicalHistory();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });

            // Refresh display
            try {
                StringBuilder text = new StringBuilder();
                for (clinicmanager.models.Medication med : medicationDAO.getActiveMedicationsByPatientId(selectedPatientId)) {
                    text.append("💊 ").append(med.getMedicationName()).append(" (").append(med.getStatus()).append(")\n");
                    text.append("  Dosage: ").append(med.getDosage()).append(" - Frequency: ").append(med.getFrequency()).append("\n");
                    text.append("  Started: ").append(med.getStartDate()).append("\n");
                    if (med.getEndDate() != null && !med.getEndDate().isEmpty()) {
                        text.append("  Ended: ").append(med.getEndDate()).append("\n");
                    }
                    text.append("  Notes: ").append(med.getNotes()).append("\n\n");
                }
                displayArea.setText(text.toString());
            } catch (SQLException ex) {
                displayArea.setText("Error loading medications");
            }

            mainPanel.add(formPanel, BorderLayout.NORTH);
            mainPanel.add(displayPanel, BorderLayout.CENTER);
            return mainPanel;
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
        
        private boolean isValidDateFormat(String date) {
            if (date == null || date.trim().isEmpty()) {
                return true; // Empty is valid (will be set to NULL)
            }
            try {
                java.sql.Date.valueOf(date.trim());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    // Dashboard Panel - Statistics and Overview
    // Dashboard Panel - Simple Statistics
    class DashboardPanel extends JPanel {
        private final PatientDAO patientDAO;
        private final AppointmentDAO appointmentDAO;
        private JLabel totalPatientsLabel, totalApptsLabel, completedLabel, missedLabel;
        private JLabel scheduledLabel, cancelledLabel;

        public DashboardPanel(PatientDAO patientDAO, AppointmentDAO appointmentDAO) {
            this.patientDAO = patientDAO;
            this.appointmentDAO = appointmentDAO;
            
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            setBackground(new Color(245, 250, 255));
            
            // Create simple text display
            JTextArea statsArea = new JTextArea();
            statsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            statsArea.setEditable(false);
            statsArea.setLineWrap(true);
            statsArea.setName("statsArea");
            
            // Create labels for updating
            totalPatientsLabel = new JLabel("0");
            totalApptsLabel = new JLabel("0");
            completedLabel = new JLabel("0");
            missedLabel = new JLabel("0");
            scheduledLabel = new JLabel("0");
            cancelledLabel = new JLabel("0");
            
            // Create stats panel with grid
            JPanel statsPanel = new JPanel(new GridLayout(3, 2, 15, 15));
            statsPanel.setBackground(Color.WHITE);
            statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
            
            statsPanel.add(createStatRow("Total Patients:", totalPatientsLabel));
            statsPanel.add(createStatRow("Total Appointments:", totalApptsLabel));
            statsPanel.add(createStatRow("Completed:", completedLabel));
            statsPanel.add(createStatRow("Missed:", missedLabel));
            statsPanel.add(createStatRow("Scheduled:", scheduledLabel));
            statsPanel.add(createStatRow("Cancelled:", cancelledLabel));
            
            // Add refresh button
            JButton refreshBtn = createModernButton("Refresh Stats", 
                new Color(52, 152, 219), new Color(41, 128, 185), 120, 35);
            refreshBtn.addActionListener(e -> refreshStats());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(new Color(245, 250, 255));
            buttonPanel.add(refreshBtn);
            
            add(statsPanel, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);
            
            refreshStats();
        }

        private JPanel createStatRow(String label, JLabel valueLabel) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            JLabel labelComp = new JLabel(label);
            labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            valueLabel.setForeground(new Color(52, 152, 219));
            panel.add(labelComp, BorderLayout.WEST);
            panel.add(valueLabel, BorderLayout.EAST);
            return panel;
        }

        private void refreshStats() {
            try {
                int totalPatients = patientDAO.getAllPatients().size();
                java.util.List<Appointment> appointments = appointmentDAO.getAllAppointments();
                int totalAppts = appointments.size();
                int completed = 0, missed = 0, scheduled = 0, cancelled = 0;
                
                for (Appointment apt : appointments) {
                    String status = apt.getStatus();
                    if (status != null) {
                        if (status.equals("completed")) completed++;
                        else if (status.equals("missed")) missed++;
                        else if (status.equals("scheduled")) scheduled++;
                        else if (status.equals("cancelled")) cancelled++;
                    }
                }
                
                totalPatientsLabel.setText(String.valueOf(totalPatients));
                totalApptsLabel.setText(String.valueOf(totalAppts));
                completedLabel.setText(String.valueOf(completed));
                missedLabel.setText(String.valueOf(missed));
                scheduledLabel.setText(String.valueOf(scheduled));
                cancelledLabel.setText(String.valueOf(cancelled));
                
                JOptionPane.showMessageDialog(this, "Stats updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Reports Panel - Export Functionality
    class ReportsPanel extends JPanel {
        private final PatientDAO patientDAO;
        private final AppointmentDAO appointmentDAO;

        public ReportsPanel(PatientDAO patientDAO, AppointmentDAO appointmentDAO) {
            this.patientDAO = patientDAO;
            this.appointmentDAO = appointmentDAO;
            
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            setBackground(new Color(245, 250, 255));
            
            JPanel buttonPanel = createButtonPanel();
            JPanel infoPanel = createInfoPanel();
            
            add(buttonPanel, BorderLayout.NORTH);
            add(infoPanel, BorderLayout.CENTER);
        }

        private JPanel createButtonPanel() {
            JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Export Reports", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
            panel.setBackground(Color.WHITE);
            
            // Patient List Report
            JButton patientListBtn = createModernButton("Export Patient List (CSV)", 
                new Color(46, 204, 113), new Color(39, 174, 96), 200, 50);
            patientListBtn.addActionListener(e -> exportPatientList());
            panel.add(patientListBtn);
            
            // Appointment Report
            JButton appointmentBtn = createModernButton("Export Appointments (CSV)", 
                new Color(52, 152, 219), new Color(41, 128, 185), 200, 50);
            appointmentBtn.addActionListener(e -> exportAppointments());
            panel.add(appointmentBtn);
            
            // Statistics Report
            JButton statsBtn = createModernButton("Export Statistics (CSV)", 
                new Color(155, 89, 182), new Color(142, 68, 173), 200, 50);
            statsBtn.addActionListener(e -> exportStatistics());
            panel.add(statsBtn);
            
            // Open Reports Folder
            JButton openFolderBtn = createModernButton("Open Reports Folder", 
                new Color(230, 126, 34), new Color(209, 109, 25), 200, 50);
            openFolderBtn.addActionListener(e -> openReportsFolder());
            panel.add(openFolderBtn);
            
            return panel;
        }

        private JPanel createInfoPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Report Information", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
            panel.setBackground(Color.WHITE);
            
            JTextArea infoArea = new JTextArea();
            infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            infoArea.setEditable(false);
            infoArea.setLineWrap(true);
            infoArea.setWrapStyleWord(true);
            infoArea.setText(
                "REPORT GENERATION\n\n" +
                "Available Reports:\n\n" +
                "1. PATIENT LIST REPORT\n" +
                "   • Exports all patients with their contact information\n" +
                "   • Includes: ID, Name, Phone, Email, Address, DOB\n" +
                "   • Format: CSV (can open in Excel)\n\n" +
                "2. APPOINTMENT REPORT\n" +
                "   • Exports all appointments with status\n" +
                "   • Includes: ID, Patient Name, Date, Reason, Status\n" +
                "   • Format: CSV (can open in Excel)\n\n" +
                "3. STATISTICS REPORT\n" +
                "   • Generates summary statistics\n" +
                "   • Shows total patients, appointments by status\n" +
                "   • Format: CSV\n\n" +
                "All reports are saved to: [Project Root]/reports/ folder\n" +
                "Use 'Open Reports Folder' to access exported files."
            );
            
            panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
            return panel;
        }

        private void exportPatientList() {
            try {
                java.util.List<Patient> patients = patientDAO.getAllPatients();
                
                // Create reports directory
                java.io.File reportsDir = new java.io.File("reports");
                if (!reportsDir.exists()) {
                    reportsDir.mkdir();
                }
                
                // Create CSV file
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new java.util.Date());
                java.io.File file = new java.io.File("reports/PatientList_" + timestamp + ".csv");
                java.io.FileWriter writer = new java.io.FileWriter(file);
                
                // Write header
                writer.write("ID,First Name,Last Name,Phone,Email,Address,Date of Birth\n");
                
                // Write data
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
                java.util.List<Appointment> appointments = appointmentDAO.getAllAppointments();
                
                // Create reports directory
                java.io.File reportsDir = new java.io.File("reports");
                if (!reportsDir.exists()) {
                    reportsDir.mkdir();
                }
                
                // Create CSV file
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new java.util.Date());
                java.io.File file = new java.io.File("reports/Appointments_" + timestamp + ".csv");
                java.io.FileWriter writer = new java.io.FileWriter(file);
                
                // Write header
                writer.write("ID,Patient ID,Patient Name,Date & Time,Reason,Status\n");
                
                // Write data
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
                        // Skip if patient not found
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
                java.util.List<Patient> patients = patientDAO.getAllPatients();
                java.util.List<Appointment> appointments = appointmentDAO.getAllAppointments();
                
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
                
                // Create reports directory
                java.io.File reportsDir = new java.io.File("reports");
                if (!reportsDir.exists()) {
                    reportsDir.mkdir();
                }
                
                // Create CSV file
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new java.util.Date());
                java.io.File file = new java.io.File("reports/Statistics_" + timestamp + ".csv");
                java.io.FileWriter writer = new java.io.FileWriter(file);
                
                // Write statistics
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
    }
}
