package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Handles the test view.
 *
 * Allows for observing subtests' statuses when the main test is running.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class TestView extends Views{
    Container container;
    JComboBox<String> fileFormatCb;

    JLabel testStatus;
    JLabel arkadeStatus;
    JLabel veraStatus;
    JLabel kostvalStatus;
    JLabel droidStatus;
    JLabel xqueryStatus;

    static final String WAITING = "Venter.";
    static final String RUNNING = "Kjører...";
    static final String DONE = "Ferdig.";
    static final String NONE = "Ingen.";
    static final String TESTRUNNING = "Kjører tester...";
    static final String TESTDONE = "Alle tester er ferdige.";

    /**
     * Creates and shows the GUI
     */
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
        testStatus = new JLabel(TESTRUNNING);
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

    /**
     * Sets up the test panel.
     */
    private void setUpTestPanel(JPanel testPanel) {
        //Vertical gap: 20, 40 and 70

        //Section 1
        JLabel arkadeTitle = new JLabel("Arkade5");
        arkadeTitle.setBounds(100, 50, 200, 30);
        arkadeTitle.setFont(primaryFont);

        arkadeStatus = new JLabel(WAITING);
        arkadeStatus.setBounds(100, 90, 200, 30);

        JLabel arkadeErrors = new JLabel("");
        arkadeErrors.setBounds(100, 110, 200, 30);

        //Section 2
        JLabel veraTitle = new JLabel("VeraPdf");
        veraTitle.setBounds(100, 180, 200, 30);
        veraTitle.setFont(primaryFont);

        veraStatus = new JLabel(WAITING);
        veraStatus.setBounds(100, 220, 200, 30);

        JLabel veraErrors = new JLabel("");
        veraErrors.setBounds(100, 240, 200, 30);

        //Section 3
        JLabel kostvalTitle = new JLabel("Kost-Val");
        kostvalTitle.setBounds(100, 310, 200, 30);
        kostvalTitle.setFont(primaryFont);

        kostvalStatus = new JLabel(WAITING);
        kostvalStatus.setBounds(100, 350, 200, 30);

        JLabel kostvalErrors = new JLabel("");
        kostvalErrors.setBounds(100, 370, 200, 30);

        //Section 4
        JLabel droidTitle = new JLabel("DROID");
        droidTitle.setBounds(300, 50, 200, 30);
        droidTitle.setFont(primaryFont);

        droidStatus = new JLabel(WAITING);
        droidStatus.setBounds(300, 90, 200, 30);

        JLabel droidErrors = new JLabel("");
        droidErrors.setBounds(300, 110, 200, 30);

        //Section 5
        JLabel xqueryTitle = new JLabel("XQuery tester");
        xqueryTitle.setBounds(300, 180, 200, 30);
        xqueryTitle.setFont(primaryFont);

        xqueryStatus = new JLabel(NONE);
        xqueryStatus.setBounds(300, 220, 200, 30);

        JLabel xqueryErrors = new JLabel("");
        xqueryErrors.setBounds(300, 240, 200, 30);

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

        testPanel.add(droidTitle);
        testPanel.add(droidStatus);
        testPanel.add(droidErrors);

        testPanel.add(xqueryTitle);
        testPanel.add(xqueryStatus);
        testPanel.add(xqueryErrors);
    }

    /**
     * Sets up the button panel.
     */
    private void setUpButtonPanel(JPanel buttonPanel) {
        String[] fileFormats = {".docx",".odf"};
        fileFormatCb = new JComboBox<>(fileFormats);
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

    /**
     * Clears the entire container.
     */
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }

    /**
     * Regular getter for selected format.
     * @return String containing the file format for which the main report will be exported as.
     */
    public String getSelectedFormat() {
        return (String)fileFormatCb.getSelectedItem();
    }

    /**
     * Updates the status of all tests in the view.
     * @param status A static final String variable from {@link arkivmester.TestView}.
     */
    public void updateTestStatus(String status) {
        testStatus.setText(status);
    }

    /**
     * Updates the status of Arkade tests in the view.
     * @param status A static final String variable from {@link arkivmester.TestView}.
     */
    public void updateArkadeStatus(String status) {
        arkadeStatus.setText(status);
    }

    /**
     * Updates the status of VeraPDF tests in the view.
     * @param status A static final String variable from {@link arkivmester.TestView}.
     */
    public void updateVeraStatus(String status) {
        veraStatus.setText(status);
    }

    /**
     * Updates the status of Kost-Val tests in the view.
     * @param status A static final String variable from {@link arkivmester.TestView}.
     */
    public void updateKostValStatus(String status) {
        kostvalStatus.setText(status);
    }

    /**
     * Updates the status of DROID tests in the view.
     * @param status A static final String variable from {@link arkivmester.TestView}.
     */
    public void updateDroidStatus(String status) {
        droidStatus.setText(status);
    }
}
