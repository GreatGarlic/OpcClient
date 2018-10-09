package com.opc.client.security;

import org.springframework.security.core.GrantedAuthority;

/**
 * 用户角色种类
 * @author Administrator
 */
public enum Role implements GrantedAuthority {
    /**
     * 管理员
     */
    ROLE_ADMIN,
    /**
     * 游客
     */
    ROLE_GUEST;

    @Override
    public String getAuthority() {
        return name();
    }

}
