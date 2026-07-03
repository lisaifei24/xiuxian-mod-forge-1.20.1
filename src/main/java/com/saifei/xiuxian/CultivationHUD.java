package com.saifei.xiuxian;

import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.capability.ICultivation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "xiuxian", value = Dist.CLIENT)
public class CultivationHUD {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        // 安全检查
        if (player == null || mc.screen != null) return;

        // 获取 Capability
        ICultivation cap = player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).orElse(null);
        if (cap == null) return;

        int currentSpirit = cap.getSpiritualPower();
        int maxSpirit = cap.getMaxSpiritualPower();
        String realmName = cap.getRealm().getDisplayName(); // 获取境界名称

        // === 1. 提前获取绘图对象和屏幕尺寸 ===
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        // === 2. 计算坐标 ===
        int healthBarX = screenWidth / 2 - 91;
        int healthBarY = screenHeight - 39;

        int barWidth = 81;
        int barHeight = 5;
        int margin = 2;

        int spiritBarX = healthBarX;
        int spiritBarY = healthBarY - barHeight - margin;

        // === 3. 计算进度比例 ===
        float ratio = maxSpirit > 0 ? Mth.clamp((float) currentSpirit / maxSpirit, 0f, 1f) : 0f;

        // === 4. 绘制灵力条背景、外框和进度 ===
        guiGraphics.fill(spiritBarX, spiritBarY,
                spiritBarX + barWidth, spiritBarY + barHeight, 0x00000000);
        guiGraphics.renderOutline(spiritBarX, spiritBarY,
                barWidth, barHeight, 0xFFFFFFFF);

        int fillWidth = (int) (barWidth * ratio);
        if (fillWidth > 0) {
            guiGraphics.fill(
                    spiritBarX + 1, spiritBarY + 1,
                    spiritBarX + 1 + fillWidth, spiritBarY + barHeight - 1,
                    0xFF00CCFF
            );
        }

        // === 5. 绘制文字（境界 + 灵力值） ===
        String text = realmName + " 灵力: " + currentSpirit + "/" + maxSpirit;
        int textWidth = mc.font.width(text);
        int textX = spiritBarX + (barWidth - textWidth) / 2;
        int textY = spiritBarY + (barHeight - mc.font.lineHeight) / 2 + 1;
        guiGraphics.drawString(mc.font, text, textX, textY, 0xFFFFFFFF, true);
    }
}