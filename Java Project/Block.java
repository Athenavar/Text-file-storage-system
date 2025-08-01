import java.security.MessageDigest;
import java.util.Date;

public class Block {
    private int index;
    private long timestamp; // Make sure this is still private
    private String fileName;
    private String filePath;
    private String previousHash;
    private String hash;
    private int nonce;

    public Block(int index, String fileName, String filePath, String previousHash) {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.fileName = fileName;
        this.filePath = filePath;
        this.previousHash = previousHash;
        this.hash = calculateHash();
        this.nonce = 0;
    }

    public String calculateHash() {
        String input = index + Long.toString(timestamp) + fileName + previousHash + nonce;
        return applySha256(input);
    }

    public void mineBlock(int difficulty) {
        String target = "0".repeat(difficulty); // Target hash pattern
        while (!hash.startsWith(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined: " + hash);
    }

    private String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }


    public int getIndex() { return index; }
    public String getHash() { return hash; }
    public String getPreviousHash() { return previousHash; }
    public String getFileName() { return fileName; }
    public String getFilePath() {
        return filePath;
    }
    public int getNonce() { return nonce; }
    public long getTimestamp() { return timestamp; } // Getter for timestamp
}