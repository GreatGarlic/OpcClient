package com.opc.client.security;

import com.opc.client.config.JwtSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    JwtSettings jwtSettings;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!jwtSettings.getUsername().equals(username)) {
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }
        String password = bCryptPasswordEncoder.encode(jwtSettings.getPassword());
        //TODO:可以是其他获取用户信息的途径
//        final User user = userRepository.findByUsername(username);
//        if (user == null) {
//            throw new UsernameNotFoundException("User '" + username + "' not found");
//        }

        // 这里设置权限和角色
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(Role.ROLE_ADMIN);
        authorities.add(Role.ROLE_GUEST);


        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(password)
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

}
