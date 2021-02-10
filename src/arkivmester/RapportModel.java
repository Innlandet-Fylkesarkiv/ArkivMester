package arkivmester;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

// Used for parsing of xml schema and exception handling

//region Variables admin Data

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
//endregion

public class RapportModel {

    XWPFDocument document;

    RapportModel() {
        //Rapport
        //kap 1, 1.1, 1.2
    }

    // Right know work as rapportModel.main in function
    public void start() {

        setUpBlankDocument();

        chapterOne();

    }

    /* Get xml kap 1 information */
    private void chapterOne() {

        Document doc;

        try{
            File file = new File("src/resources/chapters/1.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); // #NOSONOR
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            doc = dBuilder.parse(file);

            Element elem = doc.getDocumentElement();

            System.out.println("Root element :" + elem.getNodeName()); // #NOSONOR

            NodeList nList = elem.getChildNodes();

            String s;

            Node n;

            StringBuilder text = new StringBuilder();

            for (int i = 0; i < nList.getLength(); i++) {

                n = nList.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {

                    Element e = (Element) n;

                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        switch (e.getNodeName()) {
                            case "bulletPoint":
                                s = formatBulletPoint(n.getTextContent());
                                break;
                            case "paragraph":
                                s = formatParagraph(n.getTextContent());
                                break;
                            default:
                                s = "";
                                break;
                        }
                        text.append(s);
                    }
                }
            }
            System.out.println(text); // #NOSONOR

            writeDocToPath(text.toString());

        } catch(ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }
    /* Format bulletPoint */
    private String formatBulletPoint(String bulletPoint) {
        String t = bulletPoint;
        t = t.replace("         ", "   ");
        t = t.replace("       ", "");
        t = t.replace("*", "•");

        return t;
    }
    /* Format paragraph */
    private String formatParagraph(String paragraph) {
        String t = paragraph;
        t = t.replace("  ", "");

        return t;
    }
    /* Get xml from test results */
    private Document parseFromXMLFile(String filepath) {  // #NOSONOR
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // #NOSONOR

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(filepath);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /* Get report data template path */
    private XWPFDocument readFromDocxFile(String filepath) { // #NOSONOR

        try {
            return new XWPFDocument(OPCPackage.open(filepath));

        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Create rapport document */
    private void setUpBlankDocument() {
        document = new XWPFDocument();
        System.out.println("doc successfully set up"); //#NOSONAR
    }
    /* Write to rapport document */
    private void writeDocToPath(String text) {
        //Write the Document in file system
        try {

            //Write the Document in file system
            FileOutputStream out = new FileOutputStream(
                    ("../Output/report_template.docx"));


            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();

            if (text.contains("\n")) {
                String[] lines = text.split("\n");
                run.setText(lines[0], 0); // set first line into XWPFRun
                for(int i=1;i<lines.length;i++){
                    // add break and insert new text
                    run.addBreak();
                    run.setText(lines[i]);
                }
            } else {
                run.setText(text, 0);
            }

            document.write(out);

            out.close();

            //System.out.println(_text);         //#NOSONAR
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
