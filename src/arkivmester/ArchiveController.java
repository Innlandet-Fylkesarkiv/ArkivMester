package arkivmester;

import java.util.Arrays;

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
        rapportModel.generateReport();
    }

    //When "Start testing" is clicked.
    @Override
    public void testStarted() {
        testView = new TestView();
        testView.addObserver(this);
        testView.createAndShowGUI(mainView.getContainer());
        mainView.removeEditInfoBtn();
        //thirdPartiesModel.runArkadeTest("c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar"); //NOSONAR
        //thirdPartiesModel.runKostVal("c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\dokument"); //NOSONAR
        //thirdPartiesModel.runVeraPDF("c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\DOKUMENT"); //NOSONAR
        //thirdPartiesModel.unzipArchive("c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar"); //NOSONAR

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
            archiveModel.resetAdminInfo();
            archiveModel.readAdminXmlFile(archiveModel.xmlMeta);
            thirdPartiesModel.resetSelectedTests();
            mainView.updateAdminInfo(archiveModel.getAdminInfo());
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

        rapportModel.setNewInput(Arrays.asList(1, 1), archiveModel.getAdminInfo());

        rapportModel.writeReportDocument();     // editing
        rapportModel.printReportToFile();
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
