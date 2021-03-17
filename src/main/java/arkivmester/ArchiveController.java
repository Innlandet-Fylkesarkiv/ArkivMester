package arkivmester;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    ScheduledExecutorService scheduler;

    /**
     * List of the attachments which will be printed in chapter 5.
     */
    ArrayList<String> attachments = new ArrayList<>();

    /**
     * Initializes models and views.
     */
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

    /**
     *
     */
    private void arkadeTestReport(){ // NOSONAR
        arkadeModel.parseReportHtml(); // remove when all function used in testModel
        // 3 og 3.1 arkade version
        String version = arkadeModel.getArkadeVersion().replace("Arkade 5 versjon: ", "");

        reportModel.setNewInput(Arrays.asList(3, 1), Collections.singletonList(version), 0);
        // 3.1.1
        writeDeviation(Arrays.asList(3, 1, 1),"N5.01");
        writeDeviation(Arrays.asList(3, 1, 1),"N5.02");

        //Chapter 3.1.12
        int arkivert = arkadeModel.getTotal("N5.22", 1);
        int journalfort = arkadeModel.getTotal("N5.22", 5);

        if(journalfort <= 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 0);
        } else {
            if(arkivert <= 0) {
                reportModel.setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 2);
            } else {
                reportModel.setNewInput(Arrays.asList(3, 1, 12), Collections.singletonList("" + journalfort), 1);
            }
        }

        //Chapter 3.1.16 - Saksparter
        List<Integer> saksparter = arkadeModel.saksparter();
        if(saksparter.get(0) > 0){
            reportModel.setNewInput(Arrays.asList(3, 1, 16), Collections.singletonList(
                    saksparter.get(0).toString()), 1);
        } else {
            reportModel.setNewInput(Arrays.asList(3, 1, 16), Collections.emptyList(), 1);
        }

        //Chapter 3.1.17 - Merknader
        if (arkadeModel.ingenMerknader()) {
            reportModel.setNewInput(Arrays.asList(3, 1, 17), Collections.emptyList(), 0);
            reportModel.setNewParagraph(Arrays.asList(3, 1, 17), Collections.singletonList("Rename tittel from 3.1.17 to merknader "));
            reportModel.setNewParagraph(Arrays.asList(3, 3, 3), Collections.singletonList("DELETE ME: 3.3.3"));
        }

        //Chapter 3.1.18 - Kryssreferanser
        if(arkadeModel.getTotal("N5.37", 1) > 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 18), Collections.emptyList() , 0);
        }

        //Chapter 3.1.19 - Presedenser
        if(arkadeModel.getTotal("N5.38", 1) > 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 0);
        }
        else {
            reportModel.setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 1);
        }

        //Chapter 3.1.22 - Dokumentflyter
        if(arkadeModel.getTotal("N5.41",1) <= 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 22), Collections.emptyList(), 0);
            //Delete 3.3.5, Title = Dokumentflyter
        }

        //Chapter 3.1.24 - Gradering
        if(arkadeModel.getTotal("N5.43", 1) <= 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 0);
        }
        else  {
            reportModel.setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 1);
        }

        //Chapter 3.1.25 - Kassasjoner
        if(arkadeModel.getTotal("N5.44", 1) <= 0 && arkadeModel.getTotal("N5.45", 1) <=0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 0);
        }
        else {
            reportModel.setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 1);
        }

        //Chapter 3.1.28 - Arkivdelreferanser
        if(arkadeModel.getDataFromHtml("N5.48").isEmpty()) {
            reportModel.setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 0);
        }
        else {
            reportModel.setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 1);
        }

        //Chapter 3.1.32 - Endringslogg
        // Endre tittel til: Endringslogg testes i kapittel 3.3.8

        //Chapter 3.1.33
        if(arkadeModel.getDataFromHtml("N5.63").isEmpty()) {
            reportModel.setNewInput(Arrays.asList(3, 1, 33), Collections.emptyList(), 0);
        }
        else {
            reportModel.setNewInput(Arrays.asList(3, 1, 33), Collections.emptyList(), 1);
        }

        //Chapter 3.1.4
        //Endre tittel til: Se eget klassifikasjonskapittel 3.3.1.

        //Chapter 3.1.6
        //Endre tittel til: Se eget klassifikasjonskapittel 3.3.1.

        //Chapter 3.1.29
        //Endre tittel til: Se eget klassifikasjonskapittel 3.3.1.

    }


    /**
     *
     * @param kap docx kap
     * @param index test ID
     */
    private void writeDeviation(List<Integer> kap, String index) {
        List<String> avvik = arkadeModel.getDataFromHtml(index);
        if (!avvik.isEmpty()) {
            reportModel.insertTable(kap, avvik);
        } else {
            reportModel.setNewInput(kap, Collections.emptyList(), 0);
        }
    }

    /**
     * Unzips the archive and runs the selected tests.
     */
    private void runTests() {
        List<Boolean> selectedTests = thirdPartiesModel.getSelectedTests();
        thirdPartiesModel.initializePath(settingsModel.prop);
        String fileName = archiveModel.tar.getName();                                   // NOSONAR
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));                   // NOSONAR
        //String docPath = "C:\\archive\\" + "test" + "\\pakke\\content\\dokument"; // NOSONAR
        //Should use the one below, but takes too long
        String docPath = settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\content\\dokument"; // NOSONAR

        //Unzips .tar folder with the archive.
        try {
            thirdPartiesModel.unzipArchive(archiveModel.tar, settingsModel.prop);
        }catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
            mainView.exceptionPopup("Kunne ikke unzippe arkivet, prøv igjen.");
        }
        System.out.println("\n\tArchive unzipped\n"); //NOSONAR

        //Run tests depending on if they are selected or not.
        //Arkade
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
            attachments.add("\u2022 Arkade5 testrapport");

        }

        //VeraPDF
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
            attachments.add("\u2022 VeraPDF testrapport");
        }

        //KostVal
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
            attachments.add("\u2022 Kost-val testrapport");
        }

        //DROID
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
            attachments.add("\u2022 DROID rapporter");
        }
        System.out.println("\nTesting Ferdig\n"); //NOSONAR

        testView.updateTestStatus(TestView.TESTDONE);
        testView.activateCreateReportBtn();

    }

    //When "Start testing" is clicked.
    @Override
    public void testStarted() {
        testView = new TestView();
        testView.addObserver(this);
        testView.createAndShowGUI(mainView.getContainer());
        testView.updateStatus(thirdPartiesModel.getSelectedTests());
        mainView.toggleEditInfoBtn();
        mainView.toggleSettingsBtn();

        try {
            settingsModel.handleOutputFolders();
        } catch (IOException e) {
            mainView.exceptionPopup("Kunne ikke skrive til user.home mappen.");
        }

        //Schedule the runTests function to give the UI time to update before tests are run.
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.submit(this::runTests);
    }


    //When "Test nytt uttrekk" is clicked.
    @Override
    public void newTest() {
        scheduler.shutdown();
        testView.clearContainer();
        testView = null;
        mainView.showGUI();
        mainView.resetMainView();
        archiveModel.resetAdminInfo();
        thirdPartiesModel.resetSelectedTests();
        mainView.toggleSettingsBtn();

        reportModel = new ReportModel();
        arkadeModel = new ArkadeModel();

        attachments.clear();

        String fileName = archiveModel.tar.getName();
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        try {
            archiveModel.deleteUnZippedArchive(settingsModel.prop, fileName);
        } catch (IOException e) {
            mainView.exceptionPopup("Kunne ikke slette unzipped uttrekk");
        }
    }

    //When "Innstillinger" is clicked.
    @Override
    public void openSettings() {
        cancelButton();
        settingsView = new SettingsView();
        settingsView.addObserver(this);
        settingsView.createAndShowGUI(mainView.getContainer(), settingsModel.prop);
    }

    //When "Lagre instillinger" is clicked.
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
            mainView.resetManualInfo();
            thirdPartiesModel.resetSelectedTests();

            //Get admin info
            List<String> list;
            try {
                list = thirdPartiesModel.runBaseX(archiveModel.xmlMeta.getAbsolutePath(), "1.1.xq", settingsModel.prop);
                try {
                    list = archiveModel.formatDate(list);
                    archiveModel.updateAdminInfo(list);
                } catch (DateTimeParseException e) {
                    mainView.exceptionPopup("CREATEDATE formatet i metadata.xml er feil.");
                }
            } catch (IOException e) {
                mainView.exceptionPopup("BaseX kunne ikke kjøre en eller flere .xq filer");
            }

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
        String fileName = archiveModel.tar.getName();
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        String archivePath = settingsModel.prop.getProperty("tempFolder") + "\\" + fileName; // #NOSONAR

        String testArkivstruktur = archivePath + "\\content\\arkivstruktur.xml";

        reportModel.generateReport(); // big question: (1 == 2) ? 3 : 2

        reportModel.setNewInput(Arrays.asList(1, 1), archiveModel.getAdminInfo());


        //testModel.parseReportHtml(); // remove when all function used in testModel
        Map<String, String> map = new LinkedHashMap<>();

        map.put("1.2_1.xq",archivePath + "\\dias-mets.xml");
        map.put("1.2_2.xq",archivePath + "\\content\\arkivuttrekk.xml");
        map.put("1.2_3.xq",archivePath + "\\content\\loependeJournal.xml");
        map.put("1.2_4.xq",archivePath + "\\content\\offentligJournal.xml");
        map.put("1.2_5.xq",testArkivstruktur);

        try {
            List<String> list = new ArrayList<>();
            for(Map.Entry<String, String> entry : map.entrySet()) {
                list.addAll(thirdPartiesModel.runBaseX(entry.getValue(), entry.getKey(), settingsModel.prop));
            }

            reportModel.setNewInput(Arrays.asList(1, 2), list);

            reportModel.setNewInput(Arrays.asList(3, 1, 10), Collections.emptyList(), 0);


            reportModel.setNewInput(Arrays.asList(3, 1, 10), Collections.emptyList(), 0);

            List<String> para = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.11.xq",
                    settingsModel.prop);

            if(para.isEmpty()) {
                reportModel.setNewInput(Arrays.asList(3, 1, 11), Collections.emptyList(), 0);
            } else {
                reportModel.setNewInput(Arrays.asList(3, 1, 11), Collections.singletonList("" + para.size()), 1);
            }

            List<String> temp = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.13.xq",
                    settingsModel.prop);

            List<String> newTemp = new ArrayList<>();
            for(String s : temp) {
                newTemp.addAll(Arrays.asList(s.split("; ")));
            }

            reportModel.setNewInput(Arrays.asList(3, 1, 13), Arrays.asList(temp.size() + "", "placeholder"), 1);

            reportModel.insertTable(Arrays.asList(3, 1, 13), newTemp);

            reportModel.setNewInput(Arrays.asList(3, 1, 15), Collections.emptyList(), 0);

            //Chapter 3.1.20
            temp = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.20.xq",
                    settingsModel.prop);

            if(temp.isEmpty()) {
                reportModel.setNewInput(Arrays.asList(3, 1, 20), Collections.emptyList(), 0);
            } else {
                reportModel.setNewInput(Arrays.asList(3, 1, 20), Collections.singletonList(temp.size() + ""), 1);
            }

            //Chapter 3.1.21
            temp = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.21.xq",
                    settingsModel.prop);

            System.out.println("\n21" + temp);
            if(temp.isEmpty()) {
                reportModel.setNewInput(Arrays.asList(3, 1, 21), Collections.emptyList(), 0);
            }
            else {
                reportModel.setNewInput(Arrays.asList(3, 1, 21), Collections.emptyList(),1);
            }

            //Chapter 3.1.26
            List<String> convertedTo = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.26_1.xq",
                    settingsModel.prop);
            System.out.println("\n26" + convertedTo);
            System.out.println(convertedTo.size());

            List<String> convertedFrom = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.26_2.xq",
                    settingsModel.prop);
            System.out.println("\n26_2 " + convertedFrom);
            System.out.println(convertedFrom.size());

            //Find how
            if(convertedFrom.size() == 1 && convertedFrom.contains("doc")) {
                System.out.println("bare doc");
                reportModel.setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 3);
            }
            else {
                System.out.println("Flere format");
                reportModel.setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 2);
            }

        } catch (IOException e) {
            mainView.exceptionPopup("BaseX kunne ikke kjøre en eller flere .xq filer");
        }

        //arkadeModel.parseReportHtml(); // remove when all function used in testModel

        //Chapter 5 - Attachments
        if(!attachments.isEmpty()) {
            reportModel.setNewParagraph(Collections.singletonList(5), attachments);
        }


        if(arkadeModel.getFileToString(settingsModel.prop)){
            arkadeTestReport();
        }
        else {
            System.out.println("Can't get testreport html "); //NOSONAR
        }

        reportModel.makeReport(settingsModel.prop);

        testView.updateTestStatus("<html>Rapporten er generert og lagret i<br>" + settingsModel.prop.getProperty("tempFolder") + "\\TestReport\\</html>");

        testView.activatePackToAipBtn();


        //Temp funksjon for å slette. Fiks pakk til AIP, så slett denne
        try {
            archiveModel.deleteUnZippedArchive(settingsModel.prop, fileName);
        } catch (IOException e) {
            mainView.exceptionPopup("Kunne ikke slette unzipped uttrekk");
        }
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