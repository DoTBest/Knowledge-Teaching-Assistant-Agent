# Spring核心概念

## 适用人群
已掌握Java OOP，准备学习Spring框架的开发者。

## 什么是Spring
Spring是Java生态中最流行的企业级开发框架，核心是IoC容器和AOP。它简化了Java开发，让开发者专注于业务逻辑而非基础设施代码。

Spring生态包括：Spring Framework、Spring Boot、Spring MVC、Spring Data、Spring Security等。

---

## IoC（控制反转）
IoC是Spring的核心思想：对象的创建和依赖关系的管理由Spring容器负责，而不是由开发者手动控制。

**传统方式（手动管理依赖）：**
```java
// 开发者负责创建对象，耦合度高
public class OrderService {
    private UserRepository userRepository = new UserRepository(); // 直接new
    private EmailService emailService = new EmailService();       // 直接new
}
```

**Spring IoC方式（容器管理依赖）：**
```java
@Service
public class OrderService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Spring自动注入依赖
    public OrderService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}
```

---

## DI（依赖注入）
DI是IoC的具体实现方式，Spring将依赖对象注入到需要它的类中。

**三种注入方式：**

```java
// 1. 构造器注入（推荐）：依赖不可变，便于测试
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// 2. Setter注入：可选依赖
@Service
public class NotificationService {
    private EmailService emailService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}

// 3. 字段注入（不推荐，难以测试）
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
}
```

---

## Bean与Spring容器
Bean是由Spring容器管理的对象，通过注解或XML配置声明。

```java
// 常用注解
@Component   // 通用组件
@Service     // 业务层
@Repository  // 数据访问层
@Controller  // 控制层（MVC）
@RestController // REST控制层

// 配置类中定义Bean
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        // 创建并配置数据源
        return new HikariDataSource();
    }
}
```

**Bean作用域：**
- `@Scope("singleton")`：默认，整个容器只有一个实例
- `@Scope("prototype")`：每次请求创建新实例
- `@Scope("request")`：每个HTTP请求一个实例（Web环境）

---

## AOP（面向切面编程）
AOP将横切关注点（日志、事务、权限）从业务逻辑中分离，避免代码重复。

```java
@Aspect
@Component
public class LoggingAspect {

    // 切点：匹配service包下所有方法
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceLayer() {}

    // 前置通知：方法执行前
    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("调用方法：" + joinPoint.getSignature().getName());
    }

    // 环绕通知：方法执行前后
    @Around("serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed(); // 执行目标方法
        long duration = System.currentTimeMillis() - start;
        System.out.println("方法耗时：" + duration + "ms");
        return result;
    }
}
```

---

## 事务管理
Spring通过 `@Transactional` 注解声明式管理事务，底层基于AOP实现。

```java
@Service
public class TransferService {

    @Transactional // 方法内所有数据库操作在同一事务中
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        accountRepository.deduct(fromId, amount);  // 扣款
        accountRepository.deposit(toId, amount);   // 入账
        // 如果任何一步抛出异常，整个事务回滚
    }

    @Transactional(readOnly = true) // 只读事务，性能优化
    public Account findById(Long id) {
        return accountRepository.findById(id).orElseThrow();
    }
}
```

---

## 常见问题
Q: @Autowired 和 @Resource 的区别？
A: `@Autowired` 是Spring注解，按类型注入；`@Resource` 是Java标准注解，按名称注入。推荐使用构造器注入替代两者。

Q: Spring Bean 默认是单例的，线程安全吗？
A: 单例Bean本身不是线程安全的。如果Bean有可变状态（成员变量），需要使用同步机制或将状态放在方法局部变量中。

---

## 学习建议
理解Spring核心后，建议学习Spring Boot，它大幅简化了Spring应用的配置和启动过程。
