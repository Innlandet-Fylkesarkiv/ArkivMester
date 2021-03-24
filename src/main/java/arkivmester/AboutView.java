package arkivmester;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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

        //Logo panel
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(null);
        aboutPanel.setBackground(Color.WHITE);
        setUpAboutPanel(aboutPanel);



        //Adding components
        container.add(aboutPanel);
    }

    /**
     * Sets up the about panel.
     */
    private void setUpAboutPanel(JPanel aboutPanel) throws IOException {
        //Logo
        BufferedImage img = ImageIO.read(getClass().getResourceAsStream("/appicon.png"));
        ImageIcon icon = new ImageIcon(img);
        JLabel logoLbl = new JLabel();
        logoLbl.setIcon(icon);
        logoLbl.setBounds(100, 50, 200, 50);

        JLabel appLbl = new JLabel("ArkivMester");
        appLbl.setBounds(175, 50, 200, 50);
        appLbl.setFont(primaryFont);

        //Information
        JTextArea info = new JTextArea("Versjon: 1.0" + System.getProperty("line.separator") +
                "Utviklere: Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen");
        info.setEditable(false);
        info.setBounds(100, 150, 1000, 50);

        //Button
        JButton backBtn = new JButton("Tilbake");
        backBtn.setActionCommand("Avbryt");
        backBtn.addActionListener(this);
        backBtn.setBackground(primaryColor);
        backBtn.setForeground(Color.WHITE);
        backBtn.setBounds(100, 200, 85, 25);
        backBtn.setToolTipText("Går tilbake til forsiden.");

        aboutPanel.add(logoLbl);
        aboutPanel.add(appLbl);
        aboutPanel.add(info);
        aboutPanel.add(backBtn);
    }

    /**
     * Clears the entire container.
     */
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }
}
