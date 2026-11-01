import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Keymanager {
    public static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private final Path credentialFile;
    private byte[] storedSalt;
    private byte[] storedHash;
    private String storedUsername;

    public Keymanager(String credentialFileName) {
        this.credentialFile = Paths.get(credentialFileName);
        loadCredentials();
    }

    public byte[] deriveKey(char[] pwd, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(pwd, salt, ITERATIONS, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Key derivation failed", e);
        } finally { spec.clearPassword(); }
    }

    public boolean verifyKey(byte[] hash) { return storedHash != null && MessageDigest.isEqual(hash, storedHash); }
    public boolean hasCredentials() { return storedSalt != null && storedHash != null; }
    public String getUsername() { return storedUsername; }
    public byte[] getStoredSalt() { return storedSalt == null ? null : storedSalt.clone(); }

    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public void registerMasterPassword(char[] pwd) throws IOException { registerAccount(storedUsername, pwd); }

    public void registerAccount(String username, char[] pwd) throws IOException {
        byte[] salt = generateSalt();
        byte[] hash = deriveKey(pwd, salt);
        saveCredentials(username, salt, hash);
    }

    public void changeMasterPassword(char[] currentPassword, char[] newPassword) throws IOException {
        if (!authenticate(currentPassword)) throw new IllegalArgumentException("Current password is incorrect.");
        byte[] salt = generateSalt();
        saveCredentials(storedUsername, salt, deriveKey(newPassword, salt));
    }

    public boolean authenticate(char[] pwd) {
        return hasCredentials() && verifyKey(deriveKey(pwd, storedSalt));
    }

    public boolean authenticate(String username, char[] pwd) {
        return hasCredentials() && storedUsername != null && storedUsername.equalsIgnoreCase(username)
                && authenticate(pwd);
    }

    private void saveCredentials(String username, byte[] salt, byte[] hash) throws IOException {
        String content = (username == null ? "" : username) + System.lineSeparator()
                + Base64.getEncoder().encodeToString(salt) + System.lineSeparator()
                + Base64.getEncoder().encodeToString(hash) + System.lineSeparator();
        Files.write(credentialFile, content.getBytes(StandardCharsets.UTF_8));
        storedUsername = username;
        storedSalt = salt;
        storedHash = hash;
    }

    private void loadCredentials() {
        try {
            if (!Files.exists(credentialFile)) return;
            List<String> lines = Files.readAllLines(credentialFile, StandardCharsets.UTF_8);
            // Support the older two-line auth.dat format (password only).
            if (lines.size() >= 3) {
                storedUsername = lines.get(0).trim();
                storedSalt = Base64.getDecoder().decode(lines.get(1).trim());
                storedHash = Base64.getDecoder().decode(lines.get(2).trim());
            } else if (lines.size() >= 2) {
                storedSalt = Base64.getDecoder().decode(lines.get(0).trim());
                storedHash = Base64.getDecoder().decode(lines.get(1).trim());
                storedUsername = "Administrator";
            }
        } catch (IOException | IllegalArgumentException e) {
            storedSalt = null; storedHash = null; storedUsername = null;
        }
    }
}
