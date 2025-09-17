/**
 * Valid values for Filter.DRESSING
 */
public enum Dressing {
    /**
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
            case NA -> SpecialChoice.I_DONT_MIND.toString();
        };
    }

}
