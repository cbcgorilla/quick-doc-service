# quick-doc-service

1. 启用 Spring Security 功能会屏蔽Spring Boot Actuator默认的Endpoint注册

2. csrf() 在Spring Security 内默认启用CSRF，会屏蔽POST提交任务, 通过csrf().disable()关闭CSRF。

3. 