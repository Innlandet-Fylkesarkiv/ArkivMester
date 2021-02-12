package arkivmester;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExceptionHandler extends Exception {

    /*
    public static Document tryOpeningFile(String filepath) throws Exception {
        try {
            Document doc = parseFromXMLFile(filepath);
            return doc;
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());                // NOSONAR
            missingFile("file not found"); // NOSONAR
        } catch (missingFile e) {
            System.out.println(e.getMessage());
        }
        Document doc = parseFromXMLFile(filepath);
        return doc;
    }

    // Get xml from test results
    private static Document parseFromXMLFile(String filepath) throws Exception {   // NOSONAR
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(filepath);

        } catch (NullPointerException | ParserConfigurationException | SAXException | IOException e) {
            System.out.println(e.getMessage());                                                 // NOSONAR
            throw new Exception("error in retrieving file, maybe not correct filepath");        // NOSONAR
        }
    }

    private static void missingFile(String message) throws FileNotFoundException {  //constructor
        throw new FileNotFoundException("file is empty");
    }

     */

}
