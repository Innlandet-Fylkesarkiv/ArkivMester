package arkivmester;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Contains multiple methods to run different third party tools to test the archive.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ThirdPartiesModel {
    String cmd = "cmd.exe";
    String cdString = "cd \"";
    private List<Boolean> selectedTests = new ArrayList<>();
    private List<Boolean> selectedXqueries = new ArrayList<>();
    private List<String> xmlNames = new ArrayList<>();
    private String [] xqueryNames;
    Boolean runXqueries = false;
    int amountOfTests = 4;
    String tempFolder;
    String archiveName;
    String basexPathKey = "basexPath";
    String tempFolderKey = "tempFolder";

    /**
     * Initializes the selectedTests list to true.
     */
    ThirdPartiesModel() {
        for(int i = 0; i < amountOfTests; i++) {
            selectedTests.add(true);
        }

    }

    /**
     * Updates selectedTests with updated data.
     * @param selectedXqueries Updated selectedTests from the UI.
     */
    public void checkIfXquery(List<Boolean> selectedXqueries) {
        int count = 0;
        for(Boolean value : selectedXqueries) {
            if(Boolean.TRUE.equals(value))
                runXqueries = true;
            else
                count++;
        }
        if(count==selectedXqueries.size())
            runXqueries = false;
    }

    /**
     * Updates selectedTests with updated data.
     * @param selectedList Updated selectedTests from the UI.
     */
    public void updateTests(List<Boolean> selectedList, List<Boolean> selectedXqueries) {
        this.selectedTests = selectedList;
        this.selectedXqueries = selectedXqueries;
    }

    /**
     * Updates xmlNames with updated data.
     * @param xmlNames Updated xmlNames from the UI.
     */
    public void updateXmlNames(List<String> xmlNames) {
        this.xmlNames = xmlNames;
    }

    /**
     * Regular getter for selectedTests list.
     * @return selectedTests boolean list.
     */
    public List<Boolean> getSelectedTests() {
        return this.selectedTests;
    }

    /**
     * Regular getter for selectedXqueries list.
     * @return selectedXqueries boolean list.
     */
    public List<Boolean> getSelectedXqueries() {
        return this.selectedXqueries;
    }

    /**
     * Regular getter for xmlNames list.
     * @return xmlNames String list.
     */
    public List<String> getXmlNames() {
        return this.xmlNames;
    }

    /**
     * Resets the selectedTests to true. Used when the program resets.
     */
    public void resetSelectedTests() {
        for (int i = 0; i<amountOfTests; i++) {
            selectedTests.set(i, true);
        }
    }

    /**
     * Initializes the tempFolder variable with the current tempFolder path.
     */
    public void initializePath(Properties prop) {
        tempFolder = prop.getProperty(tempFolderKey);
        archiveName = "\\" + prop.getProperty("currentArchive"); // #NOSONAR
    }

    /**
     * Runs ArkadeCLI through cmd, the output gets printed to the console.
     * Report from the tests gets put in the output folder.
     *
     * @param path A file that contains the archive that is to be tested.
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runArkadeTest(File path, Properties prop) throws IOException {

        //String with path to arkadeCli
        String cd = cdString + prop.getProperty("arkadePath") + "\"";
        //Path to output folder where test report gets saved.
        String outputPath = "\"" + tempFolder + archiveName + "\\Arkade\\Report\"";
        //Path to temp folder where temporary data about the tests gets stored.
        String tempPath = "\"" + tempFolder + archiveName + "\\Arkade\"";


        //Run ArkadeCli through command line.
        runCMD(cd + " && arkade test -a " + path + " -o " + outputPath + " -p " + tempPath + " -t noark5");

    }

    /**
     * Runs Kost-Val through command line, the output gets printed to the console.
     * Report from the test gets moved from the user folder to an output folder.
     *
     * @param path A string that contains the path to the archive that is to be tested
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runKostVal(String path, Properties prop) throws IOException {

        //String with path to KostVal
        String cd = cdString + prop.getProperty("kostvalPath") + "\"";
        //Path to folder where test report gets moved to.
        String reportPath = "\"" + tempFolder + archiveName + "\\KostVal\"";
        //Run kost-val from command line
        runCMD(cd + " &&  java -jar cmd_KOST-Val.jar --sip " + path + " --en");
        //Move testreport to an output folder.
        runCMD("move %userprofile%\\.kost-val_2x\\logs\\*.* " + reportPath);

    }

    /**
     * Runs VeraPDF through command line. The report gets put in an output folder.
     *
     * @param path A string that contains the path to the archive that is to be tested
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runVeraPDF(String path, Properties prop) throws IOException {

        //String with path to VeraPDF
        String cd = cdString + prop.getProperty("veraPDFPath") + "\"";
        //Path to folder where test report gets moved to.

        String reportPath = "\"" + tempFolder + archiveName + "\\VeraPDF" + "\\verapdf.xml\"";

        //Run verapdf through command line.
        runCMD(cd + " && verapdf --recurse " + path + " > " + reportPath);

        System.out.println("VeraPDF done, report at: " + reportPath); // NOSONAR
    }

    /**
     * Runs DROID through command line. Creates four different files and places them in a folder.
     *
     * @param path A string that contains the path to the archive to be tested.
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runDROID(String path, Properties prop) throws IOException {

        //String with path to droid location.
        String cd = cdString + prop.getProperty("droidPath") + "\"";
        //String with command to run .jar file
        String jar = " && java -jar droid-command-line-6.5.jar";

        //Path to droid profile needed to run droid.
        String profilePath = "\"" + tempFolder + archiveName + "\\DROID" + "\\profile.droid\"";
        //Path to folder where test output ends up.
        String outputPath = "\"" + tempFolder + archiveName + "\\DROID\"";

        //Run first DROID function - making the droid profile.
        System.out.println("\nDroid 1"); //NOSONAR
        runCMD(cd + jar + " -R -a " + path + " -p " + profilePath);

        //Run second DROID function - making a spreadsheet of all files in archive.
        System.out.println("\nDroid 2"); //NOSONAR
        runCMD(cd + jar + " -p " + profilePath + " -e " + outputPath + "\\filliste.csv");

        //Run third DROID function - making a xml test report.
        System.out.println("\nDroid 3"); //NOSONAR
        runCMD(cd + jar + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                "-t \"DROID Report XML\" -r " + outputPath + "\\droid.xml");

        //Run fourth DROID function - making a pdf test report.
        System.out.println("\nDroid 4"); //NOSONAR
        runCMD(cd + jar + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                "-t \"PDF\" -r " + outputPath + "\\droid.pdf");
    }

    /**
     * Runs 7Zip through command line and unzips the archive.
     *
     * @param path A file that contains the archive to be unzipped
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void unzipArchive(File path, Properties prop) throws IOException {

        //String with path to 7zip location.
        String cd = cdString + prop.getProperty("7ZipPath") + "\"";
        //Run VeraPDF from command line
        runCMD(cd + " && 7z x " + path + " -o\"" + tempFolder + archiveName + "\" -r");
    }

    public void runXquery(Properties prop) throws IOException { // #NOSONAR
        String xmlName;
        String archivePath = tempFolder + archiveName;
        for(int i = 0; i<selectedXqueries.size(); i++) {
            if(Boolean.TRUE.equals(selectedXqueries.get(i))) {
                xmlName = xmlNames.get(i).toLowerCase();

                //Find full xml path
                if(xmlName.contains("droid")) {
                    xmlName = archivePath + "\\DROID\\droid.xml";
                }
                else if(xmlName.contains("verapdf")) {
                    xmlName = archivePath + "\\VeraPDF\\verapdf.xml";
                }
                else if(xmlName.contains("kostval")) {
                    xmlName = archivePath + "\\KostVal\\dokument.kost-val.log.xml";
                }
                else if(xmlName.contains("arkade")) {
                    xmlName = archivePath + "\\Arkade\\report\\Arkaderapprt-" + prop.get("currentArchive") + ".html";
                }
                else if(xmlName.contains("dias-mets")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\dias-mets.xml"; // #NOSONAR
                }
                else if(xmlName.contains("log")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\log.xml"; // #NOSONAR
                }
                else if(xmlName.contains("dias-premis")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\administrative_metadata\\diaspremis.xml"; // #NOSONAR
                }
                else if(xmlName.contains("addml")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\administrative_metadata\\addml.xml"; // #NOSONAR
                }
                else if(xmlName.contains("eac-cpf")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\descriptive_metadata\\eac-cpf.xml"; // #NOSONAR
                }
                else if(xmlName.contains("ead")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\descriptive_metadata\\ead.xml"; // #NOSONAR
                }
                else if(xmlName.contains("arkivstruktur")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\content\\arkivstruktur.xml"; // #NOSONAR
                }
                else if(xmlName.contains("arkivuttrekk")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\content\\arkivuttrekk.xml"; // #NOSONAR
                }
                else if(xmlName.contains("endringslogg")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\content\\endringslogg.xml"; // #NOSONAR
                }
                else if(xmlName.contains("loependejournal")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\content\\loependeJournal.xml"; // #NOSONAR
                }
                else if(xmlName.contains("offentligjournal")) {
                    xmlName = archivePath + "\\" + prop.get("currentArchive") + "\\content\\offentligJournal.xml"; // #NOSONAR
                }

                runCustomBaseX(xmlName, xqueryNames[i], prop);
            }
        }
    }

    /**
     * Queries an .xml file via an .xq XQuery/XPath file.
     * @param xml Path to .xml file.
     * @param xqName Config key for .xq file.
     * @param prop Properties object containing the config.
     * @return String list of the results from the query.
     */
    public List<String> runBaseX(String xml, String xqName, Properties prop) throws IOException {
        String xq = prop.getProperty("xqueryExtFolder") + "\\" + xqName + "\"";
        String temp = prop.getProperty(tempFolderKey) + "\\xqueryResult.txt";
        String pwd = cdString + prop.getProperty(basexPathKey) + "\"";
        List<String> result = new ArrayList<>();

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -o \"" + temp + "\" -i " + xml + " " + xq); // #NOSONAR

        try {
            Process p = baseXBuilder.start();
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        File xqueryResult = new File(temp);
        try (InputStream bytes = new FileInputStream(xqueryResult)) {

            Reader chars = new InputStreamReader(bytes, StandardCharsets.UTF_8);

            try (BufferedReader r = new BufferedReader(chars)) {
                String line = r.readLine();
                while (line != null) {
                    result.add(line);
                    line = r.readLine();
                }
            }
        }

        return result;
    }


    public void runCustomBaseX(String xml, String xqName, Properties prop) throws IOException {
        //XQuery
        String xq = prop.getProperty("xqueryCustomFolder") + "\\" + xqName + "\"";

        //Result name
        String outFileName = xqName;
        outFileName = outFileName.substring(0,outFileName.lastIndexOf('.'));

        String outFile = prop.getProperty(tempFolderKey) + archiveName + "\\" + outFileName + ".txt";
        String pwd = cdString + prop.getProperty(basexPathKey) + "\"";
        System.out.println(pwd + " && basex -o \"" + outFile + "\" -i " + xml + " " + xq);
        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -o \"" + outFile + "\" -i " + xml + " " + xq);

        try {
            Process p = baseXBuilder.start();
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Creates a process and runs a cmd command.
     *
     * @param command The cmd command that is to be ran.
     * @throws IOException Cannot run program.
     */
    private void runCMD(String command) throws IOException {
        //Creates a process to run a command in cmd.
        ProcessBuilder cmdBuilder = new ProcessBuilder(
                cmd, "/c", command);
        Process p = cmdBuilder.start();
        //Gets output from cmd and prints it.
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = r.readLine();
        while(line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();

        }
        r.close();
    }

    /**
     * Checks if the third party tools are present where the config defines.
     * @param prop Properties object containing the config.
     * @return True if all tools are present, false if there is one or more tools missing.
     */
    public Boolean checkIfToolsArePresent(Properties prop) {
        File file;

        file = new File(prop.getProperty(basexPathKey) + "\\basex.bat");
        if(!file.exists())
            return false;

        file = new File(prop.getProperty("7ZipPath") + "\\7zG.exe");
        if(!file.exists())
            return false;

        for(int i = 0; i<selectedTests.size(); i++) {
            if(Boolean.TRUE.equals(selectedTests.get(i))) {
                switch (i) {
                    case 0:
                        file = new File(prop.getProperty("arkadePath") + "\\Bundled\\Siegfried\\siegfried.exe");
                        break;
                    case 1:
                        file = new File(prop.getProperty("droidPath") + "\\droid-command-line-6.5.jar");
                        break;
                    case 2:
                        file = new File(prop.getProperty("kostvalPath") + "\\cmd_KOST-Val.jar");
                        break;
                    case 3:
                        file = new File(prop.getProperty("veraPDFPath") + "\\verapdf.bat");
                        break;
                    default:
                }
                if(!file.isFile())
                    return false;
            }
        }

        return true;
    }

    /**
     * Checks if there is at least 1 included test before saving.
     * @param list Boolean list from the UI which is not yet saved.
     * @return True if there is at least 1 included test, false if there are none included tests.
     */
    public Boolean noEmptyTests(List<Boolean> list) {
        int count = 0;

        for(Boolean val: list) {
            if(Boolean.FALSE.equals(val))
                count++;
        }

        return count != list.size();
    }

    /**
     * Gets the names of the custom XQueries in the custom folder. Also initiates "selectedXqueries" with false for all
     * files.
     * @param prop Config property object to get the custom folder's path.
     * @return String array with the name of the custom XQuery files.
     */
    public String[] getCustomXqueries(Properties prop) {
        File f = new File((String)prop.get("xqueryCustomFolder"));
        String[] list = f.list();

        if(selectedXqueries.isEmpty()) {
            if(list != null) {
                for (int i = 0; i<list.length; i++) {
                    selectedXqueries.add(false);
                }
                xqueryNames = list;
                return list;
            }
            return new String[]{""};
        }

        if(list != null) {
            return list;
        }
        return new String[]{""};
    }
}
