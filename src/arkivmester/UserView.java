package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UserView extends JFrame { //#NOSONAR
    int windowWidth = 1200;
    int windowHeight = 700;

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
        topPanel.setBackground(Color.CYAN);

        //Top panel buttons
        JButton settingsBtn = new JButton("Innstillinger");
        JButton aboutBtn = new JButton("Om");
        settingsBtn.setBackground(Color.BLUE);
        settingsBtn.setForeground(Color.WHITE);
        aboutBtn.setBackground(Color.BLUE);
        aboutBtn.setForeground(Color.WHITE);


        //Container
        Container container = this.getContentPane();
        container.setLayout(new BorderLayout(15, 10));
        container.setBackground(Color.MAGENTA);

        //Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.YELLOW);
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

        JLabel archiveLabel = new JLabel("Last opp arkivuttrekk");
        archiveLabel.setBounds(100, 50, 200, 30);
        archiveLabel.setFont(new Font("Sans Serif", Font.PLAIN, 20));

        JButton uploadTarBtn = new JButton("Last opp pakket uttrekk");
        uploadTarBtn.setBounds(100, 100, 200, 30);

        JButton uploadTestedFolderBtn = new JButton("Last opp ferdig testet uttrekk");
        uploadTestedFolderBtn.setBounds(400, 100, 200, 30);


        JLabel testsLabel = new JLabel("Arkivutrekk som skal testes:");
        testsLabel.setFont(new Font("Sans Serif", Font.PLAIN, 20));
        testsLabel.setBounds(100, 170, 300, 30);

        JLabel tarNameLabel = new JLabel("pakket uttrekk navn");
        tarNameLabel.setFont(new Font("Sans Serif", Font.PLAIN, 20));
        tarNameLabel.setBounds(100, 170, 300, 30);

        JButton chooseTestsBtn = new JButton("Velg tester");
        JButton startTestingBtn = new JButton("Start testing");

        JLabel doneTestedLabel = new JLabel("Ferdig testet arkivuttrekk");
        doneTestedLabel.setFont(new Font("Sans Serif", Font.PLAIN, 20));

        JLabel testedFolderNameLabel = new JLabel("ferdig testet uttrekk navn");
        JButton writeReportBtn = new JButton("Skriv rapport");

        //Adding components
        mainPanel.add(archiveLabel);
        //mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(uploadTarBtn);
        mainPanel.add(uploadTestedFolderBtn);

        mainPanel.add(testsLabel);
        mainPanel.add(tarNameLabel);
        mainPanel.add(chooseTestsBtn);
        mainPanel.add(startTestingBtn);

        mainPanel.add(doneTestedLabel);
        mainPanel.add(testedFolderNameLabel);
        mainPanel.add(writeReportBtn);
    }
}
