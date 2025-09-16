import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class is the main view for filter entry. It creates all the relevant components for filter value input.
 * <p>It is intended to filter for:
 * <li>BURGER ONLY: BUN, SAUCE_S
 * <li>SALAD ONLY: LEAFY_GREENS, CUCUMBER, DRESSING
 * <li>BOTH: PICKLES, TOMATO, PROTEIN, PRICE, CHEESE
 *
 * <li>I DON'T MIND SELECTORS: BUN, PROTEIN, CHEESE, CUCUMBER, TOMATO, DRESSING, LEAFY GREENS, SAUCES
 * <li>ALLOWS EXPLICIT 'NONE' SELECTION: PROTEIN, CHEESE, LEAFY GREENS, SAUCES
 *
 */
public final class FilterEntryPanel {
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

    private static final int MAIN_SPLIT_HORIZONTAL_PADDING = 20;

    /**
     * Creates a FilterEntryPanel. Core view for inputing user item selector filters
     * @param filterOptions a Map of Filter(key) List of Objects(value) to present for selectors
     */
    public FilterEntryPanel(Map<Filter, List<Object>> filterOptions) {

        //INITIALISE ALL FIELD ITEMS
        //NB. These field item instantiations would sit better in a helper, but it makes IntelliJ grumble.
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

        // Set the selection modes of the JLists
        sauceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        leafyGreensList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        proteinList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        setupJListSelectionListeners();

        // *** INITIALISE THE CORE PANEL TO WHICH EVERYTHING WILL BE ADDED ***
        this.corePanel = new JPanel();

        // Create a CardLayout and holder Panel--pass to relevant constructors.
        CardLayout typeSpecificCardLayout = new CardLayout();
        JPanel typeSpecificFilterCardsPanel = new JPanel(typeSpecificCardLayout);

        setupItemTypeSelectorListener(typeSpecificCardLayout, typeSpecificFilterCardsPanel);

        //BUILD THE COMPONENTS AND OVERALL LAYOUT
        buildLayout(typeSpecificFilterCardsPanel);
    }


