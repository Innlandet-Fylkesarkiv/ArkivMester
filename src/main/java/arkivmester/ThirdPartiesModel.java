package arkivmester;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.Thread.sleep;

/**
 * Contains multiple methods to run different third party tools to test the archive.
 *
 * @since 1.0
 * @version 1.0
 */
public class ThirdPartiesModel {
    String cmd = "cmd.exe";
    String cdString = "cd \"";
    private List<Boolean> selectedTests = new ArrayList<>();
    int amountOfTests = 4;

    ThirdPartiesModel() {
        //Initiate selectedTests list
        for(int i = 0; i < amountOfTests; i++)
            selectedTests.add(true);
    }
    public void updateSelectedTests(List<Boolean> selectedList) {
        selectedTests = selectedList;
    }
    public List<Boolean> getSelectedTests() {
        return selectedTests;
    }
    public void resetSelectedTests() {
        for (int i = 0; i<amountOfTests; i++) {
            selectedTests.set(i, true);
        }
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
        String outputPath = prop.getProperty("arkadeOutput");
        //Path to temp folder where temporary data about the tests gets stored.
        String tempPath = prop.getProperty("arkadeTemp");

        //Process builder to run command line.
        ProcessBuilder arkadeBuilder = new ProcessBuilder(
                cmd, "/c", cd + " && arkade test -a " + path +
                " -o " + outputPath + " -p " + tempPath + " -t noark5");
        arkadeBuilder.redirectErrorStream(true);
        Process p = arkadeBuilder.start();
        //Gets the console output and prints it.
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        line = r.readLine();
        while (line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();

        }
        r.close();

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
        String reportPath = prop.getProperty("kostValReport");

        //Process builder to run kost-val from command line
        ProcessBuilder kostvalBuilder = new ProcessBuilder( //NOSONAR
                cmd, "/c", cd + " &&  java -jar cmd_KOST-Val.jar --sip " +
                path + " --en");
        kostvalBuilder.redirectErrorStream(true);
        Process p = kostvalBuilder.start();
        //Gets the console output and prints it.
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        line = r.readLine();
        while (line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();

        }
        r.close();

        //Process builder to move report to an output folder.
        kostvalBuilder = new ProcessBuilder(
                cmd, "/c", "move %userprofile%\\.kost-val_2x\\logs\\*.* " + reportPath);
        kostvalBuilder.redirectErrorStream(true);
        p = kostvalBuilder.start();

        //Gets the console output and prints it.
        r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        line = r.readLine();
        while (line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();

        }
        r.close();

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
        String reportPath = prop.getProperty("veraPDFReport") + "\\verapdf.xml";

        //Process builder to run VeraPDF from command line
        ProcessBuilder veraPDFBuilder = new ProcessBuilder(
                cmd, "/c", cd + " && verapdf --recurse " + path + " > " + reportPath);
        veraPDFBuilder.redirectErrorStream(true);
        veraPDFBuilder.start();

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
        String profilePath = prop.getProperty("droidOutput") + "\\profile.droid";
        //Path to folder where test output ends up.
        String outputPath = prop.getProperty("droidOutput");

        //Process builder to run DROID from command line
        ProcessBuilder droidBuilder = new ProcessBuilder(
                cmd, "/c", cd + jar + " -R -a " + path + " -p " + profilePath);

        //Run first DROID function - making the droid profile.
        System.out.println("\nDroid 1"); //NOSONAR
        Process p = droidBuilder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = r.readLine();
        while(line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();

        }
        r.close();

        //Run second DROID function - making a spreadsheet of all files in archive.
        System.out.println("\nDroid 2"); //NOSONAR
        droidBuilder = new ProcessBuilder(
                cmd, "/c", cd + jar + " -p " + profilePath + " -e " + outputPath + "\\filliste.csv");
        p = droidBuilder.start();
        r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        line = r.readLine();
        while (line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();
        }
        r.close();

        //Run third DROID function - making a xml test report.
        System.out.println("\nDroid 3"); //NOSONAR
        droidBuilder = new ProcessBuilder(
                cmd, "/c", cd + jar + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                "-t \"DROID Report XML\" -r " + outputPath + "\\droid.xml");
        p = droidBuilder.start();
        r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        line = r.readLine();
        while (line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();
        }
        r.close();

        //Run fourth DROID function - making a pdf test report.
        System.out.println("\nDroid 4"); //NOSONAR
        droidBuilder = new ProcessBuilder(
                cmd, "/c", cd + jar + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                "-t \"PDF\" -r " + outputPath + "\\droid.pdf");
        p= droidBuilder.start();
        r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        line = r.readLine();
        while (line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();
        }
        r.close();

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
        //Path to where the archive gets unzipped to
        String outputpath = prop.getProperty("7ZipOutput");

        //Process builder to run VeraPDF from command line
        ProcessBuilder zipBuilder = new ProcessBuilder(
        cmd, "/c", cd + " && 7z x " + path + " -o" +outputpath+" -r");
        zipBuilder.redirectErrorStream(true);
        Process p = zipBuilder.start();
        //Gets the console output and prints it.
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        line = r.readLine();
        while (line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();

        }
        r.close();

    }

    /**
     * Queries an .xml file via an .xq XQuery/XPath file.
     * @param xml Path to .xml file.
     * @param xqName Config key for .xq file.
     * @param prop Properties object containing the config.
     * @return String list of the results from the query.
     */
    public List<String> runBaseX(String xml, String xqName, Properties prop)  {
        String xq = prop.getProperty("xqueryExtFolder") + "\\" + xqName;
        String temp = prop.getProperty("tempFolder") + "\\xqueryResult.txt";
        String pwd = cdString + prop.getProperty("basexPath") + "\""; //NOSONAR
        List<String> result = new ArrayList<>();

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -o " + temp + " -i " + xml + " " + xq);

        File xqueryResult = new File(temp);
        try (InputStream bytes = new FileInputStream(xqueryResult)) {
            baseXBuilder.start();
            sleep(1000);

            Reader chars = new InputStreamReader(bytes, StandardCharsets.UTF_8);

            try (BufferedReader r = new BufferedReader(chars)) {
                String line = r.readLine();
                while (line != null) {
                    result.add(line);
                    line = r.readLine();
                }
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(e.getMessage()); //#NOSONAR
        }

        return result;
    }
}
