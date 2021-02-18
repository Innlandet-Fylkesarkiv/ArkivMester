package arkivmester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThirdPartiesModel {
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



}

