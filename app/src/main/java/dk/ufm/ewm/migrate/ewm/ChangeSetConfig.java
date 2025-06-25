package dk.ufm.ewm.migrate.ewm;

import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.common.IChangeSet;

//A combination of a changeset and its configuration
public class ChangeSetConfig {

    private IChangeSet changeSet;
    private IConfiguration configuration;

    public ChangeSetConfig(IChangeSet changeSet, IConfiguration configuration) {
        this.changeSet = changeSet;
        this.configuration = configuration;
    }

    public IChangeSet getChangeSet() {
        return changeSet;
    }

    public IConfiguration getConfiguration() {
        return configuration;
    }

}
