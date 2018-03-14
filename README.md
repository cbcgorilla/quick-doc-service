# quick-doc-service 快速文档共享服务
概要： 该服务是研究 Spring Framework 5.0 / Spring Boot 2.0 / Kotlin / MongoDB / TensorFlow 期间探索代码功能实现的一个简单文件共享服务系统。

功能说明：

1. 用户登录，鉴权，目录分权限管理，共享设置，文件上传，下载，分类管理。

2. 文件夹管理，批量下载, 账号及权限组管理。

2. 批量打包下载仅下载权限可访问的资源, PDF及图片格式文件预览功能。

3. 异步消息发送到Kafka中间件平台，监控用户登录状态，文件上传及下载信息。.

4. Swagger UI文档化REST API接口。

5. 引入TensorFlow引擎自动分析图片，增加图片标签

**系统启动即可使用，无需数据初始化脚本，默认用户名/密码：_admin/chenbichao_**

# 架构组成： 
| **系统组件** |  **开源产品**| 
| ------   |:------:|
| 数据存储 | MongoDB 3.6.2  |
| 容器框架 | Spring Framework 5.0.4.RELEASE  |
| 系统框架 | Spring Boot 2.0.0.RELEASE |
| 状态监控 | Spring Actuator  |
| 安全框架 | Spring Security 5.0.3.RELEASE  |
| 实体Bean映射 | Kotlin 1.2  |
| 页面模板 | Thymeleaf  |
| WEB UI | Bootstrap 4.0  |
| WEB UI | Core UI  | 
| WEB UI | font-awesome 5.0.6  |
| WEB UI | LayUI  |
| 消息流 |  Kafka | 
| REST API |  Springfox Swagger 2.8.0 | 
| REST API |  swagger-bootstrap-ui 1.7.2 | 
| 系统构建 |  Gradle 4.6  |

## 待办：

1. 启用 Spring Security 功能会屏蔽Spring Boot Actuator默认的Endpoint注册, 需在application.properties文件显示配置.

2. csrf() 在Spring Security 内默认启用CSRF，会屏蔽POST提交任务, 通过csrf().disable()关闭CSRF。

3. 后台管理功能：文件夹管理，用户管理，分类管理, @TODO, 文件夹编辑，修改密码

4. 前端显示优化：文件夹图标，下载用图标替换按钮

5. Android客户端： 自动同步本地图片文件

6. 剥离TensorFlow图像分析模块，内存消耗问题。

Lombok 在 Java 9 环境导致编译异常

含Kotlin与Java代码的工程在MAVEN脚本编译过程会频繁出现找不到Kotlin类的错误，原因还未排查到。
目前可通过在Gradle环境下实现完整编译和打包不出错。

分离后端存储与前端web界面为2个独立工程, 后端存储可配合多项工程作共享存储用;
继续完善该文件服务器的其他功能，包括文件批量转储，移动端APP，文件自动同步功能等

## 界面截图
![用户管理界面](https://raw.githubusercontent.com/cbcgorilla/quick-doc-service/master/image_src/user-management-ui.png)

![文件访问界面](https://raw.githubusercontent.com/cbcgorilla/quick-doc-service/master/image_src/file-management-ui.png)

![文件夹配置界面](https://raw.githubusercontent.com/cbcgorilla/quick-doc-service/master/image_src/folder-management-ui.png)

![REST API接口](https://raw.githubusercontent.com/cbcgorilla/quick-doc-service/master/image_src/restapi.png)