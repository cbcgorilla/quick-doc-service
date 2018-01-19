# quick-doc-service
功能说明：简易登录，文件上传，下载，分类管理，文件夹管理，批量下载。

分权限管理目录，批量打包下载仅下载权限可访问的资源。
异步发送登录信息到Kafka消息平台，监控用户登录状态.

架构组成： 

存储（MongoDB 3.6.2）

服务端：Spring Framework 5.0.2.RELEASE

Spring Boot 2.0 Actuator 提供MongoDB状态及系统配置信息监控点

Spring Security 5.0.0.RELEASE

Kotlin 1.2 实现实体映射Bean

Web层： BootStrap 4.0， + font-awesome.css 5.0 + Thymeleaf

Springfox Swagger 2.8.0 实现REST API文档化交付界面

## 待办：

1. 启用 Spring Security 功能会屏蔽Spring Boot Actuator默认的Endpoint注册, 需在application.properties文件显示配置.

2. csrf() 在Spring Security 内默认启用CSRF，会屏蔽POST提交任务, 通过csrf().disable()关闭CSRF。

3. 后台管理功能：文件夹管理，用户管理，分类管理

4. 前端显示优化：文件夹图标，下载用图标替换按钮

5. Android客户端： 自动同步本地图片文件

Lombok在 Java 9环境导致编译异常