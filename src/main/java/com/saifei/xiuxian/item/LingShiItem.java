package com.saifei.xiuxian.item;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class LingShiItem extends Item {
    private final int minRecovery;
    private final int maxRecovery;

    // 构造函数：传入属性和恢复数值范围
    public LingShiItem(Properties properties, int minRecovery, int maxRecovery) {
        // 强制加上 "可以一直吃" 的食物属性（不吃饱也能吃），营养和饱和度设为0
        super(properties.food(new FoodProperties.Builder().alwaysEat().nutrition(0).saturationMod(0).build()));
        this.minRecovery = minRecovery;
        this.maxRecovery = maxRecovery;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                // 计算范围 [min, max] 内的随机整数
                int recover = level.random.nextInt(maxRecovery - minRecovery + 1) + minRecovery;

                // 调用 Capability 增加灵力 (需要 ICultivation 中定义 addSpiritualPower 方法)
                cap.addSpiritualPower(recover);

                XiuXianMod.LOGGER.info("玩家 {} 食用了 {}，恢复了 {} 点灵力", player.getName().getString(), stack.getHoverName().getString(), recover);

                // 食用后立刻向客户端同步新数据，更新 HUD
                XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
            });
        }
        return super.finishUsingItem(stack, level, entity);
    }
}