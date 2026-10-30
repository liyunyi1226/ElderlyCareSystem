package dao;
import model.HelpEvent;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class HelpEventDAO {
    public static List<HelpEvent> findAll() throws SQLException {
        String sql = "SELECT ev.*, d.device_no AS device_no, e.name AS elderly_name, " +
                     "o.order_no AS order_no, o.status AS order_status " +
                     "FROM help_event ev " +
                     "LEFT JOIN device d ON ev.device_id = d.id " +
                     "LEFT JOIN elderly e ON ev.elderly_id = e.id " +
                     "LEFT JOIN order_form o ON o.event_id = ev.id " +
                     "ORDER BY ev.event_time DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<HelpEvent> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static HelpEvent findById(int id) throws SQLException {
        String sql = "SELECT ev.*, d.device_no AS device_no, e.name AS elderly_name, " +
                     "o.order_no AS order_no, o.status AS order_status " +
                     "FROM help_event ev " +
                     "LEFT JOIN device d ON ev.device_id = d.id " +
                     "LEFT JOIN elderly e ON ev.elderly_id = e.id " +
                     "LEFT JOIN order_form o ON o.event_id = ev.id " +
                     "WHERE ev.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapEvent(rs) : null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(HelpEvent event) throws SQLException {
        String sql = "INSERT INTO help_event (device_id, elderly_id, event_type, location, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, event.getDeviceId());
            ps.setInt(2, event.getElderlyId());
            ps.setString(3, event.getEventType());
            ps.setString(4, event.getLocation());
            ps.setString(5, event.getStatus());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE help_event SET status = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql, status, id);
    }
    public static int update(HelpEvent event) throws SQLException {
        String sql = "UPDATE help_event SET event_type = ?, location = ?, status = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql, event.getEventType(), event.getLocation(), event.getStatus(), event.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM help_event WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static HelpEvent mapEvent(ResultSet rs) throws SQLException {
        HelpEvent ev = new HelpEvent();
        ev.setId(rs.getInt("id"));
        ev.setDeviceId(rs.getInt("device_id"));
        ev.setElderlyId(rs.getInt("elderly_id"));
        ev.setEventType(rs.getString("event_type"));
        ev.setEventTime(rs.getString("event_time"));
        ev.setLocation(rs.getString("location"));
        ev.setStatus(rs.getString("status"));
        ev.setCreateTime(rs.getString("create_time"));
        ev.setDeviceNo(rs.getString("device_no"));
        ev.setElderlyName(rs.getString("elderly_name"));
        ev.setOrderNo(rs.getString("order_no"));
        ev.setOrderStatus(rs.getString("order_status"));
        return ev;
    }
}
