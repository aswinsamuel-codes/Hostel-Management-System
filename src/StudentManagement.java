import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentManagement extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID","Name","Reg No","Department","Year","Room","Phone","Email"
    }, 0) {
        public boolean isCellEditable(int r, int c){ return false; }
    };
    private final JTable table = new JTable(model);

    public StudentManagement() {
        setLayout(new BorderLayout());
        setBackground(Utils.LIGHT);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);
        JButton addBtn = Utils.makeButton("Add");
        JButton editBtn = Utils.makeButton("Edit");
        JButton delBtn = Utils.makeButton("Delete");
        JButton refreshBtn = Utils.makeButton("Refresh");
        JButton exportBtn = Utils.makeButton("Export CSV");
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(delBtn); toolbar.add(refreshBtn); toolbar.add(exportBtn);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        delBtn.addActionListener(e -> onDelete());
        refreshBtn.addActionListener(e -> loadStudents());
        exportBtn.addActionListener(e -> onExport());

        loadStudents();
    }

    private void loadStudents() {
        model.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT student_id,name,reg_no,department,year,room_no,phone,email FROM students ORDER BY student_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getString(7), rs.getString(8)
                });
            }
        } catch (Exception ex) {
            Utils.showError(this, "Failed to load students: " + ex.getMessage());
        }
    }

    private void onExport() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT * FROM students");
                 ResultSet rs = ps.executeQuery()) {
                Utils.exportResultSetToCsv(rs, fc.getSelectedFile().getAbsolutePath());
                Utils.showInfo(this, "Exported successfully.");
            } catch (Exception ex) {
                Utils.showError(this, "Export failed: " + ex.getMessage());
            }
        }
    }

    private void onAdd() {
        StudentForm form = new StudentForm();
        int r = JOptionPane.showConfirmDialog(this, form.getComponent(), "Add Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            try (Connection c = DBConnection.getConnection()) {
                c.setAutoCommit(false);
                try {
                    Integer allocatedRoom = allocateRoom(c);
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO students(name,reg_no,department,year,room_no,phone,email,address,guardian_name,guardian_phone) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                        ps.setString(1, form.name.getText().trim());
                        ps.setString(2, form.regNo.getText().trim());
                        ps.setString(3, form.department.getText().trim());
                        ps.setInt(4, Integer.parseInt(form.year.getText().trim()));
                        if (allocatedRoom != null) ps.setInt(5, allocatedRoom); else ps.setNull(5, Types.INTEGER);
                        ps.setString(6, form.phone.getText().trim());
                        ps.setString(7, form.email.getText().trim());
                        ps.setString(8, form.address.getText());
                        ps.setString(9, form.guardianName.getText().trim());
                        ps.setString(10, form.guardianPhone.getText().trim());
                        ps.executeUpdate();
                    }
                    if (allocatedRoom != null) incrementRoom(c, allocatedRoom, 1);
                    c.commit();
                    Utils.showInfo(this, "Student added.");
                    loadStudents();
                } catch (Exception ex) {
                    c.rollback();
                    throw ex;
                } finally {
                    c.setAutoCommit(true);
                }
            } catch (Exception ex) {
                Utils.showError(this, "Add failed: " + ex.getMessage());
            }
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { Utils.showError(this, "Select a student."); return; }
        int id = (Integer) model.getValueAt(row, 0);
        StudentForm form = new StudentForm();
        form.name.setText(String.valueOf(model.getValueAt(row,1)));
        form.regNo.setText(String.valueOf(model.getValueAt(row,2)));
        form.department.setText(String.valueOf(model.getValueAt(row,3)));
        form.year.setText(String.valueOf(model.getValueAt(row,4)));
        form.phone.setText(String.valueOf(model.getValueAt(row,6)));
        form.email.setText(String.valueOf(model.getValueAt(row,7)));
        int r = JOptionPane.showConfirmDialog(this, form.getComponent(), "Edit Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            try (Connection c = DBConnection.getConnection()) {
                c.setAutoCommit(false);
                try {
                    Integer oldRoom = null;
                    try (PreparedStatement ps = c.prepareStatement("SELECT room_no FROM students WHERE student_id=?")) {
                        ps.setInt(1, id);
                        try (ResultSet rs = ps.executeQuery()) { if (rs.next()) oldRoom = (Integer) rs.getObject(1); }
                    }
                    Integer newRoom = oldRoom;
                    if (Utils.confirm(this, "Reallocate room automatically?")) {
                        newRoom = allocateRoom(c);
                    }
                    try (PreparedStatement ps = c.prepareStatement("UPDATE students SET name=?, reg_no=?, department=?, year=?, room_no=?, phone=?, email=? WHERE student_id=?")) {
                        ps.setString(1, form.name.getText().trim());
                        ps.setString(2, form.regNo.getText().trim());
                        ps.setString(3, form.department.getText().trim());
                        ps.setInt(4, Integer.parseInt(form.year.getText().trim()));
                        if (newRoom != null) ps.setInt(5, newRoom); else ps.setNull(5, Types.INTEGER);
                        ps.setString(6, form.phone.getText().trim());
                        ps.setString(7, form.email.getText().trim());
                        ps.setInt(8, id);
                        ps.executeUpdate();
                    }
                    if (oldRoom != null && !oldRoom.equals(newRoom)) incrementRoom(c, oldRoom, -1);
                    if (newRoom != null && !newRoom.equals(oldRoom)) incrementRoom(c, newRoom, 1);
                    c.commit();
                    Utils.showInfo(this, "Student updated.");
                    loadStudents();
                } catch (Exception ex) {
                    c.rollback();
                    throw ex;
                } finally { c.setAutoCommit(true);}            
            } catch (Exception ex) {
                Utils.showError(this, "Update failed: " + ex.getMessage());
            }
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { Utils.showError(this, "Select a student."); return; }
        int id = (Integer) model.getValueAt(row, 0);
        if (!Utils.confirm(this, "Delete selected student?")) return;
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Integer room = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT room_no FROM students WHERE student_id=?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) room = (Integer) rs.getObject(1); }
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                if (room != null) incrementRoom(c, room, -1);
                c.commit();
                loadStudents();
            } catch (Exception ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true);}            
        } catch (Exception ex) {
            Utils.showError(this, "Delete failed: " + ex.getMessage());
        }
    }

    private Integer allocateRoom(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT room_no FROM rooms WHERE occupied < capacity ORDER BY occupied ASC, room_no ASC LIMIT 1"); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }

    private void incrementRoom(Connection c, int roomNo, int delta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE rooms SET occupied = LEAST(capacity, GREATEST(0, occupied + ?)) WHERE room_no=?")) {
            ps.setInt(1, delta);
            ps.setInt(2, roomNo);
            ps.executeUpdate();
        }
    }

    static class StudentForm {
        JTextField name = new JTextField(20);
        JTextField regNo = new JTextField(20);
        JTextField department = new JTextField(20);
        JTextField year = new JTextField(5);
        JTextField phone = new JTextField(15);
        JTextField email = new JTextField(20);
        JTextArea address = new JTextArea(3, 20);
        JTextField guardianName = new JTextField(20);
        JTextField guardianPhone = new JTextField(15);

        JComponent getComponent() {
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4,4,4,4); g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
            int r=0;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Name"), g); g.gridx=1; p.add(name, g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Reg No"), g); g.gridx=1; p.add(regNo, g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Department"), g); g.gridx=1; p.add(department, g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Year"), g); g.gridx=1; p.add(year, g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Phone"), g); g.gridx=1; p.add(phone, g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Email"), g); g.gridx=1; p.add(email, g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Address"), g); g.gridx=1; p.add(new JScrollPane(address), g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Guardian Name"), g); g.gridx=1; p.add(guardianName, g); r++;
            g.gridx=0; g.gridy=r; p.add(new JLabel("Guardian Phone"), g); g.gridx=1; p.add(guardianPhone, g); r++;
            return p;
        }
    }
}


