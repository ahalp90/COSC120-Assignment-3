public enum Filter {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    TYPE,BUN,PROTEIN,CHEESE,PICKLES,CUCUMBER,TOMATO,DRESSING,LEAFY_GREENS,SAUCE_S;

    public String toString(){
        return switch (this) {
            case TYPE -> "Menu item type";
            case BUN -> "Bun/bread";
            case PROTEIN -> "Protein";
            case CHEESE -> "Cheese";
            case PICKLES -> "Pickles (gherkins)";
            case CUCUMBER -> "Cucumber (continental)";
            case TOMATO -> "Tomato";
            case DRESSING -> "Salad dressing";
            case LEAFY_GREENS -> "Leafy greens";
            case SAUCE_S -> "Sauces";
        };
    }

    /**
     * Identifies the intended text prompt for filter selection.
     * @return String of the filter prompt.
     */
    public String filterPrompt() {
        return switch (this) {
            case TYPE -> "Select item type:";
            case BUN -> "Bun choice:";
            case PROTEIN -> "Proteins:";
            case CHEESE -> "Cheese:";
            case PICKLES -> "Pickles?";
            case CUCUMBER -> "Cucumber?";
            case TOMATO -> "Tomato?";
            case DRESSING -> "Dressing?";
            case LEAFY_GREENS -> "Leafy greens?";
            case SAUCE_S -> "Sauces?";
        };
    }

    /**
     * Identifies filter values that allow multiple selections
     * @return true if Filter value allows multiple selections.
     */
    public boolean allowsMultipleChoices() {
        case TYPE -> false;
        case BUN -> false;
        case PROTEIN -> true;
        case CHEESE -> false;
        case PICKLES -> false;
        case CUCUMBER -> false;
        case TOMATO -> false;
        case DRESSING -> false;
        case LEAFY_GREENS -> true;
        case SAUCE_S -> true;
    }




}
