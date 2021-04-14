package arkivmester;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Contains multiple methods to run different third party tools to test the archive.
 *
 * @since 1.0
 * @version 1.0
 * @author Magnus Sustad, Oskar Leander Melle Keogh, Esben Lomholt Bjarnason and Tobias Ellefsen
 */
public class ThirdPartiesModel {
    String cmd = "cmd.exe";
    String cdString = "cd \"";
    private List<Boolean> selectedTests = new ArrayList<>();
    private List<Boolean> selectedXqueries = new ArrayList<>();
    private String [] xqueryNames;
    Boolean runXqueries = false;
    int amountOfTests = 4;
    String tempFolder;
    String archiveName;
    String basexPathKey = "basexPath";
    String tempFolderKey = "tempFolder";

    /**
     * Initializes the selectedTests list to true.
     */
    ThirdPartiesModel() {
        for(int i = 0; i < amountOfTests; i++) {
            selectedTests.add(true);
        }

    }

    /**
     * Updates selectedTests with updated data.
     * @param selectedXqueries Updated selectedTests from the UI.
     */
    public void checkIfXquery(List<Boolean> selectedXqueries) {
        int count = 0;
        for(Boolean value : selectedXqueries) {
            if(Boolean.TRUE.equals(value))
                runXqueries = true;
            else
                count++;
        }
        if(count==selectedXqueries.size())
            runXqueries = false;
    }

    /**
     * Updates selectedTests with updated data.
     * @param selectedList Updated selectedTests from the UI.
     */
    public void updateTests(List<Boolean> selectedList, List<Boolean> selectedXqueries) {
        this.selectedTests = selectedList;
        this.selectedXqueries = selectedXqueries;
    }

    /**
     * Regular getter for selectedTests list.
     * @return selectedTests boolean list.
     */
    public List<Boolean> getSelectedTests() {
        return this.selectedTests;
    }

    /**
     * Regular getter for selectedXqueries list.
     * @return selectedXqueries boolean list.
     */
    public List<Boolean> getSelectedXqueries() {
        return this.selectedXqueries;
    }

    /**
     * Resets the selectedTests to true. Used when the program resets.
     */
    public void resetSelectedTests() {
        for (int i = 0; i<amountOfTests; i++) {
            selectedTests.set(i, true);
        }
    }

    /**
     * Initializes the tempFolder variable with the current tempFolder path.
     */
    public void initializePath(Properties prop) {
        tempFolder = prop.getProperty(tempFolderKey);
        archiveName = "\\" + prop.getProperty("currentArchive"); // #NOSONAR
    }

