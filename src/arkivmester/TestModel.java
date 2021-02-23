package arkivmester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TestModel {
    TestModel() {
        //Test
    }

    public List<String> runBaseX(String xml, String xq)  {
        String cmd = "cmd.exe";
        String pwd = "cd \"C:\\Program Files (x86)\\BaseX\\bin\"";
        List<String> result = new ArrayList<>();

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -i " + xml + " " + xq);
        baseXBuilder.redirectErrorStream(true);

        try {
            Process p = null;
            p = baseXBuilder.start();

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
