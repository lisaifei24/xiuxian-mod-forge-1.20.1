package com.saifei.xiuxian.event;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.capability.ICultivation;
import com.saifei.xiuxian.capability.Realm;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 天劫系统（渡劫）：
 * - 突破大境界（炼气→筑基、筑基→金丹）时自动进入渡劫状态。
 * - 每隔一段时间劈下一道天雷，给予玩家明确的阶段提示（中文）。
 * - 玩家需在渡劫期间存活（连续多道雷），全部挺过才算突破成功并晋升境界。
 * - 若玩家渡劫中死亡或离线，视为渡劫失败，施加惩罚。
 *
 * 实现为静态状态管理器 + tick 驱动（由 ModEvents 的 WorldTickEvent 调用）。
 */
public class TribulationManager {

    /** 每道天雷之间的间隔（tick） */
    private static final int STRIKE_INTERVAL = 60;

    /** 各境界需要的天雷数量 */
    private static final int STRIKES_FOUNDATION = 5;
    private static final int STRIKES_GOLDEN_CORE = 7;

    /** <玩家UUID, 渡劫会话> */
    private static final Map<UUID, TribulationSession> SESSIONS = new HashMap<>();

    /** 渡劫会话（玩家在线期间的单次状态） */
    private static class TribulationSession {
        final ServerPlayer player;
        final Realm targetRealm;
        final int totalStrikes;
        int strikesPassed = 0;
        int ticksToNextStrike = STRIKE_INTERVAL;
        final double bonus;

        TribulationSession(ServerPlayer player, Realm targetRealm, int totalStrikes, double bonus) {
            this.player = player;
            this.targetRealm = targetRealm;
            this.totalStrikes = totalStrikes;
            this.bonus = bonus;
        }
    }

    /**
     * 开始渡劫。返回是否成功开启（若已处于渡劫中则返回 false）。
     */
    public static boolean start(ServerPlayer player, Realm targetRealm, double successBonus) {
        if (SESSIONS.containsKey(player.getUUID())) {
            return false;
        }
        int strikes = switch (targetRealm) {
            case FOUNDATION -> STRIKES_FOUNDATION;
            case GOLDEN_CORE -> STRIKES_GOLDEN_CORE;
            default -> 0; // 小境界/凡人阶段不触发天劫
        };
        if (strikes <= 0) {
            return false; // 该突破不触发天劫，直接由调用方晋升
        }
        SESSIONS.put(player.getUUID(), new TribulationSession(player, targetRealm, strikes, successBonus));

        player.sendSystemMessage(Component.literal("§5§l⚡ 天劫降临！"));
        player.sendSystemMessage(Component.literal("§d你正在尝试突破【" + targetRealm.getDisplayName() + "】，引动了天道雷劫！"));
        player.sendSystemMessage(Component.literal("§e共 " + strikes + " 道天雷，请坚持活下去！渡劫成功将晋升境界并重获大量灵力！"));
        player.sendSystemMessage(Component.literal("§7第一道天雷即将降临……"));
        XiuXianMod.LOGGER.info("玩家 {} 开始渡劫（{}），共 {} 道天雷", player.getName().getString(),
                targetRealm.getDisplayName(), strikes);
        return true;
    }

    /** 每 tick 驱动渡劫进行。由 ModEvents#onServerTick（WorldTickEvent）调用。 */
    public static void tick() {
        if (SESSIONS.isEmpty()) return;
        SESSIONS.entrySet().removeIf(entry -> {
            TribulationSession session = entry.getValue();
            ServerPlayer player = session.player;
            ServerLevel level = player.level() instanceof ServerLevel sl ? sl : null;

            // 玩家离线或死亡（不再处于存活且在线状态），判定渡劫失败
            boolean loaded = level != null && level.getPlayerByUUID(player.getUUID()) == player;
            if (!loaded || !player.isAlive()) {
                fail(player);
                return true;
            }

            session.ticksToNextStrike--;
            if (session.ticksToNextStrike <= 0) {
                session.ticksToNextStrike = STRIKE_INTERVAL;
                session.strikesPassed++;
                strike(player, session);
                if (session.strikesPassed >= session.totalStrikes) {
                    succeed(player, session);
                    return true;
                }
            }
            return false;
        });
    }

    /** 劈下当前这道天雷（命中率 70%，落空则提示） */
    private static void strike(ServerPlayer player, TribulationSession session) {
        ServerLevel level = (ServerLevel) player.level();
        int num = session.strikesPassed;
        int total = session.totalStrikes;
        player.sendSystemMessage(Component.literal(
                "§c§l⚡ 第 §e" + num + " §c§l道天雷劈下！（" + num + "/" + total + "）"));

        boolean hit = player.getRandom().nextDouble() < 0.70;
        if (hit) {
            BlockPos pos = player.blockPosition();
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                bolt.setVisualOnly(false); // 真实伤害
                level.addFreshEntity(bolt);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        20, 0.5, 0.5, 0.5, 0.1);
            }
            if (player.isAlive()) {
                player.sendSystemMessage(Component.literal("§e天雷击中了你！好在灵力护体，撑过了这一道！"));
            }
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal("§a这一道天雷劈偏了，你险险避开！"));
        }
    }

    /** 渡劫成功：晋升境界、重新充满灵力并同步 */
    private static void succeed(ServerPlayer player, TribulationSession session) {
        player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
            Realm target = session.targetRealm;
            ModEvents.applyRealmAttributes(player, target);
            cap.setRealm(target);
            cap.setSpiritualPower(cap.getMaxSpiritualPower()); // 渡劫成功，灵气灌体，灵力回满
            XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
            player.sendSystemMessage(Component.literal("§b§l=== 渡劫成功，修为大涨！ ==="));
            player.sendSystemMessage(Component.literal("§a你挺过了天道雷劫，成功晋升【" + target.getDisplayName() + "】！"));
            player.sendSystemMessage(Component.literal("§d灵力已完全恢复，攻击、护甲与移速全面提升！"));
            XiuXianMod.LOGGER.info("玩家 {} 渡劫成功，晋升 {}，灵力回满", player.getName().getString(), target.getDisplayName());
        });
    }

    /** 渡劫失败：保留原境界、附加失败惩罚（灵力清零 + 短时间虚弱/伤害） */
    private static void fail(ServerPlayer player) {
        if (!player.isAlive()) {
            // 玩家已死：死亡本身就是代价，不再重复惩罚
            if (player.connection != null) {
                player.sendSystemMessage(Component.literal("§c§l=== 渡劫失败！你倒在了天雷之下…… ==="));
            }
            XiuXianMod.LOGGER.info("玩家 {} 渡劫失败（死亡）", player.getName().getString());
            return;
        }
        player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
            ModEvents.applyPunishment(player, cap);
        });
        if (player.connection != null) {
            player.sendSystemMessage(Component.literal("§c§l=== 渡劫失败！ ==="));
            player.sendSystemMessage(Component.literal("§c突破失败，灵力溃散，身心遭受重创！"));
        }
        XiuXianMod.LOGGER.info("玩家 {} 渡劫失败，受到惩罚", player.getName().getString());
    }

    /** 玩家是否正在渡劫 */
    public static boolean isUnderTribulation(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    /** 调试/安全接口：强制清除某玩家的渡劫状态（如服务器重启自动清空） */
    public static void clear(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
    }
}
