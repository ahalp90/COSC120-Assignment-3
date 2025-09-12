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
        List<Object> deduplicatedIngredientsForThisFilter = new ArrayList<>(allSubtypes);
        deduplicatedIngredientsForThisFilter.add("I don't mind");
        return List.copyOf(deduplicatedIngredientsForThisFilter);
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
