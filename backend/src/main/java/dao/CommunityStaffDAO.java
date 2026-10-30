package dao;
import model.CommunityStaff;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CommunityStaffDAO {
    public static List<CommunityStaff> findAll() throws SQLException {
        String sql = "SELECT s.*, c.name AS community_name, u.username AS user_name, u.real_name AS real_name, u.role AS role " +
                     "FROM community_staff s " +
                     "LEFT JOIN community c ON s.community_id = c.id " +
                     "LEFT JOIN user u ON s.user_id = u.id " +
                     "ORDER BY s.id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CommunityStaff> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapStaff(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static CommunityStaff findByUserId(int userId) throws SQLException {
        String sql = "SELECT s.*, c.name AS community_name, u.username AS user_name, u.real_name AS real_name, u.role AS role " +
                     "FROM community_staff s " +
                     "LEFT JOIN community c ON s.community_id = c.id " +
                     "LEFT JOIN user u ON s.user_id = u.id " +
                     "WHERE s.user_id = ? LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            return rs.next() ? mapStaff(rs) : null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(CommunityStaff staff) throws SQLException {
        String sql = "INSERT INTO community_staff (community_id, user_id, start_date, position) VALUES (?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            staff.getCommunityId(), staff.getUserId(), staff.getStartDate(), staff.getPosition());
    }
    public static int update(CommunityStaff staff) throws SQLException {
        String sql = "UPDATE community_staff SET community_id = ?, user_id = ?, start_date = ?, position = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql,
            staff.getCommunityId(), staff.getUserId(), staff.getStartDate(), staff.getPosition(), staff.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM community_staff WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static CommunityStaff mapStaff(ResultSet rs) throws SQLException {
        CommunityStaff s = new CommunityStaff();
        s.setId(rs.getInt("id"));
        s.setCommunityId(rs.getInt("community_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setStartDate(rs.getString("start_date"));
        s.setPosition(rs.getString("position"));
        s.setCreateTime(rs.getString("create_time"));
        s.setCommunityName(rs.getString("community_name"));
        s.setUserName(rs.getString("user_name"));
        s.setRealName(rs.getString("real_name"));
        s.setRole(rs.getString("role"));
        return s;
    }
}
