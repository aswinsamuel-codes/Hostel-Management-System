import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        setTitle("Hostel Management - Login");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        container.setBackground(Utils.LIGHT);

        JLabel title = new JLabel("Hostel Management System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(Utils.PRIMARY);
        container.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                BorderFactory.createEmptyBorder(16,16,16,16)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Username"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Password"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(passwordField, gbc);

        JButton loginBtn = Utils.makeButton("Login");
        loginBtn.addActionListener(this::onLogin);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; form.add(loginBtn, gbc);

        container.add(form, BorderLayout.CENTER);
        add(container);
    }

    private void onLogin(ActionEvent e) {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            Utils.showError(this, "Please enter username and password.");
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT admin_id FROM admin WHERE username=? AND password=?")) {
            ps.setString(1, u);
            ps.setString(2, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.dispose();
                    SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
                } else {
                    Utils.showError(this, "Invalid credentials.");
                }
            }
        } catch (Exception ex) {
            Utils.showError(this, "Login failed: " + ex.getMessage());
        }
    }
}


