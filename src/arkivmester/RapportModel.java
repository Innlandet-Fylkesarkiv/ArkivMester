package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling report document configurations.
 */

public class RapportModel {

    XWPFDocument document;
    String templateFile = "resources/Dokumentmal_fylkesarkivet_Noark5_testrapport.docx";
    String outputFile = "C:/prog/Output/report_template.docx";

    List<ChapterList> chapterList = new ArrayList<>();
    private Iterator<ChapterList> chapterIterator = null;
    private HeadersData headersData = new HeadersData();

    RapportModel() {
        //Rapport
        //kap 1, 1.1, 1.2

    }

    /**
     * Class for storing input of each chapter section of the report.
     */

    public class ChapterList {
        private List<Integer> headers;
        private List<String> result;

        /**
         * Initialize a default list of missing input.
         */

        ChapterList(List<Integer> h) {
            headers = h.stream().filter(t -> t > 0).collect(Collectors.toList());
            result = Arrays.asList("<Mangler verdi>");
        }

        /**
         * If chapter number is correct, set new input list value to chapter-section.
         */

        public void setInput(List<Integer> h, List<String> inputList) {
            if(headers.equals(h)) result = inputList;
        }

        /**
         * Prints text of data stored.
         */

        public void getText() {
            for (int i = 0; i < headers.size(); i++) {
                System.out.print(headers.get(i) + " ");     // NOSONAR
            }
            for (int i = 0; i < result.size(); i++) {
                System.out.print(result.get(i) + " ");      // NOSONAR
            }
            System.out.print('\n');                         // NOSONAR
        }
    }

    /**
     * Class for handling headers that are fetched from document.
     */

    public class HeadersData {
        private List<String> name;
        private Map<String, Integer> headerMap;

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
                //System.out.println(other + ": ");                   // NOSONAR
                hit = true;
                int temp = name.size()-1;
                String currentName = name.get(temp);
                while(!other.equals(currentName)) {
                    //System.out.println(currentName + " removed!");  // NOSONAR
                    headerMap.put(currentName, 0);
                    currentName = name.get(--temp);
                }
            }
            //headerMap.forEach((k, v) -> System.out.println("\t" + k + " " + v));    // NOSONAR

            while(name.size() > headerMap.size()) {
                name.remove(name.size()-1);
            }

            if(!hit) {
                //System.out.println(other + " added!");                              // NOSONAR
                headerMap.put(other, 1);
                name.add(other);
            }
        }

        /**
         * Get values
         */

        public List<Integer> getValues() {
            return new ArrayList<>(headerMap.values());
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

                findNewHeader(p);
            }
        }
    }

    /**
     * Check
     */

    private void findNewHeader(XWPFParagraph p) {
        XWPFStyles styles = document.getStyles();

        if(p.getStyle() != null) {

            XWPFStyle style = styles.getStyle(p.getStyleID());

            if(style.getStyleId().contains("Overskrift"))
            {
                headersData.compareName(style.getName());

                chapterList.add(new ChapterList(headersData.getValues()));
            }
        }
    }

    /**
     * Replace every missing input with input fetched from program
     */

    public void writeReportDocument() {

        Iterator<IBodyElement> bodyElementIterator = document.getBodyElementsIterator();

        chapterIterator = chapterList.iterator();

        List<String> currentChapterInput = new ArrayList<>();
        int currentIterator = 0;

        while(bodyElementIterator.hasNext()) {
            IBodyElement element = bodyElementIterator.next();
            if(element instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph)element;

                if(foundNewHeader(p)) {
                    currentChapterInput = getNextChapterList();
                    currentIterator = 0;
                }

                currentIterator = editToFile(p, currentChapterInput, currentIterator);
            }
        }

        for (ChapterList chapter : chapterList) {
            chapter.getText();
        }
    }

    /**
     * Replace every missing input with input fetched from program
     */

    private boolean foundNewHeader(XWPFParagraph p) {
        return (p.getStyle() != null && p.getStyleID().contains("Overskrift"));
    }

    /**
     * Used for iterating the values in chapterlist in WriteReportDocument
     */

    private List<String> getNextChapterList() {
        if(chapterIterator.hasNext()) {
            return chapterIterator.next().result;
        }
        return Arrays.asList("");
    }

    /**
     * Will look for input field in each paragraph and replace it with the ones from the list
     */

    private int editToFile(XWPFParagraph p, List<String> cList, int cIterator) {
        for(XWPFRun r : p.getRuns()) {
            String text = r.getText(0);
            if(text != null && text.contains("TODO")) {
                text = text.replace("TODO", cList.get(cIterator));
                r.setText(text, 0);
                r.setBold(false);
                cIterator = clamp(++cIterator, cList.size()-1);
            }
        }
        return cIterator;
    }

    /**
     * Will not clamp the max value so it does not go "out of bounds"
     */

    public int clamp(int val, int max) {
        return Math.min(val, max);
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
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());                     // NOSONAR
        }
    }

    /**
     * Replace old inputs with new ones
     */

    public void setNewInput(List<Integer> h, List<String> inputList) {
        for(ChapterList c : chapterList) {
            c.setInput(h, inputList);
        }
    }
}
