package com.saifei.xiuxian.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.network.CastSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键绑定：R 键释放当前激活的功法。
 * 在 MOD 总线注册 KeyMapping，在 FORGE 总线监听按键按下并向服务端发送 CastSkillPacket。
 */
@Mod.EventBusSubscriber(modid = XiuXianMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

    public static final String CATEGORY = "key.categories.xiuxian";

    public static final KeyMapping CAST_SKILL = new KeyMapping(
            "key.xiuxian.cast_skill",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    /** 在 MOD 总线注册按键 */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CAST_SKILL);
        // 注册后立即在 FORGE 总线监听按下事件（静态类中通过构造器触发一次）
        MinecraftForge.EVENT_BUS.register(KeyHandler.class);
    }

    /** FORGE 总线上的按键处理（内部静态类，由 registerKeyMappings 手动注册避免重复） */
    public static class KeyHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (event.getAction() != GLFW.GLFW_PRESS) return;
            if (CAST_SKILL.consumeClick() || (event.getKey() == GLFW.GLFW_KEY_R && event.getModifiers() == 0)) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.screen == null) {
                    // 空技能名表示“释放当前激活的功法”
                    XiuXianMod.NETWORK.sendToServer(new CastSkillPacket(""));
                }
            }
        }
    }
}
