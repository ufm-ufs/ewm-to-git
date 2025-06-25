package dk.ufm.ewm.migrate.git;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import dk.ufm.ewm.migrate.Settings;

//Thread for writing to fast-import
public class GitRunnable implements Runnable {

    private Path path;
    private BlockingQueue<IFastImportGenerator> fastImportItems;
    
    private int numWrites = 0;

    public GitRunnable(Path path, BlockingQueue<IFastImportGenerator> fastImportItems) {
        this.path = path;
        this.fastImportItems = fastImportItems;
    }

    @Override
    public void run() {
        try {
            Process process = Git.fastImport(path);
            writeFastImport(process.getOutputStream());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeFastImport(OutputStream outputStream) throws InterruptedException, IOException {
        OutputStream fileOutput = new FileOutputStream(Paths.get(Settings.LOG_DIR, "fast-import_" + path.getFileName() + ".txt").toFile());
        try {
            while (true) {
                IFastImportGenerator fastImportItem = fastImportItems.take();
                if (fastImportItem instanceof Poison) {
                    break;
                }
                if (fastImportItem instanceof Commit) {
                    fileOutput.write(fastImportItem.writeImportData());
                    fileOutput.flush();
                }
                outputStream.write(fastImportItem.writeImportData());
                outputStream.flush();
                numWrites++;
                
                if (numWrites % 100 == 0) {
                    System.out.println("Number of writes to git: " + numWrites);
                }
            }
        } finally {
            outputStream.close();
            fileOutput.close();
        }
    }
}
