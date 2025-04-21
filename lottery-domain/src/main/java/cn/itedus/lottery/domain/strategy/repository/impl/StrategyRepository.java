package cn.itedus.lottery.domain.strategy.repository.impl;

import cn.itedus.lottery.domain.strategy.model.aggregates.StrategyRich;
import cn.itedus.lottery.domain.strategy.repository.IStrategyRepository;
import cn.itedus.lottery.infrastructure.dao.IAwardDao;
import cn.itedus.lottery.infrastructure.dao.IStrategyDao;
import cn.itedus.lottery.infrastructure.dao.IStrategyDetailDao;
import cn.itedus.lottery.infrastructure.po.Award;
import cn.itedus.lottery.infrastructure.po.Strategy;
import cn.itedus.lottery.infrastructure.po.StrategyDetail;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyDao strategyDao;

    @Resource
    private IStrategyDetailDao strategyDetailDao;

    @Resource
    private IAwardDao awardDao;

    /**
     * 以下是一个使用 `@Resource` 按名称注入的示例：
     *
     * 假设有两个实现类 `ServiceA` 和 `ServiceB`，它们都实现了同一个接口 `IService`：
     *
     * ```java
     * package com.example.service;
     *
     * public interface IService {
     *     void execute();
     * }
     *
     * @Component("serviceA")
     * public class ServiceA implements IService {
     *     @Override
     *     public void execute() {
     *         System.out.println("Executing ServiceA");
     *     }
     * }
     *
     * @Component("serviceB")
     * public class ServiceB implements IService {
     *     @Override
     *     public void execute() {
     *         System.out.println("Executing ServiceB");
     *     }
     * }
     * ```
     *
     * 在需要注入的类中，通过 `@Resource` 指定名称来注入特定的实现类：
     *
     * ```java
     * package com.example.controller;
     *
     * import com.example.service.IService;
     * import org.springframework.stereotype.Component;
     *
     * import javax.annotation.Resource;
     *
     * @Component
     * public class MyController {
     *
     *     @Resource(name = "serviceA") // 按名称注入 ServiceA
     *     private IService service;
     *
     *     public void performAction() {
     *         service.execute();
     *     }
     * }
     * ```
     *
     * ### 说明
     * 1. `@Resource(name = "serviceA")` 明确指定了要注入的 Bean 名称为 `serviceA`。
     * 2. 如果不指定 `name` 属性，`@Resource` 会默认按字段名称匹配 Bean 名称。
     *
     * 运行时，`MyController` 的 `service` 字段会被注入 `ServiceA` 的实例。
     * @param strategyId
     * @return
     */

    @Override
    public StrategyRich queryStrategyRich(Long strategyId) {
        Strategy strategy = strategyDao.queryStrategy(strategyId);
        List<StrategyDetail> strategyDetails = strategyDetailDao.queryStrategyDetailList(strategyId);
        return new StrategyRich(strategyId, strategy, strategyDetails);
    }

    @Override
    public Award queryAwardInfo(String awardId) {
        return awardDao.queryAwardInfo(awardId);
    }

    @Override
    public List<String> queryNoStockStrategyAwardList(Long strategyId) {
        return strategyDetailDao.queryNoStockStrategyAwardList(strategyId);
    }

    @Override
    public boolean deductStock(Long strategyId, String awardId) {
        StrategyDetail req = new StrategyDetail();
        req.setStrategyId(strategyId);
        req.setAwardId(awardId);
        int count = strategyDetailDao.deductStock(req);
        return count == 1;
    }
}
