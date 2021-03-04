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
    private final List<JCheckBox> testBoxes = new ArrayList<>(); //Final for now
    private final int amountOfTests;

    /**
     * Constructor - Initiates this.selectedTests with current data.
     * @param selectedTests The current selected tests stored in {@link ThirdPartiesModel}.
     */
    TestSettingsView(List<Boolean> selectedTests) {
        this.selectedTests = selectedTests;
        amountOfTests = selectedTests.size();
    }

    /**
     * Creates and shows the GUI
     */
    public void createAndShowGUI(Container cnt) {
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
        JPanel xqueryPanel = new JPanel();
        xqueryPanel.setBackground(Color.WHITE);


        //Adding components
        container.add(testsPanel);
        container.add(xqueryPanel, BorderLayout.EAST);
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
        JButton saveInfoBtn = new JButton("Lagre tests");
        saveInfoBtn.addActionListener(this);
        saveInfoBtn.setBackground(primaryColor);
        saveInfoBtn.setForeground(Color.WHITE);

        JButton cancelInfoBtn = new JButton("Avbryt");
        cancelInfoBtn.addActionListener(this);
        cancelInfoBtn.setBackground(primaryColor);
        cancelInfoBtn.setForeground(Color.WHITE);

        //Adding components together
        testsPanel.add(testsTitle);

        for(int i = 0; i<amountOfTests; i++) {
            testsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            testBoxes.get(i).setSelected(selectedTests.get(i));
            testsPanel.add(testBoxes.get(i));
        }

        testsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        testsPanel.add(saveInfoBtn);
        testsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        testsPanel.add(cancelInfoBtn);
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
        for(int i = 0; i<amountOfTests; i++) {
            selectedTests.set(i, testBoxes.get(i).isSelected());
        }
        return selectedTests;
    }
}
