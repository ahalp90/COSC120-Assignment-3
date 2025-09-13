import java.util.*;

public final class Menu {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    private final Set<MenuItem> menu = new HashSet<>();

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
        List<Object> options = new ArrayList();

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

    public List<MenuItem> findMatch(DreamMenuItem dreamMenuItem){
        List<MenuItem> matching = new ArrayList<>();
        for(MenuItem menuItem: menu){
            if(!menuItem.getDreamMenuItem().matches(dreamMenuItem)) continue;
            if(menuItem.getPrice()<dreamMenuItem.getMinPrice()|| menuItem.getPrice()>dreamMenuItem.getMaxPrice()) continue;
            matching.add(menuItem);
        }
        return List.copyOf(matching);
    }

    public Set<MenuItem> getMenuItems(){return Set.copyOf(menu);}
}
