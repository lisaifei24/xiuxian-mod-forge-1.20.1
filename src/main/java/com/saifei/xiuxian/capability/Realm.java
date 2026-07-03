package com.saifei.xiuxian.capability;

public enum Realm {
    MORTAL("凡人", 50),
    QI_REFINING("炼气", 100),
    FOUNDATION("筑基", 500),
    GOLDEN_CORE("金丹", 2000);

    private final String displayName;
    private final int maxSpiritualPower;

    Realm(String displayName, int maxSpiritualPower) {
        this.displayName = displayName;
        this.maxSpiritualPower = maxSpiritualPower;
    }

    public String getDisplayName() { return displayName; }
    public int getMaxSpiritualPower() { return maxSpiritualPower; }
}