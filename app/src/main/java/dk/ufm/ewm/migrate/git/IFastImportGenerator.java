package dk.ufm.ewm.migrate.git;

import java.io.IOException;

import dk.ufm.ewm.migrate.Settings;

public interface IFastImportGenerator {
    
    public static final byte[] LF = "\n".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    public static final byte[] SP = " ".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    public static final byte[] LT = "<".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    public static final byte[] GT = ">".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    public static final byte[] COLON = ":".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    public static final byte[] DQ = "\"".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    
    public byte[] writeImportData() throws IOException;
}
