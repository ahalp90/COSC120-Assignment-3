import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.StringJoiner;
import java.util.List;

public class OrderCreationPanel {

    private final JPanel corePanel;
    private final JTextArea orderSummaryArea;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextArea customisationsTextArea;
    private final JCheckBox takeawayCheckBox;
    private final JButton submitButton;
    private final JButton backButton;
    private final JTextArea detailsArea;
    private final JSplitPane splitPane;
    private final JLabel detailsHelperLabel;

    //CONSTANTS
    private final static int DIVIDER_LOCATION = 700;
    private final static String DETAILS_HELPER_DEFAULT_TEXT =
            "<html><i>For any preferences, finalise them in the customisations box below</i></html>";

    //Fields to update with order details and pass out at Order record creation
    private List<MenuItem> orderedItems;
    private String finalCheeseSelection;

    private OrderCreationPanelListener listener;

    public OrderCreationPanel() {

        //INITIALISE FIELD OBJECTS
        this.corePanel = new JPanel(new BorderLayout());
        this.orderSummaryArea  = new JTextArea(5,30); //5 rows tall, and wide enough for 30 chars.
        this.nameField = new JTextField();
        this.phoneField = new JTextField();
        this.takeawayCheckBox = new JCheckBox();
        this.customisationsTextArea = new JTextArea();
        this.backButton = new JButton("Back to Item Selection");
        this.submitButton = new JButton("Submit my Order");
        this.detailsArea = new JTextArea();
        this.splitPane = new JSplitPane();
        this.detailsHelperLabel = new JLabel(DETAILS_HELPER_DEFAULT_TEXT);


        //INSTANTIATE THE CORE PANEL COMPONENTS AND COMPOSE IT.
        createMainComponentsAndComposeCorePanel();

        addListeners();
    }

