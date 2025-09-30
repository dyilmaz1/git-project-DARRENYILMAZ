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
        }
        else {
            System.out.println("Git Repository Already Exists");
        }
    }
}