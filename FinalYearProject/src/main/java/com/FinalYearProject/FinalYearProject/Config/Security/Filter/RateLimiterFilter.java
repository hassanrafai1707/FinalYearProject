package com.FinalYearProject.FinalYearProject.Config.Security.Filter;

import com.FinalYearProject.FinalYearProject.Exceptions.ErrorResponse;
import com.FinalYearProject.FinalYearProject.Exceptions.ToManyRequests;
import com.FinalYearProject.FinalYearProject.Service.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RateLimiterFilter extends OncePerRequestFilter {
    @Value("${app.version}")
    private String appVersion;
    private final RateLimiterService rateLimiterService;

    public RateLimiterFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader =request.getHeader("Authorization");//contain auth heder
        String path = request.getServletPath();
        if (//All allowed paths without auth
                path.startsWith(appVersion+"/login") ||
                        path.startsWith(appVersion+"/register") ||
                        path.startsWith(appVersion+"/auth")||
                        path.equals("/login") ||
                        path.equals("/register") ||
                        path.equals("/admin-dashboard")||
                        path.equals("/teacher-dashboard")||
                        path.equals("/supervisor-dashboard")||
                        path.equals("/student-dashboard")||
                        path.startsWith("/confirm") ||
                        path.equals("/favicon.ico") ||
                        path.startsWith("/css") ||
                        path.startsWith("/js") ||
                        path.startsWith("/images") ||
                        path.startsWith("/webjars") ||
                        path.equals("/")
        ) {
            filterChain.doFilter(request, response);
            return;
        }
        if(authHeader!=null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);//separate the heder type form heder
            String ip=getIP(request);
            String username=SecurityContextHolder.getContext().getAuthentication().getName();
            String key=token+ip+username;
            Bucket tokenBuket= rateLimiterService.resolver(key);
            ConsumptionProbe consumptionProbe= tokenBuket.tryConsumeAndReturnRemaining(1);
            if (consumptionProbe.isConsumed()){
                response.addHeader("X-Rate-Limit-Remaining",String.valueOf(consumptionProbe.getRemainingTokens()));
                filterChain.doFilter(request,response);
            }
            else {
                //set error in a pattern
                long waitSeconds = consumptionProbe.getNanosToWaitForRefill() / 1_000_000_000;
                Map<String,String> error=new HashMap<>();

                error.put("message","YOU have exited your limit wait for "+waitSeconds);
                error.put("path",request.getRequestURI());
                error.put("timestamp",LocalDateTime.now().toString());
                error.put("states",String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
                // Set response status and headers
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                ObjectMapper objectMapper=new ObjectMapper();
                response.getWriter().write(objectMapper.writeValueAsString(error));
                response.getWriter().flush();

                logger.warn("Exception occurred: RateLimiterFilter user with name:"+username+" with ip:+"+ip);
            }
        }
    }
    private String getIP(HttpServletRequest request){
        String xfHeader=request.getHeader("X-Forwarded-For");
        if (xfHeader==null||xfHeader.isEmpty()){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
