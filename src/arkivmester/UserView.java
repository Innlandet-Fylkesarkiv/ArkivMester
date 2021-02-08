package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UserView extends JFrame { //#NOSONAR
    int windowWidth = 1200;
    int windowHeight = 700;
    Color primaryColor = new Color(8, 83, 148);

    UserView() {
        //Empty constructor
    }

    //Sets up GUI
    public void createAndShowGUI() {
        //Frame properties
        JFrame f = new JFrame("ArkivMester");
        f.setSize(windowWidth, windowHeight);
        f.setVisible(true);
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);

        //Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(primaryColor);

        //Top panel buttons
        JButton settingsBtn = new JButton("Innstillinger");
        JButton aboutBtn = new JButton("Om");
        settingsBtn.setBackground(Color.WHITE);
        aboutBtn.setBackground(Color.WHITE);


        //Container
        Container container = this.getContentPane();
        container.setLayout(new BorderLayout(15, 10));
        container.setBackground(Color.WHITE);

        //Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);
        setUpMainPanel(mainPanel);

        //Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.RED);
        JLabel archiveLabel1 = new JLabel("Last opp arkivuttrekk1");
        infoPanel.add(archiveLabel1);
        infoPanel.setBorder(new EmptyBorder(100, 100, 100, 100));

        //Adding components together
        topPanel.add(settingsBtn);
        topPanel.add(aboutBtn);

        container.add(mainPanel);
        container.add(infoPanel, BorderLayout.EAST);

        container.add(topPanel, BorderLayout.NORTH);
        f.add(container);
    }

    //Sets up the main panel
    public void setUpMainPanel(JPanel mainPanel) {
        //Vertical gap: 50 and 70

        JLabel archiveLabel = new JLabel("Last inn arkivuttrekk:");
        archiveLabel.setBounds(100, 50, 200, 30);
        archiveLabel.setFont(new Font("Sans Serif", Font.PLAIN, 20));

        JButton uploadTarBtn = new JButton("Last inn pakket uttrekk");
        uploadTarBtn.setBounds(100, 100, 200, 30);
        uploadTarBtn.setBackground(primaryColor);
        uploadTarBtn.setForeground(Color.WHITE);

        JButton uploadTestedFolderBtn = new JButton("Last inn ferdig testet uttrekk");
        uploadTestedFolderBtn.setBounds(350, 100, 200, 30);
        uploadTestedFolderBtn.setBackground(primaryColor);
        uploadTestedFolderBtn.setForeground(Color.WHITE);

        JLabel testsLabel = new JLabel("Test arkivuttrekk:");
        testsLabel.setFont(new Font("Sans Serif", Font.PLAIN, 20));
        testsLabel.setBounds(100, 170, 300, 30);

        JButton chooseTestsBtn = new JButton("Velg tester");
        chooseTestsBtn.setBounds(100, 220, 125, 30);
        chooseTestsBtn.setBackground(primaryColor);
        chooseTestsBtn.setForeground(Color.WHITE);

        JButton startTestingBtn = new JButton("Start testing");
        startTestingBtn.setBounds(275, 220, 125, 30);
        startTestingBtn.setBackground(primaryColor);
        startTestingBtn.setForeground(Color.WHITE);

        JLabel doneTestedLabel = new JLabel("Ferdig testet arkivuttrekk:");
        doneTestedLabel.setFont(new Font("Sans Serif", Font.PLAIN, 20));
        doneTestedLabel.setBounds(100, 290, 300, 30);

        JButton writeReportBtn = new JButton("Skriv rapport");
        writeReportBtn.setBounds(100, 340, 125, 30);
        writeReportBtn.setBackground(primaryColor);
        writeReportBtn.setForeground(Color.WHITE);

        //Adding components
        mainPanel.add(archiveLabel);
        //mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(uploadTarBtn);
        mainPanel.add(uploadTestedFolderBtn);

        mainPanel.add(testsLabel);
        mainPanel.add(chooseTestsBtn);
        mainPanel.add(startTestingBtn);

        mainPanel.add(doneTestedLabel);
        mainPanel.add(writeReportBtn);
    }
}
