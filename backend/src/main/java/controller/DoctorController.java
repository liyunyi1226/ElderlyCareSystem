package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.DoctorDAO;
import dao.UserDAO;
import model.Doctor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @GetMapping({"", "/list"})
    public Map<String, Object> listDoctors(HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        List<Doctor> list = DoctorDAO.findAll();
        Integer ownComm = CommunityScope.isWorker(currentUser) ? CommunityScope.workerCommunityId(currentUser)
                         : CommunityScope.isDoctor(currentUser) ? CommunityScope.doctorCommunityId(currentUser)
                         : null;
        if (ownComm != null) {
            CommunityScope.keepOwnCommunityByField(list, ownComm, Doctor::getCommunityId);
        }
        return ApiResult.success(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getDoctor(@PathVariable int id) throws SQLException {
        Doctor doctor = DoctorDAO.findById(id);
        if (doctor == null) {
            throw new BusinessException("未找到该医生");
        }
        return ApiResult.success(doctor);
    }

    @PostMapping("/create")
    public Map<String, Object> createDoctor(@RequestParam Map<String, String> p,
                                            HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        Doctor doctor = new Doctor();
        String userIdStr = p.get("userId");
        int userId = 0;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            userId = Integer.parseInt(userIdStr);
        }
        doctor.setUserId(userId);
        doctor.setName(p.get("name"));
        doctor.setGender(p.get("gender"));
        doctor.setPhone(p.get("phone"));
        doctor.setSpecialization(p.get("specialization"));
        doctor.setHospital(p.get("hospital"));
        if (CommunityScope.isWorker(currentUser)) {
            doctor.setCommunityId(CommunityScope.workerCommunityId(currentUser));
        } else {
            doctor.setCommunityId(parseIntOrNull(p.get("communityId")));
        }
        doctor.setStatus(p.get("status") != null ? p.get("status") : "离线");
        String username = null;
        if (userId == 0) {
            username = "doctor_" + System.currentTimeMillis();
            User u = new User();
            u.setUsername(username);
            u.setPassword("123456");
            u.setRealName(doctor.getName());
            u.setRole("doctor");
            u.setPhone(doctor.getPhone());
            int newUserId = UserDAO.insert(u);
            doctor.setUserId(newUserId);
        }
        DoctorDAO.insert(doctor);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "新增成功");
        result.put("username", username);
        return ApiResult.success(result);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateDoctor(@PathVariable int id,
                                            @RequestParam Map<String, String> p,
                                            HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setName(p.get("name"));
        doctor.setGender(p.get("gender"));
        doctor.setPhone(p.get("phone"));
        doctor.setSpecialization(p.get("specialization"));
        doctor.setHospital(p.get("hospital"));
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Doctor exist = DoctorDAO.findById(id);
            if (exist == null) {
                throw new BusinessException("未找到该医生");
            }
            Integer existComm = exist.getCommunityId();
            if (ownComm == null || existComm == null || ownComm.intValue() != existComm.intValue()) {
                throw new BusinessException(403, "该医生不属于您管理的社区");
            }
            doctor.setCommunityId(ownComm);
        } else {
            doctor.setCommunityId(parseIntOrNull(p.get("communityId")));
        }
        DoctorDAO.update(doctor);
        return ApiResult.message("更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDoctor(@PathVariable int id,
                                            HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Doctor exist = DoctorDAO.findById(id);
            if (exist == null) {
                throw new BusinessException("未找到该医生");
            }
            Integer existComm = exist.getCommunityId();
            if (ownComm == null || existComm == null || ownComm.intValue() != existComm.intValue()) {
                throw new BusinessException(403, "该医生不属于您管理的社区");
            }
        }
        DoctorDAO.delete(id);
        return ApiResult.message("删除成功");
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}
