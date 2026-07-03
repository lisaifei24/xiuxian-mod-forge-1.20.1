package com.saifei.xiuxian.item;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class LingShiItem extends Item {
    private final int minRecovery;
    private final int maxRecovery;

    public LingShiItem(Properties properties, int minRecovery, int maxRecovery) {
        super(properties.food(new FoodProperties.Builder().alwaysEat().nutrition(0).saturationMod(0).build()));
        this.minRecovery = minRecovery;
        this.maxRecovery = maxRecovery;
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                int recover = level.random.nextInt(maxRecovery - minRecovery + 1) + minRecovery;
                cap.addSpiritualPower(recover);

                XiuXianMod.LOGGER.info("玩家 {} 食用了 {}，恢复了 {} 点灵力", player.getName().getString(), stack.getHoverName().getString(), recover);

                XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
            });
        }
        return super.finishUsingItem(stack, level, entity);
    }
}