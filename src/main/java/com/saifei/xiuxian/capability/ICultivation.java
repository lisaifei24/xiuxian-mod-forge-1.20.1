package com.saifei.xiuxian.capability;

import java.util.Set;

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

    // ============ 功法（技能）系统 ============

    /** 是否已学会指定技能 */
    boolean hasLearnedSkill(String skill);

    /** 学会指定技能 */
    void learnSkill(String skill);

    /** 获取已学会的全部技能名集合 */
    Set<String> getLearnedSkills();

    /** 获取当前激活的功法名（可为 null） */
    String getActiveSkill();

    /** 设置当前激活的功法名 */
    void setActiveSkill(String skill);

    /** 获取某技能冷却结束时间（毫秒时间戳，0 表示没有设置过） */
    long getSkillCooldownEnd(String skill);

    /** 设置某技能冷却结束时间（毫秒时间戳） */
    void setSkillCooldownEnd(String skill, long endTimeMs);

    /** 判断某技能是否处于冷却中 */
    boolean isSkillOnCooldown(String skill, long nowMs);
}