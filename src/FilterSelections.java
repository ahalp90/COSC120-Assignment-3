import java.util.Set;

/**
 * Creates a FilterSelections record of the filters the user has selected.
 * @param selectedType Type of food (e.g. BUN/SALAD) (as Object)
 * @param selectedBun BUN selection (as Object)
 * @param selectedSauces immutable Set of sauces selected (as Object) (does not allow nulls)
 * @param selectedDressing DRESSING selected (as Object)
 * @param selectedLeafyGreens immutable set of Leafy Greens selected (as Object)(does not allow nulls)
 * @param selectedProteins immutable set of PROTEIN selected (as Object) (does not allow nulls)
 * @param tomatoSelection Boolean of tomato (true yes, false no, null I_DONT_MIND)
 * @param cucumberSelection Boolean of cucumber (true yes, false no, null I_DONT_MIND)
 * @param pickleSelection boolean of pickle selection (true yes, false no)
 * @param selectedCheese Object of selected cheese
 * @param minPrice String of min price desired
 * @param maxPrice String of max price desired
 */
public record FilterSelections(
   Type selectedType,
   Object selectedBun,
   Set<Object> selectedSauces,
   Object selectedDressing,
   Set<Object> selectedLeafyGreens,
   Set<Object> selectedProteins,
   Boolean tomatoSelection,
   Boolean cucumberSelection,
   boolean pickleSelection,
   Object selectedCheese,
   String minPrice,
   String maxPrice
) {

    /**
     * Creates a FilterSelections record of the filters the user has selected
     * @param selectedType Type of food (e.g. BUN/SALAD) (as Object)
     * @param selectedBun BUN selection (as Object)
     * @param selectedSauces immutable Set of sauces selected (as Object) (does not allow nulls)
     * @param selectedDressing DRESSING selected (as Object)
     * @param selectedLeafyGreens immutable set of Leafy Greens selected (as Object)(does not allow nulls)
     * @param selectedProteins immutable set of PROTEIN selected (as Object) (does not allow nulls)
     * @param tomatoSelection Boolean of tomato (true yes, false no, null I_DONT_MIND)
     * @param cucumberSelection Boolean of cucumber (true yes, false no, null I_DONT_MIND)
     * @param pickleSelection boolean of pickle selection (true yes, false no)
     * @param selectedCheese Object of selected cheese
     * @param minPrice String of min price desired
     * @param maxPrice String of max price desired
     */
    public FilterSelections(
            Type selectedType,
            Object selectedBun,
            Set<Object> selectedSauces,
            Object selectedDressing,
            Set<Object> selectedLeafyGreens,
            Set<Object> selectedProteins,
            Boolean tomatoSelection,
            Boolean cucumberSelection,
            boolean pickleSelection,
            Object selectedCheese,
            String minPrice,
            String maxPrice) {

        this.selectedSauces = Set.copyOf(selectedSauces);
        this.selectedLeafyGreens = Set.copyOf(selectedLeafyGreens);
        this.selectedProteins = Set.copyOf(selectedProteins);

        this.selectedType = selectedType;
        this.selectedBun = selectedBun;
        this.selectedDressing = selectedDressing;
        this.tomatoSelection = tomatoSelection;
        this.cucumberSelection = cucumberSelection;
        this.pickleSelection = pickleSelection;
        this.selectedCheese = selectedCheese;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
