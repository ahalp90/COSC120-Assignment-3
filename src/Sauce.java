/**
 * Valid values for Filter.SAUCES
 */
public enum Sauce {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    TOMATO, GARLIC, AIOLI, BBQ, CHILLI, RANCH, SPECIAL, NA;

    /**
     * Prettified toString
     * @return String
     */
    public String toString(){
        return switch (this) {
            case TOMATO -> "Tomato";
            case GARLIC -> "Garlic";
            case AIOLI -> "Aioli (vegan friendly)";
            case BBQ -> "BBQ";
            case CHILLI -> "Chilli";
            case RANCH -> "Ranch";
            case SPECIAL -> "Special sauce";
            case NA -> SpecialChoice.I_DONT_MIND.toString();
        };
    }

}
