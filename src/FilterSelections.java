import java.util.Set;

/**
 * Holds raw, unvalidated snapshot of all user selections from the filter panel.
 * Validation and parsing to be handled by consumer.
 * @param selectedType Type value
 * @param selectedBun Object
 * @param selectedSauces Set of Objects
 * @param selectedDressing Object
 * @param selectedLeafyGreens Set of Objects
 * @param selectedProteins Set of Objects
 * @param tomatoSelection Boolean--true for yes, false for no, null for skip
 * @param cucumberSelection  Boolean--true for yes, false for no, null for skip
 * @param pickleSelection boolean--true for yes, false for no
 * @param selectedCheese Object
 * @param minPrice String
 * @param maxPrice String
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
) {}
