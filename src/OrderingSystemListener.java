import java.util.List;

public interface OrderingSystemListener {

    void onSearchResults(List<MenuItem> matches);

    void onNoMatchesFound(DreamMenuItem originalDreamFilters);
}
