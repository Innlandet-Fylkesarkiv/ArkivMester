package arkivmester;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    ArkadeModel arkadeModel;
    ThirdPartiesModel thirdPartiesModel;
    SettingsModel settingsModel;

    ArchiveController() {
        mainView = new MainView();
        archiveModel = new ArchiveModel();
        reportModel = new ReportModel();
        arkadeModel = new ArkadeModel();
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

    private void arkadeTestReport(){
        // 3 og 3.1 arkade version
        String version = arkadeModel.getArkadeVersion().replace("Arkade 5 versjon: ", "");

        reportModel.setNewInput(Arrays.asList(3, 1), Collections.singletonList(version));
        // 3.1.1
        writeDeviation(Arrays.asList(3, 1, 1),"N5.01", "Lokasjon", "Avvik");
        writeDeviation(Arrays.asList(3, 1, 1),"N5.02", "Lokasjon2", "Avvik2");

    }
    private void writeDeviation(List<Integer> kap, String index, String header1, String header2){
        List<String> avvik = arkadeModel.getDataFromHtml(index);
        if (!avvik.isEmpty()) {
            reportModel.setNewTable(kap, Arrays.asList(Arrays.asList(header1, header2), avvik));
        } else {
            reportModel.setNewParagraph(kap, Collections.singletonList("Uttrekket er teknisk korrekt."));
        }
    }

    //When "Start testing" is clicked.
    @Override
    public void testStarted() {
        testView = new TestView();
        testView.addObserver(this);
        testView.createAndShowGUI(mainView.getContainer());
        mainView.toggleEditInfoBtn();

        //Schedule the runTests function to give the UI time to update before tests are run.
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.submit(this::runTests);

        testView.updateTestStatus(TestView.TESTDONE);
    }

    /**
     * Unzips the archive and runs the selected tests.
     */
    private void runTests() {
        List<Boolean> selectedTests = thirdPartiesModel.getSelectedTests();
        String fileName = archiveModel.tar.getName();                                   // NOSONAR
        //fileName = fileName.substring(0,fileName.lastIndexOf('.'));                   // NOSONAR
        String docPath = "C:\\archive\\" + "test" + "\\pakke\\content\\dokument";
        //Should use the one below, but takes too long
        //String docPath = settingsModel.prop.getProperty("7ZipOutput") + fileName + "\\content\\dokument"; // NOSONAR

        //Unzips .tar folder with the archive.
        try {
            thirdPartiesModel.unzipArchive(archiveModel.tar, settingsModel.prop);
        }catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
            mainView.exceptionPopup("Kunne ikke unzippe arkivet, prøv igjen.");
        }
        System.out.println("\n\tArchive unzipped\n"); //NOSONAR

        //Run tests depending on if they are selected or not.
        if(Boolean.TRUE.equals(selectedTests.get(0))) {
            System.out.print("\nRunning arkade\n"); //NOSONAR
            testView.updateArkadeStatus(TestView.RUNNING);
            try {

                thirdPartiesModel.runArkadeTest(archiveModel.tar, settingsModel.prop);

            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("Arkade test feilet, prøv igjen.");

            }
            System.out.println("\n\tArkade test finished\n"); //NOSONAR
            testView.updateArkadeStatus(TestView.DONE);
        }
        if(Boolean.TRUE.equals(selectedTests.get(1))) {
            System.out.println("\nRunning DROID\n"); //NOSONAR
            testView.updateDroidStatus(TestView.RUNNING);
            try {
                thirdPartiesModel.runDROID(docPath, settingsModel.prop);
            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("DROID test feilet, prøv igjen.");
            }
            System.out.println("\n\tDROID finished\n"); //NOSONAR
            testView.updateDroidStatus(TestView.DONE);

        }
        if(Boolean.TRUE.equals(selectedTests.get(2))) {
            System.out.print("\nRunning Kost-Val\n"); //NOSONAR
            testView.updateKostValStatus(TestView.RUNNING);
            try {
                thirdPartiesModel.runKostVal(docPath, settingsModel.prop);
            }catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("Kost-Val test feilet, prøv igjen.");
            }
            System.out.println("\n\tKost-Val test finished\n"); //NOSONAR
            testView.updateKostValStatus(TestView.DONE);
        }
        if(Boolean.TRUE.equals(selectedTests.get(3))) {
            System.out.print("\nRunning VeraPDF\n"); //NOSONAR
            testView.updateVeraStatus(TestView.RUNNING);
            try {
                thirdPartiesModel.runVeraPDF(docPath, settingsModel.prop);
            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("VeraPDF test feilet, prøv igjen");
            }
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
        cancelButton();
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

        Map<String, String> map = new LinkedHashMap<>();

        map.put("1.2_1.xq","C:\\Arkade5\\arkade-tmp\\work\\20210304224533-899ec389-1dc0-41d0-b6ca-15f27642511b\\dias-mets.xml");
        map.put("1.2_2.xq","C:\\Arkade5\\arkade-tmp\\work\\20210304224533-899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\arkivuttrekk.xml");
        map.put("1.2_3.xq","C:\\Arkade5\\arkade-tmp\\work\\20210304224533-899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\loependeJournal.xml");
        map.put("1.2_4.xq","C:\\Arkade5\\arkade-tmp\\work\\20210304224533-899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\offentligJournal.xml");
        map.put("1.2_5.xq","C:\\Arkade5\\arkade-tmp\\work\\20210304224533-899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\arkivstruktur.xml");

        List<String> list = new ArrayList<>();

        for(Map.Entry<String, String> entry : map.entrySet()) {
            list.addAll(thirdPartiesModel.runBaseX(entry.getValue(), entry.getKey(), settingsModel.prop));
        }

        reportModel.setNewInput(Arrays.asList(1, 2), list);

        arkadeModel.parseReportHtml(); // remove when all function used in testModel
        arkadeTestReport();

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
