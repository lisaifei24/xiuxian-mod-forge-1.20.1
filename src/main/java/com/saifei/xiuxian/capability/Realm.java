package com.saifei.xiuxian.capability;

public enum Realm {
    // 构造参数含义：(显示名, 灵力上限, 攻击力加成, 护甲值加成, 移速加成倍数)
    MORTAL("凡人", 50, 0, 0, 0.0),
    QI_REFINING("炼气", 100, 2, 1, 0.05),
    FOUNDATION("筑基", 500, 8, 5, 0.15),
    GOLDEN_CORE("金丹", 2000, 20, 15, 0.30);

    private final String displayName;
    private final int maxSpiritualPower;
    private final int attackBonus;   // 攻击力提升
    private final int armorBonus;    // 护甲值提升
    private final double speedBonus; // 移速加成 (0.05 = +5%)

    Realm(String displayName, int maxSpiritualPower, int attackBonus, int armorBonus, double speedBonus) {
        this.displayName = displayName;
        this.maxSpiritualPower = maxSpiritualPower;
        this.attackBonus = attackBonus;
        this.armorBonus = armorBonus;
        this.speedBonus = speedBonus;
    }

    public String getDisplayName() { return displayName; }
    public int getMaxSpiritualPower() { return maxSpiritualPower; }
    public int getAttackBonus() { return attackBonus; }
    public int getArmorBonus() { return armorBonus; }
    public double getSpeedBonus() { return speedBonus; }
}