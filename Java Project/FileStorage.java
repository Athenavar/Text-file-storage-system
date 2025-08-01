import java.io.*;
import java.util.ArrayList;

public class FileStorage {

    /**
     * Saves the blockchain to a file in plain-text format.
     * 
     * @param blockchain The blockchain to save.
     * @param filename   The name of the file to save to.
     */
    public static void appendBlockToFile(Block block, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) { // Open in append mode
            writer.println(serializeBlock(block));
            System.out.println("Block successfully appended to file.");
        } catch (IOException e) {
            System.err.println("Error appending block to file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Loads the blockchain from a plain-text file.
     * 
     * @param filename The name of the file to load from.
     * @return The loaded blockchain, or a new empty blockchain if loading fails.
     */
    public static Blockchain loadBlockchainFromFile(String filename) {
        File file = new File(filename);
        Blockchain blockchain = new Blockchain(4); // Default difficulty is 4

        if (!file.exists()) {
            System.out.println("Creating a new blockchain.");
            // Write the Genesis Block to the file
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                Block genesisBlock = blockchain.getChain().get(0); // Retrieve the genesis block
                writer.println(serializeBlock(genesisBlock));
                System.out.println("Genesis Block written to file.");
            } catch (IOException e) {
                System.err.println("Error creating blockchain file: " + e.getMessage());
                e.printStackTrace();
            }
            return blockchain;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            ArrayList<Block> chain = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                chain.add(deserializeBlock(line));
            }
            blockchain.getChain().clear();
            blockchain.getChain().addAll(chain);
            System.out.println("Blockchain successfully loaded from file.");
        } catch (IOException e) {
            System.err.println("Error loading blockchain from file: " + e.getMessage());
            e.printStackTrace();
        }

        return blockchain;
    }

    private static String serializeBlock(Block block) {
        return block.getIndex() + "," +
               block.getTimestamp() + "," +
               block.getFileName() + "," +
               block.getFilePath() + "," +
               block.getPreviousHash() + "," +
               block.getHash() + "," +
               block.getNonce();
    }

    private static Block deserializeBlock(String blockData) {
        String[] fields = blockData.split(",");
        int index = Integer.parseInt(fields[0]);
        long timestamp = Long.parseLong(fields[1]);
        String fileName = fields[2];
        String filePath = fields[3];
        String previousHash = fields[4];
        String hash = fields[5];
        int nonce = Integer.parseInt(fields[6]);

        Block block = new Block(index, fileName, filePath, previousHash);
        // Overwrite the calculated hash and nonce to match saved data
        block.setHash(hash);
        block.setNonce(nonce);
        return block;
    }
}