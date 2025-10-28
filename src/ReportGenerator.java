import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;

public class ReportGenerator extends JPanel {
    public ReportGenerator(){
        setLayout(new GridLayout(0,1,8,8));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        setBackground(Utils.LIGHT);
        add(makeButton("Export Student List", this::exportStudents));
        add(makeButton("Export Room Occupancy", this::exportRooms));
        add(makeButton("Export Fee Summary", this::exportFees));
        add(makeButton("Export Complaint Summary", this::exportComplaints));
    }

    private Component makeButton(String text, Runnable r){
        JButton b = Utils.makeButton(text);
        b.addActionListener(e -> r.run());
        return b;
    }

    private void exportStudents(){
        exportQueryToCsv("SELECT * FROM students", "students_report.csv");
    }
    private void exportRooms(){
        exportQueryToCsv("SELECT room_no, block_name, capacity, occupied, (occupied<capacity) AS available FROM rooms", "rooms_report.csv");
    }
    private void exportFees(){
        exportQueryToCsv("SELECT status, COUNT(*) count, SUM(amount) total FROM fees GROUP BY status", "fees_summary.csv");
    }
    private void exportComplaints(){
        exportQueryToCsv("SELECT status, COUNT(*) count FROM complaints GROUP BY status", "complaints_summary.csv");
    }

    private void exportQueryToCsv(String sql, String defaultName){
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File(defaultName));
        if (fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
                Utils.exportResultSetToCsv(rs, fc.getSelectedFile().getAbsolutePath());
                Utils.showInfo(this, "Exported");
            } catch (Exception ex){ Utils.showError(this, "Export failed: "+ex.getMessage()); }
        }
    }
}


