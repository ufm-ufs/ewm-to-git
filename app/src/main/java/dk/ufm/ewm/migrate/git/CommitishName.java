package dk.ufm.ewm.migrate.git;

import dk.ufm.ewm.migrate.Settings;

public class CommitishName extends Commitish {
    
    public CommitishName(String commitish) {
        this.commitish = commitish.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    }
    
    public CommitishName(byte[] commitish) {
        this.commitish = commitish;
    }
}
