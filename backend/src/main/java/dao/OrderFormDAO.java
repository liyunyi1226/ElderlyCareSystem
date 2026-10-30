package dao;
import model.OrderForm;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class OrderFormDAO {
    public static List<OrderForm> findAll(String status) throws SQLException {
        String sql = "SELECT o.*, e.name as elderly_name, e.phone as elderly_phone, e.address as elderly_address, " +
                     "d.name as doctor_name, u.real_name as creator_name, " +
                     "ev.event_type as event_type, ev.event_time as event_time " +
                     "FROM order_form o " +
                     "LEFT JOIN elderly e ON o.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON o.doctor_id = d.id " +
                     "LEFT JOIN user u ON o.creator_id = u.id " +
                     "LEFT JOIN help_event ev ON o.event_id = ev.id " +
                     "WHERE 1=1";
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql += " AND o.status = ?";
            params.add(status);
        }
        sql += " ORDER BY o.create_time DESC";
        return executeQuery(sql, params);
    }
    public static List<OrderForm> findByCreator(int creatorId, String status) throws SQLException {
        String sql = "SELECT o.*, e.name as elderly_name, e.phone as elderly_phone, e.address as elderly_address, " +
                     "d.name as doctor_name, u.real_name as creator_name, " +
                     "ev.event_type as event_type, ev.event_time as event_time " +
                     "FROM order_form o " +
                     "LEFT JOIN elderly e ON o.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON o.doctor_id = d.id " +
                     "LEFT JOIN user u ON o.creator_id = u.id " +
                     "LEFT JOIN help_event ev ON o.event_id = ev.id " +
                     "WHERE o.creator_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(creatorId);
        if (status != null && !status.trim().isEmpty()) {
            sql += " AND o.status = ?";
            params.add(status);
        }
        sql += " ORDER BY o.create_time DESC";
        return executeQuery(sql, params);
    }
    public static List<OrderForm> findByDoctor(int doctorId, String status) throws SQLException {
        String sql = "SELECT o.*, e.name as elderly_name, e.phone as elderly_phone, e.address as elderly_address, " +
                     "d.name as doctor_name, u.real_name as creator_name, " +
                     "ev.event_type as event_type, ev.event_time as event_time " +
                     "FROM order_form o " +
                     "LEFT JOIN elderly e ON o.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON o.doctor_id = d.id " +
                     "LEFT JOIN user u ON o.creator_id = u.id " +
                     "LEFT JOIN help_event ev ON o.event_id = ev.id " +
                     "WHERE o.doctor_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(doctorId);
        if (status != null && !status.trim().isEmpty()) {
            sql += " AND o.status = ?";
            params.add(status);
        }
        sql += " ORDER BY o.create_time DESC";
        return executeQuery(sql, params);
    }
    public static OrderForm findById(int id) throws SQLException {
        String sql = "SELECT o.*, e.name as elderly_name, e.phone as elderly_phone, e.address as elderly_address, " +
                     "d.name as doctor_name, u.real_name as creator_name, " +
                     "ev.event_type as event_type, ev.event_time as event_time " +
                     "FROM order_form o " +
                     "LEFT JOIN elderly e ON o.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON o.doctor_id = d.id " +
                     "LEFT JOIN user u ON o.creator_id = u.id " +
                     "LEFT JOIN help_event ev ON o.event_id = ev.id " +
                     "WHERE o.id = ?";
        List<Object> params = new ArrayList<>();
        params.add(id);
        List<OrderForm> list = executeQuery(sql, params);
        return list.isEmpty() ? null : list.get(0);
    }
    public static int insert(OrderForm order) throws SQLException {
        String sql = "INSERT INTO order_form (order_no, elderly_id, doctor_id, creator_id, event_id, visit_date, reason, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getOrderNo());
            ps.setInt(2, order.getElderlyId());
            ps.setObject(3, order.getDoctorId() > 0 ? order.getDoctorId() : null);
            ps.setInt(4, order.getCreatorId());
            ps.setObject(5, order.getEventId() > 0 ? order.getEventId() : null);
            ps.setObject(6, order.getVisitDate());
            ps.setObject(7, order.getReason());
            ps.setString(8, order.getStatus());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE order_form SET status = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql, status, id);
    }
    public static int assignDoctor(int id, int doctorId) throws SQLException {
        String sql = "UPDATE order_form SET doctor_id = ?, status = '待接收' WHERE id = ?";
        return DBUtil.executeUpdate(sql, doctorId, id);
    }
    public static int update(OrderForm order) throws SQLException {
        String sql = "UPDATE order_form SET elderly_id=?, doctor_id=?, visit_date=?, reason=? WHERE id=?";
        return DBUtil.executeUpdate(sql,
            order.getElderlyId(), order.getDoctorId() > 0 ? order.getDoctorId() : null,
            order.getVisitDate(), order.getReason(), order.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM order_form WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    public static List<Integer> findElderlyIdsByDoctor(int doctorId) throws SQLException {
        String sql = "SELECT DISTINCT elderly_id FROM order_form WHERE doctor_id = ? AND status != '已退回'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Integer> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doctorId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("elderly_id"));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    private static List<OrderForm> executeQuery(String sql, List<Object> params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<OrderForm> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    private static OrderForm mapOrder(ResultSet rs) throws SQLException {
        OrderForm o = new OrderForm();
        o.setId(rs.getInt("id"));
        o.setOrderNo(rs.getString("order_no"));
        o.setElderlyId(rs.getInt("elderly_id"));
        Object doctorId = rs.getObject("doctor_id");
        o.setDoctorId(doctorId != null ? ((Number) doctorId).intValue() : 0);
        o.setCreatorId(rs.getInt("creator_id"));
        Object eventId = rs.getObject("event_id");
        o.setEventId(eventId != null ? ((Number) eventId).intValue() : 0);
        o.setVisitDate(rs.getString("visit_date"));
        o.setReason(rs.getString("reason"));
        o.setStatus(rs.getString("status"));
        o.setCreateTime(rs.getString("create_time"));
        o.setUpdateTime(rs.getString("update_time"));
        o.setElderlyName(rs.getString("elderly_name"));
        o.setElderlyPhone(rs.getString("elderly_phone"));
        o.setElderlyAddress(rs.getString("elderly_address"));
        o.setDoctorName(rs.getString("doctor_name"));
        o.setCreatorName(rs.getString("creator_name"));
        o.setEventType(rs.getString("event_type"));
        o.setEventTime(rs.getString("event_time"));
        return o;
    }
}
