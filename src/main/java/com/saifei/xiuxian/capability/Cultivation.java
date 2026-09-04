package com.saifei.xiuxian.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Cultivation implements ICultivation, INBTSerializable<CompoundTag> {

    // ✅ 实际存储的数据字段
    private Realm realm = Realm.MORTAL;          // 默认为凡人
    private int spiritualPower = 0;              // 当前灵力

    // ============ 功法（技能）覆写的字段 ============
    private final Set<String> learnedSkills = new HashSet<>();
    private String activeSkill = null;
    private final java.util.Map<String, Long> skillCooldowns = new ConcurrentHashMap<>();

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
            try {
                this.realm = Realm.valueOf(nbt.getString("realm"));
            } catch (IllegalArgumentException e) {
                this.realm = Realm.MORTAL; // 旧存档/未知值兜底
            }
        }
        this.spiritualPower = nbt.getInt("spiritualPower");
        // 向后兼容读取：旧存档没有该字段时保持为空集合
        this.learnedSkills.clear();
        if (nbt.contains("learnedSkills")) {
            ListTag list = nbt.getList("learnedSkills", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                this.learnedSkills.add(list.getString(i));
            }
        }
        this.activeSkill = nbt.contains("activeSkill") ? nbt.getString("activeSkill") : null;
        this.skillCooldowns.clear();
        if (nbt.contains("skillCooldowns")) {
            CompoundTag cooldownTag = nbt.getCompound("skillCooldowns");
            for (String key : cooldownTag.getAllKeys()) {
                this.skillCooldowns.put(key, cooldownTag.getLong(key));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        // 注意：把枚举的名字存成字符串（比存数字更安全）
        tag.putString("realm", this.realm.name());
        tag.putInt("spiritualPower", this.spiritualPower);
        ListTag learned = new ListTag();
        for (String skill : this.learnedSkills) {
            learned.add(StringTag.valueOf(skill));
        }
        tag.put("learnedSkills", learned);
        if (this.activeSkill != null) {
            tag.putString("activeSkill", this.activeSkill);
        }
        CompoundTag cooldownTag = new CompoundTag();
        for (java.util.Map.Entry<String, Long> entry : this.skillCooldowns.entrySet()) {
            cooldownTag.putLong(entry.getKey(), entry.getValue());
        }
        tag.put("skillCooldowns", cooldownTag);
        return tag;
    }

    // ============== 功法（技能）系统实现 ==============

    @Override
    public boolean hasLearnedSkill(String skill) {
        return skill != null && learnedSkills.contains(skill);
    }

    @Override
    public void learnSkill(String skill) {
        if (skill != null) {
            learnedSkills.add(skill);
        }
    }

    @Override
    public Set<String> getLearnedSkills() {
        return learnedSkills;
    }

    @Override
    public String getActiveSkill() {
        // 若当前激活的功法未学过，则视为未激活
        if (activeSkill == null || !learnedSkills.contains(activeSkill)) {
            return null;
        }
        return activeSkill;
    }

    @Override
    public void setActiveSkill(String skill) {
        if (skill == null || learnedSkills.contains(skill)) {
            this.activeSkill = skill;
        }
    }

    @Override
    public long getSkillCooldownEnd(String skill) {
        return skill == null ? 0L : skillCooldowns.getOrDefault(skill, 0L);
    }

    @Override
    public void setSkillCooldownEnd(String skill, long endTimeMs) {
        if (skill != null) {
            skillCooldowns.put(skill, endTimeMs);
        }
    }

    @Override
    public boolean isSkillOnCooldown(String skill, long nowMs) {
        return skill != null && getSkillCooldownEnd(skill) > nowMs;
    }
}