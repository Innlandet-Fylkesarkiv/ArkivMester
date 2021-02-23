package arkivmester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ArchiveController implements ViewObserver {
    MainView mainView;
    TestView testView;
    AdminInfoView adminInfoView;
    TestSettingsView testSettingsView;
    ArchiveModel archiveModel;
    RapportModel rapportModel;
    TestModel testModel;
    ThirdPartiesModel thirdPartiesModel;

    ArchiveController() {
        mainView = new MainView();
        archiveModel = new ArchiveModel();
        rapportModel = new RapportModel();
        testModel = new TestModel();
        thirdPartiesModel = new ThirdPartiesModel();
    }


    public void start() {
        mainView.createFrame();
        mainView.createAndShowGUI();
        mainView.addObserver(this);
        rapportModel.start();
    }

    //When "Start testing" is clicked.
    @Override
    public void testStarted() {
        testView = new TestView();
        testView.addObserver(this);
        testView.createAndShowGUI(mainView.getContainer());
        mainView.removeEditInfoBtn();

        //String fileName = archiveModel.tar.getName();
        //fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        //System.out.println(fileName);


        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.submit(this::runTests);

        //thirdPartiesModel.unzipArchive(archiveModel.tar);
        //System.out.println("\n\tArchive unzipped\n"); //NOSONAR
        //thirdPartiesModel.runArkadeTest(archiveModel.tar);
        //System.out.println("\n\tArkade test finished\n"); //NOSONAR
        //thirdPartiesModel.runKostVal("C:\\archive\\test\\pakke\\content\\DOKUMENT");
        //System.out.println("\n\tKost-Val test finished\n"); //NOSONAR
        //thirdPartiesModel.runVeraPDF("C:\\archive\\test\\pakke\\content\\DOKUMENT");
        //System.out.println("\n\tVeraPDF test finished\n"); //NOSONAR
        //thirdPartiesModel.runKostVal("c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\dokument"); //NOSONAR
        //thirdPartiesModel.runVeraPDF("c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\DOKUMENT"); //NOSONAR
    }

    private void runTests() {
        List<Boolean> selectedTests = thirdPartiesModel.getSelectedTests();
        String fileName = archiveModel.tar.getName();
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));

        thirdPartiesModel.unzipArchive(archiveModel.tar);
        System.out.println("\n\tArchive unzipped\n"); //NOSONAR


        if(Boolean.TRUE.equals(selectedTests.get(0))) {
            System.out.print("\nRunning arkade\n"); //NOSONAR
            thirdPartiesModel.runArkadeTest(archiveModel.tar);
            System.out.println("\n\tArkade test finished\n"); //NOSONAR
        }
        if(Boolean.TRUE.equals(selectedTests.get(1))) {
            System.out.println("\nRunning DROID\n"); //NOSONAR
            // TODO: Run DROID
        }
        if(Boolean.TRUE.equals(selectedTests.get(2))) {
            System.out.print("\nRunning Kost-Val\n"); //NOSONAR
            thirdPartiesModel.runKostVal("C:\\archiv\\\test\\pakke\\content\\DOKUMENT");
            System.out.println("\n\tKost-Val test finished\n"); //NOSONAR
        }
        if(Boolean.TRUE.equals(selectedTests.get(3))) {
            System.out.print("\nRunning VeraPDF\n"); //NOSONAR
            thirdPartiesModel.runVeraPDF("C:\\archive\\test\\pakke\\content\\DOKUMENT");
            System.out.println("\n\tVeraPDF test finished\n"); //NOSONAR
        }
    }

    //When "Test nytt uttrekk" is clicked.
    @Override
    public void newTest() {
        testView.clearContainer();
        testView = null;
        mainView.showGUI();
        mainView.resetMainView();
        rapportModel.resetAdminInfo();
        thirdPartiesModel.resetSelectedTests();
    }

    //When "Rediger informasjon" is clicked.
    @Override
    public void editAdminInfo() {
        adminInfoView = new AdminInfoView();
        adminInfoView.addObserver(this);
        adminInfoView.createAndShowGUI(mainView.getContainer());
        adminInfoView.populateAdminInfo(rapportModel.getAdminInfo());
    }

    //When "Lagre" in admin info is clicked.
    @Override
    public void saveAdminInfo() {
        rapportModel.updateAdminInfo(adminInfoView.getManualInfo());

        adminInfoView.clearContainer();
        adminInfoView = null;

        mainView.showGUI();
        mainView.updateAdminInfo(rapportModel.getAdminInfo());
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
            mainView.activateButtons();
            rapportModel.resetAdminInfo();
            rapportModel.readAdminXmlFile(archiveModel.xmlMeta);
            thirdPartiesModel.resetSelectedTests();
            mainView.updateAdminInfo(rapportModel.getAdminInfo());
        }
        //Faulty folder
        else if(success == 0) {
            System.out.println("Mappen inneholder ikke .tar og .xml");//#NOSONAR
        }
    }

    //When "Lag rapport" is clicked.
    @Override
    public void makeReport() {
        String format = testView.getSelectedFormat(); //#NOSONAR
        //generateReport() ...
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
