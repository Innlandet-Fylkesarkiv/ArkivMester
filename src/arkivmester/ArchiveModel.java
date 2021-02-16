package arkivmester;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;

public class ArchiveModel {
    File folder;
    File tar;
    File xmlMeta;

    ArchiveModel() {
        //Archive
    }

    //Asks user to choose which folder to upload.
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
}
