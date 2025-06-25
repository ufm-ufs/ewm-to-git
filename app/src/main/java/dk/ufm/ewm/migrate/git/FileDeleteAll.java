package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import dk.ufm.ewm.migrate.Settings;

public class FileDeleteAll implements IFileChange {

    private static final String DELETEALL = "deleteall";

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(DELETEALL.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(LF);
        return os.toByteArray();
    }

    @Override
    public Path getPath() {
        return Paths.get("");
    }

}
