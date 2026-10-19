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
    
    private static final int ITERATIONS = 100_000;
    
    private static final int KEY_BITS = 256;
    
    private final Path credentialFile;
    
    private byte[] storedSalt;
    
    private byte[] storedHash;
    
    public Keymanager(String credentialFileName) {
        this.credentialFile = Paths.get(credentialFileName);
        loadCredentials();    
    }
    
    
    public byte[] deriveKey(char[] pwd, byte[] salt){
        
        PBEKeySpec spec = new PBEKeySpec(pwd, salt, ITERATIONS, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                                   .generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Key derivation failed", e);
        } finally {
            spec.clearPassword();  // wipe the password copy from memory
        }
    }
    
    //return true if the key macthed the stores hash
    public boolean verifyKey(byte[] hash){
        return storedHash != null && MessageDigest.isEqual(hash, storedHash);
    }
    
    //return true if the master password has already been created
    public boolean hasCredentials(){
        return storedSalt != null && storedHash != null;
    }
    
    public byte[] getStoredSalt(){
        return storedSalt == null ? null : storedSalt.clone();
    }
    
    public byte[] generateSalt(){
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    
    
    
    public void registerMasterPassword(char[] pwd) throws IOException{
        byte[] salt = generateSalt();
        byte[] hash = deriveKey(pwd, salt);
        saveCredentials(salt, hash);
    }
    
    public boolean authenticate(char[] pwd){
        if (!hasCredentials()) {
            return false;
        }
        return verifyKey(deriveKey(pwd, storedSalt));
    }
      
    private void saveCredentials(byte[] salt, byte[] hash) throws IOException {
        String content = Base64.getEncoder().encodeToString(salt) + System.lineSeparator()
                       + Base64.getEncoder().encodeToString(hash) + System.lineSeparator();
        Files.write(credentialFile, content.getBytes(StandardCharsets.UTF_8));
        this.storedSalt = salt;
        this.storedHash = hash;
    }
    
    private void loadCredentials() {
        try {
            if (Files.exists(credentialFile)) {
                List<String> lines = Files.readAllLines(credentialFile, StandardCharsets.UTF_8);
                if (lines.size() >= 2) {
                    storedSalt = Base64.getDecoder().decode(lines.get(0).trim());
                    storedHash = Base64.getDecoder().decode(lines.get(1).trim());
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            storedSalt = null;
            storedHash = null;
        }
    }
    
    
}
