package com.opc.client.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opc.client.config.JwtSettings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 自定义JWT认证过滤器
 * 该类继承自BasicAuthenticationFilter，在doFilterInternal方法中，
 * 从http头的Authorization 项读取token数据，然后用Jwts包提供的方法校验token的合法性。
 * 如果校验通过，就认为这是一个取得授权的合法请求
 *
 * @author Administrator
 */
public class JWTAuthenticationFilter extends BasicAuthenticationFilter {


    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
    @Autowired
    JwtSettings jwtSettings;

    private ObjectMapper objectMapper = new ObjectMapper();

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        long start = System.currentTimeMillis();
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new TokenException("Token为空");
        }
        // parse the token.
        String user = null;
        try {
            Claims Claims = Jwts.parser()
                    .setSigningKey(jwtSettings.getSecretKey())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
            user = Claims.getSubject();
            JsonNode jsonNode = objectMapper.readTree(Claims.get("auth", String.class));
            long end = System.currentTimeMillis();
            logger.info("执行时间: {}", (end - start) + " 毫秒");
            if (user != null) {
                ArrayList<GrantedAuthority> authorities = new ArrayList<>();
                for (JsonNode name : jsonNode) {
                    authorities.add(Role.findRoleByName(name.asText()));
                }
                return new UsernamePasswordAuthenticationToken(user, null, authorities);
            }

        } catch (ExpiredJwtException e) {
            logger.error("Token已过期: {} " + e);
            throw new TokenException("Token已过期");
        } catch (UnsupportedJwtException | IOException e) {
            logger.error("Token格式错误: {} " + e);
            throw new TokenException("Token格式错误");
        } catch (MalformedJwtException | SignatureException e) {
            logger.error("Token没有被正确构造: {} " + e);
            throw new TokenException("Token没有被正确构造");
        } catch (IllegalArgumentException e) {
            logger.error("非法参数异常: {} " + e);
            throw new TokenException("非法参数异常");
        }
        return null;
    }

}
