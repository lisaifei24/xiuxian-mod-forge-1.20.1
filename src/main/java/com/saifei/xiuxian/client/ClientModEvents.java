package com.saifei.xiuxian.client;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.entity.ModEntities;
import com.saifei.xiuxian.entity.client.FireballProjectileRenderer;
import com.saifei.xiuxian.menu.ModMenuTypes;
import com.saifei.xiuxian.screen.RefiningFurnaceScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = XiuXianMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MEDITATION_SEAT.get(), MeditationSeatRenderer::new);
        // 第二阶段：火球术抛射物（空渲染器，视觉由服务器粒子呈现）
        event.registerEntityRenderer(ModEntities.FIREBALL_PROJECTILE.get(), FireballProjectileRenderer::new);
    }

    // ✅ 改用 FMLClientSetupEvent，在里面调用 MenuScreens.register
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.REFINING_FURNACE_MENU.get(), RefiningFurnaceScreen::new);
        });
    }
}