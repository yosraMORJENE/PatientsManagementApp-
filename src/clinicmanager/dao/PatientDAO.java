package clinicmanager.dao;

import clinicmanager.models.Patient;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Optional;

public class PatientDAO {
    private Connection connection = null;

    public PatientDAO(Connection connection) {
        this.connection = connection;
    }

    public PatientDAO() {
    }

    // Create a new patient
    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patients (first_name, last_name, date_of_birth, phone_number, email, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            
            // Convert string date to java.sql.Date
            if (patient.getDateOfBirth() != null && !patient.getDateOfBirth().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date utilDate = sdf.parse(patient.getDateOfBirth().trim());
                    stmt.setDate(3, new java.sql.Date(utilDate.getTime()));
                } catch (Exception e) {
                    throw new SQLException("Invalid date format. Please use YYYY-MM-DD format.", e);
                }
            } else {
                stmt.setDate(3, null);
            }
            
            stmt.setString(4, patient.getPhoneNumber());
            stmt.setString(5, patient.getEmail());
            stmt.setString(6, patient.getAddress());
            stmt.executeUpdate();
        }
    }

    // Retrieve all patients
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date dob = rs.getDate("date_of_birth");
                String dobString = (dob != null) ? sdf.format(dob) : null;
                
                Patient patient = new Patient(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    dobString,
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    rs.getString("address")
                );
                patients.add(patient);
            }
        }
        return patients;
    }

    // Update an existing patient
    public void updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE Patients SET first_name = ?, last_name = ?, date_of_birth = ?, phone_number = ?, email = ?, address = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            
            // Convert string date to java.sql.Date
            if (patient.getDateOfBirth() != null && !patient.getDateOfBirth().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date utilDate = sdf.parse(patient.getDateOfBirth().trim());
                    stmt.setDate(3, new java.sql.Date(utilDate.getTime()));
                } catch (Exception e) {
                    throw new SQLException("Invalid date format. Please use YYYY-MM-DD format.", e);
                }
            } else {
                stmt.setDate(3, null);
            }
            
            stmt.setString(4, patient.getPhoneNumber());
            stmt.setString(5, patient.getEmail());
            stmt.setString(6, patient.getAddress());
            stmt.setInt(7, patient.getId());
            stmt.executeUpdate();
        }
    }

    // Delete a patient
    public void deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM Patients WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.executeUpdate();
        }
    }

    // Get patient by ID
    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM Patients WHERE id = ?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date dob = rs.getDate("date_of_birth");
                    String dobString = (dob != null) ? sdf.format(dob) : null;
                    
                    return new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        dobString,
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address")
                    );
                }
            }
        }
        return null;
    }

    // Search patients by name
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR phone_number LIKE ?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date dob = rs.getDate("date_of_birth");
                    String dobString = (dob != null) ? sdf.format(dob) : null;
                    
                    Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        dobString,
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address")
                    );
                    patients.add(patient);
                }
            }
        }
        return patients;
    }

    // Advanced search with multiple criteria
    public List<Patient> advancedSearch(String name, String phone, String email, String appointmentDateStart, String appointmentDateEnd) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT p.* FROM Patients p LEFT JOIN appointments a ON p.id = a.patient_id WHERE 1=1");
        
        // Build dynamic query based on parameters
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND (p.first_name LIKE ? OR p.last_name LIKE ?)");
        }
        if (phone != null && !phone.trim().isEmpty()) {
            sql.append(" AND p.phone_number LIKE ?");
        }
        if (email != null && !email.trim().isEmpty()) {
            sql.append(" AND p.email LIKE ?");
        }
        if ((appointmentDateStart != null && !appointmentDateStart.trim().isEmpty()) ||
            (appointmentDateEnd != null && !appointmentDateEnd.trim().isEmpty())) {
            if (appointmentDateStart != null && !appointmentDateStart.trim().isEmpty()) {
                sql.append(" AND a.appointment_date >= ?");
            }
            if (appointmentDateEnd != null && !appointmentDateEnd.trim().isEmpty()) {
                sql.append(" AND a.appointment_date <= ?");
            }
        }
        
        sql.append(" ORDER BY p.last_name, p.first_name");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            
            if (name != null && !name.trim().isEmpty()) {
                String searchPattern = "%" + name + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            if (phone != null && !phone.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + phone + "%");
            }
            if (email != null && !email.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + email + "%");
            }
            if (appointmentDateStart != null && !appointmentDateStart.trim().isEmpty()) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(appointmentDateStart));
            }
            if (appointmentDateEnd != null && !appointmentDateEnd.trim().isEmpty()) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(appointmentDateEnd + " 23:59:59"));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date dob = rs.getDate("date_of_birth");
                    String dobString = (dob != null) ? sdf.format(dob) : null;
                    
                    Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        dobString,
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address")
                    );
                    patients.add(patient);
                }
            }
        }
        return patients;
    }
    
    // ============== JAVA STREAMS EXAMPLES ==============
    
    /**
     * Search patients by last name using streams and filters
     * Example of Stream API with filter and collect operations
     */
    public List<Patient> searchPatientsByLastNameStream(String lastName) throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .filter(p -> p.getLastName() != null && 
                        p.getLastName().toLowerCase().contains(lastName.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get patients with email using streams
     * Example of Stream API with filter for non-null values
     */
    public List<Patient> getPatientsWithEmailStream() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .filter(p -> p.getEmail() != null && !p.getEmail().trim().isEmpty())
            .collect(Collectors.toList());
    }
    
    /**
     * Sort patients by last name then first name using streams
     * Example of Stream API with sorted and Comparator
     */
    public List<Patient> getSortedPatientsStream() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .sorted(Comparator.comparing(Patient::getLastName)
                             .thenComparing(Patient::getFirstName))
            .collect(Collectors.toList());
    }
    
    /**
     * Count patients by phone area code using streams
     * Example of Stream API with map and count operations
     */
    public long countPatientsWithAreaCode(String areaCode) throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .filter(p -> p.getPhoneNumber() != null && 
                        p.getPhoneNumber().startsWith(areaCode))
            .count();
    }
    
    /**
     * Find patient by ID using Optional and streams
     * Example of Stream API with findFirst and Optional
     */
    public Optional<Patient> findPatientByIdStream(int id) throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .filter(p -> p.getId() == id)
            .findFirst();
    }
    
    // ============== SET COLLECTION EXAMPLES ==============
    
    /**
     * Get unique email domains from all patients
     * Example of Set collection to ensure uniqueness
     */
    public Set<String> getUniqueEmailDomains() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        Set<String> domains = new HashSet<>();
        
        allPatients.stream()
            .filter(p -> p.getEmail() != null && p.getEmail().contains("@"))
            .forEach(p -> {
                String email = p.getEmail();
                String domain = email.substring(email.indexOf("@") + 1);
                domains.add(domain.toLowerCase());
            });
        
        return domains;
    }
    
    /**
     * Get unique phone area codes
     * Example of Set with stream operations
     */
    public Set<String> getUniqueAreaCodes() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .map(Patient::getPhoneNumber)
            .filter(phone -> phone != null && phone.length() >= 3)
            .map(phone -> phone.substring(0, 3))
            .collect(Collectors.toSet());
    }
    
    // ============== MAP COLLECTION EXAMPLES ==============
    
    /**
     * Create a Map of patients indexed by ID
     * Example of Map collection for fast lookups
     */
    public Map<Integer, Patient> getPatientsMapById() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        Map<Integer, Patient> patientMap = new HashMap<>();
        
        for (Patient patient : allPatients) {
            patientMap.put(patient.getId(), patient);
        }
        
        return patientMap;
    }
    
    /**
     * Create a Map of patients indexed by ID using streams
     * Example of Stream API with Collectors.toMap
     */
    public Map<Integer, Patient> getPatientsMapByIdStream() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .collect(Collectors.toMap(Patient::getId, patient -> patient));
    }
    
    /**
     * Group patients by email domain
     * Example of Map with List values and stream groupingBy
     */
    public Map<String, List<Patient>> groupPatientsByEmailDomain() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .filter(p -> p.getEmail() != null && p.getEmail().contains("@"))
            .collect(Collectors.groupingBy(p -> {
                String email = p.getEmail();
                return email.substring(email.indexOf("@") + 1).toLowerCase();
            }));
    }
    
    /**
     * Count patients per area code
     * Example of Map with counting using streams
     */
    public Map<String, Long> countPatientsByAreaCode() throws SQLException {
        List<Patient> allPatients = getAllPatients();
        return allPatients.stream()
            .filter(p -> p.getPhoneNumber() != null && p.getPhoneNumber().length() >= 3)
            .collect(Collectors.groupingBy(
                p -> p.getPhoneNumber().substring(0, 3),
                Collectors.counting()
            ));
    }
}