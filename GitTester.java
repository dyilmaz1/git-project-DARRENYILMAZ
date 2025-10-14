import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitTester {
    public static void main(String[] args) throws IOException {
        // cleanUp();
        // cleanUpTest();
        testTreesIndex();
        Git.makeCommit("mike", "hi");

        GitWrapper gw = new GitWrapper();
        gw.init();

        java.nio.file.Files.createDirectories(java.nio.file.Path.of("myProgram/inner"));
        if (!java.nio.file.Files.exists(java.nio.file.Path.of("myProgram/hello.txt"))) {
            java.nio.file.Files.writeString(java.nio.file.Path.of("myProgram/hello.txt"), "hello");
        }
        if (!java.nio.file.Files.exists(java.nio.file.Path.of("myProgram/inner/world.txt"))) {
            java.nio.file.Files.writeString(java.nio.file.Path.of("myProgram/inner/world.txt"), "world");
        }
        
        gw.add("myProgram/hello.txt");
        gw.add("myProgram/inner/world.txt");
        gw.commit("John Doe", "Initial commit");
        gw.checkout("1234567890");
    }

    public static void testTreesIndex() throws IOException {
        Git.init();
        Files.createDirectories(Path.of("test"));
        Files.createDirectories(Path.of("test/never"));
        Files.createDirectories(Path.of("test/gonna"));
        Files.createDirectories(Path.of("test/give"));
        Files.createDirectories(Path.of("test/you"));
        Files.createDirectories(Path.of("test/up"));

        if (!Files.exists(Path.of("test/hello.txt"))) Files.createFile(Path.of("test/hello.txt"));
        Git.indexFile(Path.of("test/hello.txt"));

        if (!Files.exists(Path.of("test/never/hello.txt"))) Files.createFile(Path.of("test/never/hello.txt"));
        Git.indexFile(Path.of("test/never/hello.txt"));

        if (!Files.exists(Path.of("test/gonna/hello.txt"))) Files.createFile(Path.of("test/gonna/hello.txt"));
        Git.indexFile(Path.of("test/gonna/hello.txt"));

        if (!Files.exists(Path.of("test/give/hello.txt"))) Files.createFile(Path.of("test/give/hello.txt"));
        Git.indexFile(Path.of("test/give/hello.txt"));

        if (!Files.exists(Path.of("test/you/hello.txt"))) Files.createFile(Path.of("test/you/hello.txt"));
        Git.indexFile(Path.of("test/you/hello.txt"));

        if (!Files.exists(Path.of("test/up/hello.txt"))) Files.createFile(Path.of("test/up/hello.txt"));
        Git.indexFile(Path.of("test/up/hello.txt"));

        Git.gtfIndex();
    }

    
    public static void testTreesRecursive() throws IOException {
        Git.init();
        Files.createDirectory(Path.of("test"));
        Files.createDirectory(Path.of("test/never"));
        Files.createDirectory(Path.of("test/gonna"));
        Files.createDirectory(Path.of("test/give"));
        Files.createDirectory(Path.of("test/you"));
        Files.createDirectory(Path.of("test/up"));
        Files.createFile(Path.of("test/hello.txt"));
        Files.createFile(Path.of("test/never/hello.txt"));
        Files.createFile(Path.of("test/gonna/hello.txt"));
        Files.createFile(Path.of("test/give/hello.txt"));
        Files.createFile(Path.of("test/you/hello.txt"));
        Files.createFile(Path.of("test/up/hello.txt"));

        Git.generateTreeFiles(Path.of("test"));
    }


    public static void testIndexingAndBlobs() throws IOException {
        Git.init();
        Files.createDirectory(Path.of("test"));
        try (FileWriter fw1 = new FileWriter("test/file1.txt")) {
            fw1.write("Hello World!");
        }

        try (FileWriter fw2 = new FileWriter("test/file2.txt")) {
            fw2.write("This is a Test");
        }

        Git.indexFile(Path.of("test/file1.txt"));
        Git.indexFile(Path.of("test/file2.txt"));

        try (FileWriter fw22 = new FileWriter("test/file2.txt")) {
            fw22.write("This is different");
        }

        Git.indexFile(Path.of("test/file2.txt"));


        // Files.delete(Path.of("file1.txt"));
        // Files.delete(Path.of("file2.txt"));
        // Git.resetFiles();
    }

    public static void testInitializationAndCleanup() throws IOException {
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
        if (!Files.exists(Path.of("git"))) {
            return;
        }
        Git.rmdir(Path.of("git"));
    }
    
    public static void cleanUpTest() throws IOException {
        if (!Files.exists(Path.of("test"))) {
            return;
        }
        Git.rmdir(Path.of("test"));
    }

}
