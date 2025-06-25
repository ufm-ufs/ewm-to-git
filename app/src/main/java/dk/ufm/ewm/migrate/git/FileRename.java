package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import dk.ufm.ewm.migrate.Settings;

/*
 * This is used for both file rename and file copy
 */
public class FileRename implements IFileChange {

    private static final String FILE_RENAME = "R";
    private static final String FILE_COPY = "C";
    private String fileTypeChar = FILE_RENAME;

    private Path path;
    private Path oldPath;

    public FileRename(Path path, Path oldPath, boolean isCopy) {
        this.path = path;
        this.oldPath = oldPath;
        if (isCopy) {
            fileTypeChar = FILE_COPY;
        }
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(fileTypeChar.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        os.write(DQ);
        os.write(oldPath.toString().replace('\\', '/').getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(DQ);
        os.write(SP);
        os.write(DQ);
        os.write(path.toString().replace('\\', '/').getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(DQ);
        os.write(LF);
        return os.toByteArray();
    }

    @Override
    public Path getPath() {
        return path;
    }

}
