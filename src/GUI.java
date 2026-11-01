import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.*;
import java.util.Arrays;

public class GUI {
    private final Fileprocessor processor;
    private final Keymanager keys = new Keymanager("auth.dat");
    private final Logger logger = new Logger("app.log", true);
    private JFrame frame;
    private final Color blue = new Color(55, 153, 195);
    private final Font title = new Font("SansSerif", Font.BOLD, 26);
    private Path folder;
    private char[] sessionPassword;

    public GUI(Fileprocessor processor) { this.processor = processor; }
    public void buildWindow() { SwingUtilities.invokeLater(() -> showLogin(false)); }
    public void onEncryptClick() { if (folder != null) process(true); }
    private JLabel label(String s, int size, boolean bold) {
        JLabel l = new JLabel(s); l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size)); return l;
    }
    private JButton button(String s, Runnable r) {
        JButton b = new JButton(s); b.setFocusPainted(false); b.addActionListener(e -> r.run()); return b;
    }
    private void start(String name) {
        if (frame != null) frame.dispose();
        frame = new JFrame(name + " - ENCRYPTYIT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(860, 530); frame.setMinimumSize(new Dimension(750, 480));
        frame.setLocationRelativeTo(null);
    }
    private void showLogin(boolean signup) {
        start(signup ? "Sign up" : "Login");
        JPanel root = new JPanel(new GridLayout(1, 2));
        JPanel left = new JPanel(new GridBagLayout()); left.setBackground(blue);
        JPanel right = new JPanel(new GridBagLayout()); right.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints(); c.gridx = 0; c.anchor = GridBagConstraints.CENTER;
        java.net.URL logo = GUI.class.getResource("/encryption_logo.png");
        if (logo != null) {
            ImageIcon image = new ImageIcon(logo);
            JLabel img = new JLabel(new ImageIcon(image.getImage().getScaledInstance(235, 150, Image.SCALE_SMOOTH)));
            c.gridy = 0; c.insets = new Insets(0, 0, 12, 0); left.add(img, c);
        }
        JLabel welcome = label("Welcome to", 23, true); welcome.setForeground(Color.WHITE);
        c.gridy = 1; left.add(welcome, c);
        JLabel brand = label("ENCRYPTYIT", 30, true); brand.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 30));
        c.gridy = 2; c.insets = new Insets(15, 0, 65, 0); left.add(brand, c);
        JButton toggle = button(signup ? "Back to login" : "Sign up", () -> showLogin(!signup));
        c.gridy = 3; c.insets = new Insets(0, 0, 0, 0); left.add(toggle, c);

        JPanel form = new JPanel(); form.setOpaque(false); form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JLabel heading = label(signup ? "SIGN UP" : "LOGIN", 26, true); heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = label(signup ? "Create your master password below." : "Please enter details below.", 12, false);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField user = new JTextField(19); user.setMaximumSize(new Dimension(250, 32));
        user.setToolTipText("Enter your first and surname, e.g. Victoria Mangira");
        JPasswordField pwd = new JPasswordField(19); pwd.setMaximumSize(new Dimension(250, 32));
        JPasswordField confirm = new JPasswordField(19); confirm.setMaximumSize(new Dimension(250, 32));
        form.add(heading); form.add(Box.createVerticalStrut(10)); form.add(subtitle);
        form.add(Box.createVerticalStrut(32)); form.add(label(signup ? "Full name (first name and surname)" : "Username", 13, false)); form.add(user);
        if (signup) { form.add(Box.createVerticalStrut(4)); JLabel suffix = label("Username format: FirstnameSurname@encrypt", 11, false); suffix.setForeground(Color.GRAY); form.add(suffix); }
        form.add(Box.createVerticalStrut(14)); form.add(label("Password", 13, false)); form.add(pwd);
        if (signup) {
            form.add(Box.createVerticalStrut(5));
            JLabel passwordHelp = label("Password requirements: 8+ characters, at least 1 letter, at least 1 number, exactly 1 special character, and no spaces.", 10, false);
            passwordHelp.setForeground(new Color(90, 90, 90));
            passwordHelp.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(passwordHelp);
            pwd.setToolTipText("At least 8 characters, at least one letter, at least one number, exactly one special character, and no spaces.");
            form.add(Box.createVerticalStrut(10)); form.add(label("Confirm password", 13, false)); form.add(confirm);
        }
        form.add(Box.createVerticalStrut(25));
        JButton submit = button(signup ? "Create account" : "Login", () -> {
            char[] p = pwd.getPassword();
            try {
                String enteredUser = user.getText().trim();
                String accountUsername = signup ? createUsername(enteredUser) : enteredUser;
                if (signup) {
                    if (accountUsername == null) { alert("Enter your first name and surname using letters only."); return; }
                    if (keys.hasCredentials()) { alert("A master account already exists. Please log in."); return; }
                    if (!validPassword(p)) { alert("Your password must be at least 8 characters, include at least one letter and one number, contain exactly one special character, and have no spaces."); return; }
                    if (!Arrays.equals(p, confirm.getPassword())) { alert("Passwords do not match."); return; }
                    keys.registerAccount(accountUsername, p); alert("Account created! Your username is " + accountUsername); showLogin(false);
                } else {
                    if (!keys.hasCredentials()) { alert("No account yet. Click Sign up."); return; }
                    if (!keys.authenticate(accountUsername, p)) { alert("Incorrect username or password."); return; }
                    sessionPassword = p.clone(); showDashboard();
                }
            } catch (Exception ex) { alert("Error: " + ex.getMessage()); }
            finally { Arrays.fill(p, '\0'); }
        });
        submit.setAlignmentX(Component.CENTER_ALIGNMENT); form.add(submit);
        c = new GridBagConstraints(); c.gridx = 0; c.gridy = 0; right.add(form, c);
        root.add(left); root.add(right); frame.setContentPane(root); frame.setVisible(true);
    }
    private void showDashboard() {
        start("Dashboard");
        JPanel page = new JPanel(new BorderLayout(15, 15)); page.setBorder(new EmptyBorder(25, 30, 25, 30));
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(blue);
        JLabel heading = label("  ENCRYPTYIT  |  File Encryption Dashboard", 22, true);
        heading.setForeground(Color.WHITE); top.add(heading, BorderLayout.CENTER);
        JPanel accountActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accountActions.setOpaque(false);
        accountActions.add(button("Change password", this::changePassword));
        accountActions.add(button("Log out", () -> { if (sessionPassword != null) Arrays.fill(sessionPassword, '\0'); sessionPassword = null; showLogin(false); }));
        top.add(accountActions, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);
        JPanel middle = new JPanel(); middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        JLabel selected = label("No folder selected", 14, false);
        JButton choose = button("Choose folder", () -> {
            JFileChooser chooser = new JFileChooser(); chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                folder = chooser.getSelectedFile().toPath(); selected.setText("Selected: " + folder);
            }
        });
        middle.add(label("Select a folder to encrypt or decrypt", 18, true)); middle.add(Box.createVerticalStrut(15));
        middle.add(choose); middle.add(Box.createVerticalStrut(12)); middle.add(selected);
        middle.add(Box.createVerticalStrut(24));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.add(button("Encrypt folder", () -> process(true)));
        actions.add(button("Decrypt folder", () -> process(false)));
        actions.add(button("List files", () -> {
            if (!checkFolder()) return;
            try { Folderscanner fs = new Folderscanner(); fs.scanRecursive(folder);
                StringBuilder sb = new StringBuilder(); fs.getFileList().forEach(f -> sb.append(f).append("\n"));
                showText("Files", sb.length() == 0 ? "No files found" : sb.toString());
            } catch (Exception ex) { alert(ex.getMessage()); }
        }));
        actions.add(button("Activity log", () -> showText("Activity log", String.join("\n", logger.readAll()))));
        middle.add(actions); page.add(middle, BorderLayout.CENTER);
        page.add(label("Important: Encryption replaces original files with .enc files. Back up your files first.", 12, false), BorderLayout.SOUTH);
        frame.setContentPane(page); frame.setVisible(true);
    }
    private String createUsername(String fullName) {
        String cleaned = fullName.trim().replaceAll("\\s+", " ");
        String[] parts = cleaned.split(" ");
        if (parts.length < 2) return null;
        for (String part : parts) if (!part.matches("[A-Za-z]+(?:[\\'-][A-Za-z]+)*")) return null;
        StringBuilder name = new StringBuilder();
        for (String part : parts) name.append(part);
        return name.toString() + "@encrypt";
    }
    private boolean validPassword(char[] password) {
        if (password.length < 8) return false;
        boolean letter = false, digit = false;
        int special = 0;
        for (char ch : password) {
            if (Character.isWhitespace(ch)) return false;
            if (Character.isLetter(ch)) letter = true;
            else if (Character.isDigit(ch)) digit = true;
            else special++;
        }
        return letter && digit && special == 1;
    }
    private void changePassword() {
        JPasswordField current = new JPasswordField();
        JPasswordField next = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Current password")); panel.add(current);
        panel.add(new JLabel("New password")); panel.add(next);
        JLabel passwordHelp = new JLabel("Requirements: 8+ characters, at least one letter, at least one number, exactly one special character, and no spaces.");
        panel.add(passwordHelp);
        next.setToolTipText("At least 8 characters, at least one letter, at least one number, exactly one special character, and no spaces.");
        panel.add(new JLabel("Confirm new password")); panel.add(confirm);
        if (JOptionPane.showConfirmDialog(frame, panel, "Change password", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        char[] oldPassword = current.getPassword(), newPassword = next.getPassword(), repeated = confirm.getPassword();
        try {
            if (!validPassword(newPassword)) { alert("Your new password must be at least 8 characters, include at least one letter and one number, contain exactly one special character, and have no spaces."); return; }
            if (!Arrays.equals(newPassword, repeated)) { alert("New passwords do not match."); return; }
            keys.changeMasterPassword(oldPassword, newPassword);
            if (sessionPassword != null) Arrays.fill(sessionPassword, '\0');
            sessionPassword = newPassword.clone();
            alert("Password changed successfully.");
            logger.log("Master password changed for " + keys.getUsername());
        } catch (Exception ex) { alert("Could not change password: " + ex.getMessage()); }
        finally { Arrays.fill(oldPassword, '\0'); Arrays.fill(newPassword, '\0'); Arrays.fill(repeated, '\0'); }
    }
    private boolean checkFolder() { if (folder == null) { alert("Choose a folder first."); return false; } return true; }
    private void process(boolean encrypt) {
        if (!checkFolder()) return;
        String warning = encrypt ? "Encryption replaces original files with .enc files. Back up the folder before proceeding. Continue?"
                : "Decrypt .enc files in this folder? Continue?";
        if (JOptionPane.showConfirmDialog(frame, warning, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        JPasswordField input = new JPasswordField();
        if (JOptionPane.showConfirmDialog(frame, input, "Enter folder encryption password", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        char[] p = input.getPassword();
        if (p.length == 0) { alert("Password cannot be empty."); return; }
        try { int count = encrypt ? processor.encryptFolder(folder, p) : processor.decryptFolder(folder, p);
            alert(count + " file(s) " + (encrypt ? "encrypted" : "decrypted") + ". Check the activity log for errors.");
        } catch (Exception ex) { alert("Operation failed: " + ex.getMessage()); }
        finally { Arrays.fill(p, '\0'); }
    }
    private void showText(String name, String content) {
        JTextArea area = new JTextArea(content, 18, 55); area.setEditable(false);
        JOptionPane.showMessageDialog(frame, new JScrollPane(area), name, JOptionPane.INFORMATION_MESSAGE);
    }
    private void alert(String message) { JOptionPane.showMessageDialog(frame, message); }
}
