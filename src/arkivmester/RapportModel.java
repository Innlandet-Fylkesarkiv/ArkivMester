package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.*;

// Used for parsing of xml schema and exception handling
public class RapportModel {

    public class ChapterList {
        private List<Integer> headers;
        private List<String> result;

        ChapterList(List<Integer> h) {
            headers = h;
            result = Arrays.asList("<Insert Input>");
        }

        public void setInput(List<Integer> h, List<String> inputList) {
            if(headers.equals(h)) result = inputList;
        }

    }

    public class HeadersData {
        private List<String> name;
        private Map<String, Integer> headerMap;

        HeadersData() {
            name = new ArrayList<>();
            headerMap = new LinkedHashMap<>();
        }

        public void compareName(String other) {

            boolean hit = false;
            for(int i = 0; i < headerMap.size() && !hit; i++)
            {
                if(headerMap.containsKey(other)) {
                    hit = true;
                    headerMap.put(other, headerMap.get(other) + 1);
                    name.add(other);
                    for(int j = name.size()-1; j > i; j--) {
                        headerMap.remove(name.get(j));
                    }
                }
            }
            while(name.size() > headerMap.size()) {
                name.remove(name.size()-1);
            }

            if(!hit) {
                headerMap.put(other, 1);
            }
        }

        public List<Integer> getValues() {
            return new ArrayList<>(headerMap.values());
        }
    }

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

    // Right know work as rapportModel.main in function
    public void start() {
        setUpReportDocument(templateFile);

        setUpAllInputChapters();

        writeReportDocument();

        printReportToFile(outputFile);
    }

    // Try to fetch report template, and if there are no IO problems, it will be stored

    private void setUpReportDocument(String filepath) {
        try (
                FileInputStream fis = new FileInputStream(filepath)
        ) {
            document = new XWPFDocument(fis);
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());             //NOSONAR
        }

    }

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

    private void writeReportDocument() {

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
    }

    private boolean foundNewHeader(XWPFParagraph p) {
        return (p.getStyle() != null && p.getStyleID().contains("Overskrift"));
    }

    private List<String> getNextChapterList() {
        if(chapterIterator.hasNext()) {
            return chapterIterator.next().result;
        }
        return Arrays.asList("");
    }

    private int editToFile(XWPFParagraph p, List<String> cList, int cIterator) {
        for(XWPFRun r : p.getRuns()) {
            String text = r.getText(0);
            if(text != null && text.contains("TODO") && !cList.get(cIterator).equals("")) {
                text = text.replace("TODO", cList.get(cIterator));
                r.setText(text, 0);
                r.setBold(false);
                cIterator = clamp(++cIterator, cList.size()-1);
            }
        }
        return cIterator;
    }

    public int clamp(int val, int max) {
        return Math.min(val, max);
    }

    private void printReportToFile(String filepath) {
        try {
            FileOutputStream os = new FileOutputStream(filepath);
            document.write(os);
            document.close();
            os.close();
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());                     // NOSONAR
        }
    }
}
