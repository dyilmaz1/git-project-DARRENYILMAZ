import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashFile(Path path) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String str : Files.readAllLines(path)) {
            sb.append(str);
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sha1(sb.toString());
    }

}
