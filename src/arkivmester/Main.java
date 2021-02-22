package arkivmester;

/**
 * Serves as entrypoint for the application. Kick starts the controllers.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class Main {

    /**
     * Creates and initiates the controllers.
     * @param args No arguments are used.
     */
    public static void main(String[] args) {
        ArchiveController ac = new ArchiveController();
        ac.start();
    }
}
