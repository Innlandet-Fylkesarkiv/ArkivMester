package arkivmester;

import java.io.*;
import java.util.Properties;

public class SettingsModel {
    File userFolder;
    File alteredCfg;
    Properties prop = new Properties();

    public Boolean setUpSettings() {
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

    //updateConfig()
    //getProperties()
}
