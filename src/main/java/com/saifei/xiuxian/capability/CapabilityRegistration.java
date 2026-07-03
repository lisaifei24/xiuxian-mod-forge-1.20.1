package com.saifei.xiuxian.capability;

import com.saifei.xiuxian.capability.CapabilityRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CapabilityRegistration {

    // ✅ 新增：全局唯一的 ResourceLocation 标识符（必须与 MOD_ID 一致）
    public static final ResourceLocation CULTIVATION_CAP_ID =
            new ResourceLocation("xiuxian", "cultivation");

    // ✅ Capability 实例（字段名以你实际代码为准）
    public static final Capability<ICultivation> CULTIVATION_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(IEventBus eventBus) {
        eventBus.addListener(CapabilityRegistration::setup);
    }

    private static void setup(final FMLCommonSetupEvent event) {
        // 留空即可，Capability 已通过 CapabilityManager.get() 自动注册
    }
}