    /**
     * Runs ArkadeCLI through cmd, the output gets printed to the console.
     * Report from the tests gets put in the output folder.
     *
     * @param path A file that contains the archive that is to be tested.
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runArkadeTest(File path, Properties prop) throws IOException {

        //String with path to arkadeCli
        String cd = cdString + prop.getProperty("arkadePath") + "\""; // #NOSONAR
        //Path to output folder where test report gets saved.
        String outputPath = "\"" + tempFolder + archiveName + "\\Arkade\\Report\"";
        //Path to temp folder where temporary data about the tests gets stored.
        String tempPath = "\"" + tempFolder + archiveName + "\\Arkade\"";


        //Run ArkadeCli through command line.
        runCMD(cd + " && arkade test -a " + path + " -o " + outputPath + " -p " + tempPath + " -t noark5");

    }

    /**
     * Runs Kost-Val through command line, the output gets printed to the console.
     * Report from the test gets moved from the user folder to an output folder.
     *
     * @param path A string that contains the path to the archive that is to be tested
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runKostVal(String path, Properties prop) throws IOException {

        //String with path to KostVal
        String cd = cdString + prop.getProperty("kostvalPath") + "\"";
        //Path to folder where test report gets moved to.
        String reportPath = "\"" + tempFolder + archiveName + "\\KostVal\"";
        //Run kost-val from command line
        runCMD(cd + " &&  java -jar cmd_KOST-Val.jar --sip " + path + " --en");
        //Move testreport to an output folder.
        runCMD("move %userprofile%\\.kost-val_2x\\logs\\*.* " + reportPath);

    }

    /**
     * Runs VeraPDF through command line. The report gets put in an output folder.
     *
     * @param path A string that contains the path to the archive that is to be tested
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runVeraPDF(String path, Properties prop) throws IOException {

        //String with path to VeraPDF
        String cd = cdString + prop.getProperty("veraPDFPath") + "\"";
        //Path to folder where test report gets moved to.

        String reportPath = "\"" + tempFolder + archiveName + "\\VeraPDF" + "\\verapdf.xml\"";

        //Run verapdf through command line.
        runCMD(cd + " && verapdf --recurse " + path + " > " + reportPath);

        System.out.println("VeraPDF done, report at: " + reportPath); // NOSONAR
    }

    /**
     * Runs DROID through command line. Creates four different files and places them in a folder.
     *
     * @param path A string that contains the path to the archive to be tested.
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void runDROID(String path, Properties prop) throws IOException {

        //String with path to droid location.
        String cd = cdString + prop.getProperty("droidPath") + "\"";
        //String with command to run .jar file
        String jar = " && java -jar droid-command-line-6.5.jar";

        //Path to droid profile needed to run droid.
        String profilePath = "\"" + tempFolder + archiveName + "\\DROID" + "\\profile.droid\"";
        //Path to folder where test output ends up.
        String outputPath = "\"" + tempFolder + archiveName + "\\DROID\"";

        //Run first DROID function - making the droid profile.
        System.out.println("\nDroid 1"); //NOSONAR
        runCMD(cd + jar + " -R -a " + path + " -p " + profilePath);

        //Run second DROID function - making a spreadsheet of all files in archive.
        System.out.println("\nDroid 2"); //NOSONAR
        runCMD(cd + jar + " -p " + profilePath + " -e " + outputPath + "\\filliste.csv");

        //Run third DROID function - making a xml test report.
        System.out.println("\nDroid 3"); //NOSONAR
        runCMD(cd + jar + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                "-t \"DROID Report XML\" -r " + outputPath + "\\droid.xml");

        //Run fourth DROID function - making a pdf test report.
        System.out.println("\nDroid 4"); //NOSONAR
        runCMD(cd + jar + " -p " + profilePath + " -n \"Comprehensive breakdown\" " +
                "-t \"PDF\" -r " + outputPath + "\\droid.pdf");
    }

    /**
     * Runs 7Zip through command line and unzips the archive.
     *
     * @param path A file that contains the archive to be unzipped
     * @param prop Properties object containing the config.
     * @throws IOException Cannot run program.
     */
    public void unzipArchive(File path, Properties prop) throws IOException {
        File unzipped = new File(tempFolder + archiveName + archiveName); // #NOSONAR
        if(unzipped.exists()){
            Path directory = unzipped.toPath();
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        //String with path to 7zip location.
        String cd = cdString + prop.getProperty("7ZipPath") + "\"";
        //Run VeraPDF from command line
        runCMD(cd + " && 7z x " + path + " -o\"" + tempFolder + archiveName + "\" -r");
    }

    public void runXquery(Properties prop) throws IOException { // #NOSONAR
        for(int i = 0; i<selectedXqueries.size(); i++) {
            if(Boolean.TRUE.equals(selectedXqueries.get(i))) {
                runCustomBaseX(xqueryNames[i], prop);
            }
        }
    }

    public void setUpBaseXDatabase(Properties prop) throws IOException {
        String pwd = cdString + prop.getProperty(basexPathKey) + "\"";

        StringBuilder bld = new StringBuilder();
        String archivePath = tempFolder + archiveName;
        String unzippedArchive = archivePath + "\\" + prop.get("currentArchive"); // #NOSONAR
        String dbName = "arkivmester";

        String xml1 = bld.append(archivePath).append("\\DROID\\droid.xml").toString();
        bld.setLength(0);
        String xml2 = bld.append(archivePath).append("\\VeraPDF\\verapdf.xml").toString();
        bld.setLength(0);
        String xml3 = bld.append(archivePath).append("\\KostVal\\dokumenter.kost-val.log.xml").toString();
        bld.setLength(0);

        String xml5 = bld.append(unzippedArchive).append("\\dias-mets.xml").toString();
        bld.setLength(0);
        String xml6 = bld.append(unzippedArchive).append("\\log.xml").toString();
        bld.setLength(0);
        String xml7 = bld.append(unzippedArchive).append("\\administrative_metadata\\dias-premis.xml").toString();
        bld.setLength(0);
        String xml8 = bld.append(unzippedArchive).append("\\administrative_metadata\\addml.xml").toString();
        bld.setLength(0);
        String xml9 = bld.append(unzippedArchive).append("\\descriptive_metadata\\eac-cpf.xml").toString();
        bld.setLength(0);
        String xml10 = bld.append(unzippedArchive).append("\\descriptive_metadata\\ead.xml").toString();
        bld.setLength(0);
        String xml11 = bld.append(unzippedArchive).append("\\content\\arkivstruktur.xml").toString();
        bld.setLength(0);
        String xml12 = bld.append(unzippedArchive).append("\\content\\endringslogg.xml").toString();
        bld.setLength(0);
        String xml13 = bld.append(unzippedArchive).append("\\content\\loependeJournal.xml").toString();
        bld.setLength(0);
        String xml14 = bld.append(unzippedArchive).append("\\content\\offentligJournal.xml").toString();
        bld.setLength(0);
        String xml15 = bld.append(unzippedArchive).append("\\content\\arkivuttrekk.xml").toString();
        bld.setLength(0);

        runCMD(pwd + " && basex.bat -c \"CREATE DB " + dbName + "\"");

        String cmdOpen = " && basex.bat -c \"OPEN ";
        String cmdAdd = "; ADD \"";

        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml1 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml2 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml3 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml5 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml6 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml7 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml8 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml9 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml10 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml11 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml12 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml13 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml14 + "\"\"");
        runCMD(pwd + cmdOpen + dbName + cmdAdd + xml15 + "\"\"");
    }

