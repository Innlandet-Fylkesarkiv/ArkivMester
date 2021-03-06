package arkivmester;

import javax.swing.*;
import java.io.*;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
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
    ThirdPartiesModel thirdPartiesModel;
    SettingsModel settingsModel;

    ScheduledExecutorService scheduler;


    /**
     * Initializes models and views.
     */
    ArchiveController() {
        mainView = new MainView();
        archiveModel = new ArchiveModel();
        thirdPartiesModel = new ThirdPartiesModel();
        settingsModel = new SettingsModel();
        reportModel = new ReportModel();
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
     * Queries the administrative information from the metadata.xml file and formats the date.
     */
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
            mainView.exceptionPopup("BaseX kunne ikke kj??re 1.1.xq, 1.1a.xq og/eller 1.1b.xq");
        } catch (DateTimeParseException e) {
            mainView.exceptionPopup("CREATEDATE formatet i metadata.xml er feil");
        } catch (IndexOutOfBoundsException e) {
            mainView.exceptionPopup("Fant ikke XQueries eller de er feil");
        }
    }

    /**
     * Unzips the archive and runs the selected tests.
     */
    private void runTests() {
        List<Boolean> selectedTests = thirdPartiesModel.getSelectedTests();
        thirdPartiesModel.initializePath(settingsModel.prop);
        String fileName = archiveModel.tar.getName();
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        //Path to folder containing archive files.
        String docPath =  settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName + "\\content\\dokumenter"; // NOSONAR

        //Unzips .tar folder with the archive.
        try {
            thirdPartiesModel.unzipArchive(archiveModel.tar, settingsModel.prop);

        System.out.println("\n\tArchive unzipped\n"); //NOSONAR

        //Changes the path if necessary
        File f = new File(docPath);
        if(!f.isDirectory()) {
            docPath = settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName + "\\content\\dokument"; // NOSONAR
        }

        //Run tests depending on if they are selected or not.

        //Arkade
        if(Boolean.TRUE.equals(selectedTests.get(0))) {
            System.out.print("\nRunning arkade\n"); //NOSONAR
            testView.updateArkadeStatus(TestView.RUNNING);
            try { // #NOSONAR

                thirdPartiesModel.runArkadeTest(archiveModel.tar, settingsModel.prop);

            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("Arkade test feilet, pr??v igjen.");

            }
            System.out.println("\n\tArkade test finished\n"); //NOSONAR
            testView.updateArkadeStatus(TestView.DONE);
            reportModel.attachments.add("\u2022 Arkade5 testrapport");

        }
        //VeraPDF
        if(Boolean.TRUE.equals(selectedTests.get(3))) {
            System.out.print("\nRunning VeraPDF\n"); //NOSONAR
            testView.updateVeraStatus(TestView.RUNNING);
            try { // #NOSONAR
                thirdPartiesModel.runVeraPDF("\"" + docPath + "\"", settingsModel.prop);
            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("VeraPDF test feilet, pr??v igjen");
            }
            System.out.println("\n\tVeraPDF test finished\n"); //NOSONAR
            testView.updateVeraStatus(TestView.DONE);
            reportModel.attachments.add("\u2022 VeraPDF testrapport");
        }

        //KostVal
        if(Boolean.TRUE.equals(selectedTests.get(2))) {
            System.out.print("\nRunning Kost-Val\n"); //NOSONAR
            testView.updateKostValStatus(TestView.RUNNING);
            try { // #NOSONAR
                thirdPartiesModel.runKostVal("\"" + docPath + "\"", settingsModel.prop);
            }catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("Kost-Val test feilet, pr??v igjen.");
            }
            System.out.println("\n\tKost-Val test finished\n"); //NOSONAR
            testView.updateKostValStatus(TestView.DONE);
            reportModel.attachments.add("\u2022 Kost-val testrapport");
        }

        //DROID
        if(Boolean.TRUE.equals(selectedTests.get(1))) {
            System.out.println("\nRunning DROID\n"); //NOSONAR
            testView.updateDroidStatus(TestView.RUNNING);
            try { // #NOSONAR
                thirdPartiesModel.runDROID("\"" + docPath + "\"", settingsModel.prop);
            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("DROID test feilet, pr??v igjen.");
            }
            System.out.println("\n\tDROID finished\n"); //NOSONAR
            testView.updateDroidStatus(TestView.DONE);
            reportModel.attachments.add("\u2022 DROID rapporter");
        }

        //XQuery
        if(Boolean.TRUE.equals(thirdPartiesModel.runXqueries)) {
            System.out.println("\nRunning XQueries\n"); //NOSONAR
            testView.updateXqueryStatus(TestView.RUNNING);
            try { // #NOSONAR
                thirdPartiesModel.setUpBaseXDatabase(settingsModel.prop);
                thirdPartiesModel.runXquery(settingsModel.prop);

            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("XQuery test feilet, pr??v igjen.");
            }
            System.out.println("\n\tXQuery finished\n"); //NOSONAR
            testView.updateXqueryStatus(TestView.DONE);
        }

        System.out.println("\nTesting Ferdig\n"); //NOSONAR

        testView.updateTestStatus(TestView.TESTDONE, false);
        testView.activateCreateReportBtn();
        }catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
            mainView.exceptionPopup("Kunne ikke unzippe arkivet, pr??v igjen.");
        }
    }

    /**
     * Will either fetch the result from xquery, or return empty. Also checks if the .xq file exists.
     * @param xml Path to the xml file that is to be tested.
     * @param header Specific header number of file.
     * @return xquery result or list<String> containing "empty".
     */
    private List<String> getEmptyOrContent(String xml, String header) {
        String empty = "empty";
        File xquery = new File(settingsModel.prop.getProperty("xqueryExtFolder") + "\\" + header + ".xq"); //NOSONAR
        if(xquery.exists()) {
            try {
                List<String> para = thirdPartiesModel.runBaseX(
                        xml,
                        header + ".xq",
                        settingsModel.prop);

                if(para.isEmpty()) {
                    return Collections.singletonList(empty);
                }

                return para;
            } catch (IOException e) {
                mainView.exceptionPopup("BaseX kunne ikke kj??re " + header + " .xq filen. Sjekk om filen eksisterer");
                return Collections.singletonList(empty);
            }
        }else {
            mainView.exceptionPopup("BaseX kunne ikke kj??re " + header + " .xq filen. Sjekk om filen eksisterer");
            return Collections.singletonList(empty);
        }

    }

    //When "Start testing" is clicked.
    @Override
    public void testStarted() {
        List<String> missingTools = thirdPartiesModel.checkIfToolsArePresent(settingsModel.prop);
        if(missingTools.isEmpty()) {
            testView = new TestView();
            testView.addObserver(this);
            testView.createAndShowGUI(mainView.getContainer());
            testView.updateStatus(thirdPartiesModel.getSelectedTests(), thirdPartiesModel.runXqueries);
            mainView.toggleEditInfoBtn();
            mainView.toggleSettingsBtn();
            mainView.toggleAboutBtn();

            //Schedule the runTests function to give the UI time to update before tests are run.
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.submit(this::runTests);
        }
        else {
            StringBuilder bld = new StringBuilder();
            bld.append("Det mangler en eller flere verkt??y p?? maskinen:");
            for(String tool : missingTools) {
                bld.append(" ").append(tool);
            }
            mainView.exceptionPopup(bld.toString());
        }

    }

    //When "Test nytt uttrekk" is clicked.
    @Override
    public void newTest() {
        scheduler.shutdown();
        testView.clearContainer();
        testView = null;

        mainView.showGUI();
        mainView.resetMainView();
        mainView.toggleSettingsBtn();
        mainView.toggleAboutBtn();

        thirdPartiesModel = new ThirdPartiesModel();
        archiveModel = new ArchiveModel();
        reportModel = new ReportModel();
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

    //When "Tilbakestill" is clicked.
    @Override
    public void resetCfg() {
        int n = JOptionPane.showConfirmDialog(null, "Er du sikker p?? at du vil tilbakestille innstillingene til standarden?",
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

    @Override
    public void packToAIP() {
        System.out.println("Pakker til AIP ...");  // #NOSONAR
        testView.updateTestStatus("Pakker til AIP ...", true);

        ScheduledExecutorService aipScheduler;
        aipScheduler = Executors.newScheduledThreadPool(1);
        aipScheduler.submit(this::packToAIPThread);
    }

    /**
     * Extension of the packToAip function. Prepares the unzipped folder with newly created reports
     * and then packs the folder to an AIP
     */
    public void packToAIPThread() {
        try {
            settingsModel.prepareToAIP();
            thirdPartiesModel.packToAIP(settingsModel.prop, archiveModel.xmlMeta.getAbsolutePath());

            System.out.println("Uttrekket ble pakket til AIP");  // #NOSONAR
            testView.updateTestStatus("<html>Uttrekket ble pakket til AIP og lagret i<br>" + settingsModel.prop.getProperty("tempFolder") + "\\<br>" +
                    settingsModel.prop.getProperty("currentArchive") + "</html>", false); // #NOSONAR
        } catch (IOException e) {
            mainView.exceptionPopup("Noe gikk galt med pakking til AIP ...");
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
        List<String> manualInfo = adminInfoView.getManualInfo();


        try {
            if (archiveModel.validateDates(manualInfo.get(4), manualInfo.get(5), manualInfo.get(7))) {
                archiveModel.updateAdminInfo(manualInfo);

                adminInfoView.clearContainer();
                adminInfoView = null;

                mainView.showGUI();
                mainView.updateAdminInfo(archiveModel.getAdminInfo());
            }
            else
                mainView.exceptionPopup("En eller flere datoer er ugyldige.");

        } catch (ParseException e) {
            mainView.exceptionPopup("En eller flere datoer er ugyldige.");
        }
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
        testSettingsView = new TestSettingsView(thirdPartiesModel.getSelectedTests(), thirdPartiesModel.getSelectedXqueries());
        testSettingsView.addObserver(this);
        try {
            testSettingsView.createAndShowGUI(mainView.getContainer(), thirdPartiesModel.getCustomXqueries(settingsModel.prop));
        }catch (IndexOutOfBoundsException e) {
            testSettingsView = null;
            mainView.showGUI();
            mainView.exceptionPopup("Fant ikke egendefinerte XQueries mappe. Oppdater innstillinger og pr??v igjen");
        }

    }

    //When "Last inn pakket uttrekk" is clicked.
    @Override
    public void uploadArchive() {
        ScheduledExecutorService uploadScheduler = Executors.newScheduledThreadPool(1);
        uploadScheduler.submit(this::uploadArchiveThread);
    }

    /**
     * Extension of the uploadArchive function. Uploads the archive and calls getAdminInfo().
     */
    public void uploadArchiveThread() {
        int success = archiveModel.uploadFolder(mainView.getContainer());

        //Folder uploaded
        if(success == 1) {
            try {
                mainView.loading(true);
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
                mainView.loading(false);
            } catch (IOException e) {
                mainView.exceptionPopup("Kunne ikke skrive til user.home mappen.");
            }
        }
        //Faulty folder
        else if(success == 0) {
            mainView.exceptionPopup("Mappen inneholder ikke .tar og .xml");
        }
    }

    //When "Lag rapport" is clicked.
    @Override
    public void makeReport() {
        System.out.println("Lager rapport, vennligst vent ..."); // #NOSONAR
        testView.updateTestStatus("Genererer rapporten ...", true);

        ScheduledExecutorService reportScheduler;
        reportScheduler = Executors.newScheduledThreadPool(1);
        reportScheduler.submit(this::makeReportThread);
    }

    /**
     * Extension of the makeReport function. Gets all needed information, then generates the report.
     */
    public void makeReportThread() {
        Properties prop = settingsModel.prop;

        //Get all paths
        String fileName = prop.getProperty("currentArchive");
        String archivePath = prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName; // #NOSONAR
        String testArkivstruktur = archivePath + "\\content\\arkivstruktur.xml";
        String testArkivuttrekk = "\\content\\arkivuttrekk.xml";
        String testLopendeJournal = archivePath + "\\content\\loependeJournal.xml";
        String testOffentligJournal = archivePath + "\\content\\offentligJournal.xml";
        String testDias = "\\dias-mets.xml";

        String veraPdfPath = prop.getProperty("tempFolder") + "\\" + fileName + "\\VeraPDF\\verapdf.xml"; // #NOSONAR
        String droidPath =  prop.getProperty("tempFolder") + "\\" + fileName + "\\DROID\\droid.xml"; // #NOSONAR

        if( Boolean.FALSE.equals(thirdPartiesModel.isDBAlive)) {
            try {
                thirdPartiesModel.setUpBaseXDatabase(settingsModel.prop);
            } catch (IOException e) {
                mainView.exceptionPopup("Kunne ikke generere BaseX database. Noen resultater vil v??re feil.");
            }
        }

        //Fetch archive data for the report
        Map<String, List<String>> xqueryResults = new HashMap<>();
        List<String> headerNumbers = Arrays.asList(
                "3.1.3", "3.1.5_1", "3.1.5_2",
                "3.1.7_1", "3.1.7_1b", "3.1.7_2", "3.1.9_1", "3.1.11b", "3.1.13_1", "3.1.13_2", "3.1.14_1", "3.1.14_2",
                "3.1.20", "3.1.21", "3.1.23_1", "3.1.23_2", "3.1.23_3", "3.1.26_1", "3.1.26_2","3.1.27_1","3.1.27_2",
                "3.2.1_1", "3.2.1_2", "3.2.1_3",
                "3.3.1", "3.3.2_1", "3.3.2_2", "3.3.2_3", "3.3.3_1", "3.3.3_2", "3.3.4", "3.3.5_1", "3.3.5_2", "3.3.5_3", "3.3.6", "3.3.7",
                "dokumentmedium"
        );


        for(String s :headerNumbers) {
            xqueryResults.put(s, getEmptyOrContent(testArkivstruktur, s));
        }

        //1.1
        xqueryResults.put("1.1", archiveModel.getAdminInfo());

        File v = new File(veraPdfPath);
        File d = new File(droidPath);
        String headerNumber = "3.2_1";
        if(v.exists()) {
            xqueryResults.put(headerNumber, getEmptyOrContent(veraPdfPath, headerNumber));
        }
        else {
            xqueryResults.put(headerNumber, Collections.emptyList());
        }
        headerNumber = "3.2_2";
        if(d.exists()) {
            xqueryResults.put(headerNumber, getEmptyOrContent(droidPath, headerNumber));
        }
        else {
            xqueryResults.put(headerNumber, Collections.emptyList());
        }

        xqueryResults.put("3.3.9_1a", getEmptyOrContent(testLopendeJournal, "3.3.9_1a"));
        xqueryResults.put("3.3.9_2a", getEmptyOrContent(testLopendeJournal, "3.3.9_2a"));
        xqueryResults.put("3.3.9_2b", getEmptyOrContent(testOffentligJournal, "3.3.9_2b"));

        xqueryResults.put("3.3.9_3a", getEmptyOrContent(testLopendeJournal, "3.3.9_3a"));
        xqueryResults.put("3.3.9_3b", getEmptyOrContent(testOffentligJournal, "3.3.9_3b"));
        xqueryResults.put("3.3.9_3c", getEmptyOrContent(testArkivstruktur, "3.3.9_3c"));


        //1.2
        String xqName;

        try {
            if (thirdPartiesModel.runBaseX(archiveModel.xmlMeta.getAbsolutePath(), "1.1b.xq", settingsModel.prop).get(0).contains("mets:mets"))
                xqName = "1.2_1a";
            else
                xqName = "1.2_1";

            xqueryResults.put("1.2_1", getEmptyOrContent(testDias, xqName));

        } catch (IOException e) {
            mainView.exceptionPopup("Kunne ikke kj??re 1.1b.xq");
        }

        xqueryResults.put("1.2_2", getEmptyOrContent(testArkivuttrekk, "1.2_2"));
        xqueryResults.put("1.2_3", getEmptyOrContent(testLopendeJournal, "1.2_3"));
        xqueryResults.put("1.2_4", getEmptyOrContent(testOffentligJournal, "1.2_4"));
        xqueryResults.put("1.2_5", getEmptyOrContent(testArkivstruktur, "1.2_5"));

        //Generate report
        reportModel.init(prop, xqueryResults);
        try {
            reportModel.generateReport();
            reportModel.setNewInput(Arrays.asList(1, 1), archiveModel.getAdminInfo());

            reportModel.makeReport();
            testView.updateTestStatus("<html>Rapporten er generert og lagret i<br>" + settingsModel.prop.getProperty("tempFolder") + "\\<br>" +
                    settingsModel.prop.getProperty("currentArchive") + "</html>", false);
            testView.activatePackToAipBtn();
            thirdPartiesModel.deleteBaseXDB(settingsModel.prop);
        } catch (RuntimeException | IOException e) {
            testView.updateTestStatus("En feil i genereringen av rapporten har oppst??tt", false, true);
            mainView.exceptionPopup("En feil i genereringen av rapporten har oppst??tt");
            System.out.println(e.getMessage()); // NOSONAR
        }
    }

    //When "Lagre tests" is clicked.
    @Override
    public void saveTestSettings() {
        List<Boolean> currentList = testSettingsView.getSelectedTests();
        thirdPartiesModel.checkIfXquery(testSettingsView.getSelectedXqueries());

        thirdPartiesModel.updateTests(currentList, testSettingsView.getSelectedXqueries());
        testSettingsView.clearContainer();
        testSettingsView = null;
        mainView.showGUI();

    }
}