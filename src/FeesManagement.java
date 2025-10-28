import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FeesManagement extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "Fee ID","Student","Amount","Status","Due Date"
    }, 0){ public boolean isCellEditable(int r,int c){ return false; }};
    private final JTable table = new JTable(model);

    public FeesManagement(){
        setLayout(new BorderLayout());
        setBackground(Utils.LIGHT);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(Color.WHITE);
        JButton add = Utils.makeButton("Add Fee");
        JButton markPaid = Utils.makeButton("Mark Paid");
        JButton refresh = Utils.makeButton("Refresh");
        bar.add(add); bar.add(markPaid); bar.add(refresh);
        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> onAdd());
        markPaid.addActionListener(e -> onMarkPaid());
        refresh.addActionListener(e -> load());
        load();
    }

    private void load(){
        model.setRowCount(0);
        String sql = "SELECT f.fee_id, s.name, f.amount, f.status, f.due_date FROM fees f JOIN students s ON f.student_id=s.student_id ORDER BY f.fee_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getBigDecimal(3), rs.getString(4), rs.getDate(5)});
        } catch (Exception ex){ Utils.showError(this, "Failed to load fees: "+ex.getMessage()); }
    }

    private void onAdd(){
        JTextField studentId = new JTextField(6);
        JTextField amount = new JTextField(8);
        JTextField dueDate = new JTextField(10); // YYYY-MM-DD
        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Student ID")); p.add(studentId);
        p.add(new JLabel("Amount")); p.add(amount);
        p.add(new JLabel("Due Date (YYYY-MM-DD)")); p.add(dueDate);
        int r = JOptionPane.showConfirmDialog(this, p, "Add Fee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r==JOptionPane.OK_OPTION){
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO fees(student_id,amount,status,due_date) VALUES (?,?, 'Pending', ? )")){
                ps.setInt(1, Integer.parseInt(studentId.getText().trim()));
                ps.setBigDecimal(2, new java.math.BigDecimal(amount.getText().trim()));
                ps.setDate(3, Date.valueOf(dueDate.getText().trim()));
                ps.executeUpdate();
                load();
            } catch (Exception ex){ Utils.showError(this, "Add failed: "+ex.getMessage()); }
        }
    }

    private void onMarkPaid(){
        int row = table.getSelectedRow();
        if (row<0){ Utils.showError(this, "Select a fee."); return; }
        int feeId = (Integer) model.getValueAt(row,0);
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE fees SET status='Paid' WHERE fee_id=?")){
            ps.setInt(1, feeId);
            ps.executeUpdate();
            load();
        } catch (Exception ex){ Utils.showError(this, "Update failed: "+ex.getMessage()); }
    }
}


