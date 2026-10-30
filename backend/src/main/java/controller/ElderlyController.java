package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.ElderlyDAO;
import dao.OrderFormDAO;
import dao.VisitRecordDAO;
import model.Elderly;
import model.User;
import model.VisitRecord;
import service.HealthScoreService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elderly")
public class ElderlyController {

    @GetMapping({"", "/list"})
    public Object listElderly(@RequestParam(required = false) String keyword,
                              HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        List<Elderly> list = ElderlyDAO.findAll(keyword);
        Integer ownComm = CommunityScope.isWorker(currentUser) ? CommunityScope.workerCommunityId(currentUser)
                         : CommunityScope.isDoctor(currentUser) ? CommunityScope.doctorCommunityId(currentUser)
                         : null;
        if (ownComm != null) {
            CommunityScope.keepOwnCommunityByField(list, ownComm, Elderly::getCommunityId);
        }
        for (Elderly e : list) {
            VisitRecord latest = VisitRecordDAO.findLatestByElderlyId(e.getId());
            e.setHealthScore(HealthScoreService.compute(e, latest));
        }
        if ("doctor".equals(currentUser.getRole())) {
            List<Integer> responsibleIds = OrderFormDAO.findElderlyIdsByDoctor(
                getDoctorIdFromUserId(currentUser.getId()));
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("responsibleIds", responsibleIds);
            return ApiResult.success(result);
        }
        return ApiResult.success(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getElderly(@PathVariable int id) throws SQLException {
        Elderly elderly = ElderlyDAO.findById(id);
        if (elderly == null) {
            throw new BusinessException("未找到该老人");
        }
        VisitRecord latest = VisitRecordDAO.findLatestByElderlyId(id);
        elderly.setHealthScore(HealthScoreService.compute(elderly, latest));
        return ApiResult.success(elderly);
    }

    @PostMapping("/create")
    public Map<String, Object> createElderly(@RequestParam Map<String, String> p,
                                             HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限新增老人");
        }
        Elderly elderly = buildElderly(p);
        if (CommunityScope.isWorker(currentUser)) {
            elderly.setCommunityId(CommunityScope.workerCommunityId(currentUser));
        }
        ElderlyDAO.insert(elderly);
        return ApiResult.message("新增成功");
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateElderly(@PathVariable int id,
                                             @RequestParam Map<String, String> p,
                                             HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限修改老人信息");
        }
        Elderly elderly = buildElderly(p);
        elderly.setId(id);
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Elderly exist = ElderlyDAO.findById(id);
            if (exist == null) {
                throw new BusinessException("未找到该老人");
            }
            Integer existComm = exist.getCommunityId();
            if (ownComm == null || existComm == null || ownComm.intValue() != existComm.intValue()) {
                throw new BusinessException(403, "该老人不属于您管理的社区");
            }
            elderly.setCommunityId(ownComm);
        }
        ElderlyDAO.update(elderly);
        return ApiResult.message("更新成功");
    }

    @PutMapping("/{id}/health")
    public Map<String, Object> updateHealthFields(@PathVariable int id,
                                                  @RequestParam Map<String, String> p,
                                                  HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"doctor".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        int doctorId = getDoctorIdFromUserId(currentUser.getId());
        List<Integer> responsibleIds = OrderFormDAO.findElderlyIdsByDoctor(doctorId);
        if (!responsibleIds.contains(id)) {
            throw new BusinessException(403, "该老人不是您负责的");
        }
        String currentIllness = p.get("currentIllness");
        String medications = p.get("medications");
        String bodyStatus = p.get("bodyStatus");
        ElderlyDAO.updateHealthFields(id, currentIllness, medications, bodyStatus);
        return ApiResult.message("健康信息更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteElderly(@PathVariable int id,
                                             HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限删除老人");
        }
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Elderly exist = ElderlyDAO.findById(id);
            if (exist == null) {
                throw new BusinessException("未找到该老人");
            }
            Integer existComm = exist.getCommunityId();
            if (ownComm == null || existComm == null || ownComm.intValue() != existComm.intValue()) {
                throw new BusinessException(403, "该老人不属于您管理的社区");
            }
        }
        ElderlyDAO.delete(id);
        return ApiResult.message("删除成功");
    }

    private Elderly buildElderly(Map<String, String> p) {
        Elderly e = new Elderly();
        e.setName(p.get("name"));
        e.setGender(p.get("gender"));
        String ageStr = p.get("age");
        if (ageStr != null && !ageStr.isEmpty()) {
            e.setAge(Integer.parseInt(ageStr));
        }
        e.setIdCard(p.get("idCard"));
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
        e.setCommunityId(communityId != null && !communityId.isEmpty() ? Integer.parseInt(communityId) : null);
        return e;
    }

    private int getDoctorIdFromUserId(int userId) {
        try {
            model.Doctor doctor = dao.DoctorDAO.findByUserId(userId);
            return doctor != null ? doctor.getId() : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
