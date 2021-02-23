package arkivmester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestModel {
    TestModel() {
        //Test
    }

    public void startBaseX() {
        String cmd = "cmd.exe";
        String pwd = "cd \"C:\\Program Files (x86)\\BaseX\\bin\"";
        String command = "E:\\XQuery-Statements\\bstest.bxs";

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex " + command);
        baseXBuilder.redirectErrorStream(true);
        try {
            Process p = baseXBuilder.start();

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
}
