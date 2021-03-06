package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for handling report document configurations.
 */

public class ReportModel {

    enum TextStyle {
        PLACEFOLDER,
        INPUT,
        PARAGRAPH,
        TABLE
    }

    XWPFDocument document;
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
        private final List<List<String>> result;
        private final TextStyle type;
        private int cindex;

        /**
         * Initialize a default list of missing input.
         */

        ChapterList(List<Integer> h) {
            headers = h.stream().filter(t -> t > 0).collect(Collectors.toList());
            result = Collections.singletonList(Collections.singletonList(missingField));
            type = TextStyle.PLACEFOLDER;
            cindex = 0;
        }

        /**
         * Initialize a default list of missing input.
         */

        ChapterList(List<Integer> h, List<List<String>> input, TextStyle ts) {
            headers = h.stream().filter(i -> i > 0).collect(Collectors.toList());
            result = input;
            type = ts;
            cindex = 0;
        }

        /**
         * If chapter number is correct, set table as input to chapter-section.
         */
        public String currentItem() {
            int size = result.size();
            int len = result.get(0).size();

            String temp = result.get(cindex % size).get(cindex / size);
            cindex = clamp(cindex, (size * len)-1);

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
            return Math.min(++val, max);
        }

        /**
         * Prints text of data stored.
         */

        public void getText() {
            for (Integer header : headers) {
                System.out.print(header + " ");      // NOSONAR
            }
            for (List<String> strings : result) {
                System.out.print(strings + " ");      // NOSONAR
            }
            System.out.print('\n');                         // NOSONAR
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
        setUpReportDocument(templateFile);

        setUpAllInputChapters();

    }

    /**
     * Try to fetch report template, and if there are no IO problems, it will be stored.
     */
    private void setUpReportDocument(String filepath) {
        try (
                FileInputStream fis = new FileInputStream(filepath)
        ) {
            document = new XWPFDocument(fis);
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());             //NOSONAR
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
                    chapterList.put(headersData.getNumbering(), new ArrayList<>());
                    chapterList.get(headersData.getNumbering()).add(new ChapterList(headersData.getNumbering()));
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

            if(style.getStyleId().contains("Overskrift") || style.getStyleId().contains("Heading"))
            {
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

        List<IBodyElement> bodyList = new CopyOnWriteArrayList<>(document.getBodyElements());

        Iterator<IBodyElement> bodyElementIterator = bodyList.iterator();

        List<ChapterList> currentChapterInput = new ArrayList<>();

        while(bodyElementIterator.hasNext()) {
            IBodyElement element = bodyElementIterator.next();
            if(element instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph)element;

                if(findNewHeader(p)) {
                    currentChapterInput = chapterList.get(headersData.getNumbering());
                }

                editDocument(p, currentChapterInput);
            }
        }
    }

    /**
     * Will look for input field in each paragraph and replace it with the ones from the list
     */
    private void editDocument(XWPFParagraph p, List<ChapterList> cChapter) {
        for(XWPFRun r : p.getRuns()) {
            String text = "" + r.getText(0);
            if(text.contains("TODO")) {
                for(ChapterList chapter : cChapter) {
                    switch(chapter.getType()) {
                        case PLACEFOLDER:
                        case INPUT:
                            insertInputToDocument(text, chapter.currentItem(), r);
                            break;
                        case PARAGRAPH:
                            insertParagraphToDocument(chapter.currentItem(), p);
                            break;
                        case TABLE:
                            insertTableToDocument(chapter, p);
                            break;
                        default:
                    }
                }
                if(cChapter.get(0).getType().equals(TextStyle.PARAGRAPH) || cChapter.get(0).getType().equals(TextStyle.TABLE)) {
                    document.removeBodyElement(document.getPosOfParagraph(p));
                }
            }
        }
    }

    //region Description

    private void insertInputToDocument(String text, String input, XWPFRun r) {
        text = text.replace("TODO", (!input.equals("") ? input : notFoundField));
        setRun(r, FONT , 11, false, text);
    }

    private void insertParagraphToDocument(String input, XWPFParagraph p) {
        XmlCursor cursor = p.getCTP().newCursor();//this is the key!

        XWPFParagraph para = document.insertNewParagraph(cursor);

        setRun(para.createRun() , FONT , 11, false, (!input.equals("") ? input : notFoundField));
    }

    private void insertTableToDocument(ChapterList cChapter, XWPFParagraph p) {
        XmlCursor cursor = p.getCTP().newCursor();//this is the key!

        XWPFTable table = document.insertNewTbl(cursor);
        table.removeRow(0);

        XWPFParagraph paragraph;

        for(int i = 0; i < cChapter.result.get(0).size(); i++) {
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
                        (!cChapter.currentItem().equals("") ? cChapter.currentItem() : notFoundField));

                tableOneRowVersion.getCell(j).setWidth("1500");
            }
        }
    }

    //region end


    /**
     * Print paragraph text into table cell
     */

    public void setRun(XWPFRun run, String font, int size, boolean bold, String text) {
        run.setFontFamily(font);
        run.setFontSize(size);
        run.setText(text, 0);
        run.setBold(bold);
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

    public void setNewInput(List<Integer> h, List<String> inputList) {
        setNewChapter(h, Collections.singletonList(inputList), TextStyle.INPUT);
    }

    /**
     * Replace old inputs with a table field
     */

    public void setNewTable(List<Integer> h, List<List<String>> tablefield) {
        setNewChapter(h, tablefield, TextStyle.TABLE);
    }

    public void setNewTable(List<Integer> h, List<String> tableHeaders, List<String> tableContent) {

        List<List<String>> ll = new ArrayList<>();
        for(int i = 0; i < tableHeaders.size(); i++) {
            final int tempInt = i;
            List<String> temp = IntStream.range(i, tableContent.size())
                    .filter(n -> n % tableHeaders.size() == tempInt)
                    .mapToObj(tableContent::get)
                    .collect(Collectors.toList());
            temp.add(0, tableHeaders.get(i));
            ll.add(temp);
        }

        setNewChapter(h, ll, TextStyle.TABLE);
    }

    /**
     * Replace old inputs with a paragraph
     */
    public void setNewParagraph(List<Integer> h, String para) {
        setNewChapter(h, Collections.singletonList(Collections.singletonList(para)), TextStyle.PARAGRAPH);
    }

    private void setNewChapter(List<Integer> h, List<List<String>> input, TextStyle type) {
        if(!input.isEmpty()) {
            if(chapterList.get(h).get(0).getType().equals(TextStyle.PLACEFOLDER)) {
                chapterList.get(h).set(0, new ChapterList(h, input, type));
            }
            else {
                chapterList.get(h).add(new ChapterList(h, input, type));
            }
        }
    }

}
