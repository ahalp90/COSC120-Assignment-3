/**
 * Valid values for Filter.PROTEIN
 */
public enum Protein {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    BEEF, CHICKEN, VEGAN, NA;

    /**
     * Prettified toString representation of the Protein values.
     * @return String
     */
    @Override
    public String toString(){
        return switch (this) {
            case BEEF -> "Beef";
            case CHICKEN -> "Chicken";
            case VEGAN -> "Vegan";
            case NA -> SpecialChoice.I_DONT_MIND.toString();
        };
    }


}
