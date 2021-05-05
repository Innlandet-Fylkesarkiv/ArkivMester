package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the test settings view.
 *
 * Allows for choosing which subtests that will be used in the main test.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class TestSettingsView extends Views {
    Container container;
    private final List<Boolean> selectedTests;
    private final List<Boolean> selectedXqueries;
    private final List<JCheckBox> testBoxes = new ArrayList<>();
    private final List<JCheckBox> xqueryBoxes = new ArrayList<>();
    private final int amountOfTests;

    /**
     * Constructor - Initiates this.selectedTests with current data.
     * @param selectedTests The current selected tests stored in {@link ThirdPartiesModel}.
     * @param selectedXqueries The current selected XQueries stored in {@link ThirdPartiesModel}.
     */
    TestSettingsView(List<Boolean> selectedTests, List<Boolean> selectedXqueries) {
        this.selectedTests = selectedTests;
        amountOfTests = selectedTests.size();
        this.selectedXqueries = selectedXqueries;
    }

    /**
     * Creates and shows the GUI
     */
    public void createAndShowGUI(Container cnt, String[] customXqueryList) throws IndexOutOfBoundsException {
        container = cnt;

        //Clears container
        container.remove(0); //Main panel
        container.remove(0); //Info panel
        container.revalidate();

        //Tests panel
        JPanel testsPanel = new JPanel();
        testsPanel.setLayout(new BoxLayout(testsPanel, BoxLayout.PAGE_AXIS));
        testsPanel.setBorder(new EmptyBorder(100, 100, 0, 0));
        testsPanel.setBackground(Color.WHITE);
        setUpTestsPanel(testsPanel);

        //XQuery panel
        JPanel xqueryPanel = new JPanel(new GridBagLayout());
        xqueryPanel.setBorder(new EmptyBorder(0, 0, 215, 0));
        xqueryPanel.setBackground(Color.WHITE);
        setUpXqueryPanel(xqueryPanel, customXqueryList);
        JScrollPane sp = new JScrollPane(xqueryPanel);
        sp.setBorder(BorderFactory.createEmptyBorder());

        //Adding components
        container.add(testsPanel, BorderLayout.WEST);
        container.add(sp);
        container.repaint();
    }

    /**
     * Sets up the tests panel.
     */
    private void setUpTestsPanel(JPanel testsPanel) {
        //Title
        JLabel testsTitle = new JLabel("Tester som skal kjøres:");
        testsTitle.setFont(primaryFont);

        //Checkboxes
        for(int i = 0; i<amountOfTests; i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setBackground(Color.WHITE);
            testBoxes.add(checkBox);
        }
        testBoxes.get(0).setText("Arkade5");
        testBoxes.get(1).setText("DROID");
        testBoxes.get(2).setText("Kost-Val");
        testBoxes.get(3).setText("VeraPdf");

        //Buttons
        JButton saveTestsBtn = new JButton("Lagre tester");
        saveTestsBtn.addActionListener(this);
        saveTestsBtn.setBackground(primaryColor);
        saveTestsBtn.setForeground(Color.WHITE);
        saveTestsBtn.setToolTipText("Lagrer forandringer og går tilbake til forsiden.");

        JButton cancelTestSettings = new JButton("Avbryt");
        cancelTestSettings.addActionListener(this);
        cancelTestSettings.setBackground(primaryColor);
        cancelTestSettings.setForeground(Color.WHITE);
        cancelTestSettings.setToolTipText("Avbryter forandringer og går tilbake til forsiden.");

        //Adding components together
        testsPanel.add(testsTitle);

        for(int i = 0; i<amountOfTests; i++) {
            testsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            testBoxes.get(i).setSelected(selectedTests.get(i));
            testsPanel.add(testBoxes.get(i));
        }

        testsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        testsPanel.add(saveTestsBtn);
        testsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        testsPanel.add(cancelTestSettings);
    }

    /**
     * Sets up the XQuery panel.
     * @param xqueryPanel The panel to be set up.
     * @param customXqueryList List of XQueries
     * @throws IndexOutOfBoundsException if there inconsistencies for list lenghts.
     */
    private void setUpXqueryPanel(JPanel xqueryPanel, String[] customXqueryList) throws IndexOutOfBoundsException {
        int rows = customXqueryList.length;

        //Title
        JLabel xqueryTitle = new JLabel("Egendefinerte XQueries:");
        xqueryTitle.setFont(primaryFont);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        //Col 1 Checkboxes
        for(String s : customXqueryList) {
            JCheckBox box = new JCheckBox();
            box.setText(s);
            box.setBackground(Color.WHITE);
            xqueryBoxes.add(box);
        }

        //Adding components together
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10,0,0,10);

        gbc.gridwidth = 2;
        xqueryPanel.add(xqueryTitle, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;

        //Col 1
        for(int i = 0; i<rows; i++) {
            xqueryBoxes.get(i).setSelected(selectedXqueries.get(i));
            xqueryPanel.add(xqueryBoxes.get(i), gbc);
            gbc.gridy++;
        }

        //Col 2
        gbc.gridy = 1;
        gbc.gridx++;

    }

    /**
     * Clears the entire container.
     */
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }

    /**
     * Regular getter for newly chosen tests boxes.
     * @return Updated selected tests as Boolean list.
     */
    public List<Boolean> getSelectedTests() {
        List<Boolean> currentList = new ArrayList<>();

        for (JCheckBox testBox : testBoxes) {
            currentList.add(testBox.isSelected());
        }
        return currentList;
    }

    /**
     * Regular getter for newly chosen XQuery boxes.
     * @return Updated selected XQueries as Boolean list.
     */
    public List<Boolean> getSelectedXqueries() {
        List<Boolean> currentList = new ArrayList<>();

        for (JCheckBox xqueryBox : xqueryBoxes) {
            currentList.add(xqueryBox.isSelected());
        }
        return currentList;
    }
}
