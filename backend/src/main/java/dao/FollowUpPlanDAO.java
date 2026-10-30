package dao;
import model.FollowUpPlan;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class FollowUpPlanDAO {
    public static List<FollowUpPlan> findAll() throws SQLException {
        String sql = "SELECT f.*, e.name AS elderly_name, c.name AS community_name, u.real_name AS creator_name " +
                     "FROM follow_up_plan f " +
                     "LEFT JOIN elderly e ON f.elderly_id = e.id " +
                     "LEFT JOIN community c ON f.community_id = c.id " +
                     "LEFT JOIN user u ON f.creator_id = u.id " +
                     "ORDER BY f.id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FollowUpPlan> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPlan(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static FollowUpPlan findById(int id) throws SQLException {
        String sql = "SELECT f.*, e.name AS elderly_name, c.name AS community_name, u.real_name AS creator_name " +
                     "FROM follow_up_plan f " +
                     "LEFT JOIN elderly e ON f.elderly_id = e.id " +
                     "LEFT JOIN community c ON f.community_id = c.id " +
                     "LEFT JOIN user u ON f.creator_id = u.id " +
                     "WHERE f.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapPlan(rs) : null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    public static int insert(FollowUpPlan plan) throws SQLException {
        String sql = "INSERT INTO follow_up_plan (elderly_id, community_id, creator_id, plan_type, cycle_days, start_date, next_visit_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            plan.getElderlyId(),
            plan.getCommunityId() > 0 ? plan.getCommunityId() : null,
            plan.getCreatorId(),
            plan.getPlanType(), plan.getCycleDays(),
            plan.getStartDate(), plan.getNextVisitDate(), plan.getStatus());
    }
    public static int update(FollowUpPlan plan) throws SQLException {
        String sql = "UPDATE follow_up_plan SET elderly_id = ?, community_id = ?, plan_type = ?, cycle_days = ?, " +
                     "start_date = ?, next_visit_date = ?, status = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql,
            plan.getElderlyId(),
            plan.getCommunityId() > 0 ? plan.getCommunityId() : null,
            plan.getPlanType(), plan.getCycleDays(),
            plan.getStartDate(), plan.getNextVisitDate(), plan.getStatus(), plan.getId());
    }
    public static int updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE follow_up_plan SET status = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql, status, id);
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM follow_up_plan WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static FollowUpPlan mapPlan(ResultSet rs) throws SQLException {
        FollowUpPlan f = new FollowUpPlan();
        f.setId(rs.getInt("id"));
        f.setElderlyId(rs.getInt("elderly_id"));
        Object communityId = rs.getObject("community_id");
        f.setCommunityId(communityId != null ? ((Number) communityId).intValue() : 0);
        f.setCreatorId(rs.getInt("creator_id"));
        f.setPlanType(rs.getString("plan_type"));
        Object cycleDays = rs.getObject("cycle_days");
        f.setCycleDays(cycleDays != null ? ((Number) cycleDays).intValue() : null);
        f.setStartDate(rs.getString("start_date"));
        f.setNextVisitDate(rs.getString("next_visit_date"));
        f.setStatus(rs.getString("status"));
        f.setCreateTime(rs.getString("create_time"));
        f.setElderlyName(rs.getString("elderly_name"));
        f.setCommunityName(rs.getString("community_name"));
        f.setCreatorName(rs.getString("creator_name"));
        return f;
    }
}
