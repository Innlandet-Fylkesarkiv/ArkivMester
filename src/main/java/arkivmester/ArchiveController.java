package arkivmester;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Arrays;

/**
 * Serves as the link between the views and the models.
 *
 * Controls the software by connecting the views and the models together. This class only chooses what actions will be
 * be performed and when, not how.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ArchiveController implements ViewObserver {
    MainView mainView;
    TestView testView;
    AdminInfoView adminInfoView;
    TestSettingsView testSettingsView;
    SettingsView settingsView;
    ArchiveModel archiveModel;
    ReportModel reportModel;
    ArkadeModel testModel;
    ThirdPartiesModel thirdPartiesModel;
    SettingsModel settingsModel;

    ArchiveController() {
        mainView = new MainView();
        archiveModel = new ArchiveModel();
        reportModel = new ReportModel();
        testModel = new ArkadeModel();
        thirdPartiesModel = new ThirdPartiesModel();
        settingsModel = new SettingsModel();
    }

    /**
     * Starts the application by setting up the GUI.
     */
    public void start() {
        mainView.createFrame();
        mainView.createAndShowGUI();
        mainView.addObserver(this);

        try {
            settingsModel.setUpSettings();
        } catch (IOException e) {
            mainView.exceptionPopup("Kan ikke lese config fil.");
        }
    }

    //When "Start testing" is clicked.
    @Override
    public void testStarted() {
        testView = new TestView();
        testView.addObserver(this);
        testView.createAndShowGUI(mainView.getContainer());
        mainView.toggleEditInfoBtn();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.submit(this::runTests);

        testView.updateTestStatus(TestView.TESTDONE);
    }

    /**
     * Unzips the archive and runs the selected tests.
     */
    private void runTests() {
        List<Boolean> selectedTests = thirdPartiesModel.getSelectedTests();
        String fileName = archiveModel.tar.getName();
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));

        thirdPartiesModel.unzipArchive(archiveModel.tar);
        System.out.println("\n\tArchive unzipped\n"); //NOSONAR


        if(Boolean.TRUE.equals(selectedTests.get(0))) {
            System.out.print("\nRunning arkade\n"); //NOSONAR
            testView.updateArkadeStatus(TestView.RUNNING);
            thirdPartiesModel.runArkadeTest(archiveModel.tar);
            System.out.println("\n\tArkade test finished\n"); //NOSONAR
            testView.updateArkadeStatus(TestView.DONE);
        }
        if(Boolean.TRUE.equals(selectedTests.get(1))) {
            System.out.println("\nRunning DROID\n"); //NOSONAR
            testView.updateDroidStatus(TestView.RUNNING);
            testView.updateDroidStatus(TestView.DONE);

        }
        if(Boolean.TRUE.equals(selectedTests.get(2))) {
            System.out.print("\nRunning Kost-Val\n"); //NOSONAR
            testView.updateKostValStatus(TestView.RUNNING);
            thirdPartiesModel.runKostVal("C:\\archive\\" + "test" + "\\pakke\\content\\dokument");
            System.out.println("\n\tKost-Val test finished\n"); //NOSONAR
            testView.updateKostValStatus(TestView.DONE);
        }
        if(Boolean.TRUE.equals(selectedTests.get(3))) {
            System.out.print("\nRunning VeraPDF\n"); //NOSONAR
            testView.updateVeraStatus(TestView.RUNNING);
            thirdPartiesModel.runVeraPDF("C:\\archive\\" + fileName + "\\content\\dokument");
            System.out.println("\n\tVeraPDF test finished\n"); //NOSONAR
            testView.updateVeraStatus(TestView.DONE);
        }
    }

    //When "Test nytt uttrekk" is clicked.
    @Override
    public void newTest() {
        testView.clearContainer();
        testView = null;
        mainView.showGUI();
        mainView.resetMainView();
        archiveModel.resetAdminInfo();
        thirdPartiesModel.resetSelectedTests();
    }

    //When "Innstillinger" is clicked.
    @Override
    public void openSettings() {
        settingsView = new SettingsView();
        settingsView.addObserver(this);
        settingsView.createAndShowGUI(mainView.getContainer(), settingsModel.prop);
    }

    @Override
    public void saveSettings() {
        List<String> newProp = settingsView.getNewProp();

        try {
            settingsModel.updateConfig(newProp.get(0), newProp.get(1));
        } catch (IOException e) {
            mainView.exceptionPopup("Kan ikke skrive til config fil.");
        }

        settingsView.clearContainer();
        settingsView = null;
        mainView.showGUI();
    }

    //When "Rediger informasjon" is clicked.
    @Override
    public void editAdminInfo() {
        adminInfoView = new AdminInfoView();
        adminInfoView.addObserver(this);
        adminInfoView.createAndShowGUI(mainView.getContainer());
        adminInfoView.populateAdminInfo(archiveModel.getAdminInfo());
    }

    //When "Lagre" in admin info is clicked.
    @Override
    public void saveAdminInfo() {
        archiveModel.updateAdminInfo(adminInfoView.getManualInfo());

        adminInfoView.clearContainer();
        adminInfoView = null;

        mainView.showGUI();
        mainView.updateAdminInfo(archiveModel.getAdminInfo());
    }

    //When "Avbryt" is clicked.
    @Override
    public void cancelButton() {
        if(adminInfoView != null) {
            adminInfoView.clearContainer();
            adminInfoView = null;
        }
        else if (testSettingsView != null){
            testSettingsView.clearContainer();
            testSettingsView = null;
        }
        else if (settingsView != null){
            settingsView.clearContainer();
            settingsView = null;
        }

        mainView.showGUI();
    }

    //When "Velg tester" is clicked.
    @Override
    public void chooseTests() {
        testSettingsView = new TestSettingsView(thirdPartiesModel.getSelectedTests());
        testSettingsView.addObserver(this);
        testSettingsView.createAndShowGUI(mainView.getContainer());
    }

    //When "Last inn pakket uttrekk" is clicked.
    @Override
    public void uploadArchive() {
        int success = archiveModel.uploadFolder(mainView.getContainer());

        //Folder uploaded
        if(success == 1) {
            //Reset data
            archiveModel.resetAdminInfo();
            thirdPartiesModel.resetSelectedTests();

            //Get admin info
            List<String> list = thirdPartiesModel.runBaseX(archiveModel.xmlMeta.getAbsolutePath(), "admininfo.xq", settingsModel.prop);
            list = archiveModel.formatDate(list);
            archiveModel.updateAdminInfo(list);

            //Update view
            mainView.activateButtons();
            mainView.updateAdminInfo(archiveModel.getAdminInfo());
        }
        //Faulty folder
        else if(success == 0) {
            mainView.exceptionPopup("Mappen inneholder ikke .tar og .xml");
        }
    }

    //When "Lag rapport" is clicked.
    @Override
    public void makeReport() {
        String format = testView.getSelectedFormat(); //#NOSONAR

        reportModel.generateReport(); // big question: (1 == 2) ? 3 : 2

        reportModel.setNewInput(Arrays.asList(1, 1), archiveModel.getAdminInfo());

        testModel.parseReportHtml();

        reportModel.writeReportDocument();     // editing
        reportModel.printReportToFile();
    }

    //When "Lagre tests" is clicked.
    @Override
    public void saveTestSettings() {
        //Get settings Save settings
        thirdPartiesModel.updateSelectedTests(testSettingsView.getSelectedTests());
        testSettingsView.clearContainer();
        testSettingsView = null;

        mainView.showGUI();
    }
}
