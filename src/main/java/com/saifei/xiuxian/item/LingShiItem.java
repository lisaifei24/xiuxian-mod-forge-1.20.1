package com.saifei.xiuxian.item;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class LingShiItem extends Item {
    private final int percent;
    private final String functionDesc;

    public LingShiItem(Properties properties, int percent, String functionDesc) {
        super(properties.food(new FoodProperties.Builder().alwaysEat().nutrition(0).saturationMod(0).build()));
        this.percent = percent;
        this.functionDesc = functionDesc;
    }

    // ✅【新增】让灵石拥有和食物一样的“长按并出现进度条”动画
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    // ✅【新增】设置长按持续时间。32 tick = 1.6 秒（和原版食物完全一致）
    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7食用可恢复当前灵力上限的 " + percent + "%").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7" + functionDesc).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7长按食用后可用于尝试突破境界（任意品级灵石均可突破）").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                int recover = cap.getMaxSpiritualPower() * percent / 100;
                if (recover < 1) recover = 1;
                cap.addSpiritualPower(recover);

                XiuXianMod.LOGGER.info("玩家 {} 食用了 {}，恢复了 {} 点灵力", player.getName().getString(), stack.getHoverName().getString(), recover);

                XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
            });
        }
        return super.finishUsingItem(stack, level, entity);
    }
}