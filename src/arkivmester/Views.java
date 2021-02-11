package arkivmester;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//Super class for views
public class Views implements ActionListener {
    protected int windowWidth = 1200;
    protected int windowHeight = 700;
    protected Color primaryColor = new Color(8, 83, 148);
    protected Font primaryFont = new Font("Sans Serif", Font.PLAIN, 20);
    protected final List<ViewObserver> observers = new ArrayList<>();

    //Subscribes observers to the observer list.
    public void addObserver(ViewObserver observer) {
        observers.add(observer);
    }

    //Shared action listener for all buttons
    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonName = e.getActionCommand();

        switch (buttonName) {
            case "Last inn pakket uttrekk":
                System.out.println("b1"); //#NOSONAR
                break;
            case "Start testing":
                for (ViewObserver obs : observers)
                    obs.testStarted();
                break;
            case "Test nytt uttrekk":
                for (ViewObserver obs : observers)
                    obs.newTest();
                break;
            case "Rediger informasjon":
                for (ViewObserver obs : observers)
                    obs.editAdminInfo();
                break;
            case "Avbryt":
                for (ViewObserver obs : observers)
                    obs.cancelButton();
                break;
            case "Velg tester":
                for (ViewObserver obs : observers)
                    obs.chooseTests();
                break;
            default:
                break;
        }
    }
}
