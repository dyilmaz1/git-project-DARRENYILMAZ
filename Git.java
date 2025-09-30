import java.io.IOException;
import java.nio.file.*;

public class Git {
    public static void main(String[] args) throws IOException {
        init();
    }
    
    public static void init() throws IOException {
        boolean didSomething = false;
        if (!Files.exists(Path.of("git"))) {
            Files.createDirectory(Path.of("git"));
            didSomething = true;
        }
        if (!Files.exists(Path.of("git/objects"))) {
            Files.createDirectory(Path.of("git/objects"));
            didSomething = true;
        }
        if (!Files.exists(Path.of("git/index"))) {
            Files.createFile(Path.of("git/index"));
            didSomething = true;
        }
        if (!Files.exists(Path.of("git/HEAD"))) {
            Files.createFile(Path.of("git/HEAD"));
            didSomething = true;
        }
        if (didSomething) {
            System.out.println("Git Repository Created");
        } else {
            System.out.println("Git Repository Already Exists");
        }
    }

    public static void BLOBfile(Path path) throws IOException {
        String sha1 = Hash.hashFile(path);
        Path BLOBPath = Files.createFile(Path.of("git/objects/" + sha1));
        Files.write(BLOBPath, Files.readAllLines(path));
    }

    public static void BLOBFolder(Path path) throws IOException {
        Files.list(path).forEach(p -> {
            try {
                if (!Files.isDirectory(p)) {
                    BLOBfile(p);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    

    // Helper methods
    

    public static void rmdir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(p -> {
                try {
                    rmdir(p);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
        }
        Files.delete(path);
    }
}