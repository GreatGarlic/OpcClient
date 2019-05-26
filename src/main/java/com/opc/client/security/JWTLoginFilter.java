package com.opc.client.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.opc.client.config.JwtSettings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证用户名密码正确后，生成一个token，并将token返回给客户端
 * AbstractAuthenticationProcessingFilter，重写了其中的2个方法
 * attemptAuthentication ：接收并解析用户凭证。
 * successfulAuthentication ：用户成功登录后，这个方法会被调用，我们在这个方法里生成token。
 *
 * @author Administrator
 */
public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {
    private static final Pattern p = Pattern.compile("\\s*|\t|\r|\n");

    @Autowired
    JwtSettings jwtSettings;
    private ObjectMapper objectMapper = new ObjectMapper();


    public JWTLoginFilter() {
        super(new AntPathRequestMatcher("/login", "POST"));
    }

    /**
     * 接收并解析用户凭证
     *
     * @param req
     * @param res
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            int len = req.getContentLength();
            ServletInputStream inputStream = req.getInputStream();
            byte[] buffer = new byte[len];
            inputStream.read(buffer, 0, len);
            String body = new String(buffer);
            Matcher m = p.matcher(body);
            body = m.replaceAll("");

            User user = new ObjectMapper().readValue(body, User.class);
            //封装认证请求
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    user.getPassword(),
                    new ArrayList<>());
            // Allow subclasses to set the "details" property
            setDetails(req, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用户成功登录后，这个方法会被调用，我们在这个方法里生成token
     *
     * @param request
     * @param response
     * @param chain
     * @param auth
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
        try {
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            // 定义存放角色集合的对象
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (GrantedAuthority grantedAuthority : authorities) {
                arrayNode.add(grantedAuthority.getAuthority());
            }
            // 设置过期时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, jwtSettings.getExpireLength());
            Date time = calendar.getTime();
            //获取用户名称
            Claims claims = Jwts.claims().setSubject(auth.getName());
            claims.put("auth", arrayNode.toString());
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(time) // 设置过期时间
                    .signWith(SignatureAlgorithm.HS512, jwtSettings.getSecretKey())
                    .compact();
            // 登录成功后，返回token到header里面
            response.addHeader("Authorization", "Bearer " + token);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter printWriter = response.getWriter();
            printWriter.write("{\"status\":\"success\"}");
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Provided so that subclasses may configure what is put into the authentication
     * request's details property.
     *
     * @param request     that an authentication request is being created for
     * @param authRequest the authentication request object that should have its details
     *                    set
     */
    private void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

}
