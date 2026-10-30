package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.DeviceDAO;
import model.Device;
import model.User;
import util.CommunityScope;

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private void checkRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(WebUtils.CURRENT_USER) == null) {
            throw new BusinessException(401, "请先登录");
        }
        User user = (User) session.getAttribute(WebUtils.CURRENT_USER);
        if (!"community_worker".equals(user.getRole()) && !"system_admin".equals(user.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
    }

    @GetMapping({"", "/list"})
    public Map<String, Object> list(HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        List<Device> list = DeviceDAO.findAll();
        Integer ownComm = CommunityScope.isWorker(currentUser) ? CommunityScope.workerCommunityId(currentUser)
                         : CommunityScope.isDoctor(currentUser) ? CommunityScope.doctorCommunityId(currentUser)
                         : null;
        if (ownComm != null) {
            CommunityScope.keepOwnCommunity(list, ownComm, Device::getElderlyId);
        }
        return ApiResult.success(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable int id) throws SQLException {
        Device device = DeviceDAO.findById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        return ApiResult.success(device);
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestParam Map<String, String> p,
                                      HttpServletRequest request) throws SQLException {
        checkRole(request);
        Device d = buildDevice(p);
        User currentUser = WebUtils.currentUser(request.getSession());
        validateOwnCommunity(currentUser, d);
        DeviceDAO.insert(d);
        return ApiResult.message("设备新增成功");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable int id,
                                      @RequestParam Map<String, String> p,
                                      HttpServletRequest request) throws SQLException {
        checkRole(request);
        Device d = buildDevice(p);
        d.setId(id);
        User currentUser = WebUtils.currentUser(request.getSession());
        validateOwnCommunity(currentUser, d);
        DeviceDAO.update(d);
        return ApiResult.message("更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable int id,
                                      HttpServletRequest request) throws SQLException {
        checkRole(request);
        Device exist = DeviceDAO.findById(id);
        if (exist == null) {
            throw new BusinessException("设备不存在");
        }
        User currentUser = WebUtils.currentUser(request.getSession());
        validateOwnCommunity(currentUser, exist);
        DeviceDAO.delete(id);
        return ApiResult.message("删除成功");
    }

    private Device buildDevice(Map<String, String> p) {
        Device d = new Device();
        d.setDeviceNo(p.get("deviceNo"));
        d.setDeviceType(p.get("deviceType"));
        String installTime = p.get("installTime");
        d.setInstallTime((installTime == null || installTime.trim().isEmpty()) ? null : installTime.trim());
        d.setStatus(p.get("status"));
        String elderlyId = p.get("elderlyId");
        d.setElderlyId(elderlyId != null && !elderlyId.isEmpty() ? Integer.parseInt(elderlyId) : 0);
        return d;
    }

    private void validateOwnCommunity(User currentUser, Device d) throws SQLException {
        if (!CommunityScope.isWorker(currentUser)) return;
        Integer ownComm = CommunityScope.workerCommunityId(currentUser);
        int elderlyId = d.getElderlyId();
        if (elderlyId <= 0) {
            throw new BusinessException(403, "社区人员新增/修改设备必须绑定本社区老人");
        }
        Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(elderlyId);
        if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
            throw new BusinessException(403, "只能管理本社区老人的设备");
        }
    }
}
