package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.DoctorDAO;
import dao.FallRecordDAO;
import dao.HelpEventDAO;
import dao.OrderFormDAO;
import model.FallRecord;
import model.OrderForm;
import model.User;
import util.CommunityScope;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fallvisit")
public class FallVisitController {

    @GetMapping("/list")
    public Map<String, Object> listRecords(HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        List<FallRecord> list;
        switch (currentUser.getRole()) {
            case "community_worker":
                list = FallRecordDAO.findAll();
                applyCommunityScope(list, currentUser);
                break;
            case "doctor":
                int doctorId = getDoctorId(currentUser.getId());
                list = FallRecordDAO.findByDoctor(doctorId);
                break;
            default:
                list = FallRecordDAO.findAll();
        }
        return ApiResult.success(list);
    }

    @GetMapping("/elderly/{elderlyId}")
    public Map<String, Object> listByElderly(@PathVariable int elderlyId,
                                             HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        List<FallRecord> list = FallRecordDAO.findByElderlyId(elderlyId);
        applyCommunityScope(list, currentUser);
        return ApiResult.success(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getRecord(@PathVariable int id,
                                         HttpSession session) throws SQLException {
        FallRecord record = FallRecordDAO.findById(id);
        if (record == null) {
            throw new BusinessException("未找到该处置记录");
        }
        List<FallRecord> one = new ArrayList<>();
        one.add(record);
        applyCommunityScope(one, WebUtils.currentUser(session));
        if (one.isEmpty()) {
            throw new BusinessException(403, "该处置记录不属于您管理的社区");
        }
        return ApiResult.success(record);
    }

    private void applyCommunityScope(List<FallRecord> list, User currentUser) throws SQLException {
        Integer ownComm = CommunityScope.isWorker(currentUser) ? CommunityScope.workerCommunityId(currentUser)
                         : CommunityScope.isDoctor(currentUser) ? CommunityScope.doctorCommunityId(currentUser)
                         : null;
        if (ownComm != null) {
            CommunityScope.keepOwnCommunity(list, ownComm, FallRecord::getElderlyId);
        }
    }

    @PostMapping("/create")
    public Map<String, Object> createRecord(@RequestParam Map<String, String> p,
                                            HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"doctor".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限创建处置记录");
        }
        int doctorId = getDoctorId(currentUser.getId());
        int orderId = Integer.parseInt(p.get("orderId"));
        OrderForm order = OrderFormDAO.findById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getDoctorId() != doctorId) {
            throw new BusinessException(403, "该工单不是您负责的");
        }
        if (order.getEventId() <= 0) {
            throw new BusinessException("该工单不是设备告警工单，请填写普通巡访记录");
        }
        FallRecord record = new FallRecord();
        record.setOrderId(orderId);
        record.setEventId(order.getEventId());
        record.setElderlyId(order.getElderlyId());
        record.setDoctorId(doctorId);
        Integer bpSystolic = parseIntOrNull(p.get("bpSystolic"));
        Integer bpDiastolic = parseIntOrNull(p.get("bpDiastolic"));
        Integer heartRate = parseIntOrNull(p.get("heartRate"));
        Double bloodGlucose = parseDoubleOrNull(p.get("bloodGlucose"));
        record.setBpSystolic(bpSystolic);
        record.setBpDiastolic(bpDiastolic);
        record.setHeartRate(heartRate);
        record.setBloodGlucose(bloodGlucose);
        record.setVitalSigns(composeVitalSigns(bpSystolic, bpDiastolic, heartRate, bloodGlucose));
        record.setSiteCondition(p.get("siteCondition"));
        record.setInjurySite(p.get("injurySite"));
        record.setInjurySeverity(p.get("injurySeverity"));
        record.setDisposalMeasure(p.get("disposalMeasure"));
        record.setNeedHospitalization(p.get("needHospitalization"));
        record.setHospital(p.get("hospital"));
        record.setResult(p.get("result"));
        FallRecordDAO.insert(record);
        OrderFormDAO.updateStatus(orderId, "已完成");
        HelpEventDAO.updateStatus(order.getEventId(), "已处理");
        return ApiResult.message("处置记录提交成功");
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateRecord(@PathVariable int id,
                                            @RequestParam Map<String, String> p) throws SQLException {
        FallRecord record = new FallRecord();
        record.setId(id);
        Integer bpSystolic = parseIntOrNull(p.get("bpSystolic"));
        Integer bpDiastolic = parseIntOrNull(p.get("bpDiastolic"));
        Integer heartRate = parseIntOrNull(p.get("heartRate"));
        Double bloodGlucose = parseDoubleOrNull(p.get("bloodGlucose"));
        record.setBpSystolic(bpSystolic);
        record.setBpDiastolic(bpDiastolic);
        record.setHeartRate(heartRate);
        record.setBloodGlucose(bloodGlucose);
        record.setVitalSigns(composeVitalSigns(bpSystolic, bpDiastolic, heartRate, bloodGlucose));
        record.setSiteCondition(p.get("siteCondition"));
        record.setInjurySite(p.get("injurySite"));
        record.setInjurySeverity(p.get("injurySeverity"));
        record.setDisposalMeasure(p.get("disposalMeasure"));
        record.setNeedHospitalization(p.get("needHospitalization"));
        record.setHospital(p.get("hospital"));
        record.setResult(p.get("result"));
        FallRecordDAO.update(record);
        return ApiResult.message("更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteRecord(@PathVariable int id) throws SQLException {
        FallRecordDAO.delete(id);
        return ApiResult.message("删除成功");
    }

    private String composeVitalSigns(Integer bpSystolic, Integer bpDiastolic, Integer heartRate, Double bloodGlucose) {
        List<String> parts = new ArrayList<>();
        if (bpSystolic != null && bpDiastolic != null) {
            parts.add("血压" + bpSystolic + "/" + bpDiastolic);
        }
        if (heartRate != null) {
            parts.add("心率" + heartRate);
        }
        if (bloodGlucose != null) {
            parts.add("血糖" + bloodGlucose);
        }
        return parts.isEmpty() ? null : String.join("，", parts);
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private int getDoctorId(int userId) {
        try {
            model.Doctor doctor = DoctorDAO.findByUserId(userId);
            return doctor != null ? doctor.getId() : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
