package dk.ufm.ewm.migrate.ewm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.repository.common.TeamRepositoryException;

import dk.ufm.ewm.migrate.git.Blob;
import dk.ufm.ewm.migrate.git.Data;
import dk.ufm.ewm.migrate.git.FileDelete;
import dk.ufm.ewm.migrate.git.FileMode;
import dk.ufm.ewm.migrate.git.FileModify;
import dk.ufm.ewm.migrate.git.FileRename;
import dk.ufm.ewm.migrate.git.IFastImportGenerator;
import dk.ufm.ewm.migrate.git.IFileChange;

//Thread that reads EWMDownloads from the queue and downloads them until poisoned
public class EWMDownloadRunnable implements Runnable {
    
    private BlockingQueue<IFastImportGenerator> fastImportItems;
    private BlockingQueue<EWMDownload> downloads;
    private BlockingQueue<IFileChange> fileChanges;
    
    

    public EWMDownloadRunnable(BlockingQueue<IFastImportGenerator> fastImportItems, BlockingQueue<EWMDownload> downloads, BlockingQueue<IFileChange> fileChanges) {
        this.fastImportItems = fastImportItems;
        this.downloads = downloads;
        this.fileChanges = fileChanges;
    }

    @Override
    public void run() {
        try {
            while (true) {
                EWMDownload download = downloads.take();
                if (download instanceof EWMDownloadPoison) {
                    break;
                }
                switch(ChangeType.getChangeType(download.getChange())) {
                case DELETE: {
                    IFileItem fileItemBefore = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().beforeState());
                    if (fileItemBefore == null) {
                        break;
                    }
                    Path fileItemBeforePath = EWM.getRelativeFilePath(download.getConfiguration(), fileItemBefore);
                    fileChanges.put(new FileDelete(fileItemBeforePath));
                    }
                    break;
                // We take the result of the merge and don't look at the parents
                case MERGE:
                case ADD:
                case MODIFY: {
                    IFileItem fileItem = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().afterState());
                    //This is a folder
                    if (fileItem == null) {
                        break;
                    }
                    putFileModify(download, fileItem);
                    }
                    break;
                case NONE: {
                    IFileItem fileItem = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().afterState());
                    System.out.println("[ERROR] Change type not recognized. Change kind is " + download.getChange().kind() + " for " + download.getOneLiner() + " for change " + fileItem.getName());
                }
                    break;
                case MODIFY_REPARENT:
                case MODIFY_RENAME:
                case MODIFY_RENAME_REPARENT: {
                    IFileItem fileItem = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().afterState());
                    //This is a folder
                    if (fileItem == null) {
                        break;
                    }
                    Path fileItemPath = EWM.getRelativeFilePath(download.getConfiguration(), fileItem);
                    IFileItem fileItemBefore = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().beforeState());
                    Path fileItemBeforePath = EWM.getRelativeFilePath(download.getConfiguration(), fileItemBefore);
                    fileChanges.put(new FileRename(fileItemPath, fileItemBeforePath, false));
                    putFileModify(download, fileItem);
                }
                    break;
                case RENAME:
                case REPARENT:
                case RENAME_REPARENT: {
                    IFileItem fileItem = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().afterState());
                    if (fileItem == null) {
                        break;
                    }
                    Path fileItemPath = EWM.getRelativeFilePath(download.getConfiguration(), fileItem);
                    IFileItem fileItemBefore = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().beforeState());
                    Path fileItemBeforePath = EWM.getRelativeFilePath(download.getConfiguration(), fileItemBefore);
                    fileChanges.put(new FileRename(fileItemPath, fileItemBeforePath, false));
                }
                    break;
                default: {
                    IFileItem fileItem = EWM.getFileItem(download.getWorkspaceManager(), download.getChange().afterState());
                    System.out.println("[ERROR] Change type not recognized. Change kind is " + download.getChange().kind() + " for " + download.getOneLiner() + " for change " + fileItem.getName());
                }
                    break;
                
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TeamRepositoryException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EWMException e) {
            e.printStackTrace();
        }
    }
    
    private void putFileModify(EWMDownload download, IFileItem fileItem) throws TeamRepositoryException, IOException, InterruptedException {
        Path fileItemPath = EWM.getRelativeFilePath(download.getConfiguration(), fileItem);
        byte[] fileContents = EWM.getFileContents(download.getRepository(), fileItem);
        Blob blob =  new Blob(new Data(fileContents));
        fastImportItems.put(blob);
        
        FileMode fileMode = FileMode.NORMAL;
        if (fileItem.isExecutable()) {
            fileMode = FileMode.EXECUTABLE;
        }
        fileChanges.put(new FileModify(blob.getMark(), fileMode, fileItemPath));
    }

}
