package cn.itedus.lottery.domain.strategy.service.draw;

import cn.itedus.lottery.domain.strategy.service.algorithm.IDrawAlgorithm;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 * 公众号：bugstack虫洞栈
 * Create by 小傅哥(fustack)
 */
public class DrawConfig {

    @Resource
    private IDrawAlgorithm defaultRateRandomDrawAlgorithm;

    @Resource
    private IDrawAlgorithm singleRateRandomDrawAlgorithm;

    protected static Map<Integer, IDrawAlgorithm> drawAlgorithmMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        drawAlgorithmMap.put(1, defaultRateRandomDrawAlgorithm);
        drawAlgorithmMap.put(2, singleRateRandomDrawAlgorithm);
    }

}
/**
 * 在当前代码中，`DrawConfig` 类没有使用 Spring 的注解（如 `@Component`、`@Service`、`@Configuration` 等）来标识为 Spring 容器的 Bean，因此默认情况下它不会被 Spring 容器扫描和加载。
 *
 * 虽然类成员使用了 `@Resource` 注解，并且方法使用了 `@PostConstruct` 注解，但这些注解的功能依赖于该类本身被 Spring 容器管理。如果 `DrawConfig` 没有被 Spring 容器加载，这些注解将不会生效。
 *
 * ### 解决方法
 * 如果希望 `DrawConfig` 被 Spring 容器管理并使注解生效，可以通过以下方式之一：
 *
 * 1. **添加 `@Component` 注解**：
 *    在类上添加 `@Component` 注解，并确保所在包被 Spring 的组件扫描路径包含。
 *
 *    ```java
 *    @Component
 *    public class DrawConfig {
 *        // existing code
 *    }
 *    ```
 *
 * 2. **使用 `@Configuration` 注解**：
 *    如果 `DrawConfig` 是一个配置类，可以使用 `@Configuration` 注解。
 *
 *    ```java
 *    @Configuration
 *    public class DrawConfig {
 *        // existing code
 *    }
 *    ```
 *
 * 3. **通过 Java 配置显式注册**：
 *    在一个 `@Configuration` 类中通过 `@Bean` 显式注册 `DrawConfig`。
 *
 *    ```java
 *    @Configuration
 *    public class AppConfig {
 *        @Bean
 *        public DrawConfig drawConfig() {
 *            return new DrawConfig();
 *        }
 *    }
 *    ```
 *
 * ### 总结
 * 目前的代码中，`DrawConfig` 不会被 Spring 容器管理，因此 `@Resource` 和 `@PostConstruct` 不会生效。需要通过上述方法之一将其注册为 Spring Bean，才能使这些注解正常工作。
 */
