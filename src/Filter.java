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
     * <p>HTML-ified.
     * @return String of the filter prompt.
     */
    public String filterPrompt() {
        return switch (this) {
            case TYPE -> "<html><b>Select item type:</b></html>";
            case BUN -> "<html><b>Bun choice?</b</html>";
            case PROTEIN -> "<html><b>Proteins?</b> <i>(hold ctrl to select >1)</i></html>";
            case CHEESE -> "<html><b>Cheese:</b></html>";
            case PICKLES -> "<html><b>Pickles?</b></html>";
            case CUCUMBER -> "<html><b>Cucumber?</b></html>";
            case TOMATO -> "<html><b>Tomato?</b></html>";
            case DRESSING -> "<html><b>Dressing?</b></html>";
            case LEAFY_GREENS -> "<html><b>Leafy greens?</b> <i>(hold ctrl to select >1)</i></html>";
            case SAUCE_S -> "<html><b>Sauces?</b> <i>(hold ctrl to select >1)</i></html>";
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
            case CHEESE -> true;
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
    public boolean allowsDontMindChoice() {
        return switch (this) {
            case TYPE -> false;
            case BUN -> true;
            case PROTEIN -> true;
            case CHEESE -> true;
            case PICKLES -> false;
            case CUCUMBER -> true;
            case TOMATO -> true;
            case DRESSING -> true;
            case LEAFY_GREENS -> true;
            case SAUCE_S -> true;
        };
    }

    /**
     * Coordinates the Filters' 'I don't mind'-equivalent values.
     * <p>This should be guarded behind a call to 'allowsDontMindChoice() to avoid null handling.
     * @return Object of the value (as String for some and an individual Enum value for others).
     * <p>null if there is no relevant value
     */
    public Object getDontMindValue() {
        return switch (this) {
            case DRESSING -> Dressing.NA;
            case PROTEIN -> Protein.NA;
            case SAUCE_S -> Sauce.NA;
            case BUN, CHEESE, LEAFY_GREENS, TOMATO, CUCUMBER -> SpecialChoice.I_DONT_MIND;
            default -> null;
        };
    }

    /**
     * Attempt to optimise search by ordering by most restrictive binary filters first
     * @return int of the item's search order.
     */
    public int searchOrder() {
        return switch (this) {
            case TYPE -> 1;
            case PICKLES -> 2;
            case CUCUMBER -> 3;
            case TOMATO -> 4;
            case BUN -> 5;
            case SAUCE_S -> 6;
            case DRESSING -> 7;
            case CHEESE -> 8;
            case PROTEIN -> 9;
            case LEAFY_GREENS -> 10;
            default -> Integer.MAX_VALUE; //Any new menu entries will just go to the back of the queue.
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
