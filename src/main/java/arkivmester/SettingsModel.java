package arkivmester;

import java.io.*;
import java.util.Properties;

public class SettingsModel {
    private File userFolder;
    private File alteredCfg;
    private File tempFolder;
    private Properties prop = new Properties();

    public Boolean setUpSettings() { // #NOSONAR (Creating tempFolder exceeds the Cognitive Complexity with 2)
        userFolder = new File(System.getProperty("user.home") + "\\arkivmester");
        alteredCfg = new File(userFolder.getPath() + "\\config.properties");

        try {
            if(userFolder.exists() && userFolder.isDirectory()) {

                if(alteredCfg.exists() && !alteredCfg.isDirectory())
                    loadAlteredConfig();
                else {
                    if(Boolean.FALSE.equals(createConfig()))
                        return false;
                }

                if(Boolean.FALSE.equals(createTempFolder()))
                    return false;
            }
            else {
                if(!userFolder.mkdir())
                    return false;

                if(Boolean.FALSE.equals(createConfig()))
                    return false;
            }

        } catch (SecurityException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }

        return true;
    }

    private Boolean createTempFolder() {
        tempFolder = new File(userFolder.getPath() + "\\temp");

        if(tempFolder.exists() && tempFolder.isDirectory()) {
            return true;
        }
        return tempFolder.mkdir();
    }

    private Boolean createConfig() {
        loadDefaultConfig();

        try {
            if(!alteredCfg.createNewFile())
                return false;

            try (FileOutputStream fos = new FileOutputStream(alteredCfg)) {
                prop.store(fos, null);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }

        return true;
    }

    private void loadDefaultConfig() {
        try (InputStream is = getClass().getResourceAsStream("/config.properties")){
            prop.load(is);
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }
    }

    private void loadAlteredConfig() {
        try (FileInputStream fis = new FileInputStream(alteredCfg)) {
            prop.load(fis);
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }
    }

    public Properties getProp() {
        return prop;
    }

    public void updateConfig(String key, String value) {
        prop.setProperty(key, value);

        try (FileOutputStream fos = new FileOutputStream(alteredCfg)){
            prop.store(fos, null);
        } catch (IOException e) {
            System.out.println(e.getMessage()); // #NOSONAR
        }
    }
}
