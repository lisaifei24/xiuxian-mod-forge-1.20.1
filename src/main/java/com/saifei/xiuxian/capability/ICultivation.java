package com.saifei.xiuxian.capability;

// 这是“修仙数据”的接口，定义玩家能做什么（读/写数据）
public interface ICultivation {

    // ✅ 获取境界（返回枚举类型）
    Realm getRealm();

    // ✅ 设置境界
    void setRealm(Realm realm);

    // 获取灵力值
    int getSpiritualPower();

    // 设置灵力值
    void setSpiritualPower(int power);

    // 获取最大灵力（上限，由当前境界自动决定，所以没有 setMax 方法）
    int getMaxSpiritualPower();

    // 增加灵力（比如打坐和使用灵石恢复）
    void addSpiritualPower(int amount);
}