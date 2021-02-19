package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.Document;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Used for parsing of xml schema and exception handling
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

    public class ChapterList {
        private List<Integer> headers;
        private List<String> result;

        ChapterList(List<Integer> h) {
            headers = h.stream().filter(t -> t > 0).collect(Collectors.toList());
            result = Arrays.asList("<Mangler verdi>");
        }

        public void setInput(List<Integer> h, List<String> inputList) {
            if(headers.equals(h)) result = inputList;
        }

        public void getText() {
            for (int i = 0; i < headers.size(); i++) {
                System.out.print(headers.get(i) + " ");
            }
            for (int i = 0; i < result.size(); i++) {
                System.out.print(result.get(i) + " ");
            }
            System.out.print('\n');
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

            if(headerMap.containsKey(other)) {
                System.out.println(other + ": ");
                hit = true;
                headerMap.put(other, headerMap.get(other) + 1);

                int temp = name.size()-1;
                String currentName = name.get(temp);
                while(other != currentName) {
                    System.out.println(currentName + " removed!");
                    headerMap.put(currentName, 0);
                    currentName = name.get(--temp);
                }
            }
            headerMap.forEach((k, v) -> System.out.println("\t" + k + " " + v));

            while(name.size() > headerMap.size()) {
                name.remove(name.size()-1);
            }

            if(!hit) {
                System.out.println(other + " added!");
                headerMap.put(other, 1);
                name.add(other);
            }
        }

        public List<Integer> getValues() {
            return new ArrayList<>(headerMap.values());
        }

    }


    // -------------------------


    // Right know work as rapportModel.main in function
    public void generateReport() {
        setUpReportDocument(templateFile);

        setUpAllInputChapters();

        //writeReportDocument();

        //printReportToFile(outputFile);
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

    public void setNewInput(List<Integer> h, List<String> inputList) {
        chapterList.forEach(
                t -> t.setInput(h, inputList)
        );
    }

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

        chapterList.forEach(
                t -> t.getText()
        );
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
}
