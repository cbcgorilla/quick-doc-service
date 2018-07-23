package cn.mxleader.quickdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quickdoc.ldap")
public class LDAPProperties {
    /**
     * LDAP同步功能启动开关
     */
    private Boolean enabled;

    /**
     * LDAP连接AD域控服务的管理用户名
     */
    private String username;

    /**
     * LDAP连接AD域控服务的管理用户密码
     */
    private String password;

    /**
     * LDAP连接AD域控服务的URL
     */
    private String url;

    private String domain;

    /**
     * 搜索AD域控服务的根路径
     */
    private String searchBase;

    /**
     * 屏蔽部门黑名单
     */
    private String blacklist[];

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public String[] getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(String[] blacklist) {
        this.blacklist = blacklist;
    }
}
