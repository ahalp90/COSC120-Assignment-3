import java.util.*;

public class DreamMenuItem {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    private final Map<Filter,Object> filterMap;
    private final double minPrice;
    private final double maxPrice;

    public DreamMenuItem(Map<Filter, Object> filterMap, double minPrice, double maxPrice) {
        this.filterMap=new LinkedHashMap<>(filterMap);
        this.minPrice=minPrice;
        this.maxPrice=maxPrice;
    }
    public DreamMenuItem(Map<Filter, Object> filterMap) {
        this.filterMap=new LinkedHashMap<>(filterMap);
        this.minPrice=-1;
        this.maxPrice=-1;
    }

    public Map<Filter, Object> getAllFilters() {
        return new LinkedHashMap<>(filterMap);
    }
    public Object getFilter(Filter key){return getAllFilters().get(key);}
    public double getMinPrice() {
        return minPrice;
    }
    public double getMaxPrice() {
        return maxPrice;
    }

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
     * <p>'Skip' as 'any will do' is handled implicitly by not adding the relevant key to the comparison DreamMenuItem's
     * Map and performing negative conditional checks.
     * <p> Copied from Ariel Halperin, COSC120 A2, DreamPlant match method.
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
            // Got to get 'menuItemValue' anyway; implicitly check if the criteriaKey exists within the
            // menu item's Map. 1 step fewer than get + Map.containsKey(criteriaKey).
            // Idea per discussion at
            // https://stackoverflow.com/questions/14601016/is-using-java-map-containskey-redundant-when-using-map-get.
            // If not, exit early. Key in criteria but not inventory currently implausible, but cheap check.
            // *No other null handling necessary--the Map.copyOf implementation used doesn't allow nulls.*
            // However, if design changes made it likely that criteriaMap had keys that an inventory plant didn't,
            // it would be more efficient to do a contains lookup for all keys before beginning the iterator.
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
