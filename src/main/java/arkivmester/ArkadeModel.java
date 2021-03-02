package arkivmester;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
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
     * Get Html as String, Runs all functions, Prints avvik to docx.
     * getFileToString to get the html content
     * Remove everything except for getFileToString when all functions are used.
     */
    public void parseReportHtml(){

        // Html to String
        getFileToString(filePath, htmlRawText);
        if (test){
            // Get avvik for every id
            for(String i: getAll()){
                htmlTextFormatted.append(i);
            }
            htmlTextFormatted.append(getArkadeVersion());
        }
        else {
            List<String> printToWord = getDataFromHtml("N5.59");
            for (String i : printToWord){
                System.out.println(i); //NOSONAR
                htmlTextFormatted.append(i).append("\n");
            }
        }


        // Write to docx
        writeToDocx();
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
     * Get AllIDS, Get avvik for every ID
     * @return List<String> all avvik in testRapport
     */
    private List<String> getAll () {
        List<String> htmlTable = new ArrayList<>();
        // Get avvik for every id
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
     * @param index ID for test class
     * @return String list<String> (File Lokasjon, Arkade avvik) or emptyList if noe avvik
     */
    public List<String> getDataFromHtml(String index){

        // reset list
        List<String> htmlTable = new ArrayList<>();


        Document doc = Jsoup.parse(htmlRawText.toString());

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
                    htmlTable.add("Lokasjon: " + column.text());
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

            if (text.contains("\n")) { //NOSONAR
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
            System.out.println("createparagraph.docx written successfully"); //NOSONAR
        } catch (IOException e){
            System.out.println(e.getMessage());     //NOSONAR
        }
    }

}
