package arkivmester;

import javax.swing.*;

public class UserView extends JFrame { //#NOSONAR
    JFrame f;

    UserView() {

    }

    public void setUpGUI() {
        f = new JFrame("ArkivMester");
        f.setSize(1200, 700);
        f.setVisible(true);
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
