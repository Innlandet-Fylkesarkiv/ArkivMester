package arkivmester;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// Used for parsing of xml schema and exception handling
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;



public class RapportModel {

    XWPFDocument document;

    RapportModel() {
        //Rapport
    }
    public void start() {
        // finne fil
        // opne fil
        // get variabls
        // put in string/something

        //FileInputStream fis = new FileInputStream("Muu.docx");
        Document doc = ParseFromXMLFile();

        setUpBlankDocument();

        writeDocToPath(doc);


    }
    public class BlueBalls{
        /*
        UttrekksID: 0000-yyyy-00
        Eier av uttrekket
        Kommune/kunde: Kommunenavn
        Kontaktperson: Ola Nordmann, Kari Nordmann
        Uttrekksformat: Noark5 versjon 0.0
        Produksjonsdato for uttrekket: dd.mm.yyyy
        Uttrekk mottatt dato: dd.mm.yyyy
        Test utført av: Navn Navnesen
        Dato for rapport: dd.mm.yyyy
        */
        String id;

    }

    private Document ParseFromXMLFile() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse("inputAndOutput/Inn/899ec389-1dc0-41d0-b6ca-15f27642511b.xml");
            return document;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setUpBlankDocument() {
        document = new XWPFDocument();
        System.out.println("doc successully set up");
    }

    private void writeDocToPath(Document doc) {
        //Write the Document in file system
        try {
            FileOutputStream out = new FileOutputStream(new File("inputAndOutput/Out/createdocument.docx"));

            XWPFParagraph para = document.createParagraph();

            para.getParagraphText();
            XWPFRun run;
            run = para.createRun();
            run.setText("testing run text");

            document.write(out);

            out.close();

            XWPFWordExtractor ex = new XWPFWordExtractor(document);
            String text = ex.getText();

            System.out.println(text);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
