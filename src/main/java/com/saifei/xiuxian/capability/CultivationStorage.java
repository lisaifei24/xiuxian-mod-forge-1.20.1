package com.saifei.xiuxian.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CultivationStorage implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    // ✅ 注册 Capability 令牌
    public static final Capability<ICultivation> CULTIVATION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final Cultivation cultivation = new Cultivation();
    private final LazyOptional<ICultivation> optional = LazyOptional.of(() -> cultivation);

    public CultivationStorage(Cultivation cultivation) {
        // 这里的构造方法是用来对接你之前的 Attacher 的，如果 Attacher 传了实例进来就用传进来的
        // 不过通常只需保留无参构造或者直接使用默认实例即可
    }

    // 1. 获取能力实例
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CULTIVATION_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    // 2. 将能力数据保存到 NBT（存档）
    @Override
    public CompoundTag serializeNBT() {
        return cultivation.serializeNBT();
    }

    // 3. 从 NBT 读取能力数据（读档）
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        cultivation.deserializeNBT(nbt);
    }
}