public enum Filter {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025.
     * Smart Enum methods added by Ariel Halperin.
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
        return switch (this) {
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
        };
    }

    /**
     * Identifies filter values that allow a selection of 'None'--i.e. I don't want anything at all from this category.
     * @return true if Filter value allows requesting 'none' selection for this category.
     */
    public boolean allowsNoneChoice() {
        return switch (this) {
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
        };
    }

    /**
     * Identifies filter values that allow users to 'skip' filtering by this attribute--i.e. 'I don't mind which'.
     * @return boolean true if allows skipping because 'I don't mind which'.
     */
    public boolean allowsDontCareChoice() {
        return switch (this) {
            case TYPE -> false;
            case BUN -> true;
            case PROTEIN -> true;
            case CHEESE -> true;
            case PICKLES -> false;
            case CUCUMBER -> false;
            case TOMATO -> false;
            case DRESSING -> true;
            case LEAFY_GREENS -> false;
            case SAUCE_S -> false;
        };
    }

    /**
     * Identifies Filters that do not have dedicated Enums of their possible values and cannot
     * be represented by a simple true/false/null distinction.
     * @return true if menu data is needed for filter, false otherwise.
     */
    public boolean needsMenuDataForSelectorOptions() {
        return switch (this){
            case BUN, CHEESE, LEAFY_GREENS -> true;
            default -> false;
        };
    }

    /**
     * Identifies Filters that are relevant to Burger selection
     * @return true if relevant for burgers
     */
    public boolean isRelevantForBurger() {
        return switch (this){
            case TYPE,BUN,PROTEIN,CHEESE,PICKLES,TOMATO,SAUCE_S -> true;
            default -> false;
        };
    }

    /**
     * Identifies Filters that are relevant to Salad selection
     * @return true if relevant for salads
     */
    public boolean isRelevantForSalad() {
        return switch (this) {
            case TYPE,PROTEIN,CHEESE,PICKLES,CUCUMBER,TOMATO,DRESSING,LEAFY_GREENS -> true;
            default -> false;
        };
    }
}
