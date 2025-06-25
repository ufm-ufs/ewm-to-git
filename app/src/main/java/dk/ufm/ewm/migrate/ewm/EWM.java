package dk.ufm.ewm.migrate.ewm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.team.filesystem.client.FileSystemCore;
import com.ibm.team.filesystem.client.IFileContentManager;
import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.links.common.ILink;
import com.ibm.team.links.common.ILinkHandle;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IItemHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.client.IChangeHistory;
import com.ibm.team.scm.client.IChangeHistoryDescriptor;
import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.client.IWorkspaceConnection;
import com.ibm.team.scm.client.IWorkspaceManager;
import com.ibm.team.scm.client.SCMPlatform;
import com.ibm.team.scm.common.IChangeHistoryEntryChange;
import com.ibm.team.scm.common.IChangeSet;
import com.ibm.team.scm.common.IChangeSetHandle;
import com.ibm.team.scm.common.IComponent;
import com.ibm.team.scm.common.IFolderHandle;
import com.ibm.team.scm.common.IVersionableHandle;
import com.ibm.team.scm.common.IWorkspaceHandle;
import com.ibm.team.scm.common.dto.IAncestorReport;
import com.ibm.team.scm.common.dto.IChangeSetLinkSummary;
import com.ibm.team.scm.common.dto.INameItemPair;
import com.ibm.team.scm.common.dto.IWorkspaceSearchCriteria;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

import dk.ufm.ewm.migrate.Settings;
import dk.ufm.ewm.migrate.Util;

//Class for communicating with the EWM server
public class EWM {

    private static Map<String, IAttribute> cachedAttributes = new HashMap<String, IAttribute>();
    private static boolean isCachedAttributes = false;

    private static Map<String, IContributor> cachedContributors = new HashMap<String, IContributor>();

    static {
        long startTime = System.nanoTime();
        TeamPlatform.startup();
        System.out.println("TeamPlatform startup - Took " + Util.getReadableTime(System.nanoTime() - startTime));
    }
    
    private EWM () {}
    
    public static ITeamRepository login() throws TeamRepositoryException {
        long startTime = System.nanoTime();
        ITeamRepository repository = TeamPlatform.getTeamRepositoryService().getTeamRepository(Settings.RTC_URL);
        repository.registerLoginHandler(new LoginHandler());
        repository.login(null);
        System.out.println("Log in - Took " + Util.getReadableTime(System.nanoTime() - startTime));

        return repository;
    }
    
    public static String getCommitMessage(ITeamRepository repository, IWorkspaceManager workspaceManager, IChangeSet changeSet) throws TeamRepositoryException {
        String comment = "";
        List<IChangeSetLinkSummary> linkSummaries = workspaceManager.getChangeSetLinkSummary(Arrays.asList(changeSet), null);
        for (IChangeSetLinkSummary linkSummary: linkSummaries) {
            for (ILinkHandle linkHandle: linkSummary.getLinks()) {
                ILink link = (ILink) repository.itemManager().fetchCompleteItem(linkHandle, 1, null);
                IItemHandle itemHandle = (IItemHandle) link.getTargetRef().resolve();
                    
                IWorkItem workItem = (IWorkItem) repository.itemManager().fetchCompleteItem(itemHandle, 1, null);
                
                if (!isCachedAttributes) {
                    IWorkItemClient workItemClient = (IWorkItemClient) repository.getClientLibrary(IWorkItemClient.class);
                    List<IAttribute> attributes = workItemClient.findAttributes(workItem.getProjectArea(), null);
                    for (IAttribute a: attributes) {
                        cachedAttributes.put(a.getDisplayName(), a);
                    }
                }
                
                comment += workItem.getId() + ": ";
                comment += (String) workItem.getValue(cachedAttributes.get("Summary"));
                
            }
        }
        if (!changeSet.getComment().isBlank()) {
            comment += "\n\n" + changeSet.getComment();
        }
        comment = comment.replaceAll("^\\s*", "");
        return comment;
    }
    
    public static IContributor getContributor(ITeamRepository repository, IChangeSet changeSet) throws TeamRepositoryException {
        IContributor contributor = null;
        if (cachedContributors.containsKey(changeSet.getAuthor().getItemId().getUuidValue())) {
            contributor = cachedContributors.get(changeSet.getAuthor().getItemId().getUuidValue());
        }
        else {
            contributor = (IContributor) repository.itemManager().fetchCompleteItem(changeSet.getAuthor(), 0, null);
            cachedContributors.put(changeSet.getAuthor().getItemId().getUuidValue(), contributor);
        }
        return contributor;
    }
    
