package arkivmester;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Handles the about view.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class AboutView extends Views {
    private Container container;

    /**
     * Creates and shows the GUI
     */
    public void createAndShowGUI(Container cnt) throws IOException {
        container = cnt;

        //Clears container
        if(container.getComponentCount()==4)
            container.remove(0); //Second info panel
        container.remove(0); //Main panel
        container.remove(0); //Info panel
        container.revalidate();

        //About container
        JPanel aboutContainer = new JPanel(new GridBagLayout());
        //aboutContainer.setBorder(new EmptyBorder(0, 0, 300, 600));
        aboutContainer.setBackground(Color.WHITE);

        //Logo panel
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.LINE_AXIS));
        logoPanel.setBackground(Color.WHITE);
        setUpLogoPanel(logoPanel);

        //Information
       JTextArea info = new JTextArea("Versjon: 1.0" + System.getProperty("line.separator") +
               "Utviklere: Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen");
        info.setEditable(false);

        //Adding components
        aboutContainer.add(logoPanel);
        aboutContainer.add(info);

        container.add(aboutContainer);
    }

    /**
     * Sets up the logo panel.
     */
    private void setUpLogoPanel(JPanel logoPanel) throws IOException {

        BufferedImage img = ImageIO.read(getClass().getResourceAsStream("/appicon.png"));
        ImageIcon icon = new ImageIcon(img);
        JLabel logoLbl = new JLabel();
        logoLbl.setIcon(icon);

        JLabel appLbl = new JLabel("             ArkivMester");

        logoPanel.add(logoLbl);
        logoPanel.add(appLbl);
    }

    /**
     * Clears the entire container.
     */
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }
}
