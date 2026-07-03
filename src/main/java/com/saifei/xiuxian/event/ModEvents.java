package com.saifei.xiuxian.event;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.capability.Realm;
import com.saifei.xiuxian.item.ModItems;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.text.DecimalFormat;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = XiuXianMod.MOD_ID)
public class ModEvents {

    private static final UUID ATTACK_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID ARMOR_UUID = UUID.fromString("2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e");
    private static final UUID SPEED_UUID = UUID.fromString("3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f");

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                applyRealmAttributes(player, cap.getRealm());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                applyRealmAttributes(player, cap.getRealm());
            });
        }
    }

    // ✅ 【新增】长按灵石直到动作完成（1.6秒）后，才触发突破判定
    @SubscribeEvent
    public static void onItemUseFinish(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();

        // 只有使用完的是灵石，才进行突破判定
        if (isLingShi(stack.getItem())) {
            handleBreakthrough(player, stack);
            // 因为长按消耗了1颗灵石用于突破，但你可能想在突破成功/失败后额外扣除，
            // 注意：你的 handleBreakthrough 方法内部已经调用了 consumeOneItem 扣除灵石。
        }
    }

    private static void handleBreakthrough(ServerPlayer player, ItemStack stack) {
        player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
            Realm currentRealm = cap.getRealm();
            Realm targetRealm = null;
            Item requiredItem = null;
            Item guaranteedItem = null;
            double baseChance = 0.0;
            double failIncrement = 0.0;
            String stageKey = null;

            if (currentRealm == Realm.MORTAL) {
                targetRealm = Realm.QI_REFINING;
                requiredItem = ModItems.LOW_LINGSHI.get();
                guaranteedItem = ModItems.MID_LINGSHI.get();
                baseChance = 0.5;
                failIncrement = 1.0 / 5.0;
                stageKey = "mortal_to_qi";
            } else if (currentRealm == Realm.QI_REFINING) {
                targetRealm = Realm.FOUNDATION;
                requiredItem = ModItems.LOW_LINGSHI.get();
                guaranteedItem = ModItems.MID_LINGSHI.get();
                baseChance = 0.5;
                failIncrement = 1.0 / 20.0;
                stageKey = "qi_to_foundation";
            } else if (currentRealm == Realm.FOUNDATION) {
                targetRealm = Realm.GOLDEN_CORE;
                requiredItem = ModItems.MID_LINGSHI.get();
                guaranteedItem = ModItems.HIGH_LINGSHI.get();
                baseChance = 0.5;
                failIncrement = 1.0 / 10.0;
                stageKey = "foundation_to_golden";
            }

            if (targetRealm == null) {
                player.sendSystemMessage(Component.literal("§c已达最高境界，无法继续突破！"));
                return;
            }

            // ✅【新增】突破前必须满足灵力 100%
            if (cap.getSpiritualPower() < cap.getMaxSpiritualPower()) {
                player.sendSystemMessage(Component.literal("§c你的灵力尚未圆满，请先将灵力恢复到 100% 再进行突破！"));
                return;
            }

            Item currentItem = stack.getItem();
            boolean isGuaranteed = (currentItem == guaranteedItem);
            boolean isCorrectItem = (currentItem == requiredItem);

            if (!isCorrectItem && !isGuaranteed) {
                player.sendSystemMessage(Component.literal("§c突破 " + targetRealm.getDisplayName() + " 需要使用正确的灵石！"));
                return;
            }

            if (!consumeOneItem(player, currentItem)) {
                player.sendSystemMessage(Component.literal("§c手中没有足够的灵石！"));
                return;
            }

            CompoundTag persistentData = player.getPersistentData();
            int attempts = persistentData.getInt(stageKey);
            DecimalFormat df = new DecimalFormat("0.0%");

            if (isGuaranteed) {
                attempts = 0;
                persistentData.putInt(stageKey, attempts);
                performBreakthrough(player, cap, targetRealm);
                player.sendSystemMessage(Component.literal("§a✨ 你使用了高品灵石，突破 " + targetRealm.getDisplayName() + " 必定成功！"));
            } else {
                double currentChance = baseChance + attempts * failIncrement;
                if (currentChance > 1.0) currentChance = 1.0;
                double roll = player.getRandom().nextDouble();
                if (roll < currentChance) {
                    attempts = 0;
                    persistentData.putInt(stageKey, attempts);
                    performBreakthrough(player, cap, targetRealm);
                    player.sendSystemMessage(Component.literal("§a🎉 恭喜！你成功突破至 " + targetRealm.getDisplayName() + "！"));
                } else {
                    attempts++;
                    persistentData.putInt(stageKey, attempts);
                    double nextChance = baseChance + attempts * failIncrement;
                    if (nextChance > 1.0) nextChance = 1.0;
                    player.sendSystemMessage(Component.literal("§c💥 突破失败！当前成功率 " + df.format(currentChance) + "，下一次成功率提升至 " + df.format(nextChance)));
                }
            }
        });
    }

    private static void performBreakthrough(ServerPlayer player, com.saifei.xiuxian.capability.ICultivation cap, Realm targetRealm) {
        cap.setRealm(targetRealm);
        // ✅【新增】突破后灵力清零
        cap.setSpiritualPower(0);

        applyRealmAttributes(player, targetRealm);
        XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
    }

    private static void applyRealmAttributes(ServerPlayer player, Realm realm) {
        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attackAttr != null) {
            attackAttr.removeModifier(ATTACK_UUID);
            attackAttr.addTransientModifier(new AttributeModifier(ATTACK_UUID, "realm_attack", realm.getAttackBonus(), AttributeModifier.Operation.ADDITION));
        }
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_UUID);
            armorAttr.addTransientModifier(new AttributeModifier(ARMOR_UUID, "realm_armor", realm.getArmorBonus(), AttributeModifier.Operation.ADDITION));
        }
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_UUID);
            if (realm.getSpeedBonus() != 0) {
                speedAttr.addTransientModifier(new AttributeModifier(SPEED_UUID, "realm_speed", realm.getSpeedBonus(), AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
        player.setHealth(player.getHealth());
    }

    private static boolean isLingShi(Item item) {
        return item == ModItems.LOW_LINGSHI.get() || item == ModItems.MID_LINGSHI.get() ||
                item == ModItems.HIGH_LINGSHI.get() || item == ModItems.SUPREME_LINGSHI.get();
    }
    private static boolean consumeOneItem(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item && !stack.isEmpty()) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}