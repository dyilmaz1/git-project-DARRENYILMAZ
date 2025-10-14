import java.io.*;
import java.nio.file.*;

public class GitWrapperSimpleTest {
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        // Run tests in the current directory, keep it super simple.
        run("testInit", GitWrapperSimpleTest::testInit);
        run("testAddOnce", GitWrapperSimpleTest::testAddOnce);
        run("testAddIdempotent", GitWrapperSimpleTest::testAddIdempotent);
        run("testAddModified", GitWrapperSimpleTest::testAddModified);
        run("testCommit", GitWrapperSimpleTest::testCommit);
        run("testCheckout", GitWrapperSimpleTest::testCheckout);
        run("testCheckoutBad", GitWrapperSimpleTest::testCheckoutBad);

        System.out.println("\n=== SUMMARY ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    // -------- tiny "framework" --------
    interface TestBody { void run() throws Exception; }

    static void run(String name, TestBody t) {
        try {
            cleanUp();
            t.run();
            System.out.println("[PASS] " + name);
            passed++;
        } catch (Throwable e) {
            System.out.println("[FAIL] " + name + " -> " + e.getMessage());
            failed++;
        }
    }

    static void expect(boolean cond, String msg) {
        if (!cond) throw new RuntimeException(msg);
    }

    // -------- file helpers --------
    static void write(String path, String content) {
        try {
            Path p = Paths.get(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            Files.writeString(p, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String read(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    static void cleanUp() {
        // delete the "git" dir and any files we touched
        deleteIfExists(Paths.get("git"));
        deleteIfExists(Paths.get("a.txt"));
        deleteIfExists(Paths.get("b.txt"));
        deleteIfExists(Paths.get("c.txt"));
        deleteIfExists(Paths.get("x.txt"));
        deleteIfExists(Paths.get("note.txt"));
        deleteIfExists(Paths.get("src"));
    }

    static void deleteIfExists(Path p) {
        try {
            if (Files.notExists(p)) return;
            if (Files.isDirectory(p)) {
                try (var s = Files.list(p)) {
                    for (Path c : (Iterable<Path>) s::iterator) deleteIfExists(c);
                }
            }
            Files.deleteIfExists(p);
        } catch (IOException ignored) {}
    }

    // -------- tests --------
    // 1) init creates git stuff
    static void testInit() throws Exception {
        GitWrapper gw = new GitWrapper();
        gw.init();
        expect(exists("git"), "git folder missing");
        expect(exists("git/objects"), "git/objects missing");
        expect(exists("git/index"), "git/index missing");
        expect(exists("git/HEAD"), "git/HEAD missing");
    }

    // 2) add one file creates an index entry and a blob
    static void testAddOnce() throws Exception {
        GitWrapper gw = new GitWrapper();
        gw.init();
        write("a.txt", "hello");
        gw.add("a.txt");
        String idx = read("git/index").trim();
        expect(!idx.isEmpty(), "index should have one line");
        String sha = idx.split("\\s+")[0];
        expect(exists("git/objects/" + sha), "blob object missing");
    }

    // 3) adding same file twice shouldn't duplicate
    static void testAddIdempotent() throws Exception {
        GitWrapper gw = new GitWrapper();
        gw.init();
        write("b.txt", "same");
        gw.add("b.txt");
        gw.add("b.txt");
        String idx = read("git/index").trim();
        long lines = idx.isEmpty() ? 0 : idx.lines().count();
        expect(lines == 1, "index should still have exactly 1 line");
    }

    // 4) modified file should update index (hash changes)
    static void testAddModified() throws Exception {
        GitWrapper gw = new GitWrapper();
        gw.init();
        write("c.txt", "v1");
        gw.add("c.txt");
        String first = read("git/index").trim();
        write("c.txt", "v2");
        gw.add("c.txt");
        String second = read("git/index").trim();
        expect(!first.equals(second), "index should change after modification");
        long lines = second.isEmpty() ? 0 : second.lines().count();
        expect(lines == 1, "still one entry for the file");
    }

    // 5) commit writes commit object and updates HEAD
    static void testCommit() throws Exception {
        GitWrapper gw = new GitWrapper();
        gw.init();
        write("src/nested/d.txt", "data");
        gw.add("src/nested/d.txt");
        String sha = gw.commit("Alice", "add d");
        expect(sha != null && sha.length() == 40, "commit sha looks wrong");
        String head = read("git/HEAD").trim();
        expect(sha.equals(head), "HEAD should equal the commit sha");
        expect(exists("git/objects/" + sha), "commit object should exist");
    }

    // 6) checkout restores an older version
    static void testCheckout() throws Exception {
        GitWrapper gw = new GitWrapper();
        gw.init();
        write("note.txt", "v1");
        gw.add("note.txt");
        String c1 = gw.commit("Me", "v1");
        write("note.txt", "v2");
        gw.add("note.txt");
        String c2 = gw.commit("Me", "v2");
        expect(!c1.equals(c2), "two different commits");
        gw.checkout(c1);
        String content = read("note.txt").trim();
        expect(content.equals("v1"), "checkout should restore v1");
    }

    // 7) checkout on a fake hash should throw
    static void testCheckoutBad() throws Exception {
        GitWrapper gw = new GitWrapper();
        gw.init();
        boolean threw = false;
        try {
            gw.checkout("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef");
        } catch (IOException e) {
            threw = true;
        }
        expect(threw, "expected checkout to throw on bad hash");
    }
}