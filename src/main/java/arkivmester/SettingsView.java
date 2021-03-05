package arkivmester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Handles the settings view.
 *
 * Allows for changing the configuration file via the UI.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class SettingsView extends Views {
    private Container container;
    JButton saveSettingsBtn;
    List<JLabel> keys = new ArrayList<>();
    List<JLabel> values = new ArrayList<>();
    List<JButton> buttons = new ArrayList<>();
    List<String> newPropKV = new ArrayList<>();

    /**
     * Creates and shows the GUI
     */
    public void createAndShowGUI(Container cnt, Properties prop) {
        container = cnt;

        //Clears container
        if(container.getComponentCount()==4)
            container.remove(0); //Second info panel
        container.remove(0); //Main panel
        container.remove(0); //Info panel
        container.revalidate();

        //Container panel
        JPanel cfgPanel = new JPanel();
        cfgPanel.setLayout(new BoxLayout(cfgPanel, BoxLayout.PAGE_AXIS));
        cfgPanel.setBackground(Color.WHITE);


        setUpCfgPanel(cfgPanel, prop);


        //Adding components
        container.add(cfgPanel);
    }

    /**
     * Sets up the config panel.
     */
    private void setUpCfgPanel(JPanel cfgPanel, Properties prop) {
        //Grid
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBorder(new EmptyBorder(0, 0, 300, 600));
        gridPanel.setBackground(Color.WHITE);

        JLabel configTitle = new JLabel("Alle program lokasjoner:");
        configTitle.setFont(primaryFont);

        int rows = prop.size();

        //Col1, Col2
        for(Map.Entry<Object, Object> entry : prop.entrySet()) {
            keys.add(new JLabel((String)entry.getKey()));
            values.add(new JLabel((String)entry.getValue()));
        }

        //Col3
        for(int i = 0; i<rows; i++) {
            JButton tempBtn = new JButton("Endre fil lokasjon");
            tempBtn.setActionCommand(String.valueOf(i));
            tempBtn.addActionListener(e-> updatePath(e.getActionCommand()));

            tempBtn.setBackground(primaryColor);
            tempBtn.setForeground(Color.WHITE);

            buttons.add(tempBtn);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        //Buttons
        saveSettingsBtn = new JButton("Lagre innstillinger");
        saveSettingsBtn.addActionListener(this);
        saveSettingsBtn.setEnabled(false);
        saveSettingsBtn.setBackground(primaryColor);
        saveSettingsBtn.setForeground(Color.WHITE);

        JButton cancelCfg = new JButton("Tilbake");
        cancelCfg.setActionCommand("Avbryt");
        cancelCfg.addActionListener(this);
        cancelCfg.setBackground(primaryColor);
        cancelCfg.setForeground(Color.WHITE);

        //Adding components together
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10,0,0,10);

        gbc.gridwidth = 2;
        gridPanel.add(configTitle, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;

        //Col 1
        for(int i = 0; i<rows; i++) {
            gridPanel.add(keys.get(i), gbc);
            gbc.gridy++;
        }

        //Col 2
        gbc.gridx++;
        gbc.gridy = 1;

        for(int i = 0; i<rows; i++) {
            gridPanel.add(values.get(i), gbc);
            gbc.gridy++;
        }

        //Col 3
        gbc.gridx++;
        gbc.gridy = 1;

        for(int i = 0; i<rows; i++) {
            gridPanel.add(buttons.get(i), gbc);
            gbc.gridy++;
        }

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy++;
        gbc.gridx = 0;
        gridPanel.add(saveSettingsBtn, gbc);
        gbc.gridx++;
        gridPanel.add(cancelCfg, gbc);

        JScrollPane gridPane = new JScrollPane(gridPanel);
        cfgPanel.add(gridPane);
    }

    /**
     * Clears the entire container.
     */
    public void clearContainer(){
        container.removeAll();
        container.revalidate();
    }

    /**
     * Opens the file chooser and lets the user choose a new file location.
     * @param row To identify which path which will be updated.
     */
    private void updatePath(String row) {
        JFileChooser fc = new JFileChooser("C:/");
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int response = fc.showOpenDialog(container);

        if (response == JFileChooser.APPROVE_OPTION) {
            String key = keys.get(Integer.parseInt(row)).getText();
            String value = fc.getSelectedFile().getAbsolutePath();

            newPropKV.add(key);
            newPropKV.add(value);
            values.get(Integer.parseInt(row)).setText(value);

            saveSettingsBtn.setEnabled(true);
        } else {
            System.out.println("Cancelling choosing new path");//#NOSONAR
        }
    }

    /**
     * Regular getter for newly edited configuration property.
     * @return List of key and value strings of the new property.
     */
    public List<String> getNewProp() {
        return newPropKV;
    }
}
