package com.opc.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用程序配置属性.
 */
@Component
@ConfigurationProperties(prefix = "app-config")
public class AppConfig {


    private String host;
    private String domain = "";
    private String progId;
    private String user;
    private String password;
    /**
     * Key是PLC业务编码,Value是OpcServer对应的PLC编码
     */
    private Map<String, String> plcNumberDictionary = new HashMap<String, String>();
    private String [] plcNumbers;




    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProgId() {
        return progId;
    }

    public void setProgId(String progId) {
        this.progId = progId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getPlcNumberDictionary() {
        return plcNumberDictionary;
    }

    public void setPlcNumberDictionary(Map<String, String> plcNumberDictionary) {
        this.plcNumberDictionary = plcNumberDictionary;
        this.plcNumbers=plcNumberDictionary.values().toArray(new String[0]);
    }

    public String[] getPlcNumbers() {
        return plcNumbers;
    }
}
