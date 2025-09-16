/**
 * Defines listener contract for responding to important user actions from the GUI.
 * <p>Allows GUI components (event source) to remain decoupled from the model (e.g.. MenuSearcher as event handler)</p>
 * <p>GUI broadcasts that an action has occurred, and the implementing class handles it.</p>
 */
public interface GuiListener {
    /**
     * Called when user has decided on their filters and wants to search for menu items.
     * <p>Listener is expected to take the user's search criteria, abstracted into a DreamMenuItem, and perform the
     * search against the whole menu's contents.
     * <p>The results of this search should then be sent back to the view through a separate listener interface
     * (i.e. OrderingSystemListener).
     * @param dreamMenuItem DreamMenuItem representing all the user's selected filters and price range.
     *                      Will not be null when passed in.
     */
    void performSearch(DreamMenuItem dreamMenuItem);

    /**
     * Called when the user confirms and submits their final order.
     * <p>The listener is responsible for processing the completed order. For example, by writing to a file.
     * <p>The sucess or failure of this should be communicated back to the view by an appropriate listener.
     *
     * @param order Order record containing all customer details, selected items and customisations.
     *              Will not be null when passed in.
     */
    void submitOrder(Order order);
}
