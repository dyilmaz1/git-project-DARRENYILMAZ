import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitTester {
    public static void main(String[] args) throws IOException {
        if (!Files.exists(Path.of("file.txt"))) {
            Files.createFile(Path.of("file.txt"));
        }
        if (!Files.exists(Path.of("file2.txt"))){
            Files.createFile(Path.of("file2.txt"));
        }
        cleanUp();
        Git.init();
        System.out.println("Is repo initialized: " + isRepositoryInitialized());
        Git.indexAndBlobFile(Path.of("file.txt"));
        Git.indexAndBlobFile(Path.of("file2.txt"));
        System.out.println("Is file.txt BLOBBED: " + isFileBLOBBED(Path.of("file.txt")));
    }

    public static void testInitializationAndCleanup() throws IOException {
        Git.init();
        if (isRepositoryInitialized()){
            System.out.println("Repository is properly initialized");
        } else {
            System.out.println("Repository failed to initialize");
        }
        cleanUp();
        if (isRepositoryInitialized()){
            System.out.println("Repository failed to be removed");
        }
        Git.init();
        if (isRepositoryInitialized()){
            System.out.println("Repository is properly initialized");
        } else {
            System.out.println("Repository failed to initialize");
        }
        cleanUp();
        if (isRepositoryInitialized()) {
            System.out.println("Repository failed to be removed");
        }
        else {
            System.out.println("Test complete");
        }
    }

    public static boolean isRepositoryInitialized() {
        if (!Files.exists(Path.of("git"))) {
            return false;
        }
        if (!Files.exists(Path.of("git/objects"))) {
            return false;
        }
        if (!Files.exists(Path.of("git/index"))) {
            return false;
        }
        if (!Files.exists(Path.of("git/HEAD"))) {
            return false;
        }
        return true;
    }

    // HERE IS 2.3.1
    public static boolean isFileBLOBBED(Path filePath) throws IOException{
        return Files.exists(Path.of("git/objects/" + Hash.hashFile(filePath)));
    }

    public static boolean isFolderBLOBBED(Path folderPath) throws IOException {
        return Files.list(folderPath).allMatch(p -> {
            try {
                return Files.isDirectory(p)
                    || Files.exists(Path.of("git/objects/" + Hash.hashFile(p)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static void cleanUp() throws IOException {
        if (!Files.exists(Path.of("git"))) {return;}
        Git.rmdir(Path.of("git"));
    }

}
