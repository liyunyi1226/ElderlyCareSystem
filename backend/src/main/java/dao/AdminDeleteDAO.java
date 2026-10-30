package dao;

import com.elderlycare.common.BusinessException;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理后台统一级联删除。
 * 数据库中的外键均为 RESTRICT，直接删除父表记录会触发 ERROR 1451，
 * 因此这里在同一个事务内按"先子后父"的顺序清理关联数据：
 * 可空外键采用置空解绑，NOT NULL 外键采用连带删除。
 */
public class AdminDeleteDAO {

    private AdminDeleteDAO() {}

    /**
     * 级联删除指定表的一条记录。
     *
     * @return 连带处理（删除或解绑）的关联数据条数
     */
    public static int cascadeDelete(String table, int id) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int related;
            switch (table) {
                // 叶子表：没有被其他表引用，直接删除
                case "fallrecords":
                    exec(conn, "DELETE FROM fall_record WHERE id = ?", id);
                    related = 0;
                    break;
                case "visits":
                    exec(conn, "DELETE FROM visit_record WHERE id = ?", id);
                    related = 0;
                    break;
                case "followups":
                    related = 0;
                    exec(conn, "DELETE FROM follow_up_plan WHERE id = ?", id);
                    break;
                case "staff":
                    related = 0;
                    exec(conn, "DELETE FROM community_staff WHERE id = ?", id);
                    break;
                // 父表：按外键依赖逆序级联
                case "orders":
                    related = deleteOrder(conn, id);
                    break;
                case "events":
                    related = deleteEvent(conn, id);
                    break;
                case "devices":
                    related = deleteDevice(conn, id);
                    break;
                case "doctors":
                    related = deleteDoctorById(conn, id);
                    break;
                case "elderly":
                    related = deleteElderly(conn, id);
                    break;
                case "communities":
                    related = deleteCommunity(conn, id);
                    break;
                case "users":
                    related = deleteUser(conn, id);
                    break;
                default:
                    throw new BusinessException("无效的表名");
            }

            conn.commit();
            return Math.max(related, 0);
        } catch (BusinessException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            if (e instanceof SQLIntegrityConstraintViolationException) {
                throw new BusinessException("该数据仍被其他业务记录引用，请先处理或删除相关联的数据后再试");
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

    /** 删除工单：先删其巡访记录、摔倒处置记录，再删工单 */
    private static int deleteOrder(Connection conn, int id) throws SQLException {
        int n = 0;
        n += exec(conn, "DELETE FROM fall_record WHERE order_id = ?", id);
        n += exec(conn, "DELETE FROM visit_record WHERE order_id = ?", id);
        n += exec(conn, "DELETE FROM order_form WHERE id = ?", id);
        return n - 1;
    }

    /** 删除求助事件：先删摔倒处置记录，工单解除事件关联，再删事件 */
    private static int deleteEvent(Connection conn, int id) throws SQLException {
        int n = 0;
        n += exec(conn, "DELETE FROM fall_record WHERE event_id = ?", id);
        n += exec(conn, "UPDATE order_form SET event_id = NULL WHERE event_id = ?", id);
        n += exec(conn, "DELETE FROM help_event WHERE id = ?", id);
        return n - 1;
    }

    /** 删除设备：级联清理设备产生的全部求助事件，再删设备 */
    private static int deleteDevice(Connection conn, int id) throws SQLException {
        int n = 0;
        n += exec(conn,
                "DELETE FROM fall_record WHERE event_id IN (SELECT he.id FROM help_event he WHERE he.device_id = ?)", id);
        n += exec(conn,
                "UPDATE order_form SET event_id = NULL WHERE event_id IN (SELECT he.id FROM help_event he WHERE he.device_id = ?)", id);
        n += exec(conn, "DELETE FROM help_event WHERE device_id = ?", id);
        n += exec(conn, "DELETE FROM device WHERE id = ?", id);
        return n - 1;
    }

    /** 删除医生：删其巡访/处置记录，工单医生置空（变为待分配），再删医生资料 */
    private static int deleteDoctorById(Connection conn, int doctorId) throws SQLException {
        int n = 0;
        n += exec(conn, "DELETE FROM fall_record WHERE doctor_id = ?", doctorId);
        n += exec(conn, "DELETE FROM visit_record WHERE doctor_id = ?", doctorId);
        n += exec(conn, "UPDATE order_form SET doctor_id = NULL WHERE doctor_id = ?", doctorId);
        n += exec(conn, "DELETE FROM doctor WHERE id = ?", doctorId);
        return n - 1;
    }

    /** 删除老人：连带清理其全部业务数据，设备解绑，最后删档案 */
    private static int deleteElderly(Connection conn, int id) throws SQLException {
        int n = 0;
        n += exec(conn, "DELETE FROM fall_record WHERE elderly_id = ?", id);
        n += exec(conn, "DELETE FROM visit_record WHERE elderly_id = ?", id);
        n += exec(conn,
                "UPDATE order_form SET event_id = NULL WHERE event_id IN (SELECT he.id FROM help_event he WHERE he.elderly_id = ?)", id);
        n += exec(conn, "DELETE FROM help_event WHERE elderly_id = ?", id);
        n += exec(conn, "DELETE FROM follow_up_plan WHERE elderly_id = ?", id);
        n += exec(conn, "DELETE FROM order_form WHERE elderly_id = ?", id);
        n += exec(conn, "UPDATE device SET elderly_id = NULL WHERE elderly_id = ?", id);
        n += exec(conn, "DELETE FROM elderly WHERE id = ?", id);
        return n - 1;
    }

    /** 删除社区：删除任职记录，解除老人/医生/随访的社区归属，档案保留 */
    private static int deleteCommunity(Connection conn, int id) throws SQLException {
        int n = 0;
        n += exec(conn, "DELETE FROM community_staff WHERE community_id = ?", id);
        n += exec(conn, "UPDATE elderly SET community_id = NULL WHERE community_id = ?", id);
        n += exec(conn, "UPDATE doctor SET community_id = NULL WHERE community_id = ?", id);
        n += exec(conn, "UPDATE follow_up_plan SET community_id = NULL WHERE community_id = ?", id);
        n += exec(conn, "DELETE FROM community WHERE id = ?", id);
        return n - 1;
    }

    /** 删除登录账号：连带清理医生资料/社区任职/创建的工单与随访计划，最后删账号 */
    private static int deleteUser(Connection conn, int userId) throws SQLException {
        int n = 0;
        // 若该账号是医生，按医生级联规则清理医生资料
        List<Integer> doctorIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM doctor WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doctorIds.add(rs.getInt(1));
                }
            }
        }
        for (Integer doctorId : doctorIds) {
            n += deleteDoctorById(conn, doctorId) + 1;
        }
        // 社区任职
        n += exec(conn, "DELETE FROM community_staff WHERE user_id = ?", userId);
        // 创建的工单：先删工单下的处置/巡访记录，再删工单
        n += exec(conn,
                "DELETE FROM fall_record WHERE order_id IN (SELECT o.id FROM order_form o WHERE o.creator_id = ?)", userId);
        n += exec(conn,
                "DELETE FROM visit_record WHERE order_id IN (SELECT o.id FROM order_form o WHERE o.creator_id = ?)", userId);
        n += exec(conn, "DELETE FROM order_form WHERE creator_id = ?", userId);
        // 创建的随访计划
        n += exec(conn, "DELETE FROM follow_up_plan WHERE creator_id = ?", userId);
        // 账号本身
        n += exec(conn, "DELETE FROM user WHERE id = ?", userId);
        return n - 1;
    }

    private static int exec(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
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
