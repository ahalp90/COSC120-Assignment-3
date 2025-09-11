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

    public boolean matches(DreamMenuItem dreamMenuItem){
        for(Filter key : dreamMenuItem.getAllFilters().keySet()) {
            if(this.getAllFilters().containsKey(key)){
                //For Collections, check for a non-empty intersection.
                if(getFilter(key) instanceof Collection<?> && dreamMenuItem.getFilter(key) instanceof Collection<?>){
                    Set<Object> intersect = new HashSet<>((Collection<?>) dreamMenuItem.getFilter(key));
                    intersect.retainAll((Collection<?>) getFilter(key));
                    if(intersect.size()==0) return false;
                }
                else{
                    //For single Objects, check direct equality
                    if(!this.getFilter(key).equals(dreamMenuItem.getFilter(key))) return false;
                }
            //ELSE: USER SPECIFIED FILTER DOES NOT EXIST IN MENU ITEM--EG. A BURGER LOADED WITHOUT LEAFYGREENS KEY.
            } else {
                return false;
            }
        }
        return true;
    }



}
