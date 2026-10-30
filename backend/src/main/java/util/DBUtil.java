package util;

import javax.sql.DataSource;
import java.sql.*;

public class DBUtil {

    private static volatile DataSource dataSource;

    private DBUtil() {}

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据源尚未初始化，请确认 Spring 容器已启动");
        }
        return dataSource.getConnection();
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (ps != null) ps.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }

    public static void close(Connection conn, PreparedStatement ps) {
        close(conn, ps, null);
    }

    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParams(ps, params);
            return ps.executeUpdate();
        } finally {
            close(conn, ps);
        }
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }
}
