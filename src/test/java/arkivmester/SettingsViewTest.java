package arkivmester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SettingsViewTest extends Views {
    private SettingsView settingsView;
    private final Color primaryColor = new Color(8, 83, 148);
    private Container ctn;
    Properties prop;

    @BeforeEach
    void setUp() {
        settingsView = new SettingsView();
        MainView mainView = new MainView();

        prop = new Properties();
        InputStream is = getClass().getResourceAsStream("/config.properties");
        try {
            prop.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace(); //#NOSONAR
        }


        mainView.createFrame();
        mainView.createAndShowGUI();

        ctn = mainView.getContainer();
        settingsView.createAndShowGUI(ctn, prop);
    }

    @Test
    @DisplayName("Ensure container is not empty")
    void checkContainer() {
        assertNotNull(ctn, "Container is empty");
    }

    @Test
    @DisplayName("Ensure GUI is created and shown")
    void validateGUI() { // #NOSONAR

        assertEquals(2, ctn.getComponentCount(), "Should only be 2 panels");
        assertEquals(new Point(0, 0), ctn.getComponent(0).getLocation(), "Top bar is not at the top");
        assertEquals(primaryColor, ctn.getComponent(0).getBackground(), "Top bar has wrong background color");

        Container cfgPanel = (Container)ctn.getComponent(1);
        assertEquals(1, cfgPanel.getComponentCount(), "Config panel should have 1 total components");

        Container scrollPane = (Container)cfgPanel.getComponent(0);
        assertEquals(3, scrollPane.getComponentCount(), "Scroll pane should have 3 total components");

        assertEquals(10, prop.size(), "Config should contain 10 properties");

        Container container = (Container)scrollPane.getComponent(0);
        Container gridPanel = (Container)container.getComponent(0);
        assertEquals(28, gridPanel.getComponentCount(), "Grid panel should have 28 total components");

        assertAll("Labels",
                () -> assertEquals("7ZipPath", ((JLabel)gridPanel.getComponent(1)).getText(), "Col1, Row1 is incorrect"),
                () -> assertEquals("C:\\Programfiler\\7-Zip", ((JLabel)gridPanel.getComponent(9)).getText(), "Col2, Row1 is incorrect"),
                () -> assertEquals("veraPDFPath", ((JLabel)gridPanel.getComponent(2)).getText(), "Col1, Row2 is incorrect"),
                () -> assertEquals("C:\\prog\\VeraPDF", ((JLabel)gridPanel.getComponent(10)).getText(), "Col2, Row2 is incorrect"),
                () -> assertEquals("kostvalPath", ((JLabel)gridPanel.getComponent(3)).getText(), "Col1, Row3 is incorrect"),
                () -> assertEquals("C:\\prog\\KOSTVal", ((JLabel)gridPanel.getComponent(11)).getText(), "Col2, Row3 is incorrect"),
                () -> assertEquals("xqueryExtFolder", ((JLabel)gridPanel.getComponent(4)).getText(), "Col1, Row4 is incorrect"),
                () -> assertEquals("E:\\XQuery-Statements", ((JLabel)gridPanel.getComponent(12)).getText(), "Col2, Row4 is incorrect"),
                () -> assertEquals("xqueryCustomFolder", ((JLabel)gridPanel.getComponent(5)).getText(), "Col1, Row5 is incorrect"),
                () -> assertEquals("E:\\XQuery-Statements\\Egendefinerte", ((JLabel)gridPanel.getComponent(13)).getText(), "Col2, Row5 is incorrect"),
                () -> assertEquals("basexPath", ((JLabel)gridPanel.getComponent(6)).getText(), "Col1, Row6 is incorrect"),
                () -> assertEquals("C:\\Program Files (x86)\\BaseX\\bin", ((JLabel)gridPanel.getComponent(14)).getText(), "Col2, Row6 is incorrect"),
                () -> assertEquals("droidPath", ((JLabel)gridPanel.getComponent(7)).getText(), "Col1, Row7 is incorrect"),
                () -> assertEquals("C:\\prog\\Droid", ((JLabel)gridPanel.getComponent(15)).getText(), "Col2, Row7 is incorrect"),
                () -> assertEquals("arkadePath", ((JLabel)gridPanel.getComponent(8)).getText(), "Col1, Row8 is incorrect"),
                () -> assertEquals("C:\\prog\\Arkade5", ((JLabel)gridPanel.getComponent(16)).getText(), "Col2, Row8 is incorrect")
        );

        assertAll("Label buttons",
                () -> assertTrue(gridPanel.getComponent(17).isEnabled(), "Row1 button is not enabled"),
                () -> assertTrue(gridPanel.getComponent(18).isEnabled(), "Row2 button is not enabled"),
                () -> assertTrue(gridPanel.getComponent(19).isEnabled(), "Row3 button is not enabled"),
                () -> assertTrue(gridPanel.getComponent(20).isEnabled(), "Row4 button is not enabled"),
                () -> assertTrue(gridPanel.getComponent(21).isEnabled(), "Row5 button is not enabled"),
                () -> assertTrue(gridPanel.getComponent(22).isEnabled(), "Row6 button is not enabled"),
                () -> assertTrue(gridPanel.getComponent(23).isEnabled(), "Row7 button is not enabled"),
                () -> assertTrue(gridPanel.getComponent(24).isEnabled(), "Row8 button is not enabled")
        );

        assertAll("Setting buttons",
                () -> assertFalse(gridPanel.getComponent(25).isEnabled(), "Lagre innstillinger button should be deactivated"),
                () -> assertTrue(gridPanel.getComponent(26).isEnabled(), "Tilbake button should be activated"),
                () -> assertTrue(gridPanel.getComponent(27).isEnabled(), "Tilbakestill button should be activated")
        );
    }

    @Test
    void clearContainer() {
        settingsView.clearContainer();
        assertEquals(0, settingsView.container.getComponentCount(), "Container is not cleared");
    }
}