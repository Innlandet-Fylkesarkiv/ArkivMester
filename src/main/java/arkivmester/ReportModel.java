package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class for handling report document configurations.
 */

public class ReportModel {

    enum TextStyle {
        INPUT,
        PARAGRAPH,
        TABLE
    }

    XWPFDocument document;
    String chapterFolder = "../Input/kapitler/";
    String templateFile = "src/main/resources/Dokumentmal_fylkesarkivet_Noark5_testrapport.docx";
    String outputFile = "../Output/createdocument.docx";

    static String notFoundField = "<Fant ikke verdi>";

    Map<List<Integer>, List<List<ChapterList>>> chapterList = new LinkedHashMap<>();
    private HeadersData headersData = new HeadersData();

    private static final String FONT = "Roboto (Brødtekst)";

    /**
     * Class for storing input of each chapter section of the report.
     */

    public static class ChapterList {

        String regex = "[^a-zA-Z ][A-Z]{3,}([ ][A-Z]{3,}){0,5}[^a-zA-Z ]|[A-Z]{4,}";

        private final List<String> result;
        private final int tableCol;
        private final TextStyle type;
        private int cindex;
        private boolean cases;

        /**
         * Initialize a default list of missing input.
         */
        ChapterList(List<String> input, TextStyle ts, int col, boolean c) {
            result = input;
            type = ts;
            cindex = 0;
            tableCol = col;
            cases = c;
        }

        public void insertInput(List<String> input) {
            result.remove(result.size()-1);
            result.addAll(input);
            cases = true;
        }

        /**
         *
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
         */
        public String currentItem() {
            int size = result.size();

            String temp = result.get(cindex);
            cindex = clamp(cindex+1, (size)-1);

            return temp;
        }

        /**
         * If chapter number is correct, set table as input to chapter-section.
         */
        public TextStyle getType() {
            return type;
        }

        /**
         * Will not clamp the max value so it does not go "out of bounds"
         */
        private int  clamp(int val, int max) {
            return Math.min(val, max);
        }

        /**
         * Prints header number and text from data stored.
         */
        public void getText() {
            for (String strings : result) {
                System.out.print(strings + " ");      // NOSONAR
            }
            System.out.print("\n");                 // NOSONAR
        }
    }

    /**
     * Class for handling headers that are fetched from document.
     */

    public static class HeadersData {
        private final List<String> name;
        private final Map<String, Integer> headerMap;

        /**
         * Initialize empty header and value
         */

        HeadersData() {
            name = new ArrayList<>();
            headerMap = new LinkedHashMap<>();
        }

        /**
         * Will store header name and increment value when there exist another name,
         * otherwise will add the new name to a list
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
         * Get values
         */
        public List<Integer> getNumbering() {
            return headerMap.values().stream().filter(i -> i > 0).collect(Collectors.toList());
        }

    }

    /**
     * Fetch all data from report and set up all chapters so that input can be changed
     */
    public void generateReport() {
        document = getDocumentFile(templateFile);

        setUpAllInputChapters();
    }

