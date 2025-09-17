/**
 * Listener contract for responding to actions in the OrderCreationPanel
 * <p>Allows the OrderCreationPanel to communicate user events, like submitting the final order
 * or clicking to go back to the previous view, to its controller (i.e. OrderGui).
 */
public interface OrderCreationPanelListener {

    /**
     * Called when user clicks "Submit my Order" button and all needed data is present.
     * <p>Expects a final, complete Order record to pass to the model layer for final validation and writing to file.
     * @param thisOrder immutable Order record containing the customer's details and order.
     */
    void onFinalSubmitOrder(Order thisOrder);

    /**
     * Called when user clicks "Back to Item Selection" button.
     * <p>Listener is expected to handle the navigation logic to return the user to the previous view (i.e. ResultsPanel).
     */
    void onBackToMenuSelection();
}
