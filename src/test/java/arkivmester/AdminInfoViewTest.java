package arkivmester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminInfoViewTest extends Views {
    private AdminInfoView adminInfoView;
    private final Color primaryColor = new Color(8, 83, 148);
    private Container ctn;

    @BeforeEach
    void setUp() {
        adminInfoView = new AdminInfoView();
        MainView mainView = new MainView();

        mainView.createFrame();
        mainView.createAndShowGUI();

        ctn = mainView.getContainer();
        adminInfoView.createAndShowGUI(ctn);
    }

    @Test
    @DisplayName("Ensure container is not empty")
    void checkContainer() {
        assertNotNull(ctn, "Container is empty");
    }

    @Test
    @DisplayName("Ensure GUI is created and shown")
    void validateGUI() {
        assertEquals(2, ctn.getComponentCount(), "Should only be 2 panels");
        assertEquals(new Point(0, 0), ctn.getComponent(1).getLocation(), "Top bar is not at the top");
        assertEquals(primaryColor, ctn.getComponent(0).getBackground(), "Top bar has wrong background color");

        Container infoContainer = (Container)ctn.getComponent(1);
        assertEquals(1, infoContainer.getComponentCount(), "Info container should have 1 gridpanel");

        Container gridPanel = (Container)infoContainer.getComponent(0);
        assertEquals(19, gridPanel.getComponentCount(), "GridPanel should have 19 total components");


        List<Component> elements = Arrays.asList(gridPanel.getComponents());
        assertAll("Buttons",
                () -> assertTrue(elements.get(17).isEnabled(), "Lagre admininfo button should be activated"),
                () -> assertTrue(elements.get(18).isEnabled(), "Avbryt button should be activated")
        );
    }

    @Test
    void clearContainer() {
        adminInfoView.clearContainer();
        assertEquals(0, adminInfoView.container.getComponentCount(), "Container is not cleared");
    }
}