    public void deleteBaseXDB(Properties prop) throws IOException {
        String pwd = cdString + prop.getProperty(basexPathKey) + "\"";

        runCMD(pwd + " && basex.bat -c \"DROP DB arkivmester\"");
    }

    /**
     * Queries an .xml file via an .xq XQuery/XPath file.
     * @param xml Path to .xml file.
     * @param xqName Config key for .xq file.
     * @param prop Properties object containing the config.
     * @return String list of the results from the query.
     */
    public List<String> runBaseX(String xml, String xqName, Properties prop) throws IOException {
        String xq = "\"" + prop.getProperty("xqueryExtFolder") + "\\" + xqName + "\"";
        String temp = prop.getProperty(tempFolderKey) + "\\xqueryResult.txt";

        String pwd = cdString + prop.getProperty(basexPathKey) + "\"";
        List<String> result = new ArrayList<>();

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -o \"" + temp + "\" -i \"" + xml + "\" " + xq); // #NOSONAR

        try {
            Process p = baseXBuilder.start();
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        File xqueryResult = new File(temp);
        try (InputStream bytes = new FileInputStream(xqueryResult)) {

            Reader chars = new InputStreamReader(bytes, StandardCharsets.UTF_8);

            try (BufferedReader r = new BufferedReader(chars)) {
                String line = r.readLine();
                while (line != null) {
                    result.add(line);
                    line = r.readLine();
                }
            }
        }

        return result;
    }


