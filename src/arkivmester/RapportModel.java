package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.Iterator;

// Used for parsing of xml schema and exception handling
public class RapportModel {

    XWPFDocument document;
    String templateFile = "resources/Testrapport.docx";
    String outputFile = "C:/prog/Output/report_template.docx";

    RapportModel() {
        //Rapport
        //kap 1, 1.1, 1.2
    }

    // Right know work as rapportModel.main in function
    public void start() {

        setUpReportDocument(templateFile);

        writeReportDocument();

        printReportToFile();

    }

    // Try to fetch report template, and if there are no IO problems, it will be stored

    private void setUpReportDocument(String filepath) {
        try (
                FileInputStream fis = new FileInputStream(filepath)
        ) {
            document = new XWPFDocument(fis);
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());             //NOSONAR
        }

    }

    private void writeReportDocument() {
        Iterator<IBodyElement> bodyElementIterator = document.getBodyElementsIterator();

        while(bodyElementIterator.hasNext()) {
            IBodyElement element = bodyElementIterator.next();
            if(element instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph)element;
                for(XWPFRun r : p.getRuns()) {
                    String text = r.getText(0);
                    if(text != null && text.contains("TODO")) {
                        text = text.replace("TODO", "GG");
                        r.setText(text, 0);
                    }
                }
            }
        }
    }

    private void printReportToFile() {
        try {
            FileOutputStream os = new FileOutputStream(outputFile);
            document.write(os);
            document.close();
            os.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

}
