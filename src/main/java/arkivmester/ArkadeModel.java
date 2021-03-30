package arkivmester;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Random;

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
        //firstLastRegistrering();
        kryssreferanser();
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
        List<String> testFilePath = Arrays.asList(
        "Arkaderapport-67a47ea4-68bc-4276-a599-22561e0c31df.html",
        "Arkaderapport-0439ba78-2381-430b-8f99-740f71846f1e.html",
        "Arkaderapport-4b24f025-3c3a-4dd6-a371-7dc1b9143452.html",
        "Arkaderapport-899ec389-1dc0-41d0-b6ca-15f27642511b.html",
        "Arkaderapport-7fc1fe22-d89b-42c9-aaec-5651beb0da0a.html",
        "Arkaderapport-ebc3f74b-4eb3-4358-a38f-46479cfb2feb.html",
        "arkaderapportrapport.html"
        );

        // Select random arkade html for testing
        filePath = "../Input/" + testFilePath.get(new Random().nextInt(testFilePath.size()));
        System.out.println(filePath); //NOSONAR

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
    public void firstLastRegistrering(){

        String indexN527 = "N5.27";

        // get all ID. allways one ID in N5.27
        List<String> id = getSystemID(indexN527, "systemID");

        for (int i = 0; i < id.size(); i++) {
            // One ID
            List<String> curID = getSpecificValue(indexN527, id.get(i));

            List<String> curN511;
            List<String> curN518;

            // if more than 2 ID = "-". else = ""
            String servalIDs;
            String withID;
            // If more than 1 ID. Search for ID
            if (id.size() >= 2){
                servalIDs = "-";
                withID = id.get(i);
            }
            // If 1 ID, don't search for ID in text
            else {
                servalIDs = "";
                withID = ":";

            }
            curN511 = getSpecificValue("N5.11", withID);
            curN518 = getSpecificValue("N5.18", withID);
            curN511 = getTextBetweenWords(curN511,servalIDs,":");
            curN518 = getTextBetweenWords(curN518, servalIDs,":");

            // Year in N5.11 AND N5.18
            List<Integer> curN511Num = onlyKeepNumbers(curN511);
            List<Integer> curN518Num = onlyKeepNumbers(curN518);

            // Get Start AND End date N5.27
            List<String> curN527 = getSpecificValue(indexN527, id.get(i));
            String startDate = "";
            String endDate = "";
            if(!getSpecificValueInList(curN527, "Første registrering").isEmpty()){
                startDate = getSpecificValueInList(curN527, "Første registrering").get(0);
                startDate = getTextAt(startDate,":");
                if (startDate.length() > 4 && !startDate.substring(startDate.length() - 4).matches("\\D+"))  {
                    // gj;r om til int. check //D p[ begge
                    // gj;r om til Funksjon. med 2017-01.2018 og -2017?
                    startDate = startDate.substring(startDate.length() - 4);
                }
                else {startDate = ""; }
            }
            if(!getSpecificValueInList(curN527, "Siste registrering").isEmpty()){
                endDate = getSpecificValueInList(curN527, "Siste registrering").get(0);
                endDate = getTextAt(endDate,":");
                if (endDate.length() > 4){
                    endDate = endDate.substring(endDate.length() - 4);
                }
                else {endDate = ""; }
            }

            // Check if N5.11 AND N5.18 is between N5.27 start AND end Date
            if(!startDate.isEmpty() && !endDate.isEmpty()){
                for (int j = 0; j < curN511Num.size(); j++) {
                    //if(curN5_11Num >= startDate && curN5_11Num <= endDate){
                    // ta imot int (start,slutt)
                    // Mangler å sjekke med siste variabel?
                }
            }
            else{
                System.out.println("N5.27, ID:" + " StarDate/EndDate mangler eller er feil " + // NOSONAR
                        "StartDate: " + startDate + " EndDate: " + endDate + " SystemID: " + id );
            }



            // return valg og verdier for alle ID'ene

            System.out.println(curID);  // NOSONAR
            System.out.println(startDate);  // NOSONAR
            System.out.println(endDate);  // NOSONAR
            System.out.println(curN511Num);  // NOSONAR
            System.out.println(curN518Num);  // NOSONAR

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

    /** 3.3.4, N5.37. Get totalt(klasser, mapper, basisregistreringer)
     * @return List(0-id.size) of List(klasser, mapper, basisregistreringer)
     *    eg. for id Nr 22: list.get(22) = {antall klasser, antall mapper, antall basisregistreringer}.
     */
    public List <List<Integer>> kryssreferanser(){

        String index = "N5.37";

        List <List<Integer>> kryssreferanserNumbers = new ArrayList<>();

        List <String> id = getSystemID(index, "systemID");

        // If no ID. Add "NONE" ID
        if(id.isEmpty()){
            id.add("Antall");
        }
        // Search with ID
        for (int i = 0; i < id.size(); i++) {
            List<String> kryssreferanser = getSpecificValue(index, id.get(i));

            kryssreferanserNumbers.add(Arrays.asList(getOneElementInListAsInteger(kryssreferanser, "klasser"),
                    getOneElementInListAsInteger(kryssreferanser, "mapper"),
                    getOneElementInListAsInteger(kryssreferanser, "basisregistreringer")
            ));

            if(kryssreferanserNumbers.get(i).contains(-1)){
                System.out.println(index + " . -1 = Can't find value: (klasser, mapper, basisregistreringer):  " + //NOSONAR
                        kryssreferanserNumbers.get(i));
            }
        }
        return kryssreferanserNumbers;
    }

    // Function for all Chapters

    /**
     * Get one integer from list. False if  more elements with containsValue
     * @param listString string list
     * @param containsValue get element with value
     * @return One element with containsValue. else -1
     */
    public Integer getOneElementInListAsInteger(List<String> listString, String containsValue){
        String oneElement = getOneElementInListAsString(listString, containsValue);
        oneElement = getTextAt(oneElement, ":");
        if(oneElement.equals("")){
            return -1;
        }
        else{
            return sumStringListWithOnlyNumbers(Arrays.asList(oneElement));
        }
    }

    /**
     * Get one String from list. False if more elements with containsValue
     * @param listString string list
     * @param containsValue get element with value
     * @return One element with containsValue. else ""
     */
    public String getOneElementInListAsString(List<String> listString, String containsValue){
        List<String> textList = getSpecificValueInList(listString, containsValue);
        if(textList.size() != 1){
            System.out.println("   There are: " + textList.size() + " elements in list. Should be 1 element"); //NOSONAR
            return "";
        }
        return textList.get(0);
    }

    /** Get sum of all numbers in List. List NEEDS to have ONLY NUMBERS you want to sum.
     * @param numberList String List with number.
     * @return Sum of all number in string OR -1 if no number in list.
     */
    public Integer sumStringListWithOnlyNumbers(List<String> numberList){

        int number = 0;
        boolean numberInList = false;

        for (String numberStr: numberList){
            // Check no numbers: error
            if (numberStr.isEmpty() || numberStr.matches("\\D+")) {
                System.out.println("   List has element without number in it"); //NOSONAR
                return -1;
            }
            // Remove everything except numbers in String
            numberStr = numberStr.replaceAll("\\D+", "");
            number += Integer.parseInt(numberStr);
            numberInList = true;
        }
        // List has at least one number
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
            // Check Error:  has not ":"
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
     * indexSymbol ":" = substring after last ":".
     * NOT ":" = "Other symbol" Substring ":"
     * "" = everthing before ":"
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
     * @param listText Search in this list.
     * @param indexSymbol1 substring before text you want to find.
     * @param indexSymbol2 substring after text you want to find.
     * @return "" if failed or text.
     */
    public List<String> getTextBetweenWords(List<String> listText, String indexSymbol1, String indexSymbol2){

        List<String> tmp = new ArrayList<>();

        for (String s : listText) {
            if (indexSymbol1.equals("")) {
                try {
                    tmp.add(s.substring(0, s.lastIndexOf(indexSymbol2)));
                } catch (Exception e) {
                    System.out.println("NO: " + indexSymbol2 + " int text "); //NOSONAR
                    System.out.println(e.getMessage()); //NOSONAR
                }
            } else {
                try {
                    tmp.add(s.substring(s.lastIndexOf(indexSymbol1) + 1,
                            s.lastIndexOf(indexSymbol2)));
                } catch (Exception e) {
                    System.out.println("NO: " + indexSymbol1 + " or " + indexSymbol2 + " int text "); //NOSONAR
                    System.out.println(e.getMessage()); //NOSONAR
                }
            }

        }
        return tmp;
    }

    /**
     * Make number in String to Integer.
     * @param listText Text with numbers in it.
     * @return String as Integer or -1.
     */
    public List<Integer> onlyKeepNumbers(List<String> listText) { // NOSONAR

        List<Integer> tmp = new ArrayList<>();

        for (String s : listText) {
            String onlyNumbers = "\\D+";
            if (s.matches(onlyNumbers)) {
                System.out.println("No numbers in date variable's "); //NOSONAR
            } else {
                String number = s.replaceAll(onlyNumbers, "");
                tmp.add(Integer.parseInt(number));
            }
        }
        return tmp;
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

    /**
     * Only keep elements in String list with containsValue
     * @param indexlist List with string elements
     * @param containsValue Look for value in elements
     * @return "" if empty list.
     */
    public List<String> getSpecificValueInList(List<String> indexlist, String containsValue){
        List<String> htmlTable = new ArrayList<>();
        for(String i : indexlist){
            if(i.contains(containsValue)){
                htmlTable.add(i);
            }
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
