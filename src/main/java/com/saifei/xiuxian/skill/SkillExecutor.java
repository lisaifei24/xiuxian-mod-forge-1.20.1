package com.saifei.xiuxian.skill;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.entity.FireballProjectile;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

/**
 * 技能执行器：在服务端校验法力 / 冷却 / 是否已学，并执行对应法术效果。
 */
public class SkillExecutor {

    private static final double MAX_RANGE = 32.0;

    public static void cast(ServerPlayer player, String requestedSkill) {
        if (player == null) return;
        player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
            // 确定要释放的技能：优先使用客户端请求的技能，否则代之以当前激活的功法
            String skillName = (requestedSkill == null || requestedSkill.isEmpty())
                    ? cap.getActiveSkill() : requestedSkill;
            SkillType skill = SkillType.byName(skillName);
            if (skill == null) {
                player.sendSystemMessage(Component.literal("§c你还未激活任何功法，请先使用功法卷轴学习！"));
                return;
            }
            if (!cap.hasLearnedSkill(skill.name())) {
                player.sendSystemMessage(Component.literal("§c你尚未学会功法【" + skill.getDisplayName() + "】，请先使用对应的功法卷轴！"));
                return;
            }
            long now = System.currentTimeMillis();
            long cooldownEnd = cap.getSkillCooldownEnd(skill.name());
            if (now < cooldownEnd) {
                double remain = (cooldownEnd - now) / 1000.0;
                player.sendSystemMessage(Component.literal("§c技能【" + skill.getDisplayName() + "】冷却中，剩余 " + String.format("%.1f", remain) + " 秒！"));
                return;
            }
            if (cap.getSpiritualPower() < skill.getSpiritualCost()) {
                player.sendSystemMessage(Component.literal("§c灵力不足！释放【" + skill.getDisplayName() + "】需要 " + skill.getSpiritualCost() + " 点灵力。"));
                return;
            }

            // 扣灵力、进入冷却并同步
            cap.addSpiritualPower(-skill.getSpiritualCost());
            cap.setSkillCooldownEnd(skill.name(), now + skill.getCooldownMillis());
            XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));

            applyEffect(player, skill);
            player.sendSystemMessage(Component.literal("§a你施展了功法【" + skill.getDisplayName() + "】，消耗灵力 " + skill.getSpiritualCost() + " 点！"));
        });
    }

    private static void applyEffect(ServerPlayer player, SkillType skill) {
        switch (skill.getKind()) {
            case MELEE_STRIKE -> performSwordStrike(player);
            case FIREBALL -> launchFireball(player);
            case SHIELD -> applyGoldenShield(player);
        }
    }

    // ============ 御剑术：直线斩击 ============
    private static void performSwordStrike(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(MAX_RANGE));

        LivingEntity target = null;
        double bestDistSq = MAX_RANGE * MAX_RANGE + 1;
        AABB search = player.getBoundingBox().expandTowards(look.scale(MAX_RANGE)).inflate(1.5);
        for (Entity e : serverLevel.getEntities(player, search)) {
            if (!(e instanceof LivingEntity living) || living == player) continue;
            AABB aabb = e.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = aabb.clip(eye, end);
            if (hit.isPresent()) {
                double distSq = eye.distanceToSqr(hit.get());
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    target = living;
                }
            }
        }

        // 剑气特效：无论是否命中都沿直线铺一层粒子
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX(), player.getY() + 1.0, player.getZ(),
                1, 0, 0, 0, 0.0);
        serverLevel.sendParticles(ParticleTypes.CRIT,
                eye.x + look.x * 2, eye.y + look.y * 2, eye.z + look.z * 2,
                12, 0.5, 0.5, 0.5, 0.1);

        if (target != null) {
            target.hurt(player.damageSources().playerAttack(player), 12.0F);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.5, 0.5, 0.5, 0.0);
            player.sendSystemMessage(Component.literal("§b御剑术命中目标：" + target.getName().getString() + "！"));
        } else {
            player.sendSystemMessage(Component.literal("§7御剑术落空了……"));
        }
    }

    // ============ 火球术：发射火球实体 ============
    private static void launchFireball(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition(1.0F);
        FireballProjectile fireball = new FireballProjectile(
                serverLevel, player,
                eye.x + look.x * 0.8, eye.y + look.y * 0.8 - 0.2, eye.z + look.z * 0.8,
                look, 1.2F);
        serverLevel.addFreshEntity(fireball);

        serverLevel.sendParticles(ParticleTypes.FLAME,
                player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
    }

    // ============ 护体金光：吸收 + 抗性护盾 ============
    private static void applyGoldenShield(ServerPlayer player) {
        // 4 点伤害吸收（2 颗心）+ 短暂抗性提升
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 3));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 1.0, 0.5, 0.05);
        }
    }
}
