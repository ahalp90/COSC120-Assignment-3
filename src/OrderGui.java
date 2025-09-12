import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class OrderGui implements OrderingSystemListener, ResultsPanelListener, PersonalDetailsListener {
    private final JFrame frame;
    private final CardLayout topCardLayout = new CardLayout();
    private final JPanel topCardPanel = new JPanel(topCardLayout);

    // The core Panel where filter selects are actually situated and operated.
    private final FilterEntryPanel filterEntryPanel;
    private final ResultsPanel resultsPanel;
    private final PersonalDetailsPanel detailsPanel;



    // Store subscribers to the GuiListener. Currently only intended to be MenuSearcher--so
    // Collection not strictly needed, but it's reasonable this could expand in the future.
    private final List<GuiListener> listeners = new ArrayList<>();

    //***CONSTANTS***
    private static final Dimension GUI_PREFERRED_SIZE = new Dimension(800, 450);
    private static final String SEARCH_BUTTON_IMG_PATH = "images/search-now.png";
    private static final String WELCOME_BACKGROUND_IMG_PATH = "images/welcome-background.png";
    private static final String SIDE_BANNER_IMG_PATH = "images/side_banner.png";

    private static final BufferedImage FRAME_ICON_IMAGE =
            ImgAndButtonUtilities.loadBufferedImage("images/overloaded_burgers_graphic_small.png");


    public OrderGui(Map<Filter, List<Object>> filterOptions) {
        //CREATE VIEW PANELS
        this.filterEntryPanel = new FilterEntryPanel(filterOptions);

        this.resultsPanel = new ResultsPanel();
        this.resultsPanel.setResultsPanelListener(this); //register as listener

        this.detailsPanel = new PersonalDetailsPanel();
        this.detailsPanel.setPersonalDetailsListener(this); //register as listener

        //SETUP MAIN FRAME
        frame = new JFrame("Overloaded Burgers");
        frame.setIconImage(FRAME_ICON_IMAGE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(GUI_PREFERRED_SIZE);
        frame.setLayout(new BorderLayout()); //allow responsive resize.
        frame.setResizable(true);
        frame.setLocationRelativeTo(null); //centre on screen

        //FINISH PUTTING TOGETHER THE MAIN VIEW
        composeTopCardPanel();
        frame.add(topCardPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    public void composeTopCardPanel(){
        topCardPanel.add(makeWelcomeCardPanel(), "welcomePanel");
        topCardPanel.add(makeMainBurgerFilterPanel(), "mainFilterPanel");
        topCardPanel.add(resultsPanel.getCorePanel(), "resultsPanel");
        topCardPanel.add(detailsPanel.getCorePanel(), "detailsPanel");
    }

    private JPanel makeWelcomeCardPanel() {
        //LOCAL CONSTANTS FOR LAYOUT MANAGEMENT
        int WELCOME_CARD_ROWS = 8;
        int WELCOME_CARD_COLS = 5;

        Image backgroundImage = ImgAndButtonUtilities.loadBufferedImage(WELCOME_BACKGROUND_IMG_PATH);

        //CREATE THE CORE PANEL; custom repaint logic for responsive resize of direct-painted background img
        JPanel welcomePanel = new JPanel(new GridLayout(WELCOME_CARD_ROWS, 1)) {
            //Custom repaint to paint directly to background, allowing responsive resize.
            //Code ideas as cited in Welcome Button comments
            @Override protected void paintComponent (Graphics g) {
                // Clear background and set-up non-custom aspects of the JPanel
                // https://docs.oracle.com/javase/tutorial/uiswing/painting/closer.html
                super.paintComponent(g);

                // Set Rendering hints to make it scale nicely
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                //Actually draw it!
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

                g2d.dispose();
            }
        };

        welcomePanel.setPreferredSize(GUI_PREFERRED_SIZE);//Preload with logical preferred size

        //CREATE THE 'Order Now' BUTTON AND ADD ACTION LISTENER
        JButton orderNow = makeImgOnlyButtonWithResize(
                "images/order-now-button.png",
                new Dimension((int)(GUI_PREFERRED_SIZE.width / WELCOME_CARD_COLS),
                        (int)(GUI_PREFERRED_SIZE.height / WELCOME_CARD_ROWS))
        );
        orderNow.setBorderPainted(false); //get rid of the default rectangular border
        orderNow.addActionListener(e -> switchCard("mainFilterPanel"));


        //                      *** LAYOUT MANAGEMENT ***

        //POSITION BUTTON AT THE BASE OF THE GRIDBAGLAYOUT USING THE INVISIBLE FILLER TO SQUISH IT INTO POSITION.
        //Button horizontally centred and vertically bottom row.

        //Create blank labels that occupy all but the last row of the parent's grid layout
        JLabel[] blankRowLabels = new JLabel[WELCOME_CARD_ROWS-1];
        for (int i = 0; i < WELCOME_CARD_ROWS-1; i++) {
            blankRowLabels[i] = new JLabel();
            blankRowLabels[i].setOpaque(false);
            welcomePanel.add(blankRowLabels[i]);
        }


        //Arrange the final row in a self-contained Panel to center the button
        JPanel centredButtonPanel = new JPanel(new GridLayout(1, WELCOME_CARD_COLS));
        centredButtonPanel.setOpaque(false);
        // Make an array of invisible JLabels; number 0 to push the button down to the south, and 1
        // and 2 to horizontally centre it on its row.
        JLabel[] blankColLabels = new JLabel[WELCOME_CARD_COLS];
        for (int i = 0; i < blankColLabels.length; i++) {
            blankColLabels[i] = new JLabel();
            blankColLabels[i].setOpaque(false);
            if (i == 2) {
                centredButtonPanel.add(orderNow);
            } else {
                centredButtonPanel.add(blankColLabels[i]);
            }
        }

        welcomePanel.add(centredButtonPanel);

        return welcomePanel;
    }

    /**
     * Creates the main filtering view; composes core FilterEntryPanel onto a Panel with a side banner and search button.
     * <p>Uses JSplitPane to achieve layout split.
     * <p>Ideas from: https://stackoverflow.com/questions/44027958/which-java-layout-is-suitable-for-my-layout
     * and https://docs.oracle.com/javase/tutorial/uiswing/components/splitpane.html
     * @return JPanel with the mainBurgerFilterPanel
     */
    private JPanel makeMainBurgerFilterPanel() {
        // SIDE BANNER - ALWAYS VISIBLE
        //Create image with custom scaling to fit the width of its container while keeping its ratio.
        final BufferedImage sourceImage = ImgAndButtonUtilities.loadBufferedImage(SIDE_BANNER_IMG_PATH);
        JLabel sideBanner = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                //Responsive resizing image and rendering hint ideas as for welcome button and welcome background jpanel
                super.paintComponent(g);

                // Set Rendering hints to make it scale nicely
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                // Scale by width--looks less weird to pad top and bottom than sides.
                float scale = (float) getWidth() / sourceImage.getWidth();
                int scaledHeight = (int) (sourceImage.getHeight() * scale);
                int y = (getHeight() - scaledHeight) / 2; //Figure out the origin y coordinate.

                //Actually draw it
                g2d.drawImage(sourceImage, 0, y, getWidth(), scaledHeight, this);

                g2d.dispose();
            }
        };

        JButton searchButton = makeSearchButton();
        JPanel filterPanel = this.filterEntryPanel.getCorePanel(); //local variable name to make it neater below.

        //Set minimum dimensions (token value) so that the layout manager properly respects the resize weights.
        sideBanner.setMinimumSize(new Dimension(1,1));
        filterPanel.setMinimumSize(new Dimension(1,1));
        searchButton.setMinimumSize(new Dimension(1,1));


        //This is the right side, it's a vertical split between the filter panel and the search button
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filterPanel, searchButton);
        rightSplit.setResizeWeight(0.85);
        rightSplit.setDividerSize(0);
        rightSplit.setBorder(null);
        rightSplit.setEnabled(false);

        //Force the divider to be in the correct spot from startup
        //The invokeLater feels like it shouldn't be necessary since this GUI is already on the EDT, but Swing is a pig.
        SwingUtilities.invokeLater(() -> {rightSplit.setDividerLocation(0.85);});


        //This is a horizontal split to put the banner on the left and the above components on the right.
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBanner, rightSplit);
        mainSplit.setResizeWeight(0.3);
        mainSplit.setDividerSize(0);
        mainSplit.setBorder(null);
        mainSplit.setEnabled(false);

        SwingUtilities.invokeLater(() -> {mainSplit.setDividerLocation(0.3);});

        //Finally put it all together and return the panel.
        JPanel mainBurgerFilterPanel = new JPanel(new BorderLayout());
        mainBurgerFilterPanel.setPreferredSize(GUI_PREFERRED_SIZE);
        mainBurgerFilterPanel.add(mainSplit, BorderLayout.CENTER);

        return mainBurgerFilterPanel;
    }

    /**
     * Factory helper to make a search button with an image that responsively resizes.
     * <p>Preferred size (main frame height /10).
     * <p>Has action listener attached to perform search on click.
     * @return JButton for searching
     */
    private JButton makeSearchButton() {
        JButton searchButton = makeImgOnlyButtonWithResize(
                SEARCH_BUTTON_IMG_PATH,
                new Dimension(0, (int)(GUI_PREFERRED_SIZE.height/10))
        );
        searchButton.addActionListener(e -> performSearch());
        searchButton.setBorderPainted(false); //get rid of the default rectangular border

        return searchButton;
    }

    private void performSearch() {
        //Get raw data from the core view panel
        FilterSelections selections = filterEntryPanel.getFilterSelections();

        String minPriceRaw = selections.minPrice();
        String maxPriceRaw = selections.maxPrice();

        // VALIDATE PRICE INPUT FIELDS
        if (!InputValidators.isValidPrice(minPriceRaw) || !InputValidators.isValidPrice(maxPriceRaw)) {
            JOptionPane.showMessageDialog(
                    frame, InputValidators.ERROR_INVALID_PRICE_FORMAT, "Price Input Error", JOptionPane.ERROR_MESSAGE);
            return; //Early terminate the search.
        }
        // Safely parse the known-valid prices
        double minPrice = Double.parseDouble(minPriceRaw);
        double maxPrice = Double.parseDouble(maxPriceRaw);

        if (maxPrice<minPrice) {
            JOptionPane.showMessageDialog(
                    frame, "Max Price cannot be less than Min Price.", "Price Input Error", JOptionPane.ERROR_MESSAGE);
            return; //Early terminate the search.
        }

        DreamMenuItem dreamMenuItem =
                new DreamMenuItem(
                        buildFilterMapFromRecord(selections), minPrice, maxPrice);

        //GUI's done processing logic for now--back to being a view--pass off to the relevant listener (MenuSearcher).
        for (GuiListener listener : listeners) {
            listener.performSearch(dreamMenuItem);
        }
    }


    /**
     * Private helper to translate FilterSelections Record to the required format for search by DreamMenuItem.
     * <p>Contains processing logic for empty and null selections; null (i.e. 'Skip-Any will do') selections are not added.
     * Selections irrelevant to the type are also not added--type relevance informed by Filter Smart Enum.
     * @param selections FilterSelections Record of selections by user.
     * @return unmodifiable view of the Map created, key Filter, value Object.
     */
    private Map<Filter, Object> buildFilterMapFromRecord(FilterSelections selections) {
        Map<Filter, Object> filterMap = new HashMap<>();

        Type selectedType = selections.selectedType(); //local var to avoid isRelevant lookup

        for (Filter filter : Filter.values()) {
            boolean isRelevant =
                    (selectedType == Type.BURGER && filter.isRelevantForBurger()) ||
                            (selectedType == Type.SALAD && filter.isRelevantForSalad());
            if (isRelevant) {
                Object value = switch (filter) {
                    case TYPE -> selectedType;
                    case BUN -> selections.selectedBun();
                    case SAUCE_S -> selections.selectedSauces().isEmpty() ? null : selections.selectedSauces();
                    case DRESSING -> selections.selectedDressing();
                    case LEAFY_GREENS -> selections.selectedLeafyGreens().isEmpty() ? null : selections.selectedLeafyGreens();
                    case PROTEIN ->  selections.selectedProteins().isEmpty() ? null : selections.selectedProteins();
                    case TOMATO -> selections.tomatoSelection();
                    case CUCUMBER -> selections.cucumberSelection();
                    case PICKLES -> selections.pickleSelection() ? true : null;
                    case CHEESE -> selections.selectedCheese();
                };

                if (value != null) filterMap.put(filter, value);
            }
        }
        return Collections.unmodifiableMap(filterMap);
    }

    private JButton makeImgOnlyButtonWithResize(String imagePath, Dimension size) {
        final BufferedImage sourceImage = ImgAndButtonUtilities.loadBufferedImage(imagePath); //IntelliJ warning final for inner class

        JButton button = new JButton();
        button.setPreferredSize(size);
        button.setContentAreaFilled(false);

        // Code adapted from
        // https://stackoverflow.com/questions/78701695/how-do-i-scale-or-set-the-the-size-of-an-imageicon-in-java
        // The original code used Image, but I have opted for BufferedImage based on this:
        // https://docs.oracle.com/javase/tutorial/2d/images/index.html
        // Rendering hints inspired by my work on a Java Chess gui and also this post here:
        // https://stackoverflow.com/questions/59431324/java-how-to-make-an-antialiasing-line-with-graphics2d
        button.setIcon(new Icon() {
           @Override
           public void paintIcon(Component c, Graphics g, int x, int y) {
               Graphics2D g2d = (Graphics2D) g.create();

               g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
               g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
               g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

               g2d.drawImage(sourceImage, x, y, c.getWidth(), c.getHeight(), c);

               g2d.dispose();
           }

           @Override
           public int getIconWidth() {
               return button.getWidth();
           }

           @Override
           public int getIconHeight() {
               return button.getHeight();
           }
           });

        return button;
    }


    /**
     * Helper to switch the current main card JPanel in the GUI.
     * @param cardName String of the card name to display
     */
    public void switchCard(String cardName) {
        topCardLayout.show(topCardPanel, cardName);
    }

    //              ***LISTENER INTERFACE INTERACTION METHODS***

    public void addGuiListener(GuiListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void onSearchResults(List<MenuItem> matches) {
        resultsPanel.displayItems(matches, "You've matched! Here are your results:");
        switchCard("resultsPanel");
    }

    @Override
    public void onNoMatchesFound(List<MenuItem> fullMenu) {
        String title = "Sorry, no matches were found. Here is our full menu:";
        resultsPanel.displayItems(fullMenu, title);
        switchCard("resultsPanel");
    }

    @Override
    public void onBackButtonPressed() {
        switchCard("mainFilterPanel");
    }

//TODO OLD ONPROCEEDTODETAILS CODE
        //        StringBuilder sb = new StringBuilder();
//        sb.append("You have added the following items to your order:");
//
//        Iterator<MenuItem> iterator = selectedItems.iterator();
//        int counter = 0;
//
//        while (iterator.hasNext()) {
//            MenuItem item = iterator.next();
//            sb.append("\n ");
//
//            //PREPEND THE MESSAGE WITH A HAMBURGER (EVEN) OR SALAD (ODD) FOR EACH ITEM.
//            // This was possibly not the most efficient way to spend my time...
//            if (counter % 2 == 0) {
//                sb.append("\uD83C\uDF54 \t");
//            } else {
//                sb.append("\uD83E\uDD57 \t");
//            }
//            sb.append(item.getMenuItemName());
//
//            counter++;
//        }
//
//        JOptionPane.showMessageDialog(
//                frame,
//                sb.toString() + "\n\n Now just input your details and your order will be on its way.",
//                "Items Added",
//                JOptionPane.INFORMATION_MESSAGE
//        );

        //Tell the details panel what was ordered


    @Override
    public void onBackToMenuSelection() {
        switchCard("resultsPanel"); //Invoked from personal details panel; goes back to results panel
    }

    @Override
    public void onFinalSubmitOrder(String name, String phone, String email) {
        if (!InputValidators.isFullName(name)) {
            JOptionPane.showMessageDialog(
                    frame, InputValidators.ERROR_INVALID_NAME, "Invalid name", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!InputValidators.isValidPhoneNo(phone)) {
            JOptionPane.showMessageDialog(
                    frame, InputValidators.ERROR_INVALID_PHONE, "Invalid phone", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!InputValidators.isValidEmail(email)) {
            JOptionPane.showMessageDialog(
                    frame, InputValidators.ERROR_INVALID_EMAIL, "Invalid email", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(
                frame,
                "Order submitted successfully!\n\nName: " + name + "\nPhone: " + phone,
                "Order Confirmed",
                JOptionPane.INFORMATION_MESSAGE);

        //TODO SAVE TO FILE AND GO BACK TO SELECTION TO ADD. PROBABLY STORE LISTOF MENU ITEMS IN FIELD OF GUI.
    }

    public void onProceedToDetails(List<MenuItem> selectedItems) {
        detailsPanel.displayOrderSummary(selectedItems); //Tell details panel what was ordered
        switchCard("detailsPanel");
    }

    // PUBLIC GETTERS

    /**
     * Public static helper so any relevant class' component can find the preferred GUI size; single source of truth
     * @return copy of the Dimension of the GUI's preferred size
     */
    public static Dimension getGuiPreferredSize() {
        return new Dimension(GUI_PREFERRED_SIZE);
    }
}
