package cn.itedus.lottery.domain.strategy.service.algorithm;

import cn.itedus.lottery.domain.strategy.model.vo.AwardRateInfo;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 * 公众号：bugstack虫洞栈
 * Create by 小傅哥(fustack)
 * <p>
 * 共用的算法逻辑
 */
public abstract class BaseAlgorithm implements IDrawAlgorithm {

    // 斐波那契散列增量，逻辑：黄金分割点：(√5 - 1) / 2 = 0.6180339887，Math.pow(2, 32) * 0.6180339887 = 0x61c88647
    /**
     * 是的，`Math.pow(2, 32)` 是因为 `int` 类型在 Java 中占用 4 字节（32 位），所以这里计算的是 2 的 32 次方，表示 32 位整数的取值范围（无符号情况下的最大值 + 1）。
     *
     * 在这段代码中，`HASH_INCREMENT` 的值 `0x61c88647` 是基于黄金分割比例 `(√5 - 1) / 2` 计算出来的，用于实现斐波那契散列法。`Math.pow(2, 32)` 的作用是将黄金分割比例映射到 32 位整数范围内，从而生成一个高效的散列增量值。
     *
     * 总结：
     * - `Math.pow(2, 32)` 是因为 `int` 是 32 位。
     * - 斐波那契散列法利用黄金分割点和 32 位整数范围来生成均匀分布的哈希值。
     */
    private final int HASH_INCREMENT = 0x61c88647;

    // 数组初始化长度
    /**
     * `RATE_TUPLE_LENGTH` 被设置为 128 的原因可能与以下几点有关：
     *
     * 1. **性能优化**:
     *    128 是 2 的幂（\(2^7\)），在计算机中，使用 2 的幂作为数组长度可以优化位运算操作（如取模运算）。在 `hashIdx` 方法中，`hashCode & (RATE_TUPLE_LENGTH - 1)` 利用位运算快速计算索引，而不是使用取模操作，这样可以提高性能。
     *
     * 2. **散列均匀性**:
     *    斐波那契散列法依赖黄金分割点生成均匀分布的哈希值。128 的长度足够小以节省内存，同时足够大以减少哈希冲突，确保概率分布的均匀性。
     *
     * 3. **内存与需求的平衡**:
     *    128 是一个合理的默认值，既不会占用过多内存，又能满足大多数抽奖策略的需求。如果奖品数量较多或概率分布更复杂，可以根据需求调整这个值。
     *
     * 4. **扩展性**:
     *    128 的长度为抽奖算法提供了足够的分区空间，支持将概率（如 0.01, 0.05 等）映射到数组的不同区间，确保算法的准确性。
     *
     * 如果需要更高的精度或更大的概率分布范围，可以调整 `RATE_TUPLE_LENGTH` 的值，但需要确保它是 2 的幂以保持位运算的高效性。
     */
    private final int RATE_TUPLE_LENGTH = 128;

    // 存放概率与奖品对应的散列结果，strategyId -> rateTuple
    protected Map<Long, String[]> rateTupleMap = new ConcurrentHashMap<>();

    // 奖品区间概率值，strategyId -> [awardId->begin、awardId->end]
    protected Map<Long, List<AwardRateInfo>> awardRateInfoMap = new ConcurrentHashMap<>();

    @Override
    public void initRateTuple(Long strategyId, List<AwardRateInfo> awardRateInfoList) {
        // 保存奖品概率信息
        awardRateInfoMap.put(strategyId, awardRateInfoList);

        String[] rateTuple = rateTupleMap.computeIfAbsent(strategyId, k -> new String[RATE_TUPLE_LENGTH]);
        awardRateInfoList.sort(Comparator.comparing(AwardRateInfo::getAwardRate));
        int cursorVal = 0;
        for (AwardRateInfo awardRateInfo : awardRateInfoList) {
            int rateVal = awardRateInfo.getAwardRate().multiply(new BigDecimal(100)).intValue();

            // 循环填充概率范围值
            for (int i = cursorVal + 1; i <= (rateVal + cursorVal); i++) {
                /**
                 * 是的，`rateTuple[hashIdx(i)] = awardRateInfo.getAwardId();` 这一行代码可能会出现哈希冲突。
                 *
                 * ### 原因
                 * 1. **斐波那契散列法**:
                 *    - `hashIdx(i)` 使用斐波那契散列法计算索引，虽然这种方法能生成均匀分布的哈希值，但由于 `RATE_TUPLE_LENGTH` 是固定的（128），当 `i` 的值较大或分布密集时，可能会出现多个 `i` 计算出的索引相同的情况。
                 *
                 * 2. **覆盖问题**:
                 *    - 如果 `hashIdx(i)` 计算出的索引已经被其他奖品的 `awardId` 占用，当前的 `awardId` 会覆盖之前的值，导致概率分布不准确。
                 *
                 * ### 解决方法
                 * 为了避免哈希冲突，可以考虑以下改进：
                 * 1. **冲突检测**:
                 *    - 在写入 `rateTuple` 时，检查目标索引是否已经有值，如果有值，可以选择跳过或记录冲突。
                 *
                 * 2. **开放寻址法**:
                 *    - 如果发生冲突，可以通过线性探测或二次探测等方法寻找下一个空闲位置。
                 *
                 * 3. **增加数组长度**:
                 *    - 增大 `RATE_TUPLE_LENGTH` 的值（如 256 或更大），以减少冲突的概率。
                 *
                 * 4. **链地址法**:
                 *    - 将 `rateTuple` 替换为一个数组的链表结构，在发生冲突时，将冲突的值存储在链表中。
                 *
                 * 如果需要实现冲突检测或其他改进，可以进一步调整代码逻辑。
                 */
                rateTuple[hashIdx(i)] = awardRateInfo.getAwardId();
            }

            cursorVal += rateVal;

        }
    }

