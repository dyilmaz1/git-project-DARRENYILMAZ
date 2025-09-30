import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitTester {
    public static void main(String[] args) throws IOException {
        testIndexingAndBlobs();
    }

    public static void testIndexingAndBlobs() throws IOException {
        cleanUp();
        Git.init();
        FileWriter fw1 = new FileWriter("file1.txt");
        fw1.write("Hello World!");
        fw1.close();

        FileWriter fw2 = new FileWriter("file2.txt");
        fw2.write("This is a Test");
        fw2.close();

        Git.indexAndBlobFile(Path.of("file1.txt"));
        Git.indexAndBlobFile(Path.of("file2.txt"));


        Files.delete(Path.of("file1.txt"));
        Files.delete(Path.of("file2.txt"));
        Git.resetFiles();
    }

    public static void testInitializationAndCleanup() throws IOException {
        cleanUp();
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
