package arkivmester;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.List;

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

    // Arkade report: Table "Totalt:".
    static final String TOTALT = "Totalt";

    ArkadeModel(){

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

    /** Chapter 3.1.2 Get number of deviation in arkade Report.
     */
    public Integer getNumberOfDeviation(){
        return getOneElementInListAsInteger(getFromSummary("Antall avvik funnet", true), "");
    }

    /** Chapter 3.1.14 N5.27
     * @param arkivdelStartYear get systemID, startYear, endYear from Xquery.
     * @param output set text into docx.
     * @return What text to write out to docx.
     */
    public Integer firstLastReg(List<String> arkivdelStartYear, List<String> output){

        int choose = 1;

        String indexN527 = "N5.27";

        List<String> allID = getSystemID(indexN527, "systemID");

        // for every systemID
        for (String id : allID) {
            Integer firstReg = getYearFromString(
                    getOneElementInListAsString(getSpecificValue(indexN527,"F??rste registrering"),id), false);
            Integer lastReg = getYearFromString(
                    getOneElementInListAsString(getSpecificValue(indexN527,"Siste registrering"),id), false);

            List<Integer> n511 = new ArrayList<>(Collections.emptyList());
            List<Integer> n518 = new ArrayList<>(Collections.emptyList());

            String withID = id;
            String servalIDs = "-";

            // Number of systemID < 2; = No systemID ref in arkade N5.11 and N5.18.
            if(allID.size() < 2){
                // No SystemID in N5.11 and N5.18
                withID = ":";
                servalIDs = "";
            }

            for(String curN511 : getTextBetweenWords(getSpecificValue("N5.11", withID), servalIDs,":")){
                n511.add(getYearFromString(curN511, false));
            }
            for(String curN518 : getTextBetweenWords(getSpecificValue("N5.18", withID), servalIDs,":")){
                n518.add(getYearFromString(curN518, false));
            }
            // ---------- Chapter testing start here ---------------
            for(Integer curN511: n511){
                if(firstReg > curN511 || lastReg < curN511){
                    System.out.println(indexN527 + " F??rste registrering or Siste registrering: " + // NOSONAR
                            "is bigger or smaller than one element in N5:11: " + firstReg + " " + lastReg + " " + curN511);
                    return 0;
                }
            }
            for(Integer curN518: n518){
                if(firstReg > curN518 || lastReg < curN518){
                    System.out.println(indexN527 + " F??rste registrering or Siste registrering: " + // NOSONAR
                            "is bigger or smaller than one element in N5:18" + firstReg + " " + lastReg + " " + curN518);
                    return 0;
                }
            }

            for (String s : arkivdelStartYear) {

                // Xquery. {systemID, StartDate, EndDate}.
                List<String> arkivNew = Arrays.asList(s.split(";", 3));


                if (id.contains(arkivNew.get(0))) {
                    if (arkivNew.size() == 3) {
                        if (firstReg < getYearFromString(arkivNew.get(1), true) ||
                                lastReg > getYearFromString(arkivNew.get(2), true)) {
                            System.out.println(indexN527 + " F??rste registrering is not within arkiv start and end date"); // NOSONAR
                            System.out.println(arkivNew); // NOSONAR
                            return 0;
                        } else if (firstReg > getYearFromString(arkivNew.get(1), true) + 3) {
                            output.add(Integer.toString(firstReg));
                            output.add(Integer.toString(getYearFromString(arkivNew.get(1), true)));

                            choose = 2;

                        }
                    } else {
                        System.out.println("Missing one of systemID/opprettetDato/avsluttetDato in arkivdel"); // NOSONAR
                        return 0;
                    }
                }
            }
        }


        return choose;
    }

    /**
     * Get all dates outside arkivperiode.
     * @param arkivdelStartYear Xqueries arkivperiode dates.
     * @param regDato Xqueries register dates.
     * @return Dates outside of arkivperiode.
     */
    public List<String> registratorDates(List<String> arkivdelStartYear, List<String> regDato){

        List<String> datesAfter = new ArrayList<>();


        for (String arkivdelYear : arkivdelStartYear){

            String firstReg = arkivdelYear.split(";", 3)[1];
            String lastReg = arkivdelYear.split(";", 3)[2];

            for (String dato : regDato){
                String checkDato = dato.split(";", 5)[2];

                if(getYearFromString(checkDato,true) < getYearFromString(firstReg, true)){
                    datesAfter.add(dato);
                }
                if(getYearFromString(checkDato,true) > getYearFromString(lastReg, true)){
                    datesAfter.add(dato);
                }
            }
        }
        return datesAfter;

    }

    /**
     * 3.1.16. Check for number of registrations with saksparter.
     * @return Comment on number of saksparter.
     */
    public List<Integer> saksparter(){
        List<Integer> list = new ArrayList<>();
        Integer saksparter = getTotal("N5.35", TOTALT);
        Integer antallReg = getTotal("N5.16", TOTALT);

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
     * 3.1.27: N5.47 Systemidentifikasjoner, (Arkade returns 0 here, not in use)N5.34 Dokumentfiler med referanse.
     * @param docxInput Values to put into docx text.
     * @return Number. What text form docx to output in report
     */
    public Integer systemidentifikasjonerForklaring(List<String> doukBes, List<String> klasser, List<String> docxInput){

        String empty = "empty";
        List<String> n547 = getTextBetweenWords(getSpecificValue("N5.47", "Ikke-unik ID"),"forekommer", "ganger");
        int totalSystemID = sumStringListWithOnlyNumbers(n547);

        int totalDouk = 0;

        // ANTALLBESKRIVELSER
        if(!doukBes.get(0).equals(empty)) {
            for(String douk : doukBes){
                totalDouk += getStringNumberAsInteger(douk.split(";", 2)[1]);
            }
            if(totalDouk == totalSystemID){
                docxInput.add(Integer.toString(totalSystemID));
                docxInput.add(Integer.toString(totalDouk));
                return 1;
            }
        }
        // Ingen feil
        if(totalDouk == 0 && totalSystemID == 0){
            return 0;
        }

        totalDouk = 0;

        // k-kode
        if(!klasser.get(0).equals(empty)) {
            for(String klasse : klasser){
                totalDouk += getStringNumberAsInteger(klasse.split(";", 2)[1]);
            }
            docxInput.add(Integer.toString(totalSystemID));
            docxInput.add(Integer.toString(totalDouk));
            return 2;
        }
        System.out.println("3.1.27 noe gikk galt.  N5.47 " + totalSystemID); //NOSONAR
        return 3;
    }

    // Function for all Chapters

    /**
     * Get one integer from list. False if  more elements with containsValue.
     * @param listString Get one element from this list.
     * @param containsValue Get element with this value.
     * @return One element with containsValue. else -1.
     */
    public Integer getOneElementInListAsInteger(List<String> listString, String containsValue){
        String oneElement = getOneElementInListAsString(listString, containsValue);
        oneElement = getTextAt(oneElement, ":");
        if(oneElement.equals("")){
            return -1;
        }
        else{
            return sumStringListWithOnlyNumbers(Collections.singletonList(oneElement));
        }
    }

    /**
     * Get one String from list. False if more elements with containsValue
     * @param listString Get one element from this list.
     * @param containsValue Get element with this value.
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
     * @return Sum of all number in string OR -1 if something went wrong.
     */
    public Integer sumStringListWithOnlyNumbers(List<String> numberList){

        int number = 0;

        if(numberList.isEmpty()){
            return 0;
        }

        for (String numberStr: numberList){
            // Check no numbers: error
            if (numberStr.isEmpty() || numberStr.matches("\\D+")) {
                System.out.println("   List has element without number in it"); //NOSONAR
                return -1;
            }
            // Remove everything except numbers in String
            numberStr = numberStr.replaceAll("\\D+", "");
            number += Integer.parseInt(numberStr);
        }

        return number;
    }

    /** Get Year from string.
     * @param year string with year. Need to have year at front or back
     * @param getYearAtBeginning GET: year at beginning of string. else year back of the string
     * @return year 4 numbers
     */
    public Integer getYearFromString(String year, boolean getYearAtBeginning){
        year = year.replace(" ", "");
        if(getYearAtBeginning ){
            if(year.length() >= 4){
                year = year.substring(0, 4);
                if(!year.matches("\\D+")){
                    return Integer.parseInt(year);
                }
            }
        }else{
            if(year.length() >= 4){
                year = year.substring(year.length()-4);
                if(!year.matches("\\D+")){
                    return Integer.parseInt(year);
                }
            }
        }
        return -1;
    }

    /**
     * Makes string to Integer.
     * @param number string with number in it.
     * @return number string as integer.
     */
    public Integer getStringNumberAsInteger(String number){
        String numberCheck = "\\D+";
        if(number.isEmpty()){
            return -1;
        }
        number = number.replace(" ", "");
        if(!number.matches(numberCheck)){
            number = number.replaceAll(numberCheck, "");
            return Integer.parseInt(number);
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
     * Get substring in text. 3 options: see for more info @param indexSymbol.
     * @param text Gets substring from text.
     * @param indexSymbol
     *      Get substring between Symbol and last ":".
     *      If indexSymbol = ":". THEN get substring after last ":".
     *      If indexSymbol = "". THEN get everything before ":"
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
     * @return text OR "" if something went wrong.
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
     * Get every table: N5.**.
     */
    public List<String> getAll () {
        List<String> htmlTable = new ArrayList<>();
        // Get deviation for every id
        for (String index : getAllIDs()){
            htmlTable.addAll(getDataFromHtml(index));
        }
        return htmlTable;
    }

    /**
     * Get all IDs from arkade Testreport: N5.**.
     * @return List of deviation IDs.
     */
    public List<String> getAllIDs () {
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
                String tmp = i.substring(i.lastIndexOf(")") + 2);
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
        else if (tmp.isEmpty()){
            System.out.println("   " + index + " Has  0 elements") ; //NOSONAR
        }
        else{
            System.out.println("   " + index + " Has " + tmp.size() + " elements. Only TOTALT will get first element if several elements") ; //NOSONAR

        }
        return -1;
    }

    /**
     * Only keep elements in String list with containsValue.
     * @param indexlist List with string elements.
     * @param containsValue Look for value in elements.
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
     * Get all cells in summary table with containsValue
     * @param containsValue cell has substring containsValue
     * @param getSecondCell Only get cell after cell with containValue
     * @return All Cells with containsValue OR All cells after the one with containsValue
     */
    public List<String> getFromSummary(String containsValue, boolean getSecondCell){

        // reset list
        List<String> htmlTable = new ArrayList<>();

        Document doc = Jsoup.parse(htmlRawText.toString());

        if(doc.getElementsByClass("jumbotron") == null) {
            System.out.println("No jumbotron Class "); //NOSONAR
            return htmlTable;
        }
        Elements elements = doc.getElementsByClass("jumbotron");
        org.jsoup.select.Elements rows = elements.select("tr");
        getCellsInTable(htmlTable, rows);

        List<String> getTable = new ArrayList<>();

        for (int i = 0; i < htmlTable.size(); i++){
            if(htmlTable.get(i).contains(containsValue)){
                if(!getSecondCell){
                    getTable.add(htmlTable.get(i));
                }
                else if(htmlTable.size() > i+1){
                    getTable.add(htmlTable.get(i + 1));
                }
                else{
                    System.out.println("No cell after: " + containsValue); //NOSONAR
                }
            }
        }

        return getTable;
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
     *      Example: "Location", "message", "Location", "message" etc.
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

        getCellsInTable(htmlTable, rows);

        return htmlTable;
    }

    /**
     *  Get cells from deviation table.
     * @param htmlTable Store html text in this list.
     * @param rows Html elements. Where it will get the tables from.
     */
    public void getCellsInTable(List<String> htmlTable, Elements rows){

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
    }
}
