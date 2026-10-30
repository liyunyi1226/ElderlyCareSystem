package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.FollowUpPlanDAO;
import model.FollowUpPlan;
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
@RequestMapping("/api/followup")
public class FollowUpController {

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
        List<FollowUpPlan> list = FollowUpPlanDAO.findAll();
        Integer ownComm = CommunityScope.isWorker(currentUser) ? CommunityScope.workerCommunityId(currentUser)
                         : CommunityScope.isDoctor(currentUser) ? CommunityScope.doctorCommunityId(currentUser)
                         : null;
        if (ownComm != null) {
            CommunityScope.keepOwnCommunityByField(list, ownComm, FollowUpPlan::getCommunityId);
        }
        return ApiResult.success(list);
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestParam Map<String, String> p,
                                      HttpServletRequest request) throws SQLException {
        checkRole(request);
        User currentUser = WebUtils.currentUser(request.getSession());
        FollowUpPlan plan = buildPlan(p);
        plan.setCreatorId(currentUser.getId());
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(plan.getElderlyId());
            if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
                throw new BusinessException(403, "只能为本社区老人创建随访计划");
            }
            plan.setCommunityId(ownComm);
        }
        FollowUpPlanDAO.insert(plan);
        return ApiResult.message("随访计划创建成功");
    }

    @PostMapping("/{id}/end")
    public Map<String, Object> end(@PathVariable int id,
                                   HttpServletRequest request) throws SQLException {
        checkRole(request);
        User currentUser = WebUtils.currentUser(request.getSession());
        checkOwnPlan(id, currentUser);
        FollowUpPlanDAO.updateStatus(id, "已结束");
        return ApiResult.message("计划已结束");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable int id,
                                      @RequestParam Map<String, String> p,
                                      HttpServletRequest request) throws SQLException {
        checkRole(request);
        User currentUser = WebUtils.currentUser(request.getSession());
        checkOwnPlan(id, currentUser);
        FollowUpPlan plan = buildPlan(p);
        plan.setId(id);
        if (CommunityScope.isWorker(currentUser)) {
            Integer ownComm = CommunityScope.workerCommunityId(currentUser);
            Integer elderlyComm = CommunityScope.elderlyCommunityMap().get(plan.getElderlyId());
            if (ownComm == null || elderlyComm == null || ownComm.intValue() != elderlyComm.intValue()) {
                throw new BusinessException(403, "只能为本社区老人更新随访计划");
            }
            plan.setCommunityId(ownComm);
        }
        FollowUpPlanDAO.update(plan);
        return ApiResult.message("更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable int id,
                                      HttpServletRequest request) throws SQLException {
        checkRole(request);
        User currentUser = WebUtils.currentUser(request.getSession());
        checkOwnPlan(id, currentUser);
        FollowUpPlanDAO.delete(id);
        return ApiResult.message("删除成功");
    }

    private void checkOwnPlan(int id, User currentUser) throws SQLException {
        if (!CommunityScope.isWorker(currentUser)) return;
        Integer ownComm = CommunityScope.workerCommunityId(currentUser);
        FollowUpPlan exist = FollowUpPlanDAO.findById(id);
        if (exist == null) {
            throw new BusinessException("随访计划不存在");
        }
        if (ownComm == null || ownComm.intValue() != exist.getCommunityId()) {
            throw new BusinessException(403, "该随访计划不属于您管理的社区");
        }
    }

    private FollowUpPlan buildPlan(Map<String, String> p) {
        FollowUpPlan plan = new FollowUpPlan();
        plan.setElderlyId(Integer.parseInt(p.get("elderlyId")));
        String communityId = p.get("communityId");
        plan.setCommunityId(communityId != null && !communityId.isEmpty() ? Integer.parseInt(communityId) : 0);
        plan.setPlanType(p.get("planType"));
        String cycleDays = p.get("cycleDays");
        plan.setCycleDays(cycleDays != null && !cycleDays.isEmpty() ? Integer.parseInt(cycleDays) : null);
        String startDate = p.get("startDate");
        plan.setStartDate((startDate == null || startDate.trim().isEmpty()) ? null : startDate.trim());
        String nextVisitDate = p.get("nextVisitDate");
        plan.setNextVisitDate((nextVisitDate == null || nextVisitDate.trim().isEmpty()) ? null : nextVisitDate.trim());
        String status = p.get("status");
        plan.setStatus((status == null || status.trim().isEmpty()) ? "进行中" : status.trim());
        return plan;
    }
}
