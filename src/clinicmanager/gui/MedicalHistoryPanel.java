package clinicmanager.gui;

import clinicmanager.dao.*;
import clinicmanager.models.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;

public class MedicalHistoryPanel extends JPanel implements DataChangeListener {
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
        
        JButton refreshPatientsBtn = new JButton("Refresh");
        refreshPatientsBtn.setPreferredSize(new Dimension(80, 25));
        refreshPatientsBtn.addActionListener(e -> loadPatients());
        patientPanel.add(refreshPatientsBtn);
        
        historyTabs = new JTabbedPane();
        historyTabs.addTab("Medical Conditions", new JPanel());
        historyTabs.addTab("Allergies", new JPanel());
        historyTabs.addTab("Medications", new JPanel());
        
        add(patientPanel, BorderLayout.NORTH);
        add(historyTabs, BorderLayout.CENTER);
        
        DataChangeManager.getInstance().addListener(this);
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

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Medical Condition"));
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Condition Name:"));
        JTextField nameField = new JTextField(20);
        row1.add(nameField);
        formPanel.add(row1);
        
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.add(new JLabel("Diagnosis Date:"));
        JPanel diagnosisDatePanel = MainFrame.createDatePickerPanel();
        row2.add(diagnosisDatePanel);
        row2.add(new JLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Resolved", "Pending"});
        row2.add(statusCombo);
        formPanel.add(row2);
        
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row3.add(new JLabel("Notes:"));
        JTextArea notesArea = new JTextArea(2, 30);
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        row3.add(new JScrollPane(notesArea));
        formPanel.add(row3);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addBtn = new JButton("Add Condition");
        JButton clearBtn = new JButton("Clear");
        btnPanel.add(addBtn);
        btnPanel.add(clearBtn);
        formPanel.add(btnPanel);
        
        addBtn.addActionListener(e -> {
            try {
                String condName = nameField.getText().trim();
                if (condName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter condition name");
                    return;
                }
                
                String diagDate = MainFrame.getDateString(diagnosisDatePanel);
                String status = (String) statusCombo.getSelectedItem();
                String notes = notesArea.getText().trim();
                
                MedicalCondition condition = new MedicalCondition();
                condition.setPatientId(selectedPatientId);
                condition.setConditionName(condName);
                condition.setDiagnosisDate(diagDate);
                condition.setStatus(status);
                condition.setNotes(notes);
                
                medicalConditionDAO.addMedicalCondition(condition);
                JOptionPane.showMessageDialog(this, "Condition added successfully!");
                nameField.setText("");
                notesArea.setText("");
                statusCombo.setSelectedIndex(0);
                loadMedicalHistory();
                DataChangeManager.getInstance().notifyMedicalHistoryChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding condition: " + ex.getMessage());
            }
        });
        
        clearBtn.addActionListener(e -> {
            nameField.setText("");
            notesArea.setText("");
            statusCombo.setSelectedIndex(0);
        });

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Medical Conditions History"));
        JTextArea displayArea = new JTextArea(12, 50);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        displayArea.setEditable(false);
        displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        try {
            StringBuilder text = new StringBuilder();
            for (MedicalCondition cond : medicalConditionDAO.getMedicalConditionsByPatientId(selectedPatientId)) {
                text.append("* ").append(cond.getConditionName()).append(" [").append(cond.getStatus()).append("]\n");
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

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Allergy"));
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Allergen:"));
        JTextField allergenField = new JTextField(20);
        row1.add(allergenField);
        row1.add(new JLabel("Severity:"));
        JComboBox<String> severityCombo = new JComboBox<>(new String[]{"Mild", "Moderate", "Severe"});
        row1.add(severityCombo);
        formPanel.add(row1);
        
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.add(new JLabel("Reaction:"));
        JComboBox<String> reactionCombo = new JComboBox<>(new String[]{"Rash", "Swelling", "Difficulty Breathing", "Anaphylaxis", "Other"});
        row2.add(reactionCombo);
        formPanel.add(row2);
        
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row3.add(new JLabel("Notes:"));
        JTextArea notesArea = new JTextArea(2, 30);
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        row3.add(new JScrollPane(notesArea));
        formPanel.add(row3);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addBtn = new JButton("Add Allergy");
        JButton clearBtn = new JButton("Clear");
        btnPanel.add(addBtn);
        btnPanel.add(clearBtn);
        formPanel.add(btnPanel);
        
        addBtn.addActionListener(e -> {
            try {
                String allergen = allergenField.getText().trim();
                if (allergen.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter allergen name");
                    return;
                }
                
                String severity = (String) severityCombo.getSelectedItem();
                String reaction = (String) reactionCombo.getSelectedItem();
                String notes = notesArea.getText().trim();
                
                Allergy allergy = new Allergy();
                allergy.setPatientId(selectedPatientId);
                allergy.setAllergen(allergen);
                allergy.setSeverity(severity);
                allergy.setReaction(reaction);
                allergy.setNotes(notes);
                
                allergyDAO.addAllergy(allergy);
                JOptionPane.showMessageDialog(this, "Allergy added successfully!");
                allergenField.setText("");
                notesArea.setText("");
                severityCombo.setSelectedIndex(0);
                reactionCombo.setSelectedIndex(0);
                loadMedicalHistory();
                DataChangeManager.getInstance().notifyMedicalHistoryChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding allergy: " + ex.getMessage());
            }
        });
        
        clearBtn.addActionListener(e -> {
            allergenField.setText("");
            notesArea.setText("");
            severityCombo.setSelectedIndex(0);
            reactionCombo.setSelectedIndex(0);
        });

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Patient Allergies"));
        JTextArea displayArea = new JTextArea(12, 50);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        displayArea.setEditable(false);
        displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        try {
            StringBuilder text = new StringBuilder();
            for (Allergy allergy : allergyDAO.getAllergiesByPatientId(selectedPatientId)) {
                text.append("* ").append(allergy.getAllergen()).append(" [").append(allergy.getSeverity()).append("]\n");
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

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Medication"));
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Medication Name:"));
        JTextField medNameField = new JTextField(20);
        row1.add(medNameField);
        row1.add(new JLabel("Dosage:"));
        JTextField dosageField = new JTextField(12);
        row1.add(dosageField);
        formPanel.add(row1);
        
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.add(new JLabel("Frequency:"));
        JTextField frequencyField = new JTextField(12);
        row2.add(frequencyField);
        row2.add(new JLabel("Start Date:"));
        JPanel startDatePanel = MainFrame.createDatePickerPanel();
        row2.add(startDatePanel);
        formPanel.add(row2);
        
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row3.add(new JLabel("End Date:"));
        JPanel endDatePanel = MainFrame.createDatePickerPanel();
        row3.add(endDatePanel);
        row3.add(new JLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Completed"});
        row3.add(statusCombo);
        formPanel.add(row3);
        
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row4.add(new JLabel("Notes:"));
        JTextArea notesArea = new JTextArea(2, 30);
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        row4.add(new JScrollPane(notesArea));
        formPanel.add(row4);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addBtn = new JButton("Add Medication");
        JButton clearBtn = new JButton("Clear");
        btnPanel.add(addBtn);
        btnPanel.add(clearBtn);
        formPanel.add(btnPanel);
        
        addBtn.addActionListener(e -> {
            try {
                String medName = medNameField.getText().trim();
                if (medName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter medication name");
                    return;
                }
                
                String dosage = dosageField.getText().trim();
                String frequency = frequencyField.getText().trim();
                String startDate = MainFrame.getDateString(startDatePanel);
                String endDate = MainFrame.getDateString(endDatePanel);
                String status = (String) statusCombo.getSelectedItem();
                String notes = notesArea.getText().trim();
                
                Medication medication = new Medication();
                medication.setPatientId(selectedPatientId);
                medication.setMedicationName(medName);
                medication.setDosage(dosage);
                medication.setFrequency(frequency);
                medication.setStartDate(startDate);
                medication.setEndDate(endDate);
                medication.setStatus(status);
                medication.setNotes(notes);
                
                medicationDAO.addMedication(medication);
                JOptionPane.showMessageDialog(this, "Medication added successfully!");
                medNameField.setText("");
                dosageField.setText("");
                frequencyField.setText("");
                notesArea.setText("");
                statusCombo.setSelectedIndex(0);
                loadMedicalHistory();
                DataChangeManager.getInstance().notifyMedicalHistoryChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding medication: " + ex.getMessage());
            }
        });
        
        clearBtn.addActionListener(e -> {
            medNameField.setText("");
            dosageField.setText("");
            frequencyField.setText("");
            notesArea.setText("");
            statusCombo.setSelectedIndex(0);
        });

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Current Medications"));
        JTextArea displayArea = new JTextArea(12, 50);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        displayArea.setEditable(false);
        displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        try {
            StringBuilder text = new StringBuilder();
            for (Medication med : medicationDAO.getMedicationsByPatientId(selectedPatientId)) {
                text.append("* ").append(med.getMedicationName()).append(" [").append(med.getStatus()).append("]\n");
                text.append("  Dosage: ").append(med.getDosage()).append(" | Frequency: ").append(med.getFrequency()).append("\n");
                text.append("  Started: ").append(med.getStartDate()).append("\n");
                if (med.getEndDate() != null && !med.getEndDate().isEmpty()) {
                    text.append("  End date: ").append(med.getEndDate()).append("\n");
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

    @Override
    public void onPatientsChanged() {
        loadPatients();
    }

    @Override
    public void onAppointmentsChanged() {
    }

    @Override
    public void onMedicalHistoryChanged() {
        if (selectedPatientId != -1) {
            loadMedicalHistory();
        }
    }
}