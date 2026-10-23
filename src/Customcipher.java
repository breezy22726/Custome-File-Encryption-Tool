
import java.security.MessageDigest;                        //Imports class that provides applications with the functionality of cryptographically secure, one-way hash function.
import java.security.NoSuchAlgorithmException;             //Imports exception that is thrown when a requested cryptographic algorithm is not available in the current environment.


public class Customcipher implements Encryptionalgorithm{   //Customcipher class adopts the EncryptionAlgorithm interface.
    
    private static final int BLOCK_SIZE = 32;
    
    @Override 
    public byte[] encrypt(byte[] data, byte[] key) {
        return applyKeystream(data, key);                   
    }
    
    @Override
    public byte[] decrypt(byte[] data, byte[] key){
        return applyKeystream(data, key);
    }
    
    private byte[] applyKeystream(byte[] data, byte[] key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] out = new byte[data.length];
            byte[] block = new byte[BLOCK_SIZE];
 
            for (int i = 0; i < data.length; i++) {
                if (i % BLOCK_SIZE == 0) {          // time for a fresh keystream block
                    int counter = i / BLOCK_SIZE;
                    sha.update(key);
                    sha.update(new byte[] {
                        (byte) (counter >>> 24), (byte) (counter >>> 16),
                        (byte) (counter >>> 8),  (byte) counter });
                    block = sha.digest();           // digest() also resets the hash
                }
                out[i] = (byte) (data[i] ^ block[i % BLOCK_SIZE]);
            }
            return out;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this JVM", e);
        }
    }
    
}
