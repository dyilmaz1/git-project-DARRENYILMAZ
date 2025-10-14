import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitWrapper {

    public void init() {
        try {
            Git.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(String filePath) {
        try {
            Git.indexFile(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String commit(String author, String message) {
        Git.makeCommit(author, message);
        try {
            String head = Files.readString(Path.of("git/HEAD")).trim();
            return head;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

     /**
     * EXTRA CREDIT:
     * Checks out a specific commit given its hash.
     * This method should read the HEAD file to determine the "checked out" commit.
     * Then it should update the working directory to match the
     * state of the repository at that commit by tracing through the root tree and
     * all its children.
     *
     * @param commitHash The SHA1 hash of the commit to check out.
     */
    public void checkout(String commitHash) {
        try {
            Path commitPath = Path.of("git/objects", commitHash);
            if (!Files.exists(commitPath)){
                String head = Files.readString(Path.of("git/HEAD")).trim();
                commitPath = Path.of("git/objects", head);
            }


            String commitBody = Files.readString(commitPath);
            String[] lines = commitBody.split("\n");
            if (lines.length == 0){
                throw new RuntimeException("nothing committed bruh");
            }
            if (!lines[0].startsWith("tree: ")){
                throw new RuntimeException("formatting is bad! start with 'tree:' ");
            }

            String rootTree = lines[0].substring("tree: ".length()).trim();
            restoreTree(rootTree);

            Files.writeString(Path.of("git/HEAD"), commitHash + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void restoreTree(String treeSha) throws IOException {
        Path treePath = Path.of("git/objects", treeSha);
        if (!Files.exists(treePath)){
            throw new RuntimeException("tree not found: " + treeSha);
        }

        String[] lines = Files.readString(treePath).split("\n");
        for (String line : lines){
            if (!line.isEmpty()){
                String[] parts = line.split(" ", 3);
                if (parts.length >= 3){
                    String type = parts[0];
                    String sha = parts[1];
                    Path target = Path.of(parts[2]);

                    if (type.equals("tree")){
                        Files.createDirectories(target);
                        restoreTree(sha);
                    }
                    else if (type.equals("blob")){
                        if (target.getParent() != null){
                            Files.createDirectories(target.getParent());
                        }
                        Path blobPath = Path.of("git/objects", sha);
                        byte[] data = Files.readAllBytes(blobPath);
                        Files.write(target, data);
                    }
                }
            }
        }
    }
}