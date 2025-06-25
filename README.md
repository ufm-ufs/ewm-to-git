# EWM to git migration tool
This tool will migrate each component in a Stream to its own git repository.

## Usage

1. Build with gradle
1. Fill out the properties file - migrate.properties
1. Include EWM jar files for your version of EWM
1. Run dk.ufm.rtc.Migrate

### Properties

You should make a copy of the migrate.properties.sample file called migrate.properties and input the relevant settings.

### Jar files

The jar files for the "Plain Java Client Libraries" are not included here and should be downloaded from jazz.net. They are not included partly due to licensing issues and partly because it is important that you get the libraries that match your installation version (or it won't work). You should extract the jar files in a folder in the "lib" folder.

You can find the jar files under Downloads for your release of EWM. You need to select the tab "Repositories and more". The "Plain Java Client Libraries" is under the "other downloads" section.

### Run

You can then run the main class, dk.ufm.rtc.Migrate

## Notes
Rename, reparent and rename_reparent are all handled as renames. All the kinds with modify in its name (modify_rename, modify_reparent, modify_rename_reparent) are all handled as a rename followed by a modify.

## Implementation Overview
This implementation uses the git command line tool to do the migration. JGit was considered but JGit does not implement fast-import. Fast-import is a significantly faster tool for importing many commits into git.

The program starts by getting all components in a stream. For each component, a thread for reading EWM and a thread for writing to git are created.

The EWM thread will work its way through changesets in the component from current changeset to previous changeset until hitting the root changeset. The order may not be in chronological. Eras are taken into account.

For each changeset, a commit is created. For each change in a changeset, a thread is spawned (max 25) to download the contents of the change. Once all the changes in a changeset are downloaded and included in a git commit, the commit is sent to the git thread and the next changeset is processed. Folders are disregarded because git cannot track empty folders.

The git thread writes to the git fast-import command.