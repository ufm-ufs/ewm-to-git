package dk.ufm.ewm.migrate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.TimeZone;

public class Settings {
    
private static final String PROPERTIES_FILE = "migration.properties";
    
    public static String RTC_USER = "";
    public static String RTC_PASSWORD = "";
    public static String RTC_URL = "";
    public static String STREAM_NAME = "";

    public static String GIT_EXEC_PATH = "";
    public static String GIT_REPO_ROOT = "";

    public static String LOG_DIR = "";
    
    public static Charset GIT_METADATA_OUTPUT_CHARSET = Charset.forName("UTF8");
    
    public static String TIMEZONE = "";
    
    public static String BRANCH_NAME = "refs/heads/main";
    
    static {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try(InputStream resourceStream = loader.getResourceAsStream(PROPERTIES_FILE)) {
            properties.load(resourceStream);
            RTC_USER = properties.getProperty("RTC_USER");
            RTC_PASSWORD = properties.getProperty("RTC_PASSWORD");
            RTC_URL = properties.getProperty("RTC_URL");
            STREAM_NAME = properties.getProperty("STREAM_NAME");
            GIT_EXEC_PATH = properties.getProperty("GIT_EXEC_PATH");
            GIT_REPO_ROOT = properties.getProperty("GIT_REPO_ROOT");
            LOG_DIR = properties.getProperty("LOG_DIR");
            TIMEZONE = properties.getProperty("TIMEZONE");
            BRANCH_NAME = properties.getProperty("BRANCH_NAME", BRANCH_NAME);
            
            TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Settings() {}
}
