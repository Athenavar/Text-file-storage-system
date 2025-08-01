import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import javax.swing.*;

public class BlockSafeApp {
    private static JFrame frame;

    private static final String BLOCKCHAIN_FILENAME = "Report.txt"; // Define file name as constant
    private static Blockchain blockchain = FileStorage.loadBlockchainFromFile(BLOCKCHAIN_FILENAME); // Load blockchain on startup

    public static void main(String[] args) {
        // Create the main frame
        frame = new JFrame("Block Safe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(null);

        // Create the "BLOCK SAFE" title label
        JLabel titleLabel = new JLabel("BLOCK SAFE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(titleLabel);

        // Create the "DRAG/UPLOAD" panel
        JPanel uploadPanel = new JPanel();
        uploadPanel.setBorder(BorderFactory.createDashedBorder(Color.BLACK, 2, 5));
        uploadPanel.setLayout(new GridBagLayout());
        frame.add(uploadPanel);

        JLabel dragLabel = new JLabel("DRAG/UPLOAD", SwingConstants.CENTER);
        dragLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton uploadButton = new JButton("UPLOAD");

        // Use GridBagConstraints to center components inside uploadPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add some padding
        gbc.gridx = 0; // Single column
        gbc.gridy = 0; // First row
        gbc.anchor = GridBagConstraints.CENTER; // Center the label
        uploadPanel.add(dragLabel, gbc);

        gbc.gridy = 1; // Move to the second row for the button
        uploadPanel.add(uploadButton, gbc);

        // Enable drag-and-drop functionality
        enableDragAndDrop(uploadPanel);

        // Create the "VIEW BLOCKCHAIN" button
        JButton viewBlockchainButton = new JButton("VIEW BLOCKCHAIN");
        frame.add(viewBlockchainButton);

        uploadButton.addActionListener(e -> uploadFile());
        viewBlockchainButton.addActionListener(e -> showBlockchainPage(frame));

        // Add a custom JPanel to manage layout updates
        JPanel contentPane = new JPanel(null) {
            @Override
            public void doLayout() {
                int frameWidth = getWidth();
                int frameHeight = getHeight();

                // Reposition and resize title
                titleLabel.setBounds(frameWidth / 2 - 100, 20, 200, 40);

                // Resize and reposition the "DRAG/UPLOAD" panel
                int uploadPanelWidth = frameWidth / 3;
                int uploadPanelHeight = 2 * frameHeight / 3;
                uploadPanel.setBounds(frameWidth / 12, frameHeight / 6, uploadPanelWidth, uploadPanelHeight);

                // Resize and reposition the "VIEW BLOCKCHAIN" button
                int buttonWidth = 200;
                int buttonHeight = 50;
                viewBlockchainButton.setBounds(3 * frameWidth / 4 - buttonWidth / 2, frameHeight / 2 - buttonHeight / 2, buttonWidth, buttonHeight);

                super.doLayout(); // Perform default layout operations
            }
        };

        // Set contentPane for dynamic resizing
        frame.setContentPane(contentPane);
        contentPane.add(titleLabel);
        contentPane.add(uploadPanel);
        contentPane.add(viewBlockchainButton);

        frame.setVisible(true);
    }
    private static void enableDragAndDrop(JPanel panel) {
        new DropTarget(panel, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                panel.setBorder(BorderFactory.createDashedBorder(Color.BLACK, 2, 5));
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}
            @SuppressWarnings("unchecked")
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    DataFlavor[] flavors = transferable.getTransferDataFlavors();

                    for (DataFlavor flavor : flavors) {
                        if (flavor.isFlavorJavaFileListType()) {
                            java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(flavor);
                            for (File file : files) {
                                processFile(file);
                            }
                            break;
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error processing dropped file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    panel.setBorder(BorderFactory.createDashedBorder(Color.BLACK, 2, 5));
                }
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}
        });
    }
    private static void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            processFile(file);
        }
    }

    private static void processFile(File file){
        try {
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();

            // Check for duplicates in the blockchain
            for (Block block : blockchain.getChain()) {
                if (block.getFileName().equals(fileName)) {
                    JOptionPane.showMessageDialog(frame, 
                            "File '" + fileName + "' has already been uploaded and recorded in the blockchain.",
                            "Duplicate File Detected", JOptionPane.WARNING_MESSAGE);
                    return; // Exit the method to prevent duplicate entry
                }
            }

            // Add to blockchain
            Block newBlock = new Block(blockchain.getChain().size(), fileName, filePath, blockchain.getLatestBlock().getHash());
            blockchain.addBlock(fileName, filePath);

            // Append the block to the file
            FileStorage.appendBlockToFile(newBlock, BLOCKCHAIN_FILENAME);

            JOptionPane.showMessageDialog(frame, "File metadata stored in blockchain: " + fileName);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error processing file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private static void showBlockchainPage(JFrame parentFrame) {
        JFrame blockchainFrame = new JFrame("Blockchain Files");
        blockchainFrame.setSize(800, 600);
        blockchainFrame.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("BLOCK SAFE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        blockchainFrame.add(titleLabel, BorderLayout.NORTH);

        // Create a panel to hold the buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // "Blockchain Report" button
        JButton reportButton = new JButton("Blockchain Report");
        reportButton.addActionListener(e -> showBlockchainReport());

        topPanel.add(reportButton);
        blockchainFrame.add(topPanel, BorderLayout.NORTH);

        JPanel fileListPanel = new JPanel();
        fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));

        // Retrieve data from the blockchain and populate UI
        for (Block block : blockchain.getChain()) {
            if (block.getIndex() == 0) continue; // Skip Genesis Block

            // Display file name from block data
            String fileName = block.getFileName();

            JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel fileNameLabel = new JLabel(fileName);
            JButton viewButton = new JButton("View");
            JButton verifyButton = new JButton("Verify Integrity");

            // Add action listeners
            viewButton.addActionListener(e -> showFileContent(new File(block.getFilePath()), blockchainFrame));
            verifyButton.addActionListener(e -> verifyFileIntegrity(new File(fileName), blockchainFrame));

            // Add components to the panel
            filePanel.add(fileNameLabel);
            filePanel.add(viewButton);
            filePanel.add(verifyButton);

            fileListPanel.add(filePanel);
        }

        JScrollPane scrollPane = new JScrollPane(fileListPanel);
        blockchainFrame.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            blockchainFrame.dispose();
            parentFrame.setVisible(true);
        });

        blockchainFrame.add(backButton, BorderLayout.SOUTH);

        blockchainFrame.setVisible(true);
        parentFrame.setVisible(false);
    }

    private static void showFileContent(File file, JFrame blockchainFrame) {
        JFrame fileViewerFrame = new JFrame("File Content - " + file.getName());
        fileViewerFrame.setSize(800, 600);
        fileViewerFrame.setLayout(new BorderLayout());

        JTextArea fileContentArea = new JTextArea();
        fileContentArea.setEditable(false);

        // Read the text file content and display it
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            fileContentArea.setText(content.toString());
        } catch (IOException ex) {
            fileContentArea.setText("Error reading file: " + ex.getMessage());
        }

        fileViewerFrame.add(new JScrollPane(fileContentArea), BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            fileViewerFrame.dispose();
            blockchainFrame.setVisible(true);
        });

        fileViewerFrame.add(backButton, BorderLayout.SOUTH);
        fileViewerFrame.setVisible(true);
        blockchainFrame.setVisible(false);
    }


    private static void verifyFileIntegrity(File file, JFrame blockchainFrame) {
        boolean isTampered = !blockchain.detectTampering();

        if (isTampered) {
            JOptionPane.showMessageDialog(blockchainFrame, 
                "Blockchain tampering detected! File integrity may be compromised.", 
                "Integrity Check", 
                JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(blockchainFrame, 
                "Blockchain is valid. No tampering detected.", 
                "Integrity Check", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void showBlockchainReport() {
        JFrame reportFrame = new JFrame("Blockchain Report");
        reportFrame.setSize(800, 600);
        reportFrame.setLayout(new BorderLayout());

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);

        try (BufferedReader reader = new BufferedReader(new FileReader(BLOCKCHAIN_FILENAME))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reportArea.setText(content.toString());
        } catch (IOException e) {
            reportArea.setText("Error reading blockchain file: " + e.getMessage());
        }

        reportFrame.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> reportFrame.dispose());
        reportFrame.add(closeButton, BorderLayout.SOUTH);

        reportFrame.setVisible(true);
    }
}