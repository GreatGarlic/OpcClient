/**
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opc.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSettings {

    @Value("${security.jwt.login.username}")
    private String username;
    @Value("${security.jwt.login.password}")
    private String password;
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private String expireLength;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getExpireLength() {
        return expireLength;
    }

    public void setExpireLength(String expireLength) {
        this.expireLength = expireLength;
    }
}