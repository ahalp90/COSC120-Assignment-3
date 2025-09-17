import java.util.*;

/**
 * Holds the entire collection of MenuItems available in the restaurant.
 * <p>Also provides methods to search the menu and generate lists of available ingredients.
 * <p>Created by Dr Andreas Shepley for COSC120 on 25/04/2025
 * <p>Adapted by Ariel Halperin
 */
public final class Menu {

    private final Set<MenuItem> menu;

    /**
     * Creates an empty Menu.
     * Menu items should be added using the addItem(MenuItem) method.
     */
    public Menu() {
        this.menu = new HashSet<>();
    }

    /**
     * Adds a new menu item to this menu.
     * @param menuItem the MenuItem to add. Must not be null.
     */
    public void addItem(MenuItem menuItem){
        this.menu.add(menuItem);
    }

    /**
     * Gets all known possible ingredient types for this Filter.
     * <p>Pulls directly from a public enum's values if possible, otherwise populates from Menu.
     * <p>Sorts alphabetically, and adds NONE and I_DONT_MIND choices at the end.
     * @param filter Filter value representing this menu item
     * @return immutable List of Objects with the relevant values.
     */
    public List<Object> getAllIngredientTypes(Filter filter){
        List<Object> options = new ArrayList<>();

        if (filter.hasEnumRepresentingItsValues()) {
            //Intellij's null warning is wrong; this is already guarded by guarantee of hasEnumRepresentingItsValues()
            options.addAll(filter.getEnumValues());
        } else {
            //Add options for filters who need their values read from menu because they can't be
            //identified by their own Enums
            Set<Object> deduplicatedOptions = new HashSet<>();
            for(MenuItem menuItem: menu){
                if(menuItem.getDreamMenuItem().getAllFilters().containsKey(filter)){
                    var ingredientTypes = menuItem.getDreamMenuItem().getFilter(filter);

                    if(ingredientTypes instanceof Collection<?>) {
                        deduplicatedOptions.addAll((Collection<?>) ingredientTypes);
                    } else {
                        deduplicatedOptions.add(menuItem.getDreamMenuItem().getFilter(filter));
                    }
                }
            }
            options.addAll(deduplicatedOptions);
        }

        //sort alphabetically even though they're Object;
        //https://medium.com/@AlexanderObregon/javas-comparator-comparing-method-explained-342361288af6
        options.sort(Comparator.comparing(Object::toString));

        // Add NONE as the second last option to relevant Lists
        if (filter.allowsNoneChoice()) options.add(SpecialChoice.NONE);
        // Populate the 'I don't mind' values in last position
        if (filter.allowsDontMindChoice()){
            Object dontMindValue = filter.getDontMindValue();

            // Sorting put this in its alphabetical position if it was already part of the Filter's
            // specific class values; sent it to the end.
            if (filter.hasDontMindValueDefinedInOwnEnum()) {
                options.remove(dontMindValue);
                options.add(dontMindValue);
            } else {
                //If it wasn't already added direct from its own enum, add it from the values known in Filter.
                options.add(dontMindValue);
            }
        }

        return List.copyOf(options);
    }

    /**
     * Finds all menu items that match the user's search criteria.
     * <p>Iterates through every item on the menu and makes two checks:
     * <li>Compares attributes using the DreamMenuItem.matches() method.
     * <li>Compares the menu item's price against the user's ideal min and max prices.
     * <p>An item that passes both checks will be included in the results
     * @param dreamMenuItem DreamMenuItem representing the user's search criteria
     * @return an immutable List of MenuItems of all matching items. The list will be empty if no matches are found.
     */
    public List<MenuItem> findMatch(DreamMenuItem dreamMenuItem){
        List<MenuItem> matching = new ArrayList<>();
        for(MenuItem menuItem: menu){
            if(!menuItem.getDreamMenuItem().matches(dreamMenuItem)) continue;
            if(menuItem.getPrice()<dreamMenuItem.getMinPrice()|| menuItem.getPrice()>dreamMenuItem.getMaxPrice()) continue;
            matching.add(menuItem);
        }
        return List.copyOf(matching);
    }

    /**
     * Returns a copy of the complete set of all menu items
     * @return an immutable Set of MenuItems containing all items in this Menu.
     */
    public Set<MenuItem> getMenuItems(){return Set.copyOf(menu);}
}