    /**
     * Try to fetch report template, and if there are no IO problems, it will be stored.
     */
    public static XWPFDocument getDocumentFile(String filepath) {
        try (
                FileInputStream fis = new FileInputStream(filepath)
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
     * Checks if paragraph is a Header
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
     * Replace every missing input with input fetched from program
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
            }
        }
    }

    private void addToDocument(List<List<ChapterList>> currentChapterInput, XWPFParagraph p) {
        for (List<ChapterList> chapters : currentChapterInput) {
            if(!chapters.isEmpty() && chapters.get(0).cases) {
                for(ChapterList chap : chapters) {
                    if(chap.getType().equals(TextStyle.PARAGRAPH)) {
                        insertParagraphToDocument(chap.currentItem(), p);
                    }
                    if(chap.getType().equals(TextStyle.TABLE)) {
                        insertTableToDocument(chap, p);
                    }
                }
            }
        }
    }

    /**
     * Will look for input field in each paragraph and replace it with the ones from the list
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

    private void insertInputToDocument(String text, String input, XWPFRun r) {
        text = text.replace("TODO", (!input.equals("") ? input : notFoundField));
        setRun(r, FONT , 11, false, text, false);
    }

    private void insertParagraphToDocument(String input, XWPFParagraph p) {
        XmlCursor cursor = p.getCTP().newCursor();//this is the key!

        XWPFParagraph para = document.insertNewParagraph(cursor);

        setRun(para.createRun() , FONT , 11, false, (!input.equals("") ? input : notFoundField), true);
    }

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

                tableOneRowVersion.getCell(j).setWidth("1500");
            }
        }

        cursor = p.getCTP().newCursor();//this is the key!

        XWPFParagraph para = document.insertNewParagraph(cursor);

        setRun(para.createRun() , FONT , 11, false, "", false);

    }

    //region end

    /**
     * Print paragraph text into table cell
     */
    public void setRun(XWPFRun run, String font, int size, boolean bold, String text, boolean addBreak) {
        run.setFontFamily(font);
        run.setFontSize(size);
        run.setText(text, 0);
        run.setBold(bold);
        if(addBreak) run.addBreak();
    }

    /**
     * Print the newly edited document to a new file
     */
    public void printReportToFile() {
        try {
            FileOutputStream os = new FileOutputStream(outputFile);
            document.write(os);
            document.close();
            os.close();
            System.out.println("\nfile created successfully!");     // NOSONAR
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());                     // NOSONAR
        }
    }

    /**
     * Replace old inputs with new ones
     */
    public void setNewInput(List<Integer> h, List<String> inputList, int c) {
        for(ChapterList chap : chapterList.get(h).get(c) ) {
            inputList = chap.updateText(inputList);
        }
    }

    public void setNewInput(List<Integer> h, List<String> inputList) {
        chapterList.put(h, new ArrayList<>());
        chapterList.get(h).add(new ArrayList<>());
        chapterList.get(h).get(0).add(new ChapterList(inputList, TextStyle.INPUT, 0, true));
    }

    /**
     * Replace old inputs with a table field
     */
    public void insertTable(List<Integer> h, List<String> tablefield) {
        for(List<ChapterList> chapters : chapterList.get(h)) {
            if(chapters.get(0).cases) {
                for(ChapterList chap : chapters) {
                    if(chap.getType() == TextStyle.TABLE) {
                        chap.insertInput(tablefield);
                    }
                }
            }
        }
    }

    /**
     * Replace old inputs with a paragraph
     */
    public void setNewParagraph(List<Integer> h, List<String> para) {
        chapterList.put(h, new ArrayList<>());
        chapterList.get(h).add(new ArrayList<>());
        for(String in : para) {
            chapterList.get(h).get(0).add(new ChapterList(Arrays.asList(in), TextStyle.PARAGRAPH, 0, true));
        }
    }

    /**
     * Gets text of output from each chapter file
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

    }

    /**
     * Used for fetching iterator of elements in a document
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

    private void createChapterParagraph(List<Integer> h, XWPFParagraph p) {
        if(!p.getText().contains("AND/OR")) {
            chapterList.get(h).get(chapterList.get(h).size()-1).add(
                    new ChapterList(Arrays.asList(p.getText()), TextStyle.PARAGRAPH, 0, false));
        }
        else {
            chapterList.get(h).add(new ArrayList<>());
        }
    }

    private void createChapterTable(List<Integer> h, XWPFTable t) {
        XWPFTableRow row = t.getRow(0);

        List<String> tableHeader = new ArrayList<>();

        for(XWPFTableCell cell : row.getTableCells()) {
            tableHeader.add(cell.getText());
        }

        tableHeader.add("X");

        chapterList.get(h).get(chapterList.get(h).size()-1).add(new ChapterList(
                tableHeader, TextStyle.TABLE, tableHeader.size()-1, false));

    }

    /**
     * Formats Chapter number given by converting it from List<Integer> into filename.
     * For example [1, 1] turns into "1.1".
     * @param h
     * @return
     */
    private String formatChapterNumber(List<Integer> h) {
        StringBuilder s = new StringBuilder();
        for(int i : h) {
            s.append(i).append(".");
        }
        s.append("docx");
        return s.toString();
    }

}
