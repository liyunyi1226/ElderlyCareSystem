package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

public class SessionPathParamFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getRequestURI() != null && req.getRequestURI().indexOf(';') >= 0) {
            chain.doFilter(new NormalizedRequest(req), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static String stripPathParams(String path) {
        return path == null ? null : path.replaceAll(";[^/]*", "");
    }

    private static class NormalizedRequest extends HttpServletRequestWrapper {

        private final String requestURI;

        NormalizedRequest(HttpServletRequest request) {
            super(request);
            this.requestURI = stripPathParams(request.getRequestURI());
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        @Override
        public String getServletPath() {
            return stripPathParams(super.getServletPath());
        }

        @Override
        public String getPathInfo() {
            return stripPathParams(super.getPathInfo());
        }
    }

    @Override
    public void destroy() {}
}
