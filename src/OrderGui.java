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
//        int MAIN_BURGER_FILTER_PANEL_ROWS = 10;
//        int MAIN_BURGER_FILTER_PANEL_COLS = 6;
//        int SIDE_BANNER_COLS = 2;

        //MAIN PANEL TO BE RETURNED (just 4 columns & small h/v-gap)
        //TODO: THIS HAS TO BE GRIDBAGLAYOUT FOR PROPORTIONATE X-WEIGHTING**********************************************
        JPanel mainBurgerFilterPanel = new JPanel(new GridLayout(1, 4, 5, 5));
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


        //ADD SUB-COMPONENTS TO MAIN PANEL TO BE RETURNED
        mainBurgerFilterPanel.add(sideBanner); //Add first to go in first of 4 columns.

        return mainBurgerFilterPanel;
    }

    private JPanel filterSelectorsPanel() {
        /**
         * THIS NEEDS TO FILTER FOR:
         * BURGER ONLY: BUN, SAUCE_S
         * SALAD ONLY: LEAFY_GREENS, CUCUMBER, DRESSING
         * BOTH: PICKLES, TOMATO, PROTEIN, PRICE, CHEESE
         *
         * I DON'T MIND SELECTORS: BUN, SAUCE_S,LEAFY_GREENS, CUCUMBER, DRESSING, PROTEIN, TOMATO
         * ALLOWS EXPLICIT 'NONE' SELECTION: TOMATO, CUCUMBER, PICKLES, SAUCES, PROTEINS AND LEAFY_GREENS
         */


        // MAIN PANEL TO BE RETURNED
        JPanel localParentPanel = new JPanel (new GridLayout(1, 4)); //Arranges the side banner and main filter selection area.
        localParentPanel.setPreferredSize(GUI_PREFERRED_SIZE);

        //INNER-WRAPPER JPANEL W/ CARDLAYOUT TO SWITCH BURGER & SALAD VIEWS
        CardLayout cardLayout = new CardLayout();
        JPanel filterCardsPanel = new JPanel(cardLayout);

        JPanel sharedFiltersPanel = makeSharedFiltersPanel();




        //                  *** BURGER ONLY SELECTORS ***
        // BUN PROMPT AND SELECTOR
        JPanel bunPromptAndSelectorPanel = makeBunPromptAndSelectorPanel();



        // TYPE PROMPT AND SELECTOR -- TYPE SELECTION GOVERNS CARD DISPLAY (BUN/SALAD PANELS)
        JPanel typeFilterPanel = new JPanel(new BorderLayout());
        JLabel itemTypePromptLabel = new JLabel(Filter.TYPE.filterPrompt());
        JComboBox<Type> menuItemTypeSelector = new JComboBox<>(Type.values());
        menuItemTypeSelector.addActionListener(e -> {
            Type selectedType = (Type) menuItemTypeSelector.getSelectedItem();
            //This could be simplified, but the explicit cases for both types makes it easier to add new types in future.
            if (selectedType == Type.BURGER) {
//                cardLayout.show
//                bunSelector.enabled(true); bunPromptLabel.setEnabled(true);
//                sauceSelector.enabled(true); sauceSelectorLabel.setEnabled(true);
//                greensSelector.enabled(false); greensSelector.setVisible(false); greensSelectorLabel.setEnabled(false);
//                cucumberSelector.enabled(false); cucumberSelector.setVisible(false);
//                dressingSelector.enabled(false);
//                //pickles, tomato, protein and price are not strictly necessary, as they're shared,
//                //but would help future developers.
//                picklesSelector.enabled(true); picklesPromptLabel.setEnabled(true);
//                tomatoSelector.enabled(true); tomatoPromptLabel.setEnabled(true);
//                proteinSelector.enabled(true); proteinPromptLabel.setEnabled(true);
//                //TODO price selectors both fields
            } else if (selectedType == Type.SALAD) {
//                bunSelector.enabled(false); bunPromptLabel.setEnabled(false);
//                sauceSelector.enabled(true);
//                greensSelector.enabled(true);
//                cucumberSelector.enabled(false);
//                dressingSelector.enabled(true);
//                picklesSelector.enabled(true);
//                tomatoSelector.enabled(true);
//                proteinSelector.enabled(true); proteinPromptLabel.setEnabled(true);
//                //TODO price selectors both fields
            }
        });

        // Compose the panel to return
        typeFilterPanel.add(itemTypePromptLabel, BorderLayout.NORTH);

//        CardLayout conditionalFiltersCards = new CardLayout();
//        JPanel conditionalFiltersCardsPanel = new JPanel(conditionalFiltersCards);




    }

    /**
     * Composes the pickle, tomato, protein selector and price prompt panels into a single panel for filters shared by salads and burgers. This is in a GridBagLayout that looks like:
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
        JPanel proteinPromptAndSelectorPanel = makeProteinPromptAndSelectorPanel();
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
        JRadioButton tomatoButton = new JRadioButton("Yes");
        JRadioButton tomatoButton2 = new JRadioButton("No");
        JRadioButton tomatoButton3 = new JRadioButton("I Don't Mind");
        tomatoGroup.add(tomatoButton);
        tomatoGroup.add(tomatoButton2);
        tomatoGroup.add(tomatoButton3);
        JPanel tomatoButtonsPanel = new JPanel(new GridLayout(1, 3));

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
     * Factory helper to make a protein prompt and selector child panel for the filter panel.
     * @return JPanel with a proteinPromptLabel (BorderLayout.NORTH) and a
     * scrollpane with a JList<Object> proteinSelectors allowing multi select (BorderLayout.CENTER)
     */
    private JPanel makeProteinPromptAndSelectorPanel(){
        JPanel proteinFiltersPanel = new JPanel(new BorderLayout());
        JLabel proteinPromptLabel = new JLabel(Filter.PROTEIN.filterPrompt());
        JList<Object> proteinSelectors = new JList<>(MenuSearcher.getAvailableOptions(Filter.PROTEIN).toArray());
        proteinSelectors.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane proteinSelectorsScrollPane = new JScrollPane(proteinSelectors); //Scrollable

        // Compose protein panel
        proteinFiltersPanel.add(proteinPromptLabel,  BorderLayout.NORTH);
        proteinFiltersPanel.add(proteinSelectorsScrollPane, BorderLayout.CENTER);

        return proteinFiltersPanel;
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
