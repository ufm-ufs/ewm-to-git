package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import dk.ufm.ewm.migrate.Settings;

public class Data implements IFastImportGenerator {

    private static final String DATA = "data";
    private byte[] data;
    
    public Data(byte[] data) {
        this.data = data;
    }
    
    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(DATA.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        os.write(String.valueOf(data.length).getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(LF);
        os.write(data);
        os.write(LF);
        return os.toByteArray();
    }

    public byte[] getData() {
        return data;
    }
}
