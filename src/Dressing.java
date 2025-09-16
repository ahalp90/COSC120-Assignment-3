public enum Dressing {
    /**
     * Enum values for possible salad dressing options
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    RANCH,FRENCH,ITALIAN,GREEN_GODDESS,NA;

    /**
     * A prettified toString of the values
     * @return String
     */
    @Override
    public String toString(){
        return switch (this) {
            case RANCH -> "Ranch";
            case FRENCH -> "French";
            case ITALIAN -> "Italian";
            case GREEN_GODDESS -> "Green goddess";
            case NA -> "I don't mind...";
        };
    }

}
