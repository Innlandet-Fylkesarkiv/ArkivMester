package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//View class for the admin information window
public class AdminInfoView extends Views {
    Container container;

    List<JTextField> valueList = new ArrayList<>();

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
        int rows = 8;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        //Col 1
        JLabel data1 = new JLabel("UttrekksID:");
        JLabel data2 = new JLabel("Kommune/Kunde:");
        JLabel data3 = new JLabel("Kontaktperson:");
        JLabel data4 = new JLabel("Uttrekksformat:");
        JLabel data5 = new JLabel("Produksjonsdato for uttrekket:");
        JLabel data6 = new JLabel("Uttrekk mottatt dato:");
        JLabel data7 = new JLabel("Test utført av:");
        JLabel data8 = new JLabel("Dato for rapport:");

        //Col 2
        for(int i = 0; i<8; i++) {
            valueList.add(new JTextField(15));
        }

        //Buttons
        JButton saveInfoBtn = new JButton("Lagre administrativ data");
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

        //Col 1
        gridPanel.add(data1, gbc);
        gbc.gridy++;
        gridPanel.add(data2, gbc);
        gbc.gridy++;
        gridPanel.add(data3, gbc);
        gbc.gridy++;
        gridPanel.add(data4, gbc);
        gbc.gridy++;
        gridPanel.add(data5, gbc);
        gbc.gridy++;
        gridPanel.add(data6, gbc);
        gbc.gridy++;
        gridPanel.add(data7, gbc);
        gbc.gridy++;
        gridPanel.add(data8, gbc);
        gbc.gridx++;

        //Col 2
        gbc.gridy = 0;

        for(int i = 0; i<rows; i++) {
            gridPanel.add(valueList.get(i), gbc);
            gbc.gridy++;
        }

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        gridPanel.add(saveInfoBtn, gbc);
        gbc.gridy++;
        gridPanel.add(cancelInfoBtn, gbc);
    }

    //Clears the whole frame
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }

    //Populates field text
    public void populateAdminInfo(List<String> list) {
        for (int i = 0; i<list.size(); i++) {
            valueList.get(i).setText(list.get(i));
        }
    }

    //Retrieves field text
    public List<String> getManualInfo() {
        List<String> list = new ArrayList<>();

        for (JTextField field : valueList) {
            list.add(field.getText());
        }

        return list;
    }
}
