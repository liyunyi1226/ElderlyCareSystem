package dao;
import model.User;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO {
    public static User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ? AND password = ? AND status = 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static User findById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM user ORDER BY id";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapUser(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static List<User> findByRole(String role) throws SQLException {
        String sql = "SELECT * FROM user WHERE role = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapUser(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(User user) throws SQLException {
        String sql = "INSERT INTO user (username, password, real_name, role, phone) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPhone());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int update(User user, String password) throws SQLException {
        String sql = "UPDATE user SET real_name=?, phone=?, status=?, password=? WHERE id=?";
        return DBUtil.executeUpdate(sql,
            user.getRealName(), user.getPhone(), user.getStatus(), password, user.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setRole(rs.getString("role"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getInt("status"));
        user.setCreateTime(rs.getString("create_time"));
        user.setUpdateTime(rs.getString("update_time"));
        return user;
    }
}
