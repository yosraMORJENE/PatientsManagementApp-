package clinicmanager.gui;

import clinicmanager.dao.*;
import clinicmanager.models.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;

public class MedicalHistoryPanel extends JPanel {
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
        
        JButton refreshPatientsBtn = new JButton("↻");
        refreshPatientsBtn.setPreferredSize(new Dimension(35, 25));
        refreshPatientsBtn.setToolTipText("Refresh patient list");
        refreshPatientsBtn.addActionListener(e -> loadPatients());
        patientPanel.add(refreshPatientsBtn);
        
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

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Medical Conditions History"));
        JTextArea displayArea = new JTextArea(12, 50);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        displayArea.setEditable(false);
        displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        try {
            StringBuilder text = new StringBuilder();
            for (MedicalCondition cond : medicalConditionDAO.getConditionsByPatientId(selectedPatientId)) {
                text.append("• ").append(cond.getConditionName()).append(" (").append(cond.getStatus()).append(")\n");
                text.append("  Diagnosed: ").append(cond.getDiagnosisDate()).append("\n");
                text.append("  Notes: ").append(cond.getNotes()).append("\n\n");
            }
            displayArea.setText(text.toString());
        } catch (SQLException ex) {
            displayArea.setText("Error loading conditions");
        }

        mainPanel.add(displayPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createAllergiesPanel() throws SQLException {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Patient Allergies"));
        JTextArea displayArea = new JTextArea(12, 50);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        displayArea.setEditable(false);
        displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        try {
            StringBuilder text = new StringBuilder();
            for (Allergy allergy : allergyDAO.getAllergiesByPatientId(selectedPatientId)) {
                text.append("⚠ ").append(allergy.getAllergen()).append(" [").append(allergy.getSeverity()).append("]\n");
                text.append("  Reaction: ").append(allergy.getReaction()).append("\n");
                text.append("  Notes: ").append(allergy.getNotes()).append("\n\n");
            }
            displayArea.setText(text.toString());
        } catch (SQLException ex) {
            displayArea.setText("Error loading allergies");
        }

        mainPanel.add(displayPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createMedicationsPanel() throws SQLException {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Current Medications"));
        JTextArea displayArea = new JTextArea(12, 50);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        displayArea.setEditable(false);
        displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        try {
            StringBuilder text = new StringBuilder();
            for (Medication med : medicationDAO.getActiveMedicationsByPatientId(selectedPatientId)) {
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
}
