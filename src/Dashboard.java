import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Dashboard extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private final JLabel totalStudentsLbl = new JLabel("0");
    private final JLabel totalRoomsLbl = new JLabel("0");
    private final JLabel availableRoomsLbl = new JLabel("0");
    private final JLabel pendingComplaintsLbl = new JLabel("0");
    private final JLabel pendingFeesLbl = new JLabel("0");

    private final StudentManagement studentManagement = new StudentManagement();
    private final RoomManagement roomManagement = new RoomManagement();
    private final FeesManagement feesManagement = new FeesManagement();
    private final StaffManagement staffManagement = new StaffManagement();
    private final ComplaintManagement complaintManagement = new ComplaintManagement();
    private final ReportGenerator reportGenerator = new ReportGenerator();

    public Dashboard() {
        setTitle("Hostel Management - Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        content.add(buildHome(), "HOME");
        content.add(studentManagement, "STUDENTS");
        content.add(roomManagement, "ROOMS");
        content.add(feesManagement, "FEES");
        content.add(staffManagement, "STAFF");
        content.add(complaintManagement, "COMPLAINTS");
        content.add(reportGenerator, "REPORTS");
        add(content, BorderLayout.CENTER);

        refreshStats();
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Utils.DARK);
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel brand = new JLabel("HMS", SwingConstants.CENTER);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 22f));
        brand.setBorder(BorderFactory.createEmptyBorder(16,0,16,0));
        panel.add(brand);

        panel.add(makeNavButton("Dashboard", () -> showCard("HOME")));
        panel.add(makeNavButton("Students", () -> showCard("STUDENTS")));
        panel.add(makeNavButton("Rooms", () -> showCard("ROOMS")));
        panel.add(makeNavButton("Fees", () -> showCard("FEES")));
        panel.add(makeNavButton("Staff", () -> showCard("STAFF")));
        panel.add(makeNavButton("Complaints", () -> showCard("COMPLAINTS")));
        panel.add(makeNavButton("Reports", () -> showCard("REPORTS")));

        panel.add(Box.createVerticalGlue());
        panel.add(makeNavButton("Refresh Stats", this::refreshStats));
        panel.add(Box.createVerticalStrut(8));
        panel.add(makeNavButton("Logout", this::logout));

        return panel;
    }

    private Component makeNavButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 36));
        btn.setBackground(Utils.ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> action.run());
        btn.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(btn, BorderLayout.CENTER);
        wrap.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
        return wrap;
    }

    private JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout());
        home.setBackground(Utils.LIGHT);
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 12));
        cards.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        cards.setBackground(Utils.LIGHT);

        cards.add(Utils.makeCard("Total Students", labelToText(totalStudentsLbl)));
        cards.add(Utils.makeCard("Total Rooms", labelToText(totalRoomsLbl)));
        cards.add(Utils.makeCard("Available Rooms", labelToText(availableRoomsLbl)));
        cards.add(Utils.makeCard("Pending Complaints", labelToText(pendingComplaintsLbl)));
        cards.add(Utils.makeCard("Pending Fees", labelToText(pendingFeesLbl)));

        home.add(cards, BorderLayout.NORTH);
        return home;
    }

    private String labelToText(JLabel lbl){
        return lbl.getText();
    }

    private void showCard(String name) {
        cardLayout.show(content, name);
    }

    private void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    public void refreshStats() {
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM students"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalStudentsLbl.setText(String.valueOf(rs.getInt(1)));
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM rooms"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalRoomsLbl.setText(String.valueOf(rs.getInt(1)));
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM rooms WHERE availability=TRUE"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) availableRoomsLbl.setText(String.valueOf(rs.getInt(1)));
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM complaints WHERE status='Pending'"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) pendingComplaintsLbl.setText(String.valueOf(rs.getInt(1)));
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM fees WHERE status='Pending'"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) pendingFeesLbl.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load stats: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


