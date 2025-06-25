package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import dk.ufm.ewm.migrate.Settings;

public class Mark extends Commitish implements IFastImportGenerator {

    private static AtomicLong mark = new AtomicLong(1);
    
    private long m;

    public Mark() {
        this.m = getNextMark();
    }

    private static long getNextMark() {
        return mark.getAndIncrement();
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(COLON);
        os.write(String.valueOf(m).getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        return os.toByteArray();
    }
}
