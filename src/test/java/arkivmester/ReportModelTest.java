package arkivmester;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Array;
import java.util.Arrays;

class ReportModelTest {

    private final ReportModel reportModel = new ReportModel();


    //@BeforeEach = run this function before all other tests. f. ex. read from file
    @BeforeEach
    public void init(){
        //reportModel.init(prop, xqueryResults);
        //reportModel.generateReport();
        //reportModel.setNewInput(Arrays.asList(1, 1), archiveModel.getAdminInfo());

        //reportModel.makeReport();
    }
    @Test
    @DisplayName("What this does")
    public void generateReportTest(){
        int x = 2;

        assertEquals(4, x );
    }

}