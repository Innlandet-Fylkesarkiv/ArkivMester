package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

//View class for the test window
public class TestView extends Views{
    Container container;

    TestView() {
        //Empty constructor
    }

    //Sets up GUI
    public void createAndShowGUI(Container cnt) {
        container = cnt;

        //Clears container
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

        //Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        statusPanel.setBorder(new EmptyBorder(20, 60, 0, 0));
        statusPanel.setBackground(Color.WHITE);
        JLabel testStatus = new JLabel("Kjører tester...");
        testStatus.setFont(primaryFont);

        //Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        buttonPanel.setBorder(new EmptyBorder(0, 60, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        setUpButtonPanel(buttonPanel);

        //Adding components together
        statusPanel.add(testStatus);
        testContainer.add(testPanel);
        testContainer.add(statusPanel);
        testContainer.add(buttonPanel);
        container.add(testContainer);

    }

    //Sets up the test panel
    private void setUpTestPanel(JPanel testPanel) {
        //Vertical gap: 20, 40 and 70

        //Section 1
        JLabel arkadeTitle = new JLabel("Arkade5");
        arkadeTitle.setBounds(100, 50, 200, 30);
        arkadeTitle.setFont(primaryFont);

        JLabel arkadeStatus = new JLabel("Ferdig.");
        arkadeStatus.setBounds(100, 90, 200, 30);

        JLabel arkadeErrors = new JLabel("1000 avvik");
        arkadeErrors.setBounds(100, 110, 200, 30);

        //Section 2
        JLabel veraTitle = new JLabel("VeraPdf");
        veraTitle.setBounds(100, 180, 200, 30);
        veraTitle.setFont(primaryFont);

        JLabel veraStatus = new JLabel("Ferdig.");
        veraStatus.setBounds(100, 220, 200, 30);

        JLabel veraErrors = new JLabel("123 avvik");
        veraErrors.setBounds(100, 240, 200, 30);

        //Section 3
        JLabel kostvalTitle = new JLabel("Kost-Val");
        kostvalTitle.setBounds(100, 310, 200, 30);
        kostvalTitle.setFont(primaryFont);

        JLabel kostvalStatus = new JLabel("Tester...");
        kostvalStatus.setBounds(100, 350, 200, 30);

        JLabel kostvalErrors = new JLabel("");
        kostvalErrors.setBounds(100, 370, 200, 30);

        //Section 4
        JLabel xqueryTitle = new JLabel("XQuery tester");
        xqueryTitle.setBounds(300, 50, 200, 30);
        xqueryTitle.setFont(primaryFont);

        JLabel xqueryStatus = new JLabel("Ingen.");
        xqueryStatus.setBounds(300, 90, 200, 30);

        JLabel xqueryErrors = new JLabel("");
        xqueryErrors.setBounds(300, 110, 200, 30);

        //Adding components
        testPanel.add(arkadeTitle);
        testPanel.add(arkadeStatus);
        testPanel.add(arkadeErrors);

        testPanel.add(veraTitle);
        testPanel.add(veraStatus);
        testPanel.add(veraErrors);

        testPanel.add(kostvalTitle);
        testPanel.add(kostvalStatus);
        testPanel.add(kostvalErrors);

        testPanel.add(xqueryTitle);
        testPanel.add(xqueryStatus);
        testPanel.add(xqueryErrors);
    }

    //Sets up the button panel
    private void setUpButtonPanel(JPanel buttonPanel) {


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

        //Adding components
        buttonPanel.add(fileFormatCb);
        buttonPanel.add(createRapportBtn);
        buttonPanel.add(packToAipBtn);
        buttonPanel.add(testNewBtn);
    }

    public void clearContainer(){
        container.removeAll();
        container.revalidate(); //Needed?
        container.repaint(); //Needed?
    }
}
