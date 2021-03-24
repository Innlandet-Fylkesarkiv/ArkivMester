package arkivmester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestViewTest {

    private TestView testView;
    private final Color primaryColor = new Color(8, 83, 148);
    private Container ctn;

    @BeforeEach
    void setUp() {
        testView = new TestView();
        MainView mainView = new MainView();

        mainView.createFrame();
        mainView.createAndShowGUI();

        ctn = mainView.getContainer();
        testView.createAndShowGUI(ctn);
    }

    @Test
    @DisplayName("Ensure container is not empty")
    void checkContainer() {
        assertNotNull(ctn, "Container is empty");
    }

    @Test
    @DisplayName("Ensure GUI is created and shown")
    void validateGUI() {
        assertEquals(3, ctn.getComponentCount(), "Should only be 3 panels");
        assertEquals(new Point(0, 0), ctn.getComponent(1).getLocation(), "Top bar is not at the top");
        assertEquals(primaryColor, ctn.getComponent(1).getBackground(), "Top bar has wrong background color");
        assertEquals(new Point(650, 66), ctn.getComponent(0).getLocation(), "Top bar is not at the top");

        Container testContainer = (Container)ctn.getComponent(2);
        assertEquals(3, testContainer.getComponentCount(), "Test container should have 3 panels");

        Container testPanel = (Container)testContainer.getComponent(0);
        assertEquals(15, testPanel.getComponentCount(), "TestPanel should have 15 total components, 3 per test");

        Container statusPanel = (Container)testContainer.getComponent(1);
        assertEquals(1, statusPanel.getComponentCount(), "StatusPanel should only have 1 component");
        JLabel statusLabel = (JLabel)statusPanel.getComponent(0);
        assertNotEquals("", statusLabel.getText(), "Status label should not be empty");

        Container buttonPanel = (Container)testContainer.getComponent(2);
        assertEquals(4, buttonPanel.getComponentCount(), "ButtonPanel should have 3 buttons and 1 dropdown");

        List<Component> buttons = Arrays.asList(buttonPanel.getComponents());
        assertAll("Buttons",
                () -> assertTrue(buttons.get(0).isEnabled(), "Dropdown menu should be activated"),
                () -> assertFalse(buttons.get(1).isEnabled(), "Make report button should be deactivated"),
                () -> assertFalse(buttons.get(2).isEnabled(), "Pakk til AIP button should be deactivated"),
                () -> assertTrue(buttons.get(3).isEnabled(), "Test nytt uttrekk button should be activated")
                );

    }

    @Test
    void clearContainer() {
        testView.clearContainer();
        assertEquals(0, testView.container.getComponentCount(), "Container is not cleared");
    }

    @Test
    void activateCreateReportBtn() {
        Container testContainer = (Container)ctn.getComponent(2);
        Container buttonPanel = (Container)testContainer.getComponent(2);
        List<Component> buttons = Arrays.asList(buttonPanel.getComponents());

        assertFalse(buttons.get(1).isEnabled(), "Make report button should be deactivated before activating it");
        testView.activateCreateReportBtn();
        assertTrue(buttons.get(1).isEnabled(), "Make report button is not activated after being activated");
    }

    @Test
    void activatePackToAipBtn() {
        Container testContainer = (Container)ctn.getComponent(2);
        Container buttonPanel = (Container)testContainer.getComponent(2);
        List<Component> buttons = Arrays.asList(buttonPanel.getComponents());

        assertFalse(buttons.get(2).isEnabled(), "Pakk til AIP button should be deactivated before activating it");
        testView.activatePackToAipBtn();
        assertTrue(buttons.get(2).isEnabled(), "Pakk til AIP button is not activated after being activated");
    }

    @Test
    void updateStatus() {
        Container testContainer = (Container)ctn.getComponent(2);
        Container testPanel = (Container)testContainer.getComponent(0);
        List<Boolean> mockSelectedTests = Arrays.asList(true, true, false, true);

        List<JLabel> labels = new ArrayList<>();
        for(int i = 0; i<testPanel.getComponentCount(); i++) {
            labels.add((JLabel)testPanel.getComponent(i));
        }

        assertAll("Default status label text",
                () -> assertEquals(TestView.WAITING, labels.get(1).getText(),"First test status is wrong"),
                () -> assertEquals(TestView.WAITING, labels.get(4).getText(),"Second test status is wrong"),
                () -> assertEquals(TestView.WAITING, labels.get(7).getText(),"Third test status is wrong"),
                () -> assertEquals(TestView.WAITING, labels.get(10).getText(),"Fourth test status is wrong")
        );

        //testView.updateStatus(mockSelectedTests);

        assertAll("Status label text after being updated",
                () -> assertEquals(TestView.WAITING, labels.get(1).getText(),"First test status is wrong"),
                () -> assertEquals(TestView.WAITING, labels.get(4).getText(),"Second test status is wrong"),
                () -> assertEquals(TestView.NONE, labels.get(7).getText(),"Third test status is wrong"),
                () -> assertEquals(TestView.WAITING, labels.get(10).getText(),"Fourth test status is wrong")
        );
    }
}