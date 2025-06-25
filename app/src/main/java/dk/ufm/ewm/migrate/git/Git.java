package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import dk.ufm.ewm.migrate.Settings;

//Git commands
public class Git {

    private Git() {}

    public static void init(Path path) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(Settings.GIT_EXEC_PATH, "init", path.toString());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        process.waitFor();
    }

    public static void config(Path path, String key, String value) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(Settings.GIT_EXEC_PATH, "-C", path.toString(), "config", key, value);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        System.out.println("Executing: " + String.join(" ", processBuilder.command().toArray(new String[0])));
        process.waitFor();
    }

    public static Process fastImport(Path path) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(Settings.GIT_EXEC_PATH, "-C", path.toString(), "fast-import");
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
           result.write(buffer, 0, length);
        }
        return result.toString("UTF8");
    }
}
