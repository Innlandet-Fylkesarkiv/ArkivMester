package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
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

    // Right know work as rapportModel.main in function
    public void start() {

        setUpBlankDocument();

        chapterOne();

    }

    // Get xml kap 1 information
    private void chapterOne() {

        String file = "src/resources/chapters/1.xml";

        try {
            Document doc = parseFromXMLFile(file);

            assert doc != null;
            Element elem = doc.getDocumentElement();

            System.out.println("Root element :" + elem.getNodeName());  //NOSONAR

            NodeList nList = elem.getChildNodes();

            String s;

            Node n;

            StringBuilder text = new StringBuilder();

            for (int i = 0; i < nList.getLength(); i++) {

                n = nList.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {

                    Element e = (Element) n;

                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        s = switch (e.getNodeName()) {
                            // TODO: create a subtitle function for handling subtitle tags
                            // TODO: create a title function for handling title tags
                            case "bulletPoint" -> formatBulletPoint(n.getTextContent());
                            case "paragraph" -> formatParagraph(n.getTextContent());
                            // TODO: create input function for handling input tags
                            default -> "";
                        };
                        text.append(s);
                    }
                }
            }
            System.out.println(text); //NOSONAR

            writeDocToPath(text.toString());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());             // NOSONAR TODO: have it in GUI message instead of terminal
        }

    }
    // Format bulletPoint
    private String formatBulletPoint(String bulletPoint) {
        String t = bulletPoint;
        t = t.replace("         ", "   ");
        t = t.replace("       ", "");
        t = t.replace("*", "•");

        return t;
    }
    // Format paragraph
    private String formatParagraph(String paragraph) {
        String t = paragraph;
        t = t.replace("  ", "");

        return t;
    }

    // Create rapport document
    private void setUpBlankDocument() {
        document = new XWPFDocument();
        System.out.println("doc successfully set up"); //NOSONAR
    }

    private static Document parseFromXMLFile(String filepath) {   // NOSONAR
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(filepath);

        } catch (NullPointerException | ParserConfigurationException | SAXException | IOException e) {
            System.out.println(e.getMessage());                                                 // NOSONAR
            return null;
        }
    }

    // Write to rapport document
    private void writeDocToPath(String text) {
        //Write the Document in file system
        try {

            //Write the Document in file system
            FileOutputStream out = new FileOutputStream(
                    ("C:/prog/Output/report_template.docx"));



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

            //System.out.println(_text);         //NOSONAR
        } catch (IOException e) {
            System.out.println(e.getMessage());     //NOSONAR
        }
    }

}
