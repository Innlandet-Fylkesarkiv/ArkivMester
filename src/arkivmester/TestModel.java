package arkivmester;

import org.apache.commons.codec.language.bm.Languages;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.nio.file.Files;




public class TestModel {
    boolean test = true;
    XWPFDocument document;
    FileOutputStream out;
    StringBuilder html;
    TestModel() {
        //Test
        getData();
    }

    /**
     * Gets data from arkade rapport.
     * <p> More info </p>
     */
    public void getData() {
        if (!test) {
            try {
                

            } catch (Exception ex) {
                System.out.println(ex.getMessage()); //NOSONAR
            }

        }
        else{
            getHtml();
            writeToDocx();

        }
    }
    public void getHtml(){
        System.out.println("test"); //NOSONAR
        try {
            FileReader fr = new FileReader("../Input/arkaderapportrapport.html"); //NOSONAR
            BufferedReader br = new BufferedReader(fr); //NOSONAR
            html = new StringBuilder(); // length eg. 1024
            String val;
            while ((val = br.readLine()) != null) {
                html.append(val);
            }
            br.close();
            String result = html.toString();
            System.out.println(result); //NOSONAR

        } catch (Exception ex) {
            System.out.println(ex.getMessage()); //NOSONAR
        }
    }
    public void writeToDocx(){

        try {
            document = new XWPFDocument();

            //Write the Document in file system
            out = new FileOutputStream( new File("../Input/createdocument.docx"));

            //create Paragraph
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            //run.setText("At tutorialspoint.com, we strive hard to " +
            //        "provide quality tutorials for self-learning ");
            run.setText(html.toString());

            document.write(out);
            out.close();
            System.out.println("createparagraph.docx written successfully");
        } catch (IOException e){
            System.out.println(e.getMessage());     //NOSONAR
        }
    }
}
