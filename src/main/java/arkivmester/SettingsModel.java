package arkivmester;

import java.io.*;
import java.nio.file.Files;
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
     * @throws IOException The file could not be read or written to due to permissions or not existing.
     */
    public void setUpSettings() throws IOException {
        userFolder = new File(System.getProperty("user.home") + "\\.arkivmester");
        alteredCfg = new File(userFolder.getPath() + "\\config.properties");

        if(userFolder.exists() && userFolder.isDirectory()) {
            if(alteredCfg.exists() && !alteredCfg.isDirectory())
                loadAlteredConfig();
            else
                createConfig();
        }
        else {
            Files.createDirectory(userFolder.toPath());
            createConfig();
        }

        handleTempFolder();
    }

    /**
     * If temp folder does not exist it will be created.
     * @throws IOException Folder in user.home could not be created.
     */
    private void handleTempFolder() throws IOException {
        File tempFolder = new File(userFolder.getPath() + "\\temp");

        if(!tempFolder.exists()) {
            Files.createDirectory(tempFolder.toPath());
        }

        updateConfig("tempFolder", tempFolder.getAbsolutePath());
    }

    /**
     * If output folders do not exist they will be created.
     *
     * Output folders are KostVal, VeraPDF, DROID and Arkade.
     * @throws IOException Folder in user.home could not be created.
     */
    public void handleOutputFolders() throws IOException {
        //KostVal
        File kostValFolder = new File(userFolder.getPath() + "\\temp\\KostVal");
        if(!kostValFolder.exists()) {
            Files.createDirectory(kostValFolder.toPath());
        }

        //VeraPDF
        File veraPdfFolder = new File(userFolder.getPath() + "\\temp\\VeraPDF");
        if(!veraPdfFolder.exists()) {
            Files.createDirectory(veraPdfFolder.toPath());
        }

        //DROID
        File droidFolder = new File(userFolder.getPath() + "\\temp\\DROID");
        if(!droidFolder.exists()) {
            Files.createDirectory(droidFolder.toPath());
        }

        //Arkade
        File arkadeFolder = new File(userFolder.getPath() + "\\temp\\Arkade");
        if(!arkadeFolder.exists()) {
            Files.createDirectory(arkadeFolder.toPath());
        }

        //Arkade Output
        File arkadeOutputFolder = new File(userFolder.getPath() + "\\temp\\Arkade\\Report");
        if(!arkadeOutputFolder.exists()) {
            Files.createDirectory(arkadeOutputFolder.toPath());
        }

        //TestReport
        File testReportFolder = new File(userFolder.getPath() + "\\temp\\TestReport");
        if(!testReportFolder.exists()) {
            Files.createDirectory(testReportFolder.toPath());
        }
    }

    /**
     * If a config.priorities file does not exist in user.home it will be created by
     * using the default file in resources.
     * @throws IOException Config file in user.home could not be created.
     */
    private void createConfig() throws IOException {
        loadDefaultConfig();
        Files.createFile(alteredCfg.toPath());

        try (FileOutputStream fos = new FileOutputStream(alteredCfg)) {
            prop.store(fos, null);
        }
    }

    /**
     * Loads the default config.priorities file from resources into Properties prop.
     * @throws IOException Properties object could not load input stream.
     */
    private void loadDefaultConfig() throws IOException {
        InputStream is = getClass().getResourceAsStream("/config.properties");
        prop.load(is);
        is.close();
    }

    /**
     * Loads the config.priorities file from user.home into Properties prop.
     * @throws IOException Properties object could not load input stream.
     */
    private void loadAlteredConfig() throws IOException {
        try (FileInputStream fis = new FileInputStream(alteredCfg)) {
            prop.load(fis);
        }
    }

    /**
     * Updates a property already existing in the config.properties file in user.home.
     * @param key Property key that will be updated.
     * @param value Value to update property with.
     * @throws IOException Properties object could write to output stream.
     */
    public void updateConfig(String key, String value) throws IOException {
        prop.setProperty(key, value);

        try (FileOutputStream fos = new FileOutputStream(alteredCfg)){
            prop.store(fos, null);
        }
    }
}
