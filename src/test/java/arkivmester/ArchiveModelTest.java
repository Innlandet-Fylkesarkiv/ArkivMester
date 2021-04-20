package arkivmester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveModelTest {

    ArchiveModel archiveModel;

    @BeforeEach
    void setUp() {
        archiveModel = new ArchiveModel();
    }

    @Test
    @DisplayName("Ensure constructor initiated properly")
    void checkConstructor() {
        List<String> expectedAdminInfoList = Arrays.asList("", "", "", "", "", "", "", "");

        assertEquals(archiveModel.getAdminInfo(), expectedAdminInfoList, "AdminInfoList is not empty strings");


    }

    @Test
    @DisplayName("Ensure upload folder works")
    void uploadFolderTest() {
        archiveModel.uploadFolder(new Container());

        assertEquals("Arkadepakke-4b24f025-3c3a-4dd6-a371-7dc1b9143452", archiveModel.folder.getName(), "Folder name is incorrect");
        assertEquals("4b24f025-3c3a-4dd6-a371-7dc1b9143452.tar", archiveModel.tar.getName(), "Tar name is incorrect");
        assertEquals("4b24f025-3c3a-4dd6-a371-7dc1b9143452.xml", archiveModel.xmlMeta.getName(), "Xmlmeta name is incorrect");
    }

    @Test
    @DisplayName("Ensure updating and resetting the admin info list works")
    void updateAdminInfoTest() {
        List<String> newList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
        List<String> resetList = Arrays.asList("", "", "", "", "", "", "", "");

        archiveModel.updateAdminInfo(newList);
        List<String> updatedList = archiveModel.getAdminInfo();

        assertEquals(8, updatedList.size(), "Updated list has different size");

        assertEquals("3", updatedList.get(2), "Index 4 is incorrect");

        assertEquals(updatedList, newList, "Updated list is incorrect");

        archiveModel.resetAdminInfo();
        assertEquals(archiveModel.getAdminInfo(), resetList, "AdminInfoList reset incorrectly");
    }

    @Test
    @DisplayName("Ensure formatting the date in admin info list works")
    void formatDate() {
        List<String> adminInfoList = Arrays.asList("", "", "", "", "2019-11-20T00:00:00", "", "", "");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyy");
        LocalDateTime now = LocalDateTime.now();
        String today = dtf.format(now);

        List<String> updatedList = archiveModel.formatDate(adminInfoList);

        assertEquals(8, updatedList.size(), "Updated list has different size");
        assertEquals("20.11.2019", updatedList.get(4), "Formatted date is incorrect");
        assertEquals(today, updatedList.get(7), "Current date is incorrect");


        List<String> emptyList = Arrays.asList("", "", "", "", "", "", "", "");
        List<String> updatedList2 = archiveModel.formatDate(emptyList);

        assertEquals(8, updatedList2.size(), "Updated list has different size");
        assertEquals("", updatedList2.get(4), "Formatted date is incorrect");
        assertEquals(today, updatedList2.get(7), "Current date is incorrect");
    }

    @Test
    @DisplayName("Ensure dates in admin info list are validated")
    void validateDates() {


        try {
            String prodDate = "20.11.2019";
            String recievedDate = "20.12.2019";
            String reportDate = "19.02.2021";

            Boolean success = archiveModel.validateDates(prodDate, recievedDate, reportDate);

            assertEquals(true, success, "Validation should be correct");

        } catch (ParseException e) {
            fail("Unintended exception happened");
        }

        try {
            String prodDate = "20.11.2019";
            String recievedDate = "";
            String reportDate = "19.02.2021";

            Boolean success = archiveModel.validateDates(prodDate, recievedDate, reportDate);

            assertEquals(false, success, "Validation should be correct");

        } catch (ParseException e) {
            fail("Unintended exception happened");
        }


        String prodDate = "20.11.2019";
        String recievedDate = "5";
        String reportDate = "19.02.2021";

        assertThrows(ParseException.class, () -> archiveModel.validateDates(prodDate, recievedDate, reportDate));
    }
}