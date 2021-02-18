package arkivmester;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThirdPartiesModel {
    String cmd = "cmd.exe";

    ThirdPartiesModel() {
        //ThirdParties
    }

    /*
    Runs ArkadeCLI through cmd, the output gets printed to the console.
    Report from the tests gets put in the output folder.

    Params: path - a string that contains the path to the archive that is to be tested.
     */
    public void runArkadeTest(String path) {


        //arkade process -a c:\archive\899ec389-1dc0-41d0-b6ca-15f27642511b.tar -m c:\archive\899ec389-1dc0-41d0-b6ca-15f27642511b.xml -o c:\arkade\output -p c:\arkade -t noark5
        path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar"; // NOSONAR
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

    /*
    Runs Kost-Val through command line, the output gets printed to the console.
    Report from the test gets moved from the user folder to an output folder.

    Params: path - a string that contains the path to the archive that is to be tested.
     */
    public void runKostVal(String path) {

        path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\dokument"; //NOSONAR
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
                    cmd, "/c", "move %userprofile%\\.kost-val_2x\\logs\\*.xml* " + reportPath);
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

    public void runVeraPDF(String path) {

        //verapdf --recurse c:\archive\test\pakke\content\DOKUMENT > c:\archive\verapdf.xml
        path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b\\content\\DOKUMENT"; // NOSONAR
        String reportPath = "c:\\archive\\testoutput\\verapdf.xml"; // NOSONAR

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

    public void unzipArchive(String path) {

        path = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar"; // NOSONAR
        String outputpath = "C:\\archive";  //NOSONAR

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

        //System.out.println("test"); //NOSONAR
    }
}

//cmd, "/c", "cd \"C:Programfiler\\7-Zip\" && 7z x " + path + " -o" + outputpath + " -r");
//7z x c:\archive\899ec389-1dc0-41d0-b6ca-15f27642511b.tar -oc:\archive\tt -r