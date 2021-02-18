package arkivmester;

import java.io.IOException;

//Interface listener for view
public interface ViewObserver {
    void testStarted() throws IOException;
    void newTest();
    void editAdminInfo();
    void saveAdminInfo();
    void cancelButton();
    void chooseTests();
    void uploadArchive();
    void makeReport();
    void saveTestSettings();
}
