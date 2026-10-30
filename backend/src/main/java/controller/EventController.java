package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.DeviceDAO;
import dao.HelpEventDAO;
import dao.OrderFormDAO;
import model.Device;
import model.HelpEvent;
import model.OrderForm;
import model.User;
import util.CommunityScope;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event")
public class EventController {

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
        List<HelpEvent> list = HelpEventDAO.findAll();
        Integer ownComm = CommunityScope.isWorker(currentUser) ? CommunityScope.workerCommunityId(currentUser)
                         : CommunityScope.isDoctor(currentUser) ? CommunityScope.doctorCommunityId(currentUser)
                         : null;
        if (ownComm != null) {
            CommunityScope.keepOwnCommunity(list, ownComm, HelpEvent::getElderlyId);
        }
        return ApiResult.success(list);
    }

    @PostMapping("/trigger")
    public Map<String, Object> trigger(@RequestParam Map<String, String> p,
                                       HttpServletRequest request) throws SQLException {
        checkRole(request);
        User currentUser = WebUtils.currentUser(request.getSession());
        String deviceIdStr = p.get("deviceId");
        if (deviceIdStr == null || deviceIdStr.isEmpty()) {
            throw new BusinessException("缺少设备ID");
        }
        int deviceId = Integer.parseInt(deviceIdStr);
        String eventType = p.get("eventType");
        if (eventType == null || eventType.trim().isEmpty()) {
            eventType = "摔倒告警";
        }
        String location = p.get("location");
        Device device = DeviceDAO.findById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (device.getElderlyId() <= 0) {
            throw new BusinessException("该设备未绑定老人，无法触发告警");
        }
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(device.getElderlyId());
            if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
                throw new BusinessException(403, "只能触发本社区老人的设备告警");
            }
        }
        HelpEvent event = new HelpEvent();
        event.setDeviceId(deviceId);
        event.setElderlyId(device.getElderlyId());
        event.setEventType(eventType);
        event.setLocation(location);
        event.setStatus("待处理");
        int eventId = HelpEventDAO.insert(event);
        OrderForm order = new OrderForm();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setElderlyId(device.getElderlyId());
        order.setDoctorId(0);
        order.setCreatorId(currentUser.getId());
        order.setEventId(eventId);
        order.setVisitDate(null);
        order.setReason(eventType + "（设备告警自动生成）");
        order.setStatus("待分配医生");
        int orderId = OrderFormDAO.insert(order);
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("orderId", orderId);
        data.put("eventType", eventType);
        data.put("elderlyId", device.getElderlyId());
        return ApiResult.success(data);
    }
}
