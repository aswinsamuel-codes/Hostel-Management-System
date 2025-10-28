import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StaffManagement extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID","Name","Role","Phone","Email"
    }, 0){ public boolean isCellEditable(int r,int c){ return false; }};
    private final JTable table = new JTable(model);

    public StaffManagement(){
        setLayout(new BorderLayout());
        setBackground(Utils.LIGHT);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(Color.WHITE);
        JButton add = Utils.makeButton("Add Staff");
        JButton edit = Utils.makeButton("Edit");
        JButton del = Utils.makeButton("Delete");
        JButton refresh = Utils.makeButton("Refresh");
        bar.add(add); bar.add(edit); bar.add(del); bar.add(refresh);
        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        del.addActionListener(e -> onDelete());
        refresh.addActionListener(e -> load());
        load();
    }

    private void load(){
        model.setRowCount(0);
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT staff_id,name,role,phone,email FROM staff ORDER BY staff_id DESC"); ResultSet rs = ps.executeQuery()){
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
        } catch (Exception ex){ Utils.showError(this, "Failed to load staff: "+ex.getMessage()); }
    }

    private void onAdd(){
        JTextField name=new JTextField(16), role=new JTextField(12), phone=new JTextField(12), email=new JTextField(16);
        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Name")); p.add(name);
        p.add(new JLabel("Role")); p.add(role);
        p.add(new JLabel("Phone")); p.add(phone);
        p.add(new JLabel("Email")); p.add(email);
        int r = JOptionPane.showConfirmDialog(this, p, "Add Staff", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r==JOptionPane.OK_OPTION){
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO staff(name,role,phone,email) VALUES (?,?,?,?)")){
                ps.setString(1, name.getText().trim());
                ps.setString(2, role.getText().trim());
                ps.setString(3, phone.getText().trim());
                ps.setString(4, email.getText().trim());
                ps.executeUpdate();
                load();
            } catch (Exception ex){ Utils.showError(this, "Add failed: "+ex.getMessage()); }
        }
    }

    private void onEdit(){
        int row = table.getSelectedRow(); if (row<0){ Utils.showError(this, "Select a staff."); return; }
        int id = (Integer) model.getValueAt(row,0);
        JTextField name=new JTextField(String.valueOf(model.getValueAt(row,1)));
        JTextField role=new JTextField(String.valueOf(model.getValueAt(row,2)));
        JTextField phone=new JTextField(String.valueOf(model.getValueAt(row,3)));
        JTextField email=new JTextField(String.valueOf(model.getValueAt(row,4)));
        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Name")); p.add(name);
        p.add(new JLabel("Role")); p.add(role);
        p.add(new JLabel("Phone")); p.add(phone);
        p.add(new JLabel("Email")); p.add(email);
        int r = JOptionPane.showConfirmDialog(this, p, "Edit Staff", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r==JOptionPane.OK_OPTION){
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE staff SET name=?, role=?, phone=?, email=? WHERE staff_id=?")){
                ps.setString(1, name.getText().trim());
                ps.setString(2, role.getText().trim());
                ps.setString(3, phone.getText().trim());
                ps.setString(4, email.getText().trim());
                ps.setInt(5, id);
                ps.executeUpdate();
                load();
            } catch (Exception ex){ Utils.showError(this, "Update failed: "+ex.getMessage()); }
        }
    }

    private void onDelete(){
        int row = table.getSelectedRow(); if (row<0){ Utils.showError(this, "Select a staff."); return; }
        int id = (Integer) model.getValueAt(row,0);
        if (!Utils.confirm(this, "Delete selected staff?")) return;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM staff WHERE staff_id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
            load();
        } catch (Exception ex){ Utils.showError(this, "Delete failed: "+ex.getMessage()); }
    }
}


