package cn.mxleader.quickdoc.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * WEB请求事件监听器
     * @param event
     */
    @EventListener
    public void requestHandledEventApplicationListener(RequestHandledEvent event){
        System.out.println("请求处理监听事件："+event);
    }

    /**
     * 配置拦截器的Bean
     *
     * @return
     */
    @Bean
    public WebHandlerInterceptor webHandlerInterceptor() {
        return new WebHandlerInterceptor();
    }

    /**
     * c重写addInterceptors方法，注册拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //super.addInterceptors(registry);
        registry.addInterceptor(webHandlerInterceptor());
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        //registry.addViewController("/").setViewName("index");
        registry.addViewController("/404").setViewName("404");
        registry.addViewController("/500").setViewName("500");
    }

}