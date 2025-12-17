package com.FinalYearProject.FinalYearProject.Config.Security.Filter;

import com.FinalYearProject.FinalYearProject.Service.JwtService;
import com.FinalYearProject.FinalYearProject.Service.MyUserDetailsServices;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


//this class is used to configure the Jwt json web token used for authorization
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MyUserDetailsServices myUserDetailsServices;
    @Value("${app.version}")
    private String appVersion;
    String token = null;
    String username=null;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader =request.getHeader("Authorization");//contain auth heder
        String path = request.getServletPath();
        System.out.println("\n\nincoming path= "+path);//incoming path for debugging
        if (//All allowed paths without auth
                 path.startsWith(appVersion+"/login") ||
                 path.startsWith(appVersion+"/register") ||
                 path.startsWith(appVersion+"/auth")||
                 path.equals("/login") ||
                 path.equals("/register") ||
                 path.startsWith("/confirm") ||
                 path.equals("/favicon.ico") ||
                 path.startsWith("/css") ||
                 path.startsWith("/js") ||
                 path.startsWith("/images") ||
                 path.startsWith("/webjars") ||
                 path.equals("/")
        ) {

            System.out.println("path "+path);//for debuging check path
            System.out.println("🔓 Skipped JwtFilter for path: " + path);//for debugging
            filterChain.doFilter(request, response);
            return;
        }
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            token=authHeader.substring(7); //separate the heder type form heder
            username= jwtService.extractUserEmail(token);
            try {
                jwtService.isTokenExpiredOrThrow(token);
            } catch (Exception e) {
                UserDetails userDetails = myUserDetailsServices.loadUserByUsername(username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token Expired — Please Login Again");
                return;
            }
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails =applicationContext.getBean(MyUserDetailsServices.class).loadUserByUsername(username);
            if(jwtService.validateToken(token , userDetails)){
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken
                                (userDetails,null,userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request,response);
    }
    @PostConstruct
    public void init(){ // no use just for debugging
        System.out.println("✅ JwtFilter initialized and added to SecurityFilterChain");
    }
}
