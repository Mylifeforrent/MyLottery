# Lottery

抽奖系统# MyLottery
- lottery-rpc里面只是定义一些接口的描述信息，比如接口入参出参，dto等
- lottery-interfaces是定义一些接口的具体实现步骤和逻辑，但是呢它的实现逻辑所需要的req，res，dto等基本上都来自于lottery-rpc定义的那些，除非一些通用的对象来自于lottery-common
- lottery-infrastructure 就是封装基础功能了，数据库，redis等
- lottery-domain 封装具体的业务领域实现，聚合的充血的
- lottery-common 定义通用的返回对象，常量，枚举，异常之类的
- lotter-application 逻辑包装，编排，任务，事件的发布和订阅
![基本概念梳理.png](%E5%9F%BA%E6%9C%AC%E6%A6%82%E5%BF%B5%E6%A2%B3%E7%90%86.png)