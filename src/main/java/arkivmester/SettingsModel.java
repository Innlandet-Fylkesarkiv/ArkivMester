package arkivmester;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
    private static final String CURRENTARCHIVE = "currentArchive";

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
        File archiveFolder = new File(userFolder.getPath() + "\\temp\\" + fileName);
        if(!archiveFolder.exists()) {
            Files.createDirectory(archiveFolder.toPath());
        }
        else {
            File unzipped = new File(archiveFolder.getPath() + "\\" + fileName); // #NOSONAR
            if(unzipped.exists()) {
                deleteUnZippedArchive();
            }
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
     * @param value Value to update property with.
     * @throws IOException Properties object could write to output stream.
     */
    public void updateConfig(String key, String value) throws IOException {
        prop.setProperty(key, value);

        try (FileOutputStream fos = new FileOutputStream(alteredCfg)){
            prop.store(fos, null);
        }
    }

    /**
     * Deletes the unzipped archive in preperation to test the archive again.
     * @throws IOException No permissions in tempFolder path.
     */
    public void deleteUnZippedArchive() throws IOException {
        String archive = (String)prop.get(CURRENTARCHIVE);
        File zipped = new File(prop.get("tempFolder") + "\\"+ archive + "\\" + archive); // #NOSONAR

        if(zipped.exists()) {
            Path directory = zipped.toPath();
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
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
