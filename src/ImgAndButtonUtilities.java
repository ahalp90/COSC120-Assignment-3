import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;

/**
 * A public utility class for loading images and making buttons with responsively resized images.
 * Contains only static methods.
 */
public final class ImgAndButtonUtilities {
    //CONSTANT FOR ACCEPTABLE IMAGE EXTENSIONS
    private static final List<String> imgExtensions = List.of("png", "jpg", "jpeg", "gif");

    /**
     * Private constructor to prevent instantiation of this utility class.
     * A trick I learned when writing tests and aiming for 100% coverage in another project.
     */
    private ImgAndButtonUtilities() {}

    /**
     * Loads a BufferedImage from a given path.
     * <p>Calls helpers to check against known whitelisted image-type extension
     * in case of erroneous extension provided.
     * <p>Returns a placeholder if the image could not be successfully read.
     * @param imagePath String of the image path to try
     * @return a BufferedImage of either the loaded image (success) or a placeholder yellow square (failure).
     */
    public static BufferedImage loadBufferedImage(String imagePath) {
        //CHECK A FILE EXISTS WITH THE GIVEN FILEPATH--ELSE, TRY TO FIND ONE WITH A WHITELISTED EXTENSION
        String knownImagePath = findFileWithWhitelistedExtension(imagePath);

        //EARLY EXIT WITH PLACEHOLDER if no file existed with any valid extension
        if (knownImagePath == null) {
            return placeHolderImage();
        }

        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(knownImagePath));
        } catch (IOException e) {
            System.err.println("Error loading image from " + imagePath + "\n" + e.getMessage());
            bufferedImage = placeHolderImage();
        }
        // ImageIO can weirdly return
        if (bufferedImage == null) {
            System.err.println("Error loading image from " + imagePath +"\n Null returned due to unsupported filetype");
            bufferedImage = placeHolderImage();
        }
        if (bufferedImage.getWidth() <= 0 || bufferedImage.getHeight() <= 0) {
            System.err.println("Error: Image dimensions are invalid for image at " + imagePath);
            bufferedImage = placeHolderImage();
        }
        return bufferedImage;
    }

    /**
     * Creates a JButton that displays a single, responsively resized image as its content.
     * <p>The button is transparent--only its icon is shown.
     * <p>This uses custom drawing to scale to the provided dimensions.
     *
     * Code for responsive icon painting adapted from:
     * https://stackoverflow.com/questions/78701695/how-do-i-scale-or-set-the-the-size-of-an-imageicon-in-java
     * The original code used Image, but I have opted for BufferedImage based on this:
     * https://docs.oracle.com/javase/tutorial/2d/images/index.html
     * Rendering hints inspired by my work on a Java Chess gui and also this post here:
     * https://stackoverflow.com/questions/59431324/java-how-to-make-an-antialiasing-line-with-graphics2d
     * @param imagePath String of the file path of the image to use for the button
     * @param size Dimension of the preferred size of the button
     * @return a JButton that shows the scaled image.
     */
    public static JButton makeImgOnlyButtonWithResize(String imagePath, Dimension size) {
        final BufferedImage sourceImage = loadBufferedImage(imagePath); //IntelliJ warning final for inner class

        JButton button = new JButton();
        button.setPreferredSize(size);
        button.setContentAreaFilled(false);

        button.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                g2d.drawImage(sourceImage, x, y, c.getWidth(), c.getHeight(), c);

                g2d.dispose();
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
     * Helper to create a placeholder image for an image that failed to load.
     * <p>I'll just be a 50*50 yellow square, but it'll allow the user to order while signalling an image load failure.
     * <p>Code adapted from https://stackoverflow.com/questions/1440750/set-bufferedimage-to-be-a-color-in-java
     * @return a 50x50 yellow BufferedImage
     */
    public static BufferedImage placeHolderImage() {
        BufferedImage placeholder = new BufferedImage(50,50,BufferedImage.TYPE_INT_RGB);
        Graphics g2d = placeholder.createGraphics();
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(0,0,50,50);
        g2d.dispose();
        return placeholder;
    }

    /**
     * Loops through the whitelist of valid image extensions in case a file exists
     * but was provided with the wrong extension.
     * @param imagePath String of the image path to try
     * @return a String of a filepath known to exist--either as originally supplied or
     * with one of the whitelisted extensions;
     * <b>returns null if no file was found with any valid extension<b>.
     */
    public static String findFileWithWhitelistedExtension(String imagePath) {
        //Early return if the file exists as given.
        if (Files.isRegularFile(Paths.get(imagePath))) {return imagePath;}

        int fullstop = imagePath.lastIndexOf('.');
        //File needs at least 1 character before its extension
        if (fullstop < 1) {
            System.err.println("Error loading image from " + imagePath + "\nThe image did not contain a file extension.");
            return null;
        }

        String base =  imagePath.substring(0, fullstop);

        for (String ext :  imgExtensions) {
            String possiblePath = base+"."+ext;
            //IF FILE EXISTS WITH ONE OF THE WHITELISTED EXTENSIONS, RETURN THAT AS PATH TO TRY
            if (Files.isRegularFile(Paths.get(possiblePath))) {return possiblePath;}
        }

        //HANDLE TOTAL FAILURE
        String allExts = String.join("\n", imgExtensions);
        System.err.println("No file found for: " + imagePath + ". Extensions allowed include:\n" + allExts);
        return null;
    }
}
