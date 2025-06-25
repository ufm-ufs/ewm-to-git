package dk.ufm.ewm.migrate.ewm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.client.IWorkspaceConnection;
import com.ibm.team.scm.client.IWorkspaceManager;
import com.ibm.team.scm.common.IChange;
import com.ibm.team.scm.common.IChangeSet;
import com.ibm.team.scm.common.IComponent;
import com.ibm.team.scm.common.IFolderHandle;

import dk.ufm.ewm.migrate.Settings;
import dk.ufm.ewm.migrate.Util;
import dk.ufm.ewm.migrate.git.Commit;
import dk.ufm.ewm.migrate.git.Commitish;
import dk.ufm.ewm.migrate.git.Committer;
import dk.ufm.ewm.migrate.git.IFastImportGenerator;
import dk.ufm.ewm.migrate.git.IFileChange;
import dk.ufm.ewm.migrate.git.InitialFrom;
import dk.ufm.ewm.migrate.git.Poison;

//Orchestrates all of the EWM related work
public class EWMRunnable implements Runnable {

    private BlockingQueue<IFastImportGenerator> fastImportItems;

    private BlockingQueue<EWMDownload> downloadQueue = new LinkedBlockingQueue<EWMDownload>();

    private ITeamRepository repository;
    private IWorkspaceManager workspaceManager;
    private IWorkspaceConnection workspaceConnection;
    private Commitish parentCommit;
    private IComponent component;
    
    private int numCommits = 0;
    
    private static final int MAX_NUM_THREADS = 25;
    
    private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUM_THREADS);
    
    public EWMRunnable(BlockingQueue<IFastImportGenerator> fastImportItems, ITeamRepository repository, IWorkspaceManager workspaceManager, IWorkspaceConnection workspaceConnection, IComponent component) throws TeamRepositoryException {
        this.fastImportItems = fastImportItems;
        this.repository = repository;
        this.workspaceManager = workspaceManager;
        this.workspaceConnection = workspaceConnection;
        this.component = component;
        parentCommit = new InitialFrom();
    }

    @Override
    public void run() {
        try {
            List<ChangeSetConfig> changeSetConfigs = EWM.getChangesetsForComponent(repository, workspaceConnection, component);
            handleComponent(component, changeSetConfigs);
        } catch (TeamRepositoryException e) {
            e.printStackTrace();
        }
    }
    
    public static void shutdownExecutorService() {
        executorService.shutdown();
    }
    
    private void handleComponent(IComponent component, List<ChangeSetConfig> changeSetConfigs) throws TeamRepositoryException {
        long startTime = System.nanoTime();
        Path pathRoot = Paths.get(Settings.GIT_REPO_ROOT, component.getName());
        try {

            for (ChangeSetConfig changeSetConfig: changeSetConfigs) {
                IChangeSet changeSet = changeSetConfig.getChangeSet();
                Commit commit = createCommit(repository, workspaceManager, changeSetConfig.getChangeSet());

                handleMultiThreaded(commit, changeSet, changeSetConfig.getConfiguration(), pathRoot);

                if (!allFilesFromChangesetInCommit(changeSet, commit)) {
                    System.out.println("[ERROR] Change set contains files not in commit" );
                }

                if (!commit.getFileChanges().isEmpty()) {
                    fastImportItems.put(commit);
                    numCommits++;
                }
                else {
                    System.out.println("[WARNING] Commit with no commit-able changes - " + changeSetOneLiner(changeSetConfig.getChangeSet(), commit));
                }
                
                System.out.println("Commit " + numCommits + " with " + commit.getFileChanges().size() + " files - " + changeSetOneLiner(changeSetConfig.getChangeSet(), commit));
            }
            fastImportItems.put(new Poison());
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Converting component " + component.getName() + " to git took " + Util.getReadableTime(System.nanoTime() - startTime));
    }
    
    //This is a sanity check that the file names we downloaded match the file names in the changeset
    @SuppressWarnings("unchecked")
    private boolean allFilesFromChangesetInCommit(IChangeSet changeSet, Commit commit) throws TeamRepositoryException {
        boolean result = true;
        for (IChange change: (List<IChange>) changeSet.changes()) {
            if (change.item() instanceof IFolderHandle) {
                continue;
            }
            IFileItem file = null;
            boolean fileFound = false;
            if (change.afterState() != null) {
                IFileItem fileItem = (IFileItem) workspaceManager.versionableManager().fetchCompleteState(change.afterState(), null);
                file = fileItem;
                for (IFileChange fileChange: commit.getFileChanges()) {
                    if (fileChange.getPath().endsWith(fileItem.getName())) {
                        fileFound = true;
                        break;
                    }
                }
            }

            if (change.beforeState() != null) {
                IFileItem beforeFileItem = (IFileItem) workspaceManager.versionableManager().fetchCompleteState(change.beforeState(), null);
                file = beforeFileItem;
                for (IFileChange fileChange: commit.getFileChanges()) {
                    if (fileChange.getPath().endsWith(beforeFileItem.getName())) {
                        fileFound = true;
                        break;
                    }
                }
            }
            if (!fileFound) {
                System.out.println("[ERROR] File not found in changeset [" + changeSet.getItemId().getUuidValue() + "] " + file.getName());
            }
            result &= fileFound;
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Commit handleMultiThreaded(Commit commit, IChangeSet changeSet, IConfiguration configuration, Path pathRoot) throws TeamRepositoryException, IOException, InterruptedException {
        BlockingQueue<IFileChange> fileChanges = new LinkedBlockingQueue<IFileChange>();
        int numThreads = changeSet.changes().size() < MAX_NUM_THREADS ? changeSet.changes().size(): MAX_NUM_THREADS;
        ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<Object>(executorService);
        for (int i = 0; i < numThreads; i++) {
            completionService.submit(new EWMDownloadRunnable(fastImportItems, downloadQueue, fileChanges), new Object());
        }
        
        for (IChange change: (List<IChange>) changeSet.changes()) {
                downloadQueue.put(new EWMDownload(repository, workspaceManager, configuration, change, changeSetOneLiner(changeSet, commit)));
        }

        for (int i = 0; i < numThreads; i++) {
            downloadQueue.put(new EWMDownloadPoison());
        }
            
        //wait until all threads are finished
        for (int i = 0; i < numThreads; i++) {
            completionService.take();
        }
        
        commit.getFileChanges().addAll(fileChanges);

        return commit;
    }
    
    private Commit createCommit(ITeamRepository repository, IWorkspaceManager workspaceManager, IChangeSet changeSet) {
        Commit commit = null;
        try {
            IContributor contributor = EWM.getContributor(repository, changeSet);
            String commitMessage = EWM.getCommitMessage(repository, workspaceManager, changeSet);
            Date date = changeSet.getLastChangeDate();
            Committer committer = new Committer(contributor.getName(), contributor.getEmailAddress(), date);
            
            commit = new Commit(Settings.BRANCH_NAME, commitMessage, committer, parentCommit);
        } catch (TeamRepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return commit;
    }
    
    private String changeSetOneLiner(IChangeSet changeSet, Commit commit) {
        String result = "[" + changeSet.getItemId().getUuidValue() + "] ";
        result += commit.toString().replaceAll("\n\n", " - ");
        return result;
    }
}
