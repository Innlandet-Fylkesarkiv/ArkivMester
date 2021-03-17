package arkivmester;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * Class for handling and parsing the Arkade testreport html file.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ArkadeModel {
    // html file at   ../Input/arkaderapportrapport.html
    String filePath;

    // Holds text from arkade testreport html as string
    StringBuilder htmlRawText = new StringBuilder();

    /* Remove StringBuilder and test bool when all functions are used */
    // HtmlRawText formatted for Word
    StringBuilder htmlTextFormatted = new StringBuilder();
    /* Remove StringBuilder and test bool when all functions are used */

    boolean test = false;

    /** 3.1.16. Check for number of registrations with saksparter.
     * @return Comment on number of saksparter
     */
    public List<Integer> saksparter(){
        List<Integer> list = new ArrayList<>();
        Integer saksparter = getTotal("N5.35", 1);
        Integer antallReg = getTotal("N5.16", 1);

        list.add(saksparter);
        if(saksparter <= 0){
            // Ingen saksparter er registrert.
            list.add(0);
        }
        else if(saksparter < (antallReg / 4)){
            // saksparter + saksparter er registrert, og virker normalt for uttrekket.
            list.add(1);
        }
        else{
            // saksparter + saksparter er registrert, varsel: over 25% av antall registreringer har saksparter
            list.add(2);
        }
        return list;
    }

    /** 3.1.17. Get Merkader
     * @return if merknader "", else comment
     */
    public boolean ingenMerknader(){
        int merknader = getTotal("N5.36", 1);
        //Ingen merknader er registrert.
        return merknader <= 0;
    }

    /** Get Totalt from deviation table.
     * @param index for test class.
     * @return Totalt or -1 if no deviation table.
     */
    public Integer getTotal(String index, int element){
        List<String> total = getDataFromHtml(index);

        if(total.size() < (element + 1)){
            System.out.println(index + " has less than " + element + " elements"); //NOSONAR
            return -1;
        }
        return Integer.parseInt(total.get(element).replaceAll("\\D+", ""));
    }

    /**
     * Get Html as String getFileToString.
     * Run all function. Remove after all function are used in ArchiveController.
     */
    public void parseReportHtml(){

        if (test){
            // Get deviation for every id
            for(String i: getAll()){
                htmlTextFormatted.append(i);
            }
            htmlTextFormatted.append(getSpecificValue("N5.10", "ArkadeTest"));
        }
        else {
            // Write to docx
            System.out.println(htmlTextFormatted); //NOSONAR
        }
    }

    /**
     * Get first file(testreport html) in Arkade/Output folder as a string.
     * @param prop filePath to arkade testreport html.
     */
    public boolean getFileToString(Properties prop){
        htmlRawText = new StringBuilder();
        // Folder path: Arkade/output
        filePath = prop.getProperty("tempFolder") + "\\" + prop.getProperty("currentArchive") + "\\Arkade\\Report"; //#NOSONAR

        try {
            // Dir: "arkadeOutput" folder
            File dir = new File(filePath);
            // Get first file in dir
            filePath = filePath + '\\' + Objects.requireNonNull(dir.list())[0];
        } catch (Exception ex) {
            System.out.println("Get first file in Arkade/output. Error: " + ex.getMessage()); //NOSONAR
            return false;
        }

        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {

            String val;
            while ((val = br.readLine()) != null) {
                htmlRawText.append(val);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage()); //NOSONAR
            return false;
        }
        return true;
    }

    /**
     * Get all IDs from arkade Testreport.
     * @return List of deviation IDs.
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
     * Get all IDs, Get deviation for every ID.
     * @return all deviation in file testreport.
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
     * Get arkade version.
     * @return arkade version.
     */
    public String getArkadeVersion(){
        Document doc = Jsoup.parse(htmlRawText.toString());
        if(doc.getElementsByClass("text-right") == null) {
            System.out.println("Can't find ArkadeVersion"); //NOSONAR
            return "";
        }
        Elements elements = doc.getElementsByClass("text-right");
        return elements.last().text();
    }

    /**
     * Get specific value from deviation table.
     * @param index for test class.
     * @param containsValue value to get.
     * @return one element with containsValue or error message.
     */
    public String getSpecificValue(String index, String containsValue){
        String htmlValue = "";
        int nrOfElements = 0;
        for(String i : getDataFromHtml(index)){
            if(i.contains(containsValue)){
                htmlValue = i;
                nrOfElements++;
            }
        }
        if (nrOfElements>1){
            htmlValue = "";
            System.out.println("More than 1 element with " + containsValue); //NOSONAR
        }
        else if (nrOfElements == 0) {
            System.out.println("Can't find deviation with: " + containsValue); //NOSONAR
        }
        return  htmlValue;
    }

    /**
     * Get deviation table by ID.
     * @param index ID for test class.
     * @return List where every other element contains.
     *      File Location and Arkade deviation or emptyList if no deviation table.
     */
    public List<String> getDataFromHtml(String index){

        // reset list
        List<String> htmlTable = new ArrayList<>();


        Document doc = Jsoup.parse(htmlRawText.toString());

        if(doc.getElementById(index) == null) {
            System.out.println("No index: " + index); //NOSONAR
            return htmlTable;
        }

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
                    htmlTable.add("" + column.text());
                }
                else {
                    htmlTable.add(column.text());
                }
            }
        }
        return htmlTable;
    }
}
