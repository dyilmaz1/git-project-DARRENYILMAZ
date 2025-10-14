import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        if (path == null) {
            throw new IllegalArgumentException("Path is null");
        }

        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + path);
        }

        if (Files.isDirectory(path)) {
            throw new IOException("Path is a directory, not a file: " + path);
        }
        String sha1 = Hash.hashFile(path);
        Path blobPath = Path.of("git/objects/" + sha1);

        if (Files.exists(blobPath)) {
            return;
        }

        Files.createFile(blobPath);
        Files.write(blobPath, Files.readAllBytes(path));
    }


    @SuppressWarnings("CallToPrintStackTrace")
    public static void BLOBFolder(Path path) throws IOException {
        Files.list(path).forEach(p -> {
            try {
                if (!Files.isDirectory(p)) {
                    BLOBfile(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void indexFile(Path file) throws IOException {
        // make index string
        String sha1 = Hash.hashFile(file);
        String filePath = getFullPath(file);
        String toIndex = sha1 + " " + filePath;

        // skip if it has the same contents
        if (Files.lines(Path.of("git/index")).anyMatch(p -> p.equals(toIndex))) {
            return;
        }

        // remove old file if modified
        List<String> lines = Files.readAllLines(Path.of("git/index"));
        List<String> updated = lines.stream()
                .filter(line -> !line.contains(" " + filePath))
                .collect(Collectors.toList());
        FileWriter erase = new FileWriter("git/index", false); // false = overwrite
        erase.close();

        FileWriter fw = new FileWriter("git/index", true);
        for (String s : updated) {
            fw.write(s);
            fw.write('\n');
        }

        fw.flush();

        // add new
        fw.write(toIndex);
        fw.close();
        BLOBfile(file);
    }

    public static String gtfIndex() throws IOException {
        String rootName = "";
        List<Directory> workingList = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of("git/index"));
        for (String line : lines) {
            Directory dir = new Directory("blob", line.split(" ")[0], line.split(" ")[1]);
            workingList.add(dir);
        }
        while (workingList.size() > 1 || checkWorkingList(workingList)) {
            workingList.sort(Comparator.comparingInt(d -> d.toString().split("/").length).reversed()
                    .thenComparing(d -> d.toString()));
            String leafMostTreePath = workingList.get(0).toString().substring(0,
                    workingList.get(0).toString().lastIndexOf("/") + 1);
            StringBuilder tree = new StringBuilder();
            for (int i = 0; i < workingList.size(); i++) {
                if (workingList.get(i).toString().startsWith(leafMostTreePath)) {
                    tree.append(workingList.get(i).toStringFull());
                    tree.append("\n");
                    workingList.remove(i);
                    i--;
                }
            }
            if (tree.length() > 0) {
                tree.deleteCharAt(tree.length() - 1);
            }
            String sha1 = Hash.hashStr(tree.toString());
            Path blobPath = Path.of("git/objects/" + sha1);
            rootName = sha1;
            Files.createFile(blobPath);
            try (FileWriter fw = new FileWriter(blobPath.toString())) {
                fw.write(tree.toString());
            }

            Directory treedir = new Directory("tree", sha1,
                    leafMostTreePath.substring(0, leafMostTreePath.length() - 1));
            workingList.add(treedir);
        }
        
        return rootName;
    }

    private static boolean checkWorkingList(List<Directory> workingList) {
        if (workingList.isEmpty()) {
            return false;
        }
        return workingList.get(0).toString().contains("/");
    }

    public static String generateTreeFiles(Path path) throws IOException {
        return gtf(path).split(" ")[1];
    }

    private static String gtf(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            List<Path> contents = Files.list(path).collect(Collectors.toList());
            StringBuilder tree = new StringBuilder();
            for (Path p : contents) {
                tree.append(gtf(p));
                tree.append("\n");
            }
            if (tree.length() > 0) {
                tree.deleteCharAt(tree.length() - 1);
            }
            String sha1 = Hash.hashStr(tree.toString());
            Path blobPath = Path.of("git/objects/" + sha1);

            Files.createFile(blobPath);
            try (FileWriter fw = new FileWriter(blobPath.toString())) {
                fw.write(tree.toString());
            }
            return "tree " + sha1 + " " + getFullPath(path);
        } else {
            BLOBfile(path);
            return "blob " + Hash.hashFile(path) + " " + getFullPath(path);
        }
    }
    
    public static String commit(String author, String message) throws IOException {
        String treeHash = gtfIndex();
        String parent = Files.readAllLines(Paths.get("git/HEAD")).isEmpty() ? "" : Files.readAllLines(Paths.get("git/HEAD")).get(0);
        String dateTime = LocalDateTime.now().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("tree: ").append(treeHash).append("\n");
        sb.append("parent: ").append(parent).append("\n");
        sb.append("author: ").append(author).append("\n");
        sb.append("date: ").append(dateTime).append("\n");
        sb.append("message: ").append(message);
        String sha1 = Hash.hashStr(sb.toString());
        Files.createFile(Path.of("git/objects/" + sha1));
        Files.write(Path.of("git/objects/" + sha1), sb.toString().getBytes());
        Files.write(Path.of("git/HEAD"), sha1.getBytes());
        return sha1;
    }
    

    // Helper methods

    public static void resetFiles() throws IOException {
        cleardir(Path.of("git/objects"));
        try (FileWriter fw = new FileWriter("git/index", false)) {
            fw.write("");
        }
    }
    

    @SuppressWarnings("CallToPrintStackTrace")
    public static void rmdir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(p -> {
                try {
                    rmdir(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        Files.delete(path);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void cleardir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(p -> {
                try {
                    rmdir(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        Files.delete(path);
    }

    public static String getFullPath(Path relativePath) {
        return relativePath.toAbsolutePath().toString().replace(Path.of("").toAbsolutePath().toString()
                .substring(0, Path.of("").toAbsolutePath().toString().lastIndexOf('/')), "").substring(1);
        // Takes the absolute path of the file, and removes the absolute path of the current working directory.
    }

    public static String getRelativePath(Path fullPath) {
        return fullPath.toString().replace(getFullPath(Path.of("")) + "/", "");
    }

    private static class Directory {
        String type;
        String sha1;
        String pathname;

        public Directory(String type, String sha1, String pathname) {
            this.type = type;
            this.sha1 = sha1;
            this.pathname = pathname;
        }

        @Override
        public String toString() {
            return pathname;
        }

        public String toStringFull() {
            return type + " " + sha1 + " " + getRelativePath(Path.of(pathname));
        }
        
    }
    
}

