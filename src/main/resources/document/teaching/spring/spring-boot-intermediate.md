# Spring Boot 快速开发

## 适用人群
已了解Spring核心概念，希望快速上手Spring Boot开发的开发者。

## 什么是Spring Boot
Spring Boot是Spring的脚手架，通过"约定优于配置"的理念，极大简化了Spring应用的搭建和开发。

核心特性：
- 自动配置（Auto-Configuration）
- 内嵌服务器（Tomcat/Jetty）
- Starter依赖简化配置
- Actuator生产监控

---

## 自动配置原理
Spring Boot通过 `@SpringBootApplication` 启动，它包含三个核心注解：

```java
@SpringBootApplication
// 等价于：
// @SpringBootConfiguration  - 标记为配置类
// @EnableAutoConfiguration  - 开启自动配置
// @ComponentScan            - 扫描当前包及子包的组件

public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

**自动配置工作原理：**
1. Spring Boot扫描所有jar包中的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
2. 根据条件注解（`@ConditionalOnClass`、`@ConditionalOnMissingBean`等）决定是否生效
3. 自动创建并配置Bean，无需手动配置

---

## Starter依赖
Starter是一组预定义的依赖集合，引入一个Starter即可获得完整功能。

```xml
<!-- pom.xml -->
<!-- Web开发：自动配置Spring MVC + 内嵌Tomcat -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 数据库：自动配置JPA + HikariCP连接池 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- 测试：JUnit5 + Mockito + Spring Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 配置文件
Spring Boot使用 `application.yml` 或 `application.properties` 进行配置。

```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: ${DB_PASSWORD}  # 从环境变量读取，避免硬编码密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

# 自定义配置
app:
  jwt-secret: mySecretKey
  token-expiry: 86400
```

**读取自定义配置：**
```java
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String jwtSecret;
    private int tokenExpiry;
    // getter/setter...
}
```

---

## 多环境配置
通过 Profile 管理不同环境的配置。

```yaml
# application.yml（公共配置）
spring:
  profiles:
    active: dev  # 激活dev环境

---
# application-dev.yml（开发环境）
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # 内存数据库

---
# application-prod.yml（生产环境）
spring:
  datasource:
    url: jdbc:mysql://prod-server:3306/mydb
```

---

## 构建REST API
Spring Boot + Spring MVC 快速构建RESTful接口。

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Actuator 监控
Spring Boot Actuator提供生产级监控端点。

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

访问 `/actuator/health` 查看应用健康状态，`/actuator/metrics` 查看性能指标。

---

## 常见问题
Q: Spring Boot 项目启动慢怎么优化？
A: 使用懒加载（`spring.main.lazy-initialization=true`）、减少不必要的自动配置（`@SpringBootApplication(exclude=...)`）、使用GraalVM原生镜像。

Q: 如何自定义自动配置？
A: 创建 `@Configuration` 类，使用 `@ConditionalOnMissingBean` 等条件注解，在 `META-INF/spring/` 下注册配置类。

---

## 学习建议
掌握Spring Boot后，建议学习Spring AI，它将AI能力无缝集成到Spring Boot应用中。
