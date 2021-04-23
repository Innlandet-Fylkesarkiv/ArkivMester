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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocGenerator;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import java.io.*;

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
        HEADER,
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


    static XWPFDocument document;
    String chapterFolder = "/chapters/";
    String templateFile = "/Dokumentmal_fylkesarkivet_Noark5_testrapport.docx";

    static String notFoundField = "<Fant ikke verdi>";

    private HeadersData headersData;

    // Experimental
    Map<List<Integer>, Chapter> chapterMap;

    private static final String FONT = "Roboto (Brødtekst)";

    public void init(Properties prop, Map<String, List<String>> map) {
        this.prop = prop;
        arkadeModel = new ArkadeModel();
        headersData = new HeadersData();
        chapterMap = new LinkedHashMap<>();     // Experimental
        xqueriesMap = map;
    }

    /**
     * Class for handling every ChapterList
     */

    public static class Chapter {

        String title;
        boolean active;
        List<SectionList> sections;

        Chapter(String t) {
            title = t;
            active = true;
            sections = new ArrayList<>();
        }

        public void deactivate() {
            active = false;
        }

        public void changeTitle(String t) {
            title = t;
        }

        public void addSection() {
            sections.add(new SectionList());
        }

        public void fillSection(List<String> ls, TextStyle style, int col, CTChart chart) {
            sections.get(sections.size()-1).addContent(ls, style, col, chart);
        }

        public void placeInEmptySection(CTChart chart) {
            for(SectionList section : sections) {
                if(section.placeIfEmpty(chart)) return;
            }
        }

        public void insertInput(XWPFParagraph p) {
            for(XWPFRun r : p.getRuns()) {
                if (r.getText(0) != null && r.getText(0).contains("TODO")) {
                    for (SectionList section : sections) {
                        section.insertInput(r);
                    }
                }
            }
        }

        public void insertParagraph(int sect, List<String> input) {
            sections.get(sect).insertParagraph(input);
        }

        public void insertTable(List<String> input) {
            for(SectionList section : sections) {
                if(section.isActive()) section.insertTable(input);
            }
        }

        public void insertTable(List<String> input, List<Integer> matchRows) {
            for(SectionList section : sections) {
                if(section.isActive()) section.insertTable(input, matchRows);
            }
        }

        public void insertGraph(int sect, List<String> inputG, int col, boolean vary) {
            sections.get(sect).insertGraph(inputG, col, vary);
        }

        public void writeInputText() {
            int ind = 0;
            System.out.println(title);                                                      // NOSONAR
            for (SectionList section : sections) {
                ind++;
                if(section.isActive()) {
                    System.out.print("\t" + ind + ": ");                                    // NOSONAR
                    section.writeInputText();
                    System.out.print("\n");                                                 // NOSONAR
                }
            }
        }

        public void insertToDocument(XWPFParagraph p) {
            for (SectionList section : sections) {
                if(section.isActive()) {
                    section.insertToDocument(p);
                }
            }
        }

    }

    /**
     * Handles all the sections in a certain chapter
     */

    public static class SectionList {

        boolean active;
        List<Content> contents;

        SectionList() {
            active = false;
            contents = new ArrayList<>();
        }

        public void addContent(List<String> ls, TextStyle style, int col, CTChart chart) {
            contents.add(new Content(ls, style, col, chart));
        }

        public boolean placeIfEmpty(CTChart chart) {
            if(contents.isEmpty()) {
                contents.add(new Content(null, TextStyle.GRAPH, 0, chart));
                active = true;
                return true;
            }
            return false;
        }

        public void insertInput(XWPFRun r) {
            for(Content content : contents) {
                if(content.type.equals(TextStyle.INPUT)) {
                    content.insertInputToDocument(r.getText(0), r);
                }
            }
            active = true;
        }

        public void insertParagraph(List<String> input) {
            for(Content content : contents) {
                if(content.type.equals(TextStyle.PARAGRAPH)) {
                    input = content.updateText(input);
                }
            }
            active = true;
        }

        public void insertTable(List<String> input) {
            for(Content content : contents) {
                if(content.isTableTemplate()) content.updateTable(input);
            }
            active = true;
        }

        public void insertTable(List<String> inputT, List<Integer> matchRows) {
            for(Content content : contents) {
                if(content.isTableTemplate()) content.updateTable(inputT, matchRows);
            }
            active = true;
        }

        public void insertGraph(List<String> inputG, int col, boolean vary) {
            for(Content content : contents) {
                content.insertGraph(inputG, col, vary);
            }
            active = true;
        }

        public void writeInputText() {
            for(Content content : contents) {
                content.getText();
            }
        }

        public void insertToDocument(XWPFParagraph p) {
            for(Content content : contents) {
                switch(content.getType()) {
                    case PARAGRAPH:
                        content.insertParagraphToDocument(p);
                        break;
                    case TABLE:
                        content.insertTableToDocument(p);
                        break;
                    case GRAPH:
                        content.insertGraphToDocument(p);
                        break;
                    default:
                }
            }
        }

        public boolean isActive() { return active; }

    }

    public static class Content {

        String regex = "[^a-zæøåA-ZÆØÅ ][A-ZÆØÅ]{3,}([ ][A-ZÆØÅ]{3,}){0,5}[^a-zæøåA-ZÆØÅ ]|[A-ZÆØÅ]{4,}";

        private List<String> result;

        private int tableCol;
        private final TextStyle type;
        private int cindex;
        private CTChart chart;
        private boolean isVaried;

        /**
         * Initialize a default list of missing input.
         * @param input - the input text that are going to be put from either file or code
         * @param style    - the type of text as an enum(PARAGRAPH, INPUT, TABLE)
         * @param col   - amount of coloums if it is a table
         */
        Content(List<String> input, TextStyle style, int col, CTChart ch) {
            result = input;
            type = style;
            cindex = 0;
            tableCol = col;
            chart = ch;
        }

        public void insertGraph(List<String> input, int col, boolean vary) {
            result = input;
            tableCol = col;
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
            return input.subList(index, input.size());
        }

        public void updateTable(List<String> input) {
            result.subList(tableCol, result.size()).clear();
            result.addAll(input);
        }

        public void updateTable(List<String> inputT, List<Integer> matchRows) {
            List<String> temp = new ArrayList<>(result.subList(0, tableCol));

            for(int row = 0; row*tableCol < inputT.size(); row++) {
                for(int resultrow = 1; resultrow*tableCol < result.size(); resultrow++) {
                    if(findMatchingRows(matchRows, row, resultrow, inputT, temp)) break;
                }
            }

            result = temp;
        }

        public void insertParagraphToDocument(XWPFParagraph p) {
            XmlCursor cursor = p.getCTP().newCursor();//this is the key!

            XWPFParagraph para = document.insertNewParagraph(cursor);

            String input = currentItem();

            setRun(para.createRun() , FONT , 11, true, (!input.equals("") ? input : notFoundField), true);
        }

        public void insertTableToDocument(XWPFParagraph p) {
            XmlCursor cursor = p.getCTP().newCursor();//this is the key!

            XWPFTable table = document.insertNewTbl(cursor);
            table.removeRow(0);

            XWPFParagraph paragraph;

            XWPFTableRow tableOneRowVersion;

            for(int i = 0; i < result.size(); i += tableCol) {
                tableOneRowVersion = table.createRow();
                for(int j = 0; j < tableCol; j++) {
                    if(i == 0) {
                        tableOneRowVersion.addNewTableCell();
                    }
                    paragraph = tableOneRowVersion.getCell(j).addParagraph();
                    setRun(
                            paragraph.createRun(),
                            FONT,
                            11,
                            (i != 0),
                            currentItem(),
                            false
                    );

                    tableOneRowVersion.getCell(j).setWidth("5000");
                }
            }

            cursor = p.getCTP().newCursor();//this is the key!

            XWPFParagraph para = document.insertNewParagraph(cursor);

            setRun(para.createRun() , FONT , 11, true, "", false);
        }

        public void insertGraphToDocument(XWPFParagraph p) {
            int width = 16 * Units.EMU_PER_CENTIMETER;
            int height = 10 * Units.EMU_PER_CENTIMETER;

            if(tableCol > 0) {

                XmlCursor cursor = p.getCTP().newCursor();//this is the key!

                XWPFParagraph para = document.insertNewParagraph(cursor);

                XWPFRun r = para.createRun();

                try {
                    XWPFChart charttemp = document.createChart(r, width, height);
                    CTChart ctChartTemp = charttemp.getCTChart();

                    XSSFChart chart = barColumnChart();

                    ctChartTemp.set(chart.getCTChart());
                } catch(InvalidFormatException | IOException e) {
                    System.out.println(e.getMessage());                 // NOSONAR
                }
            }
        }

        public XSSFChart barColumnChart() {
            try (XSSFWorkbook wb = new XSSFWorkbook()) {

                String sheetName = "CountryBarChart";

                XSSFSheet sheet = wb.createSheet(sheetName);

                List<String> categories = new ArrayList<>();

                String title = chart.getTitle().getTx().getRich().getPArray(0).getRArray(0).getT();

                int amountCat = ((result.size() / tableCol) - 1) / 2;

                for(int i = 0; i < amountCat; i++) {
                    categories.add(result.get((i * 2) + 1));
                }

                // Create row and put some cells in it. Rows and cells are 0 based.
                Row row = sheet.createRow((short) 0);

                Cell cell;

                for(int i = 0; i < tableCol; i++) {
                    cell = row.createCell((short) i);
                    cell.setCellValue(result.get(i * (result.size() / tableCol)));
                }

                for(int i = 1; i <= amountCat; i++) {
                    row = sheet.createRow((short) i);

                    for(int j = 0; j < tableCol; j++) {
                        cell = row.createCell((short) j);
                        int tempNum = Integer.parseInt(result.get(j * (result.size() / tableCol) + (i * 2)));
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
                        new CellRangeAddress(0, 0, 0, tableCol-1));

                List<XDDFNumericalDataSource<Double>> values = new ArrayList<>();
                for (int i = 1; i <= amountCat; i++) {
                    values.add(XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                            new CellRangeAddress(i, i, 0, tableCol-1)));
                }


                XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);

                data.setVaryColors(isVaried);

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

        /**
         * Inserts input text to document from chapterlist.
         * @param text - text to be replaced by input
         * @param r - Used for editing into a paragraph
         */
        private void insertInputToDocument(String text, XWPFRun r) {
            String s = currentItem();
            text = text.replace("TODO", (!s.equals("") ? s : notFoundField));
            setRun(r, FONT , 11, true, text, false);
        }

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
         * Will not clamp the max value so it does not go "out of bounds".
         * @param val - value to compare with the max
         * @param max - value to limit 'val' from going out of bounds
         * @return the value that is lowest between these parameters
         */
        private int  clamp(int val, int max) {
            return Math.min(val, max);
        }

        /**
         * If chapter number is correct, set table as input to chapter-section.
         * @return enum of the type from class (INPUT, PARAGRAPH, TABLE)
         */
        public TextStyle getType() {
            return type;
        }

        public boolean isTableTemplate() {
            return (type.equals(TextStyle.TABLE) && result.get(result.size()-1).equals("X"));
        }

        /**
         * Prints header number and text from data stored.
         */
        public void getText() {
            for (String strings : result) {
                System.out.print(strings + " ");      // NOSONAR
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

        //Update ToC
        try{
            String inputDocx = prop.get("tempFolder") + "\\" + prop.get("currentArchive") + "\\Rapporter\\Testrapport.docx"; //#NOSONAR

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
                    new File(inputDocx));

            TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
            //tocGenerator.generateToc( 0, "TOC \\o \"1-3\" \\h \\z \\u ", false); //#NOSONAR

            tocGenerator.updateToc(); // including page numbers

            wordMLPackage.save(new File(prop.get("tempFolder") + "\\" + prop.get("currentArchive") + "\\Rapporter\\Testrapport.docx")); //#NOSONAR
        } catch (Exception e) {
            System.out.println("Kunne ikke oppdatere innholdsfortegnelsen"); //#NOSONAR
        }

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
                    getOutputValuesFromFile(h, p.getText());
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

        XWPFDocument doc = document;

        headersData = new HeadersData();

        Chapter currentChapter = new Chapter("template");

        changeDate();

        for(int i = 0; i < doc.getParagraphs().size(); i++) {
            XWPFParagraph p = document.getParagraphs().get(i);

            if(findNewHeader(p)) {

                List<Integer> h = headersData.getNumbering();

                currentChapter = chapterMap.get(h);

                List<XWPFRun> runs = p.getRuns();

                if(currentChapter.active) {
                    String text = runs.get(0).text();
                    XWPFRun r = runs.get(0);
                    text = text.replace(p.getText(), currentChapter.title);
                    r.setText(text, 0);

                    if(i + 1 < document.getParagraphs().size()) {
                        currentChapter.insertToDocument(document.getParagraphs().get(i+1));
                    }
                } else {
                    System.out.println("deleted");      //NOSONAR
                    p.removeRun(0);
                }
            }

            currentChapter.insertInput(p);
        }
    }

    private void changeDate() {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            XmlCursor cursor = paragraph.getCTP().newCursor();
            cursor.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//*/w:txbxContent/w:p/w:r");

            List<XmlObject> ctrsintxtbx = new ArrayList<>();

            while(cursor.hasNextSelection()) {
                cursor.toNextSelection();
                XmlObject obj = cursor.getObject();
                ctrsintxtbx.add(obj);
            }
            try {
                for (XmlObject obj : ctrsintxtbx) {
                    CTR ctr = CTR.Factory.parse(obj.xmlText());
                    XWPFRun bufferrun = new XWPFRun(ctr, (IRunBody)paragraph);
                    String text = bufferrun.getText(0);
                    if (text != null && text.contains("[Dato]")) {
                        text = text.replace("[Dato]", xqueriesMap.get("1.1").get(7));
                        bufferrun.setText(text, 0);
                    }
                    obj.set(bufferrun.getCTR());
                }
            } catch(XmlException e) {
                System.out.println("xml problem");      // NOSONAR
            }
        }
    }

    /**
     * Writes to console every chapter, its various cases and text input.
     */
    private void writeInputText() {

        for(Map.Entry<List<Integer>, Chapter> entry : chapterMap.entrySet()) {
            System.out.print(entry.getKey() + ": ");                                         // NOSONAR
            entry.getValue().writeInputText();
        }

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
    public void setNewInput(List<Integer> numberH, List<String> inputP, int sect) {
        chapterMap.get(numberH).insertParagraph(sect, inputP);              // Experimental
    }

    /**
     * Add input from scratch into selected chapter.
     * @param h - The header number for the chapter
     * @param i - list of input strings
     */
    public void setNewInput(List<Integer> numberH, List<String> input) {
        chapterMap.get(numberH).addSection();
        chapterMap.get(numberH).fillSection(input, TextStyle.INPUT, 0, null);
    }

    /**
     * Look for a table from ChapterList that isn't yet in use.
     * @param h - The header number for the chapter
     * @param t - Table content text that the user wants placed in table
     */
    public void insertTable(List<Integer> numberH, List<String> inputT) {
        chapterMap.get(numberH).insertTable(inputT);
    }

    /**
     * Look for a table from ChapterList that isn't yet in use.
     * @param h - The header number for the chapter
     * @param t - Table content text that the user wants placed in table
     */
    public void insertTable(List<Integer> numberH, List<String> inputT, List<Integer> matchRows) {
        chapterMap.get(numberH).insertTable(inputT, matchRows);         // Experimental
    }

    /**
     * Add a new paragraph from scratch into selected chapter.
     * @param h - The header number for the chapter
     * @param p - String text that the user wants to manually have put into the report
     */
    public void setNewParagraph(List<Integer> numberH, List<String> inputP, int sect) {
        for(String s : inputP) {
            chapterMap.get(numberH).sections.get(sect).addContent(Arrays.asList(s), TextStyle.PARAGRAPH, 0, null);
        }
    }

    private void insertGraph(List<Integer> number, List<String> inputG, int col, int sect, boolean vary) {
        chapterMap.get(number).insertGraph(sect, inputG, col, vary);        // Experimental
    }

    /**
     * Gets all text of type 'paragraph' or 'table' after 'output' from each chapter file.
     * @param h - The header number for the chapter
     */
    private void getOutputValuesFromFile(List<Integer> h, String headerTitle) {

        String chapterFile = formatChapterNumber(h);

        System.out.println(chapterFile);

        Iterator<IBodyElement> bodyList = getDocumentIterator(chapterFolder + chapterFile);

        chapterMap.put(h, new Chapter(headerTitle));

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
                    chapterMap.get(h).addSection();             // Experimental
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
        if(p.getText().contains("AND/OR")) {
            chapterMap.get(h).addSection();
        }
        else {
            chapterMap.get(h).fillSection(Arrays.asList(p.getText()), TextStyle.PARAGRAPH, 0, null);
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

        chapterMap.get(h).fillSection(tableHeader, TextStyle.TABLE, t.getRow(0).getTableCells().size(), null);  // Experimental

    }

    private void createChapterGraph(List<Integer> h, CTChart chart) {
        chapterMap.get(h).placeInEmptySection(chart);    // Experimental
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


        //Chapter 1.2
        para = xqueriesMap.get("1.2_1");
        para.addAll(xqueriesMap.get("1.2_2"));
        para.addAll(xqueriesMap.get("1.2_3"));
        para.addAll(xqueriesMap.get("1.2_4"));
        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(1, 2), para, 0);
        }
        para = xqueriesMap.get("1.2_5");
        if(!para.get(0).contains(EMPTY)) {
            insertTable(Arrays.asList(1, 2), splitIntoTable(para));
        }

        //Chapter 3.1.3
        int arkiv = arkadeModel.getTotal("N5.04", TOTAL);
        List<String> parts = xqueriesMap.get("3.1.3");
        if(!parts.get(0).contains(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 3), Collections.singletonList("" + parts.size()), 0);
            insertTable(Arrays.asList(3, 1, 3), splitIntoTable(parts));
        }
        if(arkiv > 1) {
            setNewInput(Arrays.asList(3, 1, 3), Collections.emptyList(), 1);
        }

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

        //Chapter 3.1.10
        setNewInput(Arrays.asList(3, 1, 10), Collections.emptyList(), 0);


        //Chapter 3.1.2
        // valideringAvXML(); NOSONAR


        //Chapter 3.1.11
        para = xqueriesMap.get("3.1.11b");
        List<String> medium = xqueriesMap.get("dokumentmedium");
        if(para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 11), Collections.emptyList(), 0);
        } else if(!medium.get(0).contains("Elektronisk")) {
            setNewInput(Arrays.asList(3, 1, 11), Collections.singletonList("" + para.size()), 1);
        }
        else {
            setNewInput(Arrays.asList(3, 1, 11), Collections.singletonList("" + para.size()), 2);
            insertTable(Arrays.asList(3, 1, 11), splitIntoTable(para));
        }
        //TODO: Ny case med tabell, trenger ny xquery.

        //Chapter 3.1.13
        para = xqueriesMap.get("3.1.13_1");

        if(para.get(0).equals(EMPTY)) {
            para = xqueriesMap.get("3.1.13_2");

            if (para.get(0).equals(EMPTY)) {
                setNewInput(Arrays.asList(3, 1, 13), Collections.emptyList(), 0);
            } else {
                setNewInput(Arrays.asList(3, 1, 13), Collections.singletonList(para.size() + ""), 3);
            }
        } else {
            if(para.size() > 25) {
                setNewInput(Arrays.asList(3, 1, 13),
                        Collections.singletonList(para.size() + ""), 2); // NOSONAR
                writeAttachments("3.1.13", para);
                attachments.add("\u2022 3.1.13.txt");
            }else {
                setNewInput(Arrays.asList(3, 1, 13),
                        Collections.singletonList(para.size() + ""), 1);
                insertTable(Arrays.asList(3, 1, 13), splitIntoTable(para));
            }
        }

        //Chapter 3.1.14 N5.27, N5.11, N5.18
        para = xqueriesMap.get("3.1.14_1");
        List<Integer> output3114 = Collections.emptyList();
        int val3114 = arkadeModel.firstLastReg(para, output3114);

        if(val3114 == 0){
            // Wrong date print out table
            List<String> regDato = xqueriesMap.get("3.1.14_2");
            // Get registeringer dates that are wrong for every arkidel
            List<List<String>> listOfLists = arkadeModel.registeringerUtenomArkivdelStartOgSluttDato(para, regDato);
            setNewInput(Arrays.asList(3, 1, 14), Collections.emptyList(), val3114);
            // listOfLists for now only gets one arkivdel. insertTable does not support 0-* tables. insertTable for every element. for loop for get()
            insertTable(Arrays.asList(3, 1, 14), splitIntoTable(listOfLists.get(0)));
        }
        else if(val3114 == 1){
            setNewInput(Arrays.asList(3, 1, 14), Collections.emptyList(), val3114);
        }
        else if (val3114 == 2){
            setNewInput(Arrays.asList(3, 1, 14), Arrays.asList(output3114.get(0) + "", output3114.get(1) + ""), val3114);
        }

        //Chapter 3.1.27 N5.47, N5.34
        List<String> input = new ArrayList<>();

        int valg = arkadeModel.systemidentifikasjonerForklaring(xqueriesMap.get("3.1.27_1"),xqueriesMap.get("3.1.27_2"),input);

        if(valg == 0){
            setNewInput(Arrays.asList(3, 1, 27), Collections.emptyList(), valg);
        }
        else {
            setNewInput(Arrays.asList(3, 1, 27), input, valg);
        }

        //Chapter 3.1.31
        setNewInput(Arrays.asList(3, 1, 31), Collections.emptyList(), 0);



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

        // TODO: ha antall møter av en type, ikke antall deltagere per møte av en type

        //Chapter 3.3.2
        // N5.20 arkade gettotal case 0
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
        parts = xqueriesMap.get("3.1.3");
        int arkivdeler = arkadeModel.getTotal("N5.05", TOTAL);
        if(arkivdeler > 1) {
            setNewInput(Arrays.asList(3, 1, 3), Collections.singletonList("" + arkivdeler), 1);
            insertTable(Arrays.asList(3, 1, 3), splitIntoTable(parts));
        }

        chapterMap.get(Arrays.asList(3, 1, 3)).changeTitle("delete me");

        //Chapter 3.3.4 N5.37
        String chapter334 = "3.3.4";
        String index537 = "N5.37";
        List<String> crossReferences = xqueriesMap.get(chapter334);
        int n537 = arkadeModel.getTotal(index537, TOTAL);

        if(!crossReferences.get(0).equals(EMPTY)){
            setNewInput(Arrays.asList(3, 3, 4), Arrays.asList("" + n537, "" +  crossReferences.size()), 0);
        }
        else{
            setNewInput(Arrays.asList(3, 3, 4), Arrays.asList("" + n537, "" +  0), 0);
        }
        List<String> missingValues = arkadeModel.getSystemID(index537, "systemID");
        if (missingValues.size() <= 25){
            setNewParagraph(Arrays.asList(3, 3, 4), missingValues, 0);
        }
        else{
            setNewInput(Arrays.asList(3, 3, 4), Collections.singletonList("Over 25. Skriver til Vedlegg"), 0);
            writeAttachments(chapter334, missingValues);
        }

        //Chapter 3.3.5

        para = xqueriesMap.get("3.3.5_1");

        if(!para.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 3, 5), Arrays.asList(para.get(0), para.get(1), para.get(2)), 0);
            insertTable(Arrays.asList(3, 3, 5), Collections.emptyList());
            para = xqueriesMap.get("3.3.5_2");
            insertTable(Arrays.asList(3, 3, 5), splitIntoTable(para));
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
            }
            for (int i = 1; i <= journal.size(); i += 2) {
                int amount = Integer.parseInt(journal.get(i));
                if ((float)amount > (((float)total / 100.0f) * 90.0f)) {
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
            for (int i = 1; i <= unit.size(); i += 2) {
                total += Integer.parseInt(unit.get(i));
            }
            for(int i = 1; i <= unit.size(); i+=2 ) {
                int amount = Integer.parseInt(unit.get(i));
                if((float)amount > (((float)total / 100.0f) * 90.0f)) {
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
            setNewInput(Arrays.asList(3, 2), Collections.emptyList(),1);
            setNewParagraph(Arrays.asList(3, 2), droid, 2);
            if (nonCompliant == 0 && failed == 0) {
                setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 3);
            } else if (failed == 0) {
                setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 4);
                setNewInput(Arrays.asList(3, 2), Collections.singletonList("" + nonCompliant), 6);
            } else if (nonCompliant == 0) {
                setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 4);
                setNewInput(Arrays.asList(3, 2), Collections.singletonList("" + nonCompliant), 5);
            }
            else if (failed > 0 && nonCompliant > 0) {
                setNewInput(Arrays.asList(3, 2), Collections.emptyList(), 4);
                setNewInput(Arrays.asList(3, 2), Collections.singletonList("" + nonCompliant), 5);
                setNewInput(Arrays.asList(3, 2), Collections.singletonList("" + nonCompliant), 6);
            }
        }

        //Chapter 3.1.7
        //TODO: Kjører feil xml.
        List<String> dirs = xqueriesMap.get("3.1.7_1b");
        //System.out.println(dirs); // NOSONAR
        //System.out.println(dirs.get(0)); // NOSONAR
        if(dirs.get(0).equals(EMPTY)) {
            setNewInput(Arrays.asList(3, 1, 7), Collections.emptyList(), 0);
        }
        else {
            setNewInput(Arrays.asList(3, 1, 7), Collections.singletonList("" + dirs.size()), 1);
            setNewParagraph(Arrays.asList(3, 1, 7), dirs, 1);
        }

        //Chapter 3.3.9
        // TODO: bare case 0 og siste case dukker opp
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
                // fulle feilene blir ikke skrevet opp her.
                writeAttachments("3.1.2_Date_value", invalidDates);
                attachments.add("\u2022 3.1.2_Date_value.txt");
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
    private void arkadeTestReport() { // NOSONAR
        String version = arkadeModel.getArkadeVersion().replace("Arkade 5 versjon: ", "");

        setNewInput(Arrays.asList(3, 1), Collections.singletonList(version), 0);
        // 3.1.1
        writeDeviation(Arrays.asList(3, 1, 1), "N5.01");
        writeDeviation(Arrays.asList(3, 1, 1), "N5.02");

        //Chapter 3.1.2
        valideringAvXML();

        // 3.1.8
        List<String> dokumentstatus = arkadeModel.getTableDataFromHtml("N5.15", 4);

        setNewInput(Arrays.asList(3, 1, 8), Collections.emptyList(), 0);
        insertTable(Arrays.asList(3, 1, 8), dokumentstatus);

        //Chapter 3.1.12
        //TODO: Bruk Xquery?
        int arkivert = arkadeModel.getTotal("N5.22", "Journalstatus: Arkivert - Antall:");
        int journalfort = arkadeModel.getTotal("N5.22", "Journalstatus: Journalført - Antall:");
        
        List<String> medium = xqueriesMap.get("dokumentmedium");
        if (journalfort == -1) {
            setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 0);
        } else {
            if (arkivert == -1) {
                setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 3);
            } else {
                setNewInput(Arrays.asList(3, 1, 12), Collections.singletonList("" + journalfort), 1);
                insertTable(Arrays.asList(3, 1, 12), Arrays.asList("Journalført", String.valueOf(journalfort)));
                if (!medium.get(0).contains("Elektronisk")) {
                    setNewInput(Arrays.asList(3, 1, 12), Collections.emptyList(), 2);
                }
            }
        }
        //Chapter 3.1.16 - Saksparter
        List<Integer> saksparter = arkadeModel.saksparter();
        if (saksparter.get(0) == 0) {
            setNewInput(Arrays.asList(3, 1, 16), Collections.emptyList(), 0);
        } else {
            setNewInput(Arrays.asList(3, 1, 16), Collections.singletonList(
                    saksparter.get(0).toString()), 1);
        }

        //Chapter 3.1.17 - Merknader
        if (arkadeModel.ingenMerknader()) {
            setNewInput(Arrays.asList(3, 1, 17), Collections.emptyList(), 0);
            setNewParagraph(Arrays.asList(3, 1, 17), Collections.singletonList("Rename tittel from 3.1.17 to merknader "), 0);
        }
        chapterMap.get(Arrays.asList(3, 3, 3)).changeTitle("Delete Me");

        //Chapter 3.1.18 - Kryssreferanser
        if (arkadeModel.getTotal("N5.37", TOTAL) == 0) {
            setNewInput(Arrays.asList(3, 1, 18), Collections.emptyList(), 0);
            //Delete 3.3.4, Title = "Kryssreferanser"
        }

        //Chapter 3.1.19 - Presedenser
        if (arkadeModel.getTotal("N5.38", TOTAL) == 0) {
            setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 0);
        } else if (arkadeModel.getTotal("N5.38", TOTAL) > 0) {
            setNewInput(Arrays.asList(3, 1, 19), Collections.emptyList(), 1);
        }

        //Chapter 3.1.22 - Dokumentflyter
        if (arkadeModel.getTotal("N5.41", TOTAL) == 0) {
            setNewInput(Arrays.asList(3, 1, 22), Collections.emptyList(), 0);
            //Delete 3.3.5, Title = Dokumentflyter
        }

        //Chapter 3.1.24 - Gradering
        if (arkadeModel.getTotal("N5.43", TOTAL) == 0) {
            setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 0);
        } else if (arkadeModel.getTotal("N5.43", TOTAL) > 0) {
            setNewInput(Arrays.asList(3, 1, 24), Collections.emptyList(), 1);
        }

        //Chapter 3.1.25 - Kassasjoner
        //TODO: Ny case 2 med ny xquery.
        if (arkadeModel.getTotal("N5.44", TOTAL) == 0 &&
                arkadeModel.getTotal("N5.45", TOTAL) == 0) {
            setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 0);
            setNewInput(Arrays.asList(4, 2, 1), Collections.emptyList(), 0);
        } else if (arkadeModel.getTotal("N5.44", TOTAL) > 0 &&
                arkadeModel.getTotal("N5.45", TOTAL) > 0) {
            setNewInput(Arrays.asList(3, 1, 25), Collections.emptyList(), 1);
            setNewInput(Arrays.asList(4, 2, 1), Collections.emptyList(), 1);
        }



        //Chapter 3.1.28 - Arkivdelreferanser
        if (arkadeModel.getDataFromHtml("N5.48").isEmpty()) {
            setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 0);
        } else {
            setNewInput(Arrays.asList(3, 1, 28), Collections.emptyList(), 1);
        }

        //Chapter 3.1.30
        String chapter = "N5.59";
        if (arkadeModel.getDataFromHtml(chapter).isEmpty()) {
            setNewInput(Arrays.asList(3, 1, 30), Collections.emptyList(), 0);
        } else {
            int oj = arkadeModel.getTotal(chapter, "dokumentert i offentlig journal");
            int as = arkadeModel.getTotal(chapter, "funnet i arkivstrukturen:");
            if (oj != -1 && as != -1) {
                oj -= as;
                setNewInput(Arrays.asList(3, 1, 30), Collections.singletonList("" + oj), 1);
            }
        }

        //Chapter 3.1.32 - Endringslogg
        // Endre tittel til: Endringslogg testes i kapittel 3.3.8

        //Chapter 3.1.33
        if (arkadeModel.getDataFromHtml("N5.63").isEmpty()) {
            setNewInput(Arrays.asList(3, 1, 33), Collections.emptyList(), 0);
        } else {
            setNewInput(Arrays.asList(3, 1, 33), Collections.emptyList(), 1);
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
        //TODO: Liste over N.51 klasser i case 6. Ny case med tabell med manglende xquery.
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