    public void runCustomBaseX(String xqName, Properties prop) throws IOException {
        //XQuery
        String xq = prop.getProperty("xqueryCustomFolder") + "\\" + xqName + "\"";

        //Result name
        String outFileName = xqName;
        outFileName = outFileName.substring(0,outFileName.lastIndexOf('.'));

        String outFile = prop.getProperty(tempFolderKey) + archiveName + "\\" + outFileName + ".txt";
        String pwd = cdString + prop.getProperty(basexPathKey) + "\"";

        ProcessBuilder baseXBuilder = new ProcessBuilder(cmd, "/c", pwd + " && basex -o \"" + outFile + "\" \"" + xq);

        try {
            Process p = baseXBuilder.start();
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Creates a process and runs a cmd command.
     *
     * @param command The cmd command that is to be ran.
     * @throws IOException Cannot run program.
     */
    private void runCMD(String command) throws IOException {
        //Creates a process to run a command in cmd.
        ProcessBuilder cmdBuilder = new ProcessBuilder(
                cmd, "/c", command);
        Process p = cmdBuilder.start();
        //Gets output from cmd and prints it.
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = r.readLine();
        while(line != null) {
            System.out.println(line); //NOSONAR
            line = r.readLine();

        }
        r.close();
    }

    public void packToAIP(Properties prop, String metadataPath) throws IOException {

        String cd = cdString + prop.getProperty("arkadePath") + "\"";

        String outputPath = tempFolder + archiveName;
        String path = outputPath + archiveName;


        String processPath = "\"" + tempFolder + archiveName + "\\Arkade\"";


        //Run ArkadeCli through command line.
        runCMD(cd + " && arkade pack -a \"" + path + "\" -i AIP" + " -m \"" + metadataPath + "\" -o \"" + outputPath + "\" -p " + processPath + " -t noark5 -f");
    }

    /**
     * Checks if the third party tools are present where the config defines.
     * @param prop Properties object containing the config.
     * @return True if all tools are present, false if there is one or more tools missing.
     */
    public List<String> checkIfToolsArePresent(Properties prop) {
        File file;
        List<String> missingTools = new ArrayList<>();

        file = new File(prop.getProperty(basexPathKey) + "\\basex.bat");
        if(!file.exists())
            missingTools.add("basex.bat");

        file = new File(prop.getProperty("7ZipPath") + "\\7zG.exe");
        if(!file.exists())
            missingTools.add("7Zip");

        for(int i = 0; i<selectedTests.size(); i++) {
            if(Boolean.TRUE.equals(selectedTests.get(i))) {
                switch (i) {
                    case 0:
                        file = new File(prop.getProperty("arkadePath") + "\\Bundled\\Siegfried\\siegfried.exe");
                        break;
                    case 1:
                        file = new File(prop.getProperty("droidPath") + "\\droid-command-line-6.5.jar");
                        break;
                    case 2:
                        file = new File(prop.getProperty("kostvalPath") + "\\cmd_KOST-Val.jar");
                        break;
                    case 3:
                        file = new File(prop.getProperty("veraPDFPath") + "\\verapdf.bat");
                        break;
                    default:
                }
                if(!file.isFile())
                    missingTools.add(file.getName());
            }
        }

        return missingTools;
    }

    /**
     * Gets the names of the custom XQueries in the custom folder. Also initiates "selectedXqueries" with false for all
     * files.
     * @param prop Config property object to get the custom folder's path.
     * @return String array with the name of the custom XQuery files.
     */
    public String[] getCustomXqueries(Properties prop) {
        File f = new File((String)prop.get("xqueryCustomFolder"));
        String[] list = f.list();

        if(selectedXqueries.isEmpty()) {
            if(list != null) {
                for (int i = 0; i<list.length; i++) {
                    selectedXqueries.add(false);
                }
                xqueryNames = list;
                return list;
            }
            return new String[]{""};
        }

        if(list != null) {
            return list;
        }
        return new String[]{""};
    }
}