    private void createMainComponentsAndComposeCorePanel() {
        JPanel summaryPanel = createSummaryPanel();
        JPanel inputsPanel = createInputsPanel();
        JPanel buttonPanel = createButtonPanel();

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(inputsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel detailsPanel = createItemDetailsPanel();

        // Allows user to drag horizontally to show more or less info about the items ordered
        //https://docs.oracle.com/javase/tutorial/uiswing/components/splitpane.html
        this.splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        this.splitPane.setLeftComponent(mainPanel);
        this.splitPane.setRightComponent(detailsPanel);
        splitPane.setDividerLocation(DIVIDER_LOCATION); //default to mostly closed initially
        splitPane.setResizeWeight(1.0); //All extra space goes to left panel by default


        this.corePanel.add(splitPane, BorderLayout.CENTER);
    }

    /**
     * A panel to show the details of items added to the order.
     * @return Jpanel with JTextArea, scroll and titled border
     */
    private JPanel createItemDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,5));
        panel.setBorder(BorderFactory.createTitledBorder("Item Details"));
        panel.setMinimumSize(new Dimension(200,0)); //make sure it always shows

        this.detailsArea.setEditable(false);
        this.detailsArea.setLineWrap(true);
        this.detailsArea.setWrapStyleWord(true);

        panel.add(detailsHelperLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(this.detailsArea), BorderLayout.CENTER);
        return panel;
    }

    private void setDetailsHelperLabelText() {
        StringBuilder text = new StringBuilder("<html><i>");

        if (Filter.CHEESE.allowsDontMindChoice()
                && this.finalCheeseSelection.contains(Filter.CHEESE.getDontMindValue().toString())) {
            text.append("You selected <b>")
                    .append(Filter.CHEESE.getDontMindValue())
                    .append("</b> for cheese.<br>")
                    .append("Please specify your preference in customisations, along with preferences for any other ingredients--")
                    .append("or leave blank for chef's choice.");
        } else{
            text.append("Your cheese preference: <b>")
                    .append(this.finalCheeseSelection)
                    .append("</b><br>");
            text.append("People are serious about their cheese, ")
                    .append("so we've sent yours straight to the order.<br>")
                    .append("For any other preferences, use the customisations box.");
        }

        text.append("</i></html>");
        this.detailsHelperLabel.setText(text.toString());
    }

    private JPanel createSummaryPanel() {
        this.orderSummaryArea.setEditable(false);
        this.orderSummaryArea.setLineWrap(true);
        this.orderSummaryArea.setWrapStyleWord(true);
        //Set a logical font to allow showing fancy unicode characters:
        //https://stackoverflow.com/questions/4298279/showing-unicode-character-in-java-text-component
        this.orderSummaryArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

        JPanel summaryPanel = new JPanel(new BorderLayout(0,5));

        JLabel summaryTitle = new JLabel("<html><b>Your Order Summary</b></html>", SwingConstants.CENTER);
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 16));


        JScrollPane summaryScrollPane = new JScrollPane(orderSummaryArea); //this scrollpane will appear if needed
        summaryScrollPane.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), //Outer border
                BorderFactory.createEmptyBorder(5,5,5,5) //inner padding
        ));

        summaryPanel.add(summaryTitle, BorderLayout.NORTH);
        summaryPanel.add(summaryScrollPane, BorderLayout.CENTER);
        return summaryPanel;
    }

    private JPanel createInputsPanel() {
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel("<html><b>Full Name:</b></html>"), BorderLayout.WEST);
        namePanel.add(nameField,  BorderLayout.CENTER);

        JPanel phonePanel = new JPanel(new BorderLayout());
        phonePanel.add(new JLabel("<html><b>Phone:</b></html>"),  BorderLayout.WEST);
        phonePanel.add(phoneField,  BorderLayout.CENTER);

        //Gridlayout for name and phone to forcefully give horizontal 50-50 split
        JPanel nameAndPhonePanel = new JPanel(new GridLayout(1,2,10,0)); //1 row 2 col with a bit of horizontal gap
        nameAndPhonePanel.add(namePanel);
        nameAndPhonePanel.add(phonePanel);
        nameAndPhonePanel.setBorder(BorderFactory.createTitledBorder(
                null, "<html><b>Personal details:</b></html>", TitledBorder.CENTER, TitledBorder.TOP));

        JPanel takeawayPanel = new JPanel(new BorderLayout());
        takeawayPanel.add(new JLabel(
                "<html><b>Is this order takeaway?\t</html></b>", SwingConstants.CENTER), BorderLayout.WEST);
        takeawayPanel.add(takeawayCheckBox,   BorderLayout.CENTER);

        JPanel customisationsPanel = createCustomisationsArea();

        //LOCAL CONSTANTS - weights for gridbaglayout
        double NAME_PANEL_AND_PHONE_PANEL_WEIGHT_Y = 0.2;
        double TAKE_AWAY_PANEL_WEIGHT_Y =  0.2;
        double CUSTOMISATIONS_PANEL_WEIGHT_Y =  0.6;

        //THE MAIN PANEL everything's actually being added to
        JPanel inputsPanel = new JPanel(new GridBagLayout());
        inputsPanel.setBorder(new EmptyBorder(10,0,10,10)); //Bit of breathing space

        //gridbaglayout of 3 rows and 1 column, with bulk given to customisations panel
        //first row is a nested gridlayout of 1 row 2 col for name and phone panel
        GridBagConstraints gbc = new GridBagConstraints();
        //Properties relevant to all rows
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 0;
        gbc.weightx = 1.0;

        //FIRST ROW
        //NAME AND PHONE PANEL
        gbc.gridy = 0;
        gbc.weighty = NAME_PANEL_AND_PHONE_PANEL_WEIGHT_Y;
        inputsPanel.add(nameAndPhonePanel, gbc);

        //SECOND ROW - TAKEAWAY PANEL
        gbc.gridy = 1;
        gbc.weighty = TAKE_AWAY_PANEL_WEIGHT_Y;
        inputsPanel.add(takeawayPanel, gbc);

        //THIRD ROW - CUSTOMISATIONS PANEL
        gbc.gridy = 2;
        gbc.weighty = CUSTOMISATIONS_PANEL_WEIGHT_Y;
        inputsPanel.add(customisationsPanel, gbc);


        return inputsPanel;
    }

    private JPanel createCustomisationsArea() {
        JLabel customisationsTitleLabel = new JLabel(
                "<html><b>Describe any customisations you'd like:</b></html>", SwingConstants.CENTER);

        this.customisationsTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        this.customisationsTextArea.setLineWrap(true);
        this.customisationsTextArea.setWrapStyleWord(true);

        //scrollbar for the customisations text will show if necessary
        JScrollPane customisationsScrollPane = new JScrollPane(customisationsTextArea);
        customisationsScrollPane.setBorder(null); //get rid of the scroll's border

        //THE MAIN PANEL being returned
        JPanel  customisationsPanel = new JPanel(new BorderLayout(0,10));
        customisationsPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), //Outer border
                BorderFactory.createEmptyBorder(5,5,5,5) //inner padding
        ));

        customisationsPanel.add(customisationsTitleLabel, BorderLayout.NORTH);
        customisationsPanel.add(customisationsScrollPane, BorderLayout.CENTER);

        return customisationsPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttonPanel.add(this.backButton);
        buttonPanel.add(this.submitButton);
        return buttonPanel;
    }

    private void addListeners() {
        this.backButton.addActionListener(e -> {
            if (listener != null) {
                listener.onBackToMenuSelection();
            }
        });

        submitButton.addActionListener(e -> {
            if (listener != null && this.orderedItems != null) {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                boolean takeaway = takeawayCheckBox.isSelected();
                String customisations = customisationsTextArea.getText().trim();

                //CREATE AN ORDER RECORD TO PASS OUT
                Order order = new Order(
                        name,
                        phone,
                        customisations,
                        this.finalCheeseSelection,
                        takeaway, List.copyOf(this.orderedItems)
                );
                listener.onFinalSubmitOrder(order);
            }
        });
    }

    public void displayOrderSummary(List<MenuItem> items) {
        //STORE THE PASSED-IN ORDER ITEMS TO PASS OUT AT RECORD CREATION
        this.orderedItems = List.copyOf(items);

        //Format with commas to separate thousands, and decimals become 2f
        // DecimalFormat syntax from https://www.baeldung.com/java-decimalformat
        DecimalFormat df = new DecimalFormat("#,##0.00");
        StringJoiner sj = new StringJoiner("\n");
        double total = 0.0;
        String firstChar;

        for (MenuItem item : items) {
            //First char is burger if burger, salad if salad, or asterisk if anything else
            if (item.getDreamItemType().equals(Type.BURGER)) {
                firstChar = "🍔";
            } else if (item.getDreamItemType().equals(Type.SALAD)) {
                firstChar = "🥗";
            } else {
                firstChar = "*";
            }

            sj.add(" " + firstChar + " " + item.getMenuItemName() + "\t\t$" + df.format(item.getPrice()));
            total += item.getPrice();
        }
        sj.add("\n Total:\t\t\t$" + df.format(total));

        orderSummaryArea.setText(sj.toString());
        orderSummaryArea.setCaretPosition(0); //autoscroll to top
    }

    public void addItemDetailsToPanel() {
        StringJoiner details = new StringJoiner("\n");
        for (MenuItem item : this.orderedItems) {
            details.add(item.getMenuItemInformation());
        }

        this.detailsArea.setText(details.toString());
        this.detailsArea.setCaretPosition(0); //autoscroll to top
    }

    /**
     * Gets the main JPanel that visually composes this object's components
     * @return JPanel
     */
    public JPanel getCorePanel() {return this.corePanel;}

    /**
     * Register the listener that handles the actions from this panel
     * @param listener an object (should be OrderGui) that implements OrderCreationPanelListener
     */
    public void setPersonalDetailsListener(OrderCreationPanelListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the cheese to be used for the order; allows core GUI component (OrderGui) to pass through
     * the information and keep the OrderCreationPanel reusable--rather than requiring parameter
     * passing and reinstantiation to pass cheese to an Order record
     * @param cheese a String representation of the cheese choice
     */
    public void setCheeseForOrder(String cheese) {
        this.finalCheeseSelection = cheese;
        setDetailsHelperLabelText();

    }

    /**
     * Resets the field objects to default states so that this view panel can be reused
     */
    public void clearFields() {
        this.nameField.setText("");
        this.phoneField.setText("");
        this.customisationsTextArea.setText("");
        this.takeawayCheckBox.setSelected(false);
        this.orderSummaryArea.setText("");
        this.orderedItems = null;
        this.finalCheeseSelection = null;
        this.detailsArea.setText("");
        this.splitPane.setDividerLocation(DIVIDER_LOCATION);
        this.detailsHelperLabel.setText(DETAILS_HELPER_DEFAULT_TEXT);
    }
}
