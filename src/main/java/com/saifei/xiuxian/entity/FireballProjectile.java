package com.saifei.xiuxian.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 火球术生成的灵力火球实体（抛射物）。
 * 无贴图无法渲染，因此客户端使用空渲染器 + 服务器广播火焰粒子特效来表现。
 */
public class FireballProjectile extends ThrowableProjectile {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(FireballProjectile.class, EntityDataSerializers.ITEM_STACK);

    /** 火球造成的伤害 */
    private static final float BASE_DAMAGE = 8.0F;
    /** 燃烧秒数 */
    private static final int FIRE_SECONDS = 5;

    public FireballProjectile(EntityType<? extends FireballProjectile> type, Level level) {
        super(type, level);
    }

    public FireballProjectile(Level level, LivingEntity owner, double x, double y, double z, Vec3 direction, float speed) {
        super(ModEntities.FIREBALL_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.setPos(x, y, z);
        this.shoot(direction.x, direction.y, direction.z, speed, 0.2F);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    /** 匀速直线飞行，不带重力 */
    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        // 服务器端广播火焰粒子，让客户端看到飞行轨迹
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (this.tickCount % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(), 1,
                        0.0, 0.0, 0.0, 0.02);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(), 1,
                        0.0, 0.0, 0.0, 0.01);
            }
        }
        // 存活时间过长自动消散
        if (this.tickCount > 120) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            // 击中的位置产生爆炸粒子
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX(), this.getY(), this.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(), 20, 0.8, 0.8, 0.8, 0.05);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target instanceof LivingEntity living) {
            // 1.20.1: mobProjectile(Entity, LivingEntity) 第二参必须是 LivingEntity（发射者可全空）
            LivingEntity attackSourceOwner = owner instanceof LivingEntity livingOwner ? livingOwner : null;
            DamageSource source = this.damageSources().mobProjectile(this, attackSourceOwner);
            if (living.hurt(source, BASE_DAMAGE)) {
                living.setSecondsOnFire(FIRE_SECONDS);
            }
        }
    }

    // 需要实现受击反作用的伤害免疫基本逻辑
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }
}
