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
    // test html file at   ../Input/arkaderapportrapport.html, Arkaderapport-67a47ea4-68bc-4276-a599-22561e0c31df.html, Arkaderapport-0439ba78-2381-430b-8f99-740f71846f1e.html"
    String filePath = "../Input/arkaderapportrapport.html";

    // Holds text from arkade testreport html as string
    StringBuilder htmlRawText = new StringBuilder();

    static final String TOTALT = "Totalt";

    ArkadeModel(){

    }

    /** Not done, waiting for update. 3.1.14 and 3.1.31
     * Get id from N5.27 than get arkvidel start and end date in N5.27
     * Not done: Loop through all id in N5.11 and N5.18. and compare start and end dat
     * N5.11 and N5.18 dates needs too be between star and end date from N5.27
     * 3.1.14 and 3.1.31
     *
     */
    public String firstLastRegistrering() { // NOSONAR

        String tmp = ""; // remove this
        String kap527 = "N5.27";

        // All ID
        List<String> id = getSystemID(kap527, "systemID");

        // 60 Idw, 27 IDs, 11 18 har id'er p[ orginal arkade html
        for (int i = 0; i < id.size(); i++) {
            String tmpID = id.get(i);
            if(id.size() <= 1){
                tmpID = ":";
            }
            // One ID
            List<String> startEndDate = getSpecificValue(kap527, tmpID);

            String start = "";
            String end = "";
            // Start and end date for that one ID
            for (int j = 0; j < startEndDate.size(); j++) {
                if(startEndDate.get(i).contains("Første registrering")){
                    start = getNumberInTextAsString(kap527,startEndDate.get(i),"-").get(0);
                }
                if(startEndDate.get(i).contains("Siste registrering")){
                    end = getNumberInTextAsString(kap527,startEndDate.get(i),"-").get(0);
                }
            }
            getNumberInTextAsString("N5.11",startEndDate.get(i),"-");
            // List of

            tmp = end + start; // remove this
        }

        return tmp;
    }
    /** 3.1.27: N5.47, N5.34
     *
     */
    public Integer systemidentifikasjonerForklaring(List<String> docxInput){
        // N5.47 - Systemidentifikasjoner
        // N5.34 - Dokumentfiler med referanse
        Integer total = getTotal("N5.34",TOTALT);
        int totalSystemID = getSpecificValue("N5.47", "Ikke-unik ID").size();

        if(total == 0 && totalSystemID == 0){
            return 0;
        }
        else if(total.equals(totalSystemID)){

            docxInput.add(Integer.toString(totalSystemID));
            return 1;
        }
        else {
            docxInput.add(Integer.toString(totalSystemID));
            // antall spesial arkivdeler ???
            return 2;
        }

    }
    /** 3.1.16. Check for number of registrations with saksparter.
     * @return Comment on number of saksparter
     */
    public List<Integer> saksparter(){
        List<Integer> list = new ArrayList<>();
        Integer saksparter = getTotal("N5.35", TOTALT); //NOSONAR
        Integer antallReg = getTotal("N5.16", TOTALT);  //NOSONAR

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
     * @return true if no merknader
     */
    public boolean ingenMerknader(){
        int merknader = getTotal("N5.36", TOTALT);
        //Ingen merknader er registrert.
        return merknader <= 0;
    }
    /**
     * Gets 2012 from 2012:1 || 1 from :1
     * @param index N5.**
     * @param containsValue Find string with substring
     * @param indexSysbol ":" get number after ":". Other symbols get text between indexSysbol-":"
     * @return "" or String as a number
     */
    public List<String> getNumberInTextAsString(String index, String containsValue,  String indexSysbol){

        String onlyNumbers = "\\D+";

        List<String> allNumbers = getSpecificValue(index, containsValue);
        List<String> total = new ArrayList<>();

        for (String allNumber : allNumbers) {
            // has not :
            if (!allNumber.contains(":")) {
                System.out.println("LOOP: " + index + " value with " + containsValue + " has no \":\" "); //NOSONAR
            }
            else{
                String tmp = getTextAt(allNumber, indexSysbol);

                // no numbers -
                if (allNumber.matches(onlyNumbers)) {
                    System.out.println("LOOP: " + index + " value with " + containsValue + " has no number after last \":\" "); //NOSONAR
                }
                else{
                    tmp = tmp.replaceAll(onlyNumbers, "");
                    total.add(tmp);
                }
            }

        }

        return total;
    }

    /**
     * @param numberList String List with number.
     * @return -1 if no number in list.
     */
    public Integer sumStringListWithOnlyNumbers(List<String> numberList){

        int number = 0;
        boolean numberInList = false;
        for (String numberStr: numberList){
            if (numberStr.matches("\\D+")) {
                System.out.println("List has element without number in it"); //NOSONAR
                return -1;
            }
            number += Integer.parseInt(numberStr);
            numberInList = true;
        }
        if(numberInList){
            return number;
        }

        return -1;
    }
    /**
     * @param text Gets substring form text
     * @param indexSymbol Input ":" get substring after last ":" OR Input symbol get substring between symbol and last ":"
     * @return Substring in text or ""
     */
    public String getTextAt(String text, String indexSymbol){
        String tmp = "";
        // :
        if (indexSymbol.equals(":")){
            tmp =  text.substring(text.lastIndexOf(indexSymbol) + 1);
        }
        // -
        else {
            try {
                tmp =  text.substring(text.lastIndexOf(indexSymbol) + 1, text.lastIndexOf(":"));
            } catch (Exception e) {
                System.out.println("NO <:> or <(> or no text between <( :>"); //NOSONAR
                System.out.println(e.getMessage()); //NOSONAR
            }
        }

        return tmp;
    }

    /** Get text between two substring
     * @param text search in text
     * @param indexSymbol1 substring before text you want to find
     * @param indexSymbol2 substring after text you want to find
     * @return "" if failed or text
     */
    public String getTextBeforeAndAfterWord(String text, String indexSymbol1, String indexSymbol2){
        String tmp = "";

        try {
            tmp =  text.substring(text.lastIndexOf(indexSymbol1) + 1, text.lastIndexOf(indexSymbol2));
        } catch (Exception e) {
            System.out.println("NO: " + indexSymbol1 + " or " +  indexSymbol2 + " int text "); //NOSONAR
            System.out.println(e.getMessage()); //NOSONAR
        }

        return tmp;
    }
    /**
     * Check if date1 is bigger or equals date2
     * @param dateBig   Date 1: String with size 8. all numbers
     * @param dateSmall Date 2
     * @return true if date is same or bigger. false if smaller or date format incorrect
     */
    public boolean dateBiggerOrSame(String dateBig, String dateSmall) { // NOSONAR
        String onlyNumbers = "\\D+";
        if(dateBig.matches(onlyNumbers) || dateSmall.matches(onlyNumbers)){
            System.out.println("No numbers in date variable's ") ; //NOSONAR
            return false;
        }
        String dateB = dateBig.replaceAll(onlyNumbers, "");
        String dateS = dateSmall.replaceAll(onlyNumbers, "");

        if(dateB.length() != 8 || dateS.length() != 8 ){
            System.out.println("date variable is not length 8: yyyy,mm,dd") ; //NOSONAR
            return false;
        }

        return Integer.parseInt(dateB) >= Integer.parseInt(dateS) ;
    }

    /** Get number after last ":" from deviation table with SpecificValue/substring.
     * @param index for test class.
     * @param containsValue text in cell.
     * @return one number or -1 if no deviation table.
     */
    public Integer getTotal(String index, String containsValue){

        List<String> tmp = getNumberInTextAsString(index,containsValue, ":");
        if(tmp.size() == 1 || (!tmp.isEmpty() && containsValue.equals(TOTALT))){
            return Integer.parseInt(tmp.get(0));
        }
        else{
            // error
            System.out.println(index + " Has " + tmp.size() + " elements. Only TOTALT will get first element if several elements") ; //NOSONAR
        }
        return -1;
    }
    /** Get SystemID from deviation table with SpecificValue.
     * @param index for test class.
     * @param containsValue text in cell.
     * @return All system ID's or empty list
     */
    public List<String> getSystemID(String index, String containsValue){
        // kun en id = Ingen id
        List<String> total = new ArrayList<>();


        for(String i : getSpecificValue(index, containsValue)){
            if(i.contains(containsValue)){
                String tmp = i.substring(i.lastIndexOf(")") + 1);
                tmp = tmp.replace(":","");
                tmp = tmp.replace(",","");
                tmp = tmp.replaceFirst(" ", "");
                tmp = tmp.split(" ", 2)[0];


                if(!tmp.isEmpty() && !total.contains(tmp)){
                    total.add(tmp);
                }
            }
        }
        if(total.isEmpty()){
            System.out.println(index + " has no value with text " + containsValue); //NOSONAR
        }
        return total;
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
    private List<String> getAll () { // NOSONAR
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
     * @return all rows with containsValue or empty list.
     */
    public List<String> getSpecificValue(String index, String containsValue){
        List<String> htmlTable = new ArrayList<>();
        for(String i : getDataFromHtml(index)){
            if(i.contains(containsValue)){
                htmlTable.add(i);
            }
        }
        if (htmlTable.isEmpty()) {
            System.out.println(index + " Can't find deviation with: " + containsValue); //NOSONAR
        }
        return  htmlTable;
    }

    public List<String> getTableDataFromHtml(String index, int wordPosition) {

        List<String> htmlTable = new ArrayList<>();

        for(String t : getDataFromHtml(index)) {
            List<String> seperator = Arrays.asList(t.split("[ ]"));

            if(seperator.size() >= 4) {
                htmlTable.add(seperator.get(seperator.size()-wordPosition));
                htmlTable.add(seperator.get(seperator.size()-1));
            }
        }
        return (!htmlTable.isEmpty()) ? htmlTable : Collections.emptyList();
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
