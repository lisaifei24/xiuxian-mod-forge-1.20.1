package com.saifei.xiuxian.client;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XiuXianMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 把我们的不可见实体注册给空的渲染器
        event.registerEntityRenderer(ModEntities.MEDITATION_SEAT.get(), MeditationSeatRenderer::new);
    }
}