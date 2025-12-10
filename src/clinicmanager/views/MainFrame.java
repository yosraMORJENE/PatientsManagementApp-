package clinicmanager.views;

import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.PatientDAO;
import clinicmanager.dao.MedicalConditionDAO;
import clinicmanager.dao.AllergyDAO;
import clinicmanager.dao.MedicationDAO;
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
    private MedicalConditionDAO medicalConditionDAO;
    private AllergyDAO allergyDAO;
    private MedicationDAO medicationDAO;
    private Connection connection;

    public MainFrame() {
        try {
            connection = DatabaseConnection.getConnection();
            patientDAO = new PatientDAO(connection);
            appointmentDAO = new AppointmentDAO(connection);
            medicalConditionDAO = new MedicalConditionDAO(connection);
            allergyDAO = new AllergyDAO(connection);
            medicationDAO = new MedicationDAO(connection);
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
        
        // trying to make it look modern
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |IllegalAccessException | UnsupportedLookAndFeelException e) {
        }

        // tabbed pane stuff
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(new Color(240, 248, 255));
        
        // adding all tabs
        tabbedPane.addTab("Dashboard", new DashboardPanel(patientDAO, appointmentDAO));
        tabbedPane.addTab("Patients", new PatientPanel(patientDAO, appointmentDAO));
        tabbedPane.addTab("Appointments", new AppointmentPanel(appointmentDAO, patientDAO));
        tabbedPane.addTab("Medical History", new MedicalHistoryPanel(patientDAO, medicalConditionDAO, allergyDAO, medicationDAO));
        tabbedPane.addTab("Reports", new ReportsPanel(patientDAO, appointmentDAO));

        // putting it in the frame
        add(tabbedPane, BorderLayout.CENTER);
        
        // bottom status bar thing
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

    // makes a date picker
    public static JPanel createDatePickerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(180, 25));
        
        JTextField dateField = new JTextField();
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);
        
        // setting default date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(sdf.format(new Date()));
        
        // storing the text field so i can get it later
        panel.putClientProperty("dateField", dateField);
        
        JButton calendarBtn = new JButton("📅");
        calendarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        calendarBtn.setPreferredSize(new Dimension(30, 25));
        calendarBtn.setFocusPainted(false);
        calendarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarBtn.addActionListener(e -> showDatePickerDialog(dateField));
        
        // make the textfield clickable too
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
    
    // makes date+time picker
    public static JPanel createDateTimePickerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 25));
        
        JTextField dateTimeField = new JTextField();
        dateTimeField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateTimeField.setEditable(false);
        dateTimeField.setBackground(Color.WHITE);
        
        // default date/time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateTimeField.setText(sdf.format(new Date()));
        
        // saving reference
        panel.putClientProperty("dateTimeField", dateTimeField);
        
        JButton pickerBtn = new JButton("📅");
        pickerBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pickerBtn.setPreferredSize(new Dimension(30, 25));
        pickerBtn.setFocusPainted(false);
        pickerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pickerBtn.addActionListener(e -> showDateTimePickerDialog(dateTimeField));
        
        // make clickable
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
    
    // shows the date picker popup
    private static void showDatePickerDialog(JTextField dateField) {
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
        
        // trying to parse the date
        java.util.Date selectedDate = new java.util.Date();
        String currentDate = dateField.getText();
        if (currentDate != null && !currentDate.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                selectedDate = sdf.parse(currentDate);
            } catch (Exception e) {
                // whatever just use todays date
            }
        }
        
        // date picker panel
        DatePickerPanel datePickerPanel = new DatePickerPanel(selectedDate, date -> {
            dateField.setText(date);
            dialog.dispose();
        });
        
        dialog.add(datePickerPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    // shows date+time picker
    private static void showDateTimePickerDialog(JTextField dateTimeField) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(dateTimeField), "Select Date & Time", true);
        dialog.setSize(350, 450);
        dialog.setLocationRelativeTo(dateTimeField);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // date part
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
    public static String getDateString(JPanel datePickerPanel) {
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
    public static String getDateTimeString(JPanel dateTimePickerPanel) {
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
    public static void setDateFromString(JPanel datePickerPanel, String dateString) {
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
    public static void setDateTimeFromString(JPanel dateTimePickerPanel, String dateTimeString) {
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
    public static JButton createModernButton(String text, Color bgColor, Color hoverColor, int width, int height) {
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
}
