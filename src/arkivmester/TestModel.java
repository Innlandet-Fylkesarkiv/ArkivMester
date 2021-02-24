package arkivmester;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class TestModel {
    // Write arkade testRapport to docx
    XWPFDocument document;
    // HtmlRawText formatted
    List<String> htmlAllIDs = new ArrayList<>();
    // Holds text from arkade testRapport html as string
    StringBuilder htmlRawText = new StringBuilder();
    // HtmlRawText formatted
    StringBuilder htmlTextFormatted = new StringBuilder();

    // html file at   ../Input/arkaderapportrapport.html
    String filePath = "../Input/arkaderapportrapport.html";
    // output file at ../Output/createdocument.docx
    String outPutFile = "../Output/createdocument.docx";

    /* Output format to docx e.g
    N5.03
    Type: Strukturkontroll


    Lokasjon: arkivstruktur.xml

    File location: <arkivstruktur.xml> avvik mld
    File location: <arkivstruktur.xml> avvik mld

    Lokasjon: endringslogg.xml

    File location: <endringslogg.xml> avvik mld
    File location: <> (<-"Ingen fil") avvik mld

    N5.02
    Type: Strukturkontroll  Ingen avvik funnet.

     */

    /**
     * Print all to docx
     */
    public void parseReportHtml(){
        // Html to String
        getFileToString(filePath, htmlRawText);
        // Get IDs
        getAllIDs();
        // Get avvik for every id
        for (String index : htmlAllIDs){
            getDataFromHtml(index);
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
    private void getAllIDs () {
        Document doc = Jsoup.parse(htmlRawText.toString());

        for (org.jsoup.nodes.Element elementx : doc.select("h3")){
            htmlAllIDs.add( elementx.attr("id"));
        }
    }
    /**
     * Get issues from arkade testRapport html
     * @param index Index for arkade issues
     */
    public void getDataFromHtml(String index){

        Document doc = Jsoup.parse(htmlRawText.toString());

        htmlTextFormatted.append(index).append("\n");
        Element element = doc.getElementById(index).parent();
        // p: "Type: Strukturkontroll" and "Ingen avvik funnet" if noe mistakes found.
        htmlTextFormatted.append(element.select("p").text()).append("\n\n");

        org.jsoup.select.Elements rows = element.select("tr");


        String location = "";

        for(org.jsoup.nodes.Element row :rows){

            org.jsoup.select.Elements columns = row.select("td");

            boolean firstColumn = true;
            for (org.jsoup.nodes.Element column:columns)
            {
                // First Column
                if (firstColumn){
                    // First Column changed
                    if (!location.contains(column.text())) {
                        location = column.text();
                        htmlTextFormatted.append("\n").append("Lokasjon: ").append(location).append("\n");
                    }
                    firstColumn = false;
                    htmlTextFormatted.append("\n").append("File location: <").append(column.text()).append("> ");
                }
                else {
                    htmlTextFormatted.append(column.text()).append("\n");
                }
            }
        }


        //System.out.println(htmlTextFormatted.toString()); //NOSONAR

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
