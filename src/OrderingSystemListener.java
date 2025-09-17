import java.util.List;

/**
 * Defines the contract for the ordering system listener (i.e. OrderGui).
 * <p>Facilitates sensibly decoupled passing of data from the originating source (i.e. MenuSearcher) to
 * the OrderGui as central GUI controller, which can then act or pass to the relevant view as appropriate.
 */
public interface OrderingSystemListener {

    /**
     * Call when a search successfully finds matching menu items.
     * <p>Listener is expected to display the provided list of matches to the user, or call an appropriate view to do so.
     * @param matches List of MenuItems that matched the user search criteria. List will never be null or empty.
     */
    void onSearchResults(List<MenuItem> matches);

    /**
     * Call when a search completes with no matches.
     * <p>Listener is expected to inform the user that their search found no matches and should display
     * the provided full menu from which to seelct
     * @param fullMenu a List containing every MenuItem on the Menu. List will not be null.
     */
    void onNoMatchesFound(List<MenuItem> fullMenu);

    /**
     * Call when an order has been successfully submitted and saved.
     * <p>Listener is expected to show a confirmation message to the user, including relevant details of their Order.
     * @param order the Order that was successfully submitted.
     */
    void onOrderSubmissionSuccess(Order order);

    /**
     * Call when an attempt to submit an order failed.
     * <p>listener is expected to show an error message explaining why the order failed.
     * @param errorMessage String containing the relevant explanation of the error.
     */
    void onOrderSubmissionFailed(String errorMessage);
}
