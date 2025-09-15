import java.util.List;

public interface OrderingSystemListener {

    void onSearchResults(List<MenuItem> matches);

    void onNoMatchesFound(List<MenuItem> fullMenu);

    void onOrderSubmissionSuccess(Order order);

    void onOrderSubmissionFailed(String errorMessage);
}