    public static IWorkspaceConnection getWorkspaceConnection(ITeamRepository repository, IWorkspaceManager workspaceManager, String streamName) throws TeamRepositoryException {
        IWorkspaceSearchCriteria search = IWorkspaceSearchCriteria.FACTORY.newInstance()
                .setExactName(streamName)
                .setKind(IWorkspaceSearchCriteria.STREAMS);
        List<IWorkspaceHandle> wshs = workspaceManager.findWorkspaces(search, 1, null);
        return SCMPlatform.getWorkspaceManager(repository).getWorkspaceConnection(wshs.get(0), null);
    }
    
    public static List<ChangeSetConfig> getChangesetsForComponent(ITeamRepository repository, IWorkspaceConnection workspaceConnection, IComponent component) throws TeamRepositoryException {
        long startTime = System.nanoTime();
        int changeSetCount = 0;
        List<ChangeSetConfig> changeSetConfigs = new LinkedList<ChangeSetConfig>();
        IChangeHistory changeHistory = workspaceConnection.changeHistory(component);
        while (changeHistory != null) {
            List<IChangeSetHandle> changeSetHandles = new ArrayList<IChangeSetHandle>(100);
            List<ChangeSetConfig> eraChangeSetConfigs = new ArrayList<ChangeSetConfig>(100);
            IChangeHistoryDescriptor changeHistoryDescriptor = changeHistory.getHistoryDescriptor(true, null);
            for (IChangeHistoryEntryChange chec: changeHistoryDescriptor.recent()) {
                changeSetCount += 1;
                changeSetHandles.add(chec.changeSet());
            }
            @SuppressWarnings("unchecked")
            List<IChangeSet> changeSets = (List<IChangeSet>) repository.itemManager().fetchCompleteItems(changeSetHandles, 1, null);
            for (IChangeSet changeSet: changeSets) {
                eraChangeSetConfigs.add(new ChangeSetConfig(changeSet, changeHistory.configuration()));
            }
            changeSetConfigs.addAll(0, eraChangeSetConfigs);
            changeHistory = changeHistoryDescriptor.previousHistory();
        }
        System.out.println("Getting changeset meta data for component " + component.getName() + " with " + changeSetCount + " changes - Took " + Util.getReadableTime(System.nanoTime() - startTime));
        return changeSetConfigs;
    }
    
    public static IFileItem getFileItem( IWorkspaceManager workspaceManager, IVersionableHandle versionableHandle) throws TeamRepositoryException, EWMException {
        if (versionableHandle == null) {
            throw new EWMException("[ERROR] Unable to get the file metadata for change because the vesionable handle is null");
        }
        if (versionableHandle instanceof IFolderHandle) {
            return null;
        }

        IFileItem fileItem = null;
        try {
            fileItem = (IFileItem) workspaceManager.versionableManager().fetchCompleteState(versionableHandle, null);
        } catch (IllegalArgumentException e) {
            throw new EWMException("[ERROR] Unable to get the file metadata for change " + versionableHandle.toString());
        }
        
        return fileItem;
    }
    
    @SuppressWarnings("unchecked")
    public static Path getRelativeFilePath(IConfiguration configuration, IFileItem fileItem) throws TeamRepositoryException {
        Path path = Paths.get("");

        //We get the history of the parent of the file because otherwise it will get the path of the after change even if we supply a before change
        //and because if a file is moved or reparented the path on the item will be the last path and not the path of this version
        //We add add the filename at the end so the full path is returned
        List<IAncestorReport> ancestors = configuration.determineAncestorsInHistory(Arrays.asList(fileItem.getParent()), null);

        if (ancestors.get(0).getNameItemPairs().isEmpty()) {
            System.out.println("No path found for " + fileItem.getName());
            return null;
        }
        
        for (INameItemPair name: (List<INameItemPair>) ancestors.get(0).getNameItemPairs()) {
            if (name.getName() == null) {
                continue;
            }
            path = path.resolve(name.getName());
        }
        
        return path.resolve(fileItem.getName());
    }
    
    public static byte[] getFileContents(ITeamRepository repository, IFileItem fileItem) throws TeamRepositoryException, IOException {

        IFileContentManager contentManager = FileSystemCore.getContentManager(repository);
        InputStream is = contentManager.retrieveContentStream(fileItem, fileItem.getContent(), null);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = is.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        return result.toByteArray();
    }
    
    private static class LoginHandler implements ILoginHandler {

        @Override
        public ILoginInfo challenge(ITeamRepository arg0) {
            return new ILoginInfo() {
                public String getUserId() {
                    return Settings.RTC_USER;
                }

                public String getPassword() {
                    return Settings.RTC_PASSWORD;
                }
            };
        }
        
    }
}
