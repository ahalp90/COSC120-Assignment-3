/**
 * Enum to identify the Strings that might be ascribed to a button group offering
 * 'Yes', 'No' and 'I Don't Mind' options.
 * <p>Facilitates reliable and cross-class validation, particularly for intended null-return cases.
 */
public enum YesNoSkipValidButtonStrings {
    YES,
    NO,
    I_DONT_MIND;

    /**
     * Provide prettified toStrings for this enum's values
     * @return
     */
    @Override
    public String toString() {
        return switch (this) {
            case YES -> "Yes";
            case NO -> "No";
            case I_DONT_MIND -> Filter.TOMATO.getDontMindValue().toString(); //TODO FIX BECAUSE THIS IS NOW REDUNDANT***********
        };
    }
}
