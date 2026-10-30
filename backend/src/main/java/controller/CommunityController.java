package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.WebUtils;
import dao.CommunityDAO;
import dao.CommunityStaffDAO;
import dao.UserDAO;
import model.Community;
import model.CommunityStaff;
import model.User;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private void checkAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(WebUtils.CURRENT_USER) == null) {
            throw new BusinessException(401, "请先登录");
        }
        User user = (User) session.getAttribute(WebUtils.CURRENT_USER);
        if (!"system_admin".equals(user.getRole())) {
            throw new BusinessException(403, "仅系统管理员可进行社区管理操作");
        }
    }

    @GetMapping("/staff/users")
    public Map<String, Object> staffUsers() throws SQLException {
        List<User> users = UserDAO.findByRole("community_worker");
        List<CommunityStaff> allStaff = CommunityStaffDAO.findAll();
        Map<Integer, CommunityStaff> bindingMap = new HashMap<>();
        for (CommunityStaff s : allStaff) bindingMap.put(s.getUserId(), s);
        List<Map<String, Object>> safe = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("role", u.getRole());
            CommunityStaff bind = bindingMap.get(u.getId());
            if (bind != null) {
                Map<String, Object> cb = new HashMap<>();
                cb.put("communityId", bind.getCommunityId());
                cb.put("communityName", bind.getCommunityName());
                m.put("currentBinding", cb);
            }
            safe.add(m);
        }
        return ApiResult.success(safe);
    }

    @GetMapping("/staff")
    public Map<String, Object> staffList() throws SQLException {
        List<CommunityStaff> staffList = CommunityStaffDAO.findAll();
        return ApiResult.success(staffList);
    }

    @GetMapping({"", "/list"})
    public Map<String, Object> list() throws SQLException {
        List<Community> list = CommunityDAO.findAll();
        return ApiResult.success(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable int id) throws SQLException {
        Community community = CommunityDAO.findById(id);
        if (community == null) {
            throw new BusinessException("社区不存在");
        }
        return ApiResult.success(community);
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestParam Map<String, String> p,
                                      HttpServletRequest request) throws SQLException {
        checkAdmin(request);
        Community c = new Community();
        c.setName(p.get("name"));
        c.setAddress(p.get("address"));
        c.setPhone(p.get("phone"));
        CommunityDAO.insert(c);
        return ApiResult.message("社区创建成功");
    }

    @PostMapping("/staff")
    public Map<String, Object> bindStaff(@RequestParam Map<String, String> p,
                                         HttpServletRequest request) throws SQLException {
        checkAdmin(request);
        int userId = Integer.parseInt(p.get("userId"));
        CommunityStaff exist = CommunityStaffDAO.findByUserId(userId);
        if (exist != null) {
            throw new BusinessException("该账号已绑定到《" +
                (exist.getCommunityName() != null ? exist.getCommunityName() : "社区#" + exist.getCommunityId()) +
                "》，一人只能归属一个社区");
        }
        CommunityStaff s = new CommunityStaff();
        s.setCommunityId(Integer.parseInt(p.get("communityId")));
        s.setUserId(userId);
        String startDate = p.get("startDate");
        s.setStartDate((startDate == null || startDate.trim().isEmpty()) ? null : startDate.trim());
        s.setPosition(p.get("position"));
        CommunityStaffDAO.insert(s);
        return ApiResult.message("工作人员绑定成功");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable int id,
                                      @RequestParam Map<String, String> p,
                                      HttpServletRequest request) throws SQLException {
        checkAdmin(request);
        Community c = new Community();
        c.setId(id);
        c.setName(p.get("name"));
        c.setAddress(p.get("address"));
        c.setPhone(p.get("phone"));
        CommunityDAO.update(c);
        return ApiResult.message("更新成功");
    }

    @DeleteMapping("/staff/{id}")
    public Map<String, Object> unbindStaff(@PathVariable int id,
                                           HttpServletRequest request) throws SQLException {
        checkAdmin(request);
        CommunityStaffDAO.delete(id);
        return ApiResult.message("已解除绑定");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable int id,
                                      HttpServletRequest request) throws SQLException {
        checkAdmin(request);
        CommunityDAO.delete(id);
        return ApiResult.message("删除成功");
    }
}
