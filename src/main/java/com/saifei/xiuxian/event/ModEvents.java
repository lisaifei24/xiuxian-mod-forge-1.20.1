package com.saifei.xiuxian.event;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.capability.ICultivation;
import com.saifei.xiuxian.capability.Realm;
import com.saifei.xiuxian.item.ModItems;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.text.DecimalFormat;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = XiuXianMod.MOD_ID , bus = Mod.EventBusSubscriber.Bus.FORGE)
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
            // 新会话开始时清理可能残留的渡劫状态（渡劫为在线状态，不跨会话）
            TribulationManager.clear(player);
        }
    }

    // 天劫系统：每个世界 tick 驱动渡劫推进（仅主世界执行一次）
    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && event.level.dimension() == Level.OVERWORLD
                && !event.level.isClientSide) {
            TribulationManager.tick();
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
            // 灵石品级：下=1、中=2、上=3、极=4；该值为对应阶段“保底必成功”所需的最低品级
            int guaranteedGrade = 0;
            double baseChance = 0.0;
            double failIncrement = 0.0;
            String stageKey = null;

            if (currentRealm == Realm.MORTAL) {
                targetRealm = Realm.QI_REFINING;
                guaranteedGrade = 2; // 中品
                baseChance = 0.5;
                failIncrement = 1.0 / 5.0;
                stageKey = "mortal_to_qi";
            } else if (currentRealm == Realm.QI_REFINING) {
                targetRealm = Realm.FOUNDATION;
                guaranteedGrade = 2; // 中品
                baseChance = 0.5;
                failIncrement = 1.0 / 20.0;
                stageKey = "qi_to_foundation";
            } else if (currentRealm == Realm.FOUNDATION) {
                targetRealm = Realm.GOLDEN_CORE;
                guaranteedGrade = 3; // 上品
                baseChance = 0.5;
                failIncrement = 1.0 / 10.0;
                stageKey = "foundation_to_golden";
            } else if (currentRealm == Realm.GOLDEN_CORE) {
                targetRealm = Realm.YUANYING;
                guaranteedGrade = 3; // 上品
                baseChance = 0.5;
                failIncrement = 1.0 / 12.0;
                stageKey = "golden_to_yuanying";
            } else if (currentRealm == Realm.YUANYING) {
                targetRealm = Realm.HUASHEN;
                guaranteedGrade = 4; // 极品
                baseChance = 0.5;
                failIncrement = 1.0 / 14.0;
                stageKey = "yuanying_to_huashen";
            } else if (currentRealm == Realm.HUASHEN) {
                targetRealm = Realm.HETI;
                guaranteedGrade = 4; // 极品
                baseChance = 0.5;
                failIncrement = 1.0 / 16.0;
                stageKey = "huashen_to_heti";
            } else if (currentRealm == Realm.HETI) {
                targetRealm = Realm.DACHENG;
                guaranteedGrade = 4; // 极品
                baseChance = 0.5;
                failIncrement = 1.0 / 18.0;
                stageKey = "heti_to_dacheng";
            } else if (currentRealm == Realm.DACHENG) {
                targetRealm = Realm.DUJIE;
                guaranteedGrade = 4; // 极品
                baseChance = 0.5;
                failIncrement = 1.0 / 20.0;
                stageKey = "dacheng_to_dujie";
            } else if (currentRealm == Realm.DUJIE) {
                targetRealm = Realm.ZHENXIAN;
                guaranteedGrade = 4; // 极品
                baseChance = 0.5;
                failIncrement = 1.0 / 25.0;
                stageKey = "dujie_to_zhenxian";
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

            // 取消“灵石品质必须匹配突破阶段”的门槛：任意品级灵石均可尝试突破任意境界；
            // 仅当所用灵石品级 >= 该阶段保底品级（保留原 中品/中品/上品 的保底设定）时才必定成功。
            int usedGrade = getLingShiGrade(stack.getItem());
            if (usedGrade <= 0) {
                player.sendSystemMessage(Component.literal("§c突破 " + targetRealm.getDisplayName() + " 需要使用灵石！"));
                return;
            }
            boolean isGuaranteed = usedGrade >= guaranteedGrade;

            if (!consumeOneItem(player, stack.getItem())) {
                player.sendSystemMessage(Component.literal("§c手中没有足够的灵石！"));
                return;
            }

            // 大境界突破（炼气→筑基 起，每次晋升均引动天劫；唯 凡人→炼气 不渡劫）
            boolean isBigBreakthrough = switch (targetRealm) {
                case FOUNDATION, GOLDEN_CORE, YUANYING, HUASHEN, HETI, DACHENG, DUJIE, ZHENXIAN -> true;
                default -> false;
            };

            CompoundTag persistentData = player.getPersistentData();
            int attempts = persistentData.getInt(stageKey);
            DecimalFormat df = new DecimalFormat("0.0%");

            // 【新增】携带对应境界突破丹药时，自动消耗并提升成功率
            double pillBonus = consumePillFor(currentRealm, player);

            if (isGuaranteed) {
                attempts = 0;
                persistentData.putInt(stageKey, attempts);
                player.sendSystemMessage(Component.literal("§a✨ 灵石品级足够，突破 " + targetRealm.getDisplayName() + " 必定成功！"));
                if (isBigBreakthrough) {
                    // 大境界：仍须渡劫
                    startTribulation(player, cap, targetRealm, pillBonus);
                } else {
                    performBreakthrough(player, cap, targetRealm);
                }
            } else {
                double currentChance = baseChance + attempts * failIncrement + pillBonus;
                if (currentChance > 1.0) currentChance = 1.0;
                double roll = player.getRandom().nextDouble();
                if (roll < currentChance) {
                    attempts = 0;
                    persistentData.putInt(stageKey, attempts);
                    player.sendSystemMessage(Component.literal("§a🎉 恭喜！突破 " + targetRealm.getDisplayName() + " 成功！"));
                    if (isBigBreakthrough) {
                        // 大境界：晋升前须渡劫
                        startTribulation(player, cap, targetRealm, pillBonus);
                    } else {
                        performBreakthrough(player, cap, targetRealm);
                    }
                } else {
                    attempts++;
                    persistentData.putInt(stageKey, attempts);
                    double nextChance = baseChance + attempts * failIncrement + pillBonus;
                    if (nextChance > 1.0) nextChance = 1.0;
                    // 【新增】突破失败惩罚：灵力清零 + 短暂虚弱/伤害
                    applyPunishment(player, cap);
                    player.sendSystemMessage(Component.literal("§c💥 突破失败！当前成功率 " + df.format(currentChance)
                            + "，下一次成功率提升至 " + df.format(nextChance)));
                    player.sendSystemMessage(Component.literal("§c灵力溃散已清零，且身心受创（虚弱），请恢复后再尝试！"));
                }
            }
        });
    }

    /** 大境界突破前先引入天劫，渡劫成功才能晋升 */
    private static void startTribulation(ServerPlayer player, ICultivation cap, Realm targetRealm, double pillBonus) {
        // 渡劫是最终考验：期间不修改境界，存活后才晋升
        TribulationManager.start(player, targetRealm, pillBonus);
        if (pillBonus > 0) {
            player.sendSystemMessage(Component.literal("§b丹药之力庇护着你，渡劫成功率已获得加成！"));
        }
    }

    /**
     * 突破失败惩罚：灵力清零 + 短暂虚弱 + 少量真实伤害。
     */
    public static void applyPunishment(ServerPlayer player, ICultivation cap) {
        cap.setSpiritualPower(0);
        XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
        // 10 秒虚弱（II），降低攻击与移速
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
        // 轻微反噬伤害，最低保留 1 血防止直接死亡
        if (player.getHealth() > 1.0F) {
            player.hurt(player.damageSources().magic(), 3.0F);
        }
    }

    /**
     * 根据当前境界从玩家背包消耗对应的突破丹药，返回其成功率加成（0~1）。
     * 聚气丹/筑基丹/结丹丹 分别对应 凡人→炼气、炼气→筑基、筑基→金丹。
     */
    private static double consumePillFor(Realm currentRealm, ServerPlayer player) {
        Item pill = switch (currentRealm) {
            case MORTAL -> ModItems.JUNQI_PILL.get();
            case QI_REFINING -> ModItems.ZHUJI_PILL.get();
            case FOUNDATION -> ModItems.JIEDAN_PILL.get();
            default -> null;
        };
        if (pill == null) return 0.0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == pill) {
                stack.shrink(1);
                return switch (currentRealm) {
                    case MORTAL -> 0.30;
                    case QI_REFINING -> 0.40;
                    case FOUNDATION -> 0.50;
                    default -> 0.0;
                };
            }
        }
        return 0.0;
    }

    private static void performBreakthrough(ServerPlayer player, com.saifei.xiuxian.capability.ICultivation cap, Realm targetRealm) {
        cap.setRealm(targetRealm);
        // ✅【新增】突破后灵力清零
        cap.setSpiritualPower(0);

        applyRealmAttributes(player, targetRealm);
        XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
    }

    public static void applyRealmAttributes(ServerPlayer player, Realm realm) {
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

    /**
     * 灵石品级：下品=1、中品=2、上品=3、极品=4；不是灵石则返回 0。
     */
    private static int getLingShiGrade(Item item) {
        if (item == ModItems.LOW_LINGSHI.get()) return 1;
        if (item == ModItems.MID_LINGSHI.get()) return 2;
        if (item == ModItems.HIGH_LINGSHI.get()) return 3;
        if (item == ModItems.SUPREME_LINGSHI.get()) return 4;
        return 0;
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

    // ========== 流浪商人：5:1兑换 ==========
    @SubscribeEvent
    public static void onWanderingTraderTrades(WandererTradesEvent event) {
        // 下品 -> 中品 (5:1 兑换)
        event.getGenericTrades().add((trader, rand) -> new MerchantOffer(
                new ItemStack(ModItems.LOW_LINGSHI.get(), 5), // 花费5下品
                new ItemStack(ModItems.MID_LINGSHI.get(), 1),    // 得到1中品
                12, 5, 0.05f // 使用12次，给5经验
        ));
    }
}