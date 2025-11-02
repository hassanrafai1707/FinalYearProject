package com.FinalYearProject.FinalYearProject.Config.Security.Filter;

import com.FinalYearProject.FinalYearProject.Service.JwtService;
import com.FinalYearProject.FinalYearProject.Service.MyUserDetailsServices;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
//    private static final Set<String> excludedPaths= Set.of(
//            "/api/v1/register",
//            "/api/v1/login",
//            "/auth/login",
//            "/auth/register",
//            "/login",
//            "/register",
//            "/",
//            "/css/",
//            "/js/",
//            "/images/",
//            "/webjars/"
//    );
    String token = null;
    String username=null;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader =request.getHeader("Authorization");//contain auth heder
        String path = request.getServletPath();
        System.out.println("\n\nincoming path= "+path);//incoming path for debuging
        if (//All allowed paths without auth
                path.startsWith("/api/v1/login") ||
                        path.startsWith("/api/v1/register") ||
                        path.startsWith("/auth/login") ||
                        path.startsWith("/api/v1/auth/login")||
                        path.startsWith("/api/v1/auth/register")||
                        path.startsWith("/auth/register") ||
                        path.startsWith("/login") ||
                        path.startsWith("/register") ||
                        path.startsWith("/css") ||
                        path.startsWith("/js") ||
                        path.startsWith("/images") ||
                        path.startsWith("/webjars") ||
                        path.equals("/") ||
                        path.equals("/favicon.ico")
        ) {

            System.out.println("path "+path);//for debuging check path
            System.out.println("🔓 Skipped JwtFilter for path: " + path);//for debuging
            filterChain.doFilter(request, response);
            return;
        }
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            token=authHeader.substring(7); //separate the heder type form heder
            username= jwtService.extractUserEmail(token);
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
    public void init(){ // no use just for debuging
        System.out.println("✅ JwtFilter initialized and added to SecurityFilterChain");
    }
}
