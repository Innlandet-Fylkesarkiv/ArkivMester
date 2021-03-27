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
    // Arkade html testreport path
    String filePath;

    // Holds text from arkade testreport html as string
    StringBuilder htmlRawText = new StringBuilder();

    static final String TOTALT = "Totalt";

    ArkadeModel(){
        readHtmlFileFromTestFolder();
        firstLastRegistrering();
    }

    /**
     * Get first file in folder(testreport html) in Arkade/Output folder as a string.
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
     * Only for testing. ONLY FUNCTION: Get html before testing.
     * Reads html file at "filePath" defined in this function.
     * Does not interfere with main program.
     * This function can be removed at anytime.
     */
    public void readHtmlFileFromTestFolder(){

        // test html file at   ../Input/FileName,
        // Arkaderapport-67a47ea4-68bc-4276-a599-22561e0c31df.html,
        // Arkaderapport-0439ba78-2381-430b-8f99-740f71846f1e.html,
        // Arkaderapport-4b24f025-3c3a-4dd6-a371-7dc1b9143452.html
        // Arkaderapport-899ec389-1dc0-41d0-b6ca-15f27642511b.html
        // Arkaderapport-7fc1fe22-d89b-42c9-aaec-5651beb0da0a.html
        // Arkaderapport-ebc3f74b-4eb3-4358-a38f-46479cfb2feb.html
        filePath = "../Input/Arkaderapport-0439ba78-2381-430b-8f99-740f71846f1e.html";

        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {

            String val;
            while ((val = br.readLine()) != null) {
                htmlRawText.append(val);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage()); //NOSONAR
        }
    }

    // Chapters

    /**
     * Get arkade version. Chapter 3.1
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
     * Not done, waiting for update. 3.1.14 and 3.1.31
     * Get id from N5.27 than get arkvidel start and end date in N5.27
     * Not done: Loop through all id in N5.11 and N5.18. and compare start and end dat
     * N5.11 and N5.18 dates needs too be between star and end date from N5.27
     * 3.1.14 and 3.1.31
     *
     */
    public String firstLastRegistrering() { // NOSONAR

        // 60 Idw, 27 IDs, 11 18 har id'er p[ orginal arkade html
        // 11 only year: eg. id ... - 2017:1 OR 2017:1

        String tmp = ""; // remove this
        String kap527 = "N5.27";

        // Get All SystemID
        List<String> id = getSystemID(kap527, "systemID");


        for (int i = 0; i < id.size(); i++) {

            String tmpID = id.get(i);
            if(id.size() <= 1){
                tmpID = ":";
            }
            // One ID at a time
            List<String> kap5_27OneID = getSpecificValue(kap527, tmpID);

            String start = "";
            String end = "";
            // Start and end date for that one ID
            for (int j = 0; j < kap5_27OneID.size(); j++) {
                if(kap5_27OneID.get(j).contains("Første registrering")){
                    start = getNumberInTextAsString(kap527, tmpID,":").get(j);
                    if (start.length() > 4)
                    {
                        start = start.substring(start.length() - 4);
                    }
                }
                if(kap5_27OneID.get(j).contains("Siste registrering")){
                    end = getNumberInTextAsString(kap527, tmpID,":").get(j);
                    if (end.length() > 4)
                    {
                        end = end.substring(end.length() - 4);
                    }
                }
            }
            List<String> x = new ArrayList<>();
            // More than 1 ID
            if(id.size() >= 2){
                x = getNumberInTextAsString("N5.11", ":","-");
            }
            else{
                // if no ID
            }
            for (int j = 0; j < x.size(); j++) {
                if (onlyKeepNumbers(x.get(j)) < onlyKeepNumbers(start) ||
                        onlyKeepNumbers(x.get(j)) > onlyKeepNumbers(end)){
                    // error
                    System.out.println(x.get(j) +" date not between StartDate: " + start + " and EndDate: " + end);
                    // this ID or no ID date is wrong -1
                }
            }



            System.out.println("S " + start + " E " + end);
            System.out.println("x " + x);
            //System.out.println("id nr " + id.size());

            tmp = end + start; // remove this
        }

        return tmp;
    }

    /**
     * 3.1.27: N5.47 Systemidentifikasjoner, N5.34 Dokumentfiler med referanse.
     * @param docxInput Values to put in to docx text.
     * @return Number. What text to use in docx from chapters.
     */
    public Integer systemidentifikasjonerForklaring(List<String> docxInput){

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
    /**
     * 3.1.16. Check for number of registrations with saksparter.
     * @return Comment on number of saksparter.
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

    /**
     * 3.1.17. Get Merkader.
     * @return true if no merknader.
     */
    public boolean ingenMerknader(){
        int merknader = getTotal("N5.36", TOTALT);
        //Ingen merknader er registrert.
        return merknader <= 0;
    }

    // Function for all Chapters

    /**
     * Get sum of all numbers in List.
     * @param numberList String List with number.
     * @return Sum of all number in string OR -1 if no number in list.
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
     * Get Date as String
     * @param index N5.**.
     * @param containsValue Find string with substring.
     * @param indexSymbol Get text between Symbol and last ":" OR ":" gets text after last ":".
     * @return emptyList or String list with numbers.
     */
    public List<String> getNumberInTextAsString(String index, String containsValue,  String indexSymbol){

        String onlyNumbers = "\\D+";

        List<String> allNumbers = getSpecificValue(index, containsValue);
        List<String> total = new ArrayList<>();

        for (String allNumber : allNumbers) {
            // has not :
            if (!allNumber.contains(":")) {
                System.out.println("LOOP: " + index + " value with " + containsValue + " has no \":\" "); //NOSONAR
            }
            else{
                String tmp = getTextAt(allNumber, indexSymbol);

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
     * Get text between "indexSymbol" and last ":". OR If ":" gets text after last ":".
     * @param text Gets substring form text.
     * @param indexSymbol Input ":" get substring after last ":" OR Input symbol get substring between symbol and last ":".
     * @return Substring in text or "".
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

    /** Get text between two substring.
     * @param text search in text.
     * @param indexSymbol1 substring before text you want to find.
     * @param indexSymbol2 substring after text you want to find.
     * @return "" if failed or text.
     */
    public String getTextBetweenWords(String text, String indexSymbol1, String indexSymbol2){
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
     * Make number in String to Integer.
     * @param text Text with numbers in it.
     * @return String as Integer or -1.
     */
    public Integer onlyKeepNumbers(String text) { // NOSONAR
        String onlyNumbers = "\\D+";
        if(text.matches(onlyNumbers)){
            System.out.println("No numbers in date variable's ") ; //NOSONAR
            return -1;
        }
        String number = text.replaceAll(onlyNumbers, "");


        return Integer.parseInt(number) ;
    }

    /**
     * Get all IDs "getAllIDs()", Get deviation for every ID.
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
     * Get all SystemID from deviation table with SpecificValue.
     * @param index for test class.
     * @param containsValue text in cell.
     * @return All system ID's or empty list.
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
     * Get number after last ":" from deviation table with SpecificValue/substring.
     * @param index for test class.
     * @param containsValue text in cell. If TOTALT will only get first value in deviation table.
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

    /**
     * Get specific value from deviation table.
     * @param index for test class.
     * @param containsValue cell contains value.
     * @return all cells with containsValue or empty list.
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
     *      eg. "Location", "message", "Location", etc.
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
