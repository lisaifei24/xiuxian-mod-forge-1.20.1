package com.saifei.xiuxian.capability;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = XiuXianMod.MOD_ID)
public class CultivationCapabilityAttacher {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;

        ResourceLocation capId = new ResourceLocation(XiuXianMod.MOD_ID, "cultivation");
        if (!event.getCapabilities().containsKey(capId)) {
            event.addCapability(capId, new CultivationStorage(new Cultivation()));
        }
    }

    // ✅ 新增：玩家登录/进入世界时，服务端向客户端同步真实灵力数据
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                // 【关键修改】：改掉 sendToPlayer，使用标准的 PacketDistributor
                XiuXianMod.NETWORK.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm())
                );

                XiuXianMod.LOGGER.info("✅ 已向 {} 同步灵力: {}/{}",
                        serverPlayer.getName().getString(),
                        cap.getSpiritualPower(),
                        cap.getMaxSpiritualPower());
            });
        }
    }
}