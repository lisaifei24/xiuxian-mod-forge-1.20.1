package com.saifei.xiuxian.skill;

/**
 * 功法 / 法术枚举。
 * 每种功法包含：显示名、技能键名、灵力消耗、冷却时间（秒）以及效果类型。
 */
public enum SkillType {

    YUJIAN_SHU("御剑术", "yujian_shu", 30, 2, SkillKind.MELEE_STRIKE),
    HUOQIU_SHU("火球术", "huoqiu_shu", 20, 1, SkillKind.FIREBALL),
    HUTI_JINGUANG("护体金光", "huti_jinguang", 25, 8, SkillKind.SHIELD);

    /** 技能效果类型 */
    public enum SkillKind {
        /** 御剑术：直线斩击，对视线目标造成伤害 */
        MELEE_STRIKE,
        /** 火球术：发射一枚灵力火球实体 */
        FIREBALL,
        /** 护体金光：为自己附加吸收/抗性护盾 */
        SHIELD
    }

    private final String displayName;
    private final String registrySuffix;
    private final int spiritualCost;
    private final int cooldownSeconds;
    private final SkillKind kind;

    SkillType(String displayName, String registrySuffix, int spiritualCost, int cooldownSeconds, SkillKind kind) {
        this.displayName = displayName;
        this.registrySuffix = registrySuffix;
        this.spiritualCost = spiritualCost;
        this.cooldownSeconds = cooldownSeconds;
        this.kind = kind;
    }

    public String getDisplayName() { return displayName; }
    public String getRegistrySuffix() { return registrySuffix; }
    public int getSpiritualCost() { return spiritualCost; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public SkillKind getKind() { return kind; }

    // 冷却时间换算为毫秒
    public long getCooldownMillis() { return cooldownSeconds * 1000L; }

    /** 根据名称查找技能（用于网络包反序列化），找不到返回 null */
    public static SkillType byName(String name) {
        for (SkillType s : values()) {
            if (s.name().equalsIgnoreCase(name)) return s;
        }
        return null;
    }
}
