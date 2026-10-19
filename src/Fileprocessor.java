import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;



public class Fileprocessor {
    
    //this is an extention of the encryption file
    private static final String ENC_EXT = ".enc";
    
    
    //length of the integrity checksum
    private static final int CHECKSUM_LENTH = 32;
    
    private static final boolean DELETE__SOURCE_AFTER_SUCCESS = true;
    
    private final Encryptionalgorithm algorithm;
    private final Folderscanner scanner;
    private final Keymanager keyManager;
    private final Logger logger;
    
    
    public Fileprocessor(Encryptionalgorithm algorithm, Folderscanner scanner, Keymanager keyManager, Logger logger){
        
        this.algorithm = algorithm;
        this.scanner = scanner;
        this.keyManager = keyManager;
        this.logger = logger;
    }
    
    public int encryptFolder(Path dir, char[] pwd) throws IOException {
        requireFolder(dir);
        scanner.scanRecursive(dir);
        int done = 0, failed = 0;
        
        for(Path file : scanner.getFileList()){
            if (file.getFileName().toString().endsWith(ENC_EXT)){
                continue;
            }
            
            try{
                encryptFile(file, pwd);
                done++;
            } catch (Exception e){
                failed++;
                logger.logError(e);
            }
        }
        logger.log("Encrypt folder '" + dir + ": " + done + "succeeded, " + failed + "failed");
        return done;
    }
    
    
    public int decryptFolder(Path dir, char[] pwd) throws IOException {
        requireFolder(dir);
        scanner.scanRecursive(dir);
        int done = 0, failed = 0;
        
        for (Path file : scanner.getFileList()){
            
            if(!file.getFileName().toString().endsWith(ENC_EXT)){
                continue;
            }
            try {
                decryptFile(file, pwd);
                done++;
            } catch (Exception e){
                failed++;
                logger.logError(e);
            }
        }
        
        logger.log("Decrypt folder "+ dir + ": " +done+ " succeeded," +failed+ "failed");
        return done;
    }
    
    
    
    private void encryptFile(Path path, char[] pwd) throws IOException {
        
         byte[] plain = Files.readAllBytes(path);
         byte[] payload =concat(plain, sha256(plain));
         byte[] salt = keyManager.generateSalt();
         byte[] key = keyManager.deriveKey(pwd, salt);
         
         try {
             byte[] cipher = algorithm.encrypt(payload, key);
             Path target = path.resolveSibling(path.getFileName() + ENC_EXT);
             Files.write(target, concat(salt, cipher));
             if(DELETE__SOURCE_AFTER_SUCCESS) {
                 
                 Files.delete(path);
             }
             logger.log("Encrypted: " + path + "(" + plain.length + " bytes)");
         }  finally{
             Arrays.fill(key, (byte) 0);  //this wipes the key from memory
         }
    }
    
    private void decryptFile(Path path, char[] pwd) throws IOException {
        byte[] all = Files.readAllBytes(path);
        if (all.length < Keymanager.SALT_LENGTH + CHECKSUM_LENTH ) {
            throw new IOException("Not a valid encrypted file: " + path);
        }
        byte[] salt = Arrays.copyOfRange(all, 0, Keymanager.SALT_LENGTH);
        byte[] cipher = Arrays.copyOfRange(all, Keymanager.SALT_LENGTH, all.length);
        byte[] key = keyManager.deriveKey(pwd, salt);
        try {
            byte[] payload = algorithm.decrypt(cipher, key);
            byte[] plain = Arrays.copyOfRange(payload, 0, payload.length - CHECKSUM_LENTH );
            byte[] storedSum = Arrays.copyOfRange(payload, payload.length - CHECKSUM_LENTH , payload.length);
 
            if (!MessageDigest.isEqual(sha256(plain), storedSum)) {    // integrity check
                throw new IOException("Wrong password or corrupted file: " + path);
            }
            String name = path.getFileName().toString();
            Path target = path.resolveSibling(name.substring(0, name.length() - ENC_EXT.length()));
            Files.write(target, plain);
            if (DELETE__SOURCE_AFTER_SUCCESS) {
                Files.delete(path);
            }
            logger.log("Decrypted: " + path + " (" + plain.length + " bytes)");
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }
    
    private void requireFolder(Path dir) throws IOException{
        
        if (!Files.isDirectory(dir)) {
            throw new IOException("Folder not found: " + dir);
        }
    }
    
    
    
    
    private byte[] sha256(byte[] data){
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this JVM", e);
        }
    }
    
    private byte[] concat(byte[] a, byte[] b) {
        byte[] out = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
