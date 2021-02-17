package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//View class for the main window
public class MainView extends Views{
    private JFrame f;
    private Container container;
    private JPanel topPanel;
    private JPanel infoPanel;
    private JPanel mainPanel;

    //Buttons
    private JButton editInfoBtn;
    private JButton chooseTestsBtn;
    private JButton startTestingBtn;
    private JButton writeReportBtn; //#NOSONAR

    //Info field list
    List<JTextArea> valueList = new ArrayList<>();

    MainView() {
        //Empty constructor
    }

    //Sets up GUI
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
    }

    //Shows GUI after its created
    public void showGUI() {
        container.add(mainPanel);
        container.add(infoPanel, BorderLayout.EAST);
        container.add(topPanel, BorderLayout.NORTH);
        f.setContentPane(container);
    }

    //Create frame
    public void createFrame(){
        //Frame properties
        f = new JFrame("ArkivMester");
        f.setSize(windowWidth, windowHeight);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //#NOSONAR
        f.setLocationRelativeTo(null);
        f.setResizable(false);
    }

    //Sets up the top panel
    private void setUpTopPanel(JPanel topPanel) {
        //Top panel buttons
        JButton settingsBtn = new JButton("Innstillinger");
        settingsBtn.addActionListener(this);
        settingsBtn.setBackground(Color.WHITE);

        JButton aboutBtn = new JButton("Om");
        aboutBtn.addActionListener(this);
        aboutBtn.setBackground(Color.WHITE);

        topPanel.add(settingsBtn);
        topPanel.add(aboutBtn);
    }

    //Sets up the main panel
    private void setUpMainPanel(JPanel mainPanel) {
        //Vertical gap: 50 and 70

        //Section 1
        JLabel archiveLabel = new JLabel("Last inn arkivuttrekk:");
        archiveLabel.setBounds(100, 50, 200, 30);
        archiveLabel.setFont(primaryFont);

        JButton uploadTarBtn = new JButton("Last inn pakket uttrekk");
        uploadTarBtn.setBounds(100, 100, 200, 30);
        uploadTarBtn.addActionListener(this);
        uploadTarBtn.setBackground(primaryColor);
        uploadTarBtn.setForeground(Color.WHITE);

        JButton uploadTestedFolderBtn = new JButton("Last inn ferdig testet uttrekk");
        uploadTestedFolderBtn.setBounds(350, 100, 200, 30);
        uploadTestedFolderBtn.addActionListener(this);
        uploadTestedFolderBtn.setBackground(primaryColor);
        uploadTestedFolderBtn.setForeground(Color.WHITE);

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

        startTestingBtn = new JButton("Start testing");
        startTestingBtn.setBounds(275, 220, 125, 30);
        startTestingBtn.addActionListener(this);
        startTestingBtn.setBackground(primaryColor);
        startTestingBtn.setForeground(Color.WHITE);
        startTestingBtn.setEnabled(false);

        //Section 3
        JLabel doneTestedLabel = new JLabel("Ferdig testet arkivuttrekk:");
        doneTestedLabel.setFont(primaryFont);
        doneTestedLabel.setBounds(100, 290, 300, 30);

        writeReportBtn = new JButton("Skriv rapport");
        writeReportBtn.setBounds(100, 340, 125, 30);
        writeReportBtn.addActionListener(this);
        writeReportBtn.setBackground(primaryColor);
        writeReportBtn.setForeground(Color.WHITE);
        writeReportBtn.setEnabled(false);

        //Adding components
        mainPanel.add(archiveLabel);
        mainPanel.add(uploadTarBtn);
        mainPanel.add(uploadTestedFolderBtn);

        mainPanel.add(testsLabel);
        mainPanel.add(chooseTestsBtn);
        mainPanel.add(startTestingBtn);

        mainPanel.add(doneTestedLabel);
        mainPanel.add(writeReportBtn);
    }

    //Sets up the info panel
    private void setUpInfoPanel(JPanel infoPanel) {
        int rows = 8;

        JLabel infoLabel = new JLabel("Informasjon om uttrekket:");
        infoLabel.setFont(primaryFont);

        editInfoBtn = new JButton("Rediger informasjon");
        editInfoBtn.addActionListener(this);
        editInfoBtn.setBackground(primaryColor);
        editInfoBtn.setForeground(Color.WHITE);
        editInfoBtn.setEnabled(false);

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
        JLabel name7 = new JLabel("Test utført av:");
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

    //Returns container
    public Container getContainer() {
        return container;
    }

    //Removes edit admin info button
    public void removeEditInfoBtn(){
        infoPanel.remove(editInfoBtn);
        container.revalidate();
        container.repaint();
    }

    //Activates buttons when archive as been uploaded
    public void activateButtons() {
        chooseTestsBtn.setEnabled(true);
        startTestingBtn.setEnabled(true);
        editInfoBtn.setEnabled(true);
        //writeReportBtn.setEnabled(!writeReportBtn.isEnabled()); #NOSONAR
    }

    //Deactivates buttons when view is reset
    private void deactivateButtons() {
        chooseTestsBtn.setEnabled(false);
        startTestingBtn.setEnabled(false);
        editInfoBtn.setEnabled(false);
        writeReportBtn.setEnabled(false);
    }

    //Resets administrative information fields
    public void resetManualInfo() {
        for (JTextArea value: valueList) {
           value.setText("");
        }
    }

    //Resets mainview
    public void resetMainView() {
        resetManualInfo();
        deactivateButtons();
    }

    //Updates administrative information fields
    public void updateAdminInfo(List<String> list) {
        for (int i = 0; i<list.size(); i++) {
            valueList.get(i).setText(list.get(i));
        }
    }
}
