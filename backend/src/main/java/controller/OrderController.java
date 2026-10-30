package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.DoctorDAO;
import dao.HelpEventDAO;
import dao.OrderFormDAO;
import model.Doctor;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @GetMapping({"", "/list"})
    public Map<String, Object> list(@RequestParam(required = false) String status,
                                    HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        List<OrderForm> list;
        switch (currentUser.getRole()) {
            case "community_worker":
                list = OrderFormDAO.findAll(status);
                CommunityScope.keepOwnCommunity(list, CommunityScope.workerCommunityId(currentUser), OrderForm::getElderlyId);
                break;
            case "doctor":
                int doctorId = getDoctorId(currentUser.getId());
                list = OrderFormDAO.findByDoctor(doctorId, status);
                break;
            case "system_admin":
                list = OrderFormDAO.findAll(status);
                break;
            default:
                list = OrderFormDAO.findAll(status);
        }
        return ApiResult.success(list);
    }

    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestParam Map<String, String> p,
                                           HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限创建工单");
        }
        OrderForm order = new OrderForm();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        order.setDoctorId(Integer.parseInt(p.get("doctorId")));
        order.setCreatorId(currentUser.getId());
        String visitDate = p.get("visitDate");
        order.setVisitDate((visitDate == null || visitDate.trim().isEmpty()) ? null : visitDate.trim());
        String reason = p.get("reason");
        order.setReason((reason == null || reason.trim().isEmpty()) ? null : reason.trim());
        order.setStatus("待接收");
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(order.getElderlyId());
            if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
                throw new BusinessException(403, "只能为所属社区的老人创建工单");
            }
        }
        OrderFormDAO.insert(order);
        return ApiResult.message("工单创建成功");
    }

    @PostMapping("/{id}/dispatch")
    public Map<String, Object> dispatchOrder(@PathVariable int id,
                                             @RequestParam Map<String, String> p,
                                             HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限派单");
        }
        String doctorIdStr = p.get("doctorId");
        if (doctorIdStr == null || doctorIdStr.isEmpty()) {
            throw new BusinessException("请选择医生");
        }
        int doctorId = Integer.parseInt(doctorIdStr);
        OrderForm order = OrderFormDAO.findById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!"待分配医生".equals(order.getStatus())) {
            throw new BusinessException("仅待分配状态的工单可以派单");
        }
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(order.getElderlyId());
            if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
                throw new BusinessException(403, "该工单不属于您管理的社区");
            }
            Doctor d = DoctorDAO.findById(doctorId);
            if (d == null || d.getCommunityId() == null || ownComm.intValue() != d.getCommunityId().intValue()) {
                throw new BusinessException(403, "只能将工单派给本社区医生");
            }
        }
        OrderFormDAO.assignDoctor(id, doctorId);
        if (order.getEventId() > 0) {
            HelpEventDAO.updateStatus(order.getEventId(), "已派单");
        }
        return ApiResult.message("派单成功");
    }

    @PostMapping("/{id}/receive")
    public Map<String, Object> receiveOrder(@PathVariable int id,
                                            HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"doctor".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        int doctorId = getDoctorId(currentUser.getId());
        OrderForm order = OrderFormDAO.findById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getDoctorId() != doctorId) {
            throw new BusinessException(403, "该工单不是分配给您的");
        }
        if (!"待接收".equals(order.getStatus())) {
            throw new BusinessException("工单状态不允许接收");
        }
        OrderFormDAO.updateStatus(id, "进行中");
        return ApiResult.message("工单已接收");
    }

    @PostMapping("/{id}/start")
    public Map<String, Object> startVisit(@PathVariable int id,
                                          HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"doctor".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        int doctorId = getDoctorId(currentUser.getId());
        OrderForm order = OrderFormDAO.findById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getDoctorId() != doctorId) {
            throw new BusinessException(403, "该工单不是分配给您的");
        }
        if (!"进行中".equals(order.getStatus())) {
            throw new BusinessException("工单状态不允许开始巡访");
        }
        OrderFormDAO.updateStatus(id, "巡访中");
        return ApiResult.message("已开始巡访");
    }

    @PostMapping("/{id}/return")
    public Map<String, Object> returnOrder(@PathVariable int id,
                                           HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"doctor".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        int doctorId = getDoctorId(currentUser.getId());
        OrderForm order = OrderFormDAO.findById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getDoctorId() != doctorId) {
            throw new BusinessException(403, "该工单不是分配给您的");
        }
        OrderFormDAO.updateStatus(id, "已退回");
        return ApiResult.message("工单已退回");
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateOrder(@PathVariable int id,
                                           @RequestParam Map<String, String> p,
                                           HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        OrderForm order = new OrderForm();
        order.setId(id);
        order.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        String doctorIdStr = p.get("doctorId");
        if (doctorIdStr != null && !doctorIdStr.trim().isEmpty()) {
            order.setDoctorId(Integer.parseInt(doctorIdStr.trim()));
        } else {
            OrderForm exist = OrderFormDAO.findById(id);
            order.setDoctorId(exist != null ? exist.getDoctorId() : 0);
        }
        String visitDate = p.get("visitDate");
        order.setVisitDate((visitDate == null || visitDate.trim().isEmpty()) ? null : visitDate.trim());
        String reason = p.get("reason");
        order.setReason((reason == null || reason.trim().isEmpty()) ? null : reason.trim());
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(order.getElderlyId());
            if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
                throw new BusinessException(403, "只能为本社区老人更新工单");
            }
        }
        OrderFormDAO.update(order);
        return ApiResult.message("更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteOrder(@PathVariable int id,
                                           HttpSession session) throws SQLException {
        User currentUser = WebUtils.currentUser(session);
        if (!"community_worker".equals(currentUser.getRole()) &&
            !"system_admin".equals(currentUser.getRole())) {
            throw new BusinessException(403, "无权限操作");
        }
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            OrderForm exist = OrderFormDAO.findById(id);
            if (exist == null) {
                throw new BusinessException("工单不存在");
            }
            Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(exist.getElderlyId());
            if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
                throw new BusinessException(403, "该工单不属于您管理的社区");
            }
        }
        OrderFormDAO.delete(id);
        return ApiResult.message("删除成功");
    }

    private int getDoctorId(int userId) {
        try {
            Doctor doctor = DoctorDAO.findByUserId(userId);
            return doctor != null ? doctor.getId() : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
