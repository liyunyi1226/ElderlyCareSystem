package dao;
import model.FallRecord;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class FallRecordDAO {
    private static final String SELECT_COLUMNS =
        "SELECT fr.*, o.order_no, ev.event_type, ev.event_time, e.name AS elderly_name, d.name AS doctor_name " +
        "FROM fall_record fr " +
        "LEFT JOIN order_form o ON fr.order_id = o.id " +
        "LEFT JOIN help_event ev ON fr.event_id = ev.id " +
        "LEFT JOIN elderly e ON fr.elderly_id = e.id " +
        "LEFT JOIN doctor d ON fr.doctor_id = d.id ";
    public static List<FallRecord> findAll() throws SQLException {
        return executeQuery(SELECT_COLUMNS + "ORDER BY fr.create_time DESC", new ArrayList<>());
    }
    public static List<FallRecord> findByElderlyId(int elderlyId) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(elderlyId);
        return executeQuery(SELECT_COLUMNS + "WHERE fr.elderly_id = ? ORDER BY fr.create_time DESC", params);
    }
    public static List<FallRecord> findByDoctor(int doctorId) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(doctorId);
        return executeQuery(SELECT_COLUMNS + "WHERE fr.doctor_id = ? ORDER BY fr.create_time DESC", params);
    }
    public static List<FallRecord> findByCreator(int creatorId) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(creatorId);
        return executeQuery(SELECT_COLUMNS + "WHERE o.creator_id = ? ORDER BY fr.create_time DESC", params);
    }
    public static FallRecord findById(int id) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(id);
        List<FallRecord> list = executeQuery(SELECT_COLUMNS + "WHERE fr.id = ?", params);
        return list.isEmpty() ? null : list.get(0);
    }
    public static int insert(FallRecord r) throws SQLException {
        String sql = "INSERT INTO fall_record (order_id, event_id, elderly_id, doctor_id, site_condition, injury_site, " +
                     "injury_severity, vital_signs, bp_systolic, bp_diastolic, heart_rate, blood_glucose, " +
                     "disposal_measure, need_hospitalization, hospital, result) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql,
            r.getOrderId(), r.getEventId(), r.getElderlyId(), r.getDoctorId(),
            r.getSiteCondition(), r.getInjurySite(), r.getInjurySeverity(),
            r.getVitalSigns(), r.getBpSystolic(), r.getBpDiastolic(), r.getHeartRate(), r.getBloodGlucose(),
            r.getDisposalMeasure(), r.getNeedHospitalization(), r.getHospital(), r.getResult());
    }
    public static int update(FallRecord r) throws SQLException {
        String sql = "UPDATE fall_record SET site_condition=?, injury_site=?, injury_severity=?, vital_signs=?, " +
                     "bp_systolic=?, bp_diastolic=?, heart_rate=?, blood_glucose=?, disposal_measure=?, " +
                     "need_hospitalization=?, hospital=?, result=? WHERE id=?";
        return DBUtil.executeUpdate(sql,
            r.getSiteCondition(), r.getInjurySite(), r.getInjurySeverity(),
            r.getVitalSigns(), r.getBpSystolic(), r.getBpDiastolic(), r.getHeartRate(), r.getBloodGlucose(),
            r.getDisposalMeasure(), r.getNeedHospitalization(), r.getHospital(), r.getResult(),
            r.getId());
    }
    public static int delete(int id) throws SQLException {
        String sql = "DELETE FROM fall_record WHERE id = ?";
        return DBUtil.executeUpdate(sql, id);
    }
    private static List<FallRecord> executeQuery(String sql, List<Object> params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FallRecord> list = new ArrayList<>();
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
    private static FallRecord mapRecord(ResultSet rs) throws SQLException {
        FallRecord r = new FallRecord();
        r.setId(rs.getInt("id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setEventId(rs.getInt("event_id"));
        r.setElderlyId(rs.getInt("elderly_id"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setFallTime(rs.getString("fall_time"));
        r.setSiteCondition(rs.getString("site_condition"));
        r.setInjurySite(rs.getString("injury_site"));
        r.setInjurySeverity(rs.getString("injury_severity"));
        r.setVitalSigns(rs.getString("vital_signs"));
        r.setBpSystolic((Integer) rs.getObject("bp_systolic"));
        r.setBpDiastolic((Integer) rs.getObject("bp_diastolic"));
        r.setHeartRate((Integer) rs.getObject("heart_rate"));
        Object bg = rs.getObject("blood_glucose");
        r.setBloodGlucose(bg != null ? ((java.math.BigDecimal) bg).doubleValue() : null);
        r.setDisposalMeasure(rs.getString("disposal_measure"));
        r.setNeedHospitalization(rs.getString("need_hospitalization"));
        r.setHospital(rs.getString("hospital"));
        r.setResult(rs.getString("result"));
        r.setCreateTime(rs.getString("create_time"));
        r.setOrderNo(rs.getString("order_no"));
        r.setEventType(rs.getString("event_type"));
        r.setEventTime(rs.getString("event_time"));
        r.setElderlyName(rs.getString("elderly_name"));
        r.setDoctorName(rs.getString("doctor_name"));
        return r;
    }
}
