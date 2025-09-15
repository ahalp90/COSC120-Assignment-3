import java.util.List;

/**
 * Creates a new Order record. Custom constructor assigns an immutable List for menuItems
 * @param name String of the customer name
 * @param phoneNoAsString String of the customer's phone number
 * @param customisations String of their desired customisations
 * @param selectedCheese String representing their cheese preference
 * @param takeaway boolean true if takeaway, else false
 * @param menuItems List of MenuItems selected
 */
public record Order(String name,
                    String phoneNoAsString,
                    String customisations,
                    String selectedCheese,
                    boolean takeaway,
                    List<MenuItem> menuItems) {

    /**
     * Creates a new Order record. Custom constructor assigns an immutable List for menuItems
     * @param name String of the customer name
     * @param phoneNoAsString String of the customer's phone number
     * @param customisations String of their desired customisations
     * @param selectedCheese String representing their cheese preference
     * @param takeaway boolean true if takeaway, else false
     * @param menuItems List of MenuItems selected
     */
    public Order (String name,
                  String phoneNoAsString,
                  String customisations,
                  String selectedCheese,
                  boolean takeaway,
                  List<MenuItem> menuItems) {
        this.name = name;
        this.phoneNoAsString = phoneNoAsString;
        this.customisations = customisations;
        this.selectedCheese = selectedCheese;
        this.takeaway = takeaway;
        this.menuItems = List.copyOf(menuItems);
    }

}
