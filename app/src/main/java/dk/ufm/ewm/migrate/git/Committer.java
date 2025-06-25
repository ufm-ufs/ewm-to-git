package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import dk.ufm.ewm.migrate.Settings;

public class Committer implements IFastImportGenerator {

    private String name;
    private String email;
    private ZonedDateTime timestamp;

    public Committer(String name, String email, ZonedDateTime timestamp) {
        this.name = name;
        this.email = email;
        this.timestamp = timestamp;
    }

    public Committer(String name, String email, Date date) {
        this.name = name;
        this.email = email;
        this.timestamp = ZonedDateTime.ofInstant(date.toInstant(), TimeZone.getDefault().toZoneId());
    }

    public Committer(String name, String email, Date date, TimeZone timeZone) {
        this.name = name;
        this.email = email;
        this.timestamp = ZonedDateTime.ofInstant(date.toInstant(), timeZone.toZoneId());
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        os.write(name.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        os.write(LT);
        os.write(email.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(GT);
        os.write(SP);
        os.write(String.valueOf(timestamp.toEpochSecond()).getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(SP);
        os.write(String.format("%+05d", timestamp.getOffset().getTotalSeconds() / 36).getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(LF);
        return os.toByteArray();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

}
