import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.StringJoiner;
import java.util.List;

public class PersonalDetailsPanel {

    private final JPanel corePanel;
    private final JTextArea orderSummaryArea;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField emailField;
    private final JButton submitButton;
    private final JButton backButton;

    private PersonalDetailsListener listener;

    public PersonalDetailsPanel() {

        //INITIALISE FIELD OBJECTS
        corePanel = new JPanel(new BorderLayout(10,10));
        corePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        this.orderSummaryArea  = new JTextArea(5,30); //5 rows tall, and wide enough for 30 chars.
        this.nameField = new JTextField();
        this.phoneField = new JTextField();
        this.emailField = new JTextField();
        this.backButton = new JButton("Back to Item Selection");
        this.submitButton = new JButton("Submit my Order");

        //INSTANTIATE THE CORE PANEL COMPONENTS AND COMPOSE IT.
        JPanel summaryPanel = createSummaryPanel();
        JPanel textFieldsPanel = createTextFieldsPanel();
        JPanel buttonPanel = createButtonPanel();

        corePanel.add(summaryPanel, BorderLayout.NORTH);
        corePanel.add(textFieldsPanel, BorderLayout.CENTER);
        corePanel.add(buttonPanel, BorderLayout.SOUTH);

        addListeners();
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout(0,5));

        JLabel summaryTitle = new JLabel("Your Order Summary", SwingConstants.CENTER);
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 16));

        orderSummaryArea.setEditable(false);
        orderSummaryArea.setLineWrap(true);
        orderSummaryArea.setWrapStyleWord(true);
        JScrollPane summaryScrollPane = new JScrollPane(orderSummaryArea);

        summaryPanel.add(summaryTitle, BorderLayout.NORTH);
        summaryPanel.add(summaryScrollPane, BorderLayout.CENTER);
        return summaryPanel;
    }

    private JPanel createTextFieldsPanel() {
        JPanel textFieldsPanel = new JPanel(new GridLayout(3,2,10,10)); //3 rows 2 cols and some gaps around
        textFieldsPanel.setBorder(new EmptyBorder(10,0,10,10)); //Bit of vertical breathing space

        textFieldsPanel.add(new JLabel("Full Name:", SwingConstants.CENTER));
        textFieldsPanel.add(nameField);
        textFieldsPanel.add(new JLabel("Phone:", SwingConstants.CENTER));
        textFieldsPanel.add(phoneField);
        textFieldsPanel.add(new JLabel("Email:", SwingConstants.CENTER));
        textFieldsPanel.add(emailField);

        return textFieldsPanel;
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
            if (listener != null) {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                listener.onFinalSubmitOrder(name, phone, email);
            }
        });
    }

    public void displayOrderSummary(List<MenuItem> items) {
        //Format with commas to separate thousands, and decimals become 2f
        // DecimalFormat syntax from https://www.baeldung.com/java-decimalformat
        DecimalFormat df = new DecimalFormat("#,##0.00");
        StringJoiner sj = new StringJoiner("\n");
        double total = 0.0;

        for (MenuItem item : items) {
            sj.add(" * " + item.getMenuItemName() + " " + df.format(item.getPrice()));
            total += item.getPrice();
        }
        sj.add("\t\t**********" + df.format(total));;
        sj.add("\nTotal:\t$" + df.format(total));

        orderSummaryArea.setText(sj.toString());
        orderSummaryArea.setCaretPosition(0); //autoscroll to top
    }

    /**
     * Gets the main JPanel that visually composes this object's components
     * @return JPanel
     */
    public JPanel getCorePanel() {return this.corePanel;}

    /**
     * Register the listener that handles the actions from this panel
     * @param listener an object (should be OrderGui) that implements PersonalDetailsListener
     */
    public void setPersonalDetailsListener(PersonalDetailsListener listener) {
        this.listener = listener;
    }
}
