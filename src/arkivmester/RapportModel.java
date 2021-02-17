package arkivmester;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Used for parsing of xml schema and exception handling
public class RapportModel {

    private class ChapterList {
        private final int first;        // NOSONAR - kan brukes seinere om vi trenger
        private final int second;       // NOSONAR - å legge til at den skal hente
        private final int third;        // NOSONAR - spesifik tabell i listen
        private final String[] result;

        ChapterList(int f, int s, int t, String[] r) {
            first = f;
            second = s;
            third = t;
            result = r;
        }
    }

    XWPFDocument document;
    String templateFile = "resources/Dokumentmal_fylkesarkivet_Noark5_testrapport.docx";
    String outputFile = "C:/prog/Output/report_template.docx";

    List<ChapterList> chapterList = new ArrayList<>();
    private Iterator<ChapterList> chapterIterator = null;

    RapportModel() {
        //Rapport
        //kap 1, 1.1, 1.2
    }

    // Right know work as rapportModel.main in function
    public void start() {

        setUpReportDocument(templateFile);

        chapterList = getInputFromTests();

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

    private void writeReportDocument() {

        chapterIterator = chapterList.iterator();

        String[] currentChapterInput = new String[] {""};
        int currentIterator = 0;

        Iterator<IBodyElement> bodyElementIterator = document.getBodyElementsIterator();

        while(bodyElementIterator.hasNext()) {
            IBodyElement element = bodyElementIterator.next();
            if(element instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph)element;
                if(foundNewHeader(p)) {
                    currentChapterInput = getNextChapterList();
                    currentIterator = 0;
                }
                if(editToFile(p, currentChapterInput[currentIterator])) {
                    currentIterator = clamp(++currentIterator, currentChapterInput.length-1);
                }
            }
        }
    }

