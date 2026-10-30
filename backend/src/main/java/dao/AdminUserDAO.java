package dao;

import com.elderlycare.common.BusinessException;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * 管理后台账号创建：在同一事务内写入 user 表，并按角色同步创建关联档案。
 * doctor           -> doctor 表（社区可空）
 * community_worker -> community_staff 表（必须指定社区）
 * system_admin     -> 仅 user 表
 */
public class AdminUserDAO {

    private AdminUserDAO() {}

    /**
     * @return 新账号ID
     */
    public static int insertWithProfile(String username, String password, String realName,
                                        String role, String phone, Integer communityId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user (username, password, real_name, role, phone) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, realName);
                ps.setString(4, role);
                ps.setString(5, phone);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    userId = rs.next() ? rs.getInt(1) : 0;
                }
            }

            if ("doctor".equals(role)) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO doctor (user_id, name, phone, community_id) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setString(2, realName);
                    ps.setString(3, phone);
                    if (communityId == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, communityId);
                    }
                    ps.executeUpdate();
                }
            } else if ("community_worker".equals(role)) {
                if (communityId == null) {
                    throw new BusinessException("社区工作人员必须选择所属社区");
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO community_staff (community_id, user_id, start_date, position) "
                                + "VALUES (?, ?, CURDATE(), '社区工作人员')")) {
                    ps.setInt(1, communityId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return userId;
        } catch (BusinessException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            // 1062：唯一键冲突（用户名重复）；1452：外键不存在（社区ID无效）
            if (e.getErrorCode() == 1062) {
                throw new BusinessException("用户名已存在，请更换后重试");
            }
            if (e.getErrorCode() == 1452) {
                throw new BusinessException("所属社区不存在，请检查社区ID");
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }
}
