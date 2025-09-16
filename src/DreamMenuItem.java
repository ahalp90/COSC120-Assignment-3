import java.util.*;

/**
 * Class responsible for holding the generic attributes to be ascribed to all menu items.
 * Also used to represent user search criteria--as these match menu items' generic attributes.
 * priceMin/Max fields pertain exclusively to search objects.
 */
public final class DreamMenuItem {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     * Adapted by Ariel Halperin.
     */
    private final Map<Filter,Object> filterMap;
    private final double minPrice;
    private final double maxPrice;

    /**
     * Constructor for DreamMenuItems used as user search criteria. Accepts min and max price parameters.
     * @param filterMap immutable Map of Filters and Objects representing this item's attributes
     *                  <p><b>Note, cannot hold nulls</b></p>
     * @param minPrice double
     * @param maxPrice double
     */
    public DreamMenuItem(Map<Filter, Object> filterMap, double minPrice, double maxPrice) {
        this.filterMap=Map.copyOf(filterMap);
        this.minPrice=minPrice;
        this.maxPrice=maxPrice;
    }

    /**
     * Overloaded constructor for building DreamMenuItems read directly from menu data.
     * <p>These don't logically have a min or max price, so it is set to -1</p>
     * @param filterMap immutable Map of Filters and Objects representing this item's attributes
     *                  <p><b>Note, cannot hold nulls</b></p>
     */
    public DreamMenuItem(Map<Filter, Object> filterMap) {
        this.filterMap=Map.copyOf(filterMap);
        this.minPrice=-1;
        this.maxPrice=-1;
    }

    /**
     * Gets a Map of all Filters and their values stored by this DreamMenuItem.
     * @return Map of Filter(key) object(value)
     */
    public Map<Filter, Object> getAllFilters() {
        return new HashMap<>(filterMap);
    }

    /**
     * Gets the value associated with the parameter key for this DreamMenuItem
     * @param key a Filter value
     * @return the value associated with the parameter key
     */
    public Object getFilter(Filter key){return this.filterMap.get(key);}

    /**
     * Returns the value assigned to the TYPE key for this DreamMenuItem
     * @return the value that matches the Filter.TYPE key (probably a Type enum value),
     * <b>or null</b> if the key doesn't exist
     */
    public Object getDreamItemType() {
        return this.filterMap.getOrDefault(Filter.TYPE, null);
    }

    /**
     * Gets the min price assigned to this DreamMenuItem
     * @return double
     */
    public double getMinPrice() {return minPrice;}

    /**
     * Gets the max price assigned to this DreamMenuItem
     * @return double
     */
    public double getMaxPrice() {return maxPrice;}

    /**
     * Get a String of this DreamMenuItem's properties.
     * Iterates through its Filters and formats them into a clean, multi-line string.
     *
     * @return String of its values for all Filters-associated values held
     */
    public String getInfo(){
        StringBuilder description = new StringBuilder(); //Main String holding long descriptions and simple extras
        //simple ingredients that are only either true (present) or false (not)
        StringBuilder extras = new StringBuilder("\nExtras: ");
        for(Filter key: filterMap.keySet()) {
            if(getFilter(key) instanceof Collection<?>){
                //Create section headers for collections with an indented arrow for their sub-types
                description.append("\n").append(key).append(":");
                for(Object x:((Collection<?>) getFilter(key)).toArray()) description.append("\n").append(" --> ").append(x);
            }
            //append filter names if true
            else if(getFilter(key).equals(true)) extras.append(key).append(", ");
            //append any values that are not false; implicitly includes values tied to enum classes
            else if(!getFilter(key).equals(false)) description.append("\n").append(key).append(": ").append(getFilter(key));
        }
        //extras String goes at the end of the description String
        description.append(extras.substring(0,extras.length()-2));
        return description.toString();
    }

