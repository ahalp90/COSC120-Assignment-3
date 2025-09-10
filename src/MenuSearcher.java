import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MenuSearcher implements GuiListener {
    /**
     * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
     */
    private static final String filePath = "./menu.txt";
    private static final Icon icon = new ImageIcon("./overloaded_burgers_graphic_small.png");
    private static Menu menu;
    private static final String appName = "Overloaded Burgers";

    // Store subscribers to the OrderingSystemListener. Currently only intended to be OrderGui--so
    // Collection not strictly needed, but it's reasonable to imagine that GUI classes subscribed
    // could expand in the future.
    private static final List<OrderingSystemListener> listeners = new ArrayList<>();

    public static void main(String[] args) {
        menu = loadMenu(filePath);
        OrderGui gui = new OrderGui(getFilterOptions());
        DreamMenuItem dreamMenuItem = getFilters();
        processSearchResults(dreamMenuItem);
        System.exit(0);
    }

    /**
     * Populates an immutable map with immutable Lists for all filter options.
     * Calls Menu public helper to compile ingredient lists as needed.
     * Otherwise populates directly from Enums
     * @return Map of Lists of Object representing filters.
     */
    private static Map<Filter, List<Object>> getFilterOptions(){
        Map<Filter, List<Object>> filterOptions = new HashMap<>();

        filterOptions.put(Filter.DRESSING, List.copyOf(Arrays.asList(Dressing.values())));
        filterOptions.put(Filter.PROTEIN, List.copyOf(Arrays.asList(Protein.values())));
        filterOptions.put(Filter.SAUCE_S, List.copyOf(Arrays.asList(Sauce.values())));
        filterOptions.put(Filter.TYPE, List.copyOf(Arrays.asList(Type.values())));
        //Add options for filters who need their values read from menu because they can't be
        //identified by their own Enums or by a Boolean/boolean
        for (Filter thisFilter : Filter.values()) {
            if (thisFilter.needsMenuDataForSelectorOptions()){
                filterOptions.put(thisFilter, List.copyOf(menu.getAllIngredientTypes(thisFilter)));
            }
        }

        return Map.copyOf(filterOptions);
    }

    public static DreamMenuItem getFilters(){

        Map<Filter,Object> filterMap = new LinkedHashMap<>();
        String[] options = {"Yes", "No", "I don't mind"};

        Type type = (Type) JOptionPane.showInputDialog(null,"Which menu item would you like?",appName, JOptionPane.QUESTION_MESSAGE,null, Type.values(), Type.BURGER);
        if(type==null)System.exit(0);
        filterMap.put(Filter.TYPE,type);

        if(type==Type.BURGER) {
            Object[] allBuns = menu.getAllIngredientTypes(Filter.BUN).toArray();
            String bunType = (String) JOptionPane.showInputDialog(null, "Please select your preferred bun type:", appName, JOptionPane.QUESTION_MESSAGE, icon, allBuns, "");
            if (bunType == null) System.exit(0);
            if(!bunType.equals(allBuns[allBuns.length-1])) filterMap.put(Filter.BUN, bunType);

            Set<Sauce> dreamSauces = new HashSet<>();
            int choosingSauces=0;
            while(choosingSauces==0) {
                Sauce dreamSauce = (Sauce) JOptionPane.showInputDialog(null, "Please select your preferred sauce:", appName, JOptionPane.QUESTION_MESSAGE, icon, Sauce.values(), Sauce.TOMATO);
                if (dreamSauce == null) System.exit(0);
                if(dreamSauce.equals(Sauce.NA)) {
                    dreamSauces=new HashSet<>();
                    break;
                }
                else dreamSauces.add(dreamSauce);
                choosingSauces = JOptionPane.showConfirmDialog(null,"Would you like to add another sauce?",appName, JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,icon);
                if(choosingSauces==2) System.exit(0);
            }
            if(dreamSauces.size()>0) filterMap.put(Filter.SAUCE_S,dreamSauces);
        }

        if(type==Type.SALAD){
            Object[] allLeafyGreens = menu.getAllIngredientTypes(Filter.LEAFY_GREENS).toArray();
            Set<String> dreamLeafyGreens = new HashSet<>();
            int choosingLeafyGreens=0;
            while(choosingLeafyGreens==0) {
                String leafyGreens = (String) JOptionPane.showInputDialog(null, "Please select your preferred leafy greens:", appName, JOptionPane.QUESTION_MESSAGE, icon, allLeafyGreens, "");
                if (leafyGreens == null) System.exit(0);
                if(!leafyGreens.equals(allLeafyGreens[allLeafyGreens.length-1])) dreamLeafyGreens.add(leafyGreens);
                else break;
                choosingLeafyGreens = JOptionPane.showConfirmDialog(null,"Would you like to add another leafy greens choice?",appName, JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,icon);
                if(choosingLeafyGreens==2) System.exit(0);
            }
            if(dreamLeafyGreens.size()>0) filterMap.put(Filter.LEAFY_GREENS, dreamLeafyGreens);

            boolean cucumber=false;
            int cucumberSelection = JOptionPane.showOptionDialog(null,"Would you like cucumber?",appName, JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,icon,options,options[0]);
            if(cucumberSelection==0) cucumber = true;
            if(cucumberSelection==-1) System.exit(0);
            if(cucumberSelection!=2)filterMap.put(Filter.CUCUMBER, cucumber);

            Dressing dressing = (Dressing) JOptionPane.showInputDialog(null, "Please select your preferred dressing:", appName, JOptionPane.QUESTION_MESSAGE, icon, Dressing.values(), Dressing.FRENCH);
            if (dressing == null) System.exit(0);
            if(!dressing.equals(Dressing.NA)) filterMap.put(Filter.DRESSING, dressing);
        }

        Protein dreamProtein = (Protein) JOptionPane.showInputDialog(null,"Please select your preferred protein:",appName, JOptionPane.QUESTION_MESSAGE,icon, Protein.values(), Protein.BEEF);
        if(dreamProtein==null)System.exit(0);
        if(!dreamProtein.equals(Protein.NA)) {
            Set<Protein> dreamProteins = new HashSet<>();
            dreamProteins.add(dreamProtein);
            filterMap.put(Filter.PROTEIN,dreamProteins);
        }

        boolean tomato=false;
        int tomatoSelection = JOptionPane.showOptionDialog(null,"Would you like tomato?",appName, JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,icon,options,options[0]);
        if(tomatoSelection==0) tomato=true;
        if(tomatoSelection==-1) System.exit(0);
        if(tomatoSelection!=2) filterMap.put(Filter.TOMATO, tomato);

        boolean pickle=false;
        int pickleSelection = JOptionPane.showConfirmDialog(null,"Would you like pickles?",appName, JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,icon);
        if(pickleSelection==0) pickle = true;
        if(pickleSelection==-1) System.exit(0);
        if(pickleSelection!=2)filterMap.put(Filter.PICKLES, pickle);

        int minPrice=-1,maxPrice = -1;
        while(minPrice<0) {
            String userInput = JOptionPane.showInputDialog(null, "Please enter the lowest price", appName, JOptionPane.QUESTION_MESSAGE);
            if(userInput==null)System.exit(0);
            try {
                minPrice = Integer.parseInt(userInput);
                if(minPrice<0) JOptionPane.showMessageDialog(null,"Price must be >= 0.",appName, JOptionPane.ERROR_MESSAGE);
            }
            catch (NumberFormatException e){
                JOptionPane.showMessageDialog(null,"Invalid input. Please try again.", appName, JOptionPane.ERROR_MESSAGE);
            }
        }
        while(maxPrice<minPrice) {
            String userInput = JOptionPane.showInputDialog(null, "Please enter the highest price", appName, JOptionPane.QUESTION_MESSAGE);
            if(userInput==null)System.exit(0);
            try {
                maxPrice = Integer.parseInt(userInput);
                if(maxPrice<minPrice) JOptionPane.showMessageDialog(null,"Price must be >= "+minPrice,appName, JOptionPane.ERROR_MESSAGE);
            }
            catch (NumberFormatException e){
                JOptionPane.showMessageDialog(null,"Invalid input. Please try again.", appName, JOptionPane.ERROR_MESSAGE);
            }
        }
        DreamMenuItem dream = new DreamMenuItem(filterMap,minPrice,maxPrice);
        System.out.println(dream.getInfo());
        return new DreamMenuItem(filterMap,minPrice,maxPrice);
    }

    public static void processSearchResults(DreamMenuItem dreamMenuItem){
        List<MenuItem> matching = menu.findMatch(dreamMenuItem);
        MenuItem chosenItem = null;
        if(matching.size()>0) {
            Map<String, MenuItem> options = new HashMap<>();
            StringBuilder infoToShow = new StringBuilder("Matches found!! The following items meet your criteria: \n");
            for (MenuItem match : matching) {
                infoToShow.append(match.getMenuItemInformation());
                options.put(match.getMenuItemName(), match);
            }
            String choice = (String) JOptionPane.showInputDialog(null, infoToShow + "\n\nPlease select which item you'd like to order:", appName, JOptionPane.INFORMATION_MESSAGE, icon, options.keySet().toArray(), "");
            if(choice==null) System.exit(0);
            chosenItem = options.get(choice);
        }
        else{
            int custom = JOptionPane.showConfirmDialog(null, """
                    Unfortunately none of our items meet your criteria :(
                    \tWould you like to place a custom order?\s

                    **Price to be calculated at checkout and may exceed your chosen range**.""",appName, JOptionPane.YES_NO_OPTION);
            if(custom==0) chosenItem = new MenuItem(dreamMenuItem);
            else System.exit(0);
        }
        submitOrder(getUserContactInfo(),chosenItem);
        JOptionPane.showMessageDialog(null,"Thank you! Your order has been submitted. "+
                "Please wait for your name to be called out...",appName, JOptionPane.INFORMATION_MESSAGE);
    }

    public static Geek getUserContactInfo(){
        String name = JOptionPane.showInputDialog(null,"Please enter a name for the order.",appName, JOptionPane.QUESTION_MESSAGE);
        if(name==null) System.exit(0);
        long phoneNumber=0;
        while(phoneNumber==0) {
            try {
                String userInput = JOptionPane.showInputDialog(null, "Please enter your phone number. \nIt will be used as your order number.", appName, JOptionPane.QUESTION_MESSAGE);
                if(userInput==null) System.exit(0);
                phoneNumber = Long.parseLong(userInput);
            } catch (NumberFormatException e) {
                phoneNumber = Long.parseLong(JOptionPane.showInputDialog(null, "Invalid entry. Please enter your phone number.", appName, JOptionPane.ERROR_MESSAGE));
            }
            int length = String.valueOf(phoneNumber).length();
            if(length!=9) {
                phoneNumber=0;
                JOptionPane.showMessageDialog(null,"Invalid entry. Please enter your 10-digit phone number in the format 0412 123 345.",appName, JOptionPane.ERROR_MESSAGE);
            }
        }
        return new Geek(name,phoneNumber);
    }

    public static void submitOrder(Geek geek, MenuItem menuItem) {
        String filePath = geek.name().replace(" ","_")+"_"+menuItem.getMenuItemIdentifier()+".txt";
        Path path = Path.of(filePath);
        String lineToWrite = "Order details:\n\t" +
                "Name: "+geek.name()+
                " (0"+geek.orderNumber()+")";
        if(menuItem.getMenuItemIdentifier().equals("")) lineToWrite+="\n\nCUSTOM ORDER...\n"+menuItem.getMenuItemInformation();
        else lineToWrite+="\n\tItem: "+menuItem.getMenuItemName()+ " ("+menuItem.getMenuItemIdentifier()+")";

        try {
            Files.writeString(path, lineToWrite);
        }catch (IOException io){
            System.out.println("Order could not be placed. \nError message: "+io.getMessage());
            System.exit(0);
        }
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

            String leafyGreensRaw = info[2].replace("]","");
            String saucesRaw = info[3].replace("]","");
            String description = info[4].replace("]","");

            String menuItemIdentifier = singularInfo[0];

            Type type = null;
            try{
                type = Type.valueOf(singularInfo[1].toUpperCase().strip());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Type data could not be parsed for item on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }

            String menuItemName = singularInfo[2];

            double price = 0;
            try{
                price = Double.parseDouble(singularInfo[3]);
            }catch (NumberFormatException n){
                System.out.println("Error in file. Price could not be parsed for item on line "+(i+1)+". Terminating. \nError message: "+n.getMessage());
                System.exit(0);
            }

            String bun = singularInfo[4].toLowerCase().strip();

            Protein protein = null;
            try {
                protein = Protein.valueOf(singularInfo[5].toUpperCase());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Protein data could not be parsed for item on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
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
                System.out.println("Error in file. Dressing data could not be parsed for item on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }

            Set<String> leafyGreens = new HashSet<>();
            for(String l: leafyGreensRaw.split(",")){
                leafyGreens.add(l.toLowerCase().strip());
            }

            Set<Sauce> sauces = new HashSet<>();
            for(String s: saucesRaw.split(",")){
                Sauce sauce = null;
                try {
                    sauce = Sauce.valueOf(s.toUpperCase().strip());
                }catch (IllegalArgumentException e){
                    System.out.println("Error in file. Sauce/s data could not be parsed for item on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                    System.exit(0);
                }
                sauces.add(sauce);
            }

            Map<Filter,Object> filterMap = new LinkedHashMap<>();
            filterMap.put(Filter.TYPE,type);
            if(type.equals(Type.BURGER)){
                filterMap.put(Filter.BUN, bun);
                if(sauces.size()>0) filterMap.put(Filter.SAUCE_S,sauces);
            }
            Set<Protein> single_protein = new HashSet<>();
            single_protein.add(protein);
            filterMap.put(Filter.PROTEIN,single_protein);
            filterMap.put(Filter.PICKLES, pickles);
            filterMap.put(Filter.TOMATO, tomato);
            if(type.equals(Type.SALAD)){
                filterMap.put(Filter.DRESSING,dressing);
                filterMap.put(Filter.LEAFY_GREENS,leafyGreens);
                filterMap.put(Filter.CUCUMBER, cucumber);
            }

            DreamMenuItem dreamMenuItem = new DreamMenuItem(filterMap);
            MenuItem menuItem = new MenuItem(menuItemIdentifier, menuItemName,price,description, dreamMenuItem);
            menu.addItem(menuItem);
        }
        return menu;
    }

    /**
     * Gets the available options in the composed Menu for the given Filter value.
     * @param filter a Filter value representing the menu attribute whose collected deduplicated values are needed
     * @return  List of Objects--immutable and not-null--of all available options for the given Filter.
     * "I don't mind" is appended as the last item.
     */
    public  static List<Object> getAvailableOptions(Filter filter) {
        return List.copyOf(menu.getAllIngredientTypes(filter));
    }

}