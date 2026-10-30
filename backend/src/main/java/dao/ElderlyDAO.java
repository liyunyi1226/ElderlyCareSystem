package dao;
import model.Elderly;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ElderlyDAO {
    public static List<Elderly> findAll(String keyword) throws SQLException {
        String sql = "SELECT e.*, c.name AS community_name FROM elderly e LEFT JOIN community c ON e.community_id = c.id WHERE 1=1";
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (e.name LIKE ? OR e.phone LIKE ? OR e.address LIKE ?)";
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql += " ORDER BY e.id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Elderly> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapElderly(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static Elderly findById(int id) throws SQLException {
        String sql = "SELECT e.*, c.name AS community_name FROM elderly e LEFT JOIN community c ON e.community_id = c.id WHERE e.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapElderly(rs);
            }
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(Elderly elderly) throws SQLException {
        String sql = "INSERT INTO elderly (name, gender, age, id_card, phone, address, emergency_contact, emergency_phone, chronic_diseases, allergies, current_illness, medications, body_status, community_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            elderly.getName(), elderly.getGender(), elderly.getAge(),
            elderly.getIdCard(), elderly.getPhone(), elderly.getAddress(),
            elderly.getEmergencyContact(), elderly.getEmergencyPhone(),
            elderly.getChronicDiseases(), elderly.getAllergies(),
            elderly.getCurrentIllness(), elderly.getMedications(), elderly.getBodyStatus(),
            elderly.getCommunityId() != null && elderly.getCommunityId() > 0 ? elderly.getCommunityId() : null);
    }
    public static int update(Elderly elderly) throws SQLException {
        String sql = "UPDATE elderly SET name=?, gender=?, age=?, id_card=?, phone=?, address=?, emergency_contact=?, emergency_phone=?, chronic_diseases=?, allergies=?, current_illness=?, medications=?, body_status=?, community_id=? WHERE id=?";
        return DBUtil.executeUpdate(sql,
            elderly.getName(), elderly.getGender(), elderly.getAge(),
            elderly.getIdCard(), elderly.getPhone(), elderly.getAddress(),
            elderly.getEmergencyContact(), elderly.getEmergencyPhone(),
            elderly.getChronicDiseases(), elderly.getAllergies(),
            elderly.getCurrentIllness(), elderly.getMedications(), elderly.getBodyStatus(),
            elderly.getCommunityId() != null && elderly.getCommunityId() > 0 ? elderly.getCommunityId() : null,
            elderly.getId());
    }
    public static int updateHealthFields(int id, String currentIllness, String medications, String bodyStatus) throws SQLException {
        String sql = "UPDATE elderly SET current_illness=?, medications=?, body_status=? WHERE id=?";
        return DBUtil.executeUpdate(sql, currentIllness, medications, bodyStatus, id);
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM elderly WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static Elderly mapElderly(ResultSet rs) throws SQLException {
        Elderly e = new Elderly();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setGender(rs.getString("gender"));
        e.setAge(rs.getInt("age"));
        e.setIdCard(rs.getString("id_card"));
        e.setPhone(rs.getString("phone"));
        e.setAddress(rs.getString("address"));
        e.setEmergencyContact(rs.getString("emergency_contact"));
        e.setEmergencyPhone(rs.getString("emergency_phone"));
        e.setChronicDiseases(rs.getString("chronic_diseases"));
        e.setAllergies(rs.getString("allergies"));
        e.setCurrentIllness(rs.getString("current_illness"));
        e.setMedications(rs.getString("medications"));
        e.setBodyStatus(rs.getString("body_status"));
        Object communityId = rs.getObject("community_id");
        e.setCommunityId(communityId != null ? ((Number) communityId).intValue() : null);
        e.setCommunityName(rs.getString("community_name"));
        e.setCreateTime(rs.getString("create_time"));
        e.setUpdateTime(rs.getString("update_time"));
        return e;
    }
}
