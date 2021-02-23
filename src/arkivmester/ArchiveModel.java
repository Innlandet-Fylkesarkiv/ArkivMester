package arkivmester;

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds all data about the archive and its relevant utility functions.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ArchiveModel {
    File folder;
    File tar;
    File xmlMeta;

    private List<String> adminInfoList = new ArrayList<>(); //Always have 8 elements
    int amountAdminFields = 8;

    /**
     * Constructor - Initiates adminInfoList
     */
    ArchiveModel() {
        for (int i = 0; i<amountAdminFields; i++) {
            adminInfoList.add("");
        }
    }

    /**
     * Opens file explorer and saves selected folder to File folder.
     * @param container Frame's container used as location to create the file chooser.
     * @return 1 if successful, 0 if failed or -1 if cancelled
     */
    public int uploadFolder(Container container) {

        JFileChooser fc = new JFileChooser("C:/");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int response = fc.showOpenDialog(container);

        //If folder is chosen
        if (response == JFileChooser.APPROVE_OPTION) {
            folder = new File(fc.getSelectedFile().getAbsolutePath());

            FilenameFilter filter = (f, name) -> name.endsWith(".tar") || name.endsWith(".xml");
            File [] files = folder.listFiles(filter);

            //If there are 2 files in the directory
            if(files != null && files.length == 2) {
                if(files[0].getName().endsWith(".tar") && files[1].getName().endsWith(".xml")){
                    tar = files[0];
                    xmlMeta = files[1];
                    return 1;
                }
                else if (files[0].getName().endsWith(".xml") && files[1].getName().endsWith(".tar")) {
                    xmlMeta = files[0];
                    tar = files[1];
                    return 1;
                }
                else {
                    return 0;
                }
            }
            else {
                return 0;
            }
        } else {
            System.out.println("Cancelling opening document");//#NOSONAR
            return -1;
        }
    }

    /**
     * Regular getter for saved edited administrative information data.
     * @return String list of administrative data.
     */
    public List<String> getAdminInfo() {
        return adminInfoList;
    }

    /**
     * Updates adminInfoList with new information.
     * @param list String list of new administrative information data to be saved.
     */
    public void updateAdminInfo(List<String> list) {
        adminInfoList = list;
    }

    /**
     * Resets administrative information data saved in adminInfoList.
     */
    public void resetAdminInfo() {
        for (int i = 0; i<adminInfoList.size(); i++) {
            adminInfoList.set(i, "");
        }
    }

    /**
     * Reads administrative data from .xml file.
     * @param xml The .xml file to be read from.
     * @throws NullPointerException if given .xml file is corrupt/moved.
     */
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

    /**
     * Parses agent nodes from a node list and updates adminInfoList.
     * @param agentList The node list to be read from.
     */
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

    /**
     * Parses commune/customer node for administrative data and updates adminInfoList.
     * @param attrs The node map with .xml attributes.
     * @param eElement The node that is currently being parsed.
     */
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

    /**
     * Finds correct contact person node for administrative data.
     * @param attrs The node map with .xml attributes.
     * @param eElement The node that is currently being parsed.
     * @return Contact person node or null if node is not a contact person.
     */
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

    /**
     * Parses and reads .xml file to a Document object.
     * @param filepath Path to the .xml file to be parsed.
     * @return Document object of the .xlm file or null if exception is thrown.
     * @throws NullPointerException if the filepath is faulty.
     */
    private static Document parseFromXMLFile(String filepath) {   // NOSONAR
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(filepath);

        } catch (NullPointerException | ParserConfigurationException | SAXException | IOException e) {
            //System.out.println(e.getMessage());                                                 // NOSONAR
            return null;
        }
    }
}
