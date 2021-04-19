package arkivmester;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArkadeModelTest {

    private final ArkadeModel arkadeModel = new ArkadeModel();




    @Test
    @DisplayName("Check: Get year from string as integer")
    void testGetYearFromString(){

        int year1 = arkadeModel.getYearFromString("2012:2013", true);
        int year2 = arkadeModel.getYearFromString("2012:2013", false);

        assertEquals(2012, year1 );
        assertEquals(2013, year2 );
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
    @DisplayName("Check: Only keep elements in list with specific Values")
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

    /*
    getOneElementInListAsInteger
    getOneElementInListAsString
    // year
    getStringNumberAsInteger
    getNumberInTextAsString
    getTextAt
    getTextBetweenWords
    getSpecificValueInList
     */
    @Test
    @Disabled("Check: Most have html to read from. See readHtmlFileFromTestFolder. Read from html")
    void testReadFromHtml(){

        arkadeModel.readHtmlFileFromTestFolder();
        int size = arkadeModel.getAllIDs().size();
        System.out.println(arkadeModel.getAllIDs());

        assertEquals(53, size );
    }


}