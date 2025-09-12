import java.util.*;

public final class Menu {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    private final Set<MenuItem> menu = new HashSet<>();

    public void addItem(MenuItem menuItem){
        this.menu.add(menuItem);
    }

    public List<Object> getAllIngredientTypes(Filter filter){
        Set<Object> allSubtypes = new LinkedHashSet<>();
        for(MenuItem menuItem: menu){
            if(menuItem.getDreamMenuItem().getAllFilters().containsKey(filter)){
                var ingredientTypes = menuItem.getDreamMenuItem().getFilter(filter);
                if(ingredientTypes instanceof Collection<?>) allSubtypes.addAll((Collection<?>) ingredientTypes);
                else allSubtypes.add(menuItem.getDreamMenuItem().getFilter(filter));
            }
        }
        List<Object> deduplicatedIngredients = new ArrayList<>(allSubtypes);

        //sort alphabetically even though they're Object;
        //https://medium.com/@AlexanderObregon/javas-comparator-comparing-method-explained-342361288af6
        deduplicatedIngredients.sort(Comparator.comparing(Object::toString));
        // Add NONE as the second last option to relevant Lists
        if (filter.allowsNoneChoice()) deduplicatedIngredients.add(SpecialChoice.NONE);
        //Populate 'I don't mind'-type options for the relevant filters.
        if (filter.allowsDontMindChoice()) deduplicatedIngredients.add(filter.getDontMindValue());

        return List.copyOf(deduplicatedIngredients);
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
