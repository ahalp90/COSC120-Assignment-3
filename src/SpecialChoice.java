public enum SpecialChoice {
    NONE,
    I_DONT_MIND;

    @Override
    public String toString() {
        return switch (this) {
            case NONE -> "None";
            case I_DONT_MIND -> "I Dont Mind";
        };
    }
}
