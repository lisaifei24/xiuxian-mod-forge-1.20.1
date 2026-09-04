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

/**
 * 突破丹药：可食用（恢复大量灵力），也可作为突破道具携带——
 * 突破时若背包中存在对应阶段的丹药，将自动消耗并大幅提升突破成功率（逻辑见 ModEvents）。
 */
public class BreakthroughPillItem extends Item {

    private final String stageName;      // 用于本地化的阶段提示
    private final int minRecovery;
    private final int maxRecovery;
    private final double successBonus;   // 作为突破道具时提供的成功率加成

    public BreakthroughPillItem(Properties properties, String stageName,
                                int minRecovery, int maxRecovery, double successBonus) {
        super(properties.food(new FoodProperties.Builder().alwaysEat().nutrition(0).saturationMod(0).build()));
        this.stageName = stageName;
        this.minRecovery = minRecovery;
        this.maxRecovery = maxRecovery;
        this.successBonus = successBonus;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32; // 1.6 秒，与普通食物一致
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                int recover = level.random.nextInt(maxRecovery - minRecovery + 1) + minRecovery;
                cap.addSpiritualPower(recover);
                XiuXianMod.LOGGER.info("玩家 {} 服用 {}，恢复灵力 {} 点", player.getName().getString(),
                        stack.getHoverName().getString(), recover);
                XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
                player.displayClientMessage(Component.literal("§b服下丹药，灵力恢复了 " + recover + " 点！"), true);
            });
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7突破阶段：" + stageName).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7食用可恢复大量灵力；").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7突破时如携带此丹药，自动消耗并提升 " + Math.round(successBonus * 100) + "% 成功率")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
