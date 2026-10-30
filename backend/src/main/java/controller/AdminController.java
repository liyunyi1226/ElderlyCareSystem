package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.*;
import model.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private void checkAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(WebUtils.CURRENT_USER) == null) {
            throw new BusinessException(401, "请先登录");
        }
        User user = (User) session.getAttribute(WebUtils.CURRENT_USER);
        if (!"system_admin".equals(user.getRole())) {
            throw new BusinessException(403, "无权限访问");
        }
    }

    @GetMapping({"", "/"})
    public Map<String, Object> doGet(@RequestParam String table,
                                     HttpServletRequest request) {
        checkAdmin(request);
        try {
            switch (table) {
                case "elderly":
                    return ApiResult.success(ElderlyDAO.findAll(null));
                case "orders":
                    return ApiResult.success(OrderFormDAO.findAll(null));
                case "visits":
                    return ApiResult.success(VisitRecordDAO.findAll());
                case "doctors":
                    return ApiResult.success(DoctorDAO.findAll());
                case "users":
                    return ApiResult.success(UserDAO.findAll());
                case "communities":
                    return ApiResult.success(CommunityDAO.findAll());
                case "devices":
                    return ApiResult.success(DeviceDAO.findAll());
                case "events":
                    return ApiResult.success(HelpEventDAO.findAll());
                case "followups":
                    return ApiResult.success(FollowUpPlanDAO.findAll());
                case "staff":
                    return ApiResult.success(CommunityStaffDAO.findAll());
                case "fallrecords":
                    return ApiResult.success(FallRecordDAO.findAll());
                default:
                    throw new BusinessException("无效的表名");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BusinessException("服务器错误: " + e.getMessage());
        }
    }

    @PostMapping({"", "/"})
    public Map<String, Object> doPost(@RequestParam String table,
                                      @RequestParam Map<String, String> p,
                                      HttpServletRequest request) {
        checkAdmin(request);
        try {
            switch (table) {
                case "elderly":
                    return insertElderly(p);
                case "doctors":
                    return insertDoctor(p);
                case "orders":
                    return insertOrder(p);
                case "visits":
                    return insertVisit(p);
                case "users":
                    return insertUser(p);
                case "communities":
                    return insertCommunity(p);
                case "devices":
                    return insertDevice(p);
                case "events":
                    return insertEvent(p);
                case "followups":
                    return insertFollowUp(p);
                case "staff":
                    return insertStaff(p);
                case "fallrecords":
                    return insertFallRecord(p);
                default:
                    throw new BusinessException("无效的表名");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BusinessException("服务器错误: " + e.getMessage());
        }
    }

    @PutMapping({"", "/"})
    public Map<String, Object> doPut(@RequestParam String table,
                                     @RequestParam(required = false) Integer id,
                                     @RequestParam Map<String, String> p,
                                     HttpServletRequest request) {
        checkAdmin(request);
        if (id == null) {
            throw new BusinessException("缺少ID参数");
        }
        try {
            switch (table) {
                case "elderly":
                    return updateElderly(id, p);
                case "doctors":
                    return updateDoctor(id, p);
                case "orders":
                    return updateOrder(id, p);
                case "visits":
                    return updateVisit(id, p);
                case "users":
                    return updateUser(id, p);
                case "communities":
                    return updateCommunity(id, p);
                case "devices":
                    return updateDevice(id, p);
                case "events":
                    return updateEvent(id, p);
                case "followups":
                    return updateFollowUp(id, p);
                case "staff":
                    return updateStaff(id, p);
                case "fallrecords":
                    return updateFallRecord(id, p);
                default:
                    throw new BusinessException("无效的表名");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BusinessException("服务器错误: " + e.getMessage());
        }
    }

    @DeleteMapping({"", "/"})
    public Map<String, Object> doDelete(@RequestParam String table,
                                        @RequestParam(required = false) Integer id,
                                        HttpServletRequest request) {
        checkAdmin(request);
        if (id == null) {
            throw new BusinessException("缺少ID参数");
        }
        // 禁止删除当前登录账号，避免会话失效后无法操作
        if ("users".equals(table)) {
            HttpSession session = request.getSession(false);
            User current = session != null ? (User) session.getAttribute(WebUtils.CURRENT_USER) : null;
            if (current != null && id == current.getId()) {
                throw new BusinessException("不能删除当前登录账号");
            }
        }
        try {
            // 统一走事务级联删除，自动按外键依赖顺序清理关联数据
            int related = AdminDeleteDAO.cascadeDelete(table, id);
            String msg = related > 0
                    ? "删除成功，已连带处理 " + related + " 条关联数据"
                    : "删除成功";
            return ApiResult.message(msg);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BusinessException("服务器错误: " + e.getMessage());
        }
    }

    private Map<String, Object> insertElderly(Map<String, String> p) throws SQLException {
        Elderly e = new Elderly();
        e.setName(p.get("name"));
        e.setGender(p.get("gender"));
        String age = p.get("age");
        if (age != null && !age.isEmpty()) e.setAge(Integer.parseInt(age));
        e.setPhone(p.get("phone"));
        e.setAddress(p.get("address"));
        e.setCurrentIllness(p.get("currentIllness"));
        e.setMedications(p.get("medications"));
        e.setBodyStatus(p.get("bodyStatus"));
        String communityId = p.get("communityId");
        e.setCommunityId(communityId != null && !communityId.trim().isEmpty() ? Integer.parseInt(communityId.trim()) : null);
        ElderlyDAO.insert(e);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateElderly(int id, Map<String, String> p) throws SQLException {
        Elderly e = new Elderly();
        e.setId(id);
        e.setName(p.get("name"));
        e.setGender(p.get("gender"));
        String age = p.get("age");
        if (age != null && !age.isEmpty()) e.setAge(Integer.parseInt(age));
        e.setPhone(p.get("phone"));
        e.setAddress(p.get("address"));
        e.setEmergencyContact(p.get("emergencyContact"));
        e.setEmergencyPhone(p.get("emergencyPhone"));
        e.setChronicDiseases(p.get("chronicDiseases"));
        e.setAllergies(p.get("allergies"));
        e.setCurrentIllness(p.get("currentIllness"));
        e.setMedications(p.get("medications"));
        e.setBodyStatus(p.get("bodyStatus"));
        String communityId = p.get("communityId");
        e.setCommunityId(communityId != null && !communityId.trim().isEmpty() ? Integer.parseInt(communityId.trim()) : null);
        ElderlyDAO.update(e);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertDoctor(Map<String, String> p) throws SQLException {
        Doctor d = new Doctor();
        d.setName(p.get("name"));
        d.setGender(p.get("gender"));
        d.setPhone(p.get("phone"));
        d.setSpecialization(p.get("specialization"));
        d.setHospital(p.get("hospital"));
        String communityId = p.get("communityId");
        d.setCommunityId(communityId != null && !communityId.trim().isEmpty() ? Integer.parseInt(communityId.trim()) : null);
        d.setStatus(p.get("status") != null ? p.get("status") : "离线");
        String userIdStr = p.get("userId");
        int userId = 0;
        if (userIdStr != null && !userIdStr.trim().isEmpty()) userId = Integer.parseInt(userIdStr.trim());
        if (userId == 0) {
            User u = new User();
            u.setUsername("doctor_" + System.currentTimeMillis());
            u.setPassword("123456");
            u.setRealName(d.getName());
            u.setRole("doctor");
            u.setPhone(d.getPhone());
            userId = UserDAO.insert(u);
        }
        d.setUserId(userId);
        DoctorDAO.insert(d);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateDoctor(int id, Map<String, String> p) throws SQLException {
        Doctor d = new Doctor();
        d.setId(id);
        d.setName(p.get("name"));
        d.setGender(p.get("gender"));
        d.setPhone(p.get("phone"));
        d.setSpecialization(p.get("specialization"));
        d.setHospital(p.get("hospital"));
        String communityId = p.get("communityId");
        d.setCommunityId(communityId != null && !communityId.trim().isEmpty() ? Integer.parseInt(communityId.trim()) : null);
        DoctorDAO.update(d);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertOrder(Map<String, String> p) throws SQLException {
        OrderForm o = new OrderForm();
        o.setOrderNo("ORD" + System.currentTimeMillis());
        o.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        String doctorIdParam = p.get("doctorId");
        o.setDoctorId(doctorIdParam != null && !doctorIdParam.trim().isEmpty() ? Integer.parseInt(doctorIdParam.trim()) : 0);
        String creatorId = p.get("creatorId");
        o.setCreatorId(creatorId != null ? Integer.parseInt(creatorId) : 1);
        o.setVisitDate(p.get("visitDate"));
        o.setReason(p.get("reason"));
        o.setStatus(p.get("status") != null ? p.get("status") : "待接收");
        OrderFormDAO.insert(o);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateOrder(int id, Map<String, String> p) throws SQLException {
        String status = p.get("status");
        if (status != null) {
            OrderFormDAO.updateStatus(id, status);
        } else {
            OrderForm o = new OrderForm();
            o.setId(id);
            String elderlyId = p.get("elderlyId");
            if (elderlyId != null) o.setElderlyId(Integer.parseInt(elderlyId));
            String doctorId = p.get("doctorId");
            if (doctorId != null) o.setDoctorId(Integer.parseInt(doctorId));
            o.setVisitDate(p.get("visitDate"));
            o.setReason(p.get("reason"));
            OrderFormDAO.update(o);
        }
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertVisit(Map<String, String> p) throws SQLException {
        VisitRecord v = new VisitRecord();
        v.setOrderId(Integer.parseInt(p.get("orderId")));
        v.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        v.setDoctorId(Integer.parseInt(p.get("doctorId")));
        v.setVitalSigns(p.get("vitalSigns"));
        v.setBodyCondition(p.get("bodyCondition"));
        v.setDailyLife(p.get("dailyLife"));
        v.setMedicationSituation(p.get("medicationSituation"));
        v.setSubjectiveSymptoms(p.get("subjectiveSymptoms"));
        v.setAiAdvice(p.get("aiAdvice"));
        VisitRecordDAO.insert(v);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateVisit(int id, Map<String, String> p) throws SQLException {
        VisitRecord v = new VisitRecord();
        v.setId(id);
        v.setVitalSigns(p.get("vitalSigns"));
        v.setBodyCondition(p.get("bodyCondition"));
        v.setDailyLife(p.get("dailyLife"));
        v.setMedicationSituation(p.get("medicationSituation"));
        v.setSubjectiveSymptoms(p.get("subjectiveSymptoms"));
        v.setAiAdvice(p.get("aiAdvice"));
        VisitRecordDAO.update(v);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertUser(Map<String, String> p) throws SQLException {
        String username = p.get("username");
        String password = p.get("password");
        String realName = p.get("realName");
        String role = p.get("role");
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || realName == null || realName.trim().isEmpty()) {
            throw new BusinessException("用户名、密码、真实姓名不能为空");
        }
        if (!"doctor".equals(role) && !"community_worker".equals(role) && !"system_admin".equals(role)) {
            throw new BusinessException("请选择有效的角色");
        }
        String communityIdStr = p.get("communityId");
        Integer communityId = null;
        if (communityIdStr != null && !communityIdStr.trim().isEmpty()) {
            try {
                communityId = Integer.parseInt(communityIdStr.trim());
            } catch (NumberFormatException e) {
                throw new BusinessException("所属社区ID必须是数字");
            }
        }
        // 同一事务内写入账号并按角色同步医生档案/社区任职
        AdminUserDAO.insertWithProfile(username.trim(), password, realName.trim(),
                role, p.get("phone"), communityId);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateUser(int id, Map<String, String> p) throws SQLException {
        User u = new User();
        u.setId(id);
        u.setRealName(p.get("realName"));
        u.setPhone(p.get("phone"));
        String statusStr = p.get("status");
        u.setStatus(statusStr != null && !statusStr.isEmpty() ? Integer.parseInt(statusStr) : 1);
        String password = p.get("password");
        if (password == null || password.trim().isEmpty()) {
            User exist = UserDAO.findById(id);
            password = exist != null ? exist.getPassword() : "123456";
        }
        UserDAO.update(u, password);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertCommunity(Map<String, String> p) throws SQLException {
        Community c = new Community();
        c.setName(p.get("name"));
        c.setAddress(p.get("address"));
        c.setPhone(p.get("phone"));
        CommunityDAO.insert(c);
        return ApiResult.message("社区创建成功");
    }

    private Map<String, Object> updateCommunity(int id, Map<String, String> p) throws SQLException {
        Community c = new Community();
        c.setId(id);
        c.setName(p.get("name"));
        c.setAddress(p.get("address"));
        c.setPhone(p.get("phone"));
        CommunityDAO.update(c);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertDevice(Map<String, String> p) throws SQLException {
        Device d = new Device();
        d.setDeviceNo(p.get("deviceNo"));
        d.setDeviceType(p.get("deviceType"));
        d.setInstallTime(p.get("installTime"));
        d.setStatus(p.get("status") != null ? p.get("status") : "正常");
        String elderlyId = p.get("elderlyId");
        d.setElderlyId(elderlyId != null && !elderlyId.trim().isEmpty() ? Integer.parseInt(elderlyId.trim()) : 0);
        DeviceDAO.insert(d);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateDevice(int id, Map<String, String> p) throws SQLException {
        Device d = new Device();
        d.setId(id);
        d.setDeviceNo(p.get("deviceNo"));
        d.setDeviceType(p.get("deviceType"));
        d.setInstallTime(p.get("installTime"));
        d.setStatus(p.get("status"));
        String elderlyId = p.get("elderlyId");
        d.setElderlyId(elderlyId != null && !elderlyId.trim().isEmpty() ? Integer.parseInt(elderlyId.trim()) : 0);
        DeviceDAO.update(d);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertEvent(Map<String, String> p) throws SQLException {
        HelpEvent ev = new HelpEvent();
        ev.setDeviceId(Integer.parseInt(p.get("deviceId")));
        ev.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        ev.setEventType(p.get("eventType"));
        ev.setLocation(p.get("location"));
        ev.setStatus(p.get("status") != null ? p.get("status") : "待处理");
        HelpEventDAO.insert(ev);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateEvent(int id, Map<String, String> p) throws SQLException {
        HelpEvent ev = new HelpEvent();
        ev.setId(id);
        ev.setEventType(p.get("eventType"));
        ev.setLocation(p.get("location"));
        ev.setStatus(p.get("status"));
        HelpEventDAO.update(ev);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertFollowUp(Map<String, String> p) throws SQLException {
        FollowUpPlan pl = new FollowUpPlan();
        pl.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        String communityId = p.get("communityId");
        pl.setCommunityId(communityId != null && !communityId.trim().isEmpty() ? Integer.parseInt(communityId.trim()) : 0);
        String creatorId = p.get("creatorId");
        pl.setCreatorId(creatorId != null && !creatorId.trim().isEmpty() ? Integer.parseInt(creatorId.trim()) : 1);
        pl.setPlanType(p.get("planType"));
        String cycleDays = p.get("cycleDays");
        pl.setCycleDays(cycleDays != null && !cycleDays.trim().isEmpty() ? Integer.parseInt(cycleDays.trim()) : null);
        pl.setStartDate(p.get("startDate"));
        pl.setNextVisitDate(p.get("nextVisitDate"));
        pl.setStatus(p.get("status") != null ? p.get("status") : "进行中");
        FollowUpPlanDAO.insert(pl);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateFollowUp(int id, Map<String, String> p) throws SQLException {
        FollowUpPlan pl = new FollowUpPlan();
        pl.setId(id);
        pl.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        String communityId = p.get("communityId");
        pl.setCommunityId(communityId != null && !communityId.trim().isEmpty() ? Integer.parseInt(communityId.trim()) : 0);
        pl.setPlanType(p.get("planType"));
        String cycleDays = p.get("cycleDays");
        pl.setCycleDays(cycleDays != null && !cycleDays.trim().isEmpty() ? Integer.parseInt(cycleDays.trim()) : null);
        pl.setStartDate(p.get("startDate"));
        pl.setNextVisitDate(p.get("nextVisitDate"));
        pl.setStatus(p.get("status"));
        FollowUpPlanDAO.update(pl);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertStaff(Map<String, String> p) throws SQLException {
        CommunityStaff s = new CommunityStaff();
        s.setCommunityId(Integer.parseInt(p.get("communityId")));
        s.setUserId(Integer.parseInt(p.get("userId")));
        String startDate = p.get("startDate");
        s.setStartDate(startDate != null && !startDate.trim().isEmpty() ? startDate.trim() : null);
        s.setPosition(p.get("position"));
        CommunityStaffDAO.insert(s);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateStaff(int id, Map<String, String> p) throws SQLException {
        CommunityStaff s = new CommunityStaff();
        s.setId(id);
        s.setCommunityId(Integer.parseInt(p.get("communityId")));
        s.setUserId(Integer.parseInt(p.get("userId")));
        String startDate = p.get("startDate");
        s.setStartDate(startDate != null && !startDate.trim().isEmpty() ? startDate.trim() : null);
        s.setPosition(p.get("position"));
        CommunityStaffDAO.update(s);
        return ApiResult.message("更新成功");
    }

    private Map<String, Object> insertFallRecord(Map<String, String> p) throws SQLException {
        FallRecord r = new FallRecord();
        r.setOrderId(Integer.parseInt(p.get("orderId")));
        r.setEventId(Integer.parseInt(p.get("eventId")));
        r.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        r.setDoctorId(Integer.parseInt(p.get("doctorId")));
        r.setSiteCondition(p.get("siteCondition"));
        r.setInjurySite(p.get("injurySite"));
        r.setInjurySeverity(p.get("injurySeverity"));
        r.setDisposalMeasure(p.get("disposalMeasure"));
        r.setNeedHospitalization(p.get("needHospitalization"));
        r.setHospital(p.get("hospital"));
        r.setResult(p.get("result"));
        FallRecordDAO.insert(r);
        return ApiResult.message("新增成功");
    }

    private Map<String, Object> updateFallRecord(int id, Map<String, String> p) throws SQLException {
        FallRecord r = new FallRecord();
        r.setId(id);
        r.setSiteCondition(p.get("siteCondition"));
        r.setInjurySite(p.get("injurySite"));
        r.setInjurySeverity(p.get("injurySeverity"));
        r.setDisposalMeasure(p.get("disposalMeasure"));
        r.setNeedHospitalization(p.get("needHospitalization"));
        r.setHospital(p.get("hospital"));
        r.setResult(p.get("result"));
        FallRecordDAO.update(r);
        return ApiResult.message("更新成功");
    }
}
