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

    private static final Dimension GUI_PREFERRED_SIZE = new Dimension(800, 450);
    // Store subscribers to the GuiListener. Currently only intended to be MenuSearcher--so
    // Collection not strictly needed, but it's reasonable this could expand in the future.
    private final List<GuiListener> listeners = new ArrayList<>();
    private static final int WELCOME_CARD_ROWS = 8;
    private static final int WELCOME_CARD_COLS = 5;



    private final Map<Filter, Object> filterSelections = new HashMap<>();


    public OrderGui() {
        frame = new JFrame("Overloaded Burgers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(GUI_PREFERRED_SIZE);
        frame.setLayout(new BorderLayout()); //allow responsive resize.
        frame.setResizable(true);
        frame.setLocationRelativeTo(null); //centre on screen


    }

    public JPanel composeTopCardPanel(){
        topCardPanel.add(makeWelcomeCardPanel());
    }

    private JPanel makeWelcomeCardPanel() {
        ImageIcon backgroundIcon = new ImageIcon("icons/menu-background.png");
        Image backgroundImage = backgroundIcon.getImage();
        //Use GridLayout to allow button placement. Button should go in (3, 8) relative to intended image
        JPanel welcomePanel = new JPanel(new GridBagLayout()) {
            //Custom repaint to paint directly to background, allowing responsive resize.
            @Override protected void paintComponent (Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        welcomePanel.setPreferredSize(GUI_PREFERRED_SIZE);//Preload with logical preferred size


        JButton orderNow = makeImgOnlyButtonWithResize(
                "icons/order-now-button.png",
                "welcomePanel",
                new Dimension(GUI_PREFERRED_SIZE.width / WELCOME_CARD_COLS, GUI_PREFERRED_SIZE.height / WELCOME_CARD_ROWS)
        );

        // Layout manage to precisely place the button where it's wanted--cell (3,8).
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = WELCOME_CARD_COLS; gbc.gridy = WELCOME_CARD_ROWS;
        gbc.weightx = 1.0 /  WELCOME_CARD_COLS; gbc.weighty = 1.0 /  WELCOME_CARD_ROWS;
        gbc.fill = GridBagConstraints.BOTH;
        
        welcomePanel.add(orderNow, gbc);

        return welcomePanel;
    }

    private JPanel makeMainBurgerFilterPanel() {
        //MAIN PANEL TO BE RETURNED (just 4 columns & small h/v-gap)
        //TODO: THIS HAS TO BE GRIDBAGLAYOUT FOR PROPORTIONATE X-WEIGHTING**********************************************
//        JPanel mainBurgerFilterPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        JPanel mainBurgerFilterPanel = new JPanel(new GridBagLayout());
        mainBurgerFilterPanel.setPreferredSize(GUI_PREFERRED_SIZE);

        // SIDE BANNER - ALWAYS VISIBLE
        //Create image with custom scaling to fit the width of its container while keeping its ratio.
        final BufferedImage sourceImage = loadBufferedImage("images/oop-burger.png");
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

        JPanel filterSelectorsPanel = filterSelectorsPanel();

        //ADD SUB-COMPONENTS TO MAIN PANEL TO BE RETURNED
        GridBagConstraints gbc = new GridBagConstraints();
        //layout constants
        gbc.gridy = 0; gbc.weighty = 1.0; gbc.gridheight = 1; gbc.fill = GridBagConstraints.BOTH;
        double


        return mainBurgerFilterPanel;
    }

    private JPanel filterSelectorsPanel() {
        /*
         THIS NEEDS TO FILTER FOR:
         BURGER ONLY: BUN, SAUCE_S
         SALAD ONLY: LEAFY_GREENS, CUCUMBER, DRESSING
         BOTH: PICKLES, TOMATO, PROTEIN, PRICE, CHEESE

         I DON'T MIND SELECTORS: BUN, SAUCE_S,LEAFY_GREENS, CUCUMBER, DRESSING, PROTEIN, TOMATO
         ALLOWS EXPLICIT 'NONE' SELECTION: TOMATO, CUCUMBER, PICKLES, SAUCES, PROTEINS AND LEAFY_GREENS
         */

        // MAIN PANEL TO BE RETURNED
        JPanel localParentPanel = new JPanel (new GridBagLayout());
        localParentPanel.setPreferredSize(GUI_PREFERRED_SIZE);

        JPanel sharedFiltersPanel = makeSharedFiltersPanel();
        JPanel burgerFiltersPanel = makeBurgerFiltersPanel();
        JPanel saladFiltersPanel = makeSaladFiltersPanel();

        //INNER-WRAPPER JPANEL W/ CARDLAYOUT TO SWITCH BURGER & SALAD VIEWS
        CardLayout cardLayout = new CardLayout();
        JPanel typeSpecificFilterCardsPanel = new JPanel(cardLayout);
        typeSpecificFilterCardsPanel.add(burgerFiltersPanel, "burgerFilters");
        typeSpecificFilterCardsPanel.add(saladFiltersPanel, "saladFilters");

        // TYPE PROMPT AND SELECTOR -- TYPE SELECTION GOVERNS CARD DISPLAY (BUN/SALAD PANELS)
        JPanel typeFilterPanel = new JPanel(new BorderLayout());
        JLabel itemTypePromptLabel = new JLabel(Filter.TYPE.filterPrompt());
        JComboBox<Type> itemTypeSelector = new JComboBox<>(Type.values());
        itemTypeSelector.addActionListener(e -> {
            Type selectedType = (Type) itemTypeSelector.getSelectedItem();
            //This could be simplified, but the explicit cases for both types makes it easier to add new types in future.
            if (selectedType == Type.BURGER) {
                cardLayout.show(typeSpecificFilterCardsPanel, "burgerFilters");
            } else if (selectedType == Type.SALAD) {
                cardLayout.show(typeSpecificFilterCardsPanel, "saladFilters");
            }
        });
        //Compose typeFilterPanel layout
        typeFilterPanel.add(itemTypePromptLabel, BorderLayout.WEST);
        typeFilterPanel.add(itemTypeSelector, BorderLayout.CENTER);


        //                  *** COMPOSE THE PANEL TO RETURN ***
        //local constants for gbc values
        double typeWeightY = 0.1;
        double typeSpecificFilterCardsPanelWeightY = 0.4;
        double sharedFiltersPanelWeightY = 0.4;
        int verticalGluePanelsNo = 2;
        // Share remaining vertical space adding up to 1.0
        double verticalGlueWeightY =
                ((1 -  typeWeightY -  typeSpecificFilterCardsPanelWeightY - sharedFiltersPanelWeightY)
                        / verticalGluePanelsNo);

        GridBagConstraints gbc = new GridBagConstraints();
        //All components at gridx with weightx 1.0, as this is a 1 column layout. All fill in both directions.
        gbc.gridx = 0; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        //Heights are weight*100 due to the small size of the glue.
        //FILTER PANEL
        gbc.gridy = 0;
        gbc.gridheight = (int) (typeWeightY * 100);
        gbc.weighty = typeWeightY;
        localParentPanel.add(typeFilterPanel, gbc);
        //VERT GLUE #1
        gbc.gridy = (int) (typeWeightY * 100);
        gbc.gridheight = (int) (verticalGlueWeightY * 100);
        gbc.weighty = verticalGlueWeightY;
        localParentPanel.add(Box.createVerticalGlue(), gbc);
        //TYPE-SPECIFIC PANEL
        gbc.gridy = (int) ((typeWeightY + verticalGlueWeightY)* 100);
        gbc.gridheight = (int) (typeSpecificFilterCardsPanelWeightY * 100);
        gbc.weighty = typeSpecificFilterCardsPanelWeightY;
        localParentPanel.add(typeSpecificFilterCardsPanel, gbc);
        //VERT GLUE #2
        gbc.gridy = (int) ((typeWeightY + verticalGlueWeightY + typeSpecificFilterCardsPanelWeightY) * 100);
        gbc.gridheight = (int) (verticalGlueWeightY * 100);
        gbc.weighty = verticalGlueWeightY;
        localParentPanel.add(Box.createVerticalGlue(), gbc);
        //SHARED FILTERS PANEL
        gbc.gridy = (int) (int) ((typeWeightY + (verticalGlueWeightY*2) + typeSpecificFilterCardsPanelWeightY) * 100);
        gbc.gridheight = (int) (sharedFiltersPanelWeightY * 100);
        gbc.weighty = sharedFiltersPanelWeightY;
        localParentPanel.add(sharedFiltersPanel, gbc);

        return localParentPanel;
    }

    /**
     * Composes the pickle, tomato, protein selector and price prompt panels into a single panel for filters
     * shared by salads and burgers. This is in a GridBagLayout that looks like:
     * <li>LEFT Pickles & tomato RIGHT proteins; takes a majority of the overall panel's height</li>
     * <li>Cheese</li>
     * <li>Price</li>
     * @return JPanel for all shared filter selection
     */
    private JPanel makeSharedFiltersPanel() {
        //                  *** CONTAINER FOR THE SHARED FILTER PANELS***
        JPanel sharedFiltersPanel = new JPanel(new GridBagLayout()); //GBL for full col for proteins
        // PICKLES AND TOMATO PANEL
        JPanel picklesAndTomatoPanel = makePicklesAndTomatoSelectorPanel();
        // PROTEIN PROMPT AND SELECTOR
        JPanel proteinPromptAndSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.PROTEIN);
        //List<Object> proteinsSelected = proteinSelectors.getSelectedValuesList(); //TODO move to search button**************
        // MIN AND MAX PRICE PROMPT AND SELECTORS
        JPanel pricePromptAndSelectorPanel =  makePricePromptAndSelectorPanel();
        // CHEESE PROMPT AND SELECTOR
        JPanel cheesePromptAndSelectorPanel = makeCheesePromptAndSelectorPanel();

        GridBagConstraints gbc = new GridBagConstraints();

        //          ***GRID BAG LAYOUT CONSTRAINTS FOR SHARED FILTERS PANEL***
        //GBC height proportion allocated to this cell in shared filters panel; 3 rows
        // local constants for non-obvious GBC values to make it easy to read and maintain.
        double picklesTomatoProteinYWeight = 0.6;
        double cheeseYWeight = 0.2;
        double priceYWeight = 0.2;
        int singleColumn = 1; //
        int spanBothColumns = 2;


        gbc.fill = GridBagConstraints.BOTH; //All components stretch to fill their available space

        //PICKLES AND TOMATO PANEL
        gbc.gridx = 0; gbc.gridy = 0; // (0,0)
        gbc.gridwidth = singleColumn; gbc.gridheight = 1; //just take one cell in each direction
        gbc.weightx = 0.5; gbc.weighty = picklesTomatoProteinYWeight;
        sharedFiltersPanel.add(picklesAndTomatoPanel, gbc);

        //PROTEIN PANEL
        gbc.gridx = 1; gbc.gridy = 0; // (1,0)
        gbc.gridwidth = singleColumn; gbc.gridheight = 1;
        gbc.weightx = 0.5; gbc.weighty = picklesTomatoProteinYWeight;
        sharedFiltersPanel.add(proteinPromptAndSelectorPanel, gbc);

        //CHEESE PANEL
        gbc.gridx = 0; gbc.gridy = 1; //(0,1)
        gbc.gridwidth = spanBothColumns; gbc.gridheight = 1; //Span two cols horizontally, 1 vertically
        gbc.weightx = 1.0; gbc.weighty = cheeseYWeight;
        sharedFiltersPanel.add(cheesePromptAndSelectorPanel, gbc);

        //PRICE PANEL
        gbc.gridx = 0; gbc.gridy = 2; //(0,2)
        gbc.gridwidth = spanBothColumns; gbc.gridheight = 1;
        gbc.weightx = 1.0; gbc.weighty = priceYWeight;
        sharedFiltersPanel.add(pricePromptAndSelectorPanel, gbc);

        return sharedFiltersPanel;
    }

    /**
     * Creates the SaladFiltersPanel, a series of nested panels held at the top level in a GridBagLayout.
     * <p>Layout is:
     * <li>NW dressing selector with combobox</li>
     * <li>W between dressing and cucumber, small vertical glue</li>
     * <li>SW cucumber selector with radiobuttons</li>
     * <li>E leafy greens list multi selector</li>
     *
     * @return JPanel with all salad-only filters
     */
    private JPanel makeSaladFiltersPanel() {
        JPanel saladFiltersPanel = new JPanel(new GridBagLayout);

        JPanel leafyGreensSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.LEAFY_GREENS);

        //CUCUMBER PROMPT AND BUTTONS
        JPanel cucumberPromptAndSelectorPanel = makeCucumberPromptAndSelectorPanel();

        JPanel dressingPromptAndSelectorPanel = makeDressingPromptAndSelectorPanel();

        // Overall weightX values--must add to 1.0
        double leafyGreensWeightX = 0.5;
        double horizontalFillerWeightX = 0.2;
        double firstColWeightX = 0.3;

        // First col weight y values--must add to 1.0
        double firstColFillerWeightY = 0.2;
        double dressingWeightY = 0.4;
        double cucumberWeightY = 0.4;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1; // All grid widths are 1

        // CUCUMBER BUTTONS PANEL
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridheight = (int) (dressingWeightY * 10); //take the first 4 rows in the first col
        gbc.weightx = firstColWeightX;
        gbc.weighty = dressingWeightY;
        gbc.fill = GridBagConstraints.BOTH;
        saladFiltersPanel.add(dressingPromptAndSelectorPanel, gbc);

        //VERTICAL FILLER GLUE BETWEEN DRESSING (TOP) AND CUCUMBER BUTTONS (BELOW)
        gbc.gridx = 0;
        gbc.gridy = (int) (dressingWeightY * 10); //Start from where the dressing finished vertically
        gbc.gridheight = (int) (firstColFillerWeightY * 10); //take the next 2 rows in the first col
        gbc.weightx = firstColWeightX;
        gbc.weighty = firstColFillerWeightY;
        saladFiltersPanel.add(Box.createVerticalGlue(), gbc);

        //CUCUMBER BUTTONS PANEL
        gbc.gridx = 0;
        gbc.gridy = (int) ((dressingWeightY + firstColFillerWeightY) * 10); //Start where the vertical filler ended
        gbc.weightx = firstColWeightX;
        gbc.weighty = cucumberWeightY;
        saladFiltersPanel.add(cucumberPromptAndSelectorPanel, gbc);

        //HORIZONTAL FILLER GLUE BETWEEN DRESSING+CUCUMBER (LEFT) AND LEAFY GREENS (RIGHT)
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.gridheight = (int) ((dressingWeightY + firstColFillerWeightY + cucumberWeightY) *10); //stretch across all rows
        gbc.weightx = horizontalFillerWeightX;
        gbc.weighty = 1.0;
        saladFiltersPanel.add(Box.createHorizontalGlue(), gbc);

        //LEAFY GREENS PANEL (RIGHT PORTION OF GRID)
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.gridheight = (int) ((dressingWeightY + firstColFillerWeightY + cucumberWeightY) *10); //stretch across all rows
        gbc.weightx = leafyGreensWeightX;
        gbc.weighty = 1.0;
        saladFiltersPanel.add(leafyGreensSelectorPanel, gbc);

        return saladFiltersPanel;
    }

    /**
     * Composes the bun and sauces prompt and selector panels into a single panel for burger-only filters.
     * This is a GridBagLayout that looks like:
     * <li>LEFT 30% bun selection, top anchored and horizontal stretch</li>
     * <li>20% empty glue</li>
     * <li>RIGHT 50% sauces multi-selector</li>
     * @return JPanel for all burger-only filter selection
     */
    private JPanel makeBurgerFiltersPanel() {
        JPanel burgerFiltersPanel = new JPanel(new GridBagLayout());

        JPanel bunPromptAndSelectorPanel = makeBunPromptAndSelectorPanel();
        JPanel saucePromptAndSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.SAUCE_S);

        // Values must add up to 1; allows easy base10 conversion for grid position
        double bunWeightX = 0.3;
        double sauceWeightX = 0.5;
        double fillerWeightX = 0.2; //fill weight between bun and sauce selection

        GridBagConstraints gbc = new GridBagConstraints();

        // Bun selector takes up the first 30% of the horizontal area. It's anchored to the top of
        // the grid and fills horizontally.
        gbc.gridx = 0; gbc.gridy = 0; //(0,0)
        gbc.gridwidth = (int) (bunWeightX * 10); gbc.gridheight = 1;
        gbc.weightx = bunWeightX; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        burgerFiltersPanel.add(bunPromptAndSelectorPanel, gbc);

        // Filler glue between the two panels
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.gridwidth = 1; gbc.gridheight = 1;
        gbc.weightx = fillerWeightX; gbc.weighty = 1.0;
        burgerFiltersPanel.add(Box.createHorizontalGlue(), gbc);

        //Sauce selector takes up the right-most 50% of the horizontal area.
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1; gbc.gridheight = 1;
        gbc.weightx = sauceWeightX; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        burgerFiltersPanel.add(saucePromptAndSelectorPanel, gbc);

        return  burgerFiltersPanel;
    }

    /**
     * Creates a panel for getting Dressing selections. Prompt label in NORTH and combobox returning Object in CENTER
     * @return JPanel with JLabel and JComboBox
     */
    private JPanel makeDressingPromptAndSelectorPanel() {
        JPanel dressingPromptAndSelectorPanel = new JPanel(new BorderLayout());
        JLabel dressingPromptLabel = new JLabel(Filter.DRESSING.filterPrompt());
        JComboBox<Object> dressingPromptComboBox = new JComboBox<>(MenuSearcher.getAvailableOptions(Filter.DRESSING).toArray());
        dressingPromptAndSelectorPanel.add(dressingPromptLabel, BorderLayout.NORTH);
        dressingPromptAndSelectorPanel.add(dressingPromptComboBox, BorderLayout.CENTER);

        return  dressingPromptAndSelectorPanel;
    }

    /**
     * Panel holding label for the cheese prompt (BorderLayout.NORTH) and
     * JComboBox<Object> for selecting cheeses (BorderLayout.CENTER).
     * @return JPanel for cheese filter selection
     */
    private JPanel makeCheesePromptAndSelectorPanel(){
        JPanel cheesePromptAndSelectorPanel = new JPanel(new BorderLayout());

        JLabel cheesePromptLabel = new JLabel(Filter.CHEESE.filterPrompt());
        JComboBox<Object> cheeseSelector = new JComboBox<>(MenuSearcher.getAvailableOptions(Filter.CHEESE).toArray());

        cheesePromptAndSelectorPanel.add(cheesePromptLabel, BorderLayout.NORTH);
        cheesePromptAndSelectorPanel.add(cheeseSelector, BorderLayout.CENTER);

        return cheesePromptAndSelectorPanel;
    }

    /**
     * Panel holding labels for min and max price ala 'Min. price: $' and textfields for
     * input with '0.00' default text and 6 col width.
     * Slight horizontal padding insets it; min and max price labels and input fields separated by responsive glue.
     * @return JPanel for min/max price selection
     */
    private JPanel makePricePromptAndSelectorPanel(){
        // SINGLE ROW PANEL TO RETURN
        JPanel priceSelectorPanel = new JPanel();
        priceSelectorPanel.setLayout(new BoxLayout(priceSelectorPanel, BoxLayout.X_AXIS));

        JLabel priceMinLabel = new JLabel("Min. price: $");
        JLabel priceMaxLabel = new JLabel("Max. price: $");
        JTextField priceMinField = new JTextField("0.00", 6); //width for 6 chars
        JTextField priceMaxField = new JTextField("0.00", 6);

        priceSelectorPanel.add(priceMinLabel);
        priceSelectorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        priceSelectorPanel.add(priceMinField);
        priceSelectorPanel.add(Box.createHorizontalGlue());
        priceSelectorPanel.add(priceMaxLabel);
        priceSelectorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        priceSelectorPanel.add(priceMaxField);

        priceSelectorPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); //Bit of horizontal breathing room

        return priceSelectorPanel;

    }

    /**
     * Panel holding pickles and tomato selectors and prompts.
     * (2,2) layout with:
     * <li>tomatoPromptLabel--Position (1,1)
     * <li>tomatoButtonsPanel--Position (1,2)
     * <li>picklePromptLabel--Position (2,1)
     * <li>pickleCheckBox--Position (2,2)
     * @return JPanel with, importantly, ButtonGroup tomatoGroup (Y/N/Don't Mind) and pickleCheckBox
     */
    private JPanel makePicklesAndTomatoSelectorPanel() {
        JPanel picklesAndTomatoPanel = new JPanel(new GridLayout(2, 2)); //PANEL TO RETURN

        //TOMATOES PROMPT AND BUTTONS
        JLabel tomatoPromptLabel = new JLabel(Filter.TOMATO.filterPrompt());
        ButtonGroup tomatoGroup = new ButtonGroup();
        JRadioButton tomatoButton1 = new JRadioButton("Yes");
        JRadioButton tomatoButton2 = new JRadioButton("No");
        JRadioButton tomatoButton3 = new JRadioButton("I Don't Mind");
        //Register with the tomato ButtonGroup
        tomatoGroup.add(tomatoButton1);
        tomatoGroup.add(tomatoButton2);
        tomatoGroup.add(tomatoButton3);
        //Create holding Panel and add relevant buttons.
        JPanel tomatoButtonsPanel = new JPanel(new GridLayout(1, 3));
        tomatoButtonsPanel.add(tomatoButton1);
        tomatoButtonsPanel.add(tomatoButton2);
        tomatoButtonsPanel.add(tomatoButton3);

        // PICKLES PROMPT AND CHECKBOX
        JLabel picklePromptLabel = new JLabel(Filter.PICKLES.filterPrompt());
        JCheckBox pickleCheckBox = new JCheckBox();

        //COMPILE THE COMBINED PICKLES AND TOMATO PANEL
        picklesAndTomatoPanel.add(tomatoPromptLabel); //Position (1,1)
        picklesAndTomatoPanel.add(tomatoButtonsPanel); //Position (1,2)
        picklesAndTomatoPanel.add(picklePromptLabel); //Position (2,1)
        picklesAndTomatoPanel.add(pickleCheckBox); //Position (2,2)

        return  picklesAndTomatoPanel;
    }

    /**
     * Panel holding the cucumber prompt and selector. Buttons registered to cucumberGroup ButtonGroup
     * <p>(3,1) layout, with:
     * <li>'Yes'</li>
     * <li>'No'/li>
     * <li>'I Don't Mind'</li>
     * @return JPanel with JLabel and nested JPanel for radio buttons
     */
    private JPanel makeCucumberPromptAndSelectorPanel(){
        ButtonGroup cucumberGroup = new ButtonGroup();
        JRadioButton cucumberButton1 = new JRadioButton("Yes");
        JRadioButton cucumberButton2 = new JRadioButton("No");
        JRadioButton cucumberButton3 = new JRadioButton("I Don't Mind");
        //Register with the cucumber ButtonGroup
        cucumberGroup.add(cucumberButton1);
        cucumberGroup.add(cucumberButton2);
        cucumberGroup.add(cucumberButton3);
        //Create holding Panel and add to it
        JPanel cucumberButtonsPanel = new JPanel(new GridLayout(3, 1));
        cucumberButtonsPanel.add(cucumberButton1);
        cucumberButtonsPanel.add(cucumberButton2);
        cucumberButtonsPanel.add(cucumberButton3);

        JLabel cucumberPromptLabel = new JLabel(Filter.CUCUMBER.filterPrompt());

        // Compose the panel to return
        JPanel cucumberPromptAndSelectorPanel = new JPanel(new BorderLayout());
        cucumberPromptAndSelectorPanel.add(cucumberPromptLabel, BorderLayout.NORTH);
        cucumberButtonsPanel.add(cucumberPromptAndSelectorPanel, BorderLayout.CENTER);

        return cucumberPromptAndSelectorPanel;
    }

    /**
     * Factory helper that builds a JPanel with prompt label and a scrollable JList for multiple selection of filters.
     * Uses BorderLayout, with the promptLabel taking NORTH and the selection area taking CENTER
     * @param filter the Filter whose values and prompt are to be populated
     * @return JPanel for multi selection of the relevant Filter.
     */
    private JPanel makeMultiSelectionPanelWithLabelAndScroll(Filter filter) {
        JPanel promptAndSelectorPanelWithScroll = new JPanel(new BorderLayout());
        JLabel promptLabel = new JLabel(filter.filterPrompt());
        JList<Object> filterSelectors = new JList<>(MenuSearcher.getAvailableOptions(filter).toArray());
        filterSelectors.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane filterSelectorsScrollPane = new JScrollPane(filterSelectors); //Scrollable

        // Compose protein panel
        promptAndSelectorPanelWithScroll.add(promptLabel, BorderLayout.NORTH);
        promptAndSelectorPanelWithScroll.add(filterSelectorsScrollPane, BorderLayout.CENTER);

        return promptAndSelectorPanelWithScroll;
    }


    /**
     * Factory helper to make a bun prompt and selector child panel for the filter panel.
     * @return JPanel with a bunPromptLabel (BorderLayout.WEST) and a
     * bunFiltersPanel(BorderLayout.CENTER) for selecting buns as Object from a JComboBox
     */
    private JPanel makeBunPromptAndSelectorPanel(){
        JPanel bunFiltersPanel = new JPanel(new BorderLayout());
        JLabel bunPromptLabel = new JLabel(Filter.BUN.filterPrompt());
        JComboBox<Object> bunSelector = new JComboBox<>(MenuSearcher.getAvailableOptions(Filter.BUN).toArray());
        //Compose the bun panel to return
        bunFiltersPanel.add(bunPromptLabel,  BorderLayout.WEST);
        bunFiltersPanel.add(bunSelector, BorderLayout.CENTER);

        return bunFiltersPanel;
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

    private JButton makeImgOnlyButtonWithResize(String imagePath, String destinationPanel, Dimension size) {
        //IntelliJ tells me this should be final for the anonymous inner class' use.
        final BufferedImage sourceImage = loadBufferedImage(imagePath);

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
                   g.drawImage(finalSourceImage, x, y, c.getWidth(), c.getHeight(), c);
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

        button.addActionListener(e -> switchCard(destinationPanel));
        return button;
    }



    public void switchCard(String cardName) {
        switch (cardName) {
            case "welcomePanel" -> topCardLayout.show(topCardPanel, "mainBurgerFilterPanel");
            case "otherUndefinedcases" -> topCardLayout.show(topCardPanel, "otherUndefinedPanel");
        }
    }


}
