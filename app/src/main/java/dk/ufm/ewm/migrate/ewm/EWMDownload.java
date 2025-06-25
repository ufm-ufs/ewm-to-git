package dk.ufm.ewm.migrate.ewm;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.client.IWorkspaceManager;
import com.ibm.team.scm.common.IChange;

//Data class containing everything needed to download a change
public class EWMDownload {

    private ITeamRepository repository;
    private IWorkspaceManager workspaceManager;
    private IConfiguration configuration;
    private IChange change;
    private String oneLiner;

    public EWMDownload(ITeamRepository repository, IWorkspaceManager workspaceManager, IConfiguration configuration, IChange change, String oneLiner) {
        this.repository = repository;
        this.workspaceManager = workspaceManager;
        this.configuration = configuration;
        this.change = change;
        this.oneLiner = oneLiner;
    }

    public ITeamRepository getRepository() {
        return repository;
    }

    public IWorkspaceManager getWorkspaceManager() {
        return workspaceManager;
    }
    
    public IConfiguration getConfiguration() {
        return configuration;
    }

    public IChange getChange() {
        return change;
    }
    
    public String getOneLiner() {
        return oneLiner;
    }

}
