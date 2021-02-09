package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

//View class for the test window
public class TestView extends Views{

    TestView() {
        //Empty constructor
    }

    //Sets up GUI
    public void createAndShowGUI(Container container) {
        //Clears container
        container.remove(1);
        container.remove(0);
        container.revalidate();

        //Test container
        JPanel testContainer = new JPanel();
        testContainer.setLayout(new BoxLayout(testContainer, BoxLayout.PAGE_AXIS));

        //Test panel
        JPanel testPanel = new JPanel();
        testPanel.setLayout(null);
        testPanel.setPreferredSize(new Dimension(0,400));
        testPanel.setBackground(Color.WHITE);
        setUpTestPanel(testPanel);

        //Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        buttonPanel.setBorder(new EmptyBorder(0, 60, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        setUpButtonPanel(buttonPanel);

        //Adding components together
        testContainer.add(testPanel);
        testContainer.add(buttonPanel);
        container.add(testContainer);

    }

    //Sets up the test panel
    private void setUpTestPanel(JPanel testPanel) {
        //Vertical gap: 20, 40 and 70

        JButton backBtn = new JButton("Tilbake");
        backBtn.setBounds(20, 20, 75, 30);
        backBtn.addActionListener(this);
        backBtn.setBackground(primaryColor);
        backBtn.setForeground(Color.WHITE);

        //Section 1
        JLabel arkadeTitle = new JLabel("Arkade5");
        arkadeTitle.setBounds(100, 90, 200, 30);
        arkadeTitle.setFont(primaryFont);

        JLabel arkadeStatus = new JLabel("Ferdig.");
        arkadeStatus.setBounds(100, 130, 200, 30);

        JLabel arkadeErrors = new JLabel("1000 avvik");
        arkadeErrors.setBounds(100, 150, 200, 30);

        //Section 2
        JLabel veraTitle = new JLabel("VeraPdf");
        veraTitle.setBounds(100, 220, 200, 30);
        veraTitle.setFont(primaryFont);

        JLabel veraStatus = new JLabel("Ferdig.");
        veraStatus.setBounds(100, 260, 200, 30);

        JLabel veraErrors = new JLabel("123 avvik");
        veraErrors.setBounds(100, 280, 200, 30);

        //Section 2
        JLabel kostvalTitle = new JLabel("Kost-Val");
        kostvalTitle.setBounds(100, 350, 200, 30);
        kostvalTitle.setFont(primaryFont);

        JLabel kostvalStatus = new JLabel("Tester...");
        kostvalStatus.setBounds(100, 390, 200, 30);

        JLabel kostvalErrors = new JLabel("");
        kostvalErrors.setBounds(100, 410, 200, 30);

        //Adding components
        testPanel.add(backBtn);

        testPanel.add(arkadeTitle);
        testPanel.add(arkadeStatus);
        testPanel.add(arkadeErrors);

        testPanel.add(veraTitle);
        testPanel.add(veraStatus);
        testPanel.add(veraErrors);

        testPanel.add(kostvalTitle);
        testPanel.add(kostvalStatus);
        testPanel.add(kostvalErrors);
    }

    //Sets up the button panel
    private void setUpButtonPanel(JPanel buttonPanel) {
        JLabel testStatus = new JLabel("Kjører tester...");
        testStatus.setBounds(100, 90, 400, 30);
        testStatus.setFont(primaryFont);

        String[] fileFormats = {".odf",".docx"};
        JComboBox<String> fileFormatCb = new JComboBox<>(fileFormats);
        fileFormatCb.setBackground(primaryColor);
        fileFormatCb.setForeground(Color.WHITE);
        fileFormatCb.setPreferredSize(new Dimension(100, 20));

        JButton createRapportBtn = new JButton("Lag rapport");
        createRapportBtn.addActionListener(this);
        createRapportBtn.setBackground(primaryColor);
        createRapportBtn.setForeground(Color.WHITE);

        JButton packToAipBtn = new JButton("Pakk til AIP");
        packToAipBtn.addActionListener(this);
        packToAipBtn.setBackground(primaryColor);
        packToAipBtn.setForeground(Color.WHITE);

        JButton testNewBtn = new JButton("Test nytt uttrekk");
        testNewBtn.addActionListener(this);
        testNewBtn.setBackground(primaryColor);
        testNewBtn.setForeground(Color.WHITE);

        buttonPanel.add(testStatus);
        buttonPanel.add(fileFormatCb);
        buttonPanel.add(createRapportBtn);
        buttonPanel.add(packToAipBtn);
        buttonPanel.add(testNewBtn);
    }
}
