import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RoomManagement extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "Room No","Block","Capacity","Occupied","Available"
    }, 0){ public boolean isCellEditable(int r,int c){ return false; }};
    private final JTable table = new JTable(model);

    public RoomManagement(){
        setLayout(new BorderLayout());
        setBackground(Utils.LIGHT);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(Color.WHITE);
        JButton add = Utils.makeButton("Add Room");
        JButton edit = Utils.makeButton("Edit");
        JButton refresh = Utils.makeButton("Refresh");
        bar.add(add); bar.add(edit); bar.add(refresh);
        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        refresh.addActionListener(e -> load());

        load();
    }

    private void load(){
        model.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT room_no,block_name,capacity,occupied,(occupied<capacity) avail FROM rooms ORDER BY room_no");
             ResultSet rs = ps.executeQuery()){
            while (rs.next()){
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getBoolean(5)});
            }
        } catch (Exception ex){ Utils.showError(this, "Failed to load rooms: "+ex.getMessage()); }
    }

    private void onAdd(){
        JTextField roomNo = new JTextField(6);
        JTextField block = new JTextField(10);
        JTextField capacity = new JTextField(4);
        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Room No")); p.add(roomNo);
        p.add(new JLabel("Block")); p.add(block);
        p.add(new JLabel("Capacity")); p.add(capacity);
        int r = JOptionPane.showConfirmDialog(this, p, "Add Room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r==JOptionPane.OK_OPTION){
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("INSERT INTO rooms(room_no,block_name,capacity,occupied) VALUES (?,?,?,0)")){
                ps.setInt(1, Integer.parseInt(roomNo.getText().trim()));
                ps.setString(2, block.getText().trim());
                ps.setInt(3, Integer.parseInt(capacity.getText().trim()));
                ps.executeUpdate();
                load();
            } catch (Exception ex){ Utils.showError(this, "Add failed: "+ex.getMessage()); }
        }
    }

    private void onEdit(){
        int row = table.getSelectedRow();
        if (row<0){ Utils.showError(this, "Select a room."); return; }
        int roomNo = (Integer) model.getValueAt(row,0);
        JTextField capacity = new JTextField(String.valueOf(model.getValueAt(row,2)));
        JTextField occupied = new JTextField(String.valueOf(model.getValueAt(row,3)));
        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Capacity")); p.add(capacity);
        p.add(new JLabel("Occupied")); p.add(occupied);
        int r = JOptionPane.showConfirmDialog(this, p, "Edit Room "+roomNo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r==JOptionPane.OK_OPTION){
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE rooms SET capacity=?, occupied=LEAST(?, ?) WHERE room_no=?")){
                int cap = Integer.parseInt(capacity.getText().trim());
                int occ = Integer.parseInt(occupied.getText().trim());
                ps.setInt(1, cap);
                ps.setInt(2, occ);
                ps.setInt(3, cap);
                ps.setInt(4, roomNo);
                ps.executeUpdate();
                load();
            } catch (Exception ex){ Utils.showError(this, "Update failed: "+ex.getMessage()); }
        }
    }
}


