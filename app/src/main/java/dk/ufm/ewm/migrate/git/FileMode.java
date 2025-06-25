package dk.ufm.ewm.migrate.git;

public enum FileMode {

    NORMAL("100644"),
    EXECUTABLE("100755"),
    SYMLINK("120000"),
    GITLINK("160000"),
    SUBDIRECTORY("040000")
    ;

    private String octalValue;
    
    private FileMode(String octalValue) {
        this.octalValue = octalValue;
    }

    public String getOctalValue() {
        return octalValue;
    }
}
