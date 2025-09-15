import java.util.*;

public class DreamMenuItem {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    private final Map<Filter,Object> filterMap;
    private final double minPrice;
    private final double maxPrice;

    public DreamMenuItem(Map<Filter, Object> filterMap, double minPrice, double maxPrice) {
        this.filterMap=new HashMap<>(filterMap);
        this.minPrice=minPrice;
        this.maxPrice=maxPrice;
    }
    public DreamMenuItem(Map<Filter, Object> filterMap) {
        this.filterMap=new HashMap<>(filterMap);
        this.minPrice=-1;
        this.maxPrice=-1;
    }

    public Map<Filter, Object> getAllFilters() {
        return new HashMap<>(filterMap);
    }

    public Object getFilter(Filter key){return this.filterMap.get(key);}

    public Object getDreamItemType() {
        return this.filterMap.getOrDefault(Filter.TYPE, null);
    }

    public double getMinPrice() {return minPrice;}

    public double getMaxPrice() {return maxPrice;}

    public String getInfo(){
        StringBuilder description = new StringBuilder();
        StringBuilder extras = new StringBuilder("\nExtras: ");
        for(Filter key: filterMap.keySet()) {
            if(getFilter(key) instanceof Collection<?>){
                description.append("\n").append(key).append(":");
                for(Object x:((Collection<?>) getFilter(key)).toArray()) description.append("\n").append(" --> ").append(x);
            }
            else if(getFilter(key).equals(true)) extras.append(key).append(", ");
            else if(!getFilter(key).equals(false)) description.append("\n").append(key).append(": ").append(getFilter(key));
        }
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
