package dk.ufm.ewm.migrate;

import java.time.Duration;

public class Util {

    private Util() {}

    public static String getReadableTime(Long nanos) {
        Duration duration = Duration.ofNanos(nanos);
        return duration.toString();
    }

}
