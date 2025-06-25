package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;

import dk.ufm.ewm.migrate.Settings;

/*
 * Used for both added files or modified files
 */
public class FileModify implements IFileChange {

    private static final String FILE_TYPE_CHAR = "M";
    private FileMode fileMode;
    private Path path;
    private Mark mark;

    public FileModify(Mark mark, FileMode fileMode, Path path) {
        this.mark = mark;
        this.fileMode = fileMode;
        this.path = path;
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(FILE_TYPE_CHAR.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        os.write(fileMode.getOctalValue().getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        os.write(mark.writeImportData());
        os.write(SP);
        os.write(path.toString().replace('\\', '/').getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(LF);
        return os.toByteArray();
    }

    @Override
    public String toString() {
        try {
            return new String(writeImportData(), "UTF8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "Error generating string";
    }

    @Override
    public Path getPath() {
        return path;
    }
}
