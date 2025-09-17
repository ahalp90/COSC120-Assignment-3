/**
 * Identifies valid special choices value (i.e. selector choices that have not
 * been read-in from the menu text or a relevant Filter's Enum
 */
public enum SpecialChoice {
    NONE,
    I_DONT_MIND;

    /**
     * Prettified toString
     * @return String
     */
    @Override
    public String toString() {
        return switch (this) {
            case NONE -> "None";
            case I_DONT_MIND -> "I dont mind";
        };
    }
}
