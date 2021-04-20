package arkivmester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArkadeModelTest {

    private final ArkadeModel arkadeModel = new ArkadeModel();

    @BeforeEach
    void readFromHtml(){

        String filePath = "Arkaderapport-899ec389-1dc0-41d0-b6ca-15f27642511b.html";

        try (InputStream is = getClass().getResourceAsStream("/"+filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String val;
            while ((val = br.readLine()) != null) {
                arkadeModel.htmlRawText.append(val);
            }
        } catch (Exception ex) {
            fail("Could not read from html file");
        }

    }

    @Test
    @DisplayName("Check: Sum all numbers in list")
    void testSumStringListWithOnlyNumbers(){

        int test1 = arkadeModel.sumStringListWithOnlyNumbers(Arrays.asList("2", "asdf2"));
        int test2 = arkadeModel.sumStringListWithOnlyNumbers(Arrays.asList("2", ""));
        int test3 = arkadeModel.sumStringListWithOnlyNumbers(Arrays.asList("2", "    a ", "3"));
        int test4 = arkadeModel.sumStringListWithOnlyNumbers(Collections.emptyList());

        assertEquals(4, test1 );
        assertEquals(-1, test2 );
        assertEquals(-1, test3 );
        assertEquals(0, test4 );
    }

    @Test
    @DisplayName("Check: Get one element from List<String> as Integer")
    void testGetOneElementInListAsInteger(){

        int test1 = arkadeModel.getOneElementInListAsInteger(Collections.singletonList("bob1"), "bob");
        int test2 = arkadeModel.getOneElementInListAsInteger(Arrays.asList("bob1", "Mathias1"), "bob");
        int test3 = arkadeModel.getOneElementInListAsInteger(Arrays.asList("bob1", "bob2"), "bob");
        int test4 = arkadeModel.getOneElementInListAsInteger(Collections.singletonList(""), "");

        assertEquals(1, test1 );
        assertEquals(1, test2 );
        assertEquals(-1, test3 );
        assertEquals(-1, test4 );
    }

    @Test
    @DisplayName("Check: Get year from string as integer")
    void testGetYearFromString(){

        int year1 = arkadeModel.getYearFromString("2012:2013", true);
        int year2 = arkadeModel.getYearFromString("2012:2013", false);

        assertEquals(2012, year1 );
        assertEquals(2013, year2 );
    }

    @Test
    @DisplayName("Check: Get Integer from string")
    void testGetStringNumberAsInteger(){

        int test1 = arkadeModel.getStringNumberAsInteger("1");
        int test2 = arkadeModel.getStringNumberAsInteger("");
        int test3 = arkadeModel.getStringNumberAsInteger("asdf");
        int test4 = arkadeModel.getStringNumberAsInteger("a3sdf");

        assertEquals(1, test1 );
        assertEquals(-1, test2 );
        assertEquals(-1, test3 );
        assertEquals(3, test4 );
    }

    @Test
    @DisplayName("Check: Get substring after last character")
    void testGetTextAt(){

        String test1 = arkadeModel.getTextAt(":xxx", ":");
        String test2 = arkadeModel.getTextAt(":xxx:", ":");
        String test3 = arkadeModel.getTextAt(":xxx", "");
        String test4 = arkadeModel.getTextAt("", "");

        assertEquals("xxx", test1 );
        assertEquals("", test2 );
        assertEquals("", test3 );
        assertEquals("", test4 );
    }

    @Test
    @DisplayName("Check: Get value between words")
    void testGetTextBetweenWords(){

        List<String> test1 = arkadeModel.getTextBetweenWords(Collections.singletonList("hey3hey4"), "3", "4");
        List<String> test2 = arkadeModel.getTextBetweenWords(Collections.singletonList("hey3hey4"), "4", "");
        List<String> test3 = arkadeModel.getTextBetweenWords(Collections.singletonList("hey3hey4"), "4", "3");
        List<String> test4 = arkadeModel.getTextBetweenWords(Collections.singletonList(""), "4", "3");
        List<String> test5 = arkadeModel.getTextBetweenWords(Collections.singletonList("4"), "", "4");

        assertEquals("hey", test1.get(0) );
        assertEquals("", test2.get(0) );
        assertTrue(test3.isEmpty());
        assertTrue(test4.isEmpty());
        assertEquals("", test5.get(0));
    }

    @Test
    @DisplayName("Check: Only keep elements in list with specific Values")
    void testGetSpecificValueInList(){

        List<String> test1 = arkadeModel.getSpecificValueInList(Collections.emptyList(), "");
        List<String> test2 = arkadeModel.getSpecificValueInList(Collections.singletonList("2:2"), ":");

        assertEquals(Collections.emptyList(), test1 );
        assertEquals("2:2", test2.get(0) );
    }

    @Test
    @DisplayName("Check: Get Arkade Version")
    void testGetArkadeVersion(){
        String test1 = arkadeModel.getArkadeVersion();

        assertEquals("Arkade 5 versjon: 2.2.1", test1);
    }

    @Test
    @DisplayName("Check: Get number of avvik")
    void testGetNumberOfDeviation(){
        Integer test1 = arkadeModel.getNumberOfDeviation();

        assertEquals(1009, test1);
    }

    @Test
    @DisplayName("Check: Uses getAllID than get deviation for every ID")
    void testGetAll(){
        List<String> test1 = arkadeModel.getAll();

        assertFalse(test1.isEmpty());
    }

    @Test
    @DisplayName("Check: Get every test Id in html")
    void testGetAllIDs(){
        List<String> test1 = arkadeModel.getAllIDs();

        assertEquals(53, test1.size());
    }

    @Test
    @DisplayName("Check: Get every systemID from one test index")
    void testGetSystemID(){
        List<String> test1 = arkadeModel.getSystemID("N5.27", "systemID");

        assertEquals(1, test1.size());
    }

    @Test
    @DisplayName("Check: Get one number from arkade list or get main Total value from list")
    void testGetTotal(){
        String TOTAL = "Totalt";
        Integer test1 = arkadeModel.getTotal("N5.08", TOTAL);
        Integer test2 = arkadeModel.getTotal("N5.08", "Totalt ");
        Integer test3 = arkadeModel.getTotal("N5.08", "klasser: 443");
        Integer test4 = arkadeModel.getTotal("N5.08", "");

        assertEquals(1742, test1);
        assertEquals(-1, test2);
        assertEquals(443, test3);
        assertEquals(-1, test4);
    }

    @Test
    @DisplayName("Check: Get every elements with subtext from arkade list")
    void testGetSpecificValue(){
        List<String> test1 = arkadeModel.getSpecificValue("N5.10", "Mappetype");

        assertEquals("Mappetype: moetemappe - Antall: 247", test1.get(0));
        assertEquals("Mappetype: saksmappe - Antall: 17", test1.get(1));
    }

    @Test
    @DisplayName("Check: Get from summary part of arkade")
    void testGetFromSummary(){
        List<String> test1 = arkadeModel.getFromSummary("Systemnavn", false);
        List<String> test2 = arkadeModel.getFromSummary("Systemnavn", true);
        List<String> test3 = arkadeModel.getFromSummary("896", true);

        assertEquals("Systemnavn", test1.get(0));
        assertEquals("ESA 8.3.3 (Sikri)", test2.get(0));
        assertTrue(test3.isEmpty());
    }

    @Test
    @DisplayName("Check: Gets elements from arkade list")
    void testGetDataFromHtml(){
        List<String> test1 = arkadeModel.getDataFromHtml("N5.05");

        assertEquals("", test1.get(0));
        assertEquals("Totalt: 1", test1.get(1));
        assertEquals(2, test1.size());
    }


}