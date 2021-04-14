package arkivmester;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for handling report document configurations.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ReportModel {
    ArkadeModel arkadeModel;
    Properties prop;
    Map<String, List<String>> xqueriesMap;

    /**
     * The enum for the different kinds of text types that the program will deal with
     */
    enum TextStyle {
        INPUT,
        PARAGRAPH,
        TABLE,
        GRAPH
    }

    /**
     * List of the attachments which will be printed in chapter 5.
     */
    ArrayList<String> attachments = new ArrayList<>();
    static final String EMPTY = "empty";
    /**
     * Used for getTotal function in ARkadeModel
     */
    static final String TOTAL = "Totalt";
    static final String TABLESPLIT = "[;:][ ]";


    XWPFDocument document;
    String chapterFolder = "/chapters/";
    String templateFile = "/Dokumentmal_fylkesarkivet_Noark5_testrapport.docx";

    static String notFoundField = "<Fant ikke verdi>";

    Map<List<Integer>, List<List<ChapterList>>> chapterList;
    private HeadersData headersData;

    private static final String FONT = "Roboto (Brødtekst)";

    public void init(Properties prop, Map<String, List<String>> map) {
        this.prop = prop;
        arkadeModel = new ArkadeModel();
        chapterList = new LinkedHashMap<>();
        headersData = new HeadersData();
        xqueriesMap = map;
    }

    /**
     * Class for storing input of each chapter section of the report.
     */
    public static class ChapterList {

        String regex = "[^a-zæøåA-ZÆØÅ ][A-ZÆØÅ]{3,}([ ][A-ZÆØÅ]{3,}){0,5}[^a-zæøåA-ZÆØÅ ]|[A-ZÆØÅ]{4,}";

        private List<String> result;
        private int tableCol;
        private final TextStyle type;
        private int cindex;
        private CTChart chart;
        private boolean cases;
        private boolean isVaried;

        /**
         * Initialize a default list of missing input.
         * @param input - the input text that are going to be put from either file or code
         * @param ts    - the type of text as an enum(PARAGRAPH, INPUT, TABLE)
         * @param col   - amount of coloums if it is a table
         * @param c     - if the program are going to write this part to document
         */
        ChapterList(List<String> input, TextStyle ts, int col, boolean c) {
            result = input;
            type = ts;
            cindex = 0;
            tableCol = col;
            cases = c;
        }

        ChapterList(CTChart chrt) {
            chart = chrt;
            type = TextStyle.GRAPH;
            tableCol = 0;
            cases = false;
        }

        /**
         * Insert input into object
         * @param input - input to replace the default one
         */
        public void insertInput(List<String> input) {
            result.subList(tableCol, result.size()).clear();
            result.addAll(input);
            cases = true;
        }

        /**
         * Insert input into object
         * @param input - input to replace the default one
         * @param matchRows -
         */
        public void insertTableInput(List<String> input, List<Integer> matchRows) {
            List<String> temp = new ArrayList<>(result.subList(0, tableCol));

            for(int row = 0; row*tableCol < input.size(); row++) {
                for(int resultrow = 1; resultrow*tableCol < result.size(); resultrow++) {
                    if(findMatchingRows(matchRows, row, resultrow, input, temp)) break;
                }
            }

            result = temp;
            cases = true;
        }

        public void insertGraphInput(List<String> input, int col, boolean vary) {
            result = input;
            tableCol = col;
            cases = true;
            isVaried = vary;
        }

        /**
         *
         * @param matchRows
         * @param inputR
         * @param resultR
         * @param input
         * @param temp
         * @return
         */
        public boolean findMatchingRows(List<Integer> matchRows, int inputR, int resultR, List<String> input, List<String> temp) {
            if(
                    matchRows.stream().allMatch( r ->
                            IntStream.range(resultR*tableCol, resultR*tableCol+tableCol)
                                    .filter(i -> i % tableCol == r)
                                    .mapToObj(result::get)
                                    .anyMatch(t -> t.contains(input.get(inputR *tableCol+r)))
                    )
            ) {
                for(int cell = 0; cell < tableCol; cell++) {
                    if ((!input.get(inputR * tableCol + cell).equals(" "))) {
                        temp.add(input.get(inputR * tableCol + cell));
                    } else {
                        temp.add(result.get(resultR * tableCol + cell));
                    }
                }
                return true;
            }
            return false;
        }

        /**
         * Look for text with ALLCAPS or "ALL CAPS INSIDE QUOTATION" and replace them with proper input
         * @param input - input to replace
         * @return the rest of the input that got not used this Chapter object
         */
        public List<String> updateText(List<String> input) {
            int index = 0;

            if(!input.isEmpty()) {
                for(int i = 0; i < result.size(); i++) {
                    String s = result.get(i);
                    Pattern pat = Pattern.compile(regex);
                    Matcher m = pat.matcher(s);
                    while (m.find() && index != input.size()) {
                        String word = m.group();
                        s = s.replace(word, input.get(index));
                        result.set(i, s);
                        index = clamp(index+1, input.size());
                    }
                }
            }
            cases = true;
            return input.subList(index, input.size());
        }

        /**
         * If chapter number is correct, set table as input to chapter-section.
         * @return current iterated value from input in class
         */
        public String currentItem() {
            if(result.isEmpty()) {
                return "";
            }

            int size = result.size();

            String temp = result.get(cindex);
            cindex = clamp(cindex+1, (size)-1);

            return temp;
        }

        /**
         * If chapter number is correct, set table as input to chapter-section.
         * @return enum of the type from class (INPUT, PARAGRAPH, TABLE)
         */
        public TextStyle getType() {
            return type;
        }

        /**
         * Will not clamp the max value so it does not go "out of bounds".
         * @param val - value to compare with the max
         * @param max - value to limit 'val' from going out of bounds
         * @return the value that is lowest between these parameters
         */
        private int  clamp(int val, int max) {
            return Math.min(val, max);
        }

        /**
         * Prints header number and text from data stored.
         */
        public void getText() {
            if(cases) {
                for (String strings : result) {
                    System.out.print(strings + " ");      // NOSONAR
                }
            }
        }
    }

    /**
     * Class for handling headers that are fetched from document.
     */
    public static class HeadersData {
        private final List<String> name;
        private final Map<String, Integer> headerMap;

        /**
         * Initialize empty header and value.
         */
        HeadersData() {
            name = new ArrayList<>();
            headerMap = new LinkedHashMap<>();
        }

        /**
         * Will store header name and increment value when there exist another name,
         * otherwise will add the new name to a list.
         * @param other - header name to compare with
         */
        public void compareName(String other) {

            boolean hit = false;

            if(headerMap.computeIfPresent(other, (k, v) -> v+1) != null) {
                hit = true;
                int temp = name.size()-1;
                String currentName = name.get(temp);
                while(!other.equals(currentName)) {
                    headerMap.put(currentName, 0);
                    currentName = name.get(--temp);
                }
            }

            while(name.size() > headerMap.size()) {
                name.remove(name.size()-1);
            }

            if(!hit) {
                headerMap.put(other, 1);
                name.add(other);
            }
        }

        /**
         * Get value from number-text.
         * @return list of integers for ex. (1, 1, 1)
         */
        public List<Integer> getNumbering() {
            return headerMap.values().stream().filter(i -> i > 0).collect(Collectors.toList());
        }
    }

    /**
     * Writes and prints document to file.
     */
    public void makeReport() {
        writeReportDocument();     // editing
        printReportToFile(prop);
    }

    /**
     * Try to fetch report template, and if there are no IO problems, it will be stored.
     * @param filepath - filepath to try to get Document from
     * @return XWPFDocument if file is found, or null if no matching filepath was found
     */
    public XWPFDocument getDocumentFile(String filepath) {
        try (
                InputStream fis = getClass().getResourceAsStream(filepath)
        ) {
            return new XWPFDocument(fis);
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Create a list containing every chapter.
     */
    private void setUpAllInputChapters() {
        Iterator<IBodyElement> bodyElementIterator = document.getBodyElementsIterator();

        while(bodyElementIterator.hasNext()) {
            IBodyElement element = bodyElementIterator.next();
            if(element instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph)element;

                if(findNewHeader(p)) {
                    List<Integer> h = headersData.getNumbering();
                    getOutputValuesFromFile(h);
                }
            }
        }
    }

    /**
     * Checks if paragraph is a Header.
     * @param p - paragraph in document to look into
     * @return - true if paragraph style of header is found, and false if not found
     */
    private boolean findNewHeader(XWPFParagraph p) {
        XWPFStyles styles = document.getStyles();

        if(p.getStyle() != null) {

            XWPFStyle style = styles.getStyle(p.getStyleID());

            if(style.getStyleId().contains("Overskrift") || style.getStyleId().contains("Heading")) {
                headersData.compareName(style.getName());
                return true;
            }
        }
        return false;
    }

    /**
     * Replace every missing input with input fetched from program.
     */
    public void writeReportDocument() {

        writeInputText();

        headersData = new HeadersData();

        List<List<ChapterList>> currentChapterInput = new ArrayList<>();

        for(int i = 0; i < document.getParagraphs().size(); i++) {
            XWPFParagraph p = document.getParagraphs().get(i);

            if(findNewHeader(p)) {
                currentChapterInput = chapterList.get(headersData.getNumbering());

                if(i + 1 < document.getParagraphs().size()) {
                    addToDocument(currentChapterInput, document.getParagraphs().get(i+1));
                }
            }

            if(!currentChapterInput.isEmpty()) editDocument(p, currentChapterInput.get(0));
        }
    }

    /**
     * Writes to console every chapter, its various cases and text input.
     */
    private void writeInputText() {
        int ind;
        for(Map.Entry<List<Integer>, List<List<ChapterList>>> entry : chapterList.entrySet()) {
            System.out.println(entry.getKey());                                         // NOSONAR
            ind = 0;
            for (List<ChapterList> chapters : entry.getValue()) {
                System.out.print("\t" + ind++ + ": ");                                  // NOSONAR
                for(ChapterList chap : chapters) {
                    chap.getText();
                }
                System.out.print("\n");                                                 // NOSONAR
            }
        }
    }

    /**
     * Looks for if contents in Chapterlist has either paragraph or table.
     * @param currentChapterInput - current list of chapterlist
     * @param p - paragraph text from document to set text into
     */
    private void addToDocument(List<List<ChapterList>> currentChapterInput, XWPFParagraph p) {
        for (List<ChapterList> chapters : currentChapterInput) {
            if(!chapters.isEmpty() && chapters.get(0).cases) {
                for(ChapterList chap : chapters) {
                    switch(chap.getType()) {
                        case PARAGRAPH:
                            insertParagraphToDocument(chap.currentItem(), p);
                            break;
                        case TABLE:
                            insertTableToDocument(chap, p);
                            break;
                        case GRAPH:
                            insertGraphToDocument(chap, p);
                            break;
                        default:
                    }
                }
            }
        }
    }

    /**
     * Will look for input field in each paragraph and replace it with the ones from the list.
     * @param p - existing paragraph in document to check for "TO DO" text
     * @param cChapter - current chapterlist which is iterated
     */
    private void editDocument(XWPFParagraph p, List<ChapterList> cChapter) {
        for(XWPFRun r : p.getRuns()) {
            if(cChapter != null && r.getText(0) != null && r.getText(0).contains("TODO")) {
                for(ChapterList chapter : cChapter) {
                    if (chapter.getType().equals(TextStyle.INPUT)) {
                        insertInputToDocument(r.getText(0), chapter.currentItem(), r);
                    }
                }
            }
        }
    }
    //region Description

    /**
     * Inserts input text to document from chapterlist.
     * @param text - text to be replaced by input
     * @param input - text that will replace "TO DO" fields
     * @param r - Used for editing into a paragraph
     */
    private void insertInputToDocument(String text, String input, XWPFRun r) {
        text = text.replace("TODO", (!input.equals("") ? input : notFoundField));
        setRun(r, FONT , 11, false, text, false);
    }

    /**
     * Inserts paragraph text to document from chapterlist.
     * @param input - text to be inserted in document
     * @param p - paragraph text from document to set text into
     */
    private void insertParagraphToDocument(String input, XWPFParagraph p) {
        XmlCursor cursor = p.getCTP().newCursor();//this is the key!

        XWPFParagraph para = document.insertNewParagraph(cursor);

        setRun(para.createRun() , FONT , 11, false, (!input.equals("") ? input : notFoundField), true);
    }

    /**
     * Inserts table to document from chapterlist.
     * @param cChapter - current chapterlist which is iterated
     * @param p - paragraph text from document to create table in
     */
    private void insertTableToDocument(ChapterList cChapter, XWPFParagraph p) {
        XmlCursor cursor = p.getCTP().newCursor();//this is the key!

        XWPFTable table = document.insertNewTbl(cursor);
        table.removeRow(0);

        XWPFParagraph paragraph;

        XWPFTableRow tableOneRowVersion;

        for(int i = 0; i < cChapter.result.size(); i += cChapter.tableCol) {
            tableOneRowVersion = table.createRow();
            for(int j = 0; j < cChapter.tableCol; j++) {
                if(i == 0) {
                    tableOneRowVersion.addNewTableCell();
                }
                paragraph = tableOneRowVersion.getCell(j).addParagraph();
                setRun(
                        paragraph.createRun(),
                        FONT,
                        11,
                        (i == 0),
                        cChapter.currentItem(),
                        false
                );

                tableOneRowVersion.getCell(j).setWidth("5000");
            }
        }

        cursor = p.getCTP().newCursor();//this is the key!

        XWPFParagraph para = document.insertNewParagraph(cursor);

        setRun(para.createRun() , FONT , 11, false, "", false);

    }

    private void insertGraphToDocument(ChapterList cChapter, XWPFParagraph p) {

        int width = 16 * Units.EMU_PER_CENTIMETER;
        int height = 10 * Units.EMU_PER_CENTIMETER;

        if(cChapter.tableCol > 0) {

        XmlCursor cursor = p.getCTP().newCursor();//this is the key!

        XWPFParagraph para = document.insertNewParagraph(cursor);

        XWPFRun r = para.createRun();

            try {
                XWPFChart charttemp = document.createChart(r, width, height);
                CTChart ctChartTemp = charttemp.getCTChart();

                XSSFChart chart = barColumnChart(cChapter);

                ctChartTemp.set(chart.getCTChart());
            } catch(InvalidFormatException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    //region end

    /**
     * Print paragraph text with customized features like size and font to document.
     * @param run - Used for editing into a paragraph
     * @param font - font family
     * @param size - font size
     * @param bold - if text is meant to be bold or not
     * @param text - text that are to be put into the document
     * @param addBreak - if a breakpoint at the end is needed
     */
    public void setRun(XWPFRun run, String font, int size, boolean bold, String text, boolean addBreak) {
        run.setFontFamily(font);
        run.setFontSize(size);
        run.setText(text, 0);
        run.setBold(bold);
        if(addBreak) run.addBreak();
    }

    /**
     * Print the newly edited document to a new file.
     */
    public void printReportToFile(Properties prop) {
        try {
            FileOutputStream os = new FileOutputStream(prop.get("tempFolder") + "\\" + prop.get("currentArchive") + "\\Rapporter\\Testrapport.docx"); // #NOSONAR
            document.write(os);
            document.close();
            os.close();
            System.out.println("\nfile created successfully!");     // NOSONAR
            } catch (IOException e) {
            System.out.println(e.getMessage());                    // NOSONAR
        }
    }


    // -------------------------------- Customizing ChapterList from controller --------------------------------------

    /**
     * Add input to an already defined paragraph from chapter file.
     * @param h - The header number for the chapter
     * @param i - list of input strings
     * @param c - Which case to apply this to
     */
    public void setNewInput(List<Integer> h, List<String> i, int c) {
        for(ChapterList chap : chapterList.get(h).get(c) ) {
            if(chap.getType().equals(TextStyle.PARAGRAPH)) {
                i = chap.updateText(i);
            }
        }
    }

    /**
     * Add input from scratch into selected chapter.
     * @param h - The header number for the chapter
     * @param i - list of input strings
     */
    public void setNewInput(List<Integer> h, List<String> i) {
        chapterList.put(h, new ArrayList<>());
        chapterList.get(h).add(new ArrayList<>());
        chapterList.get(h).get(0).add(new ChapterList(i, TextStyle.INPUT, 0, true));
    }

    /**
     * Look for a table from ChapterList that isn't yet in use.
     * @param h - The header number for the chapter
     * @param t - Table content text that the user wants placed in table
     */
    public void insertTable(List<Integer> h, List<String> t) {
        for(List<ChapterList> chapters : chapterList.get(h)) {
            if(chapters.get(0).cases) {
                for(ChapterList chap : chapters) {
                    if(chap.getType() == TextStyle.TABLE && chap.result.get(chap.result.size()-1).equals("X")) {
                        chap.insertInput(t);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Look for a table from ChapterList that isn't yet in use.
     * @param h - The header number for the chapter
     * @param t - Table content text that the user wants placed in table
     */
    public void insertTable(List<Integer> h, List<String> t, List<Integer> matchRows) {
        for(List<ChapterList> chapters : chapterList.get(h)) {
            if(chapters.get(0).cases) {
                for(ChapterList chap : chapters) {
                    if(chap.getType() == TextStyle.TABLE && chap.result.get(chap.result.size()-1).equals("X")) {
                        chap.insertTableInput(t, matchRows);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Add a new paragraph from scratch into selected chapter.
     * @param h - The header number for the chapter
     * @param p - String text that the user wants to manually have put into the report
     */
    public void setNewParagraph(List<Integer> h, List<String> p, int c) {
        for(String s : p) {
            chapterList.get(h).get(c).add(new ChapterList(Arrays.asList(s), TextStyle.PARAGRAPH, 0, true));
        }
    }

    private void insertGraph(List<Integer> h, List<String> g, int col, int c, boolean vary) {
        for (ChapterList chap : chapterList.get(h).get(c)) {
            if(chap.getType() == TextStyle.GRAPH) {
                chap.insertGraphInput(g, col, vary);
            }
        }
    }

    /**
     * Gets all text of type 'paragraph' or 'table' after 'output' from each chapter file.
     * @param h - The header number for the chapter
     */
    private void getOutputValuesFromFile(List<Integer> h) {

        String chapterFile = formatChapterNumber(h);

        Iterator<IBodyElement> bodyList = getDocumentIterator(chapterFolder + chapterFile);

        chapterList.put(h, new ArrayList<>());

        boolean hit = false;
        while(bodyList.hasNext()) {
            IBodyElement body = bodyList.next();

            if(body instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph) body;

                if(hit && !p.getText().equals("")) {
                    createChapterParagraph(h, p);
                }
                if(p.getText().contains("Output")) {
                    hit = true;
                    chapterList.get(h).add(new ArrayList<>());
                }
            }

            if(body instanceof XWPFTable) {
                XWPFTable tab = (XWPFTable) body;

                createChapterTable(h, tab);
            }

        }

        List<CTChart> charts = getDocumentGraphs(chapterFolder + chapterFile);

        for(CTChart chart : charts) {
            createChapterGraph(h, chart);
        }

    }

    /**
     * Used for fetching iterator of elements in a document.
     * @param file file location
     * @return every element in document in order or if document is not found return empty iterator list
     */
    private Iterator<IBodyElement> getDocumentIterator(String file) {
        try {
            XWPFDocument doc = Objects.requireNonNull(getDocumentFile(file));
            return doc.getBodyElementsIterator();
        } catch(NullPointerException e) {
            return Collections.emptyIterator();
        }
    }

    private List<CTChart> getDocumentGraphs(String file) {

        List<CTChart> chartDatas = new ArrayList<>();

        try {
            XWPFDocument doc = Objects.requireNonNull(getDocumentFile(file));

            for(POIXMLDocumentPart part : doc.getRelations()) {
                if (part instanceof XWPFChart) {
                    chartDatas.add(((XWPFChart) part).getCTChart());
                }
            }

        } catch(NullPointerException ignored) {
            return Collections.emptyList();
        }

        return chartDatas;
    }

    public XSSFChart barColumnChart(ChapterList cinput) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            String sheetName = "CountryBarChart";

            XSSFSheet sheet = wb.createSheet(sheetName);

            List<String> categories = new ArrayList<>();

            String title = cinput.chart.getTitle().getTx().getRich().getPArray(0).getRArray(0).getT();

            int amountCat = ((cinput.result.size() / cinput.tableCol) - 1) / 2;

            for(int i = 0; i < amountCat; i++) {
                categories.add(cinput.result.get((i * 2) + 1));
            }

            // Create row and put some cells in it. Rows and cells are 0 based.
            Row row = sheet.createRow((short) 0);

            Cell cell;

            for(int i = 0; i < cinput.tableCol; i++) {
                cell = row.createCell((short) i);
                cell.setCellValue(cinput.result.get(i * (cinput.result.size() / cinput.tableCol)));
            }

            for(int i = 1; i <= amountCat; i++) {
                row = sheet.createRow((short) i);

                for(int j = 0; j < cinput.tableCol; j++) {
                    cell = row.createCell((short) j);
                    int tempNum = Integer.parseInt(cinput.result.get(j * (cinput.result.size() / cinput.tableCol) + (i * 2)));
                    cell.setCellValue(tempNum);
                }
            }

            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 4, 7, 20);

            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText(title);

            // Formats the title font
            chart.getCTChart().getTitle().getTx().getRich().getPArray(0).getRArray(0).getRPr().setB(false);
            chart.getCTChart().getTitle().getTx().getRich().getPArray(0).getRArray(0).getRPr().setSz(1400);
            chart.getCTChart().getTitle().getTx().getRich().getPArray(0).getRArray(0).getRPr().addNewLatin().setTypeface(FONT);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);

            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);

            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
            leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);

            XDDFDataSource<String> categoryFactory = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                    new CellRangeAddress(0, 0, 0, cinput.tableCol-1));

            List<XDDFNumericalDataSource<Double>> values = new ArrayList<>();
            for (int i = 1; i <= amountCat; i++) {
                values.add(XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                        new CellRangeAddress(i, i, 0, cinput.tableCol-1)));
            }


            XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);

            data.setVaryColors(cinput.isVaried);

            XDDFChartData.Series series;
            for (int i = 0; i < amountCat; i++) {
                series = data.addSeries(categoryFactory, values.get(i));
                series.setTitle(categories.get(i), null);
            }

            XDDFBarChartData bar = (XDDFBarChartData) data;
            bar.setBarDirection(BarDirection.COL);

            chart.plot(data);

            return chart;
        } catch (IOException e) {
            System.out.println(e.getMessage()); // NOSONAR
            return null;
        }

    }

    private void solidFillSeries(XDDFChartData data, int index, PresetColor color) {        // NOSONAR
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
        XDDFChartData.Series series = data.getSeries(index);
        XDDFShapeProperties properties = series.getShapeProperties();
        if (properties == null) {
            properties = new XDDFShapeProperties();
        }
        properties.setFillProperties(fill);
        series.setShapeProperties(properties);
    }

    /**
     * Will either add a paragraph to Chapterlist class,
     * Or create a new Chapterlist list if it detects string "AND/OR".
     * @param h - The header number for the chapter
     * @param p - The paragraph that are fetched from file
     */
    private void createChapterParagraph(List<Integer> h, XWPFParagraph p) {
        if(!p.getText().contains("AND/OR")) {
            chapterList.get(h).get(chapterList.get(h).size()-1).add(
                    new ChapterList(Arrays.asList(p.getText()), TextStyle.PARAGRAPH, 0, false));
        }
        else {
            chapterList.get(h).add(new ArrayList<>());
        }
    }

    /**
     * Will add a table to Chapterlist class with.
     * @param h - The header number for the chapter
     * @param t - The table that are fetched from file
     */
    private void createChapterTable(List<Integer> h, XWPFTable t) {

        List<String> tableHeader = new ArrayList<>();

        for(XWPFTableRow row : t.getRows()) {
            for(XWPFTableCell cell : row.getTableCells()) {
                tableHeader.add(cell.getText());
            }
        }

        tableHeader.add("X");

        chapterList.get(h).get(chapterList.get(h).size()-1).add(new ChapterList(
                tableHeader, TextStyle.TABLE, t.getRow(0).getTableCells().size(), false));

    }

    private void createChapterGraph(List<Integer> h, CTChart chart) {
        
        for(List<ChapterList> chapters : chapterList.get(h)) {
            if(chapters.isEmpty()) {
                chapters.add(new ChapterList(chart));
                return;
            }
        }

    }

    /**
     * Formats Chapter number given by converting it from List<Integer> into filename.
     * For example [1, 1] turns into "1.1".
     * @param h - The header number for the chapter
     * @return header number as a string
     */
    private String formatChapterNumber(List<Integer> h) {
        StringBuilder s = new StringBuilder();
        for(int i : h) {
            s.append(i).append(".");
        }
        s.append("docx");
        return s.toString();
    }

    /**
     * Fetch all data from report and set up all chapters so that input can be changed.
     */
    public void generateReport() { // NOSONAR
        document = getDocumentFile(templateFile);
        setUpAllInputChapters();

        if(arkadeModel.getFileToString(prop)){
            arkadeTestReport();
        }
        else {
            System.out.println("Can't get testreport html "); //NOSONAR
        }

        List<String> para;

        //Chapter 1.1

        //Chapter 3.1.5
        para = xqueriesMap.get("3.1.5_1");
        if(!para.get(0).equals(EMPTY)) {
            insertGraph(Arrays.asList(3, 1, 5), splitIntoTable(para), getRows(para), 0, false);
        }
        para = xqueriesMap.get("3.1.5_2");
        if(!para.get(0).equals(EMPTY)) {
            insertGraph(Arrays.asList(3, 1, 5), splitIntoTable(para), getRows(para), 1, false);
        }

        //Chapter 3.1.9
        para = xqueriesMap.get("3.1.9_1");
        if(!para.get(0).equals(EMPTY)) {
            insertGraph(Arrays.asList(3, 1, 9), splitIntoTable(para), getRows(para), 2, true);
        }

        List<String> temppara = arkadeModel.getSpecificValue("N5.18", " ");
        para = new ArrayList<>();
        for(String s : temppara) {
            List<String> t = Arrays.asList(s.split("[ ]"));
            para.add(t.get(0) + " registreringer; " + t.get(1));
        }
        if(!para.get(0).equals(EMPTY)) {
            insertGraph(Arrays.asList(3, 1, 9), splitIntoTable(para), getRows(para), 5, false);
        }

        //Chapter 3.1.11
        para = xqueriesMap.get("3.1.11");

        //Chapter 3.1.2
        // valideringAvXML(); NOSONAR

        if(para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 11), Collections.emptyList(), 0);
        } else {
            setNewInput(Arrays.asList(3, 1, 11), Collections.singletonList("" + para.size()), 1);
        }

        //Chapter 3.1.13
        para = xqueriesMap.get("3.1.13");

        if(para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 13), Collections.emptyList(), 0);
        } else if (!para.get(0).equals("utgår")) {

            if(para.size() > 25) {
                setNewInput(Arrays.asList(3, 1, 13),
                        Collections.singletonList(para.size() + ""), 3); // NOSONAR
                writeAttachments("3.1.13", para);
                attachments.add("\u2022 3.1.13.txt");
            }else {
                setNewInput(Arrays.asList(3, 1, 13),
                        Collections.singletonList(para.size() + ""), 1);
                insertTable(Arrays.asList(3, 1, 13), splitIntoTable(para));
            }

        } else {
            setNewInput(Arrays.asList(3, 1, 13), Collections.singletonList(para.size() + ""), 2);
            setNewParagraph(Arrays.asList(3, 1, 13), Collections.singletonList("afgdagfagfaga"), 2);
        }

        //Chapter 3.1.14 N5.27, N5.11, N5.18

        //Chapter 3.1.20
        para = xqueriesMap.get("3.1.20");
        if(para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 20), Collections.emptyList(), 0);
        } else {
            if(para.size() > 25) {
                setNewInput(Arrays.asList(3, 1, 20), Collections.singletonList("" + para.size()), 2);
                writeAttachments("3.1.20", para);
                attachments.add("\u2022 3.1.20.txt");
            }else {
                setNewInput(Arrays.asList(3, 1, 20), Collections.singletonList("" + para.size()), 1);
                insertTable(Arrays.asList(3, 1, 20), splitIntoTable(para));
            }
        }

        //Chapter 3.1.23
        skjerminger();

        //Chapter 3.2.1
        para = xqueriesMap.get("3.2.1_1");
        if(!para.get(0).equals(EMPTY)) {
            int total = (int) para.stream().filter(t -> t.contains("Arkivert") || t.contains("Avsluttet")).count();
            setNewInput(Arrays.asList(3, 2, 1), Arrays.asList(para.size() + "", total + ""), 0);
            insertTable(Arrays.asList(3, 2, 1), splitIntoTable(para));
        }
        para = splitIntoTable(xqueriesMap.get("3.2.1_2"));
        if(!para.get(0).equals(EMPTY)) {
            int total = IntStream.range(0, para.size()).filter(i -> i % 4 == 2)
                    .mapToObj(para::get).mapToInt(Integer::parseInt).sum();

            setNewInput(Arrays.asList(3, 2, 1), Arrays.asList(total + "", total + ""), 1);
            insertTable(Arrays.asList(3, 2, 1), para, Arrays.asList(0, 1));
        }
        para = splitIntoTable(xqueriesMap.get("3.2.1_3"));
        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 2, 1), Collections.singletonList(para.size() + ""), 4);
            insertTable(Arrays.asList(3, 2, 1), splitIntoTable(para));
        }

        //Chapter 3.3.1
        para = splitIntoTable(xqueriesMap.get("3.3.1"));
        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 3, 1), Collections.emptyList(), 0);
            insertTable(Arrays.asList(3, 3, 1), para);
        }

        //Chapter 3.3.2
        para = xqueriesMap.get("3.3.2_1");
        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 3, 2), Collections.emptyList(), 1);
            insertTable(Arrays.asList(3, 3, 2), splitIntoTable(para));
        }
        para = xqueriesMap.get("3.3.2_2");
        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 3, 2), Collections.emptyList(), 3);
            insertTable(Arrays.asList(3, 3, 2), splitIntoTable(para));
        }
        para = xqueriesMap.get("3.3.2_3");
        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 3, 2), Collections.emptyList(), 4);
            insertTable(Arrays.asList(3, 3, 2), splitIntoTable(para));
        }

        //Chapter 3.1.21
        para = xqueriesMap.get("3.1.21");

        if(para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 21), Collections.emptyList(), 0);
        }
        else {
            setNewInput(Arrays.asList(3, 1, 21), Collections.emptyList(),1);
        }

        //Chapter 3.1.26
        List<String> convertedTo = xqueriesMap.get("3.1.26_1");

        if(!convertedTo.isEmpty()) {

            List<String> convertedFrom = xqueriesMap.get("3.1.26_2");

            //Find amount of files - conversions for case 1.
            if (convertedFrom.size() == 1 && convertedFrom.contains("doc")) {
                setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 2);
            } else {
                setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 1);
            }
        }
        else {
            setNewInput(Arrays.asList(3, 1, 26), Collections.emptyList(), 3);
        }

        //Chapter 3.1.3
        List<String> parts = xqueriesMap.get("3.1.3");
        int arkivdeler = arkadeModel.getTotal("N5.05", TOTAL);
        if(arkivdeler > 1) {
            setNewInput(Arrays.asList(3, 1, 3), Collections.singletonList("" + arkivdeler), 1);
            insertTable(Arrays.asList(3, 1, 3), splitIntoTable(parts));
        }
        //Chapter 3.3.4
        List<String> crossReferences = xqueriesMap.get("3.3.4");

        int n537 = arkadeModel.getTotal("N5.37", TOTAL);

        setNewInput(Arrays.asList(3, 3, 4), Arrays.asList("" + n537, "" +  crossReferences.size()), 0);

        if (crossReferences.size() < 25){
            for (String crossReference : crossReferences) {
                setNewInput(Arrays.asList(3, 3, 4), Collections.singletonList("\u2022 " + crossReference), 1);
            }
        }
        else{
            setNewInput(Arrays.asList(3, 3, 4), Collections.singletonList("Over 25. Skriver til Vedlegg"), 1);
            writeAttachments("3.3.4", crossReferences);
        }



        //Chapter 3.3.6
        List<String> journals = xqueriesMap.get("3.3.6");
        if(!journals.get(0).equals(EMPTY)) {
            List<String> journal = splitIntoTable(journals);
            setNewInput(Arrays.asList(3, 3, 6), Collections.emptyList(), 0);
            insertTable(Arrays.asList(3, 3, 6), journal);
            int total = 0;
            for (int i = 1; i <= journal.size(); i += 2) {
                total += Integer.parseInt(journal.get(i));
                int amount = Integer.parseInt(journal.get(i));
                if (amount > (total / 100.0f * 90.0f)) {
                    setNewInput(Arrays.asList(3, 3, 6), Collections.emptyList(), 1);
                }
            }
        }else {
            setNewInput(Arrays.asList(3, 3, 6),Collections.emptyList(), 2);
        }

        //Chapter 3.3.7
        List<String> adminUnits = xqueriesMap.get("3.3.7");
        if(!adminUnits.get(0).equals(EMPTY)) {
            List<String> unit = splitIntoTable(adminUnits);
            setNewInput(Arrays.asList(3, 3, 7), Collections.emptyList(),0);
            insertTable(Arrays.asList(3, 3, 7), unit);
            int total = 0;
            for(int i = 1; i <= unit.size(); i+=2 ) {
                total += Integer.parseInt(unit.get(i));
                int amount = Integer.parseInt(unit.get(i));
                if(amount > (total / 100.0f * 90.0f)) {
                    setNewInput(Arrays.asList(3, 3, 7), Collections.emptyList(), 1);
                }
            }
        }else {
            setNewInput(Arrays.asList(3, 3, 7), Collections.emptyList(), 2);
        }

        //Chapter 3.3.3
        List<Integer> three = Arrays.asList(3, 3, 3);
        int total = arkadeModel.getTotal("N5.36", TOTAL);
        if(total > 0) {
            setNewInput(three, Collections.singletonList(total + ""), 0);

            para = xqueriesMap.get("3.3.3_1");
            insertTable(three, splitIntoTable(para));
            para = arkadeModel.getTableDataFromHtml("N5.36", 2);
            insertTable(three, para);
            para = xqueriesMap.get("3.3.3_2");
            insertTable(three, splitIntoTable(para));
        }
        
        //Chapter 3.2
        List<String> veraPDF = xqueriesMap.get("3.2_1");
        List<String> droid = xqueriesMap.get("3.2_2");
        if(!veraPDF.isEmpty() && !droid.isEmpty()) {

            int nonCompliant = Integer.parseInt(veraPDF.get(0));
            int failed = Integer.parseInt(veraPDF.get(1));

            setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 0);
            setNewInput(Arrays.asList(3, 2), droid,1);
            if (nonCompliant == 0 && failed == 0) {
                setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 2);
            } else if (failed == 0) {
                setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 3);
                setNewInput(Arrays.asList(3, 2), Collections.singletonList("" + nonCompliant), 5);
            } else if (nonCompliant == 0) {
                setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 3);
                setNewInput(Arrays.asList(3, 2), Collections.singletonList("" + nonCompliant), 4);
            }
        }

        //Chapter 3.1.7
        List<String> dirs = xqueriesMap.get("3.1.7_1");
        //System.out.println(dirs); // NOSONAR
        //System.out.println(dirs.get(0)); // NOSONAR
        if(dirs.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 7), Collections.emptyList(), 0);
        }
        else {
            setNewInput(Arrays.asList(3, 1, 7), Collections.singletonList("" + dirs.size()), 1);
            insertTable(Arrays.asList(3, 1, 7), splitIntoTable(dirs));
        }

        //Chapter 3.3.9
        para = xqueriesMap.get("3.3.9_1a");
        setNewInput(Arrays.asList(3, 3, 9), Collections.emptyList(), 0);

        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 3, 9), Collections.emptyList(), 1);
        }

        para = xqueriesMap.get("3.3.9_2a");
        List<String> para2 = xqueriesMap.get("3.3.9_2b");

        if(!para.get(0).equals(EMPTY) && !para2.get(0).equals(EMPTY)) {
            int antall = 0;
            List<String> extraNodes = new ArrayList<>();
            Collections.sort(para);
            Collections.sort(para2);

            for (String s : para2) {
                if (!para.contains(s)) {
                    extraNodes.add(s);
                    antall++;
                }
            }

            if(antall > 0 && antall <= 25) {
                setNewInput(Arrays.asList(3, 3, 9), Collections.singletonList("" + extraNodes.size()), 2);
                insertTable(Arrays.asList(3, 3, 9), splitIntoTable(extraNodes));
            }
            else if(antall > 25) {
                setNewInput(Arrays.asList(3, 3, 9), Collections.singletonList("" + extraNodes.size()), 3);
                writeAttachments("3.3.9_antall_ekstra_skjermingshjemmeler", extraNodes);
                attachments.add("\u2022 3.3.9_antall_ekstra_skjermingshjemmeler.txt");
            }
        }

        para = xqueriesMap.get("3.3.9_3a");
        para2 = xqueriesMap.get("3.3.9_3b");
        List<String> para3 = xqueriesMap.get("3.3.9_3c");

        if((para.get(0).equals(para2.get(0))) && (para.get(0).equals(para3.get(0)))) {
            setNewInput(Arrays.asList(3, 3, 9), Collections.singletonList("" + para.get(0)), 4);
        } else {
            setNewInput(Arrays.asList(3, 3, 9), Collections.emptyList(), 5);
        }

        //Chapter 5 - Attachments
        if(!attachments.isEmpty()) {
            setNewParagraph(Collections.singletonList(5), attachments, 0);
        }
    }

    /** Chapter 3.1.2 NOT DONE
     * Need arkdade report examples.
     */
    private void valideringAvXML(){ //NOSONAR
        String index ="N5.03";
        //Chapter 3.1.2
        Integer deviation = arkadeModel.getNumberOfDeviation();
        if(deviation == -1){
            System.out.println("Chapter 3.1.2: Can't find number of deviation"); //NOSONAR
        }
        if(deviation == 0){
            System.out.println("Zero deviation"); //NOSONAR
            setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 0);
        }
        // NOT DONE: Need examples
        else{
            // Kun Arkade rapporten, elementet tilknyttetDato rapportert som feil format.
            List<String> invalidDates = arkadeModel.getSpecificValue(index, "Date value.");
            if(!invalidDates.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 1);
                // Use setNewParagraph on invalidDates
            }

            //Arkade rapporten, elementet filstoerrelse mangler.
            List<String> filstoerrelse = arkadeModel.getSpecificValue(index, "filstoerrelse");
            if(!filstoerrelse.isEmpty()){
                // count(//dokumentobjekt[not (boolean(filstoerrelse))]) alle dokumentobject with out variable filstoerrelse
                // print out xqueriesMap.get("3.1.2_1")
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 2);
                // Print out fillstoerrelse: & ANTALL
            }

            //Arkade rapporten, elementet journalposttype angitt med typer som ikke følger Noark5 standarden.
            // Get example with missing journalposttype
            List<String> journalposttype = arkadeModel.getSpecificValue(index, "journalposttype");
            if(!journalposttype.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 3);
                // baseX
                // Print out journalposttype: & ANTALL
            }

            //Arkade rapporten, elementet skjermingshjemmel mangler.
            // Get example with missing konvertertFra
            List<String> konvertertFra = arkadeModel.getSpecificValue(index, "skjerming");
            if(!konvertertFra.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 4);
                // baseX
            }

            //Arkade rapporten, elementet korrespondansepart mangler.
            // Get example with missing korrespondansepart
            List<String> korrespondansepart = arkadeModel.getSpecificValue(index, "korrespondansepart");
            if(!korrespondansepart.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 5);
                // baseX
            }

            //Arkade rapporten, elementet klasse har mapper og underklasser.
            // Get example with missing elementHarMapperOgUnderklasser
            List<String> elementHarMapperOgUnderklasser = arkadeModel.getSpecificValue(index, "kriv inn her4");
            if(!elementHarMapperOgUnderklasser.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 6);
                // baseX
            }

            //Arkade rapporten, elementet dokumentfil mangler under elementet dokumentobjekt.
            // Get example with missing manglerUnderElementetDokumentobject
            List<String> manglerUnderElementetDokumentobject = arkadeModel.getSpecificValue(index, "dokumentfil,");
            if(!manglerUnderElementetDokumentobject.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 7);
                // baseX
            }

            // Arkade rapporten, elementet tittel mangler under mappe, registering eller dokumentbeskrivelse.
            // Get example with missing elementetTittelManglerUnderMappeRegisteringEllerDokumentbeskrivelse
            List<String> elementetMangler = arkadeModel.getSpecificValue(index, "registrering,");
            if(!elementetMangler.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 8);
                // baseX
            }

            // Arkade rapporten, elementet avskrivingsmaate inneholder ikke godkjente verdier.
            // Get example with missing avskrivingsmaate
            List<String> avskrivingsmaate = arkadeModel.getSpecificValue(index, "kriv inn her");
            if(!avskrivingsmaate.isEmpty()){
                setNewInput(Arrays.asList(3, 1, 2), Collections.emptyList(), 9);
                // baseX metadat.xsd
            }
        }
    }

    private void skjerminger() {
        List<String> para = xqueriesMap.get("3.1.23_1");
        if(para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 0);
        } else {

            List<String> skjermingtyper = getSkjerminger(para);
            int distinct = skjermingtyper.size();
            skjermingtyper = splitIntoTable(skjermingtyper);

            int total = skjermingtyper.stream().filter(t -> t.matches("[0-9]{0,4}"))
                    .mapToInt(Integer::parseInt)
                    .sum();

            setNewInput(Arrays.asList(3, 1, 23), Arrays.asList("" + total, "" + distinct), 1);
            insertTable(Arrays.asList(3, 1, 23), skjermingtyper);

            para = xqueriesMap.get("3.1.23_2");

            List<String> para2 = xqueriesMap.get("3.1.23_3");
            if (para2.get(0).equals(EMPTY)) {
                setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 3);
            } else {
                List<String> ls = new ArrayList<>();
                List<String> input = new ArrayList<>();
                total = 0;
                for (String s : para2) {
                    ls.addAll(Arrays.asList(s.split(TABLESPLIT)));
                    total += Integer.parseInt(ls.get(ls.size() - 1));
                }
                input.add("" + total);
                input.add(ls.get(0));
                input.add(ls.get(ls.size() - 2));
                setNewInput(Arrays.asList(3, 1, 23), input, 2);
            }

            if (para.get(0).equals(EMPTY)) {
                setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 4);
            }

            if (!para.get(0).equals(EMPTY) && !para2.get(0).equals(EMPTY)) {
                setNewInput(Arrays.asList(3, 1, 23), Collections.emptyList(), 5);
            }
        }
    }

    private List<String> getSkjerminger(List<String> ls) {
        Map<String, Integer> map = new LinkedHashMap<>();

        map.put("Unntatt offentlighet", 0);
        map.put("OFFL§13 Taushetsplikt", 0);
        map.put("OFFL§23 Forhandlingsposisjon, Økonomi-Lønn-Personalforv., Rammeavtaler, Anbudssaker, Eierinteresser", 0);
        map.put("OFFL§24 Kontroll- og reguleringstiltak, Lovbrudd, Anmeldelser, Straffbare handlinger, Miljøkriminalitet", 0);
        map.put("OFFL§25 Tilsettingssaker", 0);
        map.put("OFFL§26 Eksamensbesvarelser, Personbilder i personregister, Personovervåking", 0);

        for (String l : ls) {
            Matcher m = Pattern.compile("[§][ ][0-9]{1,3}|[§][0-9]{1,3}").matcher(l);
            if (m.find()) {
                String text = Arrays.asList(m.group().split("[§][ ]?")).get(1);
                int num = Integer.parseInt(Arrays.asList(l.split("[;][ ]")).get(1));
                switch (text) {
                    case "13" -> map.computeIfPresent("OFFL§13 Taushetsplikt",
                            (k, v) -> v += num);
                    case "23" -> map.computeIfPresent("OFFL§23 Forhandlingsposisjon, Økonomi-Lønn-Personalforv., Rammeavtaler, Anbudssaker, Eierinteresser",
                            (k, v) -> v += num);
                    case "24" -> map.computeIfPresent("OFFL§24 Kontroll- og reguleringstiltak, Lovbrudd, Anmeldelser, Straffbare handlinger, Miljøkriminalitet",
                            (k, v) -> v += num);
                    case "25" -> map.computeIfPresent("OFFL§25 Tilsettingssaker",
                            (k, v) -> v += num);
                    case "26" -> map.computeIfPresent("OFFL§26 Eksamensbesvarelser, Personbilder i personregister, Personovervåking",
                            (k, v) -> v += num);
                    default -> map.computeIfPresent("Unntatt offentlighet",
                            (k, v) -> v += num);
                }
            }
        }

        List<String> newList = new ArrayList<>();
        map.entrySet().stream().filter(entry -> entry.getValue() > 0)
                .forEach(entry ->
                        newList.add(entry.getKey() + "; " + entry.getValue().toString())
                );

        return newList;
    }

    public List<String> splitIntoTable(List<String> temp) {
        List<String> ls = new ArrayList<>();
        for (String s : temp) {
            ls.addAll(Arrays.asList(s.split(TABLESPLIT)));
        }
        return ls;
    }

    private int getRows(List<String> input) {
        int num = 0;

        for(String s : input) {
            Matcher m = Pattern.compile("[:]").matcher(s);
            while(m.find()) {
                num++;
            }

        }

        return num;
    }

    private void writeAttachments(String filename, List<String> content) {
        String path = prop.getProperty("tempFolder") + "\\" + prop.getProperty("currentArchive") //NOSONAR
                + "\\Rapporter\\" + filename + ".txt"; // NOSONAR
        File attachment = new File(path);
        try {
            if (attachment.createNewFile()) {
                System.out.println("File created: " + attachment.getName()); // NOSONAR
                Files.write(Path.of(path), content, Charset.defaultCharset());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage()); // NOSONAR
        }
    }

    /**
     * All Chapters that only uses ArkadeModel
     */
    private void arkadeTestReport(){ // NOSONAR
        String version = arkadeModel.getArkadeVersion().replace("Arkade 5 versjon: ", "");

        setNewInput(Arrays.asList(3, 1), Collections.singletonList(version), 0);
        // 3.1.1
        writeDeviation(Arrays.asList(3, 1, 1),"N5.01");
        writeDeviation(Arrays.asList(3, 1, 1),"N5.02");

        // 3.1.8
        List<String> dokumentstatus = arkadeModel.getTableDataFromHtml("N5.15", 4);

        setNewInput(Arrays.asList(3, 1, 8), Collections.emptyList(), 0);
        insertTable(Arrays.asList(3, 1, 8), dokumentstatus);

        //Chapter 3.1.12
        int arkivert = arkadeModel.sumStringListWithOnlyNumbers(
                arkadeModel.getNumberInTextAsString("N5.22", "Journalstatus: Arkivert - Antall:", ":"));
        int journalfort =  arkadeModel.sumStringListWithOnlyNumbers(
                arkadeModel.getNumberInTextAsString("N5.22", "Journalstatus: Journalført - Antall:", ":"));

        if (journalfort == -1) {
            setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 0);
        } else  {
            if (arkivert == -1) {
                setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 2);
            } else {
                setNewInput(Arrays.asList(3, 1, 12), Collections.singletonList("" + journalfort), 1);
            }
        }
        //Chapter 3.1.16 - Saksparter
        List<Integer> saksparter = arkadeModel.saksparter();
        if(saksparter.get(0) == 0){
            setNewInput(Arrays.asList(3, 1, 16), Collections.emptyList(), 0);
        } else {
            setNewInput(Arrays.asList(3, 1, 16), Collections.singletonList(
                    saksparter.get(0).toString()), 1);
        }

        //Chapter 3.1.17 - Merknader
        if (arkadeModel.ingenMerknader()) {
            setNewInput(Arrays.asList(3, 1, 17), Collections.emptyList(), 0);
            setNewParagraph(Arrays.asList(3, 1, 17), Collections.singletonList("Rename tittel from 3.1.17 to merknader "), 0);
            setNewParagraph(Arrays.asList(3, 3, 3), Collections.singletonList("DELETE ME: 3.3.3"), 0);
        }

        //Chapter 3.1.18 - Kryssreferanser
        if(arkadeModel.getTotal("N5.37", TOTAL) == 0){
            setNewInput(Arrays.asList(3, 1, 18), Collections.emptyList() , 0);
            //Delete 3.3.4, Title = "Kryssreferanser"
        }

        //Chapter 3.1.19 - Presedenser
        if(arkadeModel.getTotal("N5.38", TOTAL) == 0 ) {
            setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 0);
        }
        else if (arkadeModel.getTotal("N5.38", TOTAL) > 0 ) {
            setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 1);
        }

        //Chapter 3.1.22 - Dokumentflyter
        if(arkadeModel.getTotal("N5.41",TOTAL) == 0) {
            setNewInput(Arrays.asList(3, 1, 22), Collections.emptyList(), 0);
            //Delete 3.3.5, Title = Dokumentflyter
        }

        //Chapter 3.1.24 - Gradering
        if(arkadeModel.getTotal("N5.43", TOTAL) == 0) {
            setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 0);
        }
        else if (arkadeModel.getTotal("N5.43", TOTAL) > 0) {
            setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 1);
        }

        //Chapter 3.1.25 - Kassasjoner
        if(arkadeModel.getTotal("N5.44", TOTAL) == 0 &&
                arkadeModel.getTotal("N5.45", TOTAL) ==0) {
            setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 0);
            setNewInput(Arrays.asList(4, 2, 1), Collections.emptyList(), 0);
        }
        else if (arkadeModel.getTotal("N5.44", TOTAL) > 0 &&
                arkadeModel.getTotal("N5.45", TOTAL) > 0) {
            setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 1);
            setNewInput(Arrays.asList(4, 2, 1), Collections.emptyList(), 1);
        }
        //Chapter 3.1.27
        List<String> input = new ArrayList<>();
        int valg = arkadeModel.systemidentifikasjonerForklaring(input);
        setNewInput(Arrays.asList(3, 1, 27), input, valg);
        // AntallSepsialarkivdeler?

        //Chapter 3.1.28 - Arkivdelreferanser
        if(arkadeModel.getDataFromHtml("N5.48").isEmpty()) {
            setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 0);
        }
        else {
            setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 1);
        }

        //Chapter 3.1.30
        String chapter = "N5.59";
        if(arkadeModel.getDataFromHtml(chapter).isEmpty()) {
            setNewInput(Arrays.asList(3, 1, 30), Collections.emptyList(), 0);
        }
        else {
            int oj = arkadeModel.getTotal(chapter, "dokumentert i offentlig journal");
            int as = arkadeModel.getTotal(chapter, "funnet i arkivstrukturen:");
            if(oj != -1 && as != -1) {
                oj -= as;
                setNewInput(Arrays.asList(3, 1, 30), Collections.singletonList("" + oj), 1);
            }
        }

        //Chapter 3.1.32 - Endringslogg
        // Endre tittel til: Endringslogg testes i kapittel 3.3.8

        //Chapter 3.1.33
        if(arkadeModel.getDataFromHtml("N5.63").isEmpty()) {
            setNewInput(Arrays.asList(3, 1, 33), Collections.emptyList(), 0);
        }
        else {
            setNewInput(Arrays.asList(3, 1, 33), Collections.emptyList(), 1);
        }

        //Chapter 3.1.3
        int arkiv = arkadeModel.getTotal("N5.04", TOTAL);
        int arkivdeler = arkadeModel.getTotal("N5.05", TOTAL);
        List<String> status = arkadeModel.getDataFromHtml("N5.06");
        if(arkiv == 1 && arkivdeler == 1 && status.get(1).contains("Avsluttet periode")) {
            setNewInput(Arrays.asList(3, 1, 3), Collections.emptyList(),0);
        }
        if(!status.get(1).contains("Avsluttet periode")){
            String s = status.get(1);
            s = s.substring(s.lastIndexOf(":")+2);
            setNewInput(Arrays.asList(3, 1, 3), Collections.singletonList("\"" + s + "\""), 2);
        }
        if(arkiv > 1) {
            setNewInput(Arrays.asList(3, 1, 3), Collections.emptyList(), 3);
        }

        //Chapter 3.1.4
        //Endre tittel til: Se eget klassifikasjonskapittel 3.3.1.

        //Chapter 3.1.6
        //Endre tittel til: Se eget klassifikasjonskapittel 3.3.1.

        //Chapter 3.1.29
        //Endre tittel til: Se eget klassifikasjonskapittel 3.3.1.

        //Chapter 3.2.1
        if(!arkadeModel.getDataFromHtml("N5.48").isEmpty()) {
            setNewInput(Arrays.asList(3, 2, 1), Collections.emptyList(), 3);
        }

        //Chapter 3.3.1
        int total = arkadeModel.getTotal("N5.20", "Klasser uten registreringer");
        if(total > 0) {
            setNewInput(Arrays.asList(3, 3, 1), Collections.singletonList(total + ""), 2);
        }
        total = arkadeModel.getTotal("N5.12", TOTAL);
        if(total > 0) {
            setNewInput(Arrays.asList(3, 3, 1), Collections.singletonList(total + ""), 3);
        }
        if(!arkadeModel.getDataFromHtml("N5.47").isEmpty()) {
            setNewInput(Arrays.asList(3, 3, 1), Collections.emptyList(), 4);
        }
        total = arkadeModel.getTotal("N5.51", TOTAL);
        if(total > 0) {
            setNewInput(Arrays.asList(3, 3, 1), Collections.singletonList(total + ""), 5);
        }

        //Chapter 3.3.2
        total = arkadeModel.getTotal("N5.20", TOTAL);
        if(total > 0) {
            setNewInput(Arrays.asList(3, 3, 2), Collections.singletonList(total + ""), 0);
        }
    }

    /**
     * Chapter 3.1.1. N5.01, N5.02
     * @param kap docx kap
     * @param index deviation ID
     */
    private void writeDeviation(List<Integer> kap, String index) {
        List<String> avvik = arkadeModel.getDataFromHtml(index);
        if (!avvik.isEmpty()) {
            insertTable(kap, avvik);
        } else {
            setNewInput(kap, Collections.emptyList(), 0);
        }
    }

}
