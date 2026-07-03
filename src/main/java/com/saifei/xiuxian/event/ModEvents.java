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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber(modid = XiuXianMod.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack stack = event.getItemStack();

        // 只要手持任意灵石，就触发突破判定
        if (isLingShi(stack.getItem())) {
            handleBreakthrough(player, stack);
            // 强制刷新玩家背包（因为消耗了灵石）
            player.inventoryMenu.broadcastChanges();
        }
    }

    // 核心突破逻辑
    private static void handleBreakthrough(ServerPlayer player, ItemStack stack) {
        player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
            Realm currentRealm = cap.getRealm();
            Realm targetRealm = null;
            Item requiredItem = null;
            Item guaranteedItem = null;
            double baseChance = 0.0;
            double failIncrement = 0.0;
            String stageKey = null;

            // ================= 设定突破参数 =================
            if (currentRealm == Realm.MORTAL) {
                targetRealm = Realm.QI_REFINING;
                requiredItem = ModItems.LOW_LINGSHI.get();    // 下品
                guaranteedItem = ModItems.MID_LINGSHI.get();   // 中品（必定成功）
                baseChance = 0.5;       // 基础 50%
                failIncrement = 1.0 / 5.0; // 每失败一次 +20%
                stageKey = "mortal_to_qi";
            }
            else if (currentRealm == Realm.QI_REFINING) {
                targetRealm = Realm.FOUNDATION;
                requiredItem = ModItems.LOW_LINGSHI.get();    // 下品
                guaranteedItem = ModItems.MID_LINGSHI.get();   // 中品（必定成功）
                baseChance = 0.5;       // 基础 50%
                failIncrement = 1.0 / 20.0; // 每失败一次 +5%
                stageKey = "qi_to_foundation";
            }
            else if (currentRealm == Realm.FOUNDATION) {
                targetRealm = Realm.GOLDEN_CORE;
                requiredItem = ModItems.MID_LINGSHI.get();    // 中品
                guaranteedItem = ModItems.HIGH_LINGSHI.get();  // 上品（必定成功）
                baseChance = 0.5;       // 基础 50%
                failIncrement = 1.0 / 10.0; // 每失败一次 +10%
                stageKey = "foundation_to_golden";
            }

            if (targetRealm == null) {
                player.sendSystemMessage(Component.literal("§c已达最高境界，无法继续突破！"));
                return;
            }

            Item currentItem = stack.getItem();

            // 1. 检查是否使用了“必定成功”的高级灵石
            boolean isGuaranteed = (currentItem == guaranteedItem);

            // 2. 检查是否使用了正确的突破灵石
            boolean isCorrectItem = (currentItem == requiredItem);

            if (!isCorrectItem && !isGuaranteed) {
                player.sendSystemMessage(Component.literal("§c突破 " + targetRealm.getDisplayName() + " 需要使用正确的灵石！"));
                return;
            }

            // 3. 消耗 1 颗灵石用于本次尝试
            if (!consumeOneItem(player, currentItem)) {
                player.sendSystemMessage(Component.literal("§c手中没有足够的灵石！"));
                return;
            }

            // 4. 读取 NBT 中的失败次数记录
            CompoundTag persistentData = player.getPersistentData();
            int attempts = persistentData.getInt(stageKey);

            DecimalFormat df = new DecimalFormat("0.0%");

            if (isGuaranteed) {
                // --- 必定成功分支 ---
                attempts = 0; // 成功后重置失败计数器
                persistentData.putInt(stageKey, attempts);
                performBreakthrough(player, cap, targetRealm);
                player.sendSystemMessage(Component.literal("§a✨ 你使用了高品质灵石，天地灵气汇聚，突破 " + targetRealm.getDisplayName() + " 必定成功！"));
            } else {
                // --- 随机突破分支 ---
                double currentChance = baseChance + attempts * failIncrement;
                // 防止超出 100%
                if (currentChance > 1.0) currentChance = 1.0;

                // 触发随机
                double roll = player.getRandom().nextDouble();
                if (roll < currentChance) {
                    // 突破成功！
                    attempts = 0; // 成功后重置失败计数器
                    persistentData.putInt(stageKey, attempts);
                    performBreakthrough(player, cap, targetRealm);
                    player.sendSystemMessage(Component.literal("§a🎉 恭喜！你成功突破至 " + targetRealm.getDisplayName() + "！"));
                } else {
                    // 突破失败，记录失败次数
                    attempts++;
                    persistentData.putInt(stageKey, attempts);
                    // 计算下一次的概率
                    double nextChance = baseChance + attempts * failIncrement;
                    if (nextChance > 1.0) nextChance = 1.0;

                    player.sendSystemMessage(Component.literal("§c💥 突破失败！当前成功率 " + df.format(currentChance) + "，下一次成功率提升至 " + df.format(nextChance)));
                }
            }
        });
    }

    // 执行突破（更新境界并同步数据）
    private static void performBreakthrough(ServerPlayer player, com.saifei.xiuxian.capability.ICultivation cap, Realm targetRealm) {
        cap.setRealm(targetRealm);
        // 同步给客户端
        XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
    }

    // 辅助：检测是否为灵石
    private static boolean isLingShi(Item item) {
        return item == ModItems.LOW_LINGSHI.get() || item == ModItems.MID_LINGSHI.get() ||
                item == ModItems.HIGH_LINGSHI.get() || item == ModItems.SUPREME_LINGSHI.get();
    }

    // 辅助：消耗玩家背包里的 1 个物品
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