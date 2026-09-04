package com.saifei.xiuxian.client;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.block.ModBlocks;
import com.saifei.xiuxian.entity.ModEntities;
import com.saifei.xiuxian.entity.client.FireballProjectileRenderer;
import com.saifei.xiuxian.menu.ModMenuTypes;
import com.saifei.xiuxian.screen.RefiningFurnaceScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
            // v1.4.1：灵草/仙草使用 cutout 透明渲染（十字植物模型）
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HUI_LING_CAO.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.NING_SHEN_HUA.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHI_YAN_GUO.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TIAN_SHAN_XUE_LIAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.JIE_DU_TENG.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.XUAN_TIE_LING_ZHI.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LONG_XUE_CAO.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.JIU_ZHUAN_HUAN_HUN_CAO.get(), RenderType.cutout());
        });
    }
}