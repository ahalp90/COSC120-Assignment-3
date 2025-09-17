/**
 * Valid values for Filter.TYPE
 */
public enum Type {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    BURGER,SALAD;

    /**
     * Prettified toString
     * @return String
     */
    @Override
    public String toString(){
        return switch (this) {
            case BURGER -> "Burger";
            case SALAD -> "Salad";
        };
    }
}
