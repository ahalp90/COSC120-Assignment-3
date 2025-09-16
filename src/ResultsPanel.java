import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResultsPanel {
    private final JPanel corePanel;
    private final JLabel titleLabel;
    private final JPanel itemsListPanel;
    private final JButton proceedButton;
    private final JButton backButton;

    private ResultsPanelListener listener;

    //Store the MenuItemPanels to display.
    private final List<MenuItemPanel> menuItemPanels = new ArrayList<>();

    public ResultsPanel(){
        corePanel = new JPanel(new BorderLayout(10,10));
        corePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        titleLabel = new JLabel("Search Results", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        itemsListPanel = new JPanel();
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(itemsListPanel);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        //Default scroll is so slow! Fix from:
        //https://stackoverflow.com/questions/10119587/how-to-increase-the-slow-scroll-speed-on-a-jscrollpane
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        backButton = new JButton("Back to Search");
        proceedButton = new JButton("Confirm Selection and Order");

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtonPanel.add(backButton);
        bottomButtonPanel.add(proceedButton);

        corePanel.add(titleLabel, BorderLayout.NORTH);
        corePanel.add(scrollPane, BorderLayout.CENTER);
        corePanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        addListeners();
    }

    private void addListeners() {
        backButton.addActionListener(e -> {
            if (listener != null) {listener.onBackButtonPressed();}
        });

        proceedButton.addActionListener(e -> {
            List<MenuItem> selectedItems = getSelectedItems();
            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(
                        corePanel,
                        "Please select at least one item to add to your order.",
                        "No Items Selected",
                        JOptionPane.WARNING_MESSAGE);
            } else if (listener != null) {
                listener.onProceedToDetails(selectedItems);
            }
        });
    }

    public void displayItems(List<MenuItem> items, String title) {
        //CLEAR ANY ITEMS FROM A PREVIOUS SEARCH
        itemsListPanel.removeAll();
        menuItemPanels.clear();
        titleLabel.setText(title);

        if (items.isEmpty()) {
            JLabel noItemsLabel = new JLabel("No Items to Display.", SwingConstants.CENTER);
            itemsListPanel.add(noItemsLabel);
        } else {
            for (MenuItem item : items) {
                MenuItemPanel itemPanel = new MenuItemPanel(item);
                menuItemPanels.add(itemPanel);
                itemsListPanel.add(itemPanel.getCorePanel());
                itemsListPanel.add(Box.createRigidArea(new Dimension(0, 5))); //Add a small separator
            }
        }
        //Explicit instruction to rejig Component proportions to fit new Components and actually show them on screen.
        this.corePanel.revalidate();
        this.corePanel.repaint();
    }

    public JPanel getCorePanel() {return this.corePanel;}

    /**
     * Set the listener that will handle this Panel's events
     * <p>This panel uses a single listener because it's designed to send events directly back to its parent container
     * @param listener ResultsPanelListener interface, though ultimately it's the OrderGui listening.
     */
    public void setResultsPanelListener(ResultsPanelListener listener) {
        this.listener = listener;
    }

    private List<MenuItem> getSelectedItems() {
        List<MenuItem> selected = new ArrayList<>();
        for (MenuItemPanel panel : menuItemPanels) {
            if (panel.isSelected()) {
                selected.add(panel.getMenuItem());
            }
        }
        return selected;
    }

}
