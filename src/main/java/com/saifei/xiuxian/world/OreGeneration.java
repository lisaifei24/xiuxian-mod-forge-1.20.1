package com.saifei.xiuxian.world;

import com.saifei.xiuxian.XiuXianMod;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class OreGeneration {
    // ✅ 只需要这一个 DeferredRegister
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, XiuXianMod.MOD_ID);
}