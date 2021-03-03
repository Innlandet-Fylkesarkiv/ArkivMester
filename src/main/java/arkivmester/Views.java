package arkivmester;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Super class for all views.
 *
 * Implemented by {@link MainView}, {@link AdminInfoView}, {@link TestSettingsView}, {@link TestView}.
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class Views implements ActionListener {
    /**
     * Width for the application view in pixels.
     */
    protected int windowWidth = 1200;

    /**
     * Height for the application view in pixels.
     */
    protected int windowHeight = 700;

    /**
     * Primary color for the application.
     */
    protected Color primaryColor = new Color(8, 83, 148);

    /**
     * Primary font and font size for the application.
     */
    protected Font primaryFont = new Font("Sans Serif", Font.PLAIN, 20);

    /**
     * List over all observers who have subscribed to this view.
     */
    protected final List<ViewObserver> observers = new ArrayList<>();

    /**
     * Adds and subscribes controllers to the observers list.
     * @param observer Controller who subscribes and will be added to the observer list.
     */
    public void addObserver(ViewObserver observer) {
        observers.add(observer);
    }

    /**
     * Listens to all button clicks in this view and notifies all subscribed observers.
     * @param e Button action event from GUI
     */
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
            case "Lagre tests":
                for (ViewObserver obs : observers)
                    obs.saveTestSettings();
                break;
            case "Innstillinger":
                for (ViewObserver obs : observers)
                    obs.openSettings();
                break;
            case "Lagre innstillinger":
                for (ViewObserver obs : observers)
                    obs.saveSettings();
                break;
            default:
                break;
        }
    }
}
