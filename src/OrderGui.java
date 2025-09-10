import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class OrderGui implements OrderingSystemListener {
    private final JFrame frame;
    private final CardLayout topCardLayout = new CardLayout();
    private final JPanel topCardPanel = new JPanel(topCardLayout);

    // The core Panel where filter selects are actually situated and operated.
    private final FilterEntryPanel filterEntryPanel;



    // Store subscribers to the GuiListener. Currently only intended to be MenuSearcher--so
    // Collection not strictly needed, but it's reasonable this could expand in the future.
    private final List<GuiListener> listeners = new ArrayList<>();

    //***CONSTANTS***
    private static final Dimension GUI_PREFERRED_SIZE = new Dimension(800, 450);
    private static final String SEARCH_BUTTON_IMG_PATH = "images/search-now.png";
    private static final String WELCOME_BACKGROUND_IMG_PATH = "images/welcome.png";
    private static final String SIDE_BANNER_IMG_PATH = "images/side_banner.png";
    private static final int WELCOME_CARD_ROWS = 8;
    private static final int WELCOME_CARD_COLS = 5;


    public OrderGui(Map<Filter, List<Object>> filterOptions) {
        //CREATE VIEW PANEL
        this.filterEntryPanel = new FilterEntryPanel(filterOptions);

        //SETUP MAIN FRAME
        frame = new JFrame("Overloaded Burgers");
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
    }

    private JPanel makeWelcomeCardPanel() {
        ImageIcon backgroundIcon = new ImageIcon(WELCOME_BACKGROUND_IMG_PATH);
        Image backgroundImage = backgroundIcon.getImage();

        //CREATE THE CORE PANEL; custom repaint logic for responsive resize of direct-painted background img
        JPanel welcomePanel = new JPanel(new GridBagLayout()) {
            //Custom repaint to paint directly to background, allowing responsive resize.
            @Override protected void paintComponent (Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        welcomePanel.setPreferredSize(GUI_PREFERRED_SIZE);//Preload with logical preferred size

        //CREATE THE 'Order Now' BUTTON AND ADD ACTION LISTENER
        JButton orderNow = makeImgOnlyButtonWithResize(
                "icons/order-now-button.png",
                "welcomePanel",
                new Dimension(GUI_PREFERRED_SIZE.width / WELCOME_CARD_COLS, GUI_PREFERRED_SIZE.height / WELCOME_CARD_ROWS)
        );
        orderNow.addActionListener(e -> switchCard("mainFilterPanel"));

        //TODO FIXME*****************************************************************************************************
        // Layout manage to precisely place the button where it's wanted--cell (3,8).
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = WELCOME_CARD_COLS;
        gbc.gridy = WELCOME_CARD_ROWS;
        gbc.weightx = 1.0 /  WELCOME_CARD_COLS;
        gbc.weighty = 1.0 /  WELCOME_CARD_ROWS;
        gbc.fill = GridBagConstraints.BOTH;
        
        welcomePanel.add(orderNow, gbc);

        return welcomePanel;
    }

    /**
     * Creates the main filtering view; composes core FilterEntryPanel onto a Panel with a side banner and search button.
     * @return JPanel with the mainBurgerFilterPanel
     */
    private JPanel makeMainBurgerFilterPanel() {
        //Easy layout with banner WEST, search button SOUTH and FilterEntryPanel CENTER
        JPanel mainBurgerFilterPanel = new JPanel(new BorderLayout());
        mainBurgerFilterPanel.setPreferredSize(GUI_PREFERRED_SIZE);

        // SIDE BANNER - ALWAYS VISIBLE
        //Create image with custom scaling to fit the width of its container while keeping its ratio.
        final BufferedImage sourceImage = loadBufferedImage(SIDE_BANNER_IMG_PATH);
        JLabel sideBanner = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Scale by width--looks less weird to pad top and bottom than sides.
                float scale = (float) getWidth() / sourceImage.getWidth();
                int scaledHeight = (int) (sourceImage.getHeight() * scale);
                int y = (getHeight() - scaledHeight) / 2; //Figure out the origin y coordinate.
                g.drawImage(sourceImage, 0, y, getWidth(), scaledHeight, this);
            }
        };
        // Set preferred size to 1/4 GUI as a hint to BorderLayout
        sideBanner.setPreferredSize(new Dimension((int)(GUI_PREFERRED_SIZE.width / 4), 0));

        JButton searchButton = makeSearchButton();

        mainBurgerFilterPanel.add(sideBanner, BorderLayout.WEST);
        mainBurgerFilterPanel.add(searchButton, BorderLayout.SOUTH);
        mainBurgerFilterPanel.add(this.filterEntryPanel.getCorePanel(), BorderLayout.CENTER);

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
        return searchButton;
    }

//    private JPanel filterSelectorsPanel() {
//        /*
//         THIS NEEDS TO FILTER FOR:
//         BURGER ONLY: BUN, SAUCE_S
//         SALAD ONLY: LEAFY_GREENS, CUCUMBER, DRESSING
//         BOTH: PICKLES, TOMATO, PROTEIN, PRICE, CHEESE
//
//         I DON'T MIND SELECTORS: BUN, SAUCE_S,LEAFY_GREENS, CUCUMBER, DRESSING, PROTEIN, TOMATO
//         ALLOWS EXPLICIT 'NONE' SELECTION: TOMATO, CUCUMBER, PICKLES, SAUCES, PROTEINS AND LEAFY_GREENS
//         */
//
//        // MAIN PANEL TO BE RETURNED
//        JPanel localParentPanel = new JPanel (new GridBagLayout());
//        localParentPanel.setPreferredSize(GUI_PREFERRED_SIZE);
//
//        JPanel sharedFiltersPanel = makeSharedFiltersPanel();
//        JPanel burgerFiltersPanel = makeBurgerFiltersPanel();
//        JPanel saladFiltersPanel = makeSaladFiltersPanel();
//
//        //INNER-WRAPPER JPANEL W/ CARDLAYOUT TO SWITCH BURGER & SALAD VIEWS
//        CardLayout cardLayout = new CardLayout();
//        JPanel typeSpecificFilterCardsPanel = new JPanel(cardLayout);
//        typeSpecificFilterCardsPanel.add(burgerFiltersPanel, "burgerFilters");
//        typeSpecificFilterCardsPanel.add(saladFiltersPanel, "saladFilters");
//
//        // TYPE PROMPT AND SELECTOR -- TYPE SELECTION GOVERNS CARD DISPLAY (BUN/SALAD PANELS)
//        JPanel typeFilterPanel = new JPanel(new BorderLayout());
//        JLabel itemTypePromptLabel = new JLabel(Filter.TYPE.filterPrompt());
//        JComboBox<Type> itemTypeSelector = new JComboBox<>(Type.values());
//
//        //Compose typeFilterPanel layout
//        typeFilterPanel.add(itemTypePromptLabel, BorderLayout.WEST);
//        typeFilterPanel.add(itemTypeSelector, BorderLayout.CENTER);
//
//
//        //                  *** COMPOSE THE PANEL TO RETURN ***
//        //local constants for gbc values
//        double typeWeightY = 0.1;
//        double typeSpecificFilterCardsPanelWeightY = 0.4;
//        double sharedFiltersPanelWeightY = 0.4;
//        int verticalGluePanelsNo = 2;
//        // Share remaining vertical space adding up to 1.0
//        double verticalGlueWeightY =
//                ((1 -  typeWeightY -  typeSpecificFilterCardsPanelWeightY - sharedFiltersPanelWeightY)
//                        / verticalGluePanelsNo);
//
//        GridBagConstraints gbc = new GridBagConstraints();
//        //All components at gridx with weightx 1.0, as this is a 1 column layout. All fill in both directions.
//        gbc.gridx = 0; gbc.weightx = 1.0;
//        gbc.fill = GridBagConstraints.BOTH;
//
//        //Heights are weight*100 due to the small size of the glue.
//        //FILTER PANEL
//        gbc.gridy = 0;
//        gbc.gridheight = (int) (typeWeightY * 100);
//        gbc.weighty = typeWeightY;
//        localParentPanel.add(typeFilterPanel, gbc);
//        //VERT GLUE #1
//        gbc.gridy = (int) (typeWeightY * 100);
//        gbc.gridheight = (int) (verticalGlueWeightY * 100);
//        gbc.weighty = verticalGlueWeightY;
//        localParentPanel.add(Box.createVerticalGlue(), gbc);
//        //TYPE-SPECIFIC PANEL
//        gbc.gridy = (int) ((typeWeightY + verticalGlueWeightY)* 100);
//        gbc.gridheight = (int) (typeSpecificFilterCardsPanelWeightY * 100);
//        gbc.weighty = typeSpecificFilterCardsPanelWeightY;
//        localParentPanel.add(typeSpecificFilterCardsPanel, gbc);
//        //VERT GLUE #2
//        gbc.gridy = (int) ((typeWeightY + verticalGlueWeightY + typeSpecificFilterCardsPanelWeightY) * 100);
//        gbc.gridheight = (int) (verticalGlueWeightY * 100);
//        gbc.weighty = verticalGlueWeightY;
//        localParentPanel.add(Box.createVerticalGlue(), gbc);
//        //SHARED FILTERS PANEL
//        gbc.gridy = (int) (int) ((typeWeightY + (verticalGlueWeightY*2) + typeSpecificFilterCardsPanelWeightY) * 100);
//        gbc.gridheight = (int) (sharedFiltersPanelWeightY * 100);
//        gbc.weighty = sharedFiltersPanelWeightY;
//        localParentPanel.add(sharedFiltersPanel, gbc);
//
//        return localParentPanel;
//    }


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

        MenuSearcher.processSearchResults(dreamMenuItem);
    }


    /**
     * Factory helper to build a Filter Map of Filter-Object from a FilterSelections record.
     * <p>Contains processing logic for empty and null selections; null (i.e. 'Skip-Any will do') selections are not added.
     * Selections irrelevant to the type are also not added--type relevance informed by Filter Smart Enum.
     * @param selections Record of selections by user.
     * @return unmodifiable view of the Map created
     */
    private Map<Filter, Object> buildFilterMapFromRecord(FilterSelections selections) {
        Map<Filter, Object> filterMap = new HashMap<>();
        Type selectedType = selections.selectedType();

        for (Filter filter : Filter.values()) {
            boolean isRelevant =
                    (selectedType == Type.BURGER && filter.isRelevantForBurger()) ||
                            (selectedType == Type.SALAD && filter.isRelevantForSalad());
            if (isRelevant) {
                Object value = switch (filter) {
                    case TYPE -> selections.selectedType();
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

    private BufferedImage loadBufferedImage(String imagePath) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.err.println("Error loading image from " + imagePath + "\n" +e.getMessage());
            System.exit(1);
        }
        if (bufferedImage.getWidth() <= 0 || bufferedImage.getHeight() <= 0) {
            System.err.println("Error: Image dimensions are invalid for image at " + imagePath);
            System.exit(1);
        }
        return bufferedImage;
    }

    private JButton makeImgOnlyButtonWithResize(String imagePath, Dimension size) {
        final BufferedImage sourceImage = loadBufferedImage(imagePath); //IntelliJ warning final for inner class

        JButton button = new JButton();
        button.setPreferredSize(size);
        button.setContentAreaFilled(false);

        // Code adapted from
        // https://stackoverflow.com/questions/78701695/how-do-i-scale-or-set-the-the-size-of-an-imageicon-in-java
        // The original code used Image, but I have opted for BufferedImage based on this:
        // https://docs.oracle.com/javase/tutorial/2d/images/index.html
        button.setIcon(new Icon() {
               @Override
               public void paintIcon(Component c, Graphics g, int x, int y) {
                   g.drawImage(sourceImage, x, y, c.getWidth(), c.getHeight(), c);
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


}
