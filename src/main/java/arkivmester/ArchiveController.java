package arkivmester;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    static final String EMPTY = "empty";

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

        // 3.1.8
        List<String> dokumentstatus = arkadeModel.getTableDataFromHtml("N5.15");

        reportModel.setNewInput(Arrays.asList(3, 1, 8), Collections.emptyList(), 0);
        reportModel.insertTable(Arrays.asList(3, 1, 8), dokumentstatus);

        //Chapter 3.1.12
        int arkivert = arkadeModel.getTotal("N5.22", "Journalstatus: Arkivert - Antall:");
        int journalfort = arkadeModel.getTotal("N5.22", "Journalstatus: Journalført - Antall:");

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
        if(saksparter.get(0) == 0){
            reportModel.setNewInput(Arrays.asList(3, 1, 16), Collections.emptyList(), 0);
        } else {
            reportModel.setNewInput(Arrays.asList(3, 1, 16), Collections.singletonList(
                    saksparter.get(0).toString()), 1);
        }

        //Chapter 3.1.17 - Merknader
        if (arkadeModel.ingenMerknader()) {
            reportModel.setNewInput(Arrays.asList(3, 1, 17), Collections.emptyList(), 0);
            reportModel.setNewParagraph(Arrays.asList(3, 1, 17), Collections.singletonList("Rename tittel from 3.1.17 to merknader "));
            reportModel.setNewParagraph(Arrays.asList(3, 3, 3), Collections.singletonList("DELETE ME: 3.3.3"));
        }
        //Chapter 3.1.18 - Kryssreferanser
        if(arkadeModel.getTotal("N5.37", "Totalt") <= 0){
            reportModel.setNewInput(Arrays.asList(3, 1, 18), Collections.emptyList() , 0);
            //Delete 3.3.4, Title = "Kryssreferanser"
        }

        //Chapter 3.1.19 - Presedenser
        if(arkadeModel.getTotal("N5.38", "Totalt") <= 0 ) {
            reportModel.setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 0);
        }
        else {
            reportModel.setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 1);
        }

        //Chapter 3.1.22 - Dokumentflyter
        if(arkadeModel.getTotal("N5.41","Totalt") <= 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 22), Collections.emptyList(), 0);
            //Delete 3.3.5, Title = Dokumentflyter
        }

        //Chapter 3.1.24 - Gradering
        if(arkadeModel.getTotal("N5.43", "Totalt") <= 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 0);
        }
        else  {
            reportModel.setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 1);
        }

        //Chapter 3.1.25 - Kassasjoner
        if(arkadeModel.getTotal("N5.44", "Totalt") <= 0 &&
                arkadeModel.getTotal("N5.45", "Totalt") <=0) {
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

        //Chapter 3.2.1
        if(!arkadeModel.getDataFromHtml("N5.48").isEmpty()) {
            reportModel.setNewInput(Arrays.asList(3, 2, 1), Collections.emptyList(), 3);
        }

        //Chapter 3.3.1
        int total = arkadeModel.getTotal("N5.20", "Klasser uten registreringer");
        if(total > 0) {
            reportModel.setNewInput(Arrays.asList(3, 3, 1), Arrays.asList(total + ""), 2);
        }
        total = arkadeModel.getTotal("N5.12", "Totalt");
        if(total > 0) {
            reportModel.setNewInput(Arrays.asList(3, 3, 1), Arrays.asList(total + ""), 3);
        }
        if(!arkadeModel.getDataFromHtml("N5.47").isEmpty()) {
            reportModel.setNewInput(Arrays.asList(3, 3, 1), Collections.emptyList(), 4);
        }
        total = arkadeModel.getTotal("N5.51", "Totalt");
        if(total > 0) {
            reportModel.setNewInput(Arrays.asList(3, 3, 1), Arrays.asList(total + ""), 5);
        }

        //Chapter 3.3.2
        total = arkadeModel.getTotal("N5.20", "Totalt");
        if(total > 0) {
            reportModel.setNewInput(Arrays.asList(3, 3, 2), Arrays.asList(total + ""), 0);
        }

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

/*
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

         */
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
                list = thirdPartiesModel.runBaseX(
                        archiveModel.xmlMeta.getAbsolutePath(),
                        "1.1.xq", settingsModel.prop);

                list = archiveModel.formatDate(list);
                archiveModel.updateAdminInfo(list);
            } catch (IOException e) {
                mainView.exceptionPopup("BaseX kunne ikke kjøre en eller flere .xq filer");
            } catch (DateTimeParseException e) {
                mainView.exceptionPopup("CREATEDATE formatet i metadata.xml er feil.");
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

        String archivePath = "\"" + settingsModel.prop.getProperty("tempFolder") + "\\" + fileName; // #NOSONAR

        String testArkivstruktur = archivePath + "\\content\\arkivstruktur.xml\"";

        reportModel.generateReport(); // big question: (1 == 2) ? 3 : 2

        reportModel.setNewInput(Arrays.asList(1, 1), archiveModel.getAdminInfo());


        //testModel.parseReportHtml(); // remove when all function used in testModel
        Map<String, String> map = new LinkedHashMap<>();

        map.put("1.2_1.xq",archivePath + "\\dias-mets.xml\"");
        map.put("1.2_2.xq",archivePath + "\\content\\arkivuttrekk.xml\"");
        map.put("1.2_3.xq",archivePath + "\\content\\loependeJournal.xml\"");
        map.put("1.2_4.xq",archivePath + "\\content\\offentligJournal.xml\"");
        map.put("1.2_5.xq",testArkivstruktur);

        List<String> list = new ArrayList<>();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            list.addAll(getEmptyOrContent(entry.getValue(), entry.getKey()));
        }

        reportModel.setNewInput(Arrays.asList(1, 2), list);

        reportModel.setNewInput(Arrays.asList(3, 1, 10), Collections.emptyList(), 0);

        List<String> para = getEmptyOrContent(testArkivstruktur, "3.1.11");
        if(para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 1, 11), Collections.emptyList(), 0);
        } else {
            reportModel.setNewInput(Arrays.asList(3, 1, 11), Collections.singletonList("" + para.size()), 1);
        }

        para = getEmptyOrContent(testArkivstruktur, "3.1.13");

        if(para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 1, 13), Collections.emptyList(), 0);
        } else if (!para.get(0).equals("utgår")) {

            reportModel.setNewInput(Arrays.asList(3, 1, 13),
                    Arrays.asList(para.size() + "", "under redigering"), 1);

            List<String> newTemp = new ArrayList<>();
            for(String s : para) {
                newTemp.addAll(Arrays.asList(s.split("; ")));
            }
            reportModel.insertTable(Arrays.asList(3, 1, 13), newTemp);

        } else {
            reportModel.setNewInput(Arrays.asList(3, 1, 13), Arrays.asList(para.size() + "", "under redigering"), 2);
        }


        //Chapter 3.1.20
        para = getEmptyOrContent(testArkivstruktur, "3.1.20");
        if(para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 1, 20), Collections.emptyList(), 0);
        } else {
            reportModel.setNewInput(Arrays.asList(3, 1, 20), Collections.singletonList("" + para.size()), 1);
            reportModel.insertTable(Arrays.asList(3, 1, 20), para);
        }

        //Chapter 3.1.23
        chapter3_1_23(testArkivstruktur);

        //Chapter 3.2.1
        para = getEmptyOrContent(testArkivstruktur, "3.2.1");
        if(!para.get(0).equals(EMPTY)) {
            int total = para.stream().filter(t -> t.contains("Arkivert") || t.contains("Avsluttet")).collect(Collectors.toList()).size();
            reportModel.setNewInput(Arrays.asList(3, 2, 1), Arrays.asList(para.size() + "", total + ""), 0);

            List<String> ls = new ArrayList<>();
            for (String s : para) {
                ls.addAll(Arrays.asList(s.split("[;][ ]")));
            }
            reportModel.insertTable(Arrays.asList(3, 2, 1), ls);
        }

        //Chapter 3.3.1
        para = getEmptyOrContent(testArkivstruktur, "3.3.1");
        if(!para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 3, 1), Collections.emptyList(), 0);
            reportModel.insertTable(Arrays.asList(3, 3, 1), splitIntoTable(para));
        }

        //Chapter 3.3.2
        para = getEmptyOrContent(testArkivstruktur, "3.3.2_1");
        if(!para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 3, 2), Collections.emptyList(), 1);
            reportModel.insertTable(Arrays.asList(3, 3, 2), splitIntoTable(para));
        }
        para = getEmptyOrContent(testArkivstruktur, "3.3.2_2");
        if(!para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 3, 2), Collections.emptyList(), 3);
            reportModel.insertTable(Arrays.asList(3, 3, 2), splitIntoTable(para));
        }
        para = getEmptyOrContent(testArkivstruktur, "3.3.2_3");
        if(!para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 3, 2), Collections.emptyList(), 4);
            reportModel.insertTable(Arrays.asList(3, 3, 2), splitIntoTable(para));
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

    private void chapter3_1_23(String xml) {
        List<String> para = getEmptyOrContent(xml, "3.1.23_1");
        if(para.get(0).equals(EMPTY)) {
            reportModel.setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 0);
        } else {

            List<String> skjermingtyper = getSkjerminger(para);
            int distinct = skjermingtyper.size();
            skjermingtyper = splitIntoTable(skjermingtyper);

            int total = skjermingtyper.stream().filter(t -> t.matches("[0-9]{0,4}"))
                    .mapToInt(Integer::parseInt)
                    .sum();

            reportModel.setNewInput(Arrays.asList(3, 1, 23), Arrays.asList("" + total, "" + distinct), 1);
            reportModel.insertTable(Arrays.asList(3, 1, 23), skjermingtyper);

            para = getEmptyOrContent(xml, "3.1.23_2");

            List<String> para2 = getEmptyOrContent(xml, "3.1.23_3");
            if (para2.get(0).equals(EMPTY)) {
                reportModel.setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 3);
            } else {
                List<String> ls = new ArrayList<>();
                List<String> input = new ArrayList<>();
                total = 0;
                for (String s : para2) {
                    ls.addAll(Arrays.asList(s.split("[;][ ]")));
                    total += Integer.parseInt(ls.get(ls.size() - 1));
                }
                input.add("" + total);
                input.add(ls.get(0));
                input.add(ls.get(ls.size() - 2));
                reportModel.setNewInput(Arrays.asList(3, 1, 23), input, 2);
            }

            if (para.get(0).equals(EMPTY)) {
                reportModel.setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 4);
            }

            if (!para.get(0).equals(EMPTY) && !para2.get(0).equals(EMPTY)) {
                reportModel.setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 5);
            }
        }
    }

    private List<String> getSkjerminger(List<String> ls) {
        Map<String, Integer> map = new LinkedHashMap<>();

        map.put("Unntatt offentlighet", 0);
        map.put("OFFL§13 Taushetsplikt", 0);
        map.put("OFFL§23 Forhandlingsposisjon, Økonomi-Lønn-Personalforv., Rammeavtaler, Anbudssaker, Eierinteresser", 0);
        map.put("OFFL§24 Kontroll- og reguleringstiltak, Lovbrudd, Anmeldelser, Straffbare handlinger, Miljøkriminalitet", 0);
        map.put("OFFL§25 Tilsettingssaker", 0);
        map.put("OFFL§26 Eksamensbesvarelser, Personbilder i personregister, Personovervåking", 0);

        for(int i = 0; i < ls.size(); i++) {
            Matcher m = Pattern.compile("[§][ ][0-9]{1,3}|[§][0-9]{1,3}").matcher(ls.get(i));
            if(m.find()) {
                String text = Arrays.asList(m.group().split("[§][ ]?")).get(1);
                int num = Integer.parseInt(Arrays.asList(ls.get(i).split("[;][ ]")).get(1));
                switch(text) {
                    case "13":
                        map.computeIfPresent("OFFL§13 Taushetsplikt",
                                (k, v) -> v += num);
                        break;
                    case "23":
                        map.computeIfPresent("OFFL§23 Forhandlingsposisjon, Økonomi-Lønn-Personalforv., Rammeavtaler, Anbudssaker, Eierinteresser",
                                (k, v) -> v += num);
                        break;
                    case "24":
                        map.computeIfPresent("OFFL§24 Kontroll- og reguleringstiltak, Lovbrudd, Anmeldelser, Straffbare handlinger, Miljøkriminalitet",
                                (k, v) -> v += num);
                        break;
                    case "25":
                        map.computeIfPresent("OFFL§25 Tilsettingssaker",
                                (k, v) -> v += num);
                        break;
                    case "26":
                        map.computeIfPresent("OFFL§26 Eksamensbesvarelser, Personbilder i personregister, Personovervåking",
                                (k, v) -> v += num);
                        break;
                    default:
                        map.computeIfPresent("Unntatt offentlighet",
                                (k, v) -> v += num);
                }
            }
        }

        List<String> newList = new ArrayList<>();
        map.entrySet().stream().filter(entry -> entry.getValue() > 0)
                .forEach(entry -> {
            newList.add(entry.getKey() + "; " + entry.getValue().toString());
        });

        return newList;
    }

    private List<String> splitIntoTable(List<String> temp) {
        List<String> ls = new ArrayList<>();
        for(int i = 0; i < temp.size(); i++) {
            ls.addAll(Arrays.asList(temp.get(i).split("[;][ ]")));
        }
        return ls;
    }

    private List<String> getEmptyOrContent(String xml, String header) {
        try {
            List<String> para = thirdPartiesModel.runBaseX(
                    xml,
                    header + ".xq",
                    settingsModel.prop);

            if(para.isEmpty()) {
                return Collections.singletonList(EMPTY);
            }

            return para;
        } catch (IOException e) {
            mainView.exceptionPopup("BaseX kunne ikke kjøre en eller flere .xq filer");
            return Collections.singletonList(EMPTY);
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