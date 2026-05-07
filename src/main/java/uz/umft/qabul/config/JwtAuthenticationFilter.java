package uz.umft.qabul.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.UserRepository;
import uz.umft.qabul.service.JwtService;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtTokenService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtTokenService, UserRepository userRepository) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String token = authorizationHeader.substring(BEARER_PREFIX.length());
                DecodedJWT decodedJWT = jwtTokenService.decodeJWT(token, request.getRequestURI());
                var user = userRepository.findById(jwtTokenService.getUserId(decodedJWT))
                        .orElseThrow(() -> AuthException.unauthorized("INVALID_ACCESS_TOKEN", "Access token is invalid"));

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getType().name()));
                var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthException ex) {
                SecurityContextHolder.clearContext();
                response.sendError(ex.getStatus().value(), ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
