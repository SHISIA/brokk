package io.github.jbellis.brokk.diffTool.ui;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class BrokkDiffPanel extends JPanel implements PropertyChangeListener {
    private final JTabbedPane tabbedPane;
    private boolean started;
    private final JLabel loadingLabel = new JLabel("Processing... Please wait.");
    private final String contentLeft;
    private final String contentRight;
    private boolean isFileComparison;
    private final File leftFile;
    private final File rightFile;
    private final String contentLeftTitle;
    private final String contentRightTitle;
    private FileComparison fileComparison;
    
    public boolean isFileComparison() {
        return isFileComparison;
    }

    public void setIsFileComparison(boolean isFileComparison) {
        this.isFileComparison = isFileComparison;
    }


    /**
     * Constructor for BrokkDiffPanel, a panel designed to compare either text content or files.
     *
     * @param isFileComparison  Determines whether the comparison is file-based (true) or text-based (false).
     * @param contentLeftTitle  Title for the left content area (used in text comparison mode).
     * @param contentRightTitle Title for the right content area (used in text comparison mode).
     * @param contentLeft       The actual content for the left side (used in text comparison mode).
     * @param contentRight      The actual content for the right side (used in text comparison mode).
     * @param leftFile          The file to be used on the left side (used in file comparison mode).
     * @param rightFile         The file to be used on the right side (used in file comparison mode).
     */
    public BrokkDiffPanel(boolean isFileComparison,
                          String contentLeftTitle, String contentRightTitle,
                          String contentLeft, String contentRight,
                          File leftFile, File rightFile) {
        this.contentLeftTitle = contentLeftTitle;
        this.contentRightTitle = contentRightTitle;
        this.contentLeft = contentLeft;
        this.contentRight = contentRight;
        this.leftFile = leftFile;
        this.rightFile = rightFile;
        setIsFileComparison(isFileComparison && leftFile!=null && rightFile!=null);
        // Make the container focusable, so it can handle key events
        setFocusable(true);
        tabbedPane = new JTabbedPane();
        // Add an AncestorListener to trigger 'start()' when the panel is added to a container
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                start();
            }
            public void ancestorMoved(AncestorEvent event) {}

            public void ancestorRemoved(AncestorEvent event) {}
        });

        revalidate();
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    private void start() {
        if (started) {
            return;
        }
        started = true;
        getTabbedPane().setFocusable(false);
        setLayout(new BorderLayout());
        launchComparison();

        add(createToolbar(), BorderLayout.NORTH);
        add(getTabbedPane(), BorderLayout.CENTER);
    }

    public JButton getBtnUndo() {
        return btnUndo;
    }

    private JButton btnUndo;

    public JButton getBtnRedo() {
        return btnRedo;
    }

    private JButton btnRedo;

    private JToolBar createToolbar() {
        // Create toolbar
        JToolBar toolBar = new JToolBar();

        // Create buttons
        JButton btnNext = new JButton("Next Change");
        JButton btnPrevious = new JButton("Previous Change");
        btnUndo = new JButton("Undo");
        btnRedo = new JButton("Redo");

        btnUndo.addActionListener(e -> getCurrentContentPanel().doUndo());

        btnRedo.addActionListener(e -> getCurrentContentPanel().doRedo());

        btnNext.addActionListener(e -> {
            getCurrentContentPanel().doDown();
            repaint();
        });
        btnPrevious.addActionListener(e -> {
            getCurrentContentPanel().doUp();
            repaint();
        });
        btnUndo.addActionListener(e -> {
            getCurrentContentPanel().doUndo();
        });
        btnRedo.addActionListener(e -> {
            getCurrentContentPanel().doRedo();
        });
        // Add buttons to toolbar with spacing
        toolBar.add(btnPrevious);
        toolBar.add(Box.createHorizontalStrut(10)); // 10px spacing
        toolBar.add(btnNext);
        toolBar.add(Box.createHorizontalStrut(20)); // 20px spacing
        toolBar.addSeparator(); // Adds space between groups
        toolBar.add(Box.createHorizontalStrut(10)); // 10px spacing
        toolBar.add(btnUndo);
        toolBar.add(Box.createHorizontalStrut(10)); // 10px spacing
        toolBar.add(btnRedo);

        return toolBar;
    }

    public void updateUndoRedoButtons() {
        if(getCurrentContentPanel()!=null) {
            boolean canUndo = getCurrentContentPanel().isUndoEnabled();
            boolean canRedo = getCurrentContentPanel().isRedoEnabled();
            getBtnUndo().setEnabled(canUndo);
            getBtnRedo().setEnabled(canRedo);
        }
    }

    public void launchComparison() {
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD));
        add(loadingLabel, BorderLayout.SOUTH);
        revalidate();
        repaint();
        compare(); // Pass the stored parameters to compare()

    }

    private void compare() {
        fileComparison=new FileComparison(this,
                leftFile,
                rightFile,
                contentLeftTitle,contentRightTitle,
                contentLeft,contentRight);

        fileComparison.addPropertyChangeListener(this);
        fileComparison.execute();
    }

    public AbstractContentPanel getCurrentContentPanel() {
        return (AbstractContentPanel) getTabbedPane().getSelectedComponent();
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName()) &&
                SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
            try {
                String result = (String) ((SwingWorker<?, ?>) evt.getSource()).get();
                if (result != null) {
                    compare(); // Ensure compare() gets the correct parameters
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                remove(loadingLabel);
                revalidate();
                repaint();
            }
        }
    }

}
