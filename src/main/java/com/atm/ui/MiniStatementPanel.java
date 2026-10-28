package com.atm.ui;

import com.atm.model.ATMCard;
import com.atm.model.Transaction;
import com.atm.service.TransactionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Mini Statement screen — shows recent transactions in a styled table.
 * Only displays transactions belonging to the authenticated account.
 */
public class MiniStatementPanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final TransactionService txnService = new TransactionService();
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JTable table;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String[] COLUMNS = {"Date", "Type", "Amount", "Balance After", "Status", "Reference"};

    public MiniStatementPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 40, 20, 40));
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = ATMTheme.createGradientHeader("Mini Statement", "Your recent transaction history");
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ATMTheme.BG_SECONDARY);
        scrollPane.setBackground(ATMTheme.BG_SECONDARY);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(12, 0, 0, 0));

        statusLabel = ATMTheme.createLabel("", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        bottomBar.add(statusLabel, BorderLayout.WEST);

        JButton backBtn = ATMTheme.createSecondaryButton("← Back to Menu");
        backBtn.setPreferredSize(new Dimension(180, 38));
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_MAIN_MENU));
        bottomBar.add(backBtn, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void styleTable(JTable table) {
        table.setBackground(ATMTheme.BG_SECONDARY);
        table.setForeground(ATMTheme.TEXT_PRIMARY);
        table.setFont(ATMTheme.FONT_BODY);
        table.setRowHeight(40);
        table.setGridColor(ATMTheme.BORDER_DEFAULT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(ATMTheme.BG_CARD_HOVER);
        table.setSelectionForeground(ATMTheme.TEXT_PRIMARY);

        // Header styling
        JTableHeader headerComp = table.getTableHeader();
        headerComp.setBackground(ATMTheme.BG_CARD);
        headerComp.setForeground(ATMTheme.TEXT_SECONDARY);
        headerComp.setFont(ATMTheme.FONT_BODY_BOLD);
        headerComp.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ATMTheme.ACCENT_PRIMARY));
        headerComp.setPreferredSize(new Dimension(headerComp.getPreferredSize().width, 44));

        // Custom cell renderer for colored amounts
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                label.setBackground(sel ? ATMTheme.BG_CARD_HOVER : ATMTheme.BG_SECONDARY);
                String text = value != null ? value.toString() : "";
                if (text.startsWith("+")) {
                    label.setForeground(ATMTheme.SUCCESS);
                } else if (text.startsWith("-")) {
                    label.setForeground(ATMTheme.DANGER);
                } else {
                    label.setForeground(ATMTheme.TEXT_PRIMARY);
                }
                label.setFont(ATMTheme.FONT_MONO);
                return label;
            }
        });

        // Default renderer for other columns
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                label.setBackground(sel ? ATMTheme.BG_CARD_HOVER : ATMTheme.BG_SECONDARY);
                label.setForeground(ATMTheme.TEXT_PRIMARY);
                label.setBorder(new EmptyBorder(0, 8, 0, 8));
                return label;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 2) table.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
        }

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(140);  // Date
        table.getColumnModel().getColumn(1).setPreferredWidth(130);  // Type
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Amount
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Balance After
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Status
        table.getColumnModel().getColumn(5).setPreferredWidth(250);  // Reference
    }

    @Override
    public void onShow() {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;

        tableModel.setRowCount(0);
        statusLabel.setText("Loading transactions...");

        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                return txnService.getMiniStatement(card.getAccountId(), 20);
            }

            @Override
            protected void done() {
                try {
                    List<Transaction> txns = get();
                    tableModel.setRowCount(0);

                    for (Transaction txn : txns) {
                        tableModel.addRow(new Object[]{
                            txn.getTransactionDate().format(DATE_FMT),
                            txn.getTypeDisplay(),
                            txn.getAmountDisplay(),
                            "₹" + txn.getBalanceAfter().toPlainString(),
                            txn.getStatus().name(),
                            txn.getTransactionRef() != null ? txn.getTransactionRef().substring(0, 8) + "..." : ""
                        });
                    }

                    statusLabel.setText("Showing " + txns.size() + " recent transactions");
                    statusLabel.setForeground(ATMTheme.TEXT_SECONDARY);

                } catch (Exception ex) {
                    statusLabel.setText("Error loading transactions.");
                    statusLabel.setForeground(ATMTheme.DANGER);
                }
            }
        };
        worker.execute();
    }
}
