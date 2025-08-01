import java.io.Serializable;
import java.util.ArrayList;

public class Blockchain implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private ArrayList<Block> chain;
    private int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block(0, "Genesis Block", "GENESIS_PATH", "0");
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(String fileName, String filePath) {
        Block newBlock = new Block(chain.size(), fileName, filePath, getLatestBlock().getHash());
        System.out.println("Mining block " + newBlock.getIndex() + "...");
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.err.println("Invalid hash detected in block " + currentBlock.getIndex());
                return false;
            }
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.err.println("Invalid previous hash link between blocks " + previousBlock.getIndex() + " and " + currentBlock.getIndex());
                return false;
            }
            // Check if timestamps are consistent
            if (currentBlock.getTimestamp() < previousBlock.getTimestamp()) {
                System.err.println("Timestamp inconsistency between blocks " + previousBlock.getIndex() + " and " + currentBlock.getIndex());
                return false;
            }
        }
        return true;
    }

    public boolean detectTampering() {
        boolean valid = isChainValid();

        if (!valid) {
            System.err.println("Blockchain tampering detected!");
        } else {
            System.out.println("Blockchain is valid and free of tampering.");
        }

        return valid;
    }

    public ArrayList<Block> getChain() {
        return chain;
    }
}