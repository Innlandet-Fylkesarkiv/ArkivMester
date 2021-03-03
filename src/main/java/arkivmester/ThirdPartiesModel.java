package arkivmester;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.Thread.sleep;

/**
 * Contains multiple methods to run different third party tools to test the archive.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ThirdPartiesModel {
    String cmd = "cmd.exe";
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
     */
    public void runArkadeTest(File path) {


        //path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar"; // NOSONAR
        //Path to output folder where test report gets saved.
        String outputPath = "e:\\Arkade\\output"; //NOSONAR
        //Path to temp folder where temporary data about the tests gets stored.
        String tempPath = "e:\\Arkade\\"; //NOSONAR

        //Process builder to run command line.
        ProcessBuilder arkadeBuilder = new ProcessBuilder(
                cmd, "/c", "cd \"C:\\prog\\Arkade5\" && arkade test -a " + path +
                " -o " + outputPath + " -p " + tempPath + " -t noark5");
        arkadeBuilder.redirectErrorStream(true);
        try {
            Process p = arkadeBuilder.start();
            //Gets the console output and prints it.
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            line = r.readLine();
            while (line != null) {
                line = r.readLine();
                System.out.println(line); //NOSONAR
            }
        } catch (IOException e) {
            System.out.println(e.getMessage()); // NOSONAR
        }

    }

    /**
     * Runs Kost-Val through command line, the output gets printed to the console.
     * Report from the test gets moved from the user folder to an output folder.
     *
     * @param path A string that contains the path to the archive that is to be tested
     */
    public void runKostVal(String path) {

        //path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\dokument"; //NOSONAR
        String reportPath = "c:\\archive\\testoutput"; // NOSONAR

        //Process builder to run kost-val from command line
        ProcessBuilder kostvalBuilder = new ProcessBuilder( //NOSONAR
                cmd, "/c", "cd \"C:\\prog\\KOSTVal\" &&  java -jar cmd_KOST-Val.jar --sip " +
                path + " --en");
        kostvalBuilder.redirectErrorStream(true);
        try {
            Process p = kostvalBuilder.start();
            //Gets the console output and prints it.
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            line = r.readLine();
            while (line != null) {
                line = r.readLine();
                System.out.println(line); //NOSONAR
            }

            //Process builder to move report to an output folder.
            kostvalBuilder = new ProcessBuilder(
                    cmd, "/c", "move %userprofile%\\.kost-val_2x\\logs\\*.* " + reportPath);
            kostvalBuilder.redirectErrorStream(true);
            p = kostvalBuilder.start();

            //Gets the console output and prints it.
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                System.out.println(line); //NOSONAR
            }
        } catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
        }

    }

    /**
     * Runs VeraPDF through command line. The report gets put in an output folder.
     *
     * @param path A string that contains the path to the archive that is to be tested
     */
    public void runVeraPDF(String path) {

        //verapdf --recurse c:\archive\test\pakke\content\DOKUMENT > c:\archive\verapdf.xml
        //path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\DOKUMENT"; // NOSONAR
        String reportPath = "c:\\archive\\testoutput\\verapdf.xml"; // NOSONAR

        //Process builder to run VeraPDF from command line
        ProcessBuilder veraPDFBuilder = new ProcessBuilder(
                cmd, "/c", "cd \"C:\\prog\\VeraPDF\" && verapdf --recurse " + path + " > " + reportPath);
        veraPDFBuilder.redirectErrorStream(true);
        try {
            veraPDFBuilder.start();
        } catch (IOException e) {
            System.out.println(e.getMessage()); // NOSONAR
        }

        System.out.println("VeraPDF done, report at: " + reportPath); // NOSONAR
    }

    /**
     * Runs 7Zip through command line and unzips the archive.
     *
     * @param path A file that contains the archive to be unzipped
     */
    public void unzipArchive(File path) {

        //path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar"; // NOSONAR
        String outputpath = "C:\\archive";  //NOSONAR

        //Process builder to run VeraPDF from command line
        ProcessBuilder zipBuilder = new ProcessBuilder(
        cmd, "/c", "cd \"C:\\Programfiler\\7-Zip\" && 7z x " + path + " -o" +outputpath+" -r");
        zipBuilder.redirectErrorStream(true);
        try {
            Process p = zipBuilder.start();
            //Gets the console output and prints it.
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            line = r.readLine();
            while (line != null) {
                line = r.readLine();
                System.out.println(line); //NOSONAR
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
        }

    }

    /**
     * Queries an .xml file via an .xq XQuery/XPath file.
     * @param xml Path to .xml file.
     * @param xqName Config key for .xq file.
     * @param prop Properties object containing the config.
     * @return String list of the results from the query.
     */
    public List<String> runBaseX(String xml, String xqName, Properties prop)  {
        String xq = prop.getProperty("xqueryExtFolder") + xqName;
        String temp = prop.getProperty("tempFolder") + "\\xqueryResult.txt";
        String pwd = "cd \"" + prop.getProperty("basexPath") + "\""; //NOSONAR
        List<String> result = new ArrayList<>();

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -o " + temp + " -i " + xml + " " + xq);

        try {
            File xqueryResult = new File(temp);

            baseXBuilder.start();
            sleep(1000);

            try (BufferedReader r = new BufferedReader(new FileReader(xqueryResult))) {
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
