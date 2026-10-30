package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import dao.UserDAO;
import model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam(required = false) String username,
                                     @RequestParam(required = false) String password,
                                     HttpServletRequest request) throws SQLException {
        if (username == null || password == null) {
            throw new BusinessException("用户名和密码不能为空");
        }
        User user = UserDAO.login(username, password);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", user);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("roleName", user.getRoleName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("sessionId", session.getId());
        return ApiResult.success(userInfo);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResult.message("退出成功");
    }

    @RequestMapping(value = "/current", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = (User) session.getAttribute("currentUser");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("roleName", user.getRoleName());
        userInfo.put("phone", user.getPhone());
        return ApiResult.success(userInfo);
    }
}
