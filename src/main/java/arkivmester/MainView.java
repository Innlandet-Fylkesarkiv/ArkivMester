package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the main view, which is the front page.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class MainView extends Views{
    private JFrame f;
    private Container container;
    private JPanel topPanel;
    private JPanel infoPanel;
    private JPanel mainPanel;
    JLabel spinnerLabel;

    //Buttons
    private JButton editInfoBtn;
    private JButton chooseTestsBtn;
    private JButton startTestingBtn;
    private JButton settingsBtn;
    private JButton aboutBtn;

    //Info field list
    List<JTextArea> valueList = new ArrayList<>();

    /**
     * Creates and shows the GUI
     */
    public void createAndShowGUI() {
        //Top panel
        topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(primaryColor);
        setUpTopPanel(topPanel);

        //Container
        container = f.getContentPane();
        container.setLayout(new BorderLayout(15, 10));
        container.setBackground(Color.WHITE);

        //Main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);
        setUpMainPanel(mainPanel);

        //Info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout(0, 0));
        setUpInfoPanel(infoPanel);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(0, 0, 0, 125));

        //Adding components together
        container.add(mainPanel);
        container.add(infoPanel, BorderLayout.EAST);
        container.add(topPanel, BorderLayout.NORTH);
        container.validate();
    }

    /**
     * Shows the main view after the container has been reset.
     */
    public void showGUI() {
        container.add(mainPanel);
        container.add(infoPanel, BorderLayout.EAST);
        container.add(topPanel, BorderLayout.NORTH);
        f.setContentPane(container);
    }

    /**
     * Creates and sets the properties for the frame (window application).
     */
    public void createFrame(){
        //Frame properties
        f = new JFrame("ArkivMester");

        URL url = ClassLoader.getSystemResource("appicon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        f.setIconImage(img);

        f.setSize(windowWidth, windowHeight);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //#NOSONAR
        f.setLocationRelativeTo(null);
        f.setResizable(false);
    }

    /**
     * Sets up the top panel.
     */
    private void setUpTopPanel(JPanel topPanel) {
        //Top panel buttons
        settingsBtn = new JButton("Innstillinger");
        settingsBtn.addActionListener(this);
        settingsBtn.setBackground(Color.WHITE);
        settingsBtn.setToolTipText("Viser innstillinger.");

        aboutBtn = new JButton("Om");
        aboutBtn.addActionListener(this);
        aboutBtn.setBackground(Color.WHITE);
        aboutBtn.setToolTipText("Viser informasjon om applikasjonen.");

        topPanel.add(settingsBtn);
        topPanel.add(aboutBtn);
    }

    /**
     * Sets up the main panel.
     */
    private void setUpMainPanel(JPanel mainPanel) {
        //Vertical gap: 50 and 70

        //Section 1
        JLabel archiveLabel = new JLabel("Last opp arkivuttrekk:");
        archiveLabel.setBounds(100, 50, 200, 30);
        archiveLabel.setFont(primaryFont);

        URL imageURL = ClassLoader.getSystemResource("spinner.gif");
        ImageIcon imageIcon;
        spinnerLabel = new JLabel();
        if (imageURL != null) {
            imageIcon = new ImageIcon(imageURL);
            spinnerLabel.setIcon(imageIcon);
            imageIcon.setImageObserver(spinnerLabel);
            spinnerLabel.setBounds(300, 50, 200, 30);
        }
        spinnerLabel.setVisible(false);

        JButton uploadTarBtn = new JButton("Last opp pakket uttrekk");
        uploadTarBtn.setBounds(100, 100, 200, 30);
        uploadTarBtn.addActionListener(this);
        uploadTarBtn.setBackground(primaryColor);
        uploadTarBtn.setForeground(Color.WHITE);
        uploadTarBtn.setToolTipText("??pner filutforsker for ?? velge riktig pakket uttrekk.");

        //Section 2
        JLabel testsLabel = new JLabel("Test arkivuttrekk:");
        testsLabel.setFont(primaryFont);
        testsLabel.setBounds(100, 170, 300, 30);

        chooseTestsBtn = new JButton("Velg tester");
        chooseTestsBtn.setBounds(100, 220, 125, 30);
        chooseTestsBtn.addActionListener(this);
        chooseTestsBtn.setBackground(primaryColor);
        chooseTestsBtn.setForeground(Color.WHITE);
        chooseTestsBtn.setEnabled(false);
        chooseTestsBtn.setToolTipText("Viser deltest siden for ?? velge hvilke deltester ?? inkludere.");

        startTestingBtn = new JButton("Start testing");
        startTestingBtn.setBounds(275, 220, 125, 30);
        startTestingBtn.addActionListener(this);
        startTestingBtn.setBackground(primaryColor);
        startTestingBtn.setForeground(Color.WHITE);
        startTestingBtn.setEnabled(false);
        startTestingBtn.setToolTipText("Starter testingen av uttrekket.");

        //Adding components
        mainPanel.add(archiveLabel);
        mainPanel.add(spinnerLabel);
        mainPanel.add(uploadTarBtn);

        mainPanel.add(testsLabel);
        mainPanel.add(chooseTestsBtn);
        mainPanel.add(startTestingBtn);
    }

    /**
     * Sets up the info panel.
     */
    private void setUpInfoPanel(JPanel infoPanel) {
        int rows = 8;

        JLabel infoLabel = new JLabel("Informasjon om uttrekket:");
        infoLabel.setFont(primaryFont);

        editInfoBtn = new JButton("Rediger informasjon");
        editInfoBtn.addActionListener(this);
        editInfoBtn.setBackground(primaryColor);
        editInfoBtn.setForeground(Color.WHITE);
        editInfoBtn.setEnabled(false);
        editInfoBtn.setToolTipText("Viser redigerings siden for administrative informasjon.");

        //Grid
        JPanel infoGrid = new JPanel(new GridBagLayout());
        infoGrid.setBorder(new EmptyBorder(42, 0, 0, 0));
        infoGrid.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;


        //Col 1
        JLabel name1 = new JLabel("UttrekksID:");
        JLabel name2 = new JLabel("Kommune/Kunde:");
        JLabel name3 = new JLabel("Kontaktperson:");
        JLabel name4 = new JLabel("Uttrekksformat:");
        JLabel name5 = new JLabel("Produksjonsdato for uttrekket:");
        JLabel name6 = new JLabel("Uttrekk mottatt dato:");
        JLabel name7 = new JLabel("Test utf??rt av:");
        JLabel name8 = new JLabel("Dato for rapport:");

        //Col 2
        for(int i = 0; i<rows; i++) {
            JTextArea textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setColumns(15);
            valueList.add(textArea);
        }

        //Adding components together
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10,0,0,10);

        infoGrid.add(infoLabel, gbc);
        gbc.gridy++;

        //Col 1
        infoGrid.add(name1, gbc);
        gbc.gridy++;
        infoGrid.add(name2, gbc);
        gbc.gridy++;
        infoGrid.add(name3, gbc);
        gbc.gridy++;
        infoGrid.add(name4, gbc);
        gbc.gridy++;
        infoGrid.add(name5, gbc);
        gbc.gridy++;
        infoGrid.add(name6, gbc);
        gbc.gridy++;
        infoGrid.add(name7, gbc);
        gbc.gridy++;
        infoGrid.add(name8, gbc);
        gbc.gridx++;

        //Col 2
        gbc.gridy = 1;

        for(int i = 0; i<rows; i++) {
            infoGrid.add(valueList.get(i), gbc);
            gbc.gridy++;
        }

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx--;

        infoGrid.add(editInfoBtn, gbc);

        infoPanel.add(infoGrid, BorderLayout.NORTH);
    }

    /**
     * Regular getter for container.
     * @return Frame's container used for creating new views.
     */
    public Container getContainer() {
        return container;
    }

    /**
     * Toggles edit admin info button's visibility.
     */
    public void toggleEditInfoBtn(){
        editInfoBtn.setVisible(!editInfoBtn.isVisible());
    }

    /**
     * Activates buttons when archive as been uploaded.
     */
    public void activateButtons() {
        chooseTestsBtn.setEnabled(true);
        startTestingBtn.setEnabled(true);
        editInfoBtn.setEnabled(true);
        //writeReportBtn.setEnabled(!writeReportBtn.isEnabled()); #NOSONAR
    }

    /**
     * Deactivates buttons when view is reset.
     */
    private void deactivateButtons() {
        chooseTestsBtn.setEnabled(false);
        startTestingBtn.setEnabled(false);
        editInfoBtn.setEnabled(false);
    }

    /**
     * Resets administrative information fields.
     */
    public void resetManualInfo() {
        for (JTextArea value: valueList) {
           value.setText("");
        }
    }

    /**
     * Resets main view.
     *
     * The same as calling resetManualInfo(), deactivateButtons() and toggleEditInfoBtn().
     */
    public void resetMainView() {
        resetManualInfo();
        deactivateButtons();
        toggleEditInfoBtn();
    }

    /**
     * Updates administrative information fields
     * @param list Contains data in the correct order and must be same size as amount of fields.
     */
    public void updateAdminInfo(List<String> list) {
        for (int i = 0; i<list.size(); i++) {
            valueList.get(i).setText(list.get(i));
        }
    }

    /**
     * Creates a popup pane with error symbol and exception message.
     * @param msg Contains the exception message for the popup pane.
     */
    public void exceptionPopup(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Feil", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Deactivates the settings button if activated and vice versa.
     */
    public void toggleSettingsBtn() {
        settingsBtn.setEnabled(!settingsBtn.isEnabled());
    }

    /**
     * Deactivates the about button if activated and vice versa.
     */
    public void toggleAboutBtn() {
        aboutBtn.setEnabled(!aboutBtn.isEnabled());
    }

    /**
     * Sets the loading spinner for uploading archives to visible or invisible.
     * @param running True for visible, false for invisible.
     */
    public void loading(Boolean running) {
        spinnerLabel.setVisible(Boolean.TRUE.equals(running));
    }
}
