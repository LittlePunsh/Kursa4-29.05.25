//Перенправление либо на юзера или админа.

package org.kursplom.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasAdminRole = authorities.stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        if (hasAdminRole) {
            // Если ADMIN, перенаправляем на страницу администратора
            response.sendRedirect("/admin");
        } else {
            // Если обычный пользователь, перенаправляем на страницу выбора игры
            response.sendRedirect("/user");
        }
    }
}