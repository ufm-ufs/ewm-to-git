package dk.ufm.ewm.migrate.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dk.ufm.ewm.migrate.Settings;

public class Commit implements IFastImportGenerator {

    private static final byte[] COMMIT_KEYWORD = "commit".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    private static final byte[] ENCODING_KEYWORD = "encoding".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    private static final byte[] FROM_KEYWORD = "from".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    private static final byte[] MERGE_KEYWORD = "merge".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    private static final byte[] AUTHOR_KEYWORD = "author".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    private static final byte[] COMMITTER_KEYWORD = "committer".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    private static final byte[] MARK_KEYWORD = "mark".getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET);
    
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss");

    private String branchName;
    private Mark mark;
    private String encoding;
    private Data message;
    private Committer author;
    private Committer committer;
    Commitish from;
    List<Commitish> merge = new ArrayList<Commitish>();
    List<IFileChange> fileChanges = new ArrayList<IFileChange>();
    
    

    public Commit(String branchName, String message, Committer committer, Commitish from) {
        this.mark = new Mark();
        this.branchName = branchName;
        this.message = new Data(message.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        this.committer = committer;
        this.from = from;
    }
    
    public Commit(String branchName, String encoding, Data message, Committer author, Committer committer,
            Commitish from, List<Commitish> merge, List<IFileChange> fileChanges) {
        this.mark = new Mark();
        this.branchName = branchName;
        this.encoding = encoding;
        this.message = message;
        this.author = author;
        this.committer = committer;
        this.from = from;
        this.merge = merge;
        this.fileChanges = fileChanges;
    }



    public Mark getMark() {
        return mark;
    }

    public List<Commitish> getMerge() {
        return merge;
    }

    public List<IFileChange> getFileChanges() {
        return fileChanges;
    }

    @Override
    public byte[] writeImportData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);

        os.write(COMMIT_KEYWORD);
        os.write(SP);
        os.write(branchName.getBytes(Settings.GIT_METADATA_OUTPUT_CHARSET));
        os.write(LF);

        os.write(MARK_KEYWORD);
        os.write(SP);
        os.write(mark.writeImportData());
        os.write(LF);

        if (author != null) {
            os.write(AUTHOR_KEYWORD);
            os.write(SP);
            os.write(author.writeImportData());
        }

        os.write(COMMITTER_KEYWORD);
        os.write(SP);
        os.write(committer.writeImportData());

        if (encoding != null) {
            os.write(ENCODING_KEYWORD);
            os.write(SP);
        }

        os.write(message.writeImportData());

        if (!(from instanceof InitialFrom)) {
            os.write(FROM_KEYWORD);
            os.write(SP);
            os.write(from.writeImportData());
            os.write(LF);
        }

        for (Commitish n_merge : merge) {
            os.write(MERGE_KEYWORD);
            os.write(SP);
            os.write(n_merge.writeImportData());
            os.write(LF);
        }

        for (IFileChange fileChange : fileChanges) {
            os.write(fileChange.writeImportData());
        }

        os.write(LF);

        return os.toByteArray();
    }
    
    @Override
    public String toString() {
        String result = "[" + committer.getTimestamp().format(formatter) + "] ";
        result += committer.getName();
        try {
            result += " - " + new String(message.getData(), "UTF8");
        } catch (UnsupportedEncodingException e) {
        }
        return result;
    }
}
