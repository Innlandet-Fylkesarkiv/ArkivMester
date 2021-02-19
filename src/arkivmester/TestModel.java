package arkivmester;

import org.w3c.dom.Element;

import java.io.*;


public class TestModel {
    TestModel() {
        //Test
    }

    public void getData() {
        System.out.println("test"); //NOSONAR
        try {
            StringBuilder html = new StringBuilder();
            FileReader fr = new FileReader("../Input/arkaderapportrapport.html"); //NOSONAR
            BufferedReader br = new BufferedReader(fr); //NOSONAR
            String val;
            while ((val = br.readLine()) != null) {
                html.append(val);
                br.close();
                String result = html.toString();
                System.out.println(result); //NOSONAR
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage()); //NOSONAR
        }
    }
}
