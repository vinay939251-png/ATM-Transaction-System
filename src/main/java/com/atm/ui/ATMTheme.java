package com.atm.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Design system for the ATM application.
 * Inspired by Sight Banking (dark fintech) and Contiant (enterprise security).
 * 
 * Color palette:
 * - Background: Deep navy (#0B1121) and dark slate (#111827)
 * - Cards: Slightly lighter slate (#1E293B) with subtle borders
 * - Primary accent: Emerald green (#10B981)
 * - Secondary accent: Electric cyan (#06B6D4)
 * - Danger: Soft red (#EF4444)
 * - Warning: Amber (#F59E0B)
 * - Text: White (#F8FAFC) and muted gray (#94A3B8)
 */
public class ATMTheme {

    // ============ COLOR PALETTE ============
    public static final Color BG_PRIMARY = new Color(0x0B, 0x11, 0x21);       // Deep navy
    public static final Color BG_SECONDARY = new Color(0x11, 0x18, 0x27);     // Dark slate
    public static final Color BG_CARD = new Color(0x1E, 0x29, 0x3B);          // Card background
    public static final Color BG_CARD_HOVER = new Color(0x25, 0x33, 0x4A);    // Card hover
    public static final Color BG_INPUT = new Color(0x0F, 0x17, 0x2A);         // Input field bg

    public static final Color ACCENT_PRIMARY = new Color(0x10, 0xB9, 0x81);   // Emerald green
    public static final Color ACCENT_SECONDARY = new Color(0x06, 0xB6, 0xD4); // Electric cyan
    public static final Color ACCENT_PRIMARY_HOVER = new Color(0x05, 0x9B, 0x6A);

    public static final Color DANGER = new Color(0xEF, 0x44, 0x44);           // Red
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);          // Amber
    public static final Color SUCCESS = new Color(0x10, 0xB9, 0x81);          // Same as accent

    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);     // White
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);   // Muted gray
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);       // More muted
    public static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A);        // For light backgrounds

    public static final Color BORDER_DEFAULT = new Color(0x33, 0x41, 0x55);   // Subtle border
    public static final Color BORDER_FOCUSED = ACCENT_PRIMARY;

    // Gradient start/end for header
    public static final Color GRADIENT_START = new Color(0x06, 0x50, 0x3F);
    public static final Color GRADIENT_END = new Color(0x0B, 0x11, 0x21);

    // ============ FONTS ============
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 14);
    public static final Font FONT_BALANCE = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_BUTTON = new Font("Segoe UI Semibold", Font.PLAIN, 14);

    // ============ DIMENSIONS ============
    public static final int CARD_RADIUS = 16;
    public static final int BUTTON_RADIUS = 10;
    public static final int INPUT_RADIUS = 8;
    public static final Dimension BUTTON_SIZE = new Dimension(200, 44);
    public static final Dimension INPUT_SIZE = new Dimension(300, 44);
    public static final int PADDING = 24;
    public static final int CARD_PADDING = 20;

    // ============ FACTORY METHODS ============

    /**
     * Creates a styled primary button (emerald green, rounded).
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getModel().isPressed() ? ACCENT_PRIMARY_HOVER.darker()
                        : getModel().isRollover() ? new Color(0x34, 0xD3, 0x99) : ACCENT_PRIMARY;
                if (!isEnabled()) bg = new Color(0x33, 0x41, 0x55);

                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_RADIUS * 2, BUTTON_RADIUS * 2));

                g2.setColor(isEnabled() ? Color.WHITE : TEXT_MUTED);
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                if (hasFocus()) {
                    g2.setColor(new Color(ACCENT_SECONDARY.getRed(), ACCENT_SECONDARY.getGreen(), ACCENT_SECONDARY.getBlue(), 190));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
                }

                g2.dispose();
            }
        };
        stylePrimaryButton(btn);
        return btn;
    }

    /**
     * Creates a styled secondary button (outlined, transparent).
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color borderColor = getModel().isRollover() || hasFocus() ? ACCENT_PRIMARY : BORDER_DEFAULT;
                Color textColor = getModel().isRollover() ? ACCENT_PRIMARY : TEXT_PRIMARY;

                if (getModel().isRollover()) {
                    g2.setColor(new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(),
                            getModel().isPressed() ? 70 : 42));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_RADIUS * 2, BUTTON_RADIUS * 2));
                }

                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2));

                g2.setColor(textColor);
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                if (hasFocus()) {
                    g2.setColor(new Color(ACCENT_SECONDARY.getRed(), ACCENT_SECONDARY.getGreen(), ACCENT_SECONDARY.getBlue(), 170));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
                }

                g2.dispose();
            }
        };
        stylePrimaryButton(btn);
        return btn;
    }

    /**
     * Creates a danger button (red).
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getModel().isPressed() ? DANGER.darker().darker()
                        : getModel().isRollover() ? new Color(0xF8, 0x5B, 0x5B) : DANGER;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_RADIUS * 2, BUTTON_RADIUS * 2));

                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                if (hasFocus()) {
                    g2.setColor(new Color(ACCENT_SECONDARY.getRed(), ACCENT_SECONDARY.getGreen(), ACCENT_SECONDARY.getBlue(), 180));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
                }

                g2.dispose();
            }
        };
        stylePrimaryButton(btn);
        return btn;
    }

    private static void stylePrimaryButton(JButton btn) {
        btn.setPreferredSize(BUTTON_SIZE);
        btn.setMinimumSize(BUTTON_SIZE);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_SIZE.height));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setRolloverEnabled(true);
        btn.setFocusable(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(FONT_BUTTON);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e) { btn.repaint(); }
            @Override public void mousePressed(MouseEvent e) { btn.repaint(); }
            @Override public void mouseReleased(MouseEvent e) { btn.repaint(); }
        });
        btn.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { btn.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { btn.repaint(); }
        });
    }

    /**
     * Creates a styled text field with dark background and rounded border.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_RADIUS * 2, INPUT_RADIUS * 2));

                super.paintComponent(g);

                // Draw placeholder
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_BODY);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }

                g2.dispose();
            }
        };
        styleTextField(field);
        return field;
    }

    /**
     * Creates a styled password field.
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_RADIUS * 2, INPUT_RADIUS * 2));

                super.paintComponent(g);

                if (getPassword().length == 0 && !isFocusOwner()) {
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_BODY);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }

                g2.dispose();
            }
        };
        styleTextField(field);
        return field;
    }

    private static void styleTextField(JTextField field) {
        field.setPreferredSize(INPUT_SIZE);
        field.setMinimumSize(new Dimension(200, INPUT_SIZE.height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_SIZE.height));
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_PRIMARY);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_DEFAULT, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        // Focus border effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_FOCUSED, 2, true),
                    new EmptyBorder(7, 13, 7, 13)
                ));
                field.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_DEFAULT, 1, true),
                    new EmptyBorder(8, 14, 8, 14)
                ));
                field.repaint();
            }
        });
    }

    /**
     * Creates a styled card panel (rounded, slightly elevated).
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS));

                // Subtle top border glow
                g2.setColor(new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), 30));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, CARD_RADIUS, CARD_RADIUS));

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING));
        return card;
    }

    /**
     * Creates a gradient header panel.
     */
    public static JPanel createGradientHeader(String title, String subtitle) {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, getWidth(), getHeight(), GRADIENT_END);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS));

                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);

        if (subtitle != null) {
            header.add(Box.createVerticalStrut(6));
            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(FONT_BODY);
            subLabel.setForeground(TEXT_SECONDARY);
            subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(subLabel);
        }

        return header;
    }

    /**
     * Creates a styled label.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Creates a status badge (pill-shaped).
     */
    public static JLabel createBadge(String text, Color bgColor) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(FONT_SMALL);
        badge.setForeground(bgColor);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        return badge;
    }

    /**
     * Creates a menu item button for the main ATM menu.
     */
    public static JButton createMenuButton(String text, String icon) {
        JButton btn = new JButton() {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = hovered ? BG_CARD_HOVER : BG_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS));

                if (hovered) {
                    g2.setColor(ACCENT_PRIMARY);
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, CARD_RADIUS, CARD_RADIUS));
                } else {
                    g2.setColor(BORDER_DEFAULT);
                    g2.setStroke(new BasicStroke(1));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, CARD_RADIUS, CARD_RADIUS));
                }

                // Icon
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
                g2.setColor(hovered ? ACCENT_PRIMARY : TEXT_SECONDARY);
                FontMetrics fmIcon = g2.getFontMetrics();
                g2.drawString(icon, 20, (getHeight() - fmIcon.getHeight()) / 2 + fmIcon.getAscent());

                // Text
                g2.setFont(FONT_SUBHEADING);
                g2.setColor(hovered ? TEXT_PRIMARY : TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, 65, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());

                // Arrow indicator
                if (hovered) {
                    g2.setFont(FONT_BODY);
                    g2.setColor(ACCENT_PRIMARY);
                    g2.drawString("→", getWidth() - 35, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(340, 64));
        btn.setMinimumSize(new Dimension(300, 64));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    /**
     * Shows a styled error message dialog.
     */
    public static void showError(Component parent, String message) {
        showStyledDialog(parent, "Something needs attention", message, DANGER, false);
    }

    /**
     * Shows a styled success message dialog.
     */
    public static void showSuccess(Component parent, String message) {
        showStyledDialog(parent, "Transaction complete", message, SUCCESS, false);
    }

    /**
     * Shows a confirmation dialog. Returns true if user confirms.
     */
    public static boolean showConfirm(Component parent, String message) {
        return showStyledDialog(parent, "Please confirm", message, WARNING, true);
    }

    /**
     * Shows a compact, keyboard-friendly modal instead of the platform-default JOptionPane.
     */
    private static boolean showStyledDialog(Component parent, String title, String message,
                                            Color accent, boolean confirm) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel shell = new JPanel(new BorderLayout(0, 18)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 5, 20, 20);
                g2.setColor(BORDER_DEFAULT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(24, 26, 22, 26));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel heading = createLabel(title, FONT_SUBHEADING, TEXT_PRIMARY);
        JLabel body = createLabel("<html><div style='width:330px;line-height:1.45'>" +
                message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>") +
                "</div></html>", FONT_BODY, TEXT_SECONDARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        copy.add(heading);
        copy.add(Box.createVerticalStrut(8));
        copy.add(body);
        shell.add(copy, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        boolean[] accepted = {false};
        if (confirm) {
            JButton cancel = createSecondaryButton("Cancel");
            cancel.addActionListener(e -> dialog.dispose());
            actions.add(cancel);
        }
        JButton close = confirm ? createPrimaryButton("Continue") : createPrimaryButton("Done");
        close.addActionListener(e -> { accepted[0] = true; dialog.dispose(); });
        close.setPreferredSize(new Dimension(120, 40));
        actions.add(close);
        shell.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(shell);
        dialog.getRootPane().setDefaultButton(close);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(390, 180));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return accepted[0];
    }
}
