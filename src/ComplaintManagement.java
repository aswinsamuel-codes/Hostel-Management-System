import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ComplaintManagement extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID","Student","Text","Date","Status"
    }, 0){ public boolean isCellEditable(int r,int c){ return false; }};
    private final JTable table = new JTable(model);

    public ComplaintManagement(){
        setLayout(new BorderLayout());
        setBackground(Utils.LIGHT);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(Color.WHITE);
        JButton add = Utils.makeButton("File Complaint");
        JButton resolve = Utils.makeButton("Mark Resolved");
        JButton refresh = Utils.makeButton("Refresh");
        bar.add(add); bar.add(resolve); bar.add(refresh);
        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> onAdd());
        resolve.addActionListener(e -> onResolve());
        refresh.addActionListener(e -> load());
        load();
    }

    private void load(){
        model.setRowCount(0);
        String sql = "SELECT c.complaint_id, s.name, c.complaint_text, c.date_filed, c.status FROM complaints c JOIN students s ON c.student_id=s.student_id ORDER BY c.complaint_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDate(4), rs.getString(5)});
        } catch (Exception ex){ Utils.showError(this, "Failed to load complaints: "+ex.getMessage()); }
    }

    private void onAdd(){
        JTextField studentId = new JTextField(6);
        JTextArea text = new JTextArea(4, 22);
        JPanel p = new JPanel(new BorderLayout(6,6));
        JPanel top = new JPanel(new GridLayout(1,2,6,6));
        top.add(new JLabel("Student ID")); top.add(studentId);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(text), BorderLayout.CENTER);
        int r = JOptionPane.showConfirmDialog(this, p, "File Complaint", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r==JOptionPane.OK_OPTION){
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO complaints(student_id,complaint_text,status) VALUES (?,?, 'Pending')")){
                ps.setInt(1, Integer.parseInt(studentId.getText().trim()));
                ps.setString(2, text.getText().trim());
                ps.executeUpdate();
                load();
            } catch (Exception ex){ Utils.showError(this, "Add failed: "+ex.getMessage()); }
        }
    }

    private void onResolve(){
        int row = table.getSelectedRow(); if (row<0){ Utils.showError(this, "Select a complaint."); return; }
        int id = (Integer) model.getValueAt(row,0);
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE complaints SET status='Resolved' WHERE complaint_id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
            load();
        } catch (Exception ex){ Utils.showError(this, "Update failed: "+ex.getMessage()); }
    }
}


