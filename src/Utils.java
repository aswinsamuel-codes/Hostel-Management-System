import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Utils {

    public static final Color PRIMARY = new Color(0x0F4C81);
    public static final Color ACCENT = new Color(0x17A2B8);
    public static final Color LIGHT = new Color(0xF5F7FA);
    public static final Color DARK = new Color(0x1F2937);

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int r = JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION);
        return r == JOptionPane.YES_OPTION;
    }

    public static JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        return btn;
    }

    public static JPanel makeCard(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                BorderFactory.createEmptyBorder(12,12,12,12)));
        JLabel t = new JLabel(title);
        t.setForeground(DARK);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 12f));
        JLabel v = new JLabel(value);
        v.setForeground(PRIMARY);
        v.setFont(v.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(t, BorderLayout.NORTH);
        panel.add(v, BorderLayout.CENTER);
        return panel;
    }

    public static void exportResultSetToCsv(ResultSet rs, String filePath) throws IOException, SQLException {
        try (FileWriter writer = new FileWriter(filePath)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                writer.append(meta.getColumnName(i));
                if (i < columnCount) writer.append(',');
            }
            writer.append('\n');
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String val = rs.getString(i);
                    if (val != null) {
                        String escaped = '"' + val.replace("\"", "\"\"") + '"';
                        writer.append(escaped);
                    }
                    if (i < columnCount) writer.append(',');
                }
                writer.append('\n');
            }
        }
    }
}


