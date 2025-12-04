package clinicmanager.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    private static final String PHONE_PATTERN = 
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$";
    
    private static final String DATE_PATTERN = 
        "^\\d{4}-\\d{2}-\\d{2}(\\s\\d{2}:\\d{2}(:\\d{2})?)?$";

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
        // Remove common separators for validation
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
        return null; // No errors
    }
}

