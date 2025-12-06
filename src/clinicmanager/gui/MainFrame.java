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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(dateField), "Select Date", true);
        dialog.setSize(300, 350);
        dialog.setLocationRelativeTo(dateField);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Calendar panel
        JPanel calendarPanel = new JPanel(new GridLayout(7, 7));
        calendarPanel.setBorder(BorderFactory.createTitledBorder("Select Date"));
        
        Calendar cal = Calendar.getInstance();
        String currentDate = dateField.getText();
        if (currentDate != null && !currentDate.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                cal.setTime(sdf.parse(currentDate));
            } catch (Exception e) {
                // Use current date if parsing fails
            }
        }
        
        final Calendar selectedCal = (Calendar) cal.clone();
        
        // Month/Year selector
        JPanel monthYearPanel = new JPanel(new FlowLayout());
        JButton prevMonth = new JButton("◀");
        JLabel monthYearLabel = new JLabel();
        JButton nextMonth = new JButton("▶");
        
        updateMonthYearLabel(monthYearLabel, selectedCal);
        
        prevMonth.addActionListener(e -> {
            selectedCal.add(Calendar.MONTH, -1);
            updateMonthYearLabel(monthYearLabel, selectedCal);
            updateCalendar(calendarPanel, selectedCal, dialog, dateField);
        });
        
        nextMonth.addActionListener(e -> {
            selectedCal.add(Calendar.MONTH, 1);
            updateMonthYearLabel(monthYearLabel, selectedCal);
            updateCalendar(calendarPanel, selectedCal, dialog, dateField);
        });
        
        monthYearPanel.add(prevMonth);
        monthYearPanel.add(monthYearLabel);
        monthYearPanel.add(nextMonth);
        
        // Day labels
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            calendarPanel.add(label);
        }
        
        updateCalendar(calendarPanel, selectedCal, dialog, dateField);
        
        panel.add(monthYearPanel, BorderLayout.NORTH);
        panel.add(calendarPanel, BorderLayout.CENTER);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void updateMonthYearLabel(JLabel label, Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        label.setText(sdf.format(cal.getTime()));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
    
    private void updateCalendar(JPanel calendarPanel, Calendar cal, JDialog dialog, JTextField dateField) {
        // Remove existing day buttons (keep day labels)
        Component[] components = calendarPanel.getComponents();
        for (int i = 7; i < components.length; i++) {
            calendarPanel.remove(components[i]);
        }
        
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Add empty cells for days before month starts
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        // Add day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            dayBtn.setPreferredSize(new Dimension(35, 30));
            
            tempCal.set(Calendar.DAY_OF_MONTH, day);
            final Calendar selectedDate = (Calendar) tempCal.clone();
            
            dayBtn.addActionListener(e -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                dateField.setText(sdf.format(selectedDate.getTime()));
                dialog.dispose();
            });
            
            calendarPanel.add(dayBtn);
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
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
        
        dateField.setText(defaultDate);
        
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
            setDateTimeFromString(dateField, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
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
                
                setDateTimeFromString(dateField, (String) tableModel.getValueAt(row, 3));
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
        private JPanel dateField;
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
            panel.add(new JLabel("Visit Date *:"), gbc);
            dateField = createDatePicker();
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

            refreshButton = createModernButton("Refresh", 
                new Color(34, 139, 34), new Color(50, 160, 50), 100, 30);
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
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            panel.setBackground(new Color(245, 250, 255));

            saveButton = createModernButton("Record Visit", 
                new Color(34, 139, 34), new Color(50, 160, 50), 160, 40);
            saveButton.addActionListener(e -> saveVisit());

            updateButton = createModernButton("Update Visit", 
                new Color(0, 102, 204), new Color(0, 120, 240), 160, 40);
            updateButton.addActionListener(e -> updateVisit());

            deleteButton = createModernButton("Delete Visit", 
                new Color(220, 20, 60), new Color(240, 40, 80), 160, 40);
            deleteButton.addActionListener(e -> deleteVisit());

            clearButton = createModernButton("Clear Form", 
                new Color(128, 128, 128), new Color(150, 150, 150), 160, 40);
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

            String dateString = getDateString(dateField);
            if (dateString == null || dateString.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Visit date is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Visit visit = new Visit(0,
                    selected.getId(),
                    dateString,
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

            String dateString = getDateString(dateField);
            if (dateString == null || dateString.trim().isEmpty()) {
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
                        dateString,
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
            setDateFromString(dateField, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
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
                
                setDateFromString(dateField, (String) tableModel.getValueAt(row, 3));
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
