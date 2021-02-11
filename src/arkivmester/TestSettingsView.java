package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TestSettingsView extends Views {
    Container container;

    TestSettingsView() {
        //Empty constructor
    }

    //Sets up GUI
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

    //Sets up the grid panel
    private void setUpTestsPanel(JPanel testsPanel) {

        //Title
        JLabel testsTitle = new JLabel("Tester som skal kjøres");
        testsTitle.setFont(primaryFont);

        //Checkboxes
        JCheckBox arkadeCheck = new JCheckBox("Arkade5");
        arkadeCheck.setBackground(Color.WHITE);
        JCheckBox droidCheck = new JCheckBox("DROID");
        droidCheck.setBackground(Color.WHITE);
        JCheckBox kostvalCheck = new JCheckBox("Kost-Val");
        kostvalCheck.setBackground(Color.WHITE);
        JCheckBox verapdfCheck = new JCheckBox("VeraPdf");
        verapdfCheck.setBackground(Color.WHITE);

        //Buttons
        JButton saveInfoBtn = new JButton("Lagre");
        saveInfoBtn.addActionListener(this);
        saveInfoBtn.setBackground(primaryColor);
        saveInfoBtn.setForeground(Color.WHITE);

        JButton cancelInfoBtn = new JButton("Avbryt");
        cancelInfoBtn.addActionListener(this);
        cancelInfoBtn.setBackground(primaryColor);
        cancelInfoBtn.setForeground(Color.WHITE);

        //Adding components together
        testsPanel.add(testsTitle);
        testsPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        testsPanel.add(arkadeCheck);
        testsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        testsPanel.add(droidCheck);
        testsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        testsPanel.add(kostvalCheck);
        testsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        testsPanel.add(verapdfCheck);
        testsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        testsPanel.add(saveInfoBtn);
        testsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        testsPanel.add(cancelInfoBtn);
    }

    //Clears the whole frame
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }
}
