package com.saifei.xiuxian.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class Cultivation implements ICultivation, INBTSerializable<CompoundTag> {

    // ✅ 实际存储的数据字段
    private Realm realm = Realm.MORTAL;          // 默认为凡人
    private int spiritualPower = 0;              // 当前灵力

    // ============== 实现 ICultivation 接口的方法 ==============
    @Override
    public Realm getRealm() { return realm; }

    @Override
    public void setRealm(Realm realm) {
        this.realm = realm;
        // 突破境界时，如果当前灵力超过新境界上限，将其限制在上限
        if (this.spiritualPower > getMaxSpiritualPower()) {
            this.spiritualPower = getMaxSpiritualPower();
        }
    }

    @Override
    public int getSpiritualPower() { return spiritualPower; }

    @Override
    public void setSpiritualPower(int power) { this.spiritualPower = power; }

    @Override
    public int getMaxSpiritualPower() {
        return realm.getMaxSpiritualPower(); // 直接从枚举获取上限
    }

    @Override
    public void addSpiritualPower(int amount) {
        this.spiritualPower += amount;
        if (this.spiritualPower > getMaxSpiritualPower()) {
            this.spiritualPower = getMaxSpiritualPower();
        }
        if (this.spiritualPower < 0) {
            this.spiritualPower = 0;
        }
    }

    // ============== INBTSerializable 接口：用于存档/读档 ==============
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // 注意：现在我们把境界存成了字符串
        if (nbt.contains("realm")) {
            this.realm = Realm.valueOf(nbt.getString("realm"));
        }
        this.spiritualPower = nbt.getInt("spiritualPower");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        // 注意：把枚举的名字存成字符串（比存数字更安全）
        tag.putString("realm", this.realm.name());
        tag.putInt("spiritualPower", this.spiritualPower);
        return tag;
    }
}