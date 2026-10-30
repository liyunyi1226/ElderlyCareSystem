package dao;
import model.Community;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CommunityDAO {
    public static List<Community> findAll() throws SQLException {
        String sql = "SELECT * FROM community ORDER BY id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Community> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapCommunity(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static Community findById(int id) throws SQLException {
        String sql = "SELECT * FROM community WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapCommunity(rs) : null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(Community community) throws SQLException {
        String sql = "INSERT INTO community (name, address, phone) VALUES (?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            community.getName(), community.getAddress(), community.getPhone());
    }
    public static int update(Community community) throws SQLException {
        String sql = "UPDATE community SET name = ?, address = ?, phone = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql,
            community.getName(), community.getAddress(), community.getPhone(), community.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM community WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static Community mapCommunity(ResultSet rs) throws SQLException {
        Community c = new Community();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setAddress(rs.getString("address"));
        c.setPhone(rs.getString("phone"));
        c.setCreateTime(rs.getString("create_time"));
        return c;
    }
}
