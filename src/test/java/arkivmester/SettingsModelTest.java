package arkivmester;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SettingsModelTest {
    SettingsModel settingsModel;

    Properties configRes;
    Properties prop;

    @BeforeEach
    void setUp() {
        settingsModel = new SettingsModel();

        configRes = new Properties();
        InputStream is = getClass().getResourceAsStream("/config.properties");
        try {
            configRes.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace(); //#NOSONAR
        }
    }

    @Test
    @DisplayName("Ensure setting up the settings works properly")
    void setUpSettingsTest() {

        //Make sure SettingsModel userfolder's path is set to .arkivmesterTest when using this unittest.
        try {
            settingsModel.setUpSettings();
            String home = System.getProperty("user.home");
            File userFolder = new File(home + "\\.arkivmesterTest");
            assertTrue(userFolder.exists(), "Userfolder does not exist");
            assertTrue(userFolder.isDirectory(), "Userfolder is not a directory");

            File [] files = userFolder.listFiles();
            assert files != null;
            assertEquals(2, files.length, "There are not 2 children in user folder");

            File config = files[0];


            prop = new Properties();
            FileInputStream fis = new FileInputStream(config);
            prop.load(fis);
            fis.close();

            assertEquals(home + "\\.arkivmesterTest\\temp", prop.get("tempFolder"), "Temp folder path in cfg is wrong");

            settingsModel.updateConfig("tempFolder", "C://test");
            assertEquals("C://test", settingsModel.prop.get("tempFolder"), "Temp folder path in cfg is wrong");

            List<String> keyList = Arrays.asList("droidPath","7ZipPath");
            List<String> valueList = Arrays.asList("C:\\droid","C:\\7zip");

            settingsModel.updateConfig(keyList, valueList);
            assertEquals("C:\\droid", settingsModel.prop.get("droidPath"), "Droid folder path in cfg is wrong");
            assertEquals("C:\\7zip", settingsModel.prop.get("7ZipPath"), "7Zip folder path in cfg is wrong");

            settingsModel.resetCfg();
            assertEquals("C:\\prog\\Droid", settingsModel.prop.get("droidPath"), "Droid folder path in cfg is wrong");
            assertEquals("C:\\Programfiler\\7-Zip", settingsModel.prop.get("7ZipPath"), "7Zip folder path in cfg is wrong");
            assertEquals(userFolder.toPath() + "\\temp", settingsModel.prop.get("tempFolder"), "Temp folder path in cfg is wrong");

        } catch (IOException e) {
            fail("Could not set up settings");
        }
    }

    @Test
    @DisplayName("Ensure creating output folders works properly")
    void handleOutputFoldersTest() {
        String archiveName = "testabc";
        File archiveFolder = new File(System.getProperty("user.home") +  "\\.arkivmesterTest\\temp\\" + archiveName);
        try {
            settingsModel.setUpSettings();

            settingsModel.handleOutputFolders(archiveName);

            assertTrue(archiveFolder.exists(), "Archivefolder does not exist");
            assertTrue(archiveFolder.isDirectory(), "Archivefolder is not a directory");

            File [] folders = archiveFolder.listFiles();
            assert folders != null;
            assertEquals(5, folders.length, "There are not 5 output folders in archive folder");

            assertAll("Output folders",
                    () -> assertTrue(folders[0].exists(), "Arkade folder does not exist"),
                    () -> assertTrue(folders[0].isDirectory(), "Arkade folder is not a directory"),
                    () -> assertEquals("Arkade", folders[0].getName(), "Arkade's output folder has the wrong name"),
                    () -> assertTrue(folders[1].exists(), "DROID folder does not exist"),
                    () -> assertTrue(folders[1].isDirectory(), "DROID folder is not a directory"),
                    () -> assertEquals("DROID", folders[1].getName(), "DROID's output folder has the wrong name"),
                    () -> assertTrue(folders[2].exists(), "KostVal folder does not exist"),
                    () -> assertTrue(folders[2].isDirectory(), "KostVal folder is not a directory"),
                    () -> assertEquals("KostVal", folders[2].getName(), "KostVal's output folder has the wrong name"),
                    () -> assertTrue(folders[3].exists(), "Rapporter folder does not exist"),
                    () -> assertTrue(folders[3].isDirectory(), "Rapporter folder is not a directory"),
                    () -> assertEquals("Rapporter", folders[3].getName(), "Rapporter's output folder has the wrong name"),
                    () -> assertTrue(folders[4].exists(), "VeraPDF folder does not exist"),
                    () -> assertTrue(folders[4].isDirectory(), "VeraPDF folder is not a directory"),
                    () -> assertEquals("VeraPDF", folders[4].getName(), "VeraPDF's output folder has the wrong name")
            );
        } catch (IOException e) {
            fail("Could not handle create folders for output");
        }
    }

    @AfterAll
    static void tearDown() {
        File userFolder = new File(System.getProperty("user.home") + "\\.arkivmesterTest");
        if(userFolder.exists() && userFolder.isDirectory()) {
            Path directory = userFolder.toPath();
            try {
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
            } catch (IOException e) {
                fail("Could not delete user folder");
            }
        }
        else
            fail("User folder does not exist or path is wrong");
    }
}