package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class Commitish implements IFastImportGenerator {
    
    protected byte[] commitish;
    
    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(commitish);
        return os.toByteArray();
    }
}
