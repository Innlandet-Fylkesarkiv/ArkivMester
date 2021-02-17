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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Used for parsing of xml schema and exception handling
public class RapportModel {
    List<String> adminInfoList = new ArrayList<>(); //Always have 8 elements
    XWPFDocument document;

    RapportModel() {
        //Rapport
        //kap 1, 1.1, 1.2

        //Adds 8 empty fields in the list
        for (int i = 0; i<8; i++) {
            adminInfoList.add("");
        }
    }

    //Gets adminInfoList list
    public List<String> getAdminInfo() {
        return adminInfoList;
    }

    //Updates adminInfoList list
    public void updateAdminInfo(List<String> list) {
        adminInfoList = list;
    }

    //Resets adminInfoList
    public void resetAdminInfo() {
        for (int i = 0; i<adminInfoList.size(); i++) {
            adminInfoList.set(i, "");
        }
    }
    
    // Right know work as rapportModel.main in function
    public void start() {

        setUpBlankDocument();

        chapterOne();

    }

    //Read administrative data from .xml file
    public void readAdminXmlFile(File xml) {
        try {
            Document doc = parseFromXMLFile(xml.getAbsolutePath());

            //4, Produksjonsdato for uttrekket
            NodeList metsHdrList = Objects.requireNonNull(doc).getElementsByTagName("metsHdr");
            Node metsHdr = metsHdrList.item(0);
            if (metsHdr.getNodeType() == Node.ELEMENT_NODE) {
                Element metsHdrElement = (Element)metsHdr;
                adminInfoList.set(4, metsHdrElement.getAttribute("CREATEDATE"));
            }

            //Agent nodes (1 and 2)
            NodeList agentList = doc.getElementsByTagName("agent");
            parseAgentNodes(agentList);

        } catch (Exception e) {
            System.out.println("Could not find .xml file"); //#NOSONAR
        }
    }

    //Parsing agent nodes for administrative data
    private void parseAgentNodes(NodeList agentList) {
        List<Node> personList = new ArrayList<>();
        Node person;
        for (int i = 0; i < agentList.getLength(); i++) {
            Node nNode = agentList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element)nNode;
                NamedNodeMap attrs =  nNode.getAttributes();

                if(attrs.getLength() == 3) {
                    parseCommuneCustomer(attrs, eElement);

                    person = parseContactPerson(attrs, eElement);
                    if (person!=null && person.getNodeType() == Node.ELEMENT_NODE) {
                        personList.add(person);
                    }

                    //Missing noark version here (3)
                }
            }
        }

        //2, Kontaktperson (Formatting)
        int size = personList.size();
        for (int i = 0; i < size; i++) {
            adminInfoList.set(2, adminInfoList.get(2) + personList.get(i).getTextContent());

            if(size>1 && i != size-1) {
                adminInfoList.set(2, adminInfoList.get(2) + ", ");
            }
        }
    }

    //Parsing commune/customer nodes for administrative data
    private void parseCommuneCustomer(NamedNodeMap attrs, Element eElement) {
        //1, Kommune/Kunde (Query and Formatting)
        //Attribute 1 in .xml is (1) in list
        //Attribute 2 in .xml is (0) in list
        //Attribute 3 in .xml is (2) in list
        if(
            ((Attr)attrs.item(0)).getValue().equals("SUBMITTER")
                    && ((Attr)attrs.item(1)).getValue().equals("OTHER")
                    && ((Attr)attrs.item(2)).getValue().equals("ORGANIZATION")) {

            NodeList nameList = eElement.getElementsByTagName("name");
            Node name = nameList.item(0);
            if (name.getNodeType() == Node.ELEMENT_NODE) {
                adminInfoList.set(1, name.getTextContent());
            }
        }
    }

    //Parsing contact person nodes for administrative data
    private Node parseContactPerson(NamedNodeMap attrs, Element eElement) {
        //2, Kontaktperson (Query)
        //Attribute 1 in .xml is (1) in list
        //Attribute 2 in .xml is (0) in list
        //Attribute 3 in .xml is (2) in list
        if(
           ((Attr)attrs.item(0)).getValue().equals("SUBMITTER")
                    && ((Attr)attrs.item(1)).getValue().equals("OTHER")
                    && ((Attr)attrs.item(2)).getValue().equals("INDIVIDUAL"))  {

            NodeList tempList = eElement.getElementsByTagName("name");
            return tempList.item(0);
        }

        //If node is not a contact person
        return null;
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
