package com.saifei.xiuxian.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CapabilityRegistration {

    // 修改：使用 ResourceLocation.parse 避免弃用警告
    public static final ResourceLocation CULTIVATION_CAP_ID =
            ResourceLocation.parse("xiuxian:cultivation");

    public static final Capability<ICultivation> CULTIVATION_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(IEventBus eventBus) {
        eventBus.addListener(CapabilityRegistration::setup);
    }

    private static void setup(final FMLCommonSetupEvent event) {
        // 无需额外操作
    }
}