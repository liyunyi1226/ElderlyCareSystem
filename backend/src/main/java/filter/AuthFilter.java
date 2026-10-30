package filter;

import model.User;
import util.JsonUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = path.substring(contextPath.length());
        int semi = relativePath.indexOf(';');
        if (semi >= 0) relativePath = relativePath.substring(0, semi);

        if (relativePath.startsWith("/api/auth/login") ||
            relativePath.startsWith("/api/auth/register")) {
            chain.doFilter(request, response);
            return;
        }

        if (session == null || session.getAttribute("currentUser") == null) {
            if (relativePath.startsWith("/api/")) {
                resp.setContentType("application/json;charset=UTF-8");
                JsonUtil.error(resp, 401, "请先登录");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        String message = checkPermission(relativePath, currentUser);
        if (message != null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.setStatus(403);
            JsonUtil.error(resp, 403, message);
            return;
        }
        chain.doFilter(request, response);
    }

    private String checkPermission(String path, User user) {
        String role = user.getRole();
        if ("system_admin".equals(role)) {

            boolean isAuthApi = path.equals("/api/auth/current") || path.equals("/api/auth/logout");
            if (!(path.equals("/api/admin") || path.startsWith("/api/admin/") || isAuthApi)) {
                return "系统管理员无此功能权限";
            }
            return null;
        }
        if (path.startsWith("/api/admin/") || path.equals("/api/admin")) {
            return "无权限访问管理员页面";
        }
        return null;
    }

    @Override
    public void destroy() {}
}