    private boolean editToFile(XWPFParagraph p, String currentText) {
        for(XWPFRun r : p.getRuns()) {
            String text = r.getText(0);
            if(text != null && text.contains("TODO") && !currentText.equals("")) {
                System.out.println(currentText);                                    //NOSONAR
                text = text.replace("TODO", currentText);
                r.setText(text, 0);
                r.setBold(false);
                return true;
            }
        }
        return false;
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

    private boolean foundNewHeader(XWPFParagraph p) {

        XWPFStyles styles = document.getStyles();

        if(p.getStyle() != null) {
            XWPFStyle style = styles.getStyle(p.getStyleID());
            return (style.getName().equals("Overskrift 1 Grønn")
                    || style.getName().equals("Overskrift 2 Grønn")
                    || style.getName().equals("heading 3"));
        }
        return false;
    }

    public int clamp(int val, int max) {
        return Math.min(val, max);
    }

    private String[] getNextChapterList() {
        if(chapterIterator.hasNext()) {
            return chapterIterator.next().result;
        }
        return new String[] {""};
    }

    private List<ChapterList> getInputFromTests() {
        List<ChapterList> temp = new ArrayList<>();

        temp.add(chapters(1));

        temp.add(chapters(1, 1, new String[]{"@1_1#1", "@1_1#2", "@1_1#3", "@1_1#4", "@1_1#5", "@1_1#6", "@1_1#7", "@1_1#8"}));

        temp.add(chapters(1, 2, new String[]{"@1_2#1", "@1_2#2", "@1_2#3", "@1_2#4", "@1_2#5", "@1_2#6", "@1_2#7", "@1_2#8", "@1_2#9"}));

        temp.add(chapters(1, 3));

        temp.add(chapters(2));

        temp.add(chapters(2, 1, new String[]{"@2_1#1"}));

        temp.add(chapters(2, 2, new String[]{"@2_2#1"}));

        temp.add(chapters(2, 3, new String[]{"@2_3#1"}));

        temp.add(chapters(3));

        temp.add(chapters(3, 1, new String[]{"@3_1#1"}));

        temp.add(chapters(3, 1, 1, new String[]{"@3_1_1#1"}));

        temp.add(chapters(3, 1, 2, new String[]{"@3_1_2#1"}));

        temp.add(chapters(3, 1, 3, new String[]{"@3_1_3#1"}));

        temp.add(chapters(3, 1, 4, new String[]{"@3_1_4#1"}));

        temp.add(chapters(3, 1, 5, new String[]{"@3_1_5#1"}));

        temp.add(chapters(3, 1, 6, new String[]{"@3_1_6#1"}));

        temp.add(chapters(3, 1, 7, new String[]{"@3_1_7#1"}));

        temp.add(chapters(3, 1, 8, new String[]{"@3_1_8#1"}));

        temp.add(chapters(3, 1, 9, new String[]{"@3_1_9#1"}));

        temp.add(chapters(3, 1, 10));

        temp.add(chapters(3, 1, 11, new String[]{"@3_1_11#1"}));

        temp.add(chapters(3, 1, 12, new String[]{"@3_1_12#1"}));

        temp.add(chapters(3, 1, 13, new String[]{"@3_1_13#1"}));

        temp.add(chapters(3, 1, 14, new String[]{"@3_1_14#1"}));

        temp.add(chapters(3, 1, 15, new String[]{"@3_1_15#1"}));

        temp.add(chapters(3, 1, 16, new String[]{"@3_1_16#1"}));

        temp.add(chapters(3, 1, 17, new String[]{"@3_1_17#1"}));

        temp.add(chapters(3, 1, 18, new String[]{"@3_1_18#1"}));

        temp.add(chapters(3, 1, 19, new String[]{"@3_1_19#1"}));

        temp.add(chapters(3, 1, 20, new String[]{"@3_1_20#1"}));

        temp.add(chapters(3, 1, 21, new String[]{"@3_1_21#1"}));

        temp.add(chapters(3, 1, 22, new String[]{"@3_1_22#1"}));

        temp.add(chapters(3, 1, 23, new String[]{"@3_1_23#1"}));

        temp.add(chapters(3, 1, 24, new String[]{"@3_1_24#1"}));

        temp.add(chapters(3, 1, 25, new String[]{"@3_1_25#1"}));

        temp.add(chapters(3, 1, 26, new String[]{"@3_1_26#1"}));

        temp.add(chapters(3, 1, 27, new String[]{"@3_1_27#1"}));

        temp.add(chapters(3, 1, 28, new String[]{"@3_1_28#1"}));

        temp.add(chapters(3, 1, 29, new String[]{"@3_1_29#1"}));

        temp.add(chapters(3, 1, 30, new String[]{"@3_1_30#1"}));

        temp.add(chapters(3, 1, 31));

        temp.add(chapters(3, 1, 32, new String[]{"@3_1_32#1"}));

        temp.add(chapters(3, 1, 33, new String[]{"@3_1_33#1"}));

        temp.add(chapters(3, 2, new String[]{"@3_2#1", "@3_2#2", "@3_2#3", "@3_2#4", "@3_2#5"}));

        temp.add(chapters(3, 2, 1, new String[]{"@3_2_1#1"}));

        temp.add(chapters(3, 3));

        temp.add(chapters(3, 3, 1, new String[]{"@3_3_1#1"}));

        temp.add(chapters(3, 3, 2, new String[]{"@3_3_2#1"}));

        temp.add(chapters(3, 3, 3, new String[]{"@3_3_3#1"}));

        temp.add(chapters(3, 3, 4, new String[]{"@3_3_4#1"}));

        temp.add(chapters(3, 3, 5, new String[]{"@3_3_5#1"}));

        temp.add(chapters(3, 3, 6, new String[]{"@3_3_6#1"}));

        temp.add(chapters(3, 3, 7, new String[]{"@3_3_7#1"}));

        temp.add(chapters(3, 3, 8, new String[]{"@3_3_8#1"}));

        temp.add(chapters(3, 3, 9, new String[]{"@3_3_9#1"}));

        temp.add(chapters(3, 3, 10, new String[]{"@3_3_10#1"}));

        temp.add(chapters(4));

        temp.add(chapters(4, 1, new String[]{"@4_1#1"}));

        temp.add(chapters(4, 2));

        temp.add(chapters(4, 2, 1, new String[]{"@4_2_1#1"}));

        temp.add(chapters(4, 2, 2));

        temp.add(chapters(4, 2, 3));

        temp.add(chapters(5, new String[]{"@5#1"}));

        return temp;
    }

    private ChapterList chapters(int h1) {
        return new ChapterList(h1, 0, 0, new String[] {""});
    }

    private ChapterList chapters(int h1, String[] items) {
        return new ChapterList(h1, 0, 0, items);
    }

    private ChapterList chapters(int h1, int h2) {
        return new ChapterList(h1, h2, 0, new String[] {""});
    }

    private ChapterList chapters(int h1, int h2, String[] items) {
        return new ChapterList(h1, h2, 0, items);
    }

    private ChapterList chapters(int h1, int h2, int h3) {
        return new ChapterList(h1, h2, h3, new String[] {""});
    }

    private ChapterList chapters(int h1, int h2, int h3, String[] items) {
        return new ChapterList(h1, h2, h3, items);
    }

}
