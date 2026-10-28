package com.atm.ui;

import com.atm.model.*;
import com.atm.service.AdminService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin Panel — dashboard for administrative functions.
 * Includes tabs for Customers, Accounts, Cards, Transactions.
 * Requires admin authentication — not accessible to ATM users.
 */
public class AdminPanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final AdminService adminService = new AdminService();
    private JTabbedPane tabbedPane;
    private JLabel welcomeLabel;

    private DefaultTableModel customerTableModel;
    private DefaultTableModel accountTableModel;
    private DefaultTableModel cardTableModel;
    private DefaultTableModel txnTableModel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 30, 15, 30));
        buildUI();
    }

    private void buildUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(10, 0, 15, 0));

        welcomeLabel = ATMTheme.createLabel("Admin Dashboard", ATMTheme.FONT_HEADING, ATMTheme.TEXT_PRIMARY);
        topBar.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutBtn = ATMTheme.createDangerButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(120, 38));
        logoutBtn.addActionListener(e -> mainFrame.logout());
        topBar.add(logoutBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(ATMTheme.BG_SECONDARY);
        tabbedPane.setForeground(ATMTheme.TEXT_PRIMARY);
        tabbedPane.setFont(ATMTheme.FONT_BODY_BOLD);

        tabbedPane.addTab("👥 Customers", createCustomerTab());
        tabbedPane.addTab("🏦 Accounts", createAccountTab());
        tabbedPane.addTab("💳 Cards", createCardTab());
        tabbedPane.addTab("📊 Transactions", createTransactionTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========== CUSTOMERS TAB ===========
    private JPanel createCustomerTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ATMTheme.BG_SECONDARY);

        String[] cols = {"ID", "Name", "Email", "Phone", "City", "State"};
        customerTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(customerTableModel);
        styleAdminTable(table);

        panel.add(new JScrollPane(table) {{
            setBorder(BorderFactory.createEmptyBorder());
            getViewport().setBackground(ATMTheme.BG_SECONDARY);
        }}, BorderLayout.CENTER);

        JPanel toolbar = createToolbar();
        JButton refreshBtn = ATMTheme.createSecondaryButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> loadCustomers());
        toolbar.add(refreshBtn);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    // =========== ACCOUNTS TAB ===========
    private JPanel createAccountTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ATMTheme.BG_SECONDARY);

        String[] cols = {"Account No", "Customer", "Type", "Balance", "Min Balance", "Status"};
        accountTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(accountTableModel);
        styleAdminTable(table);

        panel.add(new JScrollPane(table) {{
            setBorder(BorderFactory.createEmptyBorder());
            getViewport().setBackground(ATMTheme.BG_SECONDARY);
        }}, BorderLayout.CENTER);

        JPanel toolbar = createToolbar();
        JButton refreshBtn = ATMTheme.createSecondaryButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> loadAccounts());
        toolbar.add(refreshBtn);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    // =========== CARDS TAB ===========
    private JPanel createCardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ATMTheme.BG_SECONDARY);

        String[] cols = {"Card Number", "Account", "Status", "Failed Attempts", "Expiry", "Actions"};
        cardTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; } // Only actions editable
        };
        JTable table = new JTable(cardTableModel);
        styleAdminTable(table);

        // Status column renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                label.setBackground(sel ? ATMTheme.BG_CARD_HOVER : ATMTheme.BG_SECONDARY);
                String status = value != null ? value.toString() : "";
                label.setForeground(switch (status) {
                    case "ACTIVE" -> ATMTheme.SUCCESS;
                    case "BLOCKED" -> ATMTheme.DANGER;
                    case "EXPIRED" -> ATMTheme.WARNING;
                    default -> ATMTheme.TEXT_PRIMARY;
                });
                label.setFont(ATMTheme.FONT_BODY_BOLD);
                return label;
            }
        });

        panel.add(new JScrollPane(table) {{
            setBorder(BorderFactory.createEmptyBorder());
            getViewport().setBackground(ATMTheme.BG_SECONDARY);
        }}, BorderLayout.CENTER);

        JPanel toolbar = createToolbar();

        JButton blockBtn = ATMTheme.createDangerButton("Block Selected");
        blockBtn.setPreferredSize(new Dimension(140, 34));
        blockBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int cardId = (int) table.getValueAt(row, 0);
                String cardNum = table.getValueAt(row, 0).toString();
                if (ATMTheme.showConfirm(this, "Block card " + cardNum + "?")) {
                    try {
                        // We need the card_id — for simplicity, load from the card number
                        adminService.getAllCards().stream()
                            .filter(c -> c.getCardNumber().equals(cardNum))
                            .findFirst()
                            .ifPresent(c -> {
                                try { adminService.blockCard(c.getCardId()); loadCards(); }
                                catch (Exception ex) { ATMTheme.showError(this, "Failed: " + ex.getMessage()); }
                            });
                    } catch (Exception ex) {
                        ATMTheme.showError(this, "Error: " + ex.getMessage());
                    }
                }
            }
        });
        toolbar.add(blockBtn);

        JButton activateBtn = ATMTheme.createPrimaryButton("Activate Selected");
        activateBtn.setPreferredSize(new Dimension(160, 34));
        activateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String cardNum = table.getValueAt(row, 0).toString();
                try {
                    adminService.getAllCards().stream()
                        .filter(c -> c.getCardNumber().equals(cardNum))
                        .findFirst()
                        .ifPresent(c -> {
                            try { adminService.activateCard(c.getCardId()); loadCards(); }
                            catch (Exception ex) { ATMTheme.showError(this, "Failed: " + ex.getMessage()); }
                        });
                } catch (Exception ex) {
                    ATMTheme.showError(this, "Error: " + ex.getMessage());
                }
            }
        });
        toolbar.add(activateBtn);

        JButton refreshBtn = ATMTheme.createSecondaryButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> loadCards());
        toolbar.add(refreshBtn);

        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    // =========== TRANSACTIONS TAB ===========
    private JPanel createTransactionTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ATMTheme.BG_SECONDARY);

        String[] cols = {"Date", "Account", "Type", "Amount", "Balance After", "Status", "Ref"};
        txnTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(txnTableModel);
        styleAdminTable(table);

        panel.add(new JScrollPane(table) {{
            setBorder(BorderFactory.createEmptyBorder());
            getViewport().setBackground(ATMTheme.BG_SECONDARY);
        }}, BorderLayout.CENTER);

        JPanel toolbar = createToolbar();
        JButton refreshBtn = ATMTheme.createSecondaryButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> loadTransactions());
        toolbar.add(refreshBtn);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        toolbar.setOpaque(false);
        return toolbar;
    }

    private void styleAdminTable(JTable table) {
        table.setBackground(ATMTheme.BG_SECONDARY);
        table.setForeground(ATMTheme.TEXT_PRIMARY);
        table.setFont(ATMTheme.FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(ATMTheme.BORDER_DEFAULT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(ATMTheme.BG_CARD_HOVER);
        table.setSelectionForeground(ATMTheme.TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(ATMTheme.BG_CARD);
        header.setForeground(ATMTheme.TEXT_SECONDARY);
        header.setFont(ATMTheme.FONT_BODY_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ATMTheme.WARNING));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

        // Default cell renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                label.setBackground(sel ? ATMTheme.BG_CARD_HOVER : ATMTheme.BG_SECONDARY);
                label.setForeground(ATMTheme.TEXT_PRIMARY);
                label.setBorder(new EmptyBorder(0, 8, 0, 8));
                return label;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    // =========== DATA LOADING ===========
    private void loadCustomers() {
        SwingWorker<List<Customer>, Void> w = new SwingWorker<>() {
            @Override protected List<Customer> doInBackground() throws Exception {
                return adminService.getAllCustomers();
            }
            @Override protected void done() {
                try {
                    customerTableModel.setRowCount(0);
                    for (Customer c : get()) {
                        customerTableModel.addRow(new Object[]{
                            c.getCustomerId(), c.getFullName(), c.getEmail(), c.getPhone(), c.getCity(), c.getState()
                        });
                    }
                } catch (Exception ex) { ATMTheme.showError(AdminPanel.this, "Error loading customers."); }
            }
        };
        w.execute();
    }

    private void loadAccounts() {
        SwingWorker<List<Account>, Void> w = new SwingWorker<>() {
            @Override protected List<Account> doInBackground() throws Exception {
                return adminService.getAllAccounts();
            }
            @Override protected void done() {
                try {
                    accountTableModel.setRowCount(0);
                    for (Account a : get()) {
                        accountTableModel.addRow(new Object[]{
                            a.getAccountNumber(),
                            a.getCustomer() != null ? a.getCustomer().getFullName() : "",
                            a.getAccountType(), "₹" + a.getBalance().toPlainString(),
                            "₹" + a.getMinBalance().toPlainString(), a.getStatus()
                        });
                    }
                } catch (Exception ex) { ATMTheme.showError(AdminPanel.this, "Error loading accounts."); }
            }
        };
        w.execute();
    }

    private void loadCards() {
        SwingWorker<List<ATMCard>, Void> w = new SwingWorker<>() {
            @Override protected List<ATMCard> doInBackground() throws Exception {
                return adminService.getAllCards();
            }
            @Override protected void done() {
                try {
                    cardTableModel.setRowCount(0);
                    for (ATMCard c : get()) {
                        cardTableModel.addRow(new Object[]{
                            c.getCardNumber(),
                            c.getAccount() != null ? c.getAccount().getAccountNumber() : "",
                            c.getCardStatus().name(), c.getFailedAttempts(),
                            c.getExpiryDate().toString(), ""
                        });
                    }
                } catch (Exception ex) { ATMTheme.showError(AdminPanel.this, "Error loading cards."); }
            }
        };
        w.execute();
    }

    private void loadTransactions() {
        SwingWorker<List<Transaction>, Void> w = new SwingWorker<>() {
            @Override protected List<Transaction> doInBackground() throws Exception {
                return adminService.getRecentTransactions(100);
            }
            @Override protected void done() {
                try {
                    txnTableModel.setRowCount(0);
                    for (Transaction t : get()) {
                        txnTableModel.addRow(new Object[]{
                            t.getTransactionDate().format(DATE_FMT),
                            t.getAccountId(), t.getTypeDisplay(),
                            t.getAmountDisplay(), "₹" + t.getBalanceAfter().toPlainString(),
                            t.getStatus().name(),
                            t.getTransactionRef() != null ? t.getTransactionRef().substring(0, 8) + "..." : ""
                        });
                    }
                } catch (Exception ex) { ATMTheme.showError(AdminPanel.this, "Error loading transactions."); }
            }
        };
        w.execute();
    }

    @Override
    public void onShow() {
        Admin admin = mainFrame.getCurrentAdmin();
        if (admin != null) {
            welcomeLabel.setText("Admin Dashboard — " + admin.getFullName());
        }
        loadCustomers();
        loadAccounts();
        loadCards();
        loadTransactions();
    }
}
