import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FilterEntryPanel {

    // FIELDS FOR STORING SELECTOR SELECTIONS BEFORE PROCESSING; Map would be possible, but less explicit = hard to maintain.
    private final JComboBox<Type> itemTypeSelector;
    private final JComboBox<Object> bunSelector;
    private final JList<Object> sauceList;
    private final JComboBox<Object> dressingSelector;
    private final JList<Object> leafyGreensList;
    private final JList<Object> proteinList;
    private final ButtonGroup tomatoGroup;
    private final ButtonGroup cucumberGroup;
    private final JCheckBox pickleCheckBox;
    private final JComboBox<Object> cheeseSelector;
    private final JTextField priceMinField;
    private final JTextField priceMaxField;

    // THE CORE COMPOSED JPANEL THAT WILL HOLD ALL THE SUB-COMPONENTS
    private final JPanel corePanel;

    /**
     * Inner private Enum for dealing with validating the button group
     * --only reliable and neat way to address intended null returns correctly.
     */
    private enum YesNoSkipValidStrings {
        YES,
        NO,
        I_DONT_MIND;

        /**
         * Provide prettified toStrings for this enum's values
         * @return
         */
        @Override
        public String toString() {
            return switch (this) {
                case YES -> "Yes";
                case NO -> "No";
                case I_DONT_MIND -> "I Don't Mind";
            };
        }


    }

    public FilterEntryPanel(Map<Filter, List<Object>> filterOptions) {
        this.corePanel = new JPanel(new GridBagLayout());

        // Initialise the CardLayout and its JPanel here to pass to relevant constructors.
        CardLayout typeSpecificCardLayout = new CardLayout();
        JPanel typeSpecificFilterCardsPanel = new JPanel(typeSpecificCardLayout);

        //INITIALISE FIELD COMPONENTS
        initialiseComponents(filterOptions);

        //BUILD THE COMPONENTS AND OVERALL LAYOUT
        buildLayout(typeSpecificCardLayout, typeSpecificFilterCardsPanel);

        //LISTENER FOR TYPE STATE--BURGER OR SALAD; FLIPS VARIABLE COMPONENTS TO RELEVANT CARD
        itemTypeSelector.addActionListener(e -> {
            Type selectedType = (Type) itemTypeSelector.getSelectedItem();
            //This could be simplified to a ternary expression, but the explicit cases for both
            //types makes it easier to add new types in future.
            if (selectedType == Type.BURGER) {
                typeSpecificCardLayout.show(typeSpecificFilterCardsPanel, "burgerFilters");
            } else if (selectedType == Type.SALAD) {
                typeSpecificCardLayout.show(typeSpecificFilterCardsPanel, "saladFilters");
            }
        });
    }


    private void initialiseComponents(CardLayout typeSpecificCardLayout, JPanel typeSpecificFilterCardsPanel,
                                      Map<Filter, List<Object>> filterOptions) {
        // Item type selector
        this.itemTypeSelector = new JComboBox<>(Type.values());

        //Burger-only Components
        this.bunSelector = new JComboBox<>(getOptionsOrFail(filterOptions, Filter.BUN).toArray());
        this.sauceList = new JList<>(getOptionsOrFail(filterOptions, Filter.SAUCE_S).toArray());

        //Salad-only Components
        this.dressingSelector = new JComboBox<>(getOptionsOrFail(filterOptions, Filter.DRESSING).toArray());
        this.leafyGreensList = new JList<>(getOptionsOrFail(filterOptions, Filter.LEAFY_GREENS).toArray());
        this.cucumberGroup = new ButtonGroup();

        //Shared filters Components
        this.proteinList = new JList<>(getOptionsOrFail(filterOptions, Filter.PROTEIN).toArray());
        this.cheeseSelector = new JComboBox<>(getOptionsOrFail(filterOptions, Filter.CHEESE).toArray());
        this.priceMinField = new JTextField("0.00", 6);
        this.priceMaxField = new JTextField("0.00", 6);
        this.pickleCheckBox = new JCheckBox();
        this.tomatoGroup = new ButtonGroup();

        // SET THE SELECTION MODES OF THE JLISTS
        sauceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        leafyGreensList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        proteinList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    private void buildLayout(CardLayout typeSpecificCardLayout, JPanel typeSpecificFilterCardsPanel) {
        JPanel sharedFiltersPanel = makeSharedFiltersPanel();
        JPanel burgerFiltersPanel = makeBurgerFiltersPanel();
        JPanel saladFiltersPanel = makeSaladFiltersPanel();
        JPanel typeFilterPanel = makeTypeFilterPanel();

        typeSpecificFilterCardsPanel.add(burgerFiltersPanel, "burgerFilters");
        typeSpecificFilterCardsPanel.add(saladFiltersPanel, "saladFilters");

        // LAYOUT FOR THE CORE FILTERS PANEL
        //TODO COME BACK AND ADD SPACE FOR THE BUTTON AT THE BOTTOM******************************************************
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; //global values

        gbc.gridy = 0; gbc.weighty = 0.1; this.corePanel.add(typeFilterPanel, gbc);

        gbc.gridy = 1; gbc.weighty = 0.45; this.corePanel.add(typeSpecificFilterCardsPanel, gbc);

        gbc.gridy = 2; gbc.weighty = 0.45; this.corePanel.add(sharedFiltersPanel, gbc);
    }


    /**
     * Factory helper to build typeFiltersPanel for constructor.
     * <p>Requires itemTypeSelector to already be instantiated in field.
     * @return JPanel with typeFilterLabel (BorderLayout.WEST) and typeFilter selector (BorderLayout.CENTER)
     */
    private JPanel makeTypeFilterPanel() {
        JPanel typeFilterPanel = new JPanel(new BorderLayout());
        JLabel typeFilterLabel = new JLabel(Filter.TYPE.filterPrompt());
        typeFilterPanel.add(typeFilterLabel, BorderLayout.WEST);
        typeFilterPanel.add(this.itemTypeSelector, BorderLayout.CENTER);

        return typeFilterPanel;
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
     * <p>Requires dressingSelector pre-instantiated at field</p>
     * @return JPanel with JLabel and JComboBox
     */
    private JPanel makeDressingPromptAndSelectorPanel() {
        JPanel dressingPromptAndSelectorPanel = new JPanel(new BorderLayout());
        JLabel dressingPromptLabel = new JLabel(Filter.DRESSING.filterPrompt());

        dressingPromptAndSelectorPanel.add(dressingPromptLabel, BorderLayout.NORTH);
        dressingPromptAndSelectorPanel.add(this.dressingSelector, BorderLayout.CENTER);

        return  dressingPromptAndSelectorPanel;
    }

    /**
     * Panel holding label for the cheese prompt (BorderLayout.NORTH) and
     * JComboBox<Object> for selecting cheeses (BorderLayout.CENTER).
     * <p>Requires cheeseSelector field pre-instantiated</p>
     * @return JPanel for cheese filter selection
     */
    private JPanel makeCheesePromptAndSelectorPanel(){
        JPanel cheesePromptAndSelectorPanel = new JPanel(new BorderLayout());

        JLabel cheesePromptLabel = new JLabel(Filter.CHEESE.filterPrompt());

        cheesePromptAndSelectorPanel.add(cheesePromptLabel, BorderLayout.NORTH);
        cheesePromptAndSelectorPanel.add(this.cheeseSelector, BorderLayout.CENTER);

        return cheesePromptAndSelectorPanel;
    }

    /**
     * Panel holding labels for min and max price ala 'Min. price: $' and textfields for
     * input with '0.00' default text and 6 col width.
     * <p>Slight horizontal padding insets it; min and max price labels and input fields separated by responsive glue.
     * <p>Requires priceMinField and priceMaxField pre-instantiated in object field</p>
     * @return JPanel for min/max price selection
     */
    private JPanel makePricePromptAndSelectorPanel(){
        // SINGLE ROW PANEL TO RETURN
        JPanel priceSelectorPanel = new JPanel();
        priceSelectorPanel.setLayout(new BoxLayout(priceSelectorPanel, BoxLayout.X_AXIS));

        JLabel priceMinLabel = new JLabel("Min. price: $");
        JLabel priceMaxLabel = new JLabel("Max. price: $");

        priceSelectorPanel.add(priceMinLabel);
        priceSelectorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        priceSelectorPanel.add(this.priceMinField);
        priceSelectorPanel.add(Box.createHorizontalGlue());
        priceSelectorPanel.add(priceMaxLabel);
        priceSelectorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        priceSelectorPanel.add(this.priceMaxField);

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
        JRadioButton tomatoButton1 = new JRadioButton(YesNoSkipValidStrings.YES.toString());
        JRadioButton tomatoButton2 = new JRadioButton(YesNoSkipValidStrings.NO.toString());
        JRadioButton tomatoButton3 = new JRadioButton(YesNoSkipValidStrings.I_DONT_MIND.toString());
        //Register with the tomato ButtonGroup; NB THIS IS THE FIELD TOMATOGROUP
        this.tomatoGroup.add(tomatoButton1);
        this.tomatoGroup.add(tomatoButton2);
        this.tomatoGroup.add(tomatoButton3);
        //Create holding Panel and add relevant buttons.
        JPanel tomatoButtonsPanel = new JPanel(new GridLayout(1, 3));
        tomatoButtonsPanel.add(tomatoButton1);
        tomatoButtonsPanel.add(tomatoButton2);
        tomatoButtonsPanel.add(tomatoButton3);

        // PICKLES PROMPT AND CHECKBOX
        JLabel picklePromptLabel = new JLabel(Filter.PICKLES.filterPrompt());

        //COMPILE THE COMBINED PICKLES AND TOMATO PANEL
        picklesAndTomatoPanel.add(tomatoPromptLabel); //Position (1,1)
        picklesAndTomatoPanel.add(tomatoButtonsPanel); //Position (1,2)
        picklesAndTomatoPanel.add(picklePromptLabel); //Position (2,1)
        picklesAndTomatoPanel.add(this.pickleCheckBox); //Position (2,2) NB ADDS THE FIELD VARIABLE

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
        JRadioButton cucumberButton1 = new JRadioButton(YesNoSkipValidStrings.YES.toString());
        JRadioButton cucumberButton2 = new JRadioButton(YesNoSkipValidStrings.NO.toString());
        JRadioButton cucumberButton3 = new JRadioButton(YesNoSkipValidStrings.I_DONT_MIND.toString());
        //Register with the cucumber ButtonGroup; NB THIS IS THE FIELD CUCUMBERGROUP
        this.cucumberGroup.add(cucumberButton1);
        this.cucumberGroup.add(cucumberButton2);
        this.cucumberGroup.add(cucumberButton3);
        //Create holding Panel and add to it
        JPanel cucumberButtonsPanel = new JPanel(new GridLayout(3, 1));
        cucumberButtonsPanel.add(cucumberButton1);
        cucumberButtonsPanel.add(cucumberButton2);
        cucumberButtonsPanel.add(cucumberButton3);

        JLabel cucumberPromptLabel = new JLabel(Filter.CUCUMBER.filterPrompt());

        // Compose the panel to return
        JPanel cucumberPromptAndSelectorPanel = new JPanel(new BorderLayout());
        cucumberPromptAndSelectorPanel.add(cucumberPromptLabel, BorderLayout.NORTH);
        cucumberPromptAndSelectorPanel.add(cucumberButtonsPanel, BorderLayout.CENTER);

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

        JList<Object> filterSelectors = null;
        switch (filter) {
            case PROTEIN -> filterSelectors = this.proteinList;
            case SAUCE_S -> filterSelectors = this.sauceList;
            case LEAFY_GREENS -> filterSelectors = this.leafyGreensList;
            // This should never really happen, but just in case, have a default return empty panel.
            default -> {
                System.out.println(
                        "Unexpected filter passed when making multi selection panel in FilterEntryPanel."
                                +"GUI selectors could not be built appropriately.");
                System.exit(1);
            }
        }
        // Compose panel to return
        promptAndSelectorPanelWithScroll.add(promptLabel, BorderLayout.NORTH);
        promptAndSelectorPanelWithScroll.add(new JScrollPane(filterSelectors), BorderLayout.CENTER);

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

        //Compose the bun panel to return
        bunFiltersPanel.add(bunPromptLabel,  BorderLayout.WEST);
        bunFiltersPanel.add(this.bunSelector, BorderLayout.CENTER);

        return bunFiltersPanel;
    }

    //GENERAL USE PRIVATE HELPERS

    /**
     * Private helper to get the List of Objects in the passed-in criteria Map's values for selector population.
     * <p>Deliberately fails with error message if List is null or empty</p>
     * @param optionsMap Map Filter, List of Objects representing the option values to populate to a filter selector
     * @param filter the Filter whose selector is to be populated in the calling method
     * @return immutable List of options Objects
     */
    private List<Object> getOptionsOrFail(Map<Filter, List<Object>> optionsMap, Filter filter) {
        List<Object> options = optionsMap.get(filter);
        if (options == null || options.size() < 1) {
            System.err.println(
                    "Error: the filter selections failed to populate at FilterEntryPanel construction when "
                            +"getting available selector options for " + filter);
            System.exit(1);
        }
        return List.copyOf(options);
    }

    /**
     * Helper to get the String of the selected button within a ButtonGroup.
     * <p>Code from Rendicahya's answer at
     * https://stackoverflow.com/questions/201287/how-do-i-get-which-jradiobutton-is-selected-from-a-buttongroup
     * @param buttonGroup the ButtonGroup holding the buttons to iterate
     * @return String of the text assigned to the selected button.
     */
    private String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }


    /**
     * Validates the relevant ButtonGroup selection--whether 'Yes', 'No' or 'Skip' (or skip equivalent)
     * @param buttonGroup the ButtonGroup to query
     * @return true for 'Yes' selection, false for 'No' selection, and null for 'Skip' or equivalent selection.
     * @throws IllegalStateException throws exception if the ButtonGroup does not contain 'Yes' or 'No' options.
     */
    private Boolean getYesNoSkipButtonGroupSelection(ButtonGroup buttonGroup) throws IllegalStateException {
        String selection = getSelectedButtonText(buttonGroup);

        //valid 'Skip' choice by not selecting or selecting 'I Don't Mind'
        if  (selection == null || selection.equals(YesNoSkipValidStrings.I_DONT_MIND.toString())) {
            return null;
        } else if (selection.equals(YesNoSkipValidStrings.YES.toString())) {
            return true;
        } else if (selection.equals(YesNoSkipValidStrings.NO.toString())) {
            return false;
        } else {
            StringJoiner sj = new StringJoiner(", ");
            for (YesNoSkipValidStrings enumValue : YesNoSkipValidStrings.values()) {sj.add(enumValue.toString());}

            throw new IllegalStateException("Invalid ButtonGroup passed to getYesNoSkipButtonGroupSelection."
                    +"Expected buttons with text "+ sj + "but there was a button called '" + selection + "'");
        }
    }


    //                  *** PUBLIC GETTERS ***

    /**
     * Gets the populated corePanel to add to a external class' Frame
     * @return a JPanel populated to function as a filter entry panel, with relevant fields for interim selection storage.
     */
    public JPanel getCorePanel(){
        return this.corePanel;
    }

    public FilterSelections getFilterSelections(){
        return new FilterSelections(
                (Type) itemTypeSelector.getSelectedItem(),
                bunSelector.getSelectedItem(),
                Set.copyOf(sauceList.getSelectedValuesList()),
                dressingSelector.getSelectedItem(),
                Set.copyOf(leafyGreensList.getSelectedValuesList()),
                Set.copyOf(proteinList.getSelectedValuesList()),
                getYesNoSkipButtonGroupSelection(tomatoGroup),
                getYesNoSkipButtonGroupSelection(cucumberGroup),
                pickleCheckBox.isSelected(),
                cheeseSelector.getSelectedItem(),
                priceMinField.getText(),
                priceMaxField.getText()
        );
    }

}
