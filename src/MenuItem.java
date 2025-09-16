import java.text.DecimalFormat;

/**
 * Created by Dr Andreas Shepley for COSC120 on 25/04/2025
 */

public final class MenuItem {

    //fields
    private final String menuItemIdentifier;
    private final String menuItemName;
    private final String description;
    private final double price;
    private final DreamMenuItem dreamMenuItem;

    //MenuItem is called to describe itself from within loops--constant DF
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    //constructor/s
    public MenuItem(String menuItemIdentifier,
                    String menuItemName,
                    double price,
                    String description,
                    DreamMenuItem dreamMenuItem) {
        this.menuItemIdentifier = menuItemIdentifier;
        this.menuItemName = menuItemName;
        this.price = price;
        this.description = description;
        this.dreamMenuItem=dreamMenuItem;
    }

    //getters
    public String getMenuItemIdentifier() {return menuItemIdentifier;}

    public String getMenuItemName() {return menuItemName;}

    public String getDescription() {return description;}

    public double getPrice() {return price;}

    public DreamMenuItem getDreamMenuItem(){ return dreamMenuItem;}

    public String getDreamMenuItemInfo() {return this.dreamMenuItem.getInfo();}
//
//    public boolean isMenuItemNotCustomItem() {return this.price != -1;}


    /**
     * Returns the fundamental type of this menu item (e.g. BURDER or SALAD).
     * <p>Needed by the GUI layer, specifically OrderCreationPanel, to show the relevant Unicode icon for type-specific display.
     * @return the Type enum value representing the item's type
     */
    public Object getDreamItemType() {return this.dreamMenuItem.getDreamItemType();}

    //menu info
    public String getMenuItemInformation(){

        String output = "\n*******************************************";

        output+="\n"+this.getMenuItemName()+" ("+getMenuItemIdentifier()+")"+ "\n"+this.getDescription() + "\n";
        output+=getDreamMenuItem().getInfo();

        return output+"\n\nPrice: $"+ DF.format(this.getPrice());
    }
}
