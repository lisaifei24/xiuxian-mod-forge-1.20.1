package com.saifei.xiuxian.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CapabilityRegistration {

    // 1.20.1 使用构造器（parse 为 1.21 API）
    public static final ResourceLocation CULTIVATION_CAP_ID =
            new ResourceLocation("xiuxian:cultivation");

    public static final Capability<ICultivation> CULTIVATION_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(IEventBus eventBus) {
        eventBus.addListener(CapabilityRegistration::setup);
    }

    private static void setup(final FMLCommonSetupEvent event) {
        // 无需额外操作
    }
}