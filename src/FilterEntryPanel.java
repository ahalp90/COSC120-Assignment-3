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
 * <li>I DON'T MIND SELECTORS: BUN, SAUCE_S,LEAFY_GREENS, CUCUMBER, DRESSING, PROTEIN, TOMATO
 * <li>ALLOWS EXPLICIT 'NONE' SELECTION: TOMATO, CUCUMBER, PICKLES, SAUCES, PROTEINS AND LEAFY_GREENS
 *
 */
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

        // *** INITIALISE THE CORE PANEL TO WHICH EVERYTHING WILL BE ADDED ***
        this.corePanel = new JPanel();

        // Create a CardLayout and holder Panel--pass to relevant constructors.
        CardLayout typeSpecificCardLayout = new CardLayout();
        JPanel typeSpecificFilterCardsPanel = new JPanel(typeSpecificCardLayout);


        //BUILD THE COMPONENTS AND OVERALL LAYOUT
        buildLayout(typeSpecificFilterCardsPanel);

        //LISTENER FOR TYPE STATE--BURGER OR SALAD; FLIPS VARIABLE COMPONENTS TO RELEVANT CARD
        //NB. This was originally added at the point of the itemType panel's construction, but IntelliJ
        //grumbled because this should happen at a point where both the CardLayout and
        //itemTypeSelector are known to the method.
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

        gbc.gridy = 0; gbc.weighty = 0.1; this.corePanel.add(typeFilterPanel, gbc);

        gbc.gridy = 2; gbc.weighty = 0.35; this.corePanel.add(typeSpecificFilterCardsPanel, gbc);

        gbc.gridy = 1; gbc.weighty = 0.55; this.corePanel.add(sharedFiltersPanel, gbc);
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
        JPanel topRow = new JPanel(new GridLayout(1,2));
        JPanel picklesAndTomatoPanel = makePicklesAndTomatoSelectorPanel();
        JPanel proteinPromptAndSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.PROTEIN);

        topRow.add(picklesAndTomatoPanel);
        topRow.add(proteinPromptAndSelectorPanel);

        //BOTTOM AREA: cheese and price in a horizontal layout
        JPanel bottomRow = new JPanel(new GridLayout(1,2));
        JPanel cheesePromptAndSelectorPanel = makeCheesePromptAndSelectorPanel();
        JPanel pricePromptAndSelectorPanel =  makePricePromptAndSelectorPanel();

        bottomRow.add(cheesePromptAndSelectorPanel);
        bottomRow.add(pricePromptAndSelectorPanel);

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
        JPanel saladFiltersPanel = new JPanel(new GridLayout(1,2));

        //CREATE THE 3 CORE COMPONENTS
        JPanel leafyGreensSelectorPanel = makeMultiSelectionPanelWithLabelAndScroll(Filter.LEAFY_GREENS);
        JPanel cucumberPromptAndSelectorPanel = makeCucumberPromptAndSelectorPanel();
        JPanel dressingPromptAndSelectorPanel = makeDressingPromptAndSelectorPanel();

        //COMPOSE THE LEFT SIDE OF THE PANEL - DRESSING, SPACER AND CUCUMBERS
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
//        JLabel blankLabel = new JLabel();
//        blankLabel.setOpaque(false);
        leftPanel.add(dressingPromptAndSelectorPanel, BorderLayout.NORTH);
