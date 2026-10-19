
public interface Encryptionalgorithm {
    
    
    
    byte[] encrypt(byte[] data, byte[] key);
    
    byte[] decrypt(byte[] data, byte[] key);
}
