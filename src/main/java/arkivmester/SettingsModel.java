package arkivmester;

import java.io.*;
import java.util.Properties;

/**
 * Contains configuration utility functions and the properties object.
 *
 * Contains multiple methods to run different third party tools to test the archive.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class SettingsModel {
    private File userFolder;
    private File alteredCfg;

    /**
     * Public properties object containing the application's configurations.
     */
    Properties prop = new Properties();

    /**
     * Creates user.home folder with temp folder and config.priorities.
     * @return True or false depending if the operation was successful.
     */
    public Boolean setUpSettings() { // #NOSONAR (Creating tempFolder exceeds the Cognitive Complexity with 2)
        userFolder = new File(System.getProperty("user.home") + "\\.arkivmester");
        alteredCfg = new File(userFolder.getPath() + "\\config.properties");

        try {
            if(userFolder.exists() && userFolder.isDirectory()) {

                if(alteredCfg.exists() && !alteredCfg.isDirectory())
                    loadAlteredConfig();
                else {
                    if(Boolean.FALSE.equals(createConfig()))
                        return false;
                }
            }
            else {
                if(!userFolder.mkdir())
                    return false;

                if(Boolean.FALSE.equals(createConfig()))
                    return false;
            }

            if(Boolean.FALSE.equals(createTempFolder())) {
                return false;
            }
        } catch (SecurityException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }

        return true;
    }

    /**
     * If temp folder does not exist it will be created.
     * @return True or false depending if the operation was successful.
     */
    private Boolean createTempFolder() {
        File tempFolder = new File(userFolder.getPath() + "\\temp");
        if(!tempFolder.exists()) {
            if(tempFolder.mkdir()) {
                updateConfig("tempFolder", tempFolder.getAbsolutePath());
                return true;
            }
            else
                return false;
        }
        else {
            updateConfig("tempFolder", tempFolder.getAbsolutePath());
        }
        return true;
    }

    /**
     * If a config.priorities file does not exist in user.home it will be created by
     * using the default file in resources.
     * @return True or false depending if the operation was successful.
     */
    private Boolean createConfig() {
        loadDefaultConfig();

        try {
            if(!alteredCfg.createNewFile())
                return false;

            try (FileOutputStream fos = new FileOutputStream(alteredCfg)) {
                prop.store(fos, null);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }

        return true;
    }

    /**
     * Loads the default config.priorities file from resources into Properties prop.
     */
    private void loadDefaultConfig() {
        try (InputStream is = getClass().getResourceAsStream("/config.properties")){
            prop.load(is);
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }
    }

    /**
     * Loads the config.priorities file from user.home into Properties prop.
     */
    private void loadAlteredConfig() {
        try (FileInputStream fis = new FileInputStream(alteredCfg)) {
            prop.load(fis);
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }
    }

    /**
     * Updates a property already existing in the config.properties file in user.home.
     * @param key Property key that will be updated.
     * @param value Value to update property with.
     */
    public void updateConfig(String key, String value) {
        prop.setProperty(key, value);

        try (FileOutputStream fos = new FileOutputStream(alteredCfg)){
            prop.store(fos, null);
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }
    }
}
