package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import dk.ufm.ewm.migrate.Settings;

public class FileDelete implements IFileChange {

    private static final String FILE_DELETE = "D";
    private Path path;

    public FileDelete(Path path) {
        this.path = path;
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(FILE_DELETE.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        os.write(path.toString().replace('\\', '/').getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(LF);
        return os.toByteArray();
    }

    @Override
    public Path getPath() {
        return path;
    }

}
