package com.saifei.xiuxian.entity;

import com.saifei.xiuxian.XiuXianMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, XiuXianMod.MOD_ID);

    public static final RegistryObject<EntityType<MeditationSeatEntity>> MEDITATION_SEAT =
            ENTITIES.register("meditation_seat", () -> EntityType.Builder.of(MeditationSeatEntity::new, MobCategory.MISC)
                    .sized(0.0f, 0.0f) // 完全不可见的实体
                    .setTrackingRange(64) // ✅【关键修复】必须大于 0，否则客户端收不到实体！
                    .setUpdateInterval(20) // 每 20 tick 同步一次
                    .build("meditation_seat"));
}