package arkivmester;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Holds all data about the program's settings.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class TestSettingsModel {

    private final Properties prop = new Properties();

    /**
     * Constructor - Reads from config.properties file.
     */
    TestSettingsModel() {
        String propName = "/config.properties";
        InputStream inputStream = getClass().getResourceAsStream(propName);

        try {
            prop.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }
    }

    /**
     * Regular getter for config.
     * @return Properties object containing the config keys/values.
     */
    public Properties getConfig() {
        return prop;
    }
}
