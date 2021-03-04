package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ArkadeModel {
    // html file at   ../Input/arkaderapportrapport.html
    String filePath = "../Input/arkaderapportrapport.html";
    // output file at ../Output/createdocument.docx
    String outPutFile = "../Output/createdocument.docx";

    // Holds text from arkade testRapport html as string
    StringBuilder htmlRawText = new StringBuilder();

    /* Remove StringBuilder and XWPFDocument when all functions are used */
    // HtmlRawText formatted for Word
    StringBuilder htmlTextFormatted = new StringBuilder();
    // Write arkade testRapport to docx
    XWPFDocument document;

    boolean test = false;
    /**
     * Get Html as String, Runs all functions, Prints deviation to docx.
     * getFileToString to get the html content
     * Remove everything except for getFileToString when all functions are used.
     */
    ArkadeModel(){
        getFileToString(filePath, htmlRawText);

        parseReportHtml(); // todo remove this before push
    }


    public void parseReportHtml(){

        // Html to String
        getFileToString(filePath, htmlRawText);
        if (test){
            // Get deviation for every id
            for(String i: getAll()){
                htmlTextFormatted.append(i);
            }
            htmlTextFormatted.append(getArkadeVersion());
        }
        else {
            // Write to docx
            writeToDocx();
        }
    }

    /**
     * Get html as string
     * @param filePath FilePath to arkade Testrapport html
     * @param htmlTextHolder Text holder
     */
    private void getFileToString(String filePath,  StringBuilder htmlTextHolder){
        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {

            String val;
            while ((val = br.readLine()) != null) {
                htmlTextHolder.append(val);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage()); //NOSONAR
        }
    }

    /**
     * Get all IDs from arkade TestRapport
     * @return List<String> AllIDs
     */
    private List<String> getAllIDs () {
        // All IDs
        List<String> htmlAllIDs = new ArrayList<>();
        Document doc = Jsoup.parse(htmlRawText.toString());

        for (org.jsoup.nodes.Element elementx : doc.select("h3")){
            htmlAllIDs.add( elementx.attr("id"));
        }
        return htmlAllIDs;
    }

    /**
     * Get AllIDS, Get deviation for every ID
     * @return List<String> all deviation in testRapport
     */
    private List<String> getAll () {
        List<String> htmlTable = new ArrayList<>();
        // Get deviation for every id
        for (String index : getAllIDs()){
            htmlTable.addAll(getDataFromHtml(index));
        }
        return htmlTable;
    }

    /**
     * Get arkade versjon.
     * classes with class name: text-right THAN last element
     * @return arkade versjon
     */
    public String getArkadeVersion(){
        Document doc = Jsoup.parse(htmlRawText.toString());
        Elements elements = doc.getElementsByClass(    "text-right");
        return elements.last().text();
    }
    /**
     * Get specific value from table
     * @param index for test class
     * @param containsValue value to get
     * @return one element with containsValue or error message
     */
    public String getSpecificValue(String index, String containsValue){
        String htmlValue = "Can't find " + containsValue;
        int nrOfElements = 0;
        for(String i : getDataFromHtml(index)){
            if(i.contains(containsValue)){
                htmlValue = i;
                nrOfElements++;
            }
        }
        if (nrOfElements>1){
            htmlValue = "More than 1 element with " + containsValue;
        }
        return  htmlValue;
    }

    /**
     * Get deviation table by ID
     * @param index ID for test class
     * @return String list<String> (File Lokasjon, Arkade deviation) or emptyList if noe deviation
     */
    public List<String> getDataFromHtml(String index){

        // reset list
        List<String> htmlTable = new ArrayList<>();


        Document doc = Jsoup.parse(htmlRawText.toString());
        // N6.10
        Element element = doc.getElementById(index).parent();

        org.jsoup.select.Elements rows = element.select("tr");


        for(org.jsoup.nodes.Element row :rows){

            org.jsoup.select.Elements columns = row.select("td");

            boolean firstColumn = true;
            for (org.jsoup.nodes.Element column:columns)
            {
                // First Column
                if (firstColumn){
                    firstColumn = false;
                    //htmlTable.add("Lokasjon: " + column.text());
                    htmlTable.add("" + column.text());
                }
                else {
                    htmlTable.add(column.text());
                }
            }
        }
        return htmlTable;
    }

    /**
     * Write output to docx
     */
    private void writeToDocx(){

        try {
            document = new XWPFDocument();

            //Write the Document in file system
            FileOutputStream out = new FileOutputStream( outPutFile);

            //create Paragraph
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();

            String text = htmlTextFormatted.toString();

            arkadeTestRapport(run);

            document.write(out);
            out.close();
            System.out.println("createparagraph.docx written successfully"); //NOSONAR
        } catch (IOException e){
            System.out.println(e.getMessage());     //NOSONAR
        }
    }

    public void arkadeTestRapport(XWPFRun run){
        StringBuilder writeOut = new StringBuilder();
        //Uttrekket er testet i Arkade 5 versjon VERSJONSNUMMER.
        String version = "Uttrekket er testet i Arkade 5 versjon: " +
                getArkadeVersion().replace("Arkade 5 versjon: ", "");
        run.setText(version);
        // New Line
        run.addBreak();
        run.setText("N5.02");
        run.addBreak();

        XWPFTable table = document.createTable();
        XWPFTableRow tableRowOne = table.getRow(0);
        tableRowOne.getCell(0).setText("col one, row one");
        tableRowOne.addNewTableCell().setText("col two, row one");
        //document.insertNewTbl(paragraph);

        run.addBreak();
        run.setText("N5.02");
        run.setText("N5.02");

        if(getDataFromHtml("N5.02").isEmpty()){
            writeOut.append("Uttrekket er teknisk korrekt.\n");
        } else {
            // TableCode(getDataFromHtml("N5.02")) -- int liste, liste header, string liste
        }
        if(getDataFromHtml("N5.02").isEmpty()){
            writeOut.append("Uttrekket er teknisk korrekt. \n");
        } else {
            // TableCode(getDataFromHtml("N5.02"))
        }
        writeOut.append(getSpecificValue("N5.06", "Arkivdelstatus"));
        writeOut.append(getArkadeVersion());

        System.out.println(writeOut);
        for(String i : getDataFromHtml("N5.02")){
            System.out.println(i + "\n");

        }
    }

}
