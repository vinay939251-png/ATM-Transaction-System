package com.atm.ui;

import com.atm.model.ATMCard;
import com.atm.model.Admin;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Main application frame — manages panel navigation using CardLayout.
 * Acts as the central container for all ATM screens.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    // Screen names for CardLayout navigation
    public static final String SCREEN_LOGIN = "LOGIN";
    public static final String SCREEN_SIGNUP = "SIGNUP";
    public static final String SCREEN_MAIN_MENU = "MAIN_MENU";
    public static final String SCREEN_BALANCE = "BALANCE";
    public static final String SCREEN_WITHDRAW = "WITHDRAW";
    public static final String SCREEN_DEPOSIT = "DEPOSIT";
    public static final String SCREEN_TRANSFER = "TRANSFER";
    public static final String SCREEN_MINI_STATEMENT = "MINI_STATEMENT";
    public static final String SCREEN_ADMIN_LOGIN = "ADMIN_LOGIN";
    public static final String SCREEN_ADMIN_PANEL = "ADMIN_PANEL";

    // Current session state
    private ATMCard currentCard;
    private Admin currentAdmin;
    private final LoginPanel loginPanel;
    private final Deque<String> navigationHistory = new ArrayDeque<>();
    private String currentScreen;
    private Point gestureStart;

    public MainFrame() {
        setTitle("ATM Transaction System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ATMTheme.BG_PRIMARY);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBackground(ATMTheme.BG_PRIMARY);

        // Add all screens
        loginPanel = new LoginPanel(this);
        contentPanel.add(loginPanel, SCREEN_LOGIN);
        contentPanel.add(new SignUpPanel(this), SCREEN_SIGNUP);
        contentPanel.add(new MainMenuPanel(this), SCREEN_MAIN_MENU);
        contentPanel.add(new BalancePanel(this), SCREEN_BALANCE);
        contentPanel.add(new WithdrawPanel(this), SCREEN_WITHDRAW);
        contentPanel.add(new DepositPanel(this), SCREEN_DEPOSIT);
        contentPanel.add(new TransferPanel(this), SCREEN_TRANSFER);
        contentPanel.add(new MiniStatementPanel(this), SCREEN_MINI_STATEMENT);
        contentPanel.add(new AdminLoginPanel(this), SCREEN_ADMIN_LOGIN);
        contentPanel.add(new AdminPanel(this), SCREEN_ADMIN_PANEL);

        add(contentPanel);
        installNavigationGestures(contentPanel);
        installKeyboardNavigation();
        showScreen(SCREEN_LOGIN);
    }

    /**
     * Navigate to a screen by name.
     */
    public void showScreen(String screenName) {
        if (screenName == null || screenName.equals(currentScreen)) {
            return;
        }
        if (currentScreen != null && !SCREEN_LOGIN.equals(screenName)) {
            navigationHistory.push(currentScreen);
        }
        currentScreen = screenName;
        cardLayout.show(contentPanel, screenName);

        // Refresh dynamic panels when shown
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof Refreshable refreshable) {
                // Only refresh the target panel
                if (comp.isVisible()) {
                    refreshable.onShow();
                }
            }
        }
    }

    /**
     * Start an ATM session after successful login.
     */
    public void startSession(ATMCard card) {
        this.currentCard = card;
        this.currentAdmin = null;
        showScreen(SCREEN_MAIN_MENU);
    }

    /**
     * Start an admin session.
     */
    public void startAdminSession(Admin admin) {
        this.currentAdmin = admin;
        this.currentCard = null;
        showScreen(SCREEN_ADMIN_PANEL);
    }

    /**
     * End the current session and return to login.
     */
    public void logout() {
        this.currentCard = null;
        this.currentAdmin = null;
        navigationHistory.clear();
        showScreen(SCREEN_LOGIN);
    }

    /**
     * Pre-fills the login screen with a newly issued card number and navigates to login.
     */
    public void prefillLogin(String cardNumber) {
        if (loginPanel != null) {
            loginPanel.setCardNumber(cardNumber);
        }
        showScreen(SCREEN_LOGIN);
    }

    public ATMCard getCurrentCard() { return currentCard; }
    public Admin getCurrentAdmin() { return currentAdmin; }

    private void installKeyboardNavigation() {
        JRootPane root = getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "atm-back");
        inputMap.put(KeyStroke.getKeyStroke("alt LEFT"), "atm-back");
        actionMap.put("atm-back", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { navigateBack(); }
        });
    }

    private void installNavigationGestures(Component component) {
        if (component instanceof JButton || component instanceof JTextComponent || component instanceof JComboBox<?>) {
            return;
        }
        if (component instanceof JComponent jc) {
            jc.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { gestureStart = e.getPoint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (gestureStart == null) return;
                    int delta = e.getX() - gestureStart.x;
                    if (Math.abs(delta) > 90 && Math.abs(delta) > Math.abs(e.getY() - gestureStart.y) * 1.25) {
                        if (delta < 0) navigateForward(); else navigateBack();
                    }
                    gestureStart = null;
                }
            });
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) installNavigationGestures(child);
        }
    }

    private void navigateBack() {
        if (SCREEN_LOGIN.equals(currentScreen) || navigationHistory.isEmpty()) return;
        String previous = navigationHistory.pop();
        String destination = previous;
        currentScreen = null;
        showScreen(destination);
    }

    private void navigateForward() {
        if (SCREEN_MAIN_MENU.equals(currentScreen) || SCREEN_LOGIN.equals(currentScreen)) return;
        // A left swipe returns to the menu; this is predictable on every transaction screen.
        currentScreen = null;
        showScreen(SCREEN_MAIN_MENU);
    }

    /**
     * Interface for panels that need to refresh data when shown.
     */
    public interface Refreshable {
        void onShow();
    }
}
