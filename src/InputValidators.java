import java.util.regex.Pattern;

public final class InputValidators {
    //All patterns private--no need for implementation details to be shared.
    //Adapted from COSC120 Tute 4 solution 3_4, FindADog.java, ln82-160 regex pattern.
    private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    //Adapted from COSC120 Tute 4 solution 3_4, FindADog.java, ln94-98
    private static final Pattern phonePattern = Pattern.compile("^0\\d{9}$");
    // Adapted from COSC120 Assignment 1, MenuSearcher, getUserInfo(), by Ariel Halperin.
    private static final Pattern fullNamePattern = Pattern.compile("^[A-Z][a-zA-Z]+\\s[A-Z][a-zA-Z]+$");

    //Error message constants to go with the relevant patterns:
    public static final String ERROR_INVALID_EMAIL = "Sorry, that email was invalid. "
            +"\nPlease enter your email address in the appropriate format, ensuring there are no spaces within. "
            +"\nE.g. burgerfan@yum.com";
    public static final String ERROR_INVALID_PHONE = "Sorry, that phone number was invalid. "
            +"\nPlease ensure your phone number consists of 10 digits and commences by a 0. e.g. 0413371337";
    public static final String ERROR_INVALID_NAME = "Sorry, that name is invalid. "
            +"\nPlease enter your full name in First name & Surname format. "
            +"\nThe first letter of each name must be uppercase, and the two separated by a space. \ne.g. Burger Bob";

    public static final String ERROR_INVALID_PRICE_FORMAT = "Sorry, that price format is invalid."
            +"\nPlease enter numbers only."
            +"\nThe only non-numeric character may be a full-stop as a decimal separator.\n"
            +"e.g. 3.50";

    /**
     * Validates whether an input email address matches the RFC 5322 standard.
     * @param email String of the purported email
     * @return boolean true if email is valid.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false; //Defensively circumvents NPE and is true--null obviously not valid.
        return emailPattern.matcher(email).matches();
    }

    /**
     * Validates whether an input phone number starts with a 0 and is followed by 9 digits.
     * @param phoneNo String of the purported phone number
     * @return boolean true if phone number is valid.
     */
    public static boolean isValidPhoneNo(String phoneNo) {
        if (phoneNo == null) return false; //Defensively circumvents NPE and is true--null obviously not valid.
        return phonePattern.matcher(phoneNo).matches();

    }

    /**
     * Validates that the input is two words separated by a space. Word1 Letter1
     * uppercase and following letters lowercase. Word2 must begin with an uppercase, though
     * subsequent letters may be of either case.
     * @param fullName String of the purported full name
     * @return boolean true if name is valid.
     */
    public static boolean isFullName(String fullName) {
        if (fullName == null) return false; //Defensively circumvents NPE and is true--null obviously not valid.
        return  fullNamePattern.matcher(fullName).matches();
    }

    /**
     * Validates that the String can be parsed to a non-negative float.
     * <p>Expects caller to handler false returns.
     * @param priceString the String to validate
     * @return true if the String is a valid, non-negative price.
     */
    public static boolean isValidPrice(String priceString) {
        if (priceString == null || priceString.isBlank()) return false;

        try {
            float price = Float.parseFloat(priceString);
            return price >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
