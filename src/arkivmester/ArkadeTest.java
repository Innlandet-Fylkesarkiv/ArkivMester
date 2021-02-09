package arkivmester;

import java.io.*;

public class ArkadeTest {

        public static void main(String[] args) throws Exception {
            String archivePath = "c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar";
            String outputPath = "c:\\arkade\\output";
            String tempPath = "c:\\arkade\\";

            ProcessBuilder arkadeBuilder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd \"C:\\arkadecli\" && arkade test -a " + archivePath +
                    " -o " + outputPath +  " -p " + tempPath + " -t noark5");
            arkadeBuilder.redirectErrorStream(true);
            Process p = arkadeBuilder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
                System.out.println(line); //NOSONAR
            }
    }
}

//arkade test -a c:\archive\899ec389-1dc0-41d0-b6ca-15f27642511b.tar -o c:\atest\output -p c:\atest\tmp -t noark5

//    ProcessBuilder builder = new ProcessBuilder(
//            "cmd.exe", "/c", "cd \"C:\\arkadecli\" && arkade test -a c:\\archive\\899ec389-1dc0-41d0-b6ca-15f27642511b.tar " +
//            "-o c:\\arkade\\output -p c:\\arkade\\tmp -t noark5");
//            builder.redirectErrorStream(true);
//                    Process p = builder.start();
//                    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                    String line;
//                    while (true) {
//                    line = r.readLine();
//                    if (line == null) { break; }
//                    System.out.println(line); //NOSONAR
//                    }