package dk.ufm.ewm.migrate.git;

import java.nio.file.Path;

public interface IFileChange extends IFastImportGenerator {

    public Path getPath();

}
