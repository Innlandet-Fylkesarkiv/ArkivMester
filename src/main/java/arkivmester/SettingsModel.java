package arkivmester;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

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
    private static final String CURRENTARCHIVE = "currentArchive";
    private File archiveFolder;
    private final List<File> folders = new ArrayList<>();

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
    public void handleOutputFolders(String fileName) throws IOException {
        updateConfig(CURRENTARCHIVE, fileName);

        //Archive folder
        archiveFolder = new File(userFolder.getPath() + "\\temp\\" + fileName);
        if(!archiveFolder.exists()) {
            Files.createDirectory(archiveFolder.toPath());
        }


        //KostVal
        File kostValFolder = new File(archiveFolder.getPath() + "\\KostVal");
        if(!kostValFolder.exists()) {
            Files.createDirectory(kostValFolder.toPath());
        }

        //VeraPDF
        File veraPdfFolder = new File(archiveFolder.getPath() + "\\VeraPDF");
        if(!veraPdfFolder.exists()) {
            Files.createDirectory(veraPdfFolder.toPath());
        }

        //DROID
        File droidFolder = new File(archiveFolder.getPath() + "\\DROID");
        if(!droidFolder.exists()) {
            Files.createDirectory(droidFolder.toPath());
        }

        //Arkade
        File arkadeFolder = new File(archiveFolder.getPath() + "\\Arkade");
        if(!arkadeFolder.exists()) {
            Files.createDirectory(arkadeFolder.toPath());
        }

        //Arkade Output
        File arkadeOutputFolder = new File(arkadeFolder.getPath() + "\\report");
        if(!arkadeOutputFolder.exists()) {
            Files.createDirectory(arkadeOutputFolder.toPath());
        }

        //Reports
        File reportsFolder = new File(archiveFolder.getPath() + "\\Rapporter");
        if(!reportsFolder.exists()) {
            Files.createDirectory(reportsFolder.toPath());
        }

        folders.add(kostValFolder);
        folders.add(droidFolder);
        folders.add(veraPdfFolder);
        folders.add(arkadeOutputFolder);
        folders.add(reportsFolder);
    }

    public void prepareToAIP() throws IOException {
        File repOpsFolder = new File(archiveFolder.getPath() + "\\" + prop.getProperty(CURRENTARCHIVE) + "\\administrative_metadata\\repository_operations"); //NOSONAR
        if(!repOpsFolder.exists()) {
            Files.createDirectory(repOpsFolder.toPath());
        }

        for (File folder : folders) {
            String repOpsPath = archiveFolder.getPath() + "\\" + prop.getProperty(CURRENTARCHIVE) + "\\administrative_metadata\\repository_operations\\"; // #NOSONAR

            repOpsPath += folder.getName();

            File repOps = new File(repOpsPath);
            try (Stream<Path> stream = Files.walk(folder.toPath())) {
                stream.forEach(source -> {
                    try {
                        Files.copy(source, repOps.toPath().resolve(folder.toPath().relativize(source)), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println(e.getMessage()); // #NOSONAR
                    }
                });
            }
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
    public void loadDefaultConfig() throws IOException {
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
     * @param value Property value to update the key with.
     * @throws IOException Properties object could not write to output stream.
     */
    public void updateConfig(String key, String value) throws IOException {
        prop.setProperty(key, value);

        try (FileOutputStream fos = new FileOutputStream(alteredCfg)){
            prop.store(fos, null);
        }
    }

    /**
     * Updates multiple properties already existing in the config.properties file in user.home.
     * @param keyList Property list keys that will be updated.
     * @param valueList Property list values to update the keys with.
     * @throws IOException Properties object could not write to output stream.
     */
    public void updateConfig(List<String> keyList, List<String> valueList) throws IOException {

        for(int i = 0; i<keyList.size(); i++) {
            prop.setProperty(keyList.get(i), valueList.get(i));
        }

        try (FileOutputStream fos = new FileOutputStream(alteredCfg)){
            prop.store(fos, null);
        }
    }

    /**
     * Deletes and recreates config.properties with temporary values.
     * @throws IOException No permissions in tempFolder path.
     */
    public void resetCfg() throws IOException {
        String archive;
        if(alteredCfg.exists()) {
            archive = prop.getProperty(CURRENTARCHIVE);
            Files.delete(alteredCfg.toPath());
            createConfig();
            handleTempFolder();
            updateConfig(CURRENTARCHIVE, archive);
        }
    }
}