    @Override
    public boolean isExistRateTuple(Long strategyId) {
        return rateTupleMap.containsKey(strategyId);
    }

    /**
     * 斐波那契（Fibonacci）散列法，计算哈希索引下标值
     * `hashIdx(int val)` 方法使用了斐波那契散列法来计算哈希索引下标值。以下是对这段代码的分析及举例：
     *
     * ### **代码分析**
     * 1. **斐波那契散列法**:
     *    - 斐波那契散列法是一种基于黄金分割比例的哈希算法，利用黄金分割点 `(√5 - 1) / 2 ≈ 0.618` 来生成均匀分布的哈希值。
     *    - `HASH_INCREMENT = 0x61c88647` 是黄金分割点映射到 32 位整数范围的值，用于生成散列增量。
     *
     * 2. **计算逻辑**:
     *    - `hashCode = val * HASH_INCREMENT + HASH_INCREMENT`:
     *      - 通过 `val` 和 `HASH_INCREMENT` 的乘法生成一个散列值。
     *      - 加上 `HASH_INCREMENT` 是为了避免 `val = 0` 时散列值为 0。
     *    - `hashCode & (RATE_TUPLE_LENGTH - 1)`:
     *      - 通过位运算取模，将散列值映射到数组的索引范围 `[0, RATE_TUPLE_LENGTH - 1]`。
     *      - 这里 `RATE_TUPLE_LENGTH` 是 128（2 的幂），位运算比取模运算更高效。
     *
     * 3. **优点**:
     *    - **均匀分布**: 黄金分割点的特性使得散列值分布更加均匀，减少哈希冲突。
     *    - **高效**: 使用位运算代替取模运算，性能更高。
     *
     * ---
     *
     * ### **举例**
     * 假设 `RATE_TUPLE_LENGTH = 128`，`HASH_INCREMENT = 0x61c88647`，我们计算几个示例值的哈希索引：
     *
     * #### 示例 1: `val = 1`
     * - `hashCode = 1 * 0x61c88647 + 0x61c88647 = 0xC3910C8E`
     * - `hashIdx = 0xC3910C8E & (128 - 1) = 0xC3910C8E & 0x7F = 14`
     *
     * #### 示例 2: `val = 2`
     * - `hashCode = 2 * 0x61c88647 + 0x61c88647 = 0x125B98CD`
     * - `hashIdx = 0x125B98CD & (128 - 1) = 0x125B98CD & 0x7F = 29`
     *
     * #### 示例 3: `val = 3`
     * - `hashCode = 3 * 0x61c88647 + 0x61c88647 = 0x18F61D0C`
     * - `hashIdx = 0x18F61D0C & (128 - 1) = 0x18F61D0C & 0x7F = 44`
     *
     * ---
     *
     * ### **总结**
     * - 这段代码通过斐波那契散列法生成均匀分布的哈希值，并将其映射到数组索引范围内。
     * - 这种方法适用于需要高效、均匀分布的场景，例如抽奖算法中的概率分区映射。
     * - 通过 `hashIdx` 方法，可以快速定位到数组中的索引位置，降低时间复杂度。
     * @param val 值
     * @return 索引
     */
    protected int hashIdx(int val) {
        int hashCode = val * HASH_INCREMENT + HASH_INCREMENT;
        return hashCode & (RATE_TUPLE_LENGTH - 1);
    }

    protected int generateSecureRandomIntCode(int bound) {
        return new SecureRandom().nextInt(bound) + 1;
    }
}
