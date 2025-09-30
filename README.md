# git-project-DARRENYILMAZ
Git Project

Git.java:

The init() method within the Git.java file initializes a git repository in the folder that the Git.java file is located in. Additionally, the main method of the Git.java file calls the init() method, meaning you can run that instead if you so choose. Challenges encountered include figuring out how to tell if a repository is already created.

GitTester.java:
The testing methodology in GitTester.java considers the possibility of errors when creating a repository. By initializing a repository, checking if it exists, then deleting it, and checking that it doesn't exist multiple times, we can ensure proper creation and deletion. cleanUp() method uses recursive clearing to delete everything within the "git" folder.

Hash.java:
Implemented the sha1 hash using messagedigest

BLOBBING:
To create a blob for a file, simply call the Git.BLOBfile on it.