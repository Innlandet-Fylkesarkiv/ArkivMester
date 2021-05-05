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

class MainViewTest extends Views {

    private MainView mainView;
    private final Color primaryColor = new Color(8, 83, 148);

    @BeforeEach
    void setUp() {
        mainView = new MainView();

        mainView.createFrame();
        mainView.createAndShowGUI();
    }

    @Test
    @DisplayName("Ensure container is not empty")
    void checkContainer() {
        assertNotNull(mainView.getContainer(), "Container is empty");
    }

    @Test
    @DisplayName("Ensure GUI is created and shown")
    void validateGUI() {
        Container ctn = mainView.getContainer();

        assertEquals(3, ctn.getComponentCount(), "Should only be 3 panels");
        assertEquals(new Point(0, 0), ctn.getComponent(2).getLocation(), "Top bar is not at the top");
        assertEquals(primaryColor, ctn.getComponent(2).getBackground(), "Top bar has wrong background color");

        Container mainPanel = (Container)ctn.getComponent(0);
        assertEquals(6, mainPanel.getComponentCount(), "MainPanel should have 9 total components");

        List<Component> elements = Arrays.asList(mainPanel.getComponents());
        assertAll("Buttons",
                () -> assertFalse(elements.get(1).isVisible(), "Spinner should be invisible"),
                () -> assertTrue(elements.get(2).isEnabled(), "Last opp pakket uttrekk button should be activated"),
                () -> assertFalse(elements.get(4).isEnabled(), "Velg tester button should be deactivated"),
                () -> assertFalse(elements.get(5).isEnabled(), "Start testing button should be deactivated")
        );

        Container infoPanel = (Container)ctn.getComponent(1);
        assertEquals(1, infoPanel.getComponentCount(), "InfoPanel should have 1 components");
        Container gridPanel = (Container)infoPanel.getComponent(0);
        assertEquals(18, gridPanel.getComponentCount(), "GridPanel should have 1 components");

        JTextArea textArea = (JTextArea)gridPanel.getComponent(9);
        assertNotEquals(null, textArea.getText(), "Text area 1 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(10);
        assertNotEquals(null, textArea.getText(), "Text area 2 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(11);
        assertNotEquals(null, textArea.getText(), "Text area 3 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(12);
        assertNotEquals(null, textArea.getText(), "Text area 4 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(13);
        assertNotEquals(null, textArea.getText(), "Text area 5 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(14);
        assertNotEquals(null, textArea.getText(), "Text area 6 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(15);
        assertNotEquals(null, textArea.getText(), "Text area 7 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(16);
        assertNotEquals(null, textArea.getText(), "Text area 8 should not be empty");
        JButton button = (JButton)gridPanel.getComponent(17);
        assertFalse(button.isEnabled(), "Rediger admininfo button should be deactivated");


    }

    @Test
    @DisplayName("Upload archive")
    void uploadArchive() { //# NOSONAR 1 extra assertion
        Container ctn = mainView.getContainer();
        Container mainPanel = (Container)ctn.getComponent(0);
        Container infoPanel = (Container)ctn.getComponent(1);

        List<Component> elements = Arrays.asList(mainPanel.getComponents());

        mainView.loading(true);
        assertTrue(elements.get(1).isVisible(), "Spinner should be invisible");
        mainView.loading(false);
        assertFalse(elements.get(1).isVisible(), "Spinner should be invisible");

        mainView.activateButtons();
        assertAll("Buttons",
                () -> assertTrue(elements.get(2).isEnabled(), "Last opp pakket uttrekk button should be activated"),
                () -> assertTrue(elements.get(4).isEnabled(), "Velg tester button should be activated"),
                () -> assertTrue(elements.get(5).isEnabled(), "Start testing button should be activated")
        );

        mainView.resetManualInfo();
        Container gridPanel = (Container)infoPanel.getComponent(0);
        assertEquals(18, gridPanel.getComponentCount(), "GridPanel should have 1 components");

        JTextArea textArea = (JTextArea)gridPanel.getComponent(9);
        assertNotEquals(null, textArea.getText(), "Text area 1 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(10);
        assertNotEquals(null, textArea.getText(), "Text area 2 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(11);
        assertNotEquals(null, textArea.getText(), "Text area 3 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(12);
        assertNotEquals(null, textArea.getText(), "Text area 4 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(13);
        assertNotEquals(null, textArea.getText(), "Text area 5 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(14);
        assertNotEquals(null, textArea.getText(), "Text area 6 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(15);
        assertNotEquals(null, textArea.getText(), "Text area 7 should not be empty");
        textArea = (JTextArea)gridPanel.getComponent(16);
        assertNotEquals(null, textArea.getText(), "Text area 8 should not be empty");



        List<String> adminInfoList = new ArrayList<>();
        adminInfoList.add(""); adminInfoList.add("Customer"); adminInfoList.add("John Doe");
        adminInfoList.add("Noark5 v3.1"); adminInfoList.add("15.04.2019"); adminInfoList.add("");
        adminInfoList.add(""); adminInfoList.add("15.04.2021");

        mainView.updateAdminInfo(adminInfoList);
        textArea = (JTextArea)gridPanel.getComponent(9);
        assertEquals("", textArea.getText(), "Text area 1 should be an empty string");
        textArea = (JTextArea)gridPanel.getComponent(10);
        assertEquals("Customer", textArea.getText(), "Text area 2 should contain Customer");
        textArea = (JTextArea)gridPanel.getComponent(11);
        assertEquals("John Doe", textArea.getText(), "Text area 3 should contain John Doe");
        textArea = (JTextArea)gridPanel.getComponent(12);
        assertEquals("Noark5 v3.1", textArea.getText(), "Text area 4 should contain Noark5 v3.1");
        textArea = (JTextArea)gridPanel.getComponent(13);
        assertEquals("15.04.2019", textArea.getText(), "Text area 5 should contain 15.04.2019");
        textArea = (JTextArea)gridPanel.getComponent(14);
        assertEquals("", textArea.getText(), "Text area 6 should be an empty string");
        textArea = (JTextArea)gridPanel.getComponent(15);
        assertEquals("", textArea.getText(), "Text area 7 should be an empty string");
        textArea = (JTextArea)gridPanel.getComponent(16);
        assertEquals("15.04.2021", textArea.getText(), "Text area 8 should contain 15.04.2021");
        JButton button = (JButton)gridPanel.getComponent(17);
        assertTrue(button.isEnabled(), "Rediger admininfo button should be activated");
    }
}