    /**
     * Helper that adds an ActionListener to the itemTypeSelector to show the appropriate card layout for Burger/Salad
     * @param typeSpecificCardLayout the CardLayout
     * @param typeSpecificFilterCardsPanel the JPanel that holds the CardLayout to flip
     */
    private void setupItemTypeSelectorListener(CardLayout typeSpecificCardLayout, JPanel typeSpecificFilterCardsPanel) {
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

    /**
     * Calls the relevant helper factories to build all sub-Components, and then arranges global layout.
     * @param typeSpecificFilterCardsPanel the JPanel that holds core CardLayout for switching views.
     */
    private void buildLayout(JPanel typeSpecificFilterCardsPanel) {
        this.corePanel.setLayout(new GridBagLayout());
        this.corePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Give it some breathing room.

        JPanel sharedFiltersPanel = makeSharedFiltersPanel();
        JPanel burgerFiltersPanel = makeBurgerFiltersPanel();
        JPanel saladFiltersPanel = makeSaladFiltersPanel();
        JPanel typeFilterPanel = makeTypeFilterPanel();

        typeSpecificFilterCardsPanel.add(burgerFiltersPanel, "burgerFilters");
        typeSpecificFilterCardsPanel.add(saladFiltersPanel, "saladFilters");

        // LAYOUT FOR THE CORE FILTERS PANEL
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; //global values

        gbc.insets = new Insets(0,0,5,0); //5px gap below Type selector
        gbc.gridy = 0; gbc.weighty = 0.1; this.corePanel.add(typeFilterPanel, gbc); //give it a sliver up top

        gbc.insets = new Insets(5,0,5,0); //5px gap above and below shared filters
        gbc.gridy = 2; gbc.weighty = 0.35; this.corePanel.add(typeSpecificFilterCardsPanel, gbc); //gets a good chunk

        gbc.insets = new Insets(5,0,0,0);
        gbc.gridy = 1; gbc.weighty = 0.55; this.corePanel.add(sharedFiltersPanel, gbc); //gets most space
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
        JPanel sharedFiltersPanel = new JPanel(new BorderLayout());

        //TOP AREA: pickles/tomatoes (left) and proteins (right)
        JPanel topRow = new JPanel(new GridLayout(1,2,MAIN_SPLIT_HORIZONTAL_PADDING, 0));
        JPanel picklesAndTomatoPanel = makePicklesAndTomatoSelectorPanel();
        JPanel proteinPromptAndSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.PROTEIN);

        topRow.add(picklesAndTomatoPanel);
        topRow.add(proteinPromptAndSelectorPanel);

        //BOTTOM AREA: cheese and price in a horizontal layout
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, MAIN_SPLIT_HORIZONTAL_PADDING, 0));
        JPanel cheesePromptAndSelectorPanel = makeCheesePromptAndSelectorPanel();
        JPanel pricePromptAndSelectorPanel =  makePricePromptAndSelectorPanel();

        bottomRow.add(cheesePromptAndSelectorPanel);
        bottomRow.add(pricePromptAndSelectorPanel);

        bottomRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10,0,0,0),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1,0,0,0, Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(10,0,0,0)
                )
        ));

        //COMPOSE THE FINAL PANEL
        sharedFiltersPanel.add(topRow, BorderLayout.CENTER);
        sharedFiltersPanel.add(bottomRow, BorderLayout.SOUTH);


        return sharedFiltersPanel;
    }

    /**
     * Creates the SaladFiltersPanel, a series of nested panels held at the top level in a GridLayout.
     * <p>Layout is:
     * <li>NW dressing selector with combobox</li>
     * <li>W between dressing and cucumber, small vertical glue</li>
     * <li>SW cucumber selector with radiobuttons</li>
     * <li>E leafy greens list multi selector</li>
     *
     * @return JPanel with all salad-only filters
     */
    private JPanel makeSaladFiltersPanel() {
        JPanel saladFiltersPanel = new JPanel(new GridLayout(1,2, MAIN_SPLIT_HORIZONTAL_PADDING, 0));

        //CREATE THE 3 CORE COMPONENTS
        JPanel leafyGreensSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.LEAFY_GREENS);
        JPanel cucumberPromptAndSelectorPanel = makeCucumberPromptAndSelectorPanel();
        JPanel dressingPromptAndSelectorPanel = makeDressingPromptAndSelectorPanel();

        //COMPOSE THE LEFT SIDE OF THE PANEL - DRESSING, SPACER AND CUCUMBERS
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));

        leftPanel.add(dressingPromptAndSelectorPanel, BorderLayout.NORTH);
        leftPanel.add(cucumberPromptAndSelectorPanel, BorderLayout.CENTER);

        // COMPOSE AND RETURN THE FINAL PANEL
        saladFiltersPanel.add(leftPanel);
        saladFiltersPanel.add(leafyGreensSelectorPanel);

        return saladFiltersPanel;
    }

    /**
     * Composes the bun and sauces prompt and selector panels into a single panel for burger-only filters.
     * This is a nested GridLayout where the RIGHT 50% goes to the sauces multi-selector,
     * and the LEFT is shared equally between the bun prompt selector and an empty label.
     * @return JPanel for all burger-only filter selection
     */
    private JPanel makeBurgerFiltersPanel() {
        JPanel burgerFiltersPanel = new JPanel(new GridLayout(1,2, MAIN_SPLIT_HORIZONTAL_PADDING, 0));

        JPanel bunPromptAndSelectorPanel = makeBunPromptAndSelectorPanel();
        JPanel saucePromptAndSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.SAUCE_S);

        burgerFiltersPanel.add(bunPromptAndSelectorPanel); //LEFT SIDE: buns
        burgerFiltersPanel.add(saucePromptAndSelectorPanel); //RIGHT SIDE: sauces

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


        //Used <nobr> to stop line breaking depending on layout changes
        //https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/nobr
        JLabel priceMinLabel = new JLabel("<html><b>Min. <nobr>price: $</nobr></b></html>");
        JLabel priceMaxLabel = new JLabel("<html><b>Max. <nobr>price: $</nobr></b></html>");

        priceSelectorPanel.add(priceMinLabel);
        priceSelectorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        priceSelectorPanel.add(this.priceMinField);
        priceSelectorPanel.add(Box.createHorizontalGlue());
        priceSelectorPanel.add(priceMaxLabel);
        priceSelectorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        priceSelectorPanel.add(this.priceMaxField);

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
        JPanel picklesAndTomatoPanel = new JPanel(new BorderLayout()); //PANEL TO RETURN

        //TOMATOES PROMPT AND BUTTONS
        JLabel tomatoPromptLabel = new JLabel(Filter.TOMATO.filterPrompt());
        JRadioButton tomatoButton1 = new JRadioButton("Yes");
        JRadioButton tomatoButton2 = new JRadioButton("No");
        JRadioButton tomatoButton3 = new JRadioButton(Filter.TOMATO.getDontMindValue().toString());
        //Register with the tomato ButtonGroup; NB THIS IS THE FIELD TOMATOGROUP
        this.tomatoGroup.add(tomatoButton1);
        this.tomatoGroup.add(tomatoButton2);
        this.tomatoGroup.add(tomatoButton3);

        //PUT THE TOMATO PROMPT AND BUTTONS IN A CONTAINER TOGETHER in a vertical grid
        JPanel tomatoSection = new JPanel(new GridLayout(4,1));
        tomatoSection.add(tomatoPromptLabel);
        tomatoSection.add(tomatoButton1);
        tomatoSection.add(tomatoButton2);
        tomatoSection.add(tomatoButton3);

        //Give the tomato buttons a panel in which they will always occupy the vertical centre
        JPanel centredTomatoWrapper = new JPanel();
        centredTomatoWrapper.setLayout(new BoxLayout(centredTomatoWrapper, BoxLayout.Y_AXIS));
        centredTomatoWrapper.add(Box.createVerticalGlue());
        centredTomatoWrapper.add(tomatoSection);
        centredTomatoWrapper.add(Box.createVerticalGlue());

        // ***PICKLE SECTION***
        JPanel pickleSection = new JPanel(new BorderLayout());
        JLabel picklePromptLabel = new JLabel(Filter.PICKLES.filterPrompt());

        JPanel checkboxWrapper = new JPanel();
        checkboxWrapper.setLayout(new BoxLayout(checkboxWrapper, BoxLayout.Y_AXIS));
        checkboxWrapper.add(this.pickleCheckBox);
        checkboxWrapper.add(Box.createVerticalGlue());

        pickleSection.add(picklePromptLabel, BorderLayout.NORTH);
        pickleSection.add(checkboxWrapper, BorderLayout.CENTER);



        picklesAndTomatoPanel.add(pickleSection, BorderLayout.NORTH);
        picklesAndTomatoPanel.add(centredTomatoWrapper, BorderLayout.CENTER);

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
        JRadioButton cucumberButton1 = new JRadioButton("Yes");
        JRadioButton cucumberButton2 = new JRadioButton("No");
        JRadioButton cucumberButton3 = new JRadioButton(Filter.CUCUMBER.getDontMindValue().toString());
        //Register with the cucumber ButtonGroup; NB THIS IS THE FIELD CUCUMBERGROUP
        this.cucumberGroup.add(cucumberButton1);
        this.cucumberGroup.add(cucumberButton2);
        this.cucumberGroup.add(cucumberButton3);

        //Create holding Panel and add to it
        JPanel cucumberPanel = new JPanel(new GridLayout(4, 1));
        JLabel cucumberPromptLabel = new JLabel(Filter.CUCUMBER.filterPrompt());
        cucumberPanel.add(cucumberPromptLabel);
        cucumberPanel.add(cucumberButton1);
        cucumberPanel.add(cucumberButton2);
        cucumberPanel.add(cucumberButton3);

        //Wrapper panel to ensure the cucumber panel sits vertically centred within any parent using a borderlayout.
        JPanel cucumberCentredWrapper = new JPanel();
        cucumberCentredWrapper.setLayout(new BoxLayout(cucumberCentredWrapper, BoxLayout.Y_AXIS));
        cucumberCentredWrapper.add(Box.createVerticalGlue());
        cucumberCentredWrapper.add(cucumberPanel);
        cucumberCentredWrapper.add(Box.createVerticalGlue());

        return cucumberCentredWrapper;
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
     * @return JPanel with a bunPromptLabel and the bunSelector combobox left aligned (x-axis) and centre-aligned (y-axis)
     */
    private JPanel makeBunPromptAndSelectorPanel(){
        JPanel bunPanel = new JPanel();
        bunPanel.setLayout(new BoxLayout(bunPanel, BoxLayout.Y_AXIS));

        JLabel bunPromptLabel = new JLabel(Filter.BUN.filterPrompt());
        bunPromptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //Cajole the combobox to just take up a single line like the other comboboxes despite the space it has to grow.
        this.bunSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.bunSelector.getPreferredSize().height));
        this.bunSelector.setAlignmentX(Component.LEFT_ALIGNMENT);

        bunPanel.add(Box.createVerticalGlue());
        bunPanel.add(bunPromptLabel);
        bunPanel.add(Box.createRigidArea(new Dimension(0,5))); //breathing space between prompt label and combobox
        bunPanel.add(this.bunSelector);
        bunPanel.add(Box.createVerticalGlue());

        return bunPanel;
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
        if (options == null || options.isEmpty()) {
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
     * Validates the relevant ButtonGroup selection--whether 'Yes', 'No' or 'I don't mind' (or equivalent)
     * @param buttonGroup the ButtonGroup to query
     * @param dontMindText String of the ButtonGroup's text for I_DONT_MIND selection
     * @return true for 'Yes' selection, false for 'No' selection, and null for 'I don't mind' or equivalent selection.
     * @throws IllegalStateException throws exception if the ButtonGroup does not contain 'Yes', 'No'
     * or the provided dontMindText.
     */
    private Boolean getBooleanSelectionFromGroup(ButtonGroup buttonGroup, String dontMindText) throws IllegalStateException {
        String selection = getSelectedButtonText(buttonGroup);

        //valid 'Skip' choice by not selecting or selecting 'I Don't Mind'
        if  (selection == null || selection.equals(dontMindText)) {
            return null;
        } else if (selection.equalsIgnoreCase("Yes")) {
            return true;
        } else if (selection.equalsIgnoreCase("No")) {
            return false;
        } else {
            throw new IllegalStateException("Invalid button text found in ButtonGroup: '" + selection + "'");
        }
    }

    /**
     * Sets up the JList selectors with ListSelectionListeners.
     * <p>They'll call handleSpecialSelections() to check for and deal with NONE or I_DONT_MIND selections.
     */
    private void setupJListSelectionListeners() {
        //getValueIsAdjusting to only trigger after a selection event is completed:
        //https://docs.oracle.com/javase/8/docs/api/javax/swing/ListSelectionModel.html
        sauceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSpecialSelections(sauceList, Filter.SAUCE_S.getDontMindValue());
            }
        });

        proteinList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSpecialSelections(proteinList, Filter.PROTEIN.getDontMindValue());
            }
        });

        leafyGreensList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSpecialSelections(leafyGreensList, Filter.LEAFY_GREENS.getDontMindValue());
            }
        });
    }

    /**
     * Helper to handle selection of SpecialChoices in JLists.
     * <li>Clears the list and shows an error if I_DONT_MIND and NONE are simultaneously selected. Shows a warning message too.
     * <li>If only one of I_DONT_MIND or NONE is selected, clears all other selections.
     * @param selector the JList of Objects that has been selected
     * @param dontMindValue the Filter's particular value for I_DONT_MIND or equivalent.
     */
    private void handleSpecialSelections(JList<Object> selector, Object dontMindValue) {
        List<Object> selected = selector.getSelectedValuesList();

        boolean hasExplicitNone = selected.contains(SpecialChoice.NONE);
        boolean hasDontMind = selected.contains(dontMindValue);

        if (hasExplicitNone && hasDontMind) {
            JOptionPane.showMessageDialog(
                    corePanel,
                    "Cannot select '" + SpecialChoice.NONE + "' and '" + dontMindValue + "' together."
                            +"\nPlease choose one or the other.",
                    "Invalid Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            selector.clearSelection();
        }

        if (((hasExplicitNone || hasDontMind)) && selected.size() > 1) {
            //Invokelater because gui behaviour is queued while technically still selecting.
            SwingUtilities.invokeLater(() -> {
                if (hasExplicitNone) selector.setSelectedValue(SpecialChoice.NONE, true);
                else  selector.setSelectedValue(dontMindValue, true);
            });
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

    /**
     * Gathers the current state of all input components and bundles them into an immutable Record.
     * <p>This will deliberately throw an NPE if Filter class stops providing I_DONT_MIND values for TOMATO and CUCUMBER.
     * @return new FilterSelections record.
     */
    public FilterSelections getFilterSelections(){
        return new FilterSelections(
                (Type) itemTypeSelector.getSelectedItem(), //Type is always concrete, so might as well recast directly.
                bunSelector.getSelectedItem(),
                Set.copyOf(sauceList.getSelectedValuesList()),
                dressingSelector.getSelectedItem(),
                Set.copyOf(leafyGreensList.getSelectedValuesList()),
                Set.copyOf(proteinList.getSelectedValuesList()),
                getBooleanSelectionFromGroup(tomatoGroup, Filter.TOMATO.getDontMindValue().toString()),
                getBooleanSelectionFromGroup(cucumberGroup, Filter.CUCUMBER.getDontMindValue().toString()),
                pickleCheckBox.isSelected(),
                cheeseSelector.getSelectedItem(),
                priceMinField.getText(),
                priceMaxField.getText()
        );
    }

    /**
     * Identifies if any selectors relevant to the Type were missing a selection.
     * <p>Creates a String of the missing selections within an appropriate error message
     * <p>Doubles as a boolean check by empty String return if all are selected.
     * @return String with message if selectors are missing, <b>or empty String</b> if all selectors have selections
     */
    public String getMissingSelectionsMessage() {
        //Hold the identifiers for missing selectors before appending to return String.
        List<String> missing = new ArrayList<>();

        //Get the Type selected first for branching panel display/check logic
        Type selectedType =  (Type) itemTypeSelector.getSelectedItem();

        if (selectedType == null) missing.add(Filter.TYPE.toString());

        //GENERAL FILTERS
        if (proteinList.getSelectedValuesList().isEmpty()) missing.add(Filter.PROTEIN.toString());
        if (getSelectedButtonText(tomatoGroup) == null) missing.add(Filter.TOMATO.toString());
        if (cheeseSelector.getSelectedItem() == null) missing.add(Filter.CHEESE.toString());
        //pickleCheckBox is always checked or unchecked (boolean)--no logical error state

        //TYPE-SPECIFIC FILTERS
        if (selectedType == Type.BURGER) {
            if (sauceList.getSelectedValuesList().isEmpty()) missing.add(Filter.SAUCE_S.toString());
            if (bunSelector.getSelectedItem() == null) missing.add(Filter.BUN.toString());
        } else if (selectedType == Type.SALAD) {
            if (leafyGreensList.getSelectedValuesList().isEmpty()) missing.add(Filter.LEAFY_GREENS.toString());
            if (getSelectedButtonText(cucumberGroup) == null) missing.add(Filter.CUCUMBER.toString());
            if (dressingSelector.getSelectedItem() == null) missing.add(Filter.DRESSING.toString());
        }

        if (missing.isEmpty()) return ""; //Return empty String if all relevant selectors had selections.

        // If selectors were missing selections
        return "Please make a selection for: " + String.join(", ", missing) +
                "\n\nYou can select '" + SpecialChoice.I_DONT_MIND + "' if you have no preference, "
                +"\nor '" +SpecialChoice.NONE + "' if you know you don't want this item in your food.";

    }

    //                                  *** PUBLIC SETTERS ***

    /**
     * Clear all selections. Intended for use when navigating back here from a different view.
     */
    public void clearSelections() {
        itemTypeSelector.setSelectedIndex(-1);
        bunSelector.setSelectedIndex(-1);
        sauceList.clearSelection();
        dressingSelector.setSelectedIndex(-1);
        leafyGreensList.clearSelection();
        proteinList.clearSelection();
        tomatoGroup.clearSelection();
        cucumberGroup.clearSelection();
        pickleCheckBox.setSelected(false);
        cheeseSelector.setSelectedIndex(-1);
        priceMinField.setText("0.00");
        priceMaxField.setText("0.00");
    }
}
