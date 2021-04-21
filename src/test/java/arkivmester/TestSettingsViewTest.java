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

class TestSettingsViewTest extends Views {
    private TestSettingsView testSettingsView;
    private final Color primaryColor = new Color(8, 83, 148);
    private Container ctn;
    List <Boolean> selectedTests = new ArrayList<>();
    List <Boolean> selectedXqueries = new ArrayList<>();
    String[] customXqueryList = {"test1.xq", "test2.xq"};

    @BeforeEach
    void setUp() {
        selectedTests.add(true); selectedTests.add(true); selectedTests.add(true); selectedTests.add(true);
        selectedXqueries.add(false); selectedXqueries.add(false); selectedXqueries.add(false); selectedXqueries.add(false);

        testSettingsView = new TestSettingsView(selectedTests, selectedXqueries);
        MainView mainView = new MainView();

        mainView.createFrame();
        mainView.createAndShowGUI();

        ctn = mainView.getContainer();
        testSettingsView.createAndShowGUI(ctn, customXqueryList);
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
        assertEquals(new Point(0, 0), ctn.getComponent(0).getLocation(), "Top bar is not at the top");
        assertEquals(primaryColor, ctn.getComponent(0).getBackground(), "Top bar has wrong background color");

        Container testsPanel = (Container)ctn.getComponent(1);
        assertEquals(13, testsPanel.getComponentCount(), "Tests panel should have 13 total components");

        JCheckBox checkBox = (JCheckBox)testsPanel.getComponent(2);
        assertTrue( checkBox.isSelected(), "Checkbox 1 should checked");
        checkBox = (JCheckBox)testsPanel.getComponent(4);
        assertTrue( checkBox.isSelected(), "Checkbox 2 should checked");
        checkBox = (JCheckBox)testsPanel.getComponent(6);
        assertTrue( checkBox.isSelected(), "Checkbox 3 should checked");
        checkBox = (JCheckBox)testsPanel.getComponent(8);
        assertTrue( checkBox.isSelected(), "Checkbox 4 should checked");

        List<Component> elements = Arrays.asList(testsPanel.getComponents());
        assertAll("Buttons",
                () -> assertTrue(elements.get(10).isEnabled(), "Lagre admininfo button should be activated"),
                () -> assertTrue(elements.get(11).isEnabled(), "Avbryt button should be activated")
        );

        Container scrollPane = (Container)ctn.getComponent(2);
        assertEquals(3, scrollPane.getComponentCount(), "Scroll pane should have 3 total components");

        Container container = (Container)scrollPane.getComponent(0);
        Container xqueryPanel = (Container)container.getComponent(0);
        assertEquals(3, xqueryPanel.getComponentCount(), "XQuery panel should have 3 total components");

        checkBox = (JCheckBox)xqueryPanel.getComponent(1);
        assertFalse( checkBox.isSelected(), "XQuery checkbox 1 should not be checked");
        checkBox = (JCheckBox)xqueryPanel.getComponent(2);
        assertFalse( checkBox.isSelected(), "XQuery checkbox 2 should not be checked");

        JCheckBox checkBoxNew = (JCheckBox)testsPanel.getComponent(2);
        checkBoxNew.setSelected(false);
        JCheckBox checkBoxNewXquery = (JCheckBox)xqueryPanel.getComponent(1);
        checkBoxNewXquery.setSelected(true);

        List<Boolean> selectedTestsNew = testSettingsView.getSelectedTests();
        assertAll("Checkboxes",
                () -> assertFalse(selectedTestsNew.get(0), "Checkbox 1 is not false"),
                () -> assertTrue(selectedTestsNew.get(1), "Checkbox 2 is not true"),
                () -> assertTrue(selectedTestsNew.get(2), "Checkbox 3 is not true"),
                () -> assertTrue(selectedTestsNew.get(3), "Checkbox 4 is not true")
        );

        List<Boolean> selectedXqueriesNew = testSettingsView.getSelectedXqueries();
        assertAll("Checkboxes",
                () -> assertTrue(selectedXqueriesNew.get(0), "Checkbox 1 is not true"),
                () -> assertFalse(selectedXqueriesNew.get(1), "Checkbox 2 is not false")
        );
    }

    @Test
    void clearContainer() {
        testSettingsView.clearContainer();
        assertEquals(0, testSettingsView.container.getComponentCount(), "Container is not cleared");
    }

}