package dao;
import model.Doctor;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class DoctorDAO {
    private static final String SELECT_COLUMNS =
        "SELECT d.*, c.name AS community_name FROM doctor d " +
        "LEFT JOIN community c ON d.community_id = c.id ";
    public static List<Doctor> findAll() throws SQLException {
        String sql = SELECT_COLUMNS + "ORDER BY d.id";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Doctor> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDoctor(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static Doctor findById(int id) throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE d.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapDoctor(rs);
            }
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static Doctor findByUserId(int userId) throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE d.user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapDoctor(rs);
            }
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(Doctor doctor) throws SQLException {
        // 不再维护医生在线状态（无法真实判断），status 列使用数据库默认值
        String sql = "INSERT INTO doctor (user_id, name, gender, phone, specialization, hospital, community_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            doctor.getUserId(), doctor.getName(), doctor.getGender(),
            doctor.getPhone(), doctor.getSpecialization(), doctor.getHospital(),
            doctor.getCommunityId());
    }
    public static int update(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctor SET name=?, gender=?, phone=?, specialization=?, hospital=?, community_id=? WHERE id=?";
        return DBUtil.executeUpdate(sql,
            doctor.getName(), doctor.getGender(), doctor.getPhone(),
            doctor.getSpecialization(), doctor.getHospital(), doctor.getCommunityId(),
            doctor.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM doctor WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static Doctor mapDoctor(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("id"));
        d.setUserId(rs.getInt("user_id"));
        d.setName(rs.getString("name"));
        d.setGender(rs.getString("gender"));
        d.setPhone(rs.getString("phone"));
        d.setSpecialization(rs.getString("specialization"));
        d.setHospital(rs.getString("hospital"));
        Object cid = rs.getObject("community_id");
        d.setCommunityId(cid != null ? ((Number) cid).intValue() : null);
        d.setCommunityName(rs.getString("community_name"));
        d.setStatus(rs.getString("status"));
        d.setCreateTime(rs.getString("create_time"));
        return d;
    }
}
