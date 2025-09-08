import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderGui implements OrderingSystemListener {
    private final JFrame frame;
    private final CardLayout topCardLayout = new CardLayout();
    private final JPanel topCardPanel = new JPanel(topCardLayout);

    private static final Dimension GUI_PREFERRED_SIZE = new Dimension(800, 450);
    // Store subscribers to the GuiListener. Currently only intended to be MenuSearcher--so
    // Collection not strictly needed, but it's reasonable this could expand in the future.
    private final List<GuiListener> listeners = new ArrayList<>();
    private static final int WELCOME_CARD_COLS = 5;
    private static final int WELCOME_CARD_ROWS = 8;


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

        // Lazy meta layout management of the Panel--just split into columns for now and add a 5px border gap.
        // Columns allow easily dedicating a chunk to the sideBanner while keeping other layout details flexible.
        JPanel mainBurgerFilterPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        mainBurgerFilterPanel.setPreferredSize(GUI_PREFERRED_SIZE);


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


        mainBurgerFilterPanel.add(sideBanner); //Add first to go in first of 4 columns.

        return mainBurgerFilterPanel;
    }

    private JPanel filterSelectorsPanel() {
        JPanel localParentPanel = new JPanel (new GridLayout(1, 4)); //Arranges the side banner and main filter selection area.
        localParentPanel.setPreferredSize(GUI_PREFERRED_SIZE);

        //Child JPanel with CardLayout to switch between burger and salad views
        CardLayout cardLayout = new CardLayout();
        JPanel filterCardsPanel = new JPanel(cardLayout);

        //This is a lazy GBL, since it's so fiddly. Combines with sub-components with their own
        //layout managers. Necessary though to give the proteins a full col vertically, while
        //tomatoes & pickles only get half a vertical col each.
        JPanel sharedFiltersPanel = new JPanel(new GridBagLayout());
        JPanel picklesAndTomatoPanel = new JPanel(new GridLayout(2, 2));

        JPanel burgerFiltersPanel = new JPanel(); //decide on layout manager after


        JLabel itemTypePromptLabel = new JLabel(Filter.TYPE.filterPrompt());
        JComboBox<Type> menuItemTypeSelector = new JComboBox<>(Type.values());
        menuItemTypeSelector.addActionListener(e -> {
            Type selectedType = (Type) menuItemTypeSelector.getSelectedItem();
            //This could be simplified, but the explicit cases for both types makes it easier to add new types in future.
            if (selectedType == Type.BURGER) {
                cardLayout.show
                bunSelector.enabled(true); bunPromptLabel.setEnabled(true);
                sauceSelector.enabled(true); sauceSelectorLabel.setEnabled(true);
                greensSelector.enabled(false); greensSelector.setVisible(false); greensSelectorLabel.setEnabled(false);
                cucumberSelector.enabled(false); cucumberSelector.setVisible(false);
                dressingSelector.enabled(false);
                //pickles, tomato, protein and price are not strictly necessary, as they're shared,
                //but would help future developers.
                picklesSelector.enabled(true); picklesPromptLabel.setEnabled(true);
                tomatoSelector.enabled(true); tomatoPromptLabel.setEnabled(true);
                proteinSelector.enabled(true); proteinPromptLabel.setEnabled(true);
                //TODO price selectors both fields
            } else if (selectedType == Type.SALAD) {
                bunSelector.enabled(false); bunPromptLabel.setEnabled(false);
                sauceSelector.enabled(true);
                greensSelector.enabled(true);
                cucumberSelector.enabled(false);
                dressingSelector.enabled(true);
                picklesSelector.enabled(true);
                tomatoSelector.enabled(true);
                proteinSelector.enabled(true); proteinPromptLabel.setEnabled(true);
                //TODO price selectors both fields
            }
        });


//        CardLayout conditionalFiltersCards = new CardLayout();
//        JPanel conditionalFiltersCardsPanel = new JPanel(conditionalFiltersCards);

        JLabel bunPromptLabel = new JLabel(Filter.BUN.filterPrompt());
        JComboBox<Object> bunSelector = new JComboBox<>(MenuSearcher.getAvailableOptions(Filter.BUN).toArray());


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
