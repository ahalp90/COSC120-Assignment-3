import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MenuSearcher implements GuiListener {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    private static final String MENU_TXT_PATH = "./menu.txt";

    // Instance objects to interact with GUI
    private final Menu menu;
    // Store subscribers to the OrderingSystemListener. Currently only intended to be OrderGui--but List because could expand
    private final List<OrderingSystemListener> listeners = new ArrayList<>();


    /**
     * MenuSearcher constructor to create a MenuSearcher instance with a Menu instance
     * @param menu the Menu of menu items to populate and reference
     */
    public MenuSearcher(Menu menu) {
        this.menu = menu;
    }

    /**
     * Main OverLoaded Burgers menu search program entry point
     * @param args command-line arguments not required.
     */
    public static void main(String[] args) {
        Menu menu = loadMenu(MENU_TXT_PATH);
        MenuSearcher menuSearcher = new MenuSearcher(menu);

        // Get filter options for the GUI view
        Map<Filter, List<Object>> filterOptions = menuSearcher.getFilterOptions();

        //Create the GUI and set up the listeners. invokeLater for EDT for Swing Components.
        SwingUtilities.invokeLater(() -> {
            //Look and feel exception handling from https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html#available
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                   IllegalAccessException e) {
                System.err.println("Nimbus look and feel could not be loaded."
                        +"\nApplication will run with default Java Look and Feel.");
            }

            OrderGui gui = new OrderGui(filterOptions);

            //Add needed listeners so the MenuSearcher can observe the GUI and vice-versa
            gui.addGuiListener(menuSearcher);
            menuSearcher.addOrderingSystemListener(gui);
        });

//        DreamMenuItem dreamMenuItem = getFilters();
//        processSearchResults(dreamMenuItem);
    }




    /**
     * Populates an immutable Map with immutable Lists for all filter options.
     * Calls Menu public helper to compile ingredient lists as needed.
     * Otherwise populates directly from Enums
     * <p>Public to call by Main when instantiating GUI with Menu data.</p>
     * @return Map of Lists of Object representing filters.
     */
    public Map<Filter, List<Object>> getFilterOptions(){
        Map<Filter, List<Object>> filterOptions = new HashMap<>();

        for (Filter thisFilter : Filter.values()) {
            //No point getting the Booleans, as they're expected to be represented by radio-buttons or checkboxes.
            if (!thisFilter.valuesCanBeRepresentedByBooleanWrapper()){
                //Already returns an immutable List--just put it in the Map.
                filterOptions.put(thisFilter, this.menu.getAllIngredientTypes(thisFilter));
            }
        }

        return Map.copyOf(filterOptions);
    }


    public static Menu loadMenu(String filePath) {
        Menu menu = new Menu();
        Path path = Path.of(filePath);
        List<String> fileContents = null;
        try {
            fileContents = Files.readAllLines(path);
        }catch (IOException io){
            System.out.println("File could not be found");
            System.exit(0);
        }

        for(int i=1;i<fileContents.size();i++){

            String[] info = fileContents.get(i).split("\\[");
            String[] singularInfo = info[0].split(",");

            String cheesesRaw =  info[1].replace("]", "");
            String leafyGreensRaw = info[2].replace("]","");
            String saucesRaw = info[3].replace("]","");
            String description = info[4].replace("]","");

            String menuItemIdentifier = singularInfo[0];

            Type type = null;
            try{
                type = Type.valueOf(singularInfo[1].toUpperCase().strip());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Type data could not be parsed for item on line "+(i+1)
                        +". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }

            String menuItemName = capitaliseFirstLettersOnly(singularInfo[2].strip());

            double price = 0;
            try{
                price = Double.parseDouble(singularInfo[3]);
            }catch (NumberFormatException n){
                System.out.println("Error in file. Price could not be parsed for item on line "+(i+1)
                        +". Terminating. \nError message: "+n.getMessage());
                System.exit(0);
            }

            String bun = capitaliseFirstLettersOnly(singularInfo[4].toLowerCase().strip());

            Protein protein = null;
            try {
                protein = Protein.valueOf(singularInfo[5].toUpperCase());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Protein data could not be parsed for item on line "+(i+1)
                        +". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }

            boolean pickles = false;
            String picklesRaw = singularInfo[6].strip().toUpperCase();
            if(picklesRaw.equals("YES")) pickles = true;

            boolean cucumber = false;
            String cucumberRaw = singularInfo[7].strip().toUpperCase();
            if(cucumberRaw.equals("YES")) cucumber = true;

            boolean tomato = false;
            String tomatoRaw = singularInfo[8].strip().toUpperCase();
            if(tomatoRaw.equals("YES")) tomato = true;

            Dressing dressing = null;
            try {
                dressing = Dressing.valueOf(singularInfo[9].toUpperCase().replace(" ","_"));
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Dressing data could not be parsed for item on line "+(i+1)
                        +". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }

            //CHEESES - only add to the Cheeses set if there's a non-blank entry in the closure that's not NA
            Set<String> cheeses = new HashSet<>();
            for (String c : cheesesRaw.split(",")){
                String cheeseStrip = c.strip();
                if (!cheeseStrip.isBlank() && !cheeseStrip.equalsIgnoreCase("NA")){
                    cheeses.add(capitaliseFirstLettersOnly(cheeseStrip));
                }
            }

            //LEAFY GREENS - only add to the Leafy Greens set if there's a non-blank entry in the closure that's not NA
            Set<String> leafyGreens = new HashSet<>();
            for(String l: leafyGreensRaw.split(",")){
                String leafyGreensStrip = l.strip();
                if (!leafyGreensStrip.isBlank() && !leafyGreensStrip.equalsIgnoreCase("NA")){
                    leafyGreens.add(capitaliseFirstLettersOnly(leafyGreensStrip));
                }

            }

            //SAUCES - only add sauces if it's a non-blank and non-NA entry.
            Set<Sauce> sauces = new HashSet<>();
            for(String s: saucesRaw.split(",")){
                String cleanSauce = s.strip().toUpperCase();
                if (!cleanSauce.isEmpty() && !cleanSauce.equalsIgnoreCase("NA")){
                    try{
                        sauces.add(Sauce.valueOf(cleanSauce));
                    } catch (IllegalArgumentException e){
                        System.out.println("Error in file. Sauce/s data could not be parsed for item on line "+(i+1)
                                +". Terminating. \nError message: "+e.getMessage());
                        System.exit(0);
                    }
                }
            }

            Map<Filter,Object> filterMap = new LinkedHashMap<>();

            filterMap.put(Filter.TYPE,type);
            filterMap.put(Filter.PROTEIN, protein);
            filterMap.put(Filter.PICKLES, pickles);
            filterMap.put(Filter.TOMATO, tomato);
            //Only add the cheeses set if it's not empty--otherwise it's meaningless
            if (!cheeses.isEmpty()) filterMap.put(Filter.CHEESE, Set.copyOf(cheeses));

            if(type.equals(Type.BURGER)){
                filterMap.put(Filter.BUN, bun);
                //only add the sauces set if it's not empty--otherwise it's meaningless;
                //currently all burgers have sauce, but a burger without sauce is conceivable
                if(!sauces.isEmpty()) filterMap.put(Filter.SAUCE_S,Set.copyOf(sauces));
            }

            if(type.equals(Type.SALAD)){
                filterMap.put(Filter.DRESSING,dressing);
                filterMap.put(Filter.LEAFY_GREENS,Set.copyOf(leafyGreens));
                filterMap.put(Filter.CUCUMBER, cucumber);
            }

            DreamMenuItem dreamMenuItem = new DreamMenuItem(filterMap);
            MenuItem menuItem = new MenuItem(menuItemIdentifier, menuItemName,price,description, dreamMenuItem);
            menu.addItem(menuItem);
        }
        return menu;
    }

    /**
     * Create customer order text file saved to system.
     * Check directory access, allocate an unused filename and write the file to the directory.
     * Calls a helper method to build the order String.
     * <p> Calls helpers to request error message display in GUI if relevant.
     * <p>Adapted from Ariel Halperin, COSC120 A1, writeCustomerOrderToTxt()</p>
     * <p>Note, currently only allows max 10000 orders per phone number.
     *
     * @param order the record holding all attribute values relevant to the order.
     * @return true if the order succeeded, false if it failed.
     */
    private boolean writeOrderToFile(Order order) {
        // Check write permissions for directory. "./" must exist because it's the program's root
        // directory, but if the write out path were moved then there should also be a dir.exists()
        // check.
        Path writeOutDir = Paths.get("./");
        if (!Files.isWritable(writeOutDir)) {
            String errorMsg = "Error: Directory " + writeOutDir + " is not writable.";
            System.err.println(errorMsg);
            notifyListenersOnOrderFailure(errorMsg);

            return false;
        }

        int requestNo = 0; // Start counting at 0 because do-while always runs at least once.

        // Short circuit the request to avoid unreasonable wait time. This problem would need to be
        // resolved if any user has ordered 10000 times off the same phone number (yay!).
        final int maxRequestsNo = 10000;
        String pathString;
        Path fullOutputPath;
        // Loop to find a valid filepath that doesn't overwrite an existing order file.
        do {
            requestNo++;
            pathString = "./Order_" + order.phoneNoAsString() + "_" + requestNo + ".txt";
            fullOutputPath = Paths.get(pathString);
        } while (Files.exists(fullOutputPath) || requestNo > maxRequestsNo);

        if (requestNo == maxRequestsNo) {
            System.err.println("When attempting to write order to output text, maxRequestsNo was "
                    + "exceeded for the user's phone number of " + order.phoneNoAsString() + ".");

            String errorMsg = "Error: It looks like you've ordered you've ordered " + maxRequestsNo
                    + " times with this phone number.\n"
                    + "You're amazing, but our ordering system is not built to handle this level of devotion."
                    + "\n\nPlease go speak with management to claim a prize if this is the case."
                    + "\nYou can still order off a different phone number while we wait for our "
                    + "dev team to fix this for you.";

            notifyListenersOnOrderFailure(errorMsg);
            return false;
        }
        String orderString = orderStringToWriteOut(order); // Helper method builds String.

        // WRITE OUT ORDER TO THE FILEPATH DETERMINED ABOVE
        try {
            Files.writeString(fullOutputPath, orderString);
        } catch (IOException e) {
            System.err.println("Error writing output file: " + fullOutputPath + "\n" + e.getMessage());

            String errorMsg = "Error: Your order could not be saved to our system. We're really sorry!"
                    + "\nYou're welcome to try again, or else go order at the front counter.";

            notifyListenersOnOrderFailure(errorMsg);
            return false;
        }
        System.out.println("Order has been saved to " + fullOutputPath + "\n");
        return true;
    }

    private void notifyListenersOnOrderSuccess(Order order){
        for (OrderingSystemListener listener : listeners) {
            listener.onOrderSubmissionSuccess(order);
        }
    }

    private void notifyListenersOnOrderFailure(String errorMessage) {
        for (OrderingSystemListener listener : listeners) {
            listener.onOrderSubmissionFailed(errorMessage);
        }
    }

    /**
     * Helper method to create a String formatted to meet the order details txt requirements.
     * Adapted from Ariel Halperin, COSC120 A1 orderStringToWriteOut()
     *
     * @param order record containing all attribute values necessary to record an order.
     * @return a String of the customer's order.
     */
    private static String orderStringToWriteOut(Order order) {
        //Adaptation if the customer is making multiple orders
        StringJoiner itemStrings = new StringJoiner("\n");
        for (MenuItem item : order.menuItems()) {
            itemStrings.add("\tItem: " + item.getMenuItemName() + (" (" + item.getMenuItemIdentifier() + ")"));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Order details:\n");
        sb.append("\tName: ").append(order.name())
                .append(" (").append(order.phoneNoAsString()).append(")\n");
        sb.append(itemStrings).append("\n\n");
        sb.append("Customisation:");
        //Don't add an extra blank line if nothing was written in the customisations box.
        if (order.customisations() != null && !order.customisations().isBlank()) {
            sb.append("\n").append(order.customisations());
        }
        sb.append("\n\n\n");
        sb.append("Selected Cheese: ").append(order.selectedCheese()).append("\n\n\n");
        sb.append("Please make this order to ").append(order.takeaway() ? "takeaway" : "have here").append(".");

        return sb.toString();
    }

    /**
     * Capitalise the first letter of each word in a string, and make following letters lowercase.
     * <p> Sends to uppercase some common abbreviations.
     * Adapted with minor modification from this tutorial:
     * https://www.geeksforgeeks.org/java/java-program-to-capitalize-the-first-letter-of-each-word-in-a-string/
     * Code copied verbatim from Ariel Halperin, UNE COSC120, Assignment 2,
     * MenuSearcher, capitaliseFirstLettersOnly(String input)
     *
     * @param input the String to be modified.
     * @return the String in the desired (capitalised[0]lowercase[1:]) format.
     */
    private static String capitaliseFirstLettersOnly(String input) {
        // Don't operate on null or blank strings.
        if (input == null || input.isBlank()) return input;

        Set<String> abbreviations = Set.of("NA", "N/A");

        String[] words = input.split("\\s+"); // Split words on whitespace of any length
        // Rebuild words in string, splitting char0 and the rest of the word.
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (abbreviations.contains(word.toUpperCase())) {
                sb.append(word.toUpperCase());
            } else if (word.length()>=3 && word.substring(0,2).equalsIgnoreCase("Gc")) {
                //WORDS BEGINNING WITH GC? should maintain their origin capitalisation at the third letter
                sb.append("G");
                sb.append("c");
                sb.append(word.charAt(2));
                if (word.length() > 3) {
                    sb.append(word.substring(3).toLowerCase());
                }
            } else {
                sb.append(Character.toUpperCase(word.charAt(0)));
                // Only apply to words with more than one letter. Avoid possible IndexOutOfBoundsException.
                if (word.length() > 1) sb.append(word.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim(); //trims last whitespace and any leading whitespace.
    }

    /**
     * Return an immutable List of all MenuItems held in the MenuSearcher's Menu
     * @return immutable List of MenuItem
     */
    public List<MenuItem> getAllMenuItems() {
        return List.copyOf(this.menu.getMenuItems());
    }

    //              ***LISTENER INTERFACE INTERACTION METHODS***

    public void addOrderingSystemListener(OrderingSystemListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void performSearch(DreamMenuItem dreamMenuItem) {
        List<MenuItem> matching = menu.findMatch(dreamMenuItem);

        if (matching != null && !matching.isEmpty()) {
            notifyListenersOnSearchResults(matching);
        } else {
            notifyListenersOnNoMatchesFound(dreamMenuItem);
        }
    }

    private void notifyListenersOnSearchResults(List<MenuItem> matching) {
        for (OrderingSystemListener listener : listeners) {
            listener.onSearchResults(matching);
        }
    }

    private void notifyListenersOnNoMatchesFound(DreamMenuItem dreamMenuItem) {
        for (OrderingSystemListener listener : listeners) {
            listener.onNoMatchesFound(this.getAllMenuItems());
        }
    }

    @Override
    public void submitOrder(Order order) {
        boolean success = writeOrderToFile(order);

        if (success) {
            notifyListenersOnOrderSuccess(order);
        }
    }

}