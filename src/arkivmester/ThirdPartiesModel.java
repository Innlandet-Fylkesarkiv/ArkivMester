package arkivmester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ThirdPartiesModel {
    ThirdPartiesModel() {
        //ThirdParties
    }

    /*
    Runs ArkadeCLI through cmd, the output gets printed to the console.
    Report from the tests gets put in the output folder.

    Params: path - a string that contains the path to the archive that is to be tested.
     */
    public void runArkadeTest(String path) throws IOException { //NOSONAR

        //Should be "String archivePath = path"
        String archivePath = path; //NOSONAR
        //Path to output folder where test report gets saved.
        String outputPath = "c:\\arkade\\output"; //NOSONAR
        //Path to temp folder where temporary data about the tests gets stored.
        String tempPath = "c:\\arkade\\"; //NOSONAR

        //Process builder to run command line.
        ProcessBuilder arkadeBuilder = new ProcessBuilder(
                "cmd.exe", "/c", "cd \"C:\\arkadecli\" && arkade test -a " + archivePath +
                " -o " + outputPath +  " -p " + tempPath + " -t noark5");
        arkadeBuilder.redirectErrorStream(true);
        Process p = arkadeBuilder.start();

        //Gets the console output and prints it.
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        line = r.readLine();
        while(line != null) {
            line = r.readLine();
            System.out.println(line); //NOSONAR
        }

    }

    public void runKostVal(String path) throws IOException {

        //String archivepath = "c:\\archive\\test\\pakke\\content\\dokument"; //NOSONAR
        path = "c:\\archive\\test\\pakke\\content\\dokument"; //NOSONAR

        ProcessBuilder kostvalBuilder = new ProcessBuilder( //NOSONAR
                "cmd.exe", "/c", "cd \"C:\\prog\\kost-val\" &&  java -jar cmd_KOST-Val.jar --sip " +
                path + " --en");
        kostvalBuilder.redirectErrorStream(true);
        Process p = kostvalBuilder.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        line = r.readLine();
        while(line != null) {
            line = r.readLine();
            System.out.println(line); //NOSONAR
        }

        kostvalBuilder = new ProcessBuilder(
                "cmd.exe", "/c", "move %userprofile%\\.kost-val_2x\\logs\\*.xml* c:\\archive");
        kostvalBuilder.redirectErrorStream(true);
        p = kostvalBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        line = reader.readLine();
        while(line != null) {
            line = reader.readLine();
            System.out.println(line); //NOSONAR
        }



    }




}

