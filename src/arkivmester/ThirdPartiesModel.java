package arkivmester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThirdPartiesModel {
    ArchiveModel archiveModel;
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
    Runs ArkadeCLI through cmd, the output gets printed to the console.
    Report from the tests gets put in the output folder.

    Params: path - a string that contains the path to the archive that is to be tested.
     */
    public void runArkadeTest(File path) {


        //path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar"; // NOSONAR
        //Path to output folder where test report gets saved.
        String outputPath = "c:\\arkade\\output"; //NOSONAR
        //Path to temp folder where temporary data about the tests gets stored.
        String tempPath = "c:\\arkade\\"; //NOSONAR

        //Process builder to run command line.
        ProcessBuilder arkadeBuilder = new ProcessBuilder(
                cmd, "/c", "cd \"C:\\arkadecli\" && arkade test -a " + path +
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
    Runs Kost-Val through command line, the output gets printed to the console.
    Report from the test gets moved from the user folder to an output folder.

    Params: path - a string that contains the path to the archive that is to be tested.
     */
    public void runKostVal(String path) {

        //path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\dokument"; //NOSONAR
        String reportPath = "c:\\archive\\testoutput"; // NOSONAR

        //Process builder to run kost-val from command line
        ProcessBuilder kostvalBuilder = new ProcessBuilder( //NOSONAR
                cmd, "/c", "cd \"C:\\prog\\kost-val\" &&  java -jar cmd_KOST-Val.jar --sip " +
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
    Runs VeraPDF through command line. The report gets put in an output folder.

    Params: path - a string that contains the path to the archive that is to be tested.
    */
    public void runVeraPDF(String path) {

        //verapdf --recurse c:\archive\test\pakke\content\DOKUMENT > c:\archive\verapdf.xml
        //path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\DOKUMENT"; // NOSONAR
        String reportPath = "c:\\archive\\testoutput\\verapdf.xml"; // NOSONAR

        //Process builder to run VeraPDF from command line
        ProcessBuilder veraPDFBuilder = new ProcessBuilder(
                cmd, "/c", "cd \"C:\\prog\\Verapdf\" && verapdf --recurse " + path + " > " + reportPath);
        veraPDFBuilder.redirectErrorStream(true);
        try {
            veraPDFBuilder.start();
        } catch (IOException e) {
            System.out.println(e.getMessage()); // NOSONAR
        }

        System.out.println("VeraPDF done, report at: " + reportPath); // NOSONAR
    }

    /**
    Runs 7Zip through command line and unzips the archive.

    Params: path - a string that contains the path to the archive that is to be tested.
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


    public void runTests(ArchiveModel archiveModel) {
        List<Boolean> selectedTests = getSelectedTests();
        String fileName = archiveModel.tar.getName();
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));

        unzipArchive(archiveModel.tar);
        System.out.println("\n\tArchive unzipped\n"); //NOSONAR


        if(Boolean.TRUE.equals(selectedTests.get(0))) {
            System.out.print("\nRunning arkade\n"); //NOSONAR
            runArkadeTest(archiveModel.tar);
            System.out.println("\n\tArkade test finished\n"); //NOSONAR
        }
        if(Boolean.TRUE.equals(selectedTests.get(1))) {
            System.out.println("\nRunning DROID\n"); //NOSONAR
            // TODO: Run DROID
        }
        if(Boolean.TRUE.equals(selectedTests.get(2))) {
            System.out.print("\nRunning Kost-Val\n"); //NOSONAR
            runKostVal("C:\\archive\\" + fileName + "\\content\\dokument");
            System.out.println("\n\tKost-Val test finished\n"); //NOSONAR
        }
        if(Boolean.TRUE.equals(selectedTests.get(3))) {
            System.out.print("\nRunning VeraPDF\n"); //NOSONAR
            runVeraPDF("C:\\archive\\" + fileName + "\\content\\DOKUMENT");
            System.out.println("\n\tVeraPDF test finished\n"); //NOSONAR
        }
    }
}
