package arkivmester;

import java.io.IOException;

/**
 * Interface listener for {@link arkivmester.Views} and {@link arkivmester.ArchiveController}.
 *
 * Implemented by the {@link arkivmester.ArchiveController} to subscribe to notifications by the {@link arkivmester.Views}.
 * Any observer must subscribe to a {@link arkivmester.Views} and implement the following functions.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public interface ViewObserver {

    /**
     * Starts the test, updates GUI.
     */
    void testStarted() throws IOException;

    /**
     * Cancels current test and wants to start a new, resets GUI and backend operations.
     */
    void newTest();

    /**
     * Opens the edit administrative information GUI.
     */
    void editAdminInfo();

    /**
     * Saves the current information and updates {@link arkivmester.ArchiveModel}'s adminInfoList.
     */
    void saveAdminInfo();

    /**
     * Cancels the current operation and returns to {@link arkivmester.MainView} view.
     */
    void cancelButton();

    /**
     * Opens the choose subtests GUI.
     */
    void chooseTests();

    /**
     * Prompts the user to upload the archive to be tested.
     *
     * A file explorer opens where the user can navigate to the correct folder containing only a .tar archive file and
     * a .xml metadata file.
     */
    void uploadArchive();

    /**
     * Starts generating the full report. Only allowed when all subtests are completed.
     */
    void makeReport();

    /**
     * Saves the current information and updates {@link arkivmester.ThirdPartiesModel}'s selectedTests.
     */
    void saveTestSettings();
}
