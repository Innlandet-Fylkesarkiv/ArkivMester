package arkivmester;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;


public class TestModel {
    // Write arkade testRapport to docx
    XWPFDocument document;

    // Holds text from arkade testRapport html as string
    StringBuilder htmlRawText = new StringBuilder();
    // HtmlRawText formatted
    StringBuilder htmlTextFormatted = new StringBuilder();

    TestModel(){
        getFileToString("../Input/arkaderapportrapport.html", htmlRawText);
    }


    /**
     * Gets data from arkade rapport.
     */
    public void getData() {
        // e.g.
        getDataFromHtml("N5.03");
        getDataFromHtml("N5.02");

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
     * Get issues from arkade testRapport html
     * @param index Index for arkade issues
     */
    public void getDataFromHtml(String index){



        String html = htmlRawText.toString();
        Document doc = Jsoup.parse(html);

        htmlTextFormatted.append(index).append("\n\n");
        Element element = doc.getElementById(index).parent();
        // p: "Type: Strukturkontroll" and "Ingen avvik funnet" if noe mistakes found.
        htmlTextFormatted.append(element.select("p").text()).append("\n");

        org.jsoup.select.Elements rows = element.select("tr");



        String location = "";

        for(org.jsoup.nodes.Element row :rows){

            org.jsoup.select.Elements columns = row.select("td");

            boolean firstColumn = true;
            for (org.jsoup.nodes.Element column:columns)
            {
                if (firstColumn){
                    if (!location.contains(column.text())) {
                        location = column.text();
                        htmlTextFormatted.append("\n").append(location).append("\n\n");
                    }
                    firstColumn = false;
                }
                else {
                    htmlTextFormatted.append(column.text()).append("\n\n");
                }
            }
        }

        System.out.println(htmlTextFormatted.toString()); //NOSONAR

    }
    /**
     * Write output to docx
     */
    private void writeToDocx(){

        try {
            document = new XWPFDocument();

            //Write the Document in file system
            FileOutputStream out = new FileOutputStream( "../Input/createdocument.docx");

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
