package arkivmester;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Array;
import java.util.*;

class ReportModelTest {

    private final ReportModel reportModel = new ReportModel();


    //@BeforeEach = run this function before all other tests. f. ex. read from file
    @BeforeEach
    public void init(){
        reportModel.headersData = new ReportModel.HeadersData();
    }

    @Test
    @DisplayName("splits a string containing two ; into three strings")
    public void splitStringList() {
        List<String> testList = Arrays.asList("cell1; cell2; cell3");

        List<String> testResult = reportModel.splitIntoTable(testList);

        assertEquals(1, testList.size());
        assertEquals(3, testResult.size());
    }

    @Test
    @DisplayName("Will run a case where there should return 3 rows")
    public void getThreeRows() {
        List<String> testList = Arrays.asList("category1: category2: cell3:");

        int testResult = reportModel.getRows(testList);

        assertEquals(3, testResult);
    }

    @Test
    @DisplayName("Formats a list of integers into file name")
    public void formatChapterNumber() {
        List<Integer> testList = Arrays.asList(1, 2, 3);

        String testResult = reportModel.formatChapterNumber(testList);

        assertEquals("1.2.3.docx", testResult);
    }


    @Test
    @DisplayName("Adds 4 header chapters, and see if the correct header numbers are set up")
    public void lookForHeaders() {
        List<String> testList = Arrays.asList("Header1", "Header2", "Header2", "Header1");

        Map<List<Integer>, String> testResult = new HashMap<>();

        for(String testString : testList) {
            reportModel.headersData.compareName(testString);
            testResult.put(reportModel.headersData.getNumbering(), testString);
        }

        // These are the items that should be in the map
        assertTrue(testResult.containsKey(Arrays.asList(1)));
        assertTrue(testResult.containsKey(Arrays.asList(1, 1)));
        assertTrue(testResult.containsKey(Arrays.asList(1, 2)));
        assertTrue(testResult.containsKey(Arrays.asList(2)));

        // these are items that should be outside of the map in this case
        assertFalse(testResult.containsKey(Arrays.asList(1, 3)));
        assertFalse(testResult.containsKey(Arrays.asList(2, 1)));
    }

}