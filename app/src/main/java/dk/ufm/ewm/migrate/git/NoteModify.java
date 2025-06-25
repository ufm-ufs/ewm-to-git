package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import dk.ufm.ewm.migrate.Settings;

public class NoteModify implements IFileChange {

    private static final String FILE_TYPE_CHAR = "N";

    private long mark;
    private Data inlineNote;
    private Commitish noteDestination;
    
    public NoteModify(Commitish noteDestination, long mark) {
        this.noteDestination = noteDestination;
        this.mark = mark;
    }

    public NoteModify(Commitish noteDestination, String inlineNote) {
        this.noteDestination = noteDestination;
        this.inlineNote = new Data(inlineNote.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(FILE_TYPE_CHAR.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        if (inlineNote == null) {
            os.write((":" + mark).getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        }
        else {
            os.write("inline".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        }
        os.write(SP);
        os.write(noteDestination.writeImportData());
        os.write(LF);
        os.write(inlineNote.writeImportData());
        return os.toByteArray();
    }

    @Override
    public Path getPath() {
        return Paths.get("");
    }
}
