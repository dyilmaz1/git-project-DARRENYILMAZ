import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitTester {
    public static void main(String[] args) throws IOException {
        
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

    public static void cleanUp() throws IOException {
        Git.rmdir(Path.of("git"));
    }
}
