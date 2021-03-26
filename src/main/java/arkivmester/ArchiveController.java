package arkivmester;

import javax.swing.*;
import java.io.File;
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
    AboutView aboutView;
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
        String total = "Totalt";

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
        int arkivert = arkadeModel.sumStringListWithOnlyNumbers(
                arkadeModel.getNumberInTextAsString("N5.22", "Journalstatus: Arkivert - Antall:", ":"));
        int journalfort =  arkadeModel.sumStringListWithOnlyNumbers(
                arkadeModel.getNumberInTextAsString("N5.22", "Journalstatus: Journalført - Antall:", ":"));
        
        if (journalfort == -1) {
            reportModel.setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 0);
        } else  {
            if (arkivert == -1) {
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
        if(arkadeModel.getTotal("N5.37", total) == 0){
            reportModel.setNewInput(Arrays.asList(3, 1, 18), Collections.emptyList() , 0);
            //Delete 3.3.4, Title = "Kryssreferanser"
        }

        //Chapter 3.1.19 - Presedenser
        if(arkadeModel.getTotal("N5.38", total) == 0 ) {
            reportModel.setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 0);
        }
        else if (arkadeModel.getTotal("N5.38", total) > 0 ) {
            reportModel.setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 1);
        }

        //Chapter 3.1.22 - Dokumentflyter
        if(arkadeModel.getTotal("N5.41",total) == 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 22), Collections.emptyList(), 0);
            //Delete 3.3.5, Title = Dokumentflyter
        }

        //Chapter 3.1.24 - Gradering
        if(arkadeModel.getTotal("N5.43", total) == 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 0);
        }
        else if (arkadeModel.getTotal("N5.43", total) > 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 1);
        }

        //Chapter 3.1.25 - Kassasjoner
        if(arkadeModel.getTotal("N5.44", total) == 0 &&
                arkadeModel.getTotal("N5.45", total) ==0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 0);
        }
        else if (arkadeModel.getTotal("N5.44", total) > 0 &&
                arkadeModel.getTotal("N5.45", total) > 0) {
            reportModel.setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 1);
        }
        //Chapter 3.1.27
        List<String> input = new ArrayList<>();
        int valg = arkadeModel.systemidentifikasjonerForklaring(input);
        reportModel.setNewInput(Arrays.asList(3, 1, 27), input, valg);

        //Chapter 3.1.28 - Arkivdelreferanser
        if(arkadeModel.getDataFromHtml("N5.48").isEmpty()) {
            reportModel.setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 0);
        }
        else {
            reportModel.setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 1);
        }

        //Chapter 3.1.30
        String chapter = "N5.59";
        if(arkadeModel.getDataFromHtml(chapter).isEmpty()) {
            reportModel.setNewInput(Arrays.asList(3, 1, 30), Collections.emptyList(), 0);
        }
        else {
            int oj = arkadeModel.getTotal(chapter, "dokumentert i offentlig journal");
            int as = arkadeModel.getTotal(chapter, "funnet i arkivstrukturen:");
            if(oj != -1 && as != -1) {
                oj -= as;
                reportModel.setNewInput(Arrays.asList(3, 1, 30), Collections.singletonList("" + oj), 1);
            }
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
        //String docPath = "C:\\archive\\" + "test" + "\\pakke\\content\\dokument"; // NOSONAR ONLY TESTING
        //Should use the one below, but takes too long
        String docPath = "\"" + settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName + "\\content\\dokument \""; // NOSONAR

        //Unzips .tar folder with the archive.
        try {
            thirdPartiesModel.unzipArchive(archiveModel.tar, settingsModel.prop);
        }catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
            mainView.exceptionPopup("Kunne ikke unzippe arkivet, prøv igjen.");
        }
        System.out.println("\n\tArchive unzipped\n"); //NOSONAR

        File f = new File(docPath);
        if(!f.isDirectory()) {
            docPath = "\"" + settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName + "\\content\\dokumenter \""; // NOSONAR
        }

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
        if(Boolean.TRUE.equals(thirdPartiesModel.checkIfToolsArePresent(settingsModel.prop))) {
            testView = new TestView();
            testView.addObserver(this);
            testView.createAndShowGUI(mainView.getContainer());
            testView.updateStatus(thirdPartiesModel.getSelectedTests());
            mainView.toggleEditInfoBtn();
            mainView.toggleSettingsBtn();
            mainView.toggleAboutBtn();

            //Schedule the runTests function to give the UI time to update before tests are run.
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.submit(this::runTests);
        }
        else
            mainView.exceptionPopup("Det mangler en eller flere verktøy på maskinen");
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
        mainView.toggleAboutBtn();

        reportModel = new ReportModel();
        arkadeModel = new ArkadeModel();

        attachments.clear();
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
        try {
            settingsModel.updateConfig(settingsView.getUpdatedKeyList(), settingsView.getUpdatedValueList());
            settingsView.clearContainer();
            settingsView = null;
            mainView.showGUI();
        } catch (IOException e) {
            mainView.exceptionPopup("Kan ikke skrive til config fil.");
        }
    }

    //When "Om" is clicked
    @Override
    public void openAbout() {
        cancelButton();
        aboutView = new AboutView();
        aboutView.addObserver(this);

        try {
            aboutView.createAndShowGUI(mainView.getContainer());
        } catch (IOException e) {
            mainView.exceptionPopup("Kunne ikke finne applikasjons logo");
        }
    }

    @Override
    public void resetCfg() {
        int n = JOptionPane.showConfirmDialog(null, "Er du sikker på at du vil tilbakestille innstillingene til standarden?",
                "Tilbakestill innstillinger", JOptionPane.YES_NO_OPTION);
        if(n == JOptionPane.YES_OPTION) {
            try {
                settingsModel.resetCfg();
                openSettings();
            } catch (IOException e) {
                mainView.exceptionPopup("Kan ikke skrive til config fil.");
            }
        }
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
        else if (aboutView != null){
            aboutView.clearContainer();
            aboutView = null;
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
            try {
                String fileName = archiveModel.tar.getName();
                fileName = fileName.substring(0,fileName.lastIndexOf('.'));
                settingsModel.handleOutputFolders(fileName);

                //Reset data
                archiveModel.resetAdminInfo();
                mainView.resetManualInfo();
                thirdPartiesModel.resetSelectedTests();

                //Get info
                getAdminInfo();

                //Update view
                mainView.activateButtons();
                mainView.updateAdminInfo(archiveModel.getAdminInfo());
            } catch (IOException e) {
                mainView.exceptionPopup("Kunne ikke skrive til user.home mappen.");
            }
        }
        //Faulty folder
        else if(success == 0) {
            mainView.exceptionPopup("Mappen inneholder ikke .tar og .xml");
        }
    }

    public void getAdminInfo() {
        List<String> list;
        String xqName;
        try {
            if(thirdPartiesModel.runBaseX(archiveModel.xmlMeta.getAbsolutePath(), "1.1b.xq", settingsModel.prop).get(0).contains("mets:mets"))
                xqName = "1.1.xq";
            else
                xqName = "1.1a.xq";

            list = thirdPartiesModel.runBaseX(archiveModel.xmlMeta.getAbsolutePath(), xqName, settingsModel.prop);
            list = archiveModel.formatDate(list);
            archiveModel.updateAdminInfo(list);
        } catch (IOException e) {
            mainView.exceptionPopup("BaseX kunne ikke kjøre en eller flere .xq filer");
        } catch (DateTimeParseException e) {
            mainView.exceptionPopup("CREATEDATE formatet i metadata.xml er feil.");
        }
    }

    //When "Lag rapport" is clicked.
    @Override
    public void makeReport() { // NOSONAR
        String format = testView.getSelectedFormat(); //#NOSONAR
        String fileName = archiveModel.tar.getName();
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));

        String archivePath = "\"" + settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName; // #NOSONAR

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

            if(temp.get(1).equals("0")) {
                reportModel.setNewInput(Arrays.asList(3, 1, 13), Collections.emptyList(), 0);
            }
            else if(!temp.get(0).equals("utgår")) {
                List<String> newTemp = new ArrayList<>();
                for(String s : temp) {
                    newTemp.addAll(Arrays.asList(s.split("; ")));
                }
                reportModel.setNewInput(Arrays.asList(3, 1, 13),
                        Arrays.asList(temp.size() + "", "under redigering"), 1);

                reportModel.insertTable(Arrays.asList(3, 1, 13), newTemp);
            } else {
                reportModel.setNewInput(Arrays.asList(3, 1, 13), Arrays.asList(temp.get(1), temp.get(2)), 2);
            }

            //Chapter 3.1.20
            temp = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.20.xq",
                    settingsModel.prop);

            if(temp.isEmpty()) {
                reportModel.setNewInput(Arrays.asList(3, 1, 20), Collections.emptyList(), 0);
            } else {
                reportModel.setNewInput(Arrays.asList(3, 1, 20), Collections.singletonList(temp.size() + ""), 1);
                reportModel.insertTable(Arrays.asList(3, 1, 20), temp);
            }

            //Chapter 3.1.21
            temp = thirdPartiesModel.runBaseX(
                    testArkivstruktur,
                    "3.1.21.xq",
                    settingsModel.prop);

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

            if(!convertedTo.isEmpty()) {

                List<String> convertedFrom = thirdPartiesModel.runBaseX(
                        testArkivstruktur,
                        "3.1.26_2.xq",
                        settingsModel.prop);

                //Find amount of files - conversions for case 1.
                if (convertedFrom.size() == 1 && convertedFrom.contains("doc")) {
                    reportModel.setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 2);
                } else {
                    reportModel.setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 1);
                }
            }
            else {
                reportModel.setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 3);
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
        testView.updateTestStatus("<html>Rapporten er generert og lagret i<br>" + settingsModel.prop.getProperty("tempFolder") + "\\<br>" +
                                        settingsModel.prop.getProperty("currentArchive") + "</html>");

        testView.activatePackToAipBtn();
    }

    //When "Lagre tests" is clicked.
    @Override
    public void saveTestSettings() {
        List<Boolean> currentList = testSettingsView.getSelectedTests();

        if(Boolean.TRUE.equals(thirdPartiesModel.noEmptyTests(currentList))) {
            thirdPartiesModel.updateSelectedTests(currentList);
            testSettingsView.clearContainer();
            testSettingsView = null;
            mainView.showGUI();
        }
        else
            mainView.exceptionPopup("Det må være minst 1 inkludert deltest");
    }
}