package dao;
import model.VisitRecord;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class VisitRecordDAO {
    public static List<VisitRecord> findAll() throws SQLException {
        String sql = "SELECT vr.*, o.order_no, e.name as elderly_name, d.name as doctor_name " +
                     "FROM visit_record vr " +
                     "LEFT JOIN order_form o ON vr.order_id = o.id " +
                     "LEFT JOIN elderly e ON vr.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON vr.doctor_id = d.id " +
                     "ORDER BY vr.create_time DESC";
        return executeQuery(sql, new ArrayList<>());
    }
    public static List<VisitRecord> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT vr.*, o.order_no, e.name as elderly_name, d.name as doctor_name " +
                     "FROM visit_record vr " +
                     "LEFT JOIN order_form o ON vr.order_id = o.id " +
                     "LEFT JOIN elderly e ON vr.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON vr.doctor_id = d.id " +
                     "WHERE vr.order_id = ? ORDER BY vr.create_time DESC";
        List<Object> params = new ArrayList<>();
        params.add(orderId);
        return executeQuery(sql, params);
    }
    public static List<VisitRecord> findByElderlyId(int elderlyId) throws SQLException {
        String sql = "SELECT vr.*, o.order_no, e.name as elderly_name, d.name as doctor_name " +
                     "FROM visit_record vr " +
                     "LEFT JOIN order_form o ON vr.order_id = o.id " +
                     "LEFT JOIN elderly e ON vr.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON vr.doctor_id = d.id " +
                     "WHERE vr.elderly_id = ? ORDER BY vr.create_time DESC";
        List<Object> params = new ArrayList<>();
        params.add(elderlyId);
        return executeQuery(sql, params);
    }
    public static VisitRecord findLatestByElderlyId(int elderlyId) throws SQLException {
        List<VisitRecord> list = findByElderlyId(elderlyId);
        return list.isEmpty() ? null : list.get(0);
    }
    public static List<VisitRecord> findByDoctor(int doctorId) throws SQLException {
        String sql = "SELECT vr.*, o.order_no, e.name as elderly_name, d.name as doctor_name " +
                     "FROM visit_record vr " +
                     "LEFT JOIN order_form o ON vr.order_id = o.id " +
                     "LEFT JOIN elderly e ON vr.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON vr.doctor_id = d.id " +
                     "WHERE vr.doctor_id = ? ORDER BY vr.create_time DESC";
        List<Object> params = new ArrayList<>();
        params.add(doctorId);
        return executeQuery(sql, params);
    }
    public static List<VisitRecord> findByCreator(int creatorId) throws SQLException {
        String sql = "SELECT vr.*, o.order_no, e.name as elderly_name, d.name as doctor_name " +
                     "FROM visit_record vr " +
                     "LEFT JOIN order_form o ON vr.order_id = o.id " +
                     "LEFT JOIN elderly e ON vr.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON vr.doctor_id = d.id " +
                     "WHERE o.creator_id = ? ORDER BY vr.create_time DESC";
        List<Object> params = new ArrayList<>();
        params.add(creatorId);
        return executeQuery(sql, params);
    }
    public static VisitRecord findById(int id) throws SQLException {
        String sql = "SELECT vr.*, o.order_no, e.name as elderly_name, d.name as doctor_name " +
                     "FROM visit_record vr " +
                     "LEFT JOIN order_form o ON vr.order_id = o.id " +
                     "LEFT JOIN elderly e ON vr.elderly_id = e.id " +
                     "LEFT JOIN doctor d ON vr.doctor_id = d.id " +
                     "WHERE vr.id = ?";
        List<Object> params = new ArrayList<>();
        params.add(id);
        List<VisitRecord> list = executeQuery(sql, params);
        return list.isEmpty() ? null : list.get(0);
    }
    public static int insert(VisitRecord record) throws SQLException {
        String sql = "INSERT INTO visit_record (order_id, elderly_id, doctor_id, vital_signs, bp_systolic, bp_diastolic, heart_rate, blood_glucose, body_condition, daily_life, medication_situation, subjective_symptoms, ai_advice) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            record.getOrderId(), record.getElderlyId(), record.getDoctorId(),
            record.getVitalSigns(), record.getBpSystolic(), record.getBpDiastolic(), record.getHeartRate(), record.getBloodGlucose(),
            record.getBodyCondition(), record.getDailyLife(),
            record.getMedicationSituation(), record.getSubjectiveSymptoms(), record.getAiAdvice());
    }
    public static int update(VisitRecord record) throws SQLException {
        String sql = "UPDATE visit_record SET vital_signs=?, bp_systolic=?, bp_diastolic=?, heart_rate=?, blood_glucose=?, body_condition=?, daily_life=?, medication_situation=?, subjective_symptoms=?, ai_advice=? WHERE id=?";
        return DBUtil.executeUpdate(sql,
            record.getVitalSigns(), record.getBpSystolic(), record.getBpDiastolic(), record.getHeartRate(), record.getBloodGlucose(),
            record.getBodyCondition(), record.getDailyLife(),
            record.getMedicationSituation(), record.getSubjectiveSymptoms(), record.getAiAdvice(),
            record.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM visit_record WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static List<VisitRecord> executeQuery(String sql, List<Object> params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<VisitRecord> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRecord(rs));
            }
            return list;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    private static VisitRecord mapRecord(ResultSet rs) throws SQLException {
        VisitRecord r = new VisitRecord();
        r.setId(rs.getInt("id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setElderlyId(rs.getInt("elderly_id"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setVitalSigns(rs.getString("vital_signs"));
        r.setBpSystolic((Integer) rs.getObject("bp_systolic"));
        r.setBpDiastolic((Integer) rs.getObject("bp_diastolic"));
        r.setHeartRate((Integer) rs.getObject("heart_rate"));
        Object bg = rs.getObject("blood_glucose");
        r.setBloodGlucose(bg != null ? ((java.math.BigDecimal) bg).doubleValue() : null);
        r.setBodyCondition(rs.getString("body_condition"));
        r.setDailyLife(rs.getString("daily_life"));
        r.setMedicationSituation(rs.getString("medication_situation"));
        r.setSubjectiveSymptoms(rs.getString("subjective_symptoms"));
        r.setAiAdvice(rs.getString("ai_advice"));
        r.setCreateTime(rs.getString("create_time"));
        r.setOrderNo(rs.getString("order_no"));
        r.setElderlyName(rs.getString("elderly_name"));
        r.setDoctorName(rs.getString("doctor_name"));
        return r;
    }
}
