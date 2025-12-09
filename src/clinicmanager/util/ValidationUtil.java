package clinicmanager.util;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

public class ValidationUtil {
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    private static final String PHONE_PATTERN = 
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$";
    
    private static final String DATE_PATTERN = 
        "^\\d{4}-\\d{2}-\\d{2}(\\s\\d{2}:\\d{2}(:\\d{2})?)?$";
    

    private static final List<String> VALID_SEVERITIES = Arrays.asList(
        "mild", "moderate", "severe", "life-threatening"
    );
    

    private static final List<String> VALID_APPOINTMENT_STATUSES = Arrays.asList(
        "scheduled", "arrived", "in-progress", "completed", "cancelled", "no_show"
    );
    

    private static final List<String> VALID_VISIT_STATUSES = Arrays.asList(
        "in-progress", "completed", "cancelled"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
        return Pattern.compile(PHONE_PATTERN).matcher(cleaned).matches() || cleaned.length() >= 10;
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile(DATE_PATTERN).matcher(date).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String validatePatientForm(String firstName, String lastName, String dob, 
                                            String phone, String email, String address) {
        if (!isNotEmpty(firstName)) {
            return "First name is required";
        }
        if (!isNotEmpty(lastName)) {
            return "Last name is required";
        }
        if (isNotEmpty(email) && !isValidEmail(email)) {
            return "Invalid email format";
        }
        if (isNotEmpty(phone) && !isValidPhone(phone)) {
            return "Invalid phone number format";
        }
        if (isNotEmpty(dob) && !isValidDate(dob)) {
            return "Invalid date format. Use YYYY-MM-DD";
        }
        return null;
    }
    

    public static boolean isValidSeverity(String severity) {
        if (severity == null || severity.trim().isEmpty()) {
            return false;
        }
        return VALID_SEVERITIES.contains(severity.toLowerCase().trim());
    }
    

    public static List<String> getValidSeverities() {
        return VALID_SEVERITIES;
    }
    

    public static boolean isValidAppointmentStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return VALID_APPOINTMENT_STATUSES.contains(status.toLowerCase().trim());
    }
    

    public static List<String> getValidAppointmentStatuses() {
        return VALID_APPOINTMENT_STATUSES;
    }
    

    public static boolean isValidVisitStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return VALID_VISIT_STATUSES.contains(status.toLowerCase().trim());
    }
    

    public static List<String> getValidVisitStatuses() {
        return VALID_VISIT_STATUSES;
    }
    

    public static String normalizeSeverity(String severity) {
        if (severity == null) return "moderate";
        String lower = severity.toLowerCase().trim();
        if (VALID_SEVERITIES.contains(lower)) {
            return lower;
        }
        return "moderate";
    }
    

    public static String normalizeStatus(String status, String defaultStatus) {
        if (status == null) return defaultStatus;
        return status.toLowerCase().trim();
    }
}

