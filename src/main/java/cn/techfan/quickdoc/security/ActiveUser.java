package cn.techfan.quickdoc.security;

import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

@Component
public class ActiveUser implements HttpSessionBindingListener {

    private static final String APP_USER_STORE = "ActiveUserStore";

    private String username;

    public ActiveUser(String username) {
        this.username = username;
    }

    public ActiveUser() {
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        ServletContext application = session.getServletContext();
        // 把用户名放入在线列表
        ActiveUserStore activeUserStore = (ActiveUserStore) application.getAttribute(APP_USER_STORE);
        // 第一次使用前，需要初始化
        if (activeUserStore == null) {
            activeUserStore = new ActiveUserStore();
            application.setAttribute(APP_USER_STORE, activeUserStore);
        }
        activeUserStore.addUser(this.username);
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        ServletContext application = session.getServletContext();

        // 从在线列表中删除用户名
        ActiveUserStore activeUserStore = (ActiveUserStore) application.getAttribute(APP_USER_STORE);
        activeUserStore.removeUser(this.username);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}