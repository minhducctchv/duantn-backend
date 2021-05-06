package duantn.backend.component.filter;

import duantn.backend.authentication.CustomException;
import duantn.backend.authentication.CustomJwtAuthenticationFilter;
import duantn.backend.authentication.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomerInterceptor implements HandlerInterceptor {
    JwtUtil jwtUtil = new JwtUtil();

    CustomJwtAuthenticationFilter customJwtAuthenticationFilter = new CustomJwtAuthenticationFilter(jwtUtil);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            String email = jwtUtil.getUsernameFromToken(customJwtAuthenticationFilter.extractJwtFromRequest(request));
            //String email="thuyvvph08009@fpt.edu.vn";
            //email staff
            //String email="ducnmph09201@fpt.edu.vn"
            if (email == null || email.trim().equals(""))
                throw new CustomException("Token không hợp lệ (filter)");
            request.setAttribute("email", email);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Token không hợp lệ (filter), hoặc đã hết hạn");
        }
    }
}
