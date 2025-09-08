public enum Protein {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    BEEF, CHICKEN, VEGAN, NA;

    public String toString(){
        return switch (this) {
            case BEEF -> "Beef";
            case CHICKEN -> "Chicken";
            case VEGAN -> "Vegan";
            case NA -> "Any protein will do...";
        };
    }


}
