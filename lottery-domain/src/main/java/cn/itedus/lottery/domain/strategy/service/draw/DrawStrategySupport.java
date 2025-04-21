package cn.itedus.lottery.domain.strategy.service.draw;

import cn.itedus.lottery.domain.strategy.model.aggregates.StrategyRich;
import cn.itedus.lottery.domain.strategy.repository.IStrategyRepository;
import cn.itedus.lottery.infrastructure.po.Award;

import javax.annotation.Resource;

/**
 * 你的理解不完全正确。`@Resource` 注解虽然是 Java 的标准注解（来自 `javax.annotation`），但它的依赖注入功能仍然依赖于 Spring 容器的管理。如果一个类没有被 Spring 容器管理（例如没有被标注为 `@Component`、`@Service` 等，或者没有通过配置显式注册为 Bean），那么 `@Resource` 注解不会生效，属性字段也无法被注入。
 *
 * ### 原因
 * - `@Resource` 的注入功能需要 Spring 容器来解析和管理依赖。
 * - 如果类本身未被 Spring 容器管理，Spring 无法识别该类，也无法为其注入依赖。
 *
 * ### 总结
 * 当前 `DrawStrategySupport` 类没有被 Spring 容器管理，因此 `@Resource` 注解不会生效，`strategyRepository` 属性无法被注入。要使注入生效，需要将该类注册为 Spring Bean。
 */
public class DrawStrategySupport  extends DrawConfig{

    @Resource
    protected IStrategyRepository strategyRepository;

    protected StrategyRich queryStrategyRich(Long strategyId) {
        return strategyRepository.queryStrategyRich(strategyId);
    }

    protected Award queryAwardInfoByAwardId(String awardId) {
        return strategyRepository.queryAwardInfo(awardId);
    }
}
