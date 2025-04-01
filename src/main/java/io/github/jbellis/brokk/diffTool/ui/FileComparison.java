package io.github.jbellis.brokk.diffTool.ui;

import io.github.jbellis.brokk.diffTool.node.FileNode;
import io.github.jbellis.brokk.diffTool.node.JMDiffNode;
import io.github.jbellis.brokk.diffTool.node.StringNode;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class FileComparison extends SwingWorker<String, Object> {
    private final BrokkDiffPanel mainPanel;
    private JMDiffNode diffNode;
    private File leftFile;
    private File rightFile;
    private BufferDiffPanel panel;
    private final String contentLeft;
    private final String contentRight;
    private final String contentLeftTitle;
    private final String contentRightTitle;
    public FileComparison(BrokkDiffPanel mainPanel, File leftFile, File rightFile,
                          String contentLeftTitle, String contentRightTitle,
                          String contentLeft,String contentRight) {
        this.mainPanel = mainPanel;
        this.leftFile = leftFile;
        this.rightFile = rightFile;
        this.contentLeftTitle = contentLeftTitle;
        this.contentRightTitle = contentRightTitle;
        this.contentLeft = contentLeft;
        this.contentRight = contentRight;
    }


    public BufferDiffPanel getPanel() {
        return panel;
    }

    @Override
    public String doInBackground() {
        try {
            if (diffNode == null) {
                if (mainPanel.isFileComparison() && rightFile != null && leftFile != null) {
                    if (leftFile.getName().isEmpty() || !leftFile.exists()) {
                        leftFile = new File(leftFile.getName());
                    }

                    if (rightFile.getName().isEmpty() || !rightFile.exists()) {
                        rightFile = new File(rightFile.getName());
                    }
                    
                    diffNode = create(leftFile.getName(), leftFile,
                            rightFile.getName(), rightFile);
                }else {
                    diffNode = createString(contentLeftTitle, contentLeft,
                            contentRightTitle, contentRight);
                }
            }
            SwingUtilities.invokeLater(() -> diffNode.diff());
        } catch (Exception ex) {
            ex.printStackTrace();

            return ex.getMessage();
        }

        return null;
    }

    public JMDiffNode create(String fileLeftName, File fileLeft,
                             String fileRightName, File fileRight) {
        JMDiffNode node = new JMDiffNode(fileLeftName, true);
        node.setBufferNodeLeft(new FileNode(fileLeftName, fileLeft));
        node.setBufferNodeRight(new FileNode(fileRightName, fileRight));

        return node;
    }

    public JMDiffNode createString(String fileLeftName, String leftContent,
                                   String fileRightName, String rightContent) {
        JMDiffNode node = new JMDiffNode(fileLeftName, true);
        node.setBufferNodeLeft(new StringNode(fileLeftName,leftContent ));
        node.setBufferNodeRight(new StringNode(fileRightName, rightContent));

        return node;
    }

    private static ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            // Load the image
            BufferedImage originalImage = ImageIO.read(Objects.requireNonNull(FileComparison.class.getResource(path)));

            // Scale the image
            Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            return new ImageIcon(scaledImage);
        } catch (IOException | NullPointerException e) {
            System.err.println("Image not found: " + path);
            return null;
        }
    }

    @Override
    protected void done() {
        try {
            String result = get();
            if (result != null) {
                JOptionPane.showMessageDialog(mainPanel, result, "Error opening file", JOptionPane.ERROR_MESSAGE);
            } else {
                panel = new BufferDiffPanel(mainPanel);
                panel.setDiffNode(diffNode);
                ImageIcon resizedIcon = getScaledIcon("/images/compare.png", 24, 24); // Change size here

                mainPanel.getTabbedPane().addTab(panel.getTitle(), resizedIcon, panel);
                mainPanel.getTabbedPane().setSelectedComponent(panel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
