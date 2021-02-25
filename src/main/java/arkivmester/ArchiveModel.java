package arkivmester;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds all data about the archive and its relevant utility functions.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ArchiveModel {
    File folder;
    File tar;
    File xmlMeta;

    private List<String> adminInfoList = new ArrayList<>(); //Always have 8 elements
    int amountAdminFields = 8;

    /**
     * Constructor - Initiates adminInfoList
     */
    ArchiveModel() {
        for (int i = 0; i<amountAdminFields; i++) {
            adminInfoList.add("");
        }
    }

    /**
     * Opens file explorer and saves selected folder to File folder.
     * @param container Frame's container used as location to create the file chooser.
     * @return 1 if successful, 0 if failed or -1 if cancelled
     */
    public int uploadFolder(Container container) {

        JFileChooser fc = new JFileChooser("C:/");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int response = fc.showOpenDialog(container);

        //If folder is chosen
        if (response == JFileChooser.APPROVE_OPTION) {
            folder = new File(fc.getSelectedFile().getAbsolutePath());

            FilenameFilter filter = (f, name) -> name.endsWith(".tar") || name.endsWith(".xml");
            File [] files = folder.listFiles(filter);

            //If there are 2 files in the directory
            if(files != null && files.length == 2) {
                if(files[0].getName().endsWith(".tar") && files[1].getName().endsWith(".xml")){
                    tar = files[0];
                    xmlMeta = files[1];
                    return 1;
                }
                else if (files[0].getName().endsWith(".xml") && files[1].getName().endsWith(".tar")) {
                    xmlMeta = files[0];
                    tar = files[1];
                    return 1;
                }
                else {
                    return 0;
                }
            }
            else {
                return 0;
            }
        } else {
            System.out.println("Cancelling opening document");//#NOSONAR
            return -1;
        }
    }

    /**
     * Regular getter for saved edited administrative information data.
     * @return String list of administrative data.
     */
    public List<String> getAdminInfo() {
        return adminInfoList;
    }

    /**
     * Updates adminInfoList with new information.
     * @param list String list of new administrative information data to be saved.
     */
    public void updateAdminInfo(List<String> list) {
        adminInfoList = list;
    }

    /**
     * Resets administrative information data saved in adminInfoList.
     */
    public void resetAdminInfo() {
        for (int i = 0; i<adminInfoList.size(); i++) {
            adminInfoList.set(i, "");
        }
    }
}
