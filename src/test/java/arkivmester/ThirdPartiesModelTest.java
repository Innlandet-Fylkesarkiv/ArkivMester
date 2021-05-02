package arkivmester;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThirdPartiesModelTest {

    private final ThirdPartiesModel thirdPartiesModel = new ThirdPartiesModel();

    @Test
    @DisplayName("Check if selected tests gets updated")
    void updateSelectedTests() {
        List<Boolean> selectedTests;
        List<Boolean> updatedTests = Arrays.asList(Boolean.TRUE,Boolean.FALSE,Boolean.TRUE, Boolean.TRUE);
        //thirdPartiesModel.updateTests(updatedTests);
        selectedTests = thirdPartiesModel.getSelectedTests();
        assertEquals(updatedTests,selectedTests, "One boolean should be false." );

    }


    @Test
    @DisplayName("Check if selected tests is reset to all true.")
    void resetSelectedTests() {
        List<Boolean> selectedTests;
        List<Boolean> updatedTests = Arrays.asList(Boolean.TRUE,Boolean.FALSE,Boolean.TRUE, Boolean.TRUE);
        //thirdPartiesModel.updateSelectedTests(updatedTests);
        thirdPartiesModel.resetSelectedTests();
        selectedTests = thirdPartiesModel.getSelectedTests();
        assertEquals(Arrays.asList(Boolean.TRUE,Boolean.TRUE,Boolean.TRUE, Boolean.TRUE), selectedTests, "All should be true" );
    }

}