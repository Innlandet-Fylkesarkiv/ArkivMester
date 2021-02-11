package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminInfoView extends Views {
    Container container;

    AdminInfoView() {
        //Empty constructor
    }

    //Sets up GUI
    public void createAndShowGUI(Container cnt) {
        container = cnt;

        //Clears container
        container.remove(0); //Main panel
        container.remove(0); //Info panel
        container.revalidate();

        //Info container
        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.PAGE_AXIS));

        //Grid
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBorder(new EmptyBorder(0, 0, 300, 600));
        gridPanel.setBackground(Color.WHITE);
        setUpGridPanel(gridPanel);

        //Adding components
        infoContainer.add(gridPanel);
        container.add(infoContainer);
    }

    //Sets up the grid panel
    private void setUpGridPanel(JPanel gridPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        //Row 1
        JLabel data1 = new JLabel("Uttrekk mottatt dato:");
        JLabel data2 = new JLabel("Test utført av:");
        JLabel data3 = new JLabel("Dato for rapport:");

        //Row 2
        JTextField receivedDateTxt = new JTextField(15);
        JTextField testByTxt = new JTextField(15);
        JTextField rapportDateTxt = new JTextField(15);

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
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10,0,0,10);

        gridPanel.add(data1, gbc);
        gbc.gridy++;
        gridPanel.add(data2, gbc);
        gbc.gridy++;
        gridPanel.add(data3, gbc);


        gbc.gridx++;
        gbc.gridy = 0;

        gridPanel.add(receivedDateTxt, gbc);
        gbc.gridy++;
        gridPanel.add(testByTxt, gbc);
        gbc.gridy++;
        gridPanel.add(rapportDateTxt, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        gridPanel.add(saveInfoBtn, gbc);
        gbc.gridy++;
        gridPanel.add(cancelInfoBtn, gbc);
    }

    //Clears the whole frame
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }
}
