package dao;
import model.Device;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class DeviceDAO {
    public static List<Device> findAll() throws SQLException {
        String sql = "SELECT d.*, e.name AS elderly_name " +
                     "FROM device d " +
                     "LEFT JOIN elderly e ON d.elderly_id = e.id " +
                     "ORDER BY d.id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Device> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDevice(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static Device findById(int id) throws SQLException {
        String sql = "SELECT d.*, e.name AS elderly_name " +
                     "FROM device d " +
                     "LEFT JOIN elderly e ON d.elderly_id = e.id " +
                     "WHERE d.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapDevice(rs) : null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(Device device) throws SQLException {
        String sql = "INSERT INTO device (device_no, device_type, install_time, status, elderly_id) VALUES (?, ?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            device.getDeviceNo(), device.getDeviceType(), device.getInstallTime(),
            device.getStatus(), device.getElderlyId() > 0 ? device.getElderlyId() : null);
    }
    public static int update(Device device) throws SQLException {
        String sql = "UPDATE device SET device_no = ?, device_type = ?, install_time = ?, status = ?, elderly_id = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql,
            device.getDeviceNo(), device.getDeviceType(), device.getInstallTime(),
            device.getStatus(), device.getElderlyId() > 0 ? device.getElderlyId() : null, device.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM device WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static Device mapDevice(ResultSet rs) throws SQLException {
        Device d = new Device();
        d.setId(rs.getInt("id"));
        d.setDeviceNo(rs.getString("device_no"));
        d.setDeviceType(rs.getString("device_type"));
        d.setInstallTime(rs.getString("install_time"));
        d.setStatus(rs.getString("status"));
        Object elderlyId = rs.getObject("elderly_id");
        d.setElderlyId(elderlyId != null ? ((Number) elderlyId).intValue() : 0);
        d.setCreateTime(rs.getString("create_time"));
        d.setElderlyName(rs.getString("elderly_name"));
        return d;
    }
}
