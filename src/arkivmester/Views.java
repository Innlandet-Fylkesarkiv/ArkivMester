package arkivmester;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
    public void actionPerformed(ActionEvent e) { //#NOSONAR
        String buttonName = e.getActionCommand();

        switch (buttonName) {
            case "Last inn pakket uttrekk":
                for (ViewObserver obs : observers)
                    obs.uploadArchive();
                break;
            case "Start testing":
                for (ViewObserver obs : observers) {
                    try {
                        obs.testStarted();
                    } catch (IOException ioException) {
                        System.out.println(ioException.getMessage()); //NOSONAR
                    }
                }
                break;
            case "Test nytt uttrekk":
                for (ViewObserver obs : observers)
                    obs.newTest();
                break;
            case "Rediger informasjon":
                for (ViewObserver obs : observers)
                    obs.editAdminInfo();
                break;
            case "Lagre administrativ data":
                for (ViewObserver obs : observers) {
                    obs.saveAdminInfo();
                }
                break;
            case "Avbryt":
                for (ViewObserver obs : observers)
                    obs.cancelButton();
                break;
            case "Velg tester":
                for (ViewObserver obs : observers)
                    obs.chooseTests();
                break;
            case "Lag rapport":
                for (ViewObserver obs : observers)
                    obs.makeReport();
                break;
            default:
                break;
        }
    }
}
