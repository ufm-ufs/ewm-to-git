package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import dk.ufm.ewm.migrate.Settings;

public class Blob implements IFastImportGenerator {

    private static final byte[] BLOB_KEYWORD = "blob".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    private static final byte[] MARK_KEYWORD = "mark".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);

    private Data data;
    private Mark mark;

    public Blob(Data data) {
        this.mark =  new Mark();
        this.data = data;
    }
    
    public Mark getMark() {
        return mark;
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(BLOB_KEYWORD);
        os.write(LF);
        os.write(MARK_KEYWORD);
        os.write(SP);
        os.write(mark.writeImportData());
        os.write(LF);
        os.write(data.writeImportData());
        return os.toByteArray();
    }

}
