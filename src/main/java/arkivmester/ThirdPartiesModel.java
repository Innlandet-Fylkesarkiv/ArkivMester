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
    int amountOfTests = 4;
    String tempFolder;

    /**
     * Initializes the selectedTests list to true.
     */
    ThirdPartiesModel() {
        for(int i = 0; i < amountOfTests; i++)
            selectedTests.add(true);
    }

    /**
     * Updates selectedTests with updated data.
     * @param selectedList Updated selectedTests from the UI.
     */
    public void updateSelectedTests(List<Boolean> selectedList) {
        selectedTests = selectedList;
    }

    /**
     * Regular getter for selectedTests list.
     * @return selectedTests boolean list.
     */
    public List<Boolean> getSelectedTests() {
        return selectedTests;
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
        tempFolder = prop.getProperty("tempFolder");
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
        String outputPath = tempFolder + "\\Arkade\\Report";
        //Path to temp folder where temporary data about the tests gets stored.
        String tempPath = tempFolder + "\\Arkade";

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
        String reportPath = tempFolder + "\\KostVal";

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
        String reportPath = tempFolder + "\\VeraPDF" + "\\verapdf.xml";

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
        String profilePath = tempFolder + "\\DROID" + "\\profile.droid";
        //Path to folder where test output ends up.
        String outputPath = tempFolder + "\\DROID";

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
        runCMD(cd + " && 7z x " + path + " -o" +tempFolder+" -r");
    }

    /**
     * Queries an .xml file via an .xq XQuery/XPath file.
     * @param xml Path to .xml file.
     * @param xqName Config key for .xq file.
     * @param prop Properties object containing the config.
     * @return String list of the results from the query.
     */
    public List<String> runBaseX(String xml, String xqName, Properties prop) throws IOException {
        String xq = prop.getProperty("xqueryExtFolder") + "\\" + xqName;
        String temp = prop.getProperty("tempFolder") + "\\xqueryResult.txt";
        String pwd = cdString + prop.getProperty("basexPath") + "\"";
        List<String> result = new ArrayList<>();

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -o " + temp + " -i " + xml + " " + xq);

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

    public Boolean checkIfToolsArePresent(Properties prop) {
        File file;
        if(Boolean.TRUE.equals(selectedTests.get(0))) {
            file = new File(prop.getProperty("arkadePath"));
            if(!file.exists())
                return false;
        }
        if(Boolean.TRUE.equals(selectedTests.get(1))) {
            file = new File(prop.getProperty("droidPath"));
            if(!file.exists())
                return false;
        }
        if(Boolean.TRUE.equals(selectedTests.get(2))) {
            file = new File(prop.getProperty("kostvalPath"));
            if(!file.exists())
                return false;
        }
        if(Boolean.TRUE.equals(selectedTests.get(3))) {
            file = new File(prop.getProperty("veraPDFPath"));
            if(!file.exists())
                return false;
        }

        file = new File(prop.getProperty("basexPath"));
        if(!file.exists())
            return false;

        file = new File(prop.getProperty("7ZipPath"));
        return file.exists();
    }
}
