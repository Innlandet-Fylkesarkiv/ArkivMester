package arkivmester;

import java.io.IOException;

/**
 * Interface listener for {@link Views} and {@link ArchiveController}.
 *
 * Implemented by the {@link ArchiveController} to subscribe to notifications by the {@link Views}.
 * Any observer must subscribe to a {@link Views} and implement the following functions.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public interface ViewObserver {
    /**
     * Prompts the user to upload the archive to be tested.
     *
     * A file explorer opens where the user can navigate to the correct folder containing only a .tar archive file and
     * a .xml metadata file.
     */
    void uploadArchive();

    /**
     * Starts the test, updates GUI.
     */
    void testStarted() throws IOException;

    /**
     * Opens the edit administrative information GUI.
     */
    void editAdminInfo();

    /**
     * Saves the current information and updates {@link ArchiveModel}'s adminInfoList.
     */
    void saveAdminInfo();

    /**
     * Opens the choose subtests GUI.
     */
    void chooseTests();

    /**
     * Saves the current information and updates {@link ThirdPartiesModel}'s selectedTests.
     */
    void saveTestSettings();

    /**
     * Cancels the current operation and returns to {@link MainView} view.
     */
    void cancelButton();

    /**
     * Starts generating the full report. Only allowed when all subtests are completed.
     */
    void makeReport();

    /**
     * Cancels current test and wants to start a new, resets GUI and backend operations.
     */
    void newTest();

    /**
     * Opens the settings view for editing application configurations.
     */
    void openSettings();

    /**
     * Saves the current information and updates the configuration file.
     */
    void saveSettings();

    /**
     * Opens the about view for information about the application.
     */
    void openAbout();

    /**
     * Resets the contents of the settings file to the default.
     */
    void resetCfg();

    /**
     * Packs the unzipped archive with all the tests to an AIP.
     */
    void packToAIP();
}
