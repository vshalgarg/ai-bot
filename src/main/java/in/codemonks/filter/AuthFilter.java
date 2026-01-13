package in.codemonks.filter;

import in.codemonks.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Order(2)
@Component
@AllArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String path = request.getRequestURI();
        return path.startsWith("/upload/")
                || path.startsWith("/widget/")
                || path.startsWith("/static/")
                || path.startsWith("/assets/")
                || path.endsWith(".js")
                || path.endsWith(".css")
                || path.endsWith(".html")
                || path.equals("/")
                || path.equals("/favicon.ico")

                // auth / public APIs
                || path.equals("/ai-bot/api/v1/login")

                // allow OPTIONS for CORS
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("Skipping auth for OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }
        final String requestPath  = request.getRequestURI();
        if (requestPath.startsWith("/ai-bot/api/")) {
            String tenantId = request.getHeader("tenantId");
            if (StringUtils.isBlank(tenantId)) {
                log.info("tenantId is blank");
                respondUnauthorized(response, "Missing tenantId");
                return;
            }
            log.info("requestPath: {}, tenantId: {}", requestPath, tenantId);
            TenantContext.setTenantId(tenantId);
        }
//        if(requestPath.contains("/v1/ingest")) {
//            //validate token from auth
//            String authHeader = request.getHeader("Authorization");
//            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
//                handleBearerToken(authHeader);
//            } else {
//                log.warn("Authorization header missing or invalid");
//                respondUnauthorized(response, "Missing or invalid Authorization header");
//                return;
//            }
//
//        }

        filterChain.doFilter(request, response);
    }

    private void respondUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader(
                "Access-Control-Allow-Headers",
                "Authorization, Content-Type, tenantId"
        );
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

}
