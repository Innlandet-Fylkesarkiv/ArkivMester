package arkivmester;

import javax.swing.*;
import java.io.*;
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
            mainView.exceptionPopup("BaseX kunne ikke kjøre 1.1.xq, 1.1a.xq og/eller 1.1b.xq");
        } catch (DateTimeParseException e) {
            mainView.exceptionPopup("CREATEDATE formatet i metadata.xml er feil");
        } catch (IndexOutOfBoundsException e) {
            mainView.exceptionPopup("Fant ikke XQueries eller de er feil");
        }
    }

    /**
     * Unzips the archive and runs the selected tests.
     */
    private void runTests() { //NOSONAR
        List<Boolean> selectedTests = thirdPartiesModel.getSelectedTests();
        thirdPartiesModel.initializePath(settingsModel.prop);
        String fileName = archiveModel.tar.getName();                                   // NOSONAR
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));                   // NOSONAR
        String docPath = "C:\\archive\\" + "test" + "\\pakke\\content\\dokument"; // NOSONAR ONLY TESTING
        //Should use the one below, but takes too long
        //String docPath = "\"" + settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName + "\\content\\dokument \""; // NOSONAR

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
            docPath = "\"" + settingsModel.prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName + "\\content\\dokumenter\""; // NOSONAR
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
            reportModel.attachments.add("\u2022 Arkade5 testrapport");

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
            reportModel.attachments.add("\u2022 VeraPDF testrapport");
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
            reportModel.attachments.add("\u2022 Kost-val testrapport");
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
            reportModel.attachments.add("\u2022 DROID rapporter");
        }

        //XQuery
        if(Boolean.TRUE.equals(thirdPartiesModel.runXqueries)) {
            System.out.println("\nRunning XQueries\n"); //NOSONAR
            testView.updateXqueryStatus(TestView.RUNNING);
            try {
                thirdPartiesModel.runXquery(settingsModel.prop);
            } catch (IOException e) {
                System.out.println(e.getMessage()); //NOSONAR
                mainView.exceptionPopup("XQuery test feilet, prøv igjen.");
            }
            System.out.println("\n\tXQuery finished\n"); //NOSONAR
            testView.updateXqueryStatus(TestView.DONE);
        }

        System.out.println("\nTesting Ferdig\n"); //NOSONAR

        testView.updateTestStatus(TestView.TESTDONE);
        testView.activateCreateReportBtn();
    }

    private List<String> getEmptyOrContent(String xml, String header) {
        String empty = "empty";
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
            mainView.exceptionPopup("BaseX kunne ikke kjøre " + header + " .xq filen. Sjekk om filen eksisterer");
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
            bld.append("Det mangler en eller flere verktøy på maskinen:");
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
        testSettingsView = new TestSettingsView(thirdPartiesModel.getSelectedTests(), thirdPartiesModel.getSelectedXqueries(), thirdPartiesModel.getXmlNames());
        testSettingsView.addObserver(this);
        try {
            testSettingsView.createAndShowGUI(mainView.getContainer(), thirdPartiesModel.getCustomXqueries(settingsModel.prop));
        }catch (IndexOutOfBoundsException e) {
            testSettingsView = null;
            mainView.showGUI();
            mainView.exceptionPopup("Fant ikke egendefinerte XQueries mappe. Oppdater innstillinger og prøv igjen");
        }

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

    //When "Lag rapport" is clicked.
    @Override
    public void makeReport() {
        Properties prop = settingsModel.prop;

        String format = testView.getSelectedFormat(); //#NOSONAR
        String fileName = prop.getProperty("currentArchive");
        String archivePath = "\"" + prop.getProperty("tempFolder") + "\\" + fileName + "\\" + fileName; // #NOSONAR
        String testArkivstruktur = archivePath + "\\content\\arkivstruktur.xml\"";

        Map<String, String> map = new LinkedHashMap<>();
        map.put("1.2_1.xq",archivePath + "\\dias-mets.xml\"");
        map.put("1.2_2.xq",archivePath + "\\content\\arkivuttrekk.xml\"");
        map.put("1.2_3.xq",archivePath + "\\content\\loependeJournal.xml\"");
        map.put("1.2_4.xq",archivePath + "\\content\\offentligJournal.xml\"");
        map.put("1.2_5.xq",testArkivstruktur);


        Map<String, List<String>> xqueryResults = new HashMap<>();
        List<String> headerNumbers = Arrays.asList("3.1.11", "3.1.13", "3.1.20", "3.2.1_1", "3.2.1_2",
                "3.2.1_3", "3.3.1", "3.3.2_1", "3.3.2_2", "3.3.2_3", "3.1.21", "3.1.26_1", "3.1.26_2",
                "3.1.3", "3.3.4", "3.3.6", "3.3.7", "3.1.23_1", "3.1.23_2", "3.1.23_3", "3.3.3_1", "3.3.3_2");

        for(String s :headerNumbers) {
            xqueryResults.put(s, getEmptyOrContent(testArkivstruktur, s));
        }

        reportModel.init(prop, xqueryResults);

        reportModel.generateReport();
        reportModel.setNewInput(Arrays.asList(1, 1), archiveModel.getAdminInfo());

        reportModel.makeReport();
        testView.updateTestStatus("<html>Rapporten er generert og lagret i<br>" + settingsModel.prop.getProperty("tempFolder") + "\\<br>" +
                                        settingsModel.prop.getProperty("currentArchive") + "</html>");
        testView.activatePackToAipBtn();
    }

    //When "Lagre tests" is clicked.
    @Override
    public void saveTestSettings() {
        boolean success = true;
        List<Boolean> currentList = testSettingsView.getSelectedTests();
        thirdPartiesModel.checkIfXquery(testSettingsView.getSelectedXqueries());

        if(Boolean.TRUE.equals(thirdPartiesModel.runXqueries)) {
            List<String> currentXmlList = testSettingsView.getXmlNames();
            if(!currentXmlList.isEmpty())
                thirdPartiesModel.updateXmlNames(currentXmlList);
            else {
                mainView.exceptionPopup("En eller flere XQuery tester mangler .xml fil navn.");
                success = false;
            }
        }

        if(Boolean.TRUE.equals(success)) {
            thirdPartiesModel.updateTests(currentList, testSettingsView.getSelectedXqueries());
            testSettingsView.clearContainer();
            testSettingsView = null;
            mainView.showGUI();
        }
    }
}