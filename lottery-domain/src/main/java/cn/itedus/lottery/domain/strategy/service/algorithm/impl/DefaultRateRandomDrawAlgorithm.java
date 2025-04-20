package cn.itedus.lottery.domain.strategy.service.algorithm.impl;

import cn.itedus.lottery.domain.strategy.model.vo.AwardRateInfo;
import cn.itedus.lottery.domain.strategy.service.algorithm.BaseAlgorithm;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * 必中奖策略抽奖，排掉已经中奖的概率，重新计算中奖范围
 *
 */
@Component("defaultRateRandomDrawAlgorithm")
public class DefaultRateRandomDrawAlgorithm extends BaseAlgorithm {


    @Override
    public String randomDraw(Long strategyId, List<String> excludeAwardIds) {
        BigDecimal availableAwardRate = BigDecimal.ZERO;
        List<AwardRateInfo> availableAwardRateList = new ArrayList<>();
        List<AwardRateInfo> awardRateIntervalValList = awardRateInfoMap.get(strategyId);
        awardRateIntervalValList.stream().filter(e -> !excludeAwardIds.contains(e.getAwardId())).forEach(availableAwardRateList::add);
        for (int i = 0; i < availableAwardRateList.size(); i++) {
            availableAwardRate = availableAwardRate.add(availableAwardRateList.get(i).getAwardRate());
        }

        if (availableAwardRateList.size() == 0) {
            return "";
        }
        if (availableAwardRateList.size() == 1) {
            return availableAwardRateList.get(0).getAwardId();
        }

        int randomVal = new SecureRandom().nextInt(100) + 1;
        // 循环获取奖品
        String awardId = "";
        int cursorVal = 0;
        availableAwardRateList.sort(Comparator.comparing(AwardRateInfo::getAwardRate));
        for (AwardRateInfo awardRateInfo : availableAwardRateList) {
            int rateVal = awardRateInfo.getAwardRate().divide(availableAwardRate, 2, BigDecimal.ROUND_UP).multiply(new BigDecimal(100)).intValue();
            if (randomVal <= (cursorVal + rateVal)) {
                awardId = awardRateInfo.getAwardId();
                break;
            }
            cursorVal += rateVal;
        }

        // 返回中奖结果
        return awardId;
    }
}
