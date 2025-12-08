package clinicmanager.util;

import clinicmanager.dao.PatientDAO;
import clinicmanager.dao.AppointmentDAO;
import clinicmanager.dao.VisitDAO;
import clinicmanager.models.Patient;
import clinicmanager.models.Appointment;
import clinicmanager.models.Visit;
import clinicmanager.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

/**
 * Demonstration class showcasing Java Streams, Collections (List, Set, Map),
 * Lambda expressions, and Exception handling in the Patients Management Application.
 * 
 * This class demonstrates:
 * 1. Java Streams API (filter, map, collect, groupingBy, sorted, limit, count)
 * 2. Collections: List, Set, Map
 * 3. Lambda expressions and functional interfaces
 * 4. Exception handling with try-catch blocks
 */
public class StreamsAndCollectionsDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Patients Management Application - Streams & Collections Demo ===\n");
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            PatientDAO patientDAO = new PatientDAO(connection);
            AppointmentDAO appointmentDAO = new AppointmentDAO(connection);
            VisitDAO visitDAO = new VisitDAO(connection);
            
            // ========== JAVA STREAMS EXAMPLES ==========
            System.out.println("--- JAVA STREAMS DEMONSTRATIONS ---\n");
            
            // Example 1: Filter patients by last name using streams
            demonstrateStreamFilter(patientDAO);
            
            // Example 2: Sort patients using streams
            demonstrateSortingWithStreams(patientDAO);
            
            // Example 3: Count operations with streams
            demonstrateCountWithStreams(patientDAO);
            
            // Example 4: Find operation with Optional
            demonstrateFindWithOptional(patientDAO);
            
            // Example 5: Filter appointments by reason
            demonstrateAppointmentFiltering(appointmentDAO);
            
            // Example 6: Limit results with streams
            demonstrateLimitWithStreams(visitDAO);
            
            // ========== SET COLLECTION EXAMPLES ==========
            System.out.println("\n--- SET COLLECTION DEMONSTRATIONS ---\n");
            
            // Example 7: Get unique email domains using Set
            demonstrateSetForUniqueness(patientDAO);
            
            // Example 8: Get unique area codes
            demonstrateUniqueAreaCodes(patientDAO);
            
            // ========== MAP COLLECTION EXAMPLES ==========
            System.out.println("\n--- MAP COLLECTION DEMONSTRATIONS ---\n");
            
            // Example 9: Create lookup Map by ID
            demonstrateMapLookup(patientDAO);
            
            // Example 10: Group by email domain
            demonstrateGroupingWithMap(patientDAO);
            
            // Example 11: Count aggregation with Map
            demonstrateCountingWithMap(patientDAO);
            
            // Example 12: Count appointments per patient
            demonstrateAppointmentCounting(appointmentDAO);
            
            System.out.println("\n=== Demo completed successfully! ===");
            
        } catch (SQLException e) {
            // Exception handling example
            System.err.println("Database error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // General exception handling
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========== STREAM DEMONSTRATIONS ==========
    
    private static void demonstrateStreamFilter(PatientDAO patientDAO) throws SQLException {
        System.out.println("1. Stream Filter - Patients with email:");
        List<Patient> patientsWithEmail = patientDAO.getPatientsWithEmailStream();
        System.out.println("   Found " + patientsWithEmail.size() + " patients with email addresses");
        patientsWithEmail.stream()
            .limit(3)
            .forEach(p -> System.out.println("   - " + p.getFirstName() + " " + p.getLastName() + " (" + p.getEmail() + ")"));
        System.out.println();
    }
    
    private static void demonstrateSortingWithStreams(PatientDAO patientDAO) throws SQLException {
        System.out.println("2. Stream Sorted - Patients sorted by name:");
        List<Patient> sortedPatients = patientDAO.getSortedPatientsStream();
        sortedPatients.stream()
            .limit(5)
            .forEach(p -> System.out.println("   - " + p.getLastName() + ", " + p.getFirstName()));
        System.out.println();
    }
    
    private static void demonstrateCountWithStreams(PatientDAO patientDAO) throws SQLException {
        System.out.println("3. Stream Count - Count patients by area code:");
        long count = patientDAO.countPatientsWithAreaCode("555");
        System.out.println("   Patients with area code 555: " + count);
        System.out.println();
    }
    
    private static void demonstrateFindWithOptional(PatientDAO patientDAO) throws SQLException {
        System.out.println("4. Stream findFirst with Optional:");
        Optional<Patient> patient = patientDAO.findPatientByIdStream(1);
        if (patient.isPresent()) {
            System.out.println("   Found patient: " + patient.get().getFirstName() + " " + patient.get().getLastName());
        } else {
            System.out.println("   Patient not found");
        }
        System.out.println();
    }
    
    private static void demonstrateAppointmentFiltering(AppointmentDAO appointmentDAO) throws SQLException {
        System.out.println("5. Stream Filter Appointments - Filter by reason keyword:");
        List<Appointment> checkups = appointmentDAO.filterAppointmentsByReasonStream("checkup");
        System.out.println("   Found " + checkups.size() + " appointments containing 'checkup'");
        System.out.println();
    }
    
    private static void demonstrateLimitWithStreams(VisitDAO visitDAO) throws SQLException {
        System.out.println("6. Stream Limit - Get recent visits (limited to 5):");
        List<Visit> recentVisits = visitDAO.getRecentVisitsStream(5);
        System.out.println("   Retrieved " + recentVisits.size() + " recent visits");
        System.out.println();
    }
    
    // ========== SET DEMONSTRATIONS ==========
    
    private static void demonstrateSetForUniqueness(PatientDAO patientDAO) throws SQLException {
        System.out.println("7. Set for Uniqueness - Unique email domains:");
        Set<String> domains = patientDAO.getUniqueEmailDomains();
        System.out.println("   Found " + domains.size() + " unique email domains:");
        domains.stream()
            .limit(5)
            .forEach(domain -> System.out.println("   - " + domain));
        System.out.println();
    }
    
    private static void demonstrateUniqueAreaCodes(PatientDAO patientDAO) throws SQLException {
        System.out.println("8. Set Collection - Unique phone area codes:");
        Set<String> areaCodes = patientDAO.getUniqueAreaCodes();
        System.out.println("   Found " + areaCodes.size() + " unique area codes:");
        areaCodes.stream()
            .limit(5)
            .forEach(code -> System.out.println("   - " + code));
        System.out.println();
    }
    
    // ========== MAP DEMONSTRATIONS ==========
    
    private static void demonstrateMapLookup(PatientDAO patientDAO) throws SQLException {
        System.out.println("9. Map for Lookup - Patient lookup by ID:");
        Map<Integer, Patient> patientMap = patientDAO.getPatientsMapByIdStream();
        System.out.println("   Created Map with " + patientMap.size() + " patients");
        
        // Lambda expression for lookup demonstration
        Integer testId = patientMap.keySet().stream().findFirst().orElse(null);
        if (testId != null) {
            Patient p = patientMap.get(testId);
            System.out.println("   Quick lookup for ID " + testId + ": " + p.getFirstName() + " " + p.getLastName());
        }
        System.out.println();
    }
    
    private static void demonstrateGroupingWithMap(PatientDAO patientDAO) throws SQLException {
        System.out.println("10. Map Grouping - Group patients by email domain:");
        Map<String, List<Patient>> groupedByDomain = patientDAO.groupPatientsByEmailDomain();
        System.out.println("   Grouped patients into " + groupedByDomain.size() + " domains");
        
        // Lambda to display grouping
        groupedByDomain.entrySet().stream()
            .limit(3)
            .forEach(entry -> System.out.println("   - " + entry.getKey() + ": " + entry.getValue().size() + " patients"));
        System.out.println();
    }
    
    private static void demonstrateCountingWithMap(PatientDAO patientDAO) throws SQLException {
        System.out.println("11. Map with Counting - Count patients per area code:");
        Map<String, Long> countByAreaCode = patientDAO.countPatientsByAreaCode();
        System.out.println("   Statistics for " + countByAreaCode.size() + " area codes");
        
        // Lambda for displaying counts
        countByAreaCode.entrySet().stream()
            .limit(3)
            .forEach(entry -> System.out.println("   - Area code " + entry.getKey() + ": " + entry.getValue() + " patients"));
        System.out.println();
    }
    
    private static void demonstrateAppointmentCounting(AppointmentDAO appointmentDAO) throws SQLException {
        System.out.println("12. Map Aggregation - Count appointments per patient:");
        Map<Integer, Long> appointmentCounts = appointmentDAO.countAppointmentsPerPatientStream();
        System.out.println("   Calculated appointment counts for " + appointmentCounts.size() + " patients");
        
        // Lambda to show top patients
        appointmentCounts.entrySet().stream()
            .limit(3)
            .forEach(entry -> System.out.println("   - Patient ID " + entry.getKey() + ": " + entry.getValue() + " appointments"));
        System.out.println();
    }
}