    /**
     * Compares the keys and their attendant values (in the case of a match) between the calling
     * MenuItem's DreamMenuItem instance's filterMap and that of a
     * comparison (non-composed) DreamMenuItem instance.
     * <p>User choice of 'I don't mind' means the filter never got added to the criteria search item;
     * but it probably exists on the menu item.
     * <p>User choice of 'None' means the filter got added with a 'None' value to the user search item;
     * but the menu item will either have no key here (i.e. null) or a false boolean
     * <p> Adapted from Ariel Halperin, COSC120 A2, DreamPlant match method. Based on COSC120 matching methods by Andreas Shipley.
     * @param dreamMenuItem an instance of DreamMenuItem against which the menu item's properties should be compared
     * @return boolean true if two DreamMenuItem instances have overlap at the values of all their shared keys.
     */
    public boolean matches(DreamMenuItem dreamMenuItem) {

        // Store references here to contract following syntax.
        Map<Filter, Object> criteriaMap = dreamMenuItem.getAllFilters();

        // Presort entries by known Filter search optimisations.
        List<Map.Entry<Filter, Object>> sortedEntries = new ArrayList<>(criteriaMap.entrySet());
        // Ideas on Comparator and lambdas from https://www.informit.com/articles/article.aspx?p=3197227&seqNum=2
        sortedEntries.sort(Comparator.comparing(entry->entry.getKey().searchOrder()));
        // Most efficient Map lookup where keys and values are needed:
        // https://stackoverflow.com/questions/46898/how-do-i-efficiently-iterate-over-each-entry-in-a-java-map
        for (Map.Entry<Filter, Object> criteriaEntry : sortedEntries) {
            Filter criteriaKey = criteriaEntry.getKey();
            Object criteriaValue = criteriaEntry.getValue();

            Object menuItemValue = this.getFilter(criteriaKey);

            //***CHECK WHETHER THIS WAS A 'NONE' CHOICE--USER EXPLICITLY DOESN'T WANT ANY ITEMS WITH THIS FILTER***
            if (criteriaValue.equals(SpecialChoice.NONE)) {
                if (menuItemValue==null || menuItemValue.equals(false)) continue; // Item didn't have any of these->go to next item

                //NB. This shouldn't occur based on current menu load logic, but it's a cheap check for robustness.
                if (menuItemValue instanceof Collection<?> menuCollection
                        && (menuCollection.isEmpty() || menuCollection.contains(false))) continue;

                //Search was an explicit 'NONE' but the menu had a value here (other than boolean false).
                return false;
            }
            //None choice within a Collection
            if (criteriaValue instanceof Collection<?> criteriaCollection) {
                if (criteriaCollection.contains(SpecialChoice.NONE)) {
                    if (menuItemValue==null || menuItemValue.equals(false)) continue;
                    //NB. This shouldn't occur based on current menu load logic, but it's a cheap check for robustness.
                    if (menuItemValue instanceof Collection<?> menuCollection
                            && (menuCollection.isEmpty() || menuCollection.contains(false))) continue;

                    //Search Collection was an explicit 'NONE' but the menu had a value here (other than boolean false).
                    return false;
                }
            }

            //                      ***REGULAR FILTER CHECKS***

            //User wants a value but the menu item doesn't have that attribute at all. Outside of a
            //desired special choice of NONE, this will cause a NullPointerException if not handled.
            if (menuItemValue ==null) return false;

            // If both values are collections, discard if the menu item doesn't have >=1 of the criteria.
            if (menuItemValue instanceof Collection<?> menuCollection
                    && criteriaValue instanceof Collection<?> criteriaCollection) {
                // Treat Set contents and Collection types as totally unknown.
                Set<?> intersect = new HashSet<>(menuCollection);
                intersect.retainAll(criteriaCollection);
                if(intersect.size()==0) return false;
            }

            // Menu item has a collection, user wants a single item; discard if the menu item
            // doesn't contain that value
            else if ((menuItemValue instanceof Collection<?> menuCollection)
                    && (!menuCollection.contains(criteriaValue))) {
                return false;
            }

            //User selected multiple options but the inventory plant's single value is not one of those; discard.
            else if ((criteriaValue instanceof Collection<?> criteriaCollection)
                    && (!criteriaCollection.contains(menuItemValue))) {
                return false;
            }

            // Finally, if they're both singular, check for equality
            // No stress about primitives and == for equality, as they've been auto-wrapped on cast to Object:
            // https://stackoverflow.com/questions/709961/determining-if-an-object-is-of-primitive-type
            else if (!(menuItemValue instanceof Collection<?>) && !(criteriaValue instanceof Collection<?>)) {
                if (!menuItemValue.equals(criteriaValue)) {
                    return false;
                }
            }
        }
        // The user's criteria instance matched on all included attributes for >=1 value
        // choice. Note, any attributes selected as 'skip' by the user were not added, and so
        // implicitly matched.
        return true;
    }
}