//        leftPanel.add(blankLabel);
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
//     * This is a GridBagLayout that looks like:
//     * <li>LEFT 30% bun selection, top anchored and horizontal stretch</li>
//     * <li>20% empty glue</li>
//     * <li>RIGHT 50% sauces multi-selector</li>
     * @return JPanel for all burger-only filter selection
     */
    private JPanel makeBurgerFiltersPanel() {
        JPanel burgerFiltersPanel = new JPanel(new GridLayout(1,2));

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
        JPanel picklesAndTomatoPanel = new JPanel(new BorderLayout()); //PANEL TO RETURN

        //TOMATOES PROMPT AND BUTTONS
        JLabel tomatoPromptLabel = new JLabel(Filter.TOMATO.filterPrompt());
        JRadioButton tomatoButton1 = new JRadioButton(YesNoSkipValidButtonStrings.YES.toString());
        JRadioButton tomatoButton2 = new JRadioButton(YesNoSkipValidButtonStrings.NO.toString());
        JRadioButton tomatoButton3 = new JRadioButton(YesNoSkipValidButtonStrings.I_DONT_MIND.toString());
        //Register with the tomato ButtonGroup; NB THIS IS THE FIELD TOMATOGROUP
        this.tomatoGroup.add(tomatoButton1);
        this.tomatoGroup.add(tomatoButton2);
        this.tomatoGroup.add(tomatoButton3);

        //ORGANISE THE TOMATO BUTTONS IN A VERTICAL GRID
        JPanel tomatoButtonsPanel = new JPanel(new GridLayout(3, 1));
        tomatoButtonsPanel.add(tomatoButton1);
        tomatoButtonsPanel.add(tomatoButton2);
        tomatoButtonsPanel.add(tomatoButton3);

        //PUT THE TOMATO PROMPT AND BUTTONS IN A CONTAINER TOGETHER
        JPanel tomatoSection = new JPanel(new BorderLayout());
        tomatoSection.add(tomatoPromptLabel, BorderLayout.NORTH);
        tomatoSection.add(tomatoButtonsPanel, BorderLayout.CENTER);

        // ***PICKLE SECTION***
        JPanel pickleSection = new JPanel(new BorderLayout());
        JLabel picklePromptLabel = new JLabel(Filter.PICKLES.filterPrompt());

        JPanel checkboxWrapper = new JPanel();
        checkboxWrapper.setLayout(new BoxLayout(checkboxWrapper, BoxLayout.Y_AXIS));
        checkboxWrapper.add(this.pickleCheckBox);
        checkboxWrapper.add(Box.createVerticalGlue());

        pickleSection.add(picklePromptLabel, BorderLayout.NORTH);
        pickleSection.add(checkboxWrapper, BorderLayout.CENTER);

        picklesAndTomatoPanel.add(pickleSection, BorderLayout.CENTER);
        picklesAndTomatoPanel.add(tomatoSection, BorderLayout.SOUTH);

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
        JRadioButton cucumberButton1 = new JRadioButton(YesNoSkipValidButtonStrings.YES.toString());
        JRadioButton cucumberButton2 = new JRadioButton(YesNoSkipValidButtonStrings.NO.toString());
        JRadioButton cucumberButton3 = new JRadioButton(YesNoSkipValidButtonStrings.I_DONT_MIND.toString());
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

        JPanel growthRestrictingWrapper = new JPanel();
        growthRestrictingWrapper.setLayout(new BoxLayout(growthRestrictingWrapper, BoxLayout.Y_AXIS));
        growthRestrictingWrapper.add(Box.createVerticalGlue());
        growthRestrictingWrapper.add(this.bunSelector);
        growthRestrictingWrapper.add(Box.createVerticalGlue());

        //Compose the bun panel to return
        bunFiltersPanel.add(bunPromptLabel, BorderLayout.WEST);
        bunFiltersPanel.add(growthRestrictingWrapper, BorderLayout.CENTER);

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
     * Validates the relevant ButtonGroup selection--whether 'Yes', 'No' or 'Skip' (or skip equivalent)
     * @param buttonGroup the ButtonGroup to query
     * @return true for 'Yes' selection, false for 'No' selection, and null for 'Skip' or equivalent selection.
     * @throws IllegalStateException throws exception if the ButtonGroup does not contain 'Yes' or 'No' options.
     */
    private Boolean getYesNoSkipButtonGroupSelection(ButtonGroup buttonGroup) throws IllegalStateException {
        String selection = getSelectedButtonText(buttonGroup);

        //valid 'Skip' choice by not selecting or selecting 'I Don't Mind'
        if  (selection == null || selection.equals(YesNoSkipValidButtonStrings.I_DONT_MIND.toString())) {
            return null;
        } else if (selection.equals(YesNoSkipValidButtonStrings.YES.toString())) {
            return true;
        } else if (selection.equals(YesNoSkipValidButtonStrings.NO.toString())) {
            return false;
        } else {
            StringJoiner sj = new StringJoiner(", ");
            for (YesNoSkipValidButtonStrings enumValue : YesNoSkipValidButtonStrings.values()) {sj.add(enumValue.toString());}

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
