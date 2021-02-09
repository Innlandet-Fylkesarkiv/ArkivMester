package arkivmester;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

// Used for parsing of xml schema and exception handling


public class RapportModel {

    XWPFDocument document;

    RapportModel() {
        //Rapport
        //kap 1, 1.1, 1.2
    }
    public void start() { // mainz
        // finne fil
        // opne fil
        // get variabls
        // put in string/something


        XWPFDocument docx = ReadFromDocxFile();


        setUpBlankDocument();
        /*
        Locale l = new Locale("en", "US");

        ResourceBundle bundle = ResourceBundle.getBundle("resources/chapter_1", l); // chapters
        System.out.println("test"); // no sonar
        String teste = bundle.getString("p1");

        List<IBodyElement> body = docx.getBodyElements();


        writeDocToPath(body);
        */

        System.out.println("teste"); //#NOSONAR

        test();
        testBetter();

    }
    private void test() {

        Document doc;

        try{
            File file = new File("src/resources/chapters/1.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            doc = dBuilder.parse(file);

            Element elem = doc.getDocumentElement();

            System.out.println("Root element :" + elem.getNodeName());

            NodeList nList = elem.getChildNodes();

            System.out.println("----------------------------");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    System.out.println("Text:" + eElement.getTextContent());

                    String t = eElement.getTextContent();

                    System.out.print(t);

                    writeDocToPath(eElement.getTextContent());
                }
            }

        } catch(ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        //System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

        //System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
        //NodeList nodeList = doc.getElementsByTagName("student");
        // System.out.println("Student id: "+ eElement.getElementsByTagName("id").item(0).getTextContent());
    }
    private void testBetter() {
        //File file = new File("src/resources/chapters/1.xml");
        //doc = dBuilder.parse(file);
        //NodeList nList = doc.getElementsByTagName("titleBall");

    }

    public class TestClasse{
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
            return builder.parse("src/resources/899ec389-1dc0-41d0-b6ca-15f27642511b.xml");

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private XWPFDocument ReadFromDocxFile() {

        try {
            XWPFDocument docx = new XWPFDocument(OPCPackage.open("src/resources/chapters/1.docx"));
            return docx;

        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setUpBlankDocument() {
        document = new XWPFDocument();
        System.out.println("doc successully set up"); //#NOSONAR
    }

    private void writeDocToPath(String _text) {
        //Write the Document in file system
        try {
            /*
            FileOutputStream out = new FileOutputStream(new File("../Output/createdocument.docx"));

            byte[] bytesArray = _text.getBytes();

            out.write(bytesArray);

            System.out.println("File Written Successfully");

            out.close();

            XWPFWordExtractor ex = new XWPFWordExtractor(document);
            String text = ex.getText();


             */

            //Write the Document in file system
            FileOutputStream out = new FileOutputStream(new File
                    ("../Output/createparagraph.docx"));

            //create Paragraph
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            _text = _text.replace("      ", "\t");
            run.setText(_text);
            document.write(out);

            out.close();

            //System.out.println(_text);         //#NOSONAR
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
