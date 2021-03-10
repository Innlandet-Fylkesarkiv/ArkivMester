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
import java.util.stream.Stream;

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

    Map<List<Integer>, List<ChapterList>> chapterList = new LinkedHashMap<>();
    private HeadersData headersData = new HeadersData();

    private static final String FONT = "Roboto (Brødtekst)";

    /**
     * Class for storing input of each chapter section of the report.
     */

    public static class ChapterList {

        static String missingField = "<Mangler verdi>";

        private final List<Integer> headers;
        private List<String> result;
        private TextStyle type;
        private int cindex;
        private boolean cases;

        /**
         * Initialize a default list of missing input.
         */
        ChapterList(List<Integer> h, List<String> input, TextStyle ts, boolean c) {
            headers = h.stream().filter(t -> t > 0).collect(Collectors.toList());
            result = input;
            type = ts;
            cindex = 0;
            cases = c;
        }

        /**
         *
         */
        public void updateText(List<String> input) {
            int index = 0;
            for(int i = 0; i < result.size(); i++) {
                String s = result.get(i);
                Pattern pat = Pattern.compile("[^a-zA-Z ][A-Z]{3,}([ ][A-Z]{3,})*[^a-zA-Z ]|[A-Z]{4,}");
                Matcher m = pat.matcher(s);
                while (m.find()) {
                    String word = m.group();
                    s = s.replace(word, input.get(index));
                    result.set(i, s);
                    index = clamp(index+1, input.size()-1);
                }
            }
            cases = true;
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
         * Prints text of data stored.
         */

        public void getText() {
            for (Integer header : headers) {
                System.out.print(header + " ");      // NOSONAR
            }
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
                    List<List<String>> input = getFileFromHeader(h);
                    chapterList.put(h, new ArrayList<>());
                    for(List<String> list : input) {
                        chapterList.get(h).add(new ChapterList(h, list, TextStyle.PARAGRAPH, false));
                    }
                    if(input.isEmpty()) chapterList.get(h).add(new ChapterList(h, Arrays.asList(notFoundField), TextStyle.PARAGRAPH, false));
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

        for(Map.Entry<List<Integer>, List<ChapterList>> entry : chapterList.entrySet()) {
            for (ChapterList chapter : entry.getValue()) {
                chapter.getText();
            }
        }

        headersData = new HeadersData();

        List<ChapterList> currentChapterInput = new ArrayList<>();

        boolean hit = false;

        for(int i = 0; i < document.getParagraphs().size(); i++) {
            XWPFParagraph p = document.getParagraphs().get(i);

            if(hit) {
                addParagraphToDocument(currentChapterInput, p);
                hit = false;
            }

            if(findNewHeader(p)) {
                currentChapterInput = chapterList.get(headersData.getNumbering());
                hit = true;
            }

            editDocument(p, currentChapterInput);
        }
    }

    private void addParagraphToDocument(List<ChapterList> currentChapterInput, XWPFParagraph p) {
        if(currentChapterInput != null) {
            for(int i = 0; i < currentChapterInput.size(); i++) {
                ChapterList chap = currentChapterInput.get(i);
                if(chap.getType().equals(TextStyle.PARAGRAPH) && chap.cases) {
                    for(String s : chap.result) {
                        insertParagraphToDocument(s, p);
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
            if(r.getText(0) != null && r.getText(0).contains("TODO")) {
                if(cChapter != null) {
                    for(ChapterList chapter : cChapter) {
                        switch(chapter.getType()) {
                            case INPUT:
                                insertInputToDocument(r.getText(0), chapter.currentItem(), r);
                                break;
                            case TABLE:
                                insertTableToDocument(chapter, p);
                                break;
                            default:
                        }
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

        for(int i = 0; i < cChapter.result.size(); i++) {
            XWPFTableRow tableOneRowVersion = table.createRow();
            for(int j = 0; j < cChapter.result.size(); j++) {
                if(i == 0) {
                    tableOneRowVersion.addNewTableCell();
                }
                paragraph = tableOneRowVersion.getCell(j).addParagraph();
                setRun(
                        paragraph.createRun(),
                        FONT,
                        11,
                        (i == 0),
                        (!cChapter.currentItem().equals("") ? cChapter.currentItem() : notFoundField),
                        false
                );

                tableOneRowVersion.getCell(j).setWidth("1500");
            }
        }
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

    public void setNewInput(List<Integer> h, List<String> inputList, List<Integer> cases) {
        for(int i : cases) {
            chapterList.get(h).get(i).updateText(inputList);
        }
    }

    public void setNewInput(List<Integer> h, List<String> inputList) {
        chapterList.put(h, new ArrayList<>());
        chapterList.get(h).add(new ChapterList(h, inputList, TextStyle.INPUT, true));
    }

    /**
     * Replace old inputs with a table field
     */

    public void setNewTable(List<Integer> h, List<String> tablefield) {
        setNewChapter(h, tablefield, TextStyle.TABLE);
    }

    public void setNewTable(List<Integer> h, List<String> tableHeaders, List<String> tableContent) {

        List<String> ll = Stream.concat(tableHeaders.stream(), tableContent.stream())
                .collect(Collectors.toList());

        setNewChapter(h, ll, TextStyle.TABLE);
    }

    /**
     * Replace old inputs with a paragraph
     */
    public void setNewParagraph(List<Integer> h, List<String> para) {
        setNewChapter(h, para, TextStyle.PARAGRAPH);
    }

    private void setNewChapter(List<Integer> h, List<String> input, TextStyle type) {
        if (chapterList.get(h).get(0).result.get(0) == notFoundField) {
            chapterList.put(h, new ArrayList<>());
        }
        chapterList.get(h).add(new ChapterList(h, input, type, true));
    }

    /**
     * Gets text of output from chapter file
     */
    private List<List<String>> getFileFromHeader(List<Integer> h) {
        String chapterFile = formatChapterNumber(h);

        XWPFDocument doc = getDocumentFile(chapterFolder + chapterFile);

        if(doc != null) {
            return getOutputValuesFromFile(doc);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Gets text of output from chapter file
     */
    private List<List<String>> getOutputValuesFromFile(XWPFDocument doc) {
        List<XWPFParagraph> paragraphs = doc.getParagraphs();

        List<List<String>> temp = new ArrayList<>();
        temp.add(new ArrayList<>());

        int index = 0;

        boolean hit = false;
        for(XWPFParagraph p : paragraphs) {
            if(hit && !p.getText().equals("")) {
                if(!p.getText().contains("AND/OR")) {
                    temp.get(index).add(p.getText());
                }
                else {
                    temp.add(new ArrayList<>());
                    index++;
                }
            }
            if(p.getText().contains("Output")) {
                hit = true;
            }
        }

        return temp;
    }

    /**
     * Formats Chapter number given by converting it from List<Integer> into filename.
     * For example [1, 1] turns into "1.1".
     */
    private String formatChapterNumber(List<Integer> h) {
        StringBuilder s = new StringBuilder();
        for(int i : h) {
            s.append(i + ".");
        }
        s.append("docx");
        return s.toString();
    }

}
