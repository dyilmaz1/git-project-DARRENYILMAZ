import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GitWrapper {

    /**
     * Initializes a new Git repository.
     * This method creates the necessary directory structure
     * and initial files (index, HEAD) required for a Git repository.
     * @throws IOException 
     */
    public void init() throws IOException {
        Git.init();
    };

    /**
     * Stages a file for the next commit.
     * This method adds a file to the index file.
     * If the file does not exist, it throws an IOException.
     * If the file is a directory, it throws an IOException.
     * If the file is already in the index, it does nothing.
     * If the file is successfully staged, it creates a blob for the file.
     * @param filePath The path to the file to be staged.
     * @throws IOException 
     */
    public void add(String filePath) throws IOException {
        Path p = Path.of(filePath);
        if (!Files.exists(p) || Files.isDirectory(p)) {
            throw new IOException();
        }
        Git.indexFile(p);
    };

    /**
     * Creates a commit with the given author and message.
     * It should capture the current state of the repository by building trees based on the index file,
     * writing the tree to the objects directory,
     * writing the commit to the objects directory,
     * updating the HEAD file,
     * and returning the commit hash.
     * 
     * The commit should be formatted as follows:
     * tree: <tree_sha>
     * parent: <parent_sha>
     * author: <author>
     * date: <date>
     * message: <message>
     *
     * @param author  The name of the author making the commit.
     * @param message The commit message describing the changes.
     * @return The SHA1 hash of the new commit.
     * @throws IOException 
     */
    public String commit(String author, String message) throws IOException {
        // to-do: implement functionality here
        return Git.commit(author, message);
    };

     /**
     * EXTRA CREDIT:
     * Checks out a specific commit given its hash.
     * This method should read the HEAD file to determine the "checked out" commit.
     * Then it should update the working directory to match the
     * state of the repository at that commit by tracing through the root tree and
     * all its children.
     *
     * @param commitHash The SHA1 hash of the commit to check out.
     * @throws IOException 
     */
    public void checkout(String commitHash) throws IOException {
        String checkout = Files.readAllLines(Path.of("git/Head")).get(0);
        while (!checkout.isEmpty() && !checkout.equals(commitHash)) {
            checkout = Files.readAllLines(Path.of("git/objects/" + checkout)).get(1).split(" ")[1];
        }
        if (!checkout.equals(commitHash)) {
            throw new FileNotFoundException();
        }

        reconstruct("git/objects/" + Files.readAllLines(Path.of("git/objects/" + checkout)).get(0).split(" ")[1]);

    };

    private void reconstruct(String tree) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(tree));
        for (String file : lines) {
            String type = file.split(" ")[0];
            String hash = file.split(" ")[1];
            String path = file.split(" ")[2];
            make(type, hash, Path.of(path));
        }
    }

    private void make(String type, String hash, Path path) throws IOException {
        if (type.equals("blob")) {
            Files.createFile(path);
            Files.write(path, Files.readAllBytes(Path.of("git/objects/" + hash)));
        }
        else if (type.equals("tree")) {
            Files.createDirectory(path);
            reconstruct("git/objects/" + hash);
        }
    }
}