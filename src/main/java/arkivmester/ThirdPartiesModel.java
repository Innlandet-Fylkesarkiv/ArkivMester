package arkivmester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the functions to run third party test tools.
 *
 * Contains multiple methods to run different third party tools to test the archive.
 * @since 1.0
 * @version 1.0
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
                System.out.println(line); //NOSONAR
                line = r.readLine();

            }
            r.close();
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

    public void runDROID(String path) {

        String cd = "cd \"C:\\prog\\droid\" && java -jar droid-command-line-6.5.jar";
        String profilePath = "C:\\archive\\droid\\profile.droid"; //NOSONAR
        String outputPath = "C:\\archive\\droid\\"; //NOSONAR
        path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\DOKUMENT"; //NOSONAR

        ProcessBuilder droidBuilder = new ProcessBuilder(
                cmd, "/c", cd + " -R -a " + path + " -p " + profilePath);
        try{
            System.out.println("\nDroid 1"); //NOSONAR
            Process p = droidBuilder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = r.readLine();
            while(line != null) {
                System.out.println(line); //NOSONAR
                line = r.readLine();

            }
            r.close();
            System.out.println("\nDroid 2"); //NOSONAR
            droidBuilder = new ProcessBuilder(
                    cmd, "/c", cd + " -p " + profilePath + " -e " + outputPath + "filliste.csv");
            p = droidBuilder.start();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = r.readLine();
            while (line != null) {
                System.out.println(line); //NOSONAR
                line = r.readLine();
            }
            r.close();
            System.out.println("\nDroid 3"); //NOSONAR
            droidBuilder = new ProcessBuilder(
                    cmd, "/c", cd + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                    "-t \"DROID Report XML\" -r " + outputPath + "droid.xml");
            p = droidBuilder.start();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = r.readLine();
            while (line != null) {
                System.out.println(line); //NOSONAR
                line = r.readLine();
            }
            r.close();

            System.out.println("\nDroid 4"); //NOSONAR
            droidBuilder = new ProcessBuilder(
                    cmd, "/c", cd + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                    "-t \"PDF\" -r " + outputPath + "droid.pdf");
            p= droidBuilder.start();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = r.readLine();
            while (line != null) {
                System.out.println(line); //NOSONAR
                line = r.readLine();
            }
            r.close();

        }
        catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
        }

        //C:\prog\Droid>java -jar droid-command-line-6.5.jar -R -a c:\archive\899ec389-1dc0-41d0-b6ca-15f27642511b\content\DOKUMENT -p c:\archive\droid\profile.droid
        //C:\prog\Droid>java -jar droid-command-line-6.5.jar -p c:\archive\droid\profile.droid -e c:\archive\droid\filliste.csv
        //java -jar droid-command-line-6.5.jar -p c:\archive\droid\profile.droid -n "Comprehensive breakdown" -t "DROID Report XML" -r c:\archive\droid\droid.xml
        //java -jar droid-command-line-6.5.jar -p c:\archive\droid\profile.droid -n "Comprehensive breakdown" -t "PDF" -r c:\archive\droid\droid.pdf
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
                System.out.println(line); //NOSONAR
                line = r.readLine();

            }
            r.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage()); //NOSONAR
        }

    }

    /**
     * Queries an .xml file via an .xq XQuery/XPath file.
     * @param xml Path to .xml.
     * @param xq Path to .xq.
     * @return String list of the results from the query.
     */
    public List<String> runBaseX(String xml, String xq)  {
        String pwd = "cd \"C:\\Program Files (x86)\\BaseX\\bin\""; //NOSONAR
        List<String> result = new ArrayList<>();

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -i " + xml + " " + xq);

        try {
            Process p = baseXBuilder.start();

            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = r.readLine();
            while (line != null) {
                result.add(line);
                line = r.readLine();
            }
            r.close();
        } catch (IOException e) {
            System.out.println(e.getMessage()); //#NOSONAR
        }

        return result;
    }
}
