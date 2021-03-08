package arkivmester;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThirdPartiesModelTest {

    private ThirdPartiesModel thirdPartiesModel;

    @Test
    @DisplayName("Check if selected tests gets updated")
    void updateSelectedTests() {
        thirdPartiesModel = new ThirdPartiesModel();
        List<Boolean> selectedTests;
        List<Boolean> updatedTests = Arrays.asList(Boolean.TRUE,Boolean.FALSE,Boolean.TRUE, Boolean.TRUE);
        thirdPartiesModel.updateSelectedTests(updatedTests);
        selectedTests = thirdPartiesModel.getSelectedTests();
        assertEquals(updatedTests,selectedTests, "One test should be false" );

    }


    @Test
    void resetSelectedTests() {
        
    }

    @Test
    void runArkadeTest() {
    }

    @Test
    void runKostVal() {
    }

    @Test
    void runVeraPDF() {
    }

    @Test
    void runDROID() {
    }

    @Test
    void unzipArchive() {
    }

    @Test
    void runBaseX() {
    }
}