package dk.ufm.ewm.migrate.git;

public class GitWriteException extends Exception {

    private static final long serialVersionUID = 592871955401080696L;

    public GitWriteException(String message) {
        super(message);
    }
}
