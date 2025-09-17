import java.util.List;

/**
 * Contract for a listener that responds to user actions from the ResultsPanel (e.g. OrderGui).
 * <p>Allows the ResultsPanel to communicate navigation button events to its controller.
 */
public interface ResultsPanelListener {

    /**
     * Call when use clicks the "Back to Search" button.
     * <p>Listener expected to handle navigation logic.</p>
     */
    void onBackButtonPressed();

    /**
     * Call when user selects one or more items and clicks the "Confirm Selection and Order" button.
     * <p>Listener expected to take the List of selected items and proceed to final order confirmation view.
     * @param selectedItems (non-empty) List of selected MenuItems.
     */
    void onProceedToDetails(List<MenuItem> selectedItems);
}
