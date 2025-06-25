package dk.ufm.ewm.migrate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.client.IWorkspaceConnection;
import com.ibm.team.scm.client.IWorkspaceManager;
import com.ibm.team.scm.client.SCMPlatform;
import com.ibm.team.scm.common.IComponent;

import dk.ufm.ewm.migrate.ewm.EWM;
import dk.ufm.ewm.migrate.ewm.EWMRunnable;
import dk.ufm.ewm.migrate.git.Git;
import dk.ufm.ewm.migrate.git.GitRunnable;
import dk.ufm.ewm.migrate.git.IFastImportGenerator;

public class Migrate {

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        System.setOut(new TimestampPrintStream(System.out));
        System.out.println("Program start");
        BlockingQueue<IFastImportGenerator> fastImportItem = new LinkedBlockingQueue<IFastImportGenerator>();

        try {
            ITeamRepository repository = EWM.login();
            IWorkspaceManager workspaceManager = SCMPlatform.getWorkspaceManager(repository);
            IWorkspaceConnection workspaceConnection = EWM.getWorkspaceConnection(repository, workspaceManager, Settings.STREAM_NAME);
            @SuppressWarnings("unchecked")
            List<IComponent> components = (List<IComponent>) repository.itemManager().fetchCompleteItems(workspaceConnection.getComponents(), 1, null);
            for (IComponent component : components) {
                Path path = Paths.get(Settings.GIT_REPO_ROOT, component.getName());
                Git.init(path);
                //If this a windows system ignorecase will be true, which will make File.java and file.java the same, which we don't want.
                Git.config(path, "core.ignorecase", "false");
                
                Thread gitThread = new Thread(new GitRunnable(path, fastImportItem));
                gitThread.start();

                Thread rtcThread = new Thread(new EWMRunnable(fastImportItem, repository, workspaceManager, workspaceConnection, component));
                rtcThread.start();
                
                rtcThread.join();
                gitThread.join();

            }
        } catch (TeamRepositoryException | InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        EWMRunnable.shutdownExecutorService();
        System.out.println("Program end - Took " + Util.getReadableTime(System.nanoTime() - startTime));
    }
    
    //This class is incomplete, so the timestamp and filewriting will only work for println(String)
    private static class TimestampPrintStream extends PrintStream {
        
        private static OutputStream fileOutput;
        
        static {
           try {
            fileOutput = new FileOutputStream(Paths.get(Settings.LOG_DIR, "stdout.txt").toFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        }

        public TimestampPrintStream(OutputStream out) {
            super(out);
        }
        
        @Override
        public void println(String x) {
            String s = "(" + String.format("%1$-16s", Thread.currentThread().getName()) + ")[" + String.format("%1$-30s", Instant.now().toString()) + "] " + x;
            try {
                fileOutput.write((s + "\n").getBytes("UTF8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.println(s);
        }
        
    }